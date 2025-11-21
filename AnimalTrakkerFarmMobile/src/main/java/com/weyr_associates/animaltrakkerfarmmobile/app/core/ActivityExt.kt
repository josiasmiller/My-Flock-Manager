package com.weyr_associates.animaltrakkerfarmmobile.app.core

import android.app.Activity
import android.content.Intent
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseManagementActivity
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseManager
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseQueryCheckFailed
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseValidityCheckPassed
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseVersionPatchRecommended
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseVersionUnsupported

fun Activity.hideKeyboard() {
    getSystemService(InputMethodManager::class.java).hideSoftInputFromWindow(
        window.decorView.rootView.windowToken, 0
    )
}

fun Activity.checkDatabaseValidityThen(onValid: () -> Unit) {
    val databaseManager = DatabaseManager.getInstance(this)
    if (!databaseManager.isDatabaseFilePresent()) {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_title_install_database)
            .setMessage(R.string.dialog_message_install_database)
            .setPositiveButton(R.string.yes_label) { _, _ ->
                this.startActivity(
                    Intent(
                        this,
                        DatabaseManagementActivity::class.java
                    )
                )
            }
            .setNegativeButton(R.string.no_label) { _, _ -> }
            .create()
            .show()
    } else {
        when (val validityCheckResult = databaseManager.checkDatabaseValidity()) {
            DatabaseValidityCheckPassed -> {
                onValid.invoke()
            }
            is DatabaseQueryCheckFailed -> {
                AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_title_invalid_database)
                    .setMessage(
                        getString(
                            R.string.dialog_message_invalid_database,
                            validityCheckResult.failureMessage
                        )
                    )
                    .setPositiveButton(R.string.yes_label) { _, _ ->
                        this.startActivity(Intent(this, DatabaseManagementActivity::class.java))
                    }
                    .setNegativeButton(R.string.no_label) { _, _ -> }
                    .create()
                    .show()
            }
            is DatabaseVersionUnsupported -> {
                val supportedVersion = validityCheckResult.supportedDatabaseVersion
                AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_title_unsupported_database_version)
                    .setMessage(
                        getString(
                            R.string.dialog_message_unsupported_database_version,
                            validityCheckResult.databaseVersionString,
                            "${supportedVersion.major}.${supportedVersion.minor}.x"
                        )
                    )
                    .setPositiveButton(R.string.yes_label) { _, _ ->
                        this.startActivity(Intent(this, DatabaseManagementActivity::class.java))
                    }
                    .setNegativeButton(R.string.no_label) { _, _ -> }
                    .create()
                    .show()
            }
            is DatabaseVersionPatchRecommended -> {
                if (databaseManager.hasAcceptedDatabasePatchVersionDifferences) {
                    onValid.invoke()
                    return
                }
                val supportedVersion = validityCheckResult.supportedDatabaseVersion
                AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_title_database_patch_update_recommended)
                    .setMessage(
                        getString(
                            R.string.dialog_message_database_patch_update_recommended,
                            "${supportedVersion.major}.${supportedVersion.minor}.${supportedVersion.patch}",
                            validityCheckResult.databaseVersionString
                        )
                    )
                    .setPositiveButton(R.string.yes_label) { _, _ ->
                        databaseManager.acceptDatabasePatchVersionDifferences()
                        onValid.invoke()
                    }
                    .setNegativeButton(R.string.no_label) { _, _ ->
                        AlertDialog.Builder(this)
                            .setTitle(R.string.dialog_title_prompt_install_database)
                            .setMessage(R.string.dialog_message_promp_install_database)
                            .setPositiveButton(R.string.yes_label) { _, _ ->
                                this.startActivity(Intent(this, DatabaseManagementActivity::class.java))
                            }
                            .setNegativeButton(R.string.no_label) { _, _ -> }
                            .create()
                            .show()
                    }
                    .create()
                    .show()
            }
        }
    }
}
