package com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.id

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.collectLatestOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.viewBinding
import com.weyr_associates.animaltrakkerfarmmobile.app.core.widget.recyclerview.OutlineDividerDecoration
import com.weyr_associates.animaltrakkerfarmmobile.app.model.formatForDisplay
import com.weyr_associates.animaltrakkerfarmmobile.app.model.itemCallbackUsingOnlyIdentity
import com.weyr_associates.animaltrakkerfarmmobile.databinding.FragmentAnimalIdHistoryBinding
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ItemAnimalIdHistoryBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.IdInfo
import kotlinx.coroutines.flow.StateFlow

interface AnimalIdHistoryViewModelContract {
    val animalIdHistory: StateFlow<List<IdInfo>?>
}

abstract class AnimalIdHistoryFragment : Fragment(R.layout.fragment_animal_id_history) {

    protected abstract val viewModel: AnimalIdHistoryViewModelContract

    private val binding by viewBinding<FragmentAnimalIdHistoryBinding>()
    private val animalIdHistoryAdapter: AnimalIdHistoryAdapter = AnimalIdHistoryAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.recyclerAnimalIdHistory) {
            adapter = animalIdHistoryAdapter
            layoutManager = LinearLayoutManager(
                view.context,
                RecyclerView.VERTICAL,
                false
            )
            addItemDecoration(OutlineDividerDecoration(view.context))
        }
        collectLatestOnStart(viewModel.animalIdHistory) { animalIdHistory ->
            animalIdHistoryAdapter.submitList(animalIdHistory) {
                updateDisplay(hasList = animalIdHistory != null)
            }
        }
    }

    private fun updateDisplay(hasList: Boolean) {
        binding.recyclerAnimalIdHistory.isVisible = hasList && animalIdHistoryAdapter.currentList.isNotEmpty()
        binding.textNoAnimalIdHistoryFound.isVisible = hasList && animalIdHistoryAdapter.currentList.isEmpty()
    }

    private class AnimalIdHistoryAdapter : ListAdapter<IdInfo, AnimalIdInfoViewHolder>(
        itemCallbackUsingOnlyIdentity<IdInfo>()
    ) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimalIdInfoViewHolder {
            return AnimalIdInfoViewHolder(
                ItemAnimalIdHistoryBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: AnimalIdInfoViewHolder, position: Int) {
            holder.bind(currentList[position])
        }
    }

    private class AnimalIdInfoViewHolder(
        private val binding: ItemAnimalIdHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(idInfo: IdInfo) {
            binding.textDateOn.text = idInfo.dateOn.formatForDisplay()
            binding.textIdNumber.text = idInfo.number
            binding.textIdTypeName.text = idInfo.type.name
            binding.textIdLocationName.text = idInfo.location.name
            binding.textIdColorName.text = idInfo.color.name
            binding.groupIdRemoval.isVisible = idInfo.removal != null
            binding.textDateOff.text = idInfo.removal?.dateOff?.formatForDisplay() ?: ""
            binding.textRemoveReason.text = idInfo.removal?.reason?.text ?: ""
            binding.dividerSectionCorrected.isVisible = idInfo.corrected
            binding.textCorrected.isVisible = idInfo.corrected
        }
    }
}
