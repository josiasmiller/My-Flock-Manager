package com.weyr_associates.animaltrakkerfarmmobile.app.label

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PrintLabelFormatter(private val printLabelData: PrintLabelData) {

    companion object {
        private val DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    }

    fun formatData(): String {
        return printLabelData.eidNumber.replace("_", "")
    }

    fun formatData1(): String {
        return printLabelData.labelText
    }

    fun formatSheepName(): String {
        return printLabelData.secondaryIdInfo?.let {
            "${it.type.name} = ${it.number} ${it.color.name}"
        } ?: ""
    }

    fun formatDateTime(): String {
        return LocalDateTime.now().format(DATETIME_FORMAT)
    }
}
