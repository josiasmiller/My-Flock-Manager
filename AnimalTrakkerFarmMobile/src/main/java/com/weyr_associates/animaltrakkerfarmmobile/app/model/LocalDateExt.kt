package com.weyr_associates.animaltrakkerfarmmobile.app.model

import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val DISPLAY_FORMAT_DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy")

fun LocalDate.formatForDisplay(): String {
    return format(DISPLAY_FORMAT_DATE)
}
