package com.weyr_associates.animaltrakkerfarmmobile.app.animal.info

import android.annotation.SuppressLint
import android.content.Context
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ViewAnimalInfoSectionFlockBreedBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalBasicInfo

class AnimalFlockBreedPresenter(binding: ViewAnimalInfoSectionFlockBreedBinding? = null) {

    var binding: ViewAnimalInfoSectionFlockBreedBinding? = binding
        set(value) {
            field = value
            bindViews()
        }

    var animalBasicInfo: AnimalBasicInfo? = null
        set(value) {
            field = value
            bindViews()
        }

    @SuppressLint("DefaultLocale")
    private fun bindViews() {
        val binding = binding ?: return
        val context = binding.root.context
        binding.textFlockName.text = animalBasicInfo?.let {
            it.flockPrefix ?: getNotApplicableString(context)
        } ?: ""
        binding.textBreedName.text = animalBasicInfo?.let {
            "${it.breedName} ${String.format("%.0f", it.breedPercentage)}%"
        } ?: ""
    }

    private fun getNotApplicableString(context: Context): String {
        return context.getString(R.string.text_not_applicable)
    }
}
