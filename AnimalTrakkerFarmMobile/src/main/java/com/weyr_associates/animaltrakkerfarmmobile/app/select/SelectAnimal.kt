package com.weyr_associates.animaltrakkerfarmmobile.app.select

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.SexStandard

object SelectAnimal {
    const val EXTRA_SEX_STANDARD = "EXTRA_SEX_STANDARD"
    const val EXTRA_SELECTED_ANIMAL_ID = "EXTRA_SELECTED_ANIMAL_ID"

    class Contract : ActivityResultContract<SexStandard?, EntityId?>() {
        override fun createIntent(context: Context, input: SexStandard?): Intent {
            return SelectAnimalActivity.newIntent(context, input)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): EntityId? {
            return intent?.getParcelableExtra<EntityId>(EXTRA_SELECTED_ANIMAL_ID)
                ?.takeIf { it.isValid }
        }
    }
}
