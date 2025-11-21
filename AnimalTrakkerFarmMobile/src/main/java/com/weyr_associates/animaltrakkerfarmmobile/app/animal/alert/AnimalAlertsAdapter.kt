package com.weyr_associates.animaltrakkerfarmmobile.app.animal.alert

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.weyr_associates.animaltrakkerfarmmobile.app.model.itemCallbackUsingOnlyIdentity
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ItemAnimalAlertBinding

class AnimalAlertsAdapter : ListAdapter<AnimalAlertItem, AnimalAlertViewHolder>(itemCallbackUsingOnlyIdentity()) {

    override fun getItemCount(): Int {
        return currentList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimalAlertViewHolder {
        return AnimalAlertViewHolder(
            ItemAnimalAlertBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: AnimalAlertViewHolder, position: Int) {
        holder.bind(currentList[position])
    }
}
