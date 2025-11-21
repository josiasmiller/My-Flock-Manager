package com.weyr_associates.animaltrakkerfarmmobile.app.permissions

import android.Manifest.permission.BLUETOOTH_CONNECT
import android.Manifest.permission.BLUETOOTH_SCAN
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager.FEATURE_BLUETOOTH
import android.content.pm.PackageManager.PERMISSION_GRANTED
import com.weyr_associates.animaltrakkerfarmmobile.app.core.Sdk

object RequiredPermissions {
    @JvmStatic
    fun areFulfilled(context: Context): Boolean {
        return !Sdk.requiresRuntimeBluetoothPermissions() ||
                (isBluetoothConnectFulfilled(context) &&
                isBluetoothScanFulfilled(context) &&
                isBluetoothEnabled(context) &&
                isUnusedAppRestrictionDisabled(context))
    }

    fun isBluetoothConnectFulfilled(context: Context): Boolean {
        return !Sdk.requiresRuntimeBluetoothPermissions() ||
                context.checkSelfPermission(BLUETOOTH_CONNECT) == PERMISSION_GRANTED
    }

    fun isBluetoothScanFulfilled(context: Context): Boolean {
        return !Sdk.requiresRuntimeBluetoothPermissions() ||
                context.checkSelfPermission(BLUETOOTH_SCAN) == PERMISSION_GRANTED
    }

    fun isBluetoothEnabled(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(FEATURE_BLUETOOTH) &&
                BluetoothAdapter.getDefaultAdapter()?.isEnabled ?: false
    }

    fun isUnusedAppRestrictionDisabled(context: Context): Boolean {
        return Sdk.requiresRuntimeBluetoothPermissions() &&
                context.packageManager.isAutoRevokeWhitelisted
    }
}
