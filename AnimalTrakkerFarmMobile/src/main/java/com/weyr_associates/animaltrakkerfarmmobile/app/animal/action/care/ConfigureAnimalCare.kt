package com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.care

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

object ConfigureAnimalCare {

    const val EXTRA_ANIMAL_CARE_CONFIG = "EXTRA_ANIMAL_CARE_CONFIG"

    class Contract : ActivityResultContract<AnimalCareConfiguration?, AnimalCareConfiguration?>() {

        override fun createIntent(context: Context, input: AnimalCareConfiguration?): Intent {
            return AnimalCareConfigurationActivity.newIntent(context, input)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): AnimalCareConfiguration? {
            return if (resultCode == Activity.RESULT_OK && intent != null) {
                intent.getParcelableExtra(EXTRA_ANIMAL_CARE_CONFIG)
            } else null
        }
    }
}
