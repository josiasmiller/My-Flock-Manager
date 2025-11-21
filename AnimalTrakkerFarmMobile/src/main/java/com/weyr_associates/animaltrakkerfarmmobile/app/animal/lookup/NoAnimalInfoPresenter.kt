package com.weyr_associates.animaltrakkerfarmmobile.app.animal.lookup

import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ViewLookupAnimalInfoNoneBinding

class NoAnimalInfoPresenter(binding: ViewLookupAnimalInfoNoneBinding? = null) {

    var binding: ViewLookupAnimalInfoNoneBinding? = binding
        set(value) {
            field = value
            bindViews()
        }

    var animalInfoState: LookupAnimalInfo.AnimalInfoState? = null
        set(value) {
            field = value
            bindViews()
        }

    var onAddAnimalWithEIDClicked: ((String) -> Unit)? = null

    private fun bindViews() {
        val binding = binding ?: return
        when (val animalInfoState = animalInfoState) {
            null -> {
                binding.setupToHide()
            }
            LookupAnimalInfo.AnimalInfoState.Initial -> {
                binding.setupForInitialLookup()
            }
            is LookupAnimalInfo.AnimalInfoState.Loaded -> {
                binding.setupToHide()
            }
            is LookupAnimalInfo.AnimalInfoState.NotFound -> {
                when (val lookup = animalInfoState.lookup) {
                    is LookupAnimalInfo.Lookup.ByAnimalId -> {
                        binding.setupForAnimalNotFound()
                    }
                    is LookupAnimalInfo.Lookup.ByScannedEID -> {
                        binding.setupForAnimalWithEIDNotFound(lookup.eidNumber) {
                            onAddAnimalWithEIDClicked?.invoke(lookup.eidNumber)
                        }
                    }
                }
            }
        }
    }

    private fun ViewLookupAnimalInfoNoneBinding.setupToHide() {
        btnAddAnimal.isGone = true
        textNoAnimal.text = ""
    }

    private fun ViewLookupAnimalInfoNoneBinding.setupForInitialLookup() {
        btnAddAnimal.isGone = true
        textNoAnimal.setText(R.string.text_no_animal_loaded_scan_or_lookup)
    }

    private fun ViewLookupAnimalInfoNoneBinding.setupForAnimalNotFound() {
        textNoAnimal.setText(R.string.text_no_animal_found_for_animal_id)
        btnAddAnimal.isGone = true
        btnAddAnimal.setOnClickListener(null)
    }

    private fun ViewLookupAnimalInfoNoneBinding.setupForAnimalWithEIDNotFound(eidNumber: String, onAddAnimal: () -> Unit) {
        textNoAnimal.text = root.context.getString(R.string.text_no_animal_found_for_scanned_eid, eidNumber)
        btnAddAnimal.isVisible = true
        btnAddAnimal.setOnClickListener {
            onAddAnimal.invoke()
        }
    }
}
