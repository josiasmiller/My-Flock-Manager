package com.weyr_associates.animaltrakkerfarmmobile.app.main.menu

import android.widget.Button
import androidx.core.view.isGone
import com.weyr_associates.animaltrakkerfarmmobile.BuildConfig

fun Button.deactivate() {
    isEnabled = BuildConfig.SHOW_INACTIVE_MENU_OPTIONS_AS_ENABLED
    isGone = BuildConfig.HIDE_INACTIVE_MENU_OPTIONS
}
