package com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.genetics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.collectLatestOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.viewBinding
import com.weyr_associates.animaltrakkerfarmmobile.app.model.formatForDisplay
import com.weyr_associates.animaltrakkerfarmmobile.databinding.FragmentAnimalGeneticsBinding
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ItemAnimalGeneticCharacteristicBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalGeneticCharacteristic
import com.weyr_associates.animaltrakkerfarmmobile.model.CoatColorCharacteristic
import com.weyr_associates.animaltrakkerfarmmobile.model.CodonCharacteristic
import com.weyr_associates.animaltrakkerfarmmobile.model.GeneticCharacteristic
import com.weyr_associates.animaltrakkerfarmmobile.model.HornTypeCharacteristic
import kotlinx.coroutines.flow.StateFlow

interface AnimalGeneticsViewModelContract {
    val animalGenetics: StateFlow<List<AnimalGeneticCharacteristic>?>
}

abstract class AnimalGeneticsFragment : Fragment(R.layout.fragment_animal_genetics) {

    protected abstract val viewModel: AnimalGeneticsViewModelContract

    private val binding by viewBinding<FragmentAnimalGeneticsBinding>()
    private val animalGeneticCharacteristicsAdapter = AnimalGeneticCharacteristicsAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.recyclerAnimalGeneticCharacteristics) {
            adapter = animalGeneticCharacteristicsAdapter
            layoutManager = LinearLayoutManager(
                view.context,
                RecyclerView.VERTICAL,
                false
            )
            addItemDecoration(DividerItemDecoration(view.context, DividerItemDecoration.VERTICAL))
        }
        collectLatestOnStart(viewModel.animalGenetics) { animalGenetics ->
            animalGeneticCharacteristicsAdapter.submitList(animalGenetics) {
                updateDisplay(hasList = animalGenetics != null)
            }
        }
    }

    private fun updateDisplay(hasList: Boolean) {
        binding.recyclerAnimalGeneticCharacteristics.isVisible = hasList && animalGeneticCharacteristicsAdapter.currentList.isNotEmpty()
        binding.textNoGeneticCharacteristicsFound.isVisible = hasList && animalGeneticCharacteristicsAdapter.currentList.isEmpty()
    }

    private class AnimalGeneticCharacteristicsAdapter : ListAdapter<AnimalGeneticCharacteristic, AnimalGeneticCharacteristicViewHolder>(
        AnimalGeneticCharacteristicDiffer()
    ) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimalGeneticCharacteristicViewHolder {
            return AnimalGeneticCharacteristicViewHolder(
                ItemAnimalGeneticCharacteristicBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: AnimalGeneticCharacteristicViewHolder, position: Int) {
            holder.bind(currentList[position])
        }
    }

    private class AnimalGeneticCharacteristicViewHolder(
        private val binding: ItemAnimalGeneticCharacteristicBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(characteristic: AnimalGeneticCharacteristic) {
            binding.textCharacteristicName.text = characteristic.geneticCharacteristic.name
            binding.textCharacteristicValue.text = characteristic.geneticCharacteristic.displayValue
            binding.textCalculationMethod.text = characteristic.calculationMethod.name
            binding.textTimestamp.text = characteristic.dateTime.formatForDisplay()
        }

        private val GeneticCharacteristic.displayValue: String get() = when (this) {
            is CodonCharacteristic -> alleles
            is CoatColorCharacteristic -> coatColor
            is HornTypeCharacteristic -> hornType
        }
    }

    private class AnimalGeneticCharacteristicDiffer : DiffUtil.ItemCallback<AnimalGeneticCharacteristic>() {
        override fun areItemsTheSame(
            oldItem: AnimalGeneticCharacteristic,
            newItem: AnimalGeneticCharacteristic
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: AnimalGeneticCharacteristic,
            newItem: AnimalGeneticCharacteristic
        ): Boolean {
            return oldItem.geneticCharacteristicId == newItem.geneticCharacteristicId &&
                    oldItem.geneticCharacteristicValueId == newItem.geneticCharacteristicValueId &&
                    oldItem.calculationMethod.id == newItem.calculationMethod.id &&
                    oldItem.date == newItem.date &&
                    oldItem.time == newItem.time
        }
    }
}
