package com.weyr_associates.animaltrakkerfarmmobile.app.core

import android.os.Build

object Sdk {
    @JvmStatic
    fun requiresRuntimeBluetoothPermissions(): Boolean {
        return Build.VERSION_CODES.S <= Build.VERSION.SDK_INT
    }
}
