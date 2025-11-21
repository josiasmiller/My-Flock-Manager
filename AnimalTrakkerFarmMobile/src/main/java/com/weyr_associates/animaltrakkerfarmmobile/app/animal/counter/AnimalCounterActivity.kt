package com.weyr_associates.animaltrakkerfarmmobile.app.animal.counter

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.device.DeviceConnectionState.Companion.fromStateString
import com.weyr_associates.animaltrakkerfarmmobile.app.device.DeviceConnectionStatePresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.device.eid.EIDReaderService
import com.weyr_associates.animaltrakkerfarmmobile.app.permissions.RequiredPermissionsWatcher
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.DefaultSettingsTable.readAsMap
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ActivityAnimalCounterBinding

class AnimalCounterActivity : AppCompatActivity() {
    var numAnimals: Int = 0

    var animaltrakker_defaults: Map<String, Int>? = null

    var LastEID: String? = null
    var FileName: String? = null

    var mService: Messenger? = null
    var mIsBound: Boolean = false

    val mMessenger: Messenger = Messenger(IncomingHandler())

    private val requiredPermissionsWatcher = RequiredPermissionsWatcher(this)

    private val binding: ActivityAnimalCounterBinding by lazy {
        ActivityAnimalCounterBinding.inflate(layoutInflater)
    }

    private lateinit var eidReaderConnectionStatePresenter: DeviceConnectionStatePresenter

    internal inner class IncomingHandler : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                EIDReaderService.MSG_UPDATE_STATUS -> {
                    val b1 = msg.data
                    val state = b1.getString("stat")
                    if (state != null) {
                        eidReaderConnectionStatePresenter.connectionState = fromStateString(state)
                    }
                }

                EIDReaderService.MSG_NEW_EID_FOUND -> {
                    val b2 = msg.data

                    LastEID = (b2.getString("info1"))
                    FileName = (b2.getString("stat"))
                    //We have a good whole EID number
                    //           Log.i ("Counter" , "got eid of " + LastEID);
                    gotEID()
                }

                EIDReaderService.MSG_UPDATE_LOG_APPEND -> {}
                EIDReaderService.MSG_UPDATE_LOG_FULL -> {}
                EIDReaderService.MSG_THREAD_SUICIDE -> {
                    Log.i("Counter", "Service informed Activity of Suicide.")
                    doUnbindService()
                    stopService(Intent(this@AnimalCounterActivity, EIDReaderService::class.java))
                }

