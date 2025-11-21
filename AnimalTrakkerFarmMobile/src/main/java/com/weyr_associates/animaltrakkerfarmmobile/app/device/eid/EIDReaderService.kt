package com.weyr_associates.animaltrakkerfarmmobile.app.device.eid

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import com.handheldgroup.serialport.SerialPort
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.files.AppDirectories.eidTagsDirectory
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.files.AppDirectories.raceTagsDirectory
import com.weyr_associates.animaltrakkerfarmmobile.BuildConfig
import com.weyr_associates.animaltrakkerfarmmobile.app.device.DeviceConnectionState
import com.weyr_associates.animaltrakkerfarmmobile.app.device.DeviceService
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.reflect.Method
import java.security.InvalidParameterException
import java.util.Calendar
import java.util.LinkedList
import java.util.Timer
import java.util.TimerTask
import java.util.UUID

class EIDReaderService : Service(), OnSharedPreferenceChangeListener {

    private enum class ScanAction {
        NONE,
        SCAN_SINGLE,
        SCAN_COUNTER
    }

    private var mSerialPort: SerialPort? = null
    private var mOutputStream: OutputStream? = null
    private var mInputStream: InputStream? = null
    private var mReadThread: ReadThread? = null

    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mBTConnectThread: BTConnectThread? = null
    private var mBTConnectedThread: BTConnectedThread? = null

    // Bluetooth Reader Stuff

    @get:Synchronized
    @set:Synchronized
    private var bTState: Int = 0

    private var scanAction = ScanAction.NONE

    private val window = LinkedList<String>()
    private val dedupSet = HashSet<String>()

    private var useSerialEID = false
    private var shouldDuplicate = false
    private val timer: Timer = Timer()
    private var logmsgs = ""
    private var displayMsgType = 0

    // Keeps track of all current registered clients.
    private var mClients: ArrayList<Messenger> = ArrayList()

    // to pass the last eid stuff for passing to do sheep task
    //	public final static String LASTEID = "com.weyr_associates.lambtracker.LASTEID";
    //Target we publish for clients to send messages to IncomingHandler.
    private val messenger: Messenger = Messenger(IncomingHandler())

    private var macAddress: String? = "00:00:00:00:00:00"
    private var useHTCConnectionWorkaround = false
    private var eidDataToSave = ""
    private var eid = ""
    private var lastEID = "zzz"
    private var completeline = false

    private var ticksSinceLastStatusSent = 0
    private val byteReceivedBackSemaphore: Any = Any()

    private lateinit var preferences: SharedPreferences

    override fun onBind(intent: Intent): IBinder? {
        return messenger.binder
    }

