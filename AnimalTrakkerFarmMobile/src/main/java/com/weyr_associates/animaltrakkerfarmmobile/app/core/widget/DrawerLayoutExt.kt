package com.weyr_associates.animaltrakkerfarmmobile.app.core.widget

import androidx.annotation.GravityInt
import androidx.drawerlayout.widget.DrawerLayout

fun DrawerLayout.toggle(@GravityInt drawerGravity: Int) {
    if (isDrawerOpen(drawerGravity)) {
        closeDrawer(drawerGravity)
    } else {
        openDrawer(drawerGravity)
    }
}
