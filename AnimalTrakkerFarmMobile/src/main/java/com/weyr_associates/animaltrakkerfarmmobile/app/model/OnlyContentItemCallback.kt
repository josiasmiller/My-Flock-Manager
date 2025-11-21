package com.weyr_associates.animaltrakkerfarmmobile.app.model

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil

fun <T> itemCallbackUsingOnlyContent(): DiffUtil.ItemCallback<T> where T : Any {
    return OnlyContentItemCallback()
}

class OnlyContentItemCallback<T> : DiffUtil.ItemCallback<T>() where T : Any {
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem == newItem
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem == newItem
    }
}