    internal inner class IncomingHandler : Handler() {
        // handler of incoming messages from clients
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_REGISTER_CLIENT -> mClients.add(msg.replyTo)
                MSG_UNREGISTER_CLIENT -> mClients.remove(msg.replyTo)
                MSG_UPDATE_STATUS -> sendStatusMessageToUI() // Client requested a status update
                MSG_RELOAD_PREFERENCES -> loadPreferences() // Client requested that the service reload the shared preferences
                MSG_UPDATE_LOG_FULL -> {
                    sendAllLogMessagesToUI() // Client requested all of the log messages.
                    if (!isRunning) {
                        informActivityOfThreadSuicide()
                    }
                }

                MSG_TOGGLE_LOG_TYPE -> if (displayMsgType == 0) {
                    setDisplayMsgType(1)
                } else {
                    setDisplayMsgType(0)
                }

                MSG_SEND_ME_TAGS -> {
                    scanAction = ScanAction.SCAN_SINGLE
                    if (BuildConfig.EID_READER_SERVICE_AUTO_POST_TEST_EID_ON_SCAN) {
                        lastEID = TEST_EID
                        sendNewEIDMessageToUI()
                    }
                }

                MSG_NO_TAGS_PLEASE -> {
                    scanAction = ScanAction.NONE
                }

                MSG_SEND_ME_COUNTER_TAGS -> {
                    scanAction = ScanAction.SCAN_COUNTER
                }

                MSG_NO_COUNTER_TAGS_PLEASE -> {
                    scanAction = ScanAction.NONE
                    clearDupSet() // Clear the dup set and window
                    updateFileNameMod()
                }

                else -> super.handleMessage(msg)
            }
        }
    }

    private fun updateFileNameMod() {
        fileNameMod = preferences.getString("filenamemod", "0")
        val editor = preferences.edit()
        var mod = preferences.getString("filenamemod", "0")!!.toInt()
        mod = mod + 1
        editor.putString("filenamemod", mod.toString())
        editor.apply()
    }

    private fun clearDupSet() {
        window.clear()
        dedupSet.clear()
    }

    private inner class ReadThread : Thread() {
        override fun run() {
            super.run()
            while (!isInterrupted) {
                var size: Int
                try {
                    val buffer = ByteArray(64)
                    if (mInputStream == null) return
                    size = mInputStream!!.read(buffer)
                    if (size > 0) {
                        onDataReceived(buffer, size)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    return
                }
            }
        }
    }

    private fun informActivityOfThreadSuicide() {
        for (i in mClients.indices.reversed()) {
            try {
                mClients[i].send(Message.obtain(null, MSG_THREAD_SUICIDE, 0, 0))
                Log.i("eidService", "Service informed Activity of Suicide. +")
            } catch (e: RemoteException) {
                // The client is dead. Remove it from the list; we are going
                // through the list from back to front so this is safe to do
                // inside the loop.
                mClients.removeAt(i)
                Log.i("eidService", "Service informed Activity of Suicide. -")
            }
        }
        Log.i("eidService", "Service informed Activity of Suicide.")
    }

    private fun logMessage(m: String) {
        // Check if log is too long, shorten if necessary.
        if (logmsgs.length > 1000) {
            var tempi = logmsgs.length
            tempi = logmsgs.indexOf("\n", tempi - 500)
            logmsgs = logmsgs.substring(tempi + 1)
        }

        // Append new message to the log.
        logmsgs += """
            
            ${theDateTimeIs()}$m
            """.trimIndent()

        if (displayMsgType == 0) {
            // Build bundle
            val b = Bundle()
            b.putString("logappend", theDateTimeIs() + m)
            for (i in mClients.indices.reversed()) {
                try {
                    val msg = Message.obtain(null, MSG_UPDATE_LOG_APPEND)
                    msg.data = b
                    mClients[i].send(msg)
                } catch (e: RemoteException) {
                    // The client is dead. Remove it from the list; we are going
                    // through the list from back to front so this is safe to do
                    // inside the loop.
                    mClients.removeAt(i)
                }
            }
        }
    }

    private fun sendAllLogMessagesToUI() {
        val b = Bundle()
        b.putString("logfull", logmsgs)

        for (i in mClients.indices.reversed()) {
            try {
                val msg = Message.obtain(null, MSG_UPDATE_LOG_FULL)
                msg.data = b
                mClients[i].send(msg)
            } catch (e: RemoteException) {
                // The client is dead. Remove it from the list; we are going
                // through the list from back to front so this is safe to do
                // inside the loop.
                mClients.removeAt(i)
            }
        }
    }

    private fun setDisplayMsgType(msgType: Int) {
        var MsgType = msgType
        MsgType = 0

        if (displayMsgType != MsgType) { //Type changed. Need to re-send everything
            displayMsgType = MsgType
            sendAllLogMessagesToUI()
        }
    }

    private fun sendStatusMessageToUI() {
        // Build bundle
        val stat = when (bTState) {
            STATE_NONE -> DeviceConnectionState.NONE.stateString
            STATE_LISTEN -> DeviceConnectionState.LISTENING.stateString
            STATE_CONNECTED -> DeviceConnectionState.CONNECTED.stateString
            STATE_CONNECTING -> DeviceConnectionState.CONNECTING.stateString
            else -> DeviceConnectionState.SCANNING.stateString
        }
        val b = Bundle()
        b.putString("stat", stat)
        b.putString("info1", lastEID)
        b.putString("info2", theTimeIs())

        for (i in mClients.indices.reversed()) {
            try {
                val msg = Message.obtain(null, MSG_UPDATE_STATUS)
                msg.data = b
                mClients[i].send(msg)
            } catch (e: RemoteException) {
                // The client is dead. Remove it from the list; we are going
                // through the list from back to front so this is safe to do
                // inside the loop.
                mClients.removeAt(i)
            }
        }
        ticksSinceLastStatusSent = 0 // Reset to zero
    }

    private fun sendNewEIDMessageToUI() {  // tell non counter clients when a new EID is available, save to EID-TAGS folder
        if (scanAction == ScanAction.SCAN_SINGLE) {
            // Build bundle
            val stat = "Scanning"

            val b = Bundle()
            b.putString("stat", stat)
            b.putString("info1", lastEID)
            b.putString("info2", theTimeIs())

            for (i in mClients.indices.reversed()) {
                try {
                    val msg = Message.obtain(null, MSG_NEW_EID_FOUND)
                    msg.data = b
                    mClients[i].send(msg)
                } catch (e: RemoteException) {
                    // The client is dead. Remove it from the list; we are going
                    // through the list from back to front so this is safe to do
                    // inside the loop.
                    mClients.removeAt(i)
                }
            }

            saveEIDLineToFile(lastEID + "\r")
            if (useSerialEID) {
                beep()
            }
            scanAction = ScanAction.NONE
        }
    }

    private fun saveEIDToFile() {  // tell counter clients when a new EID is available save to RACE-TAGS folder
        if (scanAction == ScanAction.SCAN_COUNTER) {
            // De Duplicate
            if (shouldDuplicate) {
                if (lastEID.length != 16) { // look out for too many tags read at once, usually a race reader problem
                    return
                }
                if (!dedupSet.contains(lastEID)) {
                    // Remove the oldest entry if the window size exceeds 5

                    if (window.size >= WINDOW_SIZE) {
                        val removed = window.removeFirst() // remove first element from window list
                        dedupSet.remove(removed) // remove the
                        //			}
                    }
                    // Check if the line is a duplicate within the window
                    //		if (!dedupSet.contains(LastEID)) {
                    // if unique Add the line to the window and the dedupSet
                    window.addLast(lastEID)
                    dedupSet.add(lastEID)
                } else {
                    return
                }
            }

            // Build bundle
            val stat = saveEIDLineToCounterFile(lastEID + "\r")
            val b = Bundle()
            b.putString("stat", stat)
            b.putString("info1", lastEID)
            b.putString("info2", theTimeIs())

            for (i in mClients.indices.reversed()) {
                try {
                    val msg = Message.obtain(null, MSG_NEW_EID_FOUND)
                    msg.data = b
                    mClients[i].send(msg)
                } catch (e: RemoteException) {
                    // The client is dead. Remove it from the list; we are going
                    // through the list from back to front so this is safe to do
                    // inside the loop.
                    mClients.removeAt(i)
                }
            }

            beep()
        }
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        loadPreferences()
        registerForPreferenceChanges()
        if (!useSerialEID) {
            Log.i("SerialEID", "Generic Bluetooth Reader")

            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

            timer.schedule(object : TimerTask() {
                override fun run() {
                    onTimerTickTimerThread()
                }
            }, 0, 500L)
        } else {
            Log.i("SerialEID", "X6P Serial Reader")
            try {
                SerialPort.setDevicePower(applicationContext, true)
            } catch (e: SecurityException) {
                Log.e("SecurityException", e.message!!)
            }
            Log.e("No SecurityException", "Port power ON")

            try {
                SerialPort.setNx6pBackNode("/sys/class/ext_dev/function/pin10_en", "1")
            } catch (e: SecurityException) {
                Log.e("SecurityException", e.message!!)
            }
            Log.e("No SecurityException", "enable pin 10")

            try {
                val path = File(SerialPort.getSerialPath()) // --> /dev/ttyHSL1
                mSerialPort = SerialPort(path, 9600, 0x02)
                mOutputStream = mSerialPort!!.outputStream
                mInputStream = mSerialPort!!.inputStream

                /* Create a receiving thread */
                mReadThread = ReadThread()
                mReadThread!!.start()
            } catch (e: SecurityException) {
                Log.e("SecurityException", e.message!!)
            } catch (e: IOException) {
                Log.e("IOException", e.message!!)
            } catch (e: InvalidParameterException) {
                Log.e("InvalidParameterException", e.message!!)
            }

            //		Log.e("No SecurityException","Serial port open");
            for (i in mClients.indices.reversed()) {
                try {
                    val msg = Message.obtain(null, MSG_READER_CONNECTED)
                    mClients[i].send(msg)
                } catch (e: RemoteException) {
                    // The client is dead. Remove it from the list; we are going
                    // through the list from back to front so this is safe to do
                    // inside the loop.
                    mClients.removeAt(i)
                }
            }

            // do more serial stuff here ? ... maybe not ...
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY // run until explicitly stopped.
    }

    private fun loadPreferences() {

        useSerialEID = preferences.getBoolean("useserialeid", false)
        shouldDuplicate = preferences.getBoolean("deduplicate", false)
        fileNameMod = preferences.getString("filenamemod", "0")

        try {
            val newUseHTCConnectionWorkaround = preferences.getBoolean("htcconnectworkaround", false)
            val newMACAddress = preferences.getString("bluetooth_mac", "00:00:00:00:00:00")
            if (newMACAddress != macAddress || newUseHTCConnectionWorkaround != useHTCConnectionWorkaround) {
                if (macAddress != "00:00:00:00:00:00") {
                    logMessage("BT: Target Device Changed. You will need to Disconnect/Reconnect.")
                }
                macAddress = newMACAddress
                useHTCConnectionWorkaround = newUseHTCConnectionWorkaround
                btStop()
            }
        } catch (nfe: NumberFormatException) {
        }
    }

    private fun registerForPreferenceChanges() {
        preferences.registerOnSharedPreferenceChangeListener(this)
    }

    private fun unregisterForPreferenceChanges() {
        preferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key in listOf("useserialeid", "deduplicate", "filenamemod", "bluetooth_mac", "htcconnectworkaround")) {
            loadPreferences()
        }
    }

    private fun theDateTimeIs(): String {  // maybe move this to its own class ?
        val calendar = Calendar.getInstance()
        val day = calendar[Calendar.DAY_OF_MONTH]
        val month = calendar[Calendar.MONTH]
        val year = calendar[Calendar.YEAR]
        val hours = calendar[Calendar.HOUR_OF_DAY]
        val minutes = calendar[Calendar.MINUTE]
        val seconds = calendar[Calendar.SECOND]
        return (make2Digits(month + 1) + "/" + make2Digits(day) + "/" + year + ":" + make2Digits(
            hours
        ) + ":" + make2Digits(minutes) + ":"
                + make2Digits(seconds) + " ")
    }

    private fun theTimeIs(): String { // maybe move this too ?
        val calendar = Calendar.getInstance()
        val hours = calendar[Calendar.HOUR_OF_DAY]
        val minutes = calendar[Calendar.MINUTE]
        val seconds = calendar[Calendar.SECOND]
        return (make2Digits(hours) + ":" + make2Digits(minutes) + ":"
                + make2Digits(seconds) + " ")
    }

    private fun make2Digits(i: Int): String { // maybe move this too ?
        return if (i < 10) {
            "0$i"
        } else {
            i.toString()
        }
    }

    private fun onTimerTickTimerThread() {
        // This is running on a separate thread. Cannot do UI stuff from here.
        // Send a message to the handler to do that stuff on the main thread.
        handler.sendMessage(handler.obtainMessage(MSG_TIMER_TICK))
    }

    private fun onTimerTick() { // Back on the main thread.
        ticksSinceLastStatusSent++
        if (ticksSinceLastStatusSent > 0) {
            sendStatusMessageToUI()
        }

        if (bTState == STATE_NONE) { //We're not connected, try to start.
            val bluetoothAdapter = mBluetoothAdapter
            if (bluetoothAdapter == null) { //No adapter. Fail
                //Log.e("Bluetooth", "getDefaultAdapter returned null");
                Toast.makeText(this, "This device does not support Bluetooth.", Toast.LENGTH_SHORT)
                    .show()
                logMessage("Bluetooth is NOT supported.")
                haltService()
            } else if (bluetoothAdapter.isEnabled) { //Bluetooth disabled
                btStart()
            } else { //and auto-switch is disabled. Fail
				Toast.makeText(this, "Bluetooth is Disabled", Toast.LENGTH_SHORT).show()
				logMessage("Bluetooth is Disabled")
				haltService()
			}
        }
    }

    private fun haltService() {
        isRunning = false
        informActivityOfThreadSuicide()
        timer.cancel()
        this.stopSelf()
    }

    @Synchronized
    private fun btStart() {
        setDisplayMsgType(0)
        //		Log.i("eidService", "BTstart");
        // Cancel any thread attempting to make a connection
        if (mBTConnectThread != null) {
            mBTConnectThread!!.cancel()
            mBTConnectThread = null
        }
        // Cancel any thread currently running a connection
        if (mBTConnectedThread != null) {
            mBTConnectedThread!!.cancel()
            mBTConnectedThread = null
        }

        if (!BluetoothAdapter.checkBluetoothAddress(macAddress)) {
            logMessage("Invalid Bluetooth MAC Address: \"$macAddress\"")
            informActivityOfThreadSuicide()
        } else if (macAddress == "00:00:00:00:00:00") {
            logMessage("Error: No Bluetooth device has been selected.")
            isRunning = false
            informActivityOfThreadSuicide()
            timer.cancel()
            this.stopSelf()
        } else {
            bTState = STATE_LISTEN
            val btdevice = mBluetoothAdapter!!.getRemoteDevice(macAddress)
            btConnect(btdevice)
        }
    }

    @Synchronized
    private fun btConnect(device: BluetoothDevice) {
        logMessage("Device: " + device.address)
        // Cancel any thread attempting to make a connection
        if (bTState == STATE_CONNECTING) {
            if (mBTConnectThread != null) {
                mBTConnectThread!!.cancel()
                mBTConnectThread = null
            }
        }
        // Cancel any thread currently running a connection
        if (mBTConnectedThread != null) {
            mBTConnectedThread!!.cancel()
            mBTConnectedThread = null
        }

        // Start the thread to connect with the given device
        mBTConnectThread = BTConnectThread(device, useHTCConnectionWorkaround)
        mBTConnectThread!!.start()
        bTState = STATE_CONNECTING

        for (i in mClients.indices.reversed()) {
            try {
                val msg = Message.obtain(null, MSG_READER_CONNECTING)
                mClients[i].send(msg)
            } catch (e: RemoteException) {
                // The client is dead. Remove it from the list; we are going
                // through the list from back to front so this is safe to do
                // inside the loop.
                mClients.removeAt(i)
            }
        }
    }

    private inner class BTConnectThread(
        private val mmDevice: BluetoothDevice,
        isAnHTCDevice: Boolean
    ) : Thread() {
        private val mmSocket: BluetoothSocket?

        init {
            var tmp: BluetoothSocket? = null

            // Get a BluetoothSocket for a connection with the given BluetoothDevice
            if (isAnHTCDevice) {
                try {
                    val m: Method = mmDevice.javaClass.getMethod(
                        "createRfcommSocket", *arrayOf<Class<*>?>(
                            Int::class.javaPrimitiveType
                        )
                    )
                    tmp = m.invoke(mmDevice, 1) as BluetoothSocket
                } catch (e: Exception) {
                    handleExceptionForSocketCreation(e)
                }
            } else {
                try {
                    val myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                    tmp = mmDevice.createRfcommSocketToServiceRecord(myUUID)
                } catch (e: SecurityException) {
                    handleExceptionForSocketCreation(e)
                    //Prevent lint from flagging duplicate catch block for Exception.
                } catch (e: Exception) {
                    handleExceptionForSocketCreation(e)
                }
            }

            mmSocket = tmp
        }

        override fun run() {
            //Log.i("eidService", "BEGIN BTConnectThread");
            handler.sendMessage(handler.obtainMessage(MSG_BT_LOG_MESSAGE, "Trying to Connect..."))

            // Always try to cancel discovery because it will slow down a connection
            try {
                mBluetoothAdapter!!.cancelDiscovery()
            } catch (e: SecurityException) {
                // No BLUETOOTH_SCAN permission. Shouldn't happen, but it could.
                // NO-OP since there is nothing we can really do here except abort
                // and connections can still be made with discovery in progress...
                // just slowly...
                e.printStackTrace()
            }

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a successful connection or an exception
                mmSocket!!.connect()
            } catch (e: SecurityException) {
                handleExceptionOnConnectionFailure(e)
                return
            } catch (e: IOException) {
                handleExceptionOnConnectionFailure(e)
                return
            }

            // Reset the ConnectThread because we're done
            synchronized(this@EIDReaderService) {
                mBTConnectThread = null
            }

            // Start the connected thread
            onBluetoothConnected(mmSocket, mmDevice)
        }

        fun cancel() {
            try {
                mmSocket!!.close()
            } catch (e: IOException) {
                //Log.e("EidService", "close() of connect socket failed", e);
            }
        }

        private fun handleExceptionForSocketCreation(e: Exception) {
            //Log.e("EidService", "Error at createRfcommSocketToServiceRecord: " + e);
            e.printStackTrace()
            handler.sendMessage(
                handler.obtainMessage(
                    MSG_BT_LOG_MESSAGE,
                    "Exception creating socket: $e"
                )
            )
        }

        private fun handleExceptionOnConnectionFailure(e: Exception) {
            // Close the socket
            try {
                mmSocket!!.close()
            } catch (e2: IOException) {
                //Log.e("EidService", "unable to close() socket during connection failure", e2);
            }
            //Log.e("EidService", "unable to connect() socket. Error: ", e);
            handler.sendMessage(handler.obtainMessage(MSG_BT_LOG_MESSAGE, "Failed to Connect: $e"))
            handler.sendMessage(handler.obtainMessage(MSG_BT_FINISHED))
        }
    }

    @Synchronized
    private fun onBluetoothConnected(socket: BluetoothSocket, device: BluetoothDevice?) {
        //Log.i("eidService", "Connected");
        // Cancel the thread that completed the connection
        if (mBTConnectThread != null) {
            mBTConnectThread!!.cancel()
            mBTConnectThread = null
        }
        // Cancel any thread currently running a connection
        if (mBTConnectedThread != null) {
            mBTConnectedThread!!.cancel()
            mBTConnectedThread = null
        }

        // Start the thread to manage the connection and perform transmissions
        mBTConnectedThread = BTConnectedThread(socket)
        mBTConnectedThread!!.start()

        bTState = STATE_CONNECTED

        for (i in mClients.indices.reversed()) {
            try {
                val msg = Message.obtain(null, MSG_READER_CONNECTED)
                mClients[i].send(msg)
            } catch (e: RemoteException) {
                // The client is dead. Remove it from the list; we are going
                // through the list from back to front so this is safe to do
                // inside the loop.
                mClients.removeAt(i)
            }
        }
    }

    private inner class BTConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {

        private val mmInStream: InputStream?
        private val mmOutStream: OutputStream?

        init {
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = mmSocket.inputStream
                tmpOut = mmSocket.outputStream
            } catch (e: IOException) {
                //Log.e("EidService", "temp sockets not created", e);
                handler.sendMessage(
                    handler.obtainMessage(
                        MSG_BT_LOG_MESSAGE,
                        "Could not create Streams"
                    )
                )
                handler.sendMessage(handler.obtainMessage(MSG_BT_FINISHED))
            }

            mmInStream = tmpIn
            mmOutStream = tmpOut
        }

        override fun run() {
            //Log.i("eidService", "BEGIN BTConnectedThread");
            handler.sendMessage(
                handler.obtainMessage(
                    MSG_BT_LOG_MESSAGE,
                    "Bluetooth Device Connected"
                )
            )

            // maybe add an on screen indicator here that we have a connected Bluetooth device
            val buffer = ByteArray(1024)
            var bytesread: Int

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    bytesread = mmInStream!!.read(buffer) //This is a blocking call
                    val tempData = ByteArray(bytesread)
                    System.arraycopy(buffer, 0, tempData, 0, bytesread)
                    handler.sendMessage(handler.obtainMessage(MSG_BT_GOT_DATA, tempData))
                } catch (e: IOException) {
                    handler.sendMessage(handler.obtainMessage(MSG_BT_FINISHED))
                    break
                }
            }
        }

        fun write(buffer: ByteArray?) {
            try {
                mmOutStream!!.write(buffer)
            } catch (e: IOException) {
                //Log.e("EidService", "Exception during write", e);
            }
        }

        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
                //Log.e("EidService", "close() of connected socket failed", e);
            }
        }
    }

    @Synchronized
    private fun btStop() {
//        Log.i("eidService", "BTstop");
        if (mBTConnectThread != null) {
            mBTConnectThread!!.cancel()
            mBTConnectThread = null
        }
        if (mBTConnectedThread != null) {
            mBTConnectedThread!!.cancel()
            mBTConnectedThread = null
        }
        bTState = STATE_NONE
    }

    private fun sendDataToBluetooth(buffer: ByteArray?) { // You run this from the main thread.
        // Create temporary object
        var r: BTConnectedThread?
        // Synchronize a copy of the ConnectedThread
        synchronized(this) {
            if (bTState != STATE_CONNECTED) return
            r = mBTConnectedThread
        }
        // Perform the write unsynchronized
        r!!.write(buffer)
    }

    private fun parseBluetoothDataStream(buffer: ByteArray) {
        if (buffer.size <= 18) {
            parseEIDStream(String(buffer))
        }
    }

    private fun parseEIDStream(newData: String) {
        eid += newData
        // String[] lines = EID.split("\\r?\\n"); //works for Priority1
        // String[] lines = EID.split("\r"); //works for y-tex
        val lines = eid.split("\n".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray() // works for both

        Log.e("EidService", eid)
        if (lines.isNotEmpty()) {
            for (i in lines.indices) {
                completeline = false // Reset this
                if (lines.isNotEmpty()) { // There is some data here
                    if (lines[i].lastIndexOf("\r") + 1 == lines[i].length) { // Line ends with a \r
                        completeline = true
                        if (lines[i].substring(3, 4) == "_") {
                        } else if (lines[i].substring(3, 4) == " ") {
                            eid = eid.substring(0, 3) + "_" + eid.substring(4, eid.length - 1)
                        } else {
                            if (lines[i].substring(0, 1) == "0") {
                                //						    Log.i("eidService", "ShearWell Wand/Panel");
                                eid = eid.substring(1, 4) + "_" + eid.substring(4, eid.length)
                                eid = eid.substring(0, eid.length - 1) //prune off \n
                            } else {
                                //				         LogMessage("eidService0, Y-Tex Wand");
                                eid = eid.substring(0, 3) + "_" + eid.substring(3, eid.length - 1)
                            }
                        }
                        lastEID = eid.substring(
                            0,
                            eid.length - 1
                        ) //prune off end of line for the database
                        if (scanAction == ScanAction.SCAN_COUNTER) {
                            saveEIDToFile() // send the EID to counter clients and save to RACE-TAGS folder
                        } else if (scanAction == ScanAction.SCAN_SINGLE){
                            sendNewEIDMessageToUI() // send the EID to non counter clients and save to EID-TAGS folder
                            Log.i("eidService", "sendNewEIDMessageToUI")
                        }
                        eid = "" // Clear out EID for next one
                    }
                }
            }

            if (!completeline) { // Last line wasn't complete, put last incomplete line back
                if (lines[lines.size - 1].length < 1000) { // Only if less than 1000 characters long.
                    eid = lines[lines.size - 1]
                }
            }
        }
    }

    private fun saveEIDLineToCounterFile(line: String): String {  // Saves EID to RACE-TAGS folder and returns name of the file
        eidDataToSave += theDateTimeIs() + line
        var filename = "zzz"
        if (eidDataToSave.length > 15) {
            try {
                val state = Environment.getExternalStorageState()
                if (Environment.MEDIA_MOUNTED == state) { // We can read and write the media
                    val dir = raceTagsDirectory(this)
                    dir.mkdirs()

                    val calendar = Calendar.getInstance()
                    val year = calendar[Calendar.YEAR]
                    val month = calendar[Calendar.MONTH] + 1
                    val day = calendar[Calendar.DAY_OF_MONTH]

                    filename =
                        year.toString() + "-" + make2Digits(month) + "-" + make2Digits(day) + "_" + fileNameMod + ".txt"
                    val file = File(dir, filename)
                    val writer = FileWriter(file, true)
                    writer.append(eidDataToSave)
                    writer.flush()
                    writer.close()
                    eidDataToSave = ""
                }
            } catch (e: Exception) {
            }
        }
        return filename
    }

    private fun saveEIDLineToFile(line: String) {
        eidDataToSave += theDateTimeIs() + line
        if (eidDataToSave.length > 15) {
            saveEIDChunk()
        }
    }

    private fun saveEIDChunk() {
        if (eidDataToSave.isNotEmpty()) {
            try {
                val state = Environment.getExternalStorageState()
                if (Environment.MEDIA_MOUNTED == state) { // We can read and write the media
                    val dir = eidTagsDirectory(this)
                    dir.mkdirs()

                    val calendar = Calendar.getInstance()
                    val year = calendar[Calendar.YEAR]
                    val month = calendar[Calendar.MONTH] + 1
                    val day = calendar[Calendar.DAY_OF_MONTH]
                    val filename =
                        year.toString() + "-" + make2Digits(month) + "-" + make2Digits(day) + ".txt"

                    val file = File(dir, filename)
                    val writer = FileWriter(file, true)
                    writer.append(eidDataToSave)
                    writer.flush()
                    writer.close()
                    eidDataToSave = ""
                }
            } catch (e: Exception) {
            }
        }
    }

    var handler: Handler = object : Handler() {
        // Handler for data coming from bluetooth sockets
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_TIMER_TICK -> onTimerTick()
                MSG_BT_GOT_DATA -> {
                    Log.i("handleMessage", "MSG_BT_GOT_DATA")
                    val buffer2 = msg.obj as ByteArray
                    parseBluetoothDataStream(buffer2)
                }

                MSG_BT_LOG_MESSAGE -> logMessage(msg.obj as String)
                MSG_BT_FINISHED -> //				Log.i("handleMessage", "MSG_BT_FINISHED");
                    btStop()

                else -> super.handleMessage(msg)
            }
        }
    }

    private fun onDataReceived(buffer: ByteArray, size: Int) {
        synchronized(byteReceivedBackSemaphore) {
            val tempData = ByteArray(size)
            System.arraycopy(buffer, 0, tempData, 0, size)
            handler.sendMessage(handler.obtainMessage(MSG_BT_GOT_DATA, tempData))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterForPreferenceChanges()
        saveEIDChunk()

        // Kill threads
        timer.cancel()

        if (!useSerialEID) {
            btStop()
        } else {
            try {
                SerialPort.setNx6pBackNode("/sys/class/ext_dev/function/pin10_en", "0")
            } catch (e: SecurityException) {
                Log.e("SecurityException", e.message!!)
            }

            //			Log.e("No SecurityException","disable pin 10");
            try {
                mSerialPort!!.close()
            } catch (e: SecurityException) {
                Log.e("SecurityException", e.message!!)
            }

            //		Log.e("No SecurityException","Serial port closed");
            for (i in mClients.indices.reversed()) {
                try {
                    val msg = Message.obtain(null, MSG_READER_CONNECTING)
                    mClients[i].send(msg)
                } catch (e: RemoteException) {
                    // The client is dead. Remove it from the list; we are going
                    // through the list from back to front so this is safe to do
                    // inside the loop.
                    mClients.removeAt(i)
                }
            }

            try {
                SerialPort.setDevicePower(applicationContext, false)
            } catch (e: SecurityException) {
                Log.e("SecurityException", e.message!!)
            }
            //		Log.e("No SecurityException","Port power OFF");
        }

        stopForeground(true)
        //		Log.i("eidService", "Service Stopped.");
        isRunning = false
    }

    companion object {

        private const val TEST_EID = "940_100000286625"
        private const val WINDOW_SIZE = 4 // Size of the sliding window
        private var fileNameMod: String? = "0"

        private val toneG = ToneGenerator(AudioManager.STREAM_ALARM, 100)

        @JvmStatic
        var isRunning: Boolean = false
            private set

        const val STATE_NONE: Int = 0 // we're doing nothing
        const val STATE_LISTEN: Int = 1 // now listening for incoming connections
        const val STATE_CONNECTING: Int = 2 // now initiating an outgoing connection
        const val STATE_CONNECTED: Int = 3 // now connected to a remote device

        const val MSG_THREAD_SUICIDE: Int = DeviceService.MSG_THREAD_SUICIDE
        const val MSG_REGISTER_CLIENT: Int = DeviceService.MSG_REGISTER_CLIENT
        const val MSG_UNREGISTER_CLIENT: Int = DeviceService.MSG_UNREGISTER_CLIENT
        const val MSG_UPDATE_STATUS: Int = DeviceService.MSG_UPDATE_STATUS
        const val MSG_NEW_EID_FOUND: Int = 4
        const val MSG_UPDATE_LOG_APPEND: Int = 5
        const val MSG_UPDATE_LOG_FULL: Int = 6
        const val MSG_TOGGLE_LOG_TYPE: Int = 7
        const val MSG_RELOAD_PREFERENCES: Int = 8
        const val MSG_SEND_ME_TAGS: Int = 9
        const val MSG_NO_TAGS_PLEASE: Int = 10
        const val MSG_SEND_ME_COUNTER_TAGS: Int = 39
        const val MSG_NO_COUNTER_TAGS_PLEASE: Int = 40

        const val MSG_TIMER_TICK: Int = 100
        const val MSG_BT_LOG_MESSAGE: Int = 200
        const val MSG_BT_GOT_DATA: Int = 201
        const val MSG_READER_CONNECTED: Int = 202
        const val MSG_READER_CONNECTING: Int = 203
        const val MSG_BT_FINISHED: Int = 299

        private fun beep() {
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200)
        }
    }
}