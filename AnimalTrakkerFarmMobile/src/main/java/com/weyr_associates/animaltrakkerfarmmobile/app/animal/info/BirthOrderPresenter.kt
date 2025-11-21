package com.weyr_associates.animaltrakkerfarmmobile.app.animal.info

import android.content.Context
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.core.text.NumericOrder

object BirthOrderPresenter {
    fun stringForDisplay(context: Context, birthOrder: Int): String {
        return when {
            birthOrder < 1 -> context.getString(R.string.text_unknown)
            else -> "${birthOrder}${NumericOrder.suffixForOrdinal(context, birthOrder)}"
        }
    }
}
