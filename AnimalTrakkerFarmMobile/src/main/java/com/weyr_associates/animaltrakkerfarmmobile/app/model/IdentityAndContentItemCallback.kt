package com.weyr_associates.animaltrakkerfarmmobile.app.model

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil
import com.weyr_associates.animaltrakkerfarmmobile.model.HasIdentity

fun <T> itemCallbackUsingIdentityAndContent(): DiffUtil.ItemCallback<T> where T : HasIdentity {
    return IdentityAndContentItemCallback()
}

class IdentityAndContentItemCallback<T> : DiffUtil.ItemCallback<T>() where T : HasIdentity {
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem.id == newItem.id
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem == newItem
    }
}
