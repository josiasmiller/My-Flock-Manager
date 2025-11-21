package com.weyr_associates.animaltrakkerfarmmobile.app.animal.info

import androidx.core.view.isGone
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.model.formatForDisplay
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ViewAnimalInfoSectionLifetimeBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalLifetime

class AnimalLifetimePresenter(binding: ViewAnimalInfoSectionLifetimeBinding? = null) {

    var binding: ViewAnimalInfoSectionLifetimeBinding? = binding
        set(value) {
            field = value
            bindViews()
        }

    var animalLifetime: AnimalLifetime? = null
        set(value) {
            field = value
            bindViews()
        }

    private fun bindViews() {
        val binding = binding ?: return
        val animalLifetime = animalLifetime
        binding.textBirthDate.text = animalLifetime?.birthDate?.formatForDisplay() ?: ""
        binding.textDeathDate.text = when {
            animalLifetime == null -> ""
            animalLifetime.death == null -> {
                binding.root.context.getString(R.string.text_not_applicable)
            }
            else -> animalLifetime.death.date.formatForDisplay()
        }
        val hideDeathReason = animalLifetime?.death == null
        binding.dividerDeathReason.isGone = hideDeathReason
        binding.textDeathReasonLabel.isGone = hideDeathReason
        binding.textDeathReason.isGone = hideDeathReason
        binding.textDeathReason.text = animalLifetime?.death?.reason
    }
}
