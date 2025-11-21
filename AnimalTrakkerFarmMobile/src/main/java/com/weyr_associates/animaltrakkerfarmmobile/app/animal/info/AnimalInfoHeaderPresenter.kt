package com.weyr_associates.animaltrakkerfarmmobile.app.animal.info

import androidx.activity.result.ActivityResultLauncher
import androidx.core.view.isVisible
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.alert.AddAnimalAlert
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.alert.AddAnimalAlertActivity
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.details.AnimalDetailsActivity
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.note.TakeNotesActivity
import com.weyr_associates.animaltrakkerfarmmobile.app.model.formattedForDisplay
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ViewAnimalInfoHeaderBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalBasicInfo

class AnimalInfoHeaderPresenter(binding: ViewAnimalInfoHeaderBinding? = null) {

    var binding: ViewAnimalInfoHeaderBinding? = binding
        set(value) {
            field = value
            bindViews()
        }

    var animalBasicInfo: AnimalBasicInfo? = null
        set(value) {
            field = value
            bindViews()
        }

    var displayTakeNote: Boolean = true
        set(value) {
            if (field != value) {
                field = value
                bindViews()
            }
        }

    var displayAddAlert: Boolean = true
        set(value) {
            if (field != value) {
                field = value
                bindViews()
            }
        }

    var displayShowDetails: Boolean = true
        set(value) {
            if (field != value) {
                field = value
                bindViews()
            }
        }

    var addAnimalAlertLauncher: ActivityResultLauncher<AddAnimalAlert.Request>? = null

    private fun bindViews() {
        val binding = binding ?: return
        val context = binding.root.context
        binding.textAnimalName.text = animalBasicInfo?.name ?: ""
        binding.imageAddNote.isVisible = displayTakeNote
        binding.imageAddNote.setOnClickListener {
            animalBasicInfo?.let {
                context.startActivity(
                    TakeNotesActivity.newIntent(context, it.id)
                )
            }
        }
        binding.imageAddAlert.isVisible = displayAddAlert
        binding.imageAddAlert.setOnClickListener {
            animalBasicInfo?.let { animalBasicInfo ->
                addAnimalAlertLauncher?.launch(
                    AddAnimalAlert.Request(
                        animalBasicInfo.id,
                        animalBasicInfo.name
                    )
                ) ?: context.startActivity(
                    AddAnimalAlertActivity.newIntent(
                        context,
                        animalBasicInfo.id,
                        animalBasicInfo.name
                    )
                )
            }
        }
        binding.imageAnimalDetails.isVisible = displayShowDetails
        binding.imageAnimalDetails.setOnClickListener {
            animalBasicInfo?.let {
                context.startActivity(
                    AnimalDetailsActivity.newIntent(context, it.id)
                )
            }
        }
        binding.textAnimalSex.text = animalBasicInfo?.sexName ?: ""
        binding.textAnimalAge.text = animalBasicInfo?.animalAge?.formattedForDisplay(context) ?: ""
        binding.imageAnimalDead.isVisible = animalBasicInfo?.isDead ?: false
    }
}
