package com.weyr_associates.animaltrakkerfarmmobile.app.model

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val DISPLAY_FORMAT_DATE_TIME: DateTimeFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy hh:mm:ss a")

fun LocalDateTime.formatForDisplay(): String {
    return format(DISPLAY_FORMAT_DATE_TIME)
}
