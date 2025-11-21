package com.weyr_associates.animaltrakkerfarmmobile.app.animal.info

import androidx.activity.result.ActivityResultLauncher
import androidx.core.view.isGone
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.alert.AddAnimalAlert
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.AnimalIdsPresenter
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ViewAnimalInfoBasicBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalBasicInfo
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalWeight
import com.weyr_associates.animaltrakkerfarmmobile.model.Premise

class AnimalBasicInfoPresenter(binding: ViewAnimalInfoBasicBinding? = null) {

    private val animalHeaderPresenter = AnimalInfoHeaderPresenter(binding?.header)
    private val animalFlockBreedPresenter = AnimalFlockBreedPresenter(binding?.animalFlockBreed)
    private val animalWeightPresenter = AnimalWeightPresenter(binding?.animalWeight)
    private val animalPremisePresenter = AnimalPremisePresenter(binding?.animalPremise)
    private val animalIdsPresenter = AnimalIdsPresenter(binding?.animalIds)

    var binding: ViewAnimalInfoBasicBinding? = binding
        set(value) {
            field = value
            animalHeaderPresenter.binding = value?.header
            animalFlockBreedPresenter.binding = value?.animalFlockBreed
            animalWeightPresenter.binding = value?.animalWeight
            animalPremisePresenter.binding = value?.animalPremise
            animalIdsPresenter.binding = value?.animalIds
            bindViews()
        }

    var displayTakeNoteButton: Boolean
        get() = animalHeaderPresenter.displayTakeNote
        set(value) { animalHeaderPresenter.displayTakeNote = value }

    var displayAddAlertButton: Boolean
        get() = animalHeaderPresenter.displayAddAlert
        set(value) { animalHeaderPresenter.displayAddAlert = value }

    var displayShowAnimalDetailsButton: Boolean
        get() = animalHeaderPresenter.displayShowDetails
        set(value) { animalHeaderPresenter.displayShowDetails = value }

    var displayFlockAndBreed: Boolean = true
        set(value) {
            if (field != value) {
                field = value
                bindViews()
            }
        }

    var displayAnimalWeight: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                bindViews()
            }
        }

    var displayAnimalPremise: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                bindViews()
            }
        }

    var displayAnimalIdInfo: Boolean = true
        set(value) {
            if (field != value) {
                field = value
                bindViews()
            }
        }

    var animalBasicInfo: AnimalBasicInfo? = null
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

    var addAnimalAlertLauncher: ActivityResultLauncher<AddAnimalAlert.Request>?
        get() = animalHeaderPresenter.addAnimalAlertLauncher
        set(value) { animalHeaderPresenter.addAnimalAlertLauncher = value }

    private fun bindViews() {
        val binding = binding ?: return
        animalHeaderPresenter.animalBasicInfo = animalBasicInfo
        animalFlockBreedPresenter.animalBasicInfo = if (displayFlockAndBreed)
            animalBasicInfo else null
        animalWeightPresenter.animalWeight = if (displayAnimalWeight)
            animalWeight else null
        animalPremisePresenter.animalPremise = if (displayAnimalPremise)
            animalPremise else null
        binding.dividerFlockBreed.isGone = !displayFlockAndBreed
        binding.animalFlockBreed.root.isGone = !displayFlockAndBreed
        binding.dividerWeight.isGone = !displayAnimalWeight
        binding.animalWeight.root.isGone = !displayAnimalWeight
        binding.dividerPremise.isGone = !displayAnimalPremise
        binding.animalPremise.root.isGone = !displayAnimalPremise
        animalIdsPresenter.idInfoItems = if (displayAnimalIdInfo)
            animalBasicInfo?.ids else null
        binding.animalIds.root.isGone = !displayAnimalIdInfo
    }
}
