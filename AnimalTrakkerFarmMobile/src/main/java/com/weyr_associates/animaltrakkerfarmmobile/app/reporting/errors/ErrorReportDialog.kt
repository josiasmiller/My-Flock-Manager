package com.weyr_associates.animaltrakkerfarmmobile.app.reporting.errors

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.core.ErrorReport

object ErrorReportDialog {
    fun show(context: Context, errorReport: ErrorReport) {
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.dialog_title_format_error_report, errorReport.action))
            .setMessage(context.getString(R.string.dialog_message_format_error_report, errorReport.summary))
            .setNeutralButton(R.string.ok) { _, _ -> }
            .setPositiveButton(R.string.text_show) { _, _ ->
                context.startActivity(ErrorReportActivity.newIntent(context, errorReport))
            }
            .create()
            .show()
    }
}
