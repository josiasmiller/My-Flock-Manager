@file:JvmName("Permissions")

package com.weyr_associates.animaltrakkerfarmmobile.app.permissions

import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED

fun Context.checkAllSelfPermission(vararg permissions: String): Boolean {
    return permissions.all { permission ->
        PERMISSION_GRANTED == checkSelfPermission(permission)
    }
}

fun Context.checkAnySelfPermission(vararg permissions: String): Boolean {
    return permissions.any { permission ->
        PERMISSION_GRANTED == checkSelfPermission(permission)
    }
}
