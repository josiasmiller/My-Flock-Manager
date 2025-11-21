package com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.isGone
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.weyr_associates.animaltrakkerfarmmobile.BuildConfig
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.core.ErrorReport
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.coroutines.flow.observeOneTimeEvents
import com.weyr_associates.animaltrakkerfarmmobile.app.main.menu.deactivate
import com.weyr_associates.animaltrakkerfarmmobile.app.reporting.errors.ErrorReportDialog
import com.weyr_associates.animaltrakkerfarmmobile.app.reporting.errors.observeErrorReports
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseManagementViewModel.Event
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseManagementViewModel.State.Idle
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseManagementViewModel.State.Working
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseManagementViewModel.State.Working.LoadingDatabase
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseManagementViewModel.State.Working.SavingDatabase
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ActivityDatabaseManagementBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DatabaseManagementActivity : AppCompatActivity() {

    companion object {
        private const val PREFS_NAME_DATABASE_MANAGEMENT = DatabaseManager.PREFS_NAME_DATABASE_MANAGEMENT
        private const val PREFS_KEY_LAST_SELECTED_DATABASE_URI = DatabaseManager.PREFS_KEY_LAST_SELECTED_DATABASE_URI
        private const val REQUEST_CODE_OPEN_DATABASE_FILE = 1
    }

    private val binding by lazy {
        ActivityDatabaseManagementBinding.inflate(layoutInflater)
    }

    private val viewModel: DatabaseManagementViewModel by viewModels { ViewModelFactory(this) }

    private val sharedPreferences: SharedPreferences by lazy {
        getSharedPreferences(PREFS_NAME_DATABASE_MANAGEMENT, Context.MODE_PRIVATE)
    }

    private val progressDialog: ProgressDialog by lazy {
        ProgressDialog(this).apply {
            setCancelable(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        DatabaseManager.getInstance(this)
        binding.btnSelectDatabase.setOnClickListener {
            selectDatabase()
        }
        binding.btnBackupDbToDocuments.setOnClickListener {
            viewModel.saveDatabaseToDocuments()
        }
        binding.btnBackupDbToUsbDrive.setOnClickListener {
            viewModel.saveDatabaseToUSB()
        }
        with(binding.btnLoadSeedDatabase) {
            setOnClickListener { confirmLoadSeedDatabase() }
        }
        //TODO: Re-enable once this feature is complete.
        binding.btnBackupDbToUsbDrive.deactivate()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collectLatest { state ->
                    updateDisplayForState(state)
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.observeOneTimeEvents { handleEvent(it) }
            }
        }
        observeErrorReports(viewModel.errorReportFlow)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (REQUEST_CODE_OPEN_DATABASE_FILE == requestCode) {
            if (resultCode == RESULT_OK) {
                val inUri: Uri? = data?.data
                if (inUri != null) {
                    saveSelectedDatabaseUri(inUri)
                    viewModel.loadDatabaseFrom(inUri)
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun updateDisplayForState(state: DatabaseManagementViewModel.State) {
        when (state) {
            Idle -> progressDialog.dismiss()
            is Working -> {
                when (state) {
                    LoadingDatabase -> {
                        progressDialog.setTitle(R.string.dialog_title_loading_database)
                        progressDialog.setMessage(getString(R.string.dialog_message_loading_database))
                    }
                    SavingDatabase -> {
                        progressDialog.setTitle(R.string.dialog_title_saving_database)
                        progressDialog.setMessage(getString(R.string.dialog_message_saving_database))
                    }
                }
                progressDialog.show()
            }
        }
    }

    private fun selectDatabase() {
        val lastSelectedDatabaseUri = loadLastSelectedDatabaseUri()
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(
                Intent.EXTRA_MIME_TYPES,
                arrayOf("application/vnd.sqlite3", "application/octet-stream")
            )
            if (lastSelectedDatabaseUri != null) {
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, lastSelectedDatabaseUri)
            }
        }
        startActivityForResult(intent, REQUEST_CODE_OPEN_DATABASE_FILE)
    }

    private fun confirmLoadSeedDatabase() {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_title_confirm_load_seed_database)
            .setMessage(R.string.dialog_message_confirm_load_seed_database)
            .setPositiveButton(R.string.yes_label) { _, _ -> viewModel.loadSeedDatabase() }
            .setNegativeButton(R.string.no_label) { _, _ -> /*NO-OP*/ }
            .create()
            .show()
    }

    private fun loadLastSelectedDatabaseUri(): Uri? {
        return sharedPreferences.getString(PREFS_KEY_LAST_SELECTED_DATABASE_URI, null)
            ?.let { Uri.parse(it) }
    }

    private fun saveSelectedDatabaseUri(uri: Uri) {
        sharedPreferences.edit {
            putString(PREFS_KEY_LAST_SELECTED_DATABASE_URI, uri.toString())
        }
    }

    private fun handleEvent(event: Event) {
        when (event) {
            is Event.DatabaseLoadSucceeded -> {
                showDatabaseLoadSucceeded(event.numberOfAnimals)
            }
            is Event.DatabaseLoadFailed -> {
                showDatabaseLoadFailed(event.error)
            }
            is Event.DatabaseBackupSucceeded -> {
                showDatabaseBackupSuccess(event.backupFileName)
            }
            is Event.DatabaseBackupFailed -> {
                showDatabaseBackupError(event.error)
            }
            Event.NoDatabaseToBackup -> {
                showNoDatabaseToBackupMessage()
            }
        }
    }

    private fun showDatabaseLoadSucceeded(numberOfAnimals: Long) {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_title_database_load_succeeded)
            .setMessage(getString(R.string.dialog_message_database_load_succeeded, numberOfAnimals))
            .setPositiveButton(R.string.ok) { _, _ -> /*NO-OP*/ }
            .create()
            .show()
    }

    private fun showDatabaseLoadFailed(error: DatabaseLoadError) {
        when (error) {
            is DatabaseLoadError.UnableToReadFromDatabaseSourceFile -> {
                error.thrownError?.let {
                    ErrorReportDialog.show(
                        context = this,
                        errorReport = ErrorReport(
                            action = "Load Database",
                            summary = "An error occurred while loading your database.",
                            error = it
                        )
                    )
                } ?: AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_title_database_load_failed)
                    .setMessage(R.string.dialog_message_database_load_failed)
                    .setPositiveButton(R.string.ok) { _, _ -> /*NO-OP*/ }
                    .create()
                    .show()
            }
            is DatabaseLoadError.UnableToWriteLoadedDatabaseFile -> {
                error.thrownError?.let {
                    ErrorReportDialog.show(
                        context = this,
                        errorReport = ErrorReport(
                            action = "Load Database",
                            summary = "An error occurred while loading writing a loaded database to disk.",
                            error = it
                        )
                    )
                } ?: AlertDialog.Builder(this)
                        .setTitle(R.string.dialog_title_database_load_failed)
                        .setMessage(R.string.dialog_message_database_load_failed)
                        .setPositiveButton(R.string.ok) { _, _ -> /*NO-OP*/ }
                        .create()
                        .show()
            }
            is DatabaseLoadError.LoadedDatabaseFailedQueryCheck -> {
                error.thrownError?.let {
                    ErrorReportDialog.show(
                        context = this,
                        errorReport = ErrorReport(
                            action = "Check Loaded Database",
                            summary = "An error occurred while checking the loaded database.",
                            error = it
                        )
                    )
                } ?: AlertDialog.Builder(this)
                        .setTitle(R.string.dialog_title_database_load_query_check_failed)
                        .setMessage(R.string.dialog_message_database_load_query_check_failed)
                        .setPositiveButton(R.string.ok) { _ , _ -> /*NO-OP*/ }
                        .create()
                        .show()
            }
            is DatabaseLoadError.DatabaseValidityCheckFailed -> {
                when(error.failure) {
                    is DatabaseQueryCheckFailed -> {
                        showDatabaseQueryCheckFailed()
                    }
                    is DatabaseVersionUnsupported -> {
                        showDatabaseVersionUnsupported(error.failure)
                    }
                    is DatabaseVersionPatchRecommended -> {
                        showDatabasePatchVersionRecommended(error.failure, error.selectedDatabaseUri)
                    }
                }
            }
        }
    }

    private fun showDatabaseQueryCheckFailed() {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_title_database_load_query_check_failed)
            .setMessage(R.string.dialog_message_database_load_query_check_failed)
            .setPositiveButton(R.string.ok) { _ , _ -> /*NO-OP*/ }
            .create()
            .show()
    }

    private fun showDatabaseVersionUnsupported(failure: DatabaseVersionUnsupported) {
        val supportedVersion = failure.supportedDatabaseVersion
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_title_unsupported_database_version)
            .setMessage(
                getString(
                    R.string.dialog_message_unsupported_database_version,
                    failure.databaseVersionString,
                    "${supportedVersion.major}.${supportedVersion.minor}.x"
                )
            )
            .setPositiveButton(R.string.yes_label) { _, _ ->
                selectDatabase()
            }
            .setNegativeButton(R.string.no_label) { _, _ -> }
            .create()
            .show()
    }

    private fun showDatabasePatchVersionRecommended(
        failure: DatabaseVersionPatchRecommended,
        selectedDatabaseUri: Uri?
    ) {
        val supportedVersion = failure.supportedDatabaseVersion
        val currentVersionString = failure.databaseVersionString
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_title_database_patch_update_recommended)
            .setMessage(
                getString(
                    R.string.dialog_message_database_patch_update_recommended,
                    "${supportedVersion.major}.${supportedVersion.minor}.${supportedVersion.patch}",
                    currentVersionString
                )
            )
            .setPositiveButton(R.string.yes_label) { _, _ ->
                if (selectedDatabaseUri != null) {
                    viewModel.loadDatabaseFrom(
                        uri = selectedDatabaseUri,
                        ignorePatchVersion = true
                    )
                } else {
                    viewModel.loadSeedDatabase(
                        ignorePatchVersion = true
                    )
                }
            }
            .setNegativeButton(R.string.no_label) { _, _ -> }
            .create()
            .show()
    }

    private fun showNoDatabaseToBackupMessage() {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_title_database_backup_no_database_found)
            .setMessage(R.string.dialog_message_database_backup_no_database_found)
            .setPositiveButton(R.string.ok) { _, _ -> /*NO-OP*/ }
            .create()
            .show()
    }

    private fun showDatabaseBackupSuccess(backupFileName: String) {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_title_database_backup_complete)
            .setMessage(getString(R.string.dialog_message_database_backup_complete, backupFileName))
            .setPositiveButton(R.string.ok) { _, _ -> /*NO-OP*/ }
            .create()
            .show()
    }

    private fun showDatabaseBackupError(error: DatabaseBackupError) {
        when (error) {
            DatabaseBackupError.SourceDatabaseFileNotFound -> {
                AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_title_database_backup_no_database_found)
                    .setMessage(R.string.dialog_message_database_backup_no_database_found)
                    .setPositiveButton(R.string.ok) { _, _ -> /*NO-OP*/ }
                    .create()
                    .show()
            }
            is DatabaseBackupError.UnableToWriteBackupDatabaseFile -> {
                ErrorReportDialog.show(
                    context = this,
                    errorReport = ErrorReport(
                        action = "Backup Database",
                        summary = "An error occurred while backing up the database.",
                        error = error.thrownError
                    )
                )
            }
        }
    }
}

private class ViewModelFactory(context: Context) : ViewModelProvider.Factory {
    private val appContext = context.applicationContext
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        when (modelClass) {
            DatabaseManagementViewModel::class.java -> {
                val databaseManager = DatabaseManager.getInstance(appContext)
                @Suppress("UNCHECKED_CAST")
                return DatabaseManagementViewModel(databaseManager) as T
            }
            else -> throw IllegalStateException("Cannot create view model of type ${modelClass.simpleName}")
        }
    }
}
