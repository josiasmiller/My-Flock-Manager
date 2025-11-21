package com.weyr_associates.animaltrakkerfarmmobile.app

import android.app.Application
import android.util.Log
import com.weyr_associates.animaltrakkerfarmmobile.app.core.ErrorReport
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseManager
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.files.AppDirectories
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Thread.UncaughtExceptionHandler

class AnimalTrakkerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        registerCrashReporter()
        ensureAppDirectoryStructure()
        loadSeedDatabaseIfRequired()
    }

    private fun registerCrashReporter() {
        Thread.setDefaultUncaughtExceptionHandler(
            CrashReportHandler(Thread.getDefaultUncaughtExceptionHandler())
        )
    }

    private fun ensureAppDirectoryStructure() {
        if (!AppDirectories.ensureAppDirectoriesExist(this)) {
            Log.w(TAG, "Failed to ensure the existence of the application directory structure.")
        }
    }

    private fun loadSeedDatabaseIfRequired() {
        with(DatabaseManager.getInstance(this)) {
            if(!isDatabaseFilePresent()) {
                @Suppress("OPT_IN_USAGE")
                GlobalScope.launch { loadSeedDatabase(ignorePatchVersion = true) }
            }
        }
    }

    private inner class CrashReportHandler(private val delegate: UncaughtExceptionHandler?) : UncaughtExceptionHandler {

        private var isReporting = false

        override fun uncaughtException(thread: Thread, throwable: Throwable) {

            //Avoid endless cycles
            if (isReporting) {
                return
            }

            isReporting = true

            ErrorReport(
                action = "Crash Report",
                summary = "Reporting Uncaught Exception",
                error = throwable
            ).writeAsCrashReport(this@AnimalTrakkerApplication)

            //Forward to original handler
            delegate?.uncaughtException(thread, throwable)

            isReporting = false
        }
    }

    companion object {
        private val TAG = AnimalTrakkerApplication::class.java.simpleName
    }
}
