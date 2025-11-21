package com.weyr_associates.animaltrakkerfarmmobile.app.animal.info

import com.weyr_associates.animaltrakkerfarmmobile.databinding.ViewAnimalInfoSectionRearingBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalRearing

class AnimalRearingPresenter(binding: ViewAnimalInfoSectionRearingBinding? = null) {

    var binding: ViewAnimalInfoSectionRearingBinding? = binding
        set(value) {
            field = value
            bindViews()
        }

    var animalRearing: AnimalRearing? = null
        set(value) {
            field = value
            bindViews()
        }

    private fun bindViews() {
        val binding = binding ?: return
        binding.textBirthOrder.text = animalRearing?.birthOrder?.let { birthOrder ->
            BirthOrderPresenter.stringForDisplay(binding.root.context, birthOrder)
        }
        binding.textBirthType.text = animalRearing?.birthType
        binding.textRearType.text = animalRearing?.rearType
    }
}
