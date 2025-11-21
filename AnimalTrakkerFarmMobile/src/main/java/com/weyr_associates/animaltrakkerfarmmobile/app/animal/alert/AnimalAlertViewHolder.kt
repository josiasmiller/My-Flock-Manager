package com.weyr_associates.animaltrakkerfarmmobile.app.animal.alert

import androidx.recyclerview.widget.RecyclerView
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ItemAnimalAlertBinding

class AnimalAlertViewHolder(private val binding: ItemAnimalAlertBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(animalAlertItem: AnimalAlertItem) {
        binding.textAlertHeader.text = animalAlertItem.header
        binding.textAlertContent.text = animalAlertItem.content
    }
}
