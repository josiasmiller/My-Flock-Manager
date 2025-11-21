package com.weyr_associates.animaltrakkerfarmmobile.app.animal.lookup

import androidx.activity.result.ActivityResultLauncher
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.alert.AddAnimalAlert
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.info.AnimalBasicInfoPresenter
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ViewLookupAnimalInfoBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalWeight
import com.weyr_associates.animaltrakkerfarmmobile.model.Premise

class LookupAnimalInfoPresenter(binding: ViewLookupAnimalInfoBinding? = null) {

    private val animalBasicInfoPresenter = AnimalBasicInfoPresenter(binding?.animalBasicInfo)
    private val noAnimalInfoPresenter = NoAnimalInfoPresenter(binding?.noAnimalInfo)

    var binding: ViewLookupAnimalInfoBinding? = binding
        set(value) {
            field = value
            animalBasicInfoPresenter.binding = binding?.animalBasicInfo
            noAnimalInfoPresenter.binding = binding?.noAnimalInfo
            bindViews()
        }

    var displayAnimalDetailsButton: Boolean
        get() = animalBasicInfoPresenter.displayShowAnimalDetailsButton
        set(value) { animalBasicInfoPresenter.displayShowAnimalDetailsButton = value }

    var displayFlockAndBreed: Boolean
        get() = animalBasicInfoPresenter.displayFlockAndBreed
        set(value) { animalBasicInfoPresenter.displayFlockAndBreed = value }

    var displayAnimalWeight: Boolean
        get() = animalBasicInfoPresenter.displayAnimalWeight
        set(value) { animalBasicInfoPresenter.displayAnimalWeight = value }

    var displayAnimalPremise: Boolean
        get() = animalBasicInfoPresenter.displayAnimalPremise
        set(value) { animalBasicInfoPresenter.displayAnimalPremise = value }

    var displayAnimalIdInfo: Boolean
        get() = animalBasicInfoPresenter.displayAnimalIdInfo
        set(value) { animalBasicInfoPresenter.displayAnimalIdInfo = value }

    var animalInfoState: LookupAnimalInfo.AnimalInfoState? = null
        set(value) {
            field = value
            bindViews()
        }

    var animalPremise: Premise? = null
        set(value) {
            field = value
            bindViews()
        }

    var animalWeight: AnimalWeight? = null
        set(value) {
            field = value
            bindViews()
        }

    var onAddAnimalWithEIDClicked: ((String) -> Unit)? = null
        set(value) {
            field = value
            noAnimalInfoPresenter.onAddAnimalWithEIDClicked = value
        }

    var addAnimalAlertLauncher: ActivityResultLauncher<AddAnimalAlert.Request>?
        get() = animalBasicInfoPresenter.addAnimalAlertLauncher
        set(value) { animalBasicInfoPresenter.addAnimalAlertLauncher = value }

    private fun bindViews() {
        val binding = binding ?: return
        noAnimalInfoPresenter.animalInfoState = animalInfoState
        animalBasicInfoPresenter.animalWeight = animalWeight
        animalBasicInfoPresenter.animalPremise = animalPremise
        when (val animalInfoState = animalInfoState) {
            null -> {
                animalBasicInfoPresenter.animalBasicInfo = null
                binding.animalBasicInfo.root.isInvisible = true
                binding.noAnimalInfo.root.isGone = true
            }
            LookupAnimalInfo.AnimalInfoState.Initial -> {
                animalBasicInfoPresenter.animalBasicInfo = null
                binding.animalBasicInfo.root.isInvisible = true
                binding.noAnimalInfo.root.isVisible = true
            }
            is LookupAnimalInfo.AnimalInfoState.Loaded -> {
                animalBasicInfoPresenter.animalBasicInfo = animalInfoState.animalBasicInfo
                binding.animalBasicInfo.root.isVisible = true
                binding.noAnimalInfo.root.isGone = true
            }
            is LookupAnimalInfo.AnimalInfoState.NotFound -> {
                animalBasicInfoPresenter.animalBasicInfo = null
                binding.animalBasicInfo.root.isInvisible = true
                binding.noAnimalInfo.root.isVisible = true
            }
        }
    }
}