                else -> super.handleMessage(msg)
            }
        }
    }

    var mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            mService = Messenger(service)
            Log.i("Counter", "At Service Connected")
            try {
                //Register client with service
                val msg = Message.obtain(null, EIDReaderService.MSG_REGISTER_CLIENT)
                msg.replyTo = mMessenger
                mService!!.send(msg)
            } catch (e: RemoteException) {
                // In this case the service has crashed before we could even do anything with it
            }
        }

        override fun onServiceDisconnected(className: ComponentName) {
            // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
            mService = null
        }
    }

    private fun checkIfServiceIsRunning() {
        //If the service is running when the activity starts, we want to automatically bind to it.
        Log.i("Counter", "At isRunning?.")
        if (EIDReaderService.isRunning) {
            //Log.i("Counter", "is.");
            doBindService()
        } else {
            //Log.i("Counter", "is not, start it");
            startService(Intent(this@AnimalCounterActivity, EIDReaderService::class.java))
            doBindService()
        }
    }

    fun doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        //Log.i("Counter", "At doBind1.");
        bindService(Intent(this, EIDReaderService::class.java), mConnection, BIND_AUTO_CREATE)

        //Log.i("Counter", "At doBind2.");
        mIsBound = true

        if (mService != null) {
            //Log.i("Counter", "At doBind3.");
            try {
                //Request reload preferences
                var msg = Message.obtain(null, EIDReaderService.MSG_RELOAD_PREFERENCES)
                msg.replyTo = mMessenger
                mService!!.send(msg)
                //Request status update
                msg = Message.obtain(null, EIDReaderService.MSG_UPDATE_STATUS, 0, 0)
                msg.replyTo = mMessenger
                mService!!.send(msg)
                //         Log.i("Counter", "At doBind4.");
                //Request full log from service.
                msg = Message.obtain(null, EIDReaderService.MSG_UPDATE_LOG_FULL, 0, 0)
                mService!!.send(msg)
            } catch (e: RemoteException) { /* NO-OP */
            }
        }
    }

    fun doUnbindService() {
        //Log.i("Counter", "At DoUnbindservice");
        if (mService != null) {
            try {
                //Stop eidService from sending tags
                val msg = Message.obtain(null, EIDReaderService.MSG_NO_COUNTER_TAGS_PLEASE)
                msg.replyTo = mMessenger
                mService!!.send(msg)
            } catch (e: RemoteException) {
                // In this case the service has crashed before we could even do anything with it
            }
        }
        if (mIsBound) {
            // If we have received the service, and hence registered with it, then now is the time to unregister.
            if (mService != null) {
                try {
                    val msg = Message.obtain(null, EIDReaderService.MSG_UNREGISTER_CLIENT)
                    msg.replyTo = mMessenger
                    mService!!.send(msg)
                } catch (e: RemoteException) {
                    // There is nothing special we need to do if the service has crashed.
                }
            }
            // Detach our existing connection.
            unbindService(mConnection)
            mIsBound = false
        }
    }

    fun gotEID() {
        binding.tagNumberEID.text = LastEID
        Log.i("Counter", " with LastEID of $LastEID")
        numAnimals = numAnimals + 1
        binding.numAnimals.text = numAnimals.toString()
        binding.fileName.text = FileName

        //  Set Stop Counter button Green
        binding.btnStopCounter.isEnabled = true
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        Log.i("Counter", " after set content view")

        // Get the defaults and fill the defaults list for use later.
        animaltrakker_defaults = readAsMap(this)

        checkIfServiceIsRunning()
        Log.i("Counter", "back from isRunning")

        var btn = binding.btnStartCounter
        btn.setOnClickListener { v: View -> this.startCounter(v) }

        btn = binding.btnStopCounter
        btn.setOnClickListener { v: View -> this.stopCounter(v) }
        btn.isEnabled = false

        eidReaderConnectionStatePresenter =
            DeviceConnectionStatePresenter(this, binding.imageScannerStatus)

        lifecycle.addObserver(requiredPermissionsWatcher)
    }

    //  user clicked 'Start Counter' button
    private fun startCounter(v: View) {
        // Here is where scanning starts and tags scanned are put the data into the variable LastEID
        if (mService != null) {
            try {
                //Request reload preferences
                var msg = Message.obtain(null, EIDReaderService.MSG_RELOAD_PREFERENCES)
                msg.replyTo = mMessenger
                mService!!.send(msg)
                //Start eidService sending tags
                msg = Message.obtain(null, EIDReaderService.MSG_SEND_ME_COUNTER_TAGS)
                msg.replyTo = mMessenger
                mService!!.send(msg)
                numAnimals = 0
                binding.numAnimals.text = numAnimals.toString()
                binding.fileName.text = ""
                binding.tagNumberEID.text = ""
                //	make the buttons green
                binding.btnStartCounter.setText(R.string.btn_reset_counter)
                binding.btnStopCounter.isEnabled = true
            } catch (e: RemoteException) {
                // In this case the service has crashed before we could even do anything with it
            }
        }
    }

    //  user clicked 'Stop Counter' button
    private fun stopCounter(v: View) {
        if (mService != null) {
            try {
                //Stop eidService sending tags
                val msg = Message.obtain(null, EIDReaderService.MSG_NO_COUNTER_TAGS_PLEASE)
                msg.replyTo = mMessenger
                mService!!.send(msg)
                binding.btnStartCounter.setText(R.string.btn_start_counter)
                binding.btnStopCounter.isEnabled = false
            } catch (e: RemoteException) {
                // In this case the service has crashed before we could even do anything with it
            }
        }
        Log.i("Counter", " Stop Counter")
    }

    public override fun onResume() {
        super.onResume()
        checkIfServiceIsRunning()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.repeatCount == 0) {
            Log.i("Counter", "Back clicked ")
            finish()
        }
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        doUnbindService()
    }
}
