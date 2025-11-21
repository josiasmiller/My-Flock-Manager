package com.weyr_associates.animaltrakkerfarmmobile.app.core.text

import android.content.Context
import com.weyr_associates.animaltrakkerfarmmobile.R
import kotlin.math.abs

object NumericOrder {
    fun suffixForOrdinal(context: Context, position: Int): String {
        return when {
            abs(position) % 100 in 11..19 -> {
                context.getString(R.string.numeric_ordinal_suffix_other)
            }
            else -> {
                context.resources.getStringArray(R.array.numeric_ordinal_suffix)[abs(position) % 10]
            }
        }
    }
}
