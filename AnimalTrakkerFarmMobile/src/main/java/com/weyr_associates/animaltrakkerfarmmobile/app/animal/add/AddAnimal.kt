package com.weyr_associates.animaltrakkerfarmmobile.app.animal.add

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId

object AddAnimal {

    const val ACTION_ADD_AND_SELECT = "ACTION_ADD_AND_SELECT"

    const val EXTRA_PRIMARY_ID_TYPE_ID = "EXTRA_PRIMARY_ID_TYPE_ID"
    const val EXTRA_PRIMARY_ID_NUMBER = "EXTRA_PRIMARY_ID_NUMBER"

    const val EXTRA_RESULTING_ANIMAL_NAME = "EXTRA_RESULTING_ANIMAL_NAME"
    const val EXTRA_RESULTING_ANIMAL_ID = "EXTRA_RESULTING_ANIMAL_ID"

    data class Request(
        val idTypeId: EntityId,
        val idNumber: String
    )

    data class Result(
        val animalId: EntityId,
        val animalName: String
    )

    class Contract : ActivityResultContract<Request, Result?>() {
        override fun createIntent(context: Context, input: Request): Intent {
            return SimpleAddAnimalActivity.newIntentToAddAndSelect(
                context,
                input.idTypeId,
                input.idNumber
            )
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Result? {
            return intent?.getParcelableExtra<EntityId>(EXTRA_RESULTING_ANIMAL_ID)?.let {
                Result(it, intent.getStringExtra(EXTRA_RESULTING_ANIMAL_NAME) ?: "")
            }
        }
    }
}
