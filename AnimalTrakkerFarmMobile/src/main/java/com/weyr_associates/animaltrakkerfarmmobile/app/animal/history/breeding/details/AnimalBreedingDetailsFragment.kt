package com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.breeding.details

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.commitNow
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.collectLatestOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.viewBinding
import com.weyr_associates.animaltrakkerfarmmobile.databinding.FragmentAnimalBreedingDetailsBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import kotlinx.coroutines.flow.StateFlow

sealed interface BreedingDetailsState

data object NoBreedingDetailsState : BreedingDetailsState

data class MaleBreedingDetailsState(
    val animalId: EntityId
) : BreedingDetailsState

data class FemaleBreedingDetailsState(
    val animalId: EntityId
) : BreedingDetailsState

interface AnimalBreedingDetailsViewModelContract {
    val breedingDetailsState: StateFlow<BreedingDetailsState>
}

abstract class AnimalBreedingDetailsFragment : Fragment(R.layout.fragment_animal_breeding_details) {

    protected abstract val viewModel: AnimalBreedingDetailsViewModelContract
    private val binding by viewBinding<FragmentAnimalBreedingDetailsBinding>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        collectLatestOnStart(viewModel.breedingDetailsState) { breedingDetailsState ->
            updateDisplay(breedingDetailsState)
        }
    }

    private fun updateDisplay(breedingDetailsState: BreedingDetailsState) {
        when (breedingDetailsState) {
            NoBreedingDetailsState -> removeBreedingDetailsFragment()
            is MaleBreedingDetailsState -> setupMaleBreedingDetails(breedingDetailsState.animalId)
            is FemaleBreedingDetailsState -> setupFemaleBreedingDetails(breedingDetailsState.animalId)
        }
    }

    private fun removeBreedingDetailsFragment() {
        with(childFragmentManager) {
            findFragmentById(R.id.container_breeding_details)?.let {
                childFragmentManager.commitNow {
                    remove(it)
                }
            }
        }
        binding.textNoBreedingDetailsFound.isVisible = true
    }

    private fun setupMaleBreedingDetails(animalId: EntityId) {
        binding.textNoBreedingDetailsFound.isVisible = false
        childFragmentManager.commitNow {
            replace(R.id.container_breeding_details, MaleBreedingDetailsFragment.newInstance(animalId))
        }
    }

    private fun setupFemaleBreedingDetails(animalId: EntityId) {
        binding.textNoBreedingDetailsFound.isVisible = false
        childFragmentManager.commitNow {
            replace(R.id.container_breeding_details, FemaleBreedingDetailsFragment.newInstance(animalId))
        }
    }
}
