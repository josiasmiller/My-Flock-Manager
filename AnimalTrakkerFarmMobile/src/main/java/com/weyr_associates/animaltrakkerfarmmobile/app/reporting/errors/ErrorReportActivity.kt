package com.weyr_associates.animaltrakkerfarmmobile.app.reporting.errors

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.core.ErrorReport
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.collectLatestOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.observeOneTimeEventsOnStart
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ActivityErrorReportBinding
import java.io.File

class ErrorReportActivity : AppCompatActivity() {

    // This is a simple activity, and so we will avoid a view model for now.

    companion object {
        fun newIntent(context: Context, errorReport: ErrorReport) =
            Intent(context, ErrorReportActivity::class.java).apply {
                putExtra(EXTRA_ERROR_REPORT, errorReport)
            }
        private const val EXTRA_ERROR_REPORT = "EXTRA_ERROR_REPORT"
    }

    private val binding by lazy {
        ActivityErrorReportBinding.inflate(layoutInflater)
    }

    private val viewModel by viewModels<ErrorReportViewModel>()

    private val errorReport by lazy {
        requireNotNull(intent?.getParcelableExtra<ErrorReport>(EXTRA_ERROR_REPORT)) {
            "ErrorReport must be provided as an intent extra."
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.textAction.text = errorReport.action
        binding.textSummary.text = errorReport.summary

        val error = errorReport.error
        binding.textNoStackTraceAvailable.isVisible = error == null
        binding.textStackTrace.text = error?.stackTraceToString() ?: ""
        binding.textStackTrace.isVisible = error != null

        collectLatestOnStart(viewModel.canSaveReportToFile) {
            supportInvalidateOptionsMenu()
        }
        observeOneTimeEventsOnStart(viewModel.events) {
            handleEvents(it)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_error_report, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val menuItem = menu?.findItem(R.id.menu_item_save_to_file)
        menuItem?.let { it.isEnabled = viewModel.canSaveReportToFile.value }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_item_save_to_file) {
            viewModel.saveErrorReportToFile(errorReport)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun handleEvents(event: ErrorReportViewModel.Event) {
        when (event) {
            is ErrorReportViewModel.SaveReportSucceeded -> {
                showSaveToFileSuccess(event.file)
            }
            is ErrorReportViewModel.SaveReportFailed -> {
                showSaveToFileFailure(event.exception)
            }
        }
    }

    private fun showSaveToFileSuccess(file: File) {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_title_error_report_save_success)
            .setMessage(getString(R.string.dialog_message_error_report_save_success, file.name))
            .setPositiveButton(R.string.ok) { _, _ -> }
            .create()
            .show()
    }

    private fun showSaveToFileFailure(exception: Exception) {
        val errorReport = ErrorReport(
            action = "Save Error Report",
            summary = "Another error occurred while attempting to save an error report file.",
            error = exception
        )
        ErrorReportDialog.show(this, errorReport)
    }
}
