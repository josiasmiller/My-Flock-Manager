package com.weyr_associates.animaltrakkerfarmmobile.app.core.select

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.weyr_associates.animaltrakkerfarmmobile.R

class ItemListAdapter<T : Any>(
    diffCallback: DiffUtil.ItemCallback<T>,
    private val onSelected: (T) -> Unit
) : ListAdapter<ItemOption<T>, ItemOptionViewHolder<T>>(DataDiffCallback(diffCallback)) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemOptionViewHolder<T> {
        return LayoutInflater.from(parent.context)
            .inflate(R.layout.item_simple_selection, parent, false).let {
                ItemOptionViewHolder(it, onSelected)
            }
    }

    override fun onBindViewHolder(holder: ItemOptionViewHolder<T>, position: Int) {
        holder.bind(currentList[position])
    }

    private class DataDiffCallback<T : Any>(
        private val diffCallback: DiffUtil.ItemCallback<T>
    ) : DiffUtil.ItemCallback<ItemOption<T>>() {
        override fun areItemsTheSame(
            oldItem: ItemOption<T>,
            newItem: ItemOption<T>
        ): Boolean = diffCallback.areItemsTheSame(oldItem.data, newItem.data)

        override fun areContentsTheSame(
            oldItem: ItemOption<T>,
            newItem: ItemOption<T>
        ): Boolean = diffCallback.areContentsTheSame(oldItem.data, newItem.data)
    }
}
