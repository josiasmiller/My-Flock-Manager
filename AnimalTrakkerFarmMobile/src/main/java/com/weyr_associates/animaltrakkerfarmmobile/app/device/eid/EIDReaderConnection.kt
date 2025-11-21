package com.weyr_associates.animaltrakkerfarmmobile.app.device.eid

import android.content.Context
import android.os.Message
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import com.weyr_associates.animaltrakkerfarmmobile.app.device.DeviceServiceConnection
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EIDReaderConnection(
    context: Context, lifecycle: Lifecycle
) : DeviceServiceConnection(context, lifecycle, EIDReaderService::class.java) {

    private val coroutineScope = lifecycle.coroutineScope

    override val isDeviceServiceRunning: Boolean
        get() = EIDReaderService.isRunning

    private val _isScanningForEID = MutableStateFlow(false)
    val isScanningForEID = _isScanningForEID.asStateFlow()

    private val onEIDScannedChannel = Channel<String>()
    val onEIDScanned = onEIDScannedChannel.receiveAsFlow()

    //TODO: Send no more tags on unbinding. Is this really necessary?
    //TODO: Some use cases include preferences reload messages. When to do this?

    fun scanEID() {
        if (!isScanningForEID.value) {
            _isScanningForEID.update { true }
            if (isConnected) {
                sendMessage(EIDReaderService.MSG_SEND_ME_TAGS)
            }
        }
    }

    fun toggleScanningEID() {
        if (isScanningForEID.value) {
            cancelEIDScan()
        } else {
            scanEID()
        }
    }

    fun cancelEIDScan() {
        if (isScanningForEID.value) {
            _isScanningForEID.update { false }
            if (isConnected) {
                sendMessage(EIDReaderService.MSG_NO_TAGS_PLEASE)
            }
        }
    }

    override fun onConnected() {
        sendMessage(
            if (isScanningForEID.value) EIDReaderService.MSG_SEND_ME_TAGS
            else EIDReaderService.MSG_NO_TAGS_PLEASE
        )
    }

    override fun onDisconnecting() {
        sendMessage(EIDReaderService.MSG_NO_TAGS_PLEASE)
    }

    override fun onMessageReceived(message: Message) {
        val bundle = message.data
        when (message.what) {
            EIDReaderService.MSG_NEW_EID_FOUND -> {
                val eidString = bundle.getString("info1")
                if (eidString != null && isScanningForEID.value) {
                    _isScanningForEID.update { false }
                    coroutineScope.launch {
                        onEIDScannedChannel.send(eidString)
                    }
                }
            }
        }
    }
}
