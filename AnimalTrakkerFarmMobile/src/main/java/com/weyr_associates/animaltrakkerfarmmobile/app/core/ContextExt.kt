package com.weyr_associates.animaltrakkerfarmmobile.app.core

import android.content.Context

val Context.versionName: String get() {
    return requireNotNull(packageManager.getPackageInfo(packageName, 0).versionName)
}
