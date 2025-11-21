package com.weyr_associates.animaltrakkerfarmmobile.app.animal.alert

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId

object AddAnimalAlert {
    const val EXTRA_ANIMAL_ID = "EXTRA_ANIMAL_ID"
    const val EXTRA_ANIMAL_NAME = "EXTRA_ANIMAL_NAME"

    data class Request(
        val animalId: EntityId,
        val animalName: String
    )

    data class Result(
        val animalId: EntityId,
        val success: Boolean
    )

    class Contract : ActivityResultContract<Request, Result>() {
        override fun createIntent(context: Context, input: Request): Intent {
            return AddAnimalAlertActivity.newIntent(context, input.animalId, input.animalName)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Result {
            val isSuccess = resultCode == Activity.RESULT_OK
            return Result(
                animalId = if (isSuccess) {
                    requireNotNull(intent).getParcelableExtra(EXTRA_ANIMAL_ID)
                        ?: EntityId.UNKNOWN
                } else EntityId.UNKNOWN,
                success = isSuccess
            )
        }
    }
}
