package com.weyr_associates.animaltrakkerfarmmobile.app.animal.care.drugs

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.weyr_associates.animaltrakkerfarmmobile.model.Drug

object AddDrug {

    const val ACTION_ADD_DRUG_AND_SELECT = "ACTION_ADD_DRUG_AND_SELECT"
    const val EXTRA_RESULTING_DRUG = "EXTRA_RESULTING_DRUG"

    class Contact : ActivityResultContract<Unit, Drug?>() {
        override fun createIntent(context: Context, input: Unit): Intent {
            return AddDrugActivity.newIntentToAddAndSelect(context)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Drug? {
            return intent?.takeIf { resultCode == Activity.RESULT_OK }
                ?.getParcelableExtra(EXTRA_RESULTING_DRUG)
        }
    }
}
