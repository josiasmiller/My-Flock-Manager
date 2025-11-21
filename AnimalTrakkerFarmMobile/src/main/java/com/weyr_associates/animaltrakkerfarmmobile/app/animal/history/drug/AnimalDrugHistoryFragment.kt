package com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.drug

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.collectLatestOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.viewBinding
import com.weyr_associates.animaltrakkerfarmmobile.app.model.formatForDisplay
import com.weyr_associates.animaltrakkerfarmmobile.app.model.itemCallbackUsingOnlyIdentity
import com.weyr_associates.animaltrakkerfarmmobile.databinding.FragmentAnimalDrugHistoryBinding
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ItemAnimalDrugEventBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalDrugEvent
import kotlinx.coroutines.flow.StateFlow

interface AnimalDrugHistoryViewModelContract {
    val animalDrugHistory: StateFlow<List<AnimalDrugEvent>?>
}

abstract class AnimalDrugHistoryFragment : Fragment(R.layout.fragment_animal_drug_history) {

    protected abstract val viewModel: AnimalDrugHistoryViewModelContract

    private val binding by viewBinding<FragmentAnimalDrugHistoryBinding>()
    private val drugEventsAdapter = AnimalDrugEventsAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.recyclerAnimalDrugHistory) {
            adapter = drugEventsAdapter
            layoutManager = LinearLayoutManager(
                view.context,
                RecyclerView.VERTICAL,
                false
            )
            addItemDecoration(DividerItemDecoration(view.context, DividerItemDecoration.VERTICAL))
        }
        collectLatestOnStart(viewModel.animalDrugHistory) { animalDrugEvents ->
            drugEventsAdapter.submitList(animalDrugEvents) {
                updateDisplay(hasList = animalDrugEvents != null)
            }
        }
    }

    private fun updateDisplay(hasList: Boolean) {
        binding.recyclerAnimalDrugHistory.isVisible = hasList && drugEventsAdapter.currentList.isNotEmpty()
        binding.textNoDrugHistoryFound.isVisible = hasList && drugEventsAdapter.currentList.isEmpty()
    }

    private class AnimalDrugEventsAdapter : ListAdapter<AnimalDrugEvent, AnimalDrugEventViewHolder>(
        itemCallbackUsingOnlyIdentity()
    ) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimalDrugEventViewHolder {
            return AnimalDrugEventViewHolder(
                ItemAnimalDrugEventBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: AnimalDrugEventViewHolder, position: Int) {
            holder.bind(currentList[position])
        }
    }

    private class AnimalDrugEventViewHolder(
        private val binding: ItemAnimalDrugEventBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(animalDrugEvent: AnimalDrugEvent) {
            binding.textEventDate.text = animalDrugEvent.eventDate.formatForDisplay()
            binding.textEventTime.text = animalDrugEvent.eventTime?.formatForDisplay()
            @SuppressLint("SetTextI18n")
            binding.textEventDescription.text = "${animalDrugEvent.tradeDrugName} ${animalDrugEvent.drugLot}"
        }
    }
}
