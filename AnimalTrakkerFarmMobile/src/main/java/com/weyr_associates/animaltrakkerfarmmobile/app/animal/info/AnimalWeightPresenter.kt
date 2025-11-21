package com.weyr_associates.animaltrakkerfarmmobile.app.animal.info

import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.model.formatForDisplay
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ViewAnimalInfoSectionWeightBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalWeight

class AnimalWeightPresenter(binding: ViewAnimalInfoSectionWeightBinding? = null) {

    var binding: ViewAnimalInfoSectionWeightBinding? = binding
        set(value) {
            field = value
            bindViews()
        }

    var animalWeight: AnimalWeight? = null
        set(value) {
            field = value
            bindViews()
        }

    private fun bindViews() {
        val binding = binding ?: return
        val animalWeight = animalWeight
        if (animalWeight != null) {
            "${animalWeight.weight} ${animalWeight.unitsAbbreviation}"
                .also { binding.textWeight.text = it }
            binding.textWeighedOn.text = animalWeight.weighedOn.formatForDisplay()
        } else {
            binding.root.context.getString(R.string.text_not_applicable).also {
                binding.textWeight.text = it
                binding.textWeighedOn.text = it
            }
        }
    }
}
