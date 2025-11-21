package com.weyr_associates.animaltrakkerfarmmobile.app.storage.files

import android.content.Context
import android.os.Environment
import java.io.File

object AppDirectories {

    private const val DIR_NAME_ROOT = "AnimalTrakker"
    private const val DIR_NAME_DATABASES = "Databases"
    private const val DIR_NAME_BACKUPS = "Backups"
    private const val DIR_NAME_BARCODES = "Barcodes"
    private const val DIR_NAME_BAA_TAGS = "BAA-TAGS"
    private const val DIR_NAME_RAW_BAA_TAGS = "BAA-TAGS-RAW"
    private const val DIR_NAME_RACE_TAGS = "RACE-TAGS"
    private const val DIR_NAME_EID_TAGS = "EID-TAGS"
    private const val DIR_NAME_REPORTS = "Reports"
    private const val DIR_NAME_ERROR = "Error"
    private const val DIR_NAME_CRASH = "Crash"

    @JvmStatic
    fun ensureAppDirectoriesExist(context: Context): Boolean {
        return listOf(
            rootDirectory(context),
            databasesDirectory(context),
            databaseBackupsDirectory(context),
            barcodesDirectory(context),
            baaTagsDirectory(context),
            rawBaaTagsDirectory(context),
            raceTagsDirectory(context),
            eidTagsDirectory(context),
            reportsDirectory(context),
            errorReportsDirectory(context),
            crashReportDirectory(context)
        ).map { file ->
            file.exists() || file.mkdirs()
        }.all { it } //we don't just do 'all' so we attempt to create each directory.
    }

    @JvmStatic
    fun rootDirectory(@Suppress("UNUSED_PARAMETER") context: Context): File {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            .childAt(DIR_NAME_ROOT)
    }

    @JvmStatic
    fun databasesDirectory(context: Context): File {
        return rootDirectory(context)
            .childAt(DIR_NAME_DATABASES)
    }

    @JvmStatic
    fun databaseBackupsDirectory(context: Context): File {
        return databasesDirectory(context)
            .childAt(DIR_NAME_BACKUPS)
    }

    @JvmStatic
    fun barcodesDirectory(context: Context): File {
        return rootDirectory(context)
            .childAt(DIR_NAME_BARCODES)
    }

    @JvmStatic
    fun baaTagsDirectory(context: Context): File {
        return rootDirectory(context)
            .childAt(DIR_NAME_BAA_TAGS)
    }

    @JvmStatic
    fun rawBaaTagsDirectory(context: Context): File {
        return rootDirectory(context)
            .childAt(DIR_NAME_RAW_BAA_TAGS)
    }

    @JvmStatic
    fun raceTagsDirectory(context: Context): File {
        return rootDirectory(context)
            .childAt(DIR_NAME_RACE_TAGS)
    }

    @JvmStatic
    fun eidTagsDirectory(context: Context): File {
        return rootDirectory(context)
            .childAt(DIR_NAME_EID_TAGS)
    }

    @JvmStatic
    fun reportsDirectory(context: Context): File {
        return rootDirectory(context)
            .childAt(DIR_NAME_REPORTS)
    }

    @JvmStatic
    fun errorReportsDirectory(context: Context): File {
        return reportsDirectory(context)
            .childAt(DIR_NAME_ERROR)
    }

    @JvmStatic
    fun crashReportDirectory(context: Context): File {
        return reportsDirectory(context)
            .childAt(DIR_NAME_CRASH)
    }

    private fun File.childAt(path: String): File {
        return File(this, path)
    }
}
