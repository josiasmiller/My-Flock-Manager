package com.weyr_associates.animaltrakkerfarmmobile.app.core

import android.content.Context
import android.os.Parcelable
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.files.AppDirectories
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.files.Files
import kotlinx.parcelize.Parcelize
import java.io.File
import java.io.PrintWriter
import java.time.LocalDateTime

@Parcelize
data class ErrorReport(
    val action: String,
    val summary: String,
    val error: Throwable? = null,
    val timeStamp: LocalDateTime = LocalDateTime.now()
) : Parcelable {

    companion object {
        private const val REPORT_PREFIX_ERROR = "Error"
        private const val REPORT_PREFIX_CRASH = "Crash"
    }

    fun writeAsErrorReport(context: Context): File {
        return writeErrorReportToFile(
            AppDirectories.errorReportsDirectory(context),
            REPORT_PREFIX_ERROR
        )
    }

    fun writeAsCrashReport(context: Context): File {
        return writeErrorReportToFile(
            AppDirectories.crashReportDirectory(context),
            REPORT_PREFIX_CRASH
        )
    }

    private fun writeErrorReportToFile(reportDirectory: File, prefix: String): File {
        val fileNameTimeStamp = timeStamp.format(Files.TIME_STAMP_FORMAT_IN_FILE_NAME)
        val reportFile = File(reportDirectory, "${prefix}-Report-${fileNameTimeStamp}.txt")
        writeToFile(reportFile)
        return reportFile
    }

    private fun writeToFile(file: File) {
        PrintWriter(file).use { printWriter ->
            with(printWriter) {
                appendLine("")
                appendLine()
                appendLine("Action:")
                appendLine(action)
                appendLine()
                appendLine("Summary:")
                appendLine(summary)
                appendLine()
                appendLine("Stack Trace:")
                appendLine(
                    error?.stackTraceToString()
                        ?: "No stack trace information available."
                )
            }
        }
    }
}
