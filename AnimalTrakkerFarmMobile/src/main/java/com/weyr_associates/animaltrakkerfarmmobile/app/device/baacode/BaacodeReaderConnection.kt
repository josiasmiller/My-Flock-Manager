package com.weyr_associates.animaltrakkerfarmmobile.app.device.baacode

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

class BaacodeReaderConnection(
    context: Context, lifecycle: Lifecycle
) : DeviceServiceConnection(context, lifecycle, BaacodeReaderService::class.java) {

    private val coroutineScope = lifecycle.coroutineScope

    override val isDeviceServiceRunning: Boolean
        get() = BaacodeReaderService.isBaaRunning

    private val _isScanningForBaacode = MutableStateFlow(false)
    val isScanningForBaacode = _isScanningForBaacode.asStateFlow()

    private val onBaacodeScannedChannel = Channel<String>()
    val onBaacodeScanned = onBaacodeScannedChannel.receiveAsFlow()

    //TODO: Send no more tags on unbinding. Is this really necessary?
    //TODO: Some use cases include preferences reload messages. When to do this?

    fun scanBaacode() {
        if (!isScanningForBaacode.value) {
            _isScanningForBaacode.update { true }
            if (isConnected) {
                sendMessage(BaacodeReaderService.MSG_SEND_ME_BAACODES)
            }
        }
    }

    fun toggleScanningBaacode() {
        if (isScanningForBaacode.value) {
            cancelBaacodeScan()
        } else {
            scanBaacode()
        }
    }

    fun cancelBaacodeScan() {
        if (isScanningForBaacode.value) {
            _isScanningForBaacode.update { false }
            if (isConnected) {
                sendMessage(BaacodeReaderService.MSG_NO_BAACODES_PLEASE)
            }
        }
    }

    override fun onConnected() {
        sendMessage(
            if (isScanningForBaacode.value) BaacodeReaderService.MSG_SEND_ME_BAACODES
            else BaacodeReaderService.MSG_NO_BAACODES_PLEASE
        )
    }

    override fun onDisconnecting() {
        sendMessage(BaacodeReaderService.MSG_NO_BAACODES_PLEASE)
    }

    override fun onMessageReceived(message: Message) {
        val bundle = message.data
        when (message.what) {
            BaacodeReaderService.MSG_NEW_BAACODE_FOUND -> {
                val baaCodeString = bundle.getString("info1")
                if (baaCodeString != null && isScanningForBaacode.value) {
                    _isScanningForBaacode.update { false }
                    coroutineScope.launch {
                        onBaacodeScannedChannel.send(baaCodeString)
                    }
                }
            }
        }
    }
}
