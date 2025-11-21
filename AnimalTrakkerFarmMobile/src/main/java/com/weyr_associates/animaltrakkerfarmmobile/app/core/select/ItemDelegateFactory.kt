package com.weyr_associates.animaltrakkerfarmmobile.app.core.select

import androidx.recyclerview.widget.DiffUtil

interface ItemDelegateFactory<T> {
    fun createDataSource(): ItemDataSource<T>
    fun createItemDiffCallback(): DiffUtil.ItemCallback<T>
    fun createDisplayTextProvider(): ItemDisplayTextProvider<T>
}
