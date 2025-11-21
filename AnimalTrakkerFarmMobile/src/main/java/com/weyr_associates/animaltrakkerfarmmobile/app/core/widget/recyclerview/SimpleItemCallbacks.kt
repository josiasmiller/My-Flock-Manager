package com.weyr_associates.animaltrakkerfarmmobile.app.core.widget.recyclerview

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil.ItemCallback

object SimpleItemCallbacks {

    @JvmStatic
    val INTEGERS: ItemCallback<Int> = SimpleComparableItemCallback<Int>()

    private class SimpleComparableItemCallback<T> : ItemCallback<T>() where T : Comparable<T> {
        override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
            return oldItem == newItem
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
            return oldItem == newItem
        }
    }
}
