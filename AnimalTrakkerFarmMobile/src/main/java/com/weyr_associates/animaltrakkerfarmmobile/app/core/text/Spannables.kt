package com.weyr_associates.animaltrakkerfarmmobile.app.core.text

import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan

fun String.styledBold(): SpannableString {
    return SpannableString(this).apply {
        setSpan(StyleSpan(Typeface.BOLD), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}
