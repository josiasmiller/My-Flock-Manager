package com.weyr_associates.animaltrakkerfarmmobile.app.animal.info

import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.AnimalIdsPresenter
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ViewAnimalInfoDetailedBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalDetails

class AnimalDetailedInfoPresenter(binding: ViewAnimalInfoDetailedBinding? = null) {

    private val animalInfoHeaderPresenter = AnimalInfoHeaderPresenter(binding?.header).apply {
        displayShowDetails = false
    }
    private val animalFlockBreedPresenter = AnimalFlockBreedPresenter(binding?.animalFlockBreed)
    private val animalLifetimePresenter = AnimalLifetimePresenter(binding?.animalLifetime)
    private val animalRearingPresenter = AnimalRearingPresenter(binding?.animalRearing)
    private val animalParentagePresenter = AnimalParentagePresenter(binding?.animalParentage)
    private val animalWeightPresenter = AnimalWeightPresenter(binding?.animalWeight)
    private val animalIdsPresenter = AnimalIdsPresenter(binding?.animalIds)

    var binding: ViewAnimalInfoDetailedBinding? = binding
        set(value) {
            field = value
            animalInfoHeaderPresenter.binding = binding?.header
            animalFlockBreedPresenter.binding = binding?.animalFlockBreed
            animalLifetimePresenter.binding = binding?.animalLifetime
            animalRearingPresenter.binding = binding?.animalRearing
            animalParentagePresenter.binding = binding?.animalParentage
            animalWeightPresenter.binding = binding?.animalWeight
            animalIdsPresenter.binding = binding?.animalIds
            spreadValues()
        }

    var animalDetails: AnimalDetails? = null
        set(value) {
            field = value
            spreadValues()
        }

    private fun spreadValues() {
        animalInfoHeaderPresenter.animalBasicInfo = animalDetails?.basicInfo
        animalFlockBreedPresenter.animalBasicInfo = animalDetails?.basicInfo
        animalLifetimePresenter.animalLifetime = animalDetails?.lifetime
        animalRearingPresenter.animalRearing = animalDetails?.rearing
        animalParentagePresenter.animalParentage = animalDetails?.parentage
        animalWeightPresenter.animalWeight = animalDetails?.weight
        animalIdsPresenter.idInfoItems = animalDetails?.basicInfo?.ids
        val binding = binding ?: return
        binding.textOwnerName.text = animalDetails?.basicInfo?.ownerName ?: ""
        binding.textAnimalBreederName.text = animalDetails?.breeders?.animalBreederInfo?.breederName ?: ""
        binding.textSireOwnerName.text = animalDetails?.parentage?.sireInfo?.ownerName ?: ""
        binding.textSireBreederName.text = animalDetails?.breeders?.sireBreederInfo?.breederName ?: ""
        binding.textDamOwnerName.text = animalDetails?.parentage?.damInfo?.ownerName ?: ""
        binding.textDamBreederName.text = animalDetails?.breeders?.damBreederInfo?.breederName ?: ""
    }
}
