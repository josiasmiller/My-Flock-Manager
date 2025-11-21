package com.weyr_associates.animaltrakkerfarmmobile.app.core.text

import android.text.InputFilter

object InputFilters {
    val DIGITS = InputFilter { source, start, end, _, _, _ ->
        source.subSequence(start, end).filter { it.isDigit() }
    }
}
