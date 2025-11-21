package com.weyr_associates.animaltrakkerfarmmobile.app.core.widget

import android.content.Context
import android.util.AttributeSet
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout

//Simply a drawer layout that assumes right drawers instead of left drawers.

class ReverseDrawerLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : DrawerLayout(context, attrs) {

    override fun isOpen(): Boolean {
        return isDrawerOpen(GravityCompat.END)
    }

    override fun open() {
        openDrawer(GravityCompat.END)
    }

    override fun close() {
        closeDrawer(GravityCompat.END)
    }
}
