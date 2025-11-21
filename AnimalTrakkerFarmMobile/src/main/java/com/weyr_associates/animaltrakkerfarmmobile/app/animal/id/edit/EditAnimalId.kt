package com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.edit

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalBasicInfo
import com.weyr_associates.animaltrakkerfarmmobile.model.IdInfo

object EditAnimalId {

    const val EXTRA_ANIMAL_ID_TO_EDIT = "EXTRA_ANIMAL_ID_TO_EDIT"
    const val EXTRA_ANIMAL_INFO = "EXTRA_ANIMAL_INFO"

    data class Request(
        val animalIdToEdit: IdInfo,
        val animalBasicInfo: AnimalBasicInfo
    )

    class Contract : ActivityResultContract<Request, Boolean>() {
        override fun createIntent(context: Context, input: Request): Intent {
            return EditAnimalIdActivity.newIntent(context, input.animalIdToEdit, input.animalBasicInfo)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
            return resultCode == Activity.RESULT_OK
        }
    }
}