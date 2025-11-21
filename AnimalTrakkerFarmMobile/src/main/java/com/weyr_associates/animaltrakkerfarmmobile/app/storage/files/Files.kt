package com.weyr_associates.animaltrakkerfarmmobile.app.storage.files

import java.time.format.DateTimeFormatter

object Files {
    val TIME_STAMP_FORMAT_IN_FILE_NAME: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
}
