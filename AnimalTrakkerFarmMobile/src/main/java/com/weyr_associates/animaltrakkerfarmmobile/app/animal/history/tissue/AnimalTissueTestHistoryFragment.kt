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
import com.weyr_associates.animaltrakkerfarmmobile.databinding.FragmentTissueTestHistoryBinding
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ItemTissueTestEventBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.TissueTestEvent
import kotlinx.coroutines.flow.StateFlow

interface AnimalTissueTestHistoryViewModelContract {
    val tissueTestEventHistory: StateFlow<List<TissueTestEvent>?>
}

abstract class AnimalTissueTestHistoryFragment : Fragment(R.layout.fragment_tissue_test_history) {

    protected abstract val viewModel: AnimalTissueTestHistoryViewModelContract

    private val binding by viewBinding<FragmentTissueTestHistoryBinding>()
    private val tissueTestEventsAdapter = AnimalTissueTestEventsAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.recyclerAnimalTissueTestHistory) {
            adapter = tissueTestEventsAdapter
            layoutManager = LinearLayoutManager(
                view.context,
                RecyclerView.VERTICAL,
                false
            )
            addItemDecoration(DividerItemDecoration(view.context, DividerItemDecoration.VERTICAL))
        }
        collectLatestOnStart(viewModel.tissueTestEventHistory) { tissueTestEvents ->
            tissueTestEventsAdapter.submitList(tissueTestEvents) {
                updateDisplay(hasList = tissueTestEvents != null)
            }
        }
    }

    private fun updateDisplay(hasList: Boolean) {
        binding.recyclerAnimalTissueTestHistory.isVisible = hasList && tissueTestEventsAdapter.currentList.isNotEmpty()
        binding.textNoTissueTestHistoryFound.isVisible = hasList && tissueTestEventsAdapter.currentList.isEmpty()
    }

    private class AnimalTissueTestEventsAdapter : ListAdapter<TissueTestEvent, TissueTestEventViewHolder>(
        itemCallbackUsingOnlyIdentity()
    ) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TissueTestEventViewHolder {
            return TissueTestEventViewHolder(
                ItemTissueTestEventBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: TissueTestEventViewHolder, position: Int) {
            holder.bind(currentList[position])
        }
    }

    private class TissueTestEventViewHolder(
        private val binding: ItemTissueTestEventBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(tissueTestEvent: TissueTestEvent) {
            binding.textEventDate.text = tissueTestEvent.eventDate.formatForDisplay()
            binding.textEventTime.text = tissueTestEvent.eventTime?.formatForDisplay()
            binding.textTissueTestName.text = tissueTestEvent.tissueTestName
            binding.textLaboratoryName.text = tissueTestEvent.labCompanyName
            binding.textLabAccessionId.text = tissueTestEvent.labAscensionId
            binding.textResultsDate.text = tissueTestEvent.tissueTestResultsDate?.formatForDisplay()
            binding.textTissueTestResults.text = tissueTestEvent.tissueTestResults
            binding.textLabAccessionLabel.isVisible = tissueTestEvent.labAscensionId != null
            binding.textResultsLabel.isVisible = tissueTestEvent.tissueTestResults != null
            binding.textResultsDate.isVisible = tissueTestEvent.tissueTestResults != null
            binding.textTissueTestResults.isVisible = tissueTestEvent.tissueTestResults != null
        }
    }
}
