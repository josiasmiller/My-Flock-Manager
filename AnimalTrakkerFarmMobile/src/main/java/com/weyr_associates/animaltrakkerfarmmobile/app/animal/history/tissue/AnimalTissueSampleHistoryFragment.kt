package com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.tissue

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
import com.weyr_associates.animaltrakkerfarmmobile.databinding.FragmentTissueSampleHistoryBinding
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ItemTissueSampleEventBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.TissueSampleEvent
import kotlinx.coroutines.flow.StateFlow

interface AnimalTissueSampleHistoryViewModelContract {
    val tissueSampleEventHistory: StateFlow<List<TissueSampleEvent>?>
}

abstract class AnimalTissueSampleHistoryFragment : Fragment(R.layout.fragment_tissue_sample_history) {

    protected abstract val viewModel: AnimalTissueSampleHistoryViewModelContract

    private val binding by viewBinding<FragmentTissueSampleHistoryBinding>()
    private val tissueSampleEventsAdapter = AnimalTissueSampleEventsAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.recyclerAnimalTissueSampleHistory) {
            adapter = tissueSampleEventsAdapter
            layoutManager = LinearLayoutManager(
                view.context,
                RecyclerView.VERTICAL,
                false
            )
            addItemDecoration(DividerItemDecoration(view.context, DividerItemDecoration.VERTICAL))
        }
        collectLatestOnStart(viewModel.tissueSampleEventHistory) { tissueSampleEvents ->
            tissueSampleEventsAdapter.submitList(tissueSampleEvents) {
                updateDisplay(hasList = tissueSampleEvents != null)
            }
        }
    }

    private fun updateDisplay(hasList: Boolean) {
        binding.recyclerAnimalTissueSampleHistory.isVisible = hasList && tissueSampleEventsAdapter.currentList.isNotEmpty()
        binding.textNoTissueSampleHistoryFound.isVisible = hasList && tissueSampleEventsAdapter.currentList.isEmpty()
    }

    private class AnimalTissueSampleEventsAdapter : ListAdapter<TissueSampleEvent, TissueSampleEventViewHolder>(
        itemCallbackUsingOnlyIdentity()
    ) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TissueSampleEventViewHolder {
            return TissueSampleEventViewHolder(
                ItemTissueSampleEventBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: TissueSampleEventViewHolder, position: Int) {
            holder.bind(currentList[position])
        }
    }

    private class TissueSampleEventViewHolder(
        private val binding: ItemTissueSampleEventBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(tissueSampleEvent: TissueSampleEvent) {
            binding.textEventDate.text = tissueSampleEvent.eventDate.formatForDisplay()
            binding.textEventTime.text = tissueSampleEvent.eventTime?.formatForDisplay()
            binding.textEventDescription.text = tissueSampleEvent.tissueSampleName
        }
    }
}
