package com.weyr_associates.animaltrakkerfarmmobile.app.animal.evaluation

import androidx.activity.result.ActivityResultLauncher
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.alert.AddAnimalAlert
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.lookup.LookupAnimalInfo
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.lookup.LookupAnimalInfoPresenter
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ViewAnimalEvaluationBinding

class AnimalEvaluationPresenter(binding: ViewAnimalEvaluationBinding? = null) {

    private val lookupAnimalInfoPresenter = LookupAnimalInfoPresenter(binding?.lookupAnimalInfo)

    var binding: ViewAnimalEvaluationBinding? = binding
        set(value) {
            field = value
            lookupAnimalInfoPresenter.binding = value?.lookupAnimalInfo
            bindViews()
        }

    var animalInfoState: LookupAnimalInfo.AnimalInfoState? = null
        set(value) {
            field = value
            bindViews()
        }

    var onAddAnimalWithEIDClicked: ((String) -> Unit)? = null
        set(value) {
            field = value
            lookupAnimalInfoPresenter.onAddAnimalWithEIDClicked = value
        }

    var addAnimalAlertLauncher: ActivityResultLauncher<AddAnimalAlert.Request>?
        get() = lookupAnimalInfoPresenter.addAnimalAlertLauncher
        set(value) { lookupAnimalInfoPresenter.addAnimalAlertLauncher = value }

    private fun bindViews() {
        val binding = binding ?: return
        lookupAnimalInfoPresenter.animalInfoState = animalInfoState
        with(binding.evaluationEditor.root) {
            when (animalInfoState) {
                is LookupAnimalInfo.AnimalInfoState.Loaded -> isVisible = true
                else -> isGone = true
            }
        }
    }
}
