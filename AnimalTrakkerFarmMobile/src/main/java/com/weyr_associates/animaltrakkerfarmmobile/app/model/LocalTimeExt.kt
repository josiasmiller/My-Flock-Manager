package com.weyr_associates.animaltrakkerfarmmobile.app.model

import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val DISPLAY_FORMAT_TIME: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a")

fun LocalTime.formatForDisplay(): String {
    return format(DISPLAY_FORMAT_TIME)
}
