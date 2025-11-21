package com.weyr_associates.animaltrakkerfarmmobile.app.animal.info

import com.weyr_associates.animaltrakkerfarmmobile.databinding.ViewAnimalInfoSectionParentageBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalParentage

class AnimalParentagePresenter(binding: ViewAnimalInfoSectionParentageBinding? = null) {

    var binding: ViewAnimalInfoSectionParentageBinding? = binding
        set(value) {
            field = value
            bindViews()
        }

    var animalParentage: AnimalParentage? = null
        set(value) {
            field = value
            bindViews()
        }

    private fun bindViews() {
        val binding = binding ?: return
        val parentage = animalParentage
        if (parentage != null) {
            with(binding) {
                textSireFlockName.text = parentage.sireInfo?.flockPrefix
                textSireAnimalName.text = parentage.sireInfo?.name
                textDamFlockName.text = parentage.damInfo?.flockPrefix
                textDamAnimalName.text = parentage.damInfo?.name
            }
        } else {
            with(binding) {
                textSireFlockName.text = ""
                textSireAnimalName.text = ""
                textDamFlockName.text = ""
                textDamAnimalName.text = ""
            }
        }
    }
}
