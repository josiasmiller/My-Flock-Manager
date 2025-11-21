package com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.breeding.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.AnimalRepository
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

class FemaleBreedingDetailsViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val animalRepo: AnimalRepository
) : ViewModel() {

    companion object {
        const val EXTRA_ANIMAL_ID = "EXTRA_ANIMAL_ID"
    }

    val femaleBreedingDetails = savedStateHandle.getStateFlow<EntityId>(EXTRA_ANIMAL_ID, EntityId.UNKNOWN)
        .flatMapLatest { animalRepo.breedingDetailsForDam(it) }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun updateAnimalId(animalId: EntityId) {
        savedStateHandle[EXTRA_ANIMAL_ID] = animalId
    }
}
