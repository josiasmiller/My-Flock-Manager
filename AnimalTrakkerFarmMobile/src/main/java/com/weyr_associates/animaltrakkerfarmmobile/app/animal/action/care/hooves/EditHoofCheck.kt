package com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.care.hooves

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.weyr_associates.animaltrakkerfarmmobile.model.HoofCheck

object EditHoofCheck {

    const val EXTRA_HOOF_CHECK = "EXTRA_HOOF_CHECK"

    class Contract : ActivityResultContract<HoofCheck?, HoofCheck?>() {
        override fun createIntent(context: Context, input: HoofCheck?): Intent {
            return EditHoofCheckActivity.newIntent(context, input)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): HoofCheck? {
            return if (resultCode == Activity.RESULT_OK && intent != null) {
                intent.getParcelableExtra(EXTRA_HOOF_CHECK)
            } else null
        }
    }
}
