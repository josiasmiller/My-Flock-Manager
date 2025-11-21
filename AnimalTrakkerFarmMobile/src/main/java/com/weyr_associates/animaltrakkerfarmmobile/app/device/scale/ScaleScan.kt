package com.weyr_associates.animaltrakkerfarmmobile.app.device.scale

sealed interface ScaleScanState {
    val isScanning: Boolean
}

data object ScaleScanIdle : ScaleScanState {
    override val isScanning: Boolean = false
}

data class ScaleScanActive(
    val reason: String
) : ScaleScanState {
    override val isScanning: Boolean = true
}

data class ScaleScanResult(
    val weight: Float,
    val reason: String
)
