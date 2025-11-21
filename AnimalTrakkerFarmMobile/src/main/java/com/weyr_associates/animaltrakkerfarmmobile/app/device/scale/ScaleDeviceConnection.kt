package com.weyr_associates.animaltrakkerfarmmobile.app.device.scale

import android.content.Context
import android.os.Message
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import com.weyr_associates.animaltrakkerfarmmobile.app.device.DeviceServiceConnection
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ScaleDeviceConnection(
    context: Context, lifecycle: Lifecycle
) : DeviceServiceConnection(context, lifecycle, ScaleDeviceService::class.java) {

    private val coroutineScope = lifecycle.coroutineScope

    private val _scaleScanningState = MutableStateFlow<ScaleScanState>(ScaleScanIdle)
    val scaleScanningState = _scaleScanningState.asStateFlow()

    private val onWeightScannedChannel = Channel<ScaleScanResult>()
    val onWeightScanned = onWeightScannedChannel.receiveAsFlow()

    override val isDeviceServiceRunning: Boolean
        get() = ScaleDeviceService.isScaleRunning

    fun scanWeight(reason: String) {
        _scaleScanningState.update { ScaleScanActive(reason) }
        if (isConnected) {
            sendMessage(
                ScaleDeviceService.MSG_SEND_ME_TAGS
            )
        }
    }

    fun cancelWeightScan() {
        _scaleScanningState.update { ScaleScanIdle }
        if (isConnected) {
            sendMessage(ScaleDeviceService.MSG_NO_TAGS_PLEASE)
        }
    }

    override fun onConnected() {
        sendMessage(
            if (scaleScanningState.value is ScaleScanActive) ScaleDeviceService.MSG_SEND_ME_TAGS
            else ScaleDeviceService.MSG_NO_TAGS_PLEASE
        )
    }

    override fun onDisconnecting() {
        sendMessage(ScaleDeviceService.MSG_NO_TAGS_PLEASE)
    }

    override fun onMessageReceived(message: Message) {
        val bundle = message.data
        when (message.what) {
            ScaleDeviceService.MSG_NEW_SCALE_FOUND -> {
                val weightString = bundle.getString("info1")
                val weight = weightString?.toFloatOrNull()
                val scanState = scaleScanningState.value
                if (weight != null && scanState is ScaleScanActive) {
                    _scaleScanningState.getAndUpdate { ScaleScanIdle }
                    coroutineScope.launch {
                        onWeightScannedChannel.send(
                            ScaleScanResult(weight, scanState.reason)
                        )
                    }
                }
            }
        }
    }
}
