package com.weyr_associates.animaltrakkerfarmmobile.app.reporting.errors

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.observeOneTimeEventsOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.ErrorReport
import kotlinx.coroutines.flow.Flow

fun AppCompatActivity.observeErrorReports(errorReports: Flow<ErrorReport>) {
    observeOneTimeEventsOnStart(errorReports) {
        ErrorReportDialog.show(this, it)
    }
}

fun Fragment.observeErrorReports(errorReports: Flow<ErrorReport>) {
    observeOneTimeEventsOnStart(errorReports) {
        ErrorReportDialog.show(requireContext(), it)
    }
}
