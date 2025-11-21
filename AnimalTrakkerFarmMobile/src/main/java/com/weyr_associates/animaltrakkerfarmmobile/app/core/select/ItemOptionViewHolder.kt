package com.weyr_associates.animaltrakkerfarmmobile.app.core.select

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ItemOptionViewHolder<T>(
    itemView: View,
    private val onItemSelected: (T) -> Unit
) : RecyclerView.ViewHolder(itemView) {
    fun bind(item: ItemOption<T>) {
        (itemView as TextView).text = item.displayText
        itemView.setOnClickListener {
            onItemSelected.invoke(item.data)
        }
    }
}