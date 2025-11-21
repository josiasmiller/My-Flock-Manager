package com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.breeding.summary

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
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.details.AnimalDetailsActivity
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.collectLatestOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.viewBinding
import com.weyr_associates.animaltrakkerfarmmobile.app.model.formatForDisplay
import com.weyr_associates.animaltrakkerfarmmobile.app.model.itemCallbackUsingOnlyContent
import com.weyr_associates.animaltrakkerfarmmobile.databinding.FragmentBreedingSummaryBinding
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ItemBreedingSummaryOffspringBinding
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ItemSummaryTotalBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.BreedingSummary
import kotlinx.coroutines.flow.StateFlow

interface AnimalBreedingSummaryViewModelContract {
    val animalBreedingSummary: StateFlow<BreedingSummary?>
}

abstract class AnimalBreedingSummaryFragment : Fragment(R.layout.fragment_breeding_summary) {

    protected abstract val viewModel: AnimalBreedingSummaryViewModelContract

    private val binding by viewBinding<FragmentBreedingSummaryBinding>()

    private val breedingSummaryBirthsAdapter = BreedingSummaryTotalsAdapter()
    private val breedingSummaryWeanedAdapter = BreedingSummaryTotalsAdapter()
    private val breedingSummaryOffspringAdapter = BreedingSummaryOffspringAdapter(
        onOffspringClickedListener = ::onOffspringClicked
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.recyclerBreedingSummaryBirths) {
            adapter = breedingSummaryBirthsAdapter
            layoutManager = LinearLayoutManager(
                view.context,
                RecyclerView.VERTICAL,
                false
            )
        }
        with(binding.recyclerBreedingSummaryWeaned) {
            adapter = breedingSummaryWeanedAdapter
            layoutManager = LinearLayoutManager(
                view.context,
                RecyclerView.VERTICAL,
                false
            )
        }
        with(binding.recyclerBreedingSummaryOffspring) {
            adapter = breedingSummaryOffspringAdapter
            layoutManager = LinearLayoutManager(
                view.context,
                RecyclerView.VERTICAL,
                false
            )
            addItemDecoration(DividerItemDecoration(view.context, DividerItemDecoration.VERTICAL))
        }
        collectLatestOnStart(viewModel.animalBreedingSummary) { breedingSummary ->
            breedingSummaryBirthsAdapter.submitList(breedingSummary?.born, ::updateDisplay)
            breedingSummaryWeanedAdapter.submitList(breedingSummary?.weaned, ::updateDisplay)
            breedingSummaryOffspringAdapter.submitList(breedingSummary?.offspring, ::updateDisplay)
        }
    }

    private fun updateDisplay() {
        val hasBirths = breedingSummaryBirthsAdapter.currentList.isNotEmpty()
        val hasWeaned = breedingSummaryWeanedAdapter.currentList.isNotEmpty()
        val hasOffspring = breedingSummaryOffspringAdapter.currentList.isNotEmpty()
        val hasSummary = hasBirths || hasWeaned || hasOffspring
        binding.containerBreedingSummary.isVisible = hasSummary
        binding.textNoBreedingSummaryFound.isVisible = !hasSummary
        binding.containerTotalsBirths.isVisible = hasSummary && hasBirths
        binding.containerTotalsWeaned.isVisible = hasSummary && hasWeaned
        binding.recyclerBreedingSummaryOffspring.isVisible = hasSummary && hasOffspring
    }

    private fun onOffspringClicked(offSpring: BreedingSummary.Offspring) {
        startActivity(AnimalDetailsActivity.newIntent(requireContext(), offSpring.animalId))
    }

    private class BreedingSummaryTotalsAdapter : ListAdapter<BreedingSummary.Total, BreedingSummaryTotalViewHolder>(
        itemCallbackUsingOnlyContent<BreedingSummary.Total>()
    ) {
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): BreedingSummaryTotalViewHolder {
            return BreedingSummaryTotalViewHolder(
                ItemSummaryTotalBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: BreedingSummaryTotalViewHolder, position: Int) {
            holder.bind(currentList[position])
        }
    }

    private class BreedingSummaryOffspringAdapter(
        private val onOffspringClickedListener: (BreedingSummary.Offspring) -> Unit
    ) : ListAdapter<BreedingSummary.Offspring, BreedingSummaryOffspringViewHolder>(
        itemCallbackUsingOnlyContent<BreedingSummary.Offspring>()
    ) {
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int,
        ): BreedingSummaryOffspringViewHolder {
            return BreedingSummaryOffspringViewHolder(
                ItemBreedingSummaryOffspringBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ),
                onOffspringClickedListener
            )
        }

        override fun onBindViewHolder(holder: BreedingSummaryOffspringViewHolder, position: Int) {
            holder.bind(currentList[position])
        }
    }

    private class BreedingSummaryTotalViewHolder(
        private val binding: ItemSummaryTotalBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(total: BreedingSummary.Total) {
            binding.textTotalName.text = total.name
            binding.textTotalValue.text = total.value.toString()
        }
    }

    private class BreedingSummaryOffspringViewHolder(
        private val binding: ItemBreedingSummaryOffspringBinding,
        private val onOffspringClickedListener: (BreedingSummary.Offspring) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(offspring: BreedingSummary.Offspring) {
            binding.textBirthDate.text = offspring.birthDate?.formatForDisplay() ?: "???"
            binding.textBirthTime.text = offspring.birthTime?.formatForDisplay() ?: "???"
            binding.textAnimalName.text = offspring.flockPrefix?.let { "$it ${offspring.name}" }
                ?: offspring.name
            binding.textSexName.text = offspring.sex.name
            binding.textRegistrationNumber.text = offspring.registrationNumber ?: ""
            binding.textRegistrationNumber.isVisible = offspring.registrationNumber != null
            binding.root.setOnClickListener { onOffspringClickedListener(offspring) }
        }
    }
}
