package com.weyr_associates.animaltrakkerfarmmobile.app.device

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.lang.ref.WeakReference

abstract class DeviceServiceConnection(
    context: Context,
    private val lifecycle: Lifecycle,
    private val deviceServiceClass: Class<out Service>
) : LifecycleEventObserver, ServiceConnection {

    private val appContext: Context = context.applicationContext

    private val inboundMessageHandler: InboundMessageHandler by lazy {
        InboundMessageHandler(this)
    }

    private val inboundMessenger: Messenger by lazy {
        Messenger(inboundMessageHandler)
    }

    private val shouldBeBound: Boolean get() = lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)
    private var bindingState: BindingState = BindingState.UNBOUND

    protected var isConnected = false
        private set

    private var outboundMessenger: Messenger? = null

    private val _deviceConnectionState = MutableStateFlow(DeviceConnectionState.NONE)
    val deviceConnectionState = _deviceConnectionState.asStateFlow()

    final override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        checkServiceIsRunning()
        assessBindingRequirement()
    }

    final override fun onServiceConnected(componentName: ComponentName, binder: IBinder) {
        if (shouldBeBound) {
            establishConnection(binder)
        }
    }

    final override fun onServiceDisconnected(componentName: ComponentName) {
        isConnected = false
        outboundMessenger = null
    }

    protected abstract val isDeviceServiceRunning: Boolean

    protected abstract fun onMessageReceived(message: Message)

    protected open fun onConnected() { /*NO-OP*/ }

    protected open fun onDisconnecting() { /*NO-OP*/ }

    protected fun sendMessage(messageId: Int): Boolean {
        if (!isConnected) {
            return false
        }
        return outboundMessenger?.sendToService(messageId) ?: false
    }

    protected fun sendMessage(message: Message): Boolean {
        if (!isConnected) {
            return false
        }
        return outboundMessenger?.sendToService(message) ?: false
    }

    private fun checkServiceIsRunning() {
        if (!isDeviceServiceRunning) {
            startDeviceService()
        }
    }

    private fun assessBindingRequirement() {
        if (shouldBeBound && bindingState == BindingState.UNBOUND) {
            bindToService()
        } else if (!shouldBeBound && bindingState == BindingState.BOUND) {
            unbindFromService()
        }
    }

    private fun startDeviceService() {
        appContext.startService(Intent(appContext, deviceServiceClass))
    }

    private fun stopDeviceService() {
        appContext.stopService(Intent(appContext, deviceServiceClass))
    }

    private fun bindToService() {
        val intent = Intent(appContext, deviceServiceClass)
        if (appContext.bindService(intent, this, Context.BIND_AUTO_CREATE)) {
            bindingState = BindingState.BOUND
        } else {
            unbindFromService()
        }
    }

    private fun unbindFromService() {
        removeConnection()
        appContext.unbindService(this)
        bindingState = BindingState.UNBOUND
    }

    private fun establishConnection(binder: IBinder) {
        outboundMessenger = Messenger(binder)
        with(requireNotNull(outboundMessenger)) {
            sendToService(DeviceService.MSG_REGISTER_CLIENT)
            sendToService(DeviceService.MSG_UPDATE_STATUS)
        }
        isConnected = true
        onConnected()
    }

    private fun removeConnection() {
        onDisconnecting()
        isConnected = false
        outboundMessenger?.sendToService(
            DeviceService.MSG_UNREGISTER_CLIENT
        )
        outboundMessenger = null
    }

    private fun handleMessage(message: Message) {
        if(!isConnected) { return }
        val bundle = message.data
        when (message.what) {
            DeviceService.MSG_UPDATE_STATUS -> {
                val state: String? = bundle.getString("stat")
                _deviceConnectionState.update {
                    state?.let { stateString ->
                        DeviceConnectionState.fromStateString(stateString)
                    } ?: DeviceConnectionState.NONE
                }
            }
            DeviceService.MSG_THREAD_SUICIDE -> {
                unbindFromService()
                stopDeviceService()
            }
            else -> onMessageReceived(message)
        }
    }

    private fun Messenger.sendToService(what: Int): Boolean {
        return sendToService(Message.obtain(null, what))
    }

    private fun Messenger.sendToService(message: Message): Boolean {
        return try {
            send(message.apply { replyTo = inboundMessenger })
            true
        } catch(ex: RemoteException) {
            false
        }
    }

    private enum class BindingState {
        UNBOUND,
        BOUND
    }

    private class InboundMessageHandler(
        connection: DeviceServiceConnection
    ) : Handler(Looper.getMainLooper()) {
        private val connection = WeakReference(connection)
        override fun handleMessage(msg: Message) {
            connection.get()?.handleMessage(msg)
        }
    }
}
