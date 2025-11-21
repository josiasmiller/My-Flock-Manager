package com.weyr_associates.animaltrakkerfarmmobile.app.device

import androidx.annotation.DrawableRes
import com.weyr_associates.animaltrakkerfarmmobile.R

enum class DeviceConnectionState(val stateString: String) {
    NONE("None"),
    LISTENING("Listen"),
    CONNECTING("Connecting"),
    CONNECTED("Connected"),
    SCANNING("Scanning");

    companion object {

        @JvmStatic
        fun fromStateString(stateString: String): DeviceConnectionState {
            return DeviceConnectionState.entries.firstOrNull {
                it.stateString == stateString
            } ?: NONE
        }

        @JvmStatic
        @DrawableRes
        fun iconForState(state: DeviceConnectionState) = when (state) {
            NONE -> R.drawable.ic_bluetooth_disabled
            LISTENING,
            SCANNING,
            CONNECTING -> R.drawable.ic_bluetooth_searching
            CONNECTED -> R.drawable.ic_bluetooth_connected
        }

        fun colorForState(state: DeviceConnectionState) = when (state) {
            CONNECTED -> R.color.status_ok
            else -> R.color.status_error
        }
    }
}
