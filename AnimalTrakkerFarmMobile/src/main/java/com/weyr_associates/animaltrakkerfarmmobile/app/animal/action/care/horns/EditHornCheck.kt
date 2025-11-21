package com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.care.horns

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.weyr_associates.animaltrakkerfarmmobile.model.HornCheck

object EditHornCheck {

    const val EXTRA_HORN_CHECK = "EXTRA_HORN_CHECK"

    class Contract : ActivityResultContract<HornCheck?, HornCheck?>() {
        override fun createIntent(context: Context, input: HornCheck?): Intent {
            return EditHornCheckActivity.newIntent(context, input)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): HornCheck? {
            return if (resultCode == Activity.RESULT_OK && intent != null) {
                intent.getParcelableExtra(EXTRA_HORN_CHECK)
            } else null
        }
    }
}
