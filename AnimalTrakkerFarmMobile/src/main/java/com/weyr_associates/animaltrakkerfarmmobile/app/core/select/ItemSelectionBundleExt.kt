package com.weyr_associates.animaltrakkerfarmmobile.app.core.select

import android.os.Bundle

fun Bundle.selectedIntItem(): Int {
    return getInt(SelectItem.EXTRA_SELECTED_ITEM)
}

fun Bundle.selectedIntItem(default: Int): Int {
    return getInt(SelectItem.EXTRA_SELECTED_ITEM, default)
}

fun Bundle.selectedOptStringItem(): String? {
    return getString(SelectItem.EXTRA_SELECTED_ITEM)
}

fun Bundle.selectedStringItem(): String {
    return requireNotNull(getString(SelectItem.EXTRA_SELECTED_ITEM))
}

fun Bundle.selectedStringItem(default: String): String {
    return getString(SelectItem.EXTRA_SELECTED_ITEM, default)
}

inline fun <reified T> Bundle.selectedOptItem(): T? {
    return T::class.java.cast(getParcelable(SelectItem.EXTRA_SELECTED_ITEM))
}

inline fun <reified T> Bundle.selectedItem(): T {
    return requireNotNull(T::class.java.cast(getParcelable(SelectItem.EXTRA_SELECTED_ITEM)))
}
