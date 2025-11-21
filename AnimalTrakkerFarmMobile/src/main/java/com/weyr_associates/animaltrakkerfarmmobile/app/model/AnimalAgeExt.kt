package com.weyr_associates.animaltrakkerfarmmobile.app.model

import android.content.Context
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalAge

fun AnimalAge.formattedForDisplay(context: Context): String {
    return context.getString(R.string.text_animal_age_format, yearsOfAge, monthsOfAge)
}
