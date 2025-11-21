package com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import com.weyr_associates.animaltrakkerfarmmobile.app.core.Result
import com.weyr_associates.animaltrakkerfarmmobile.app.core.deleteSafely
import com.weyr_associates.animaltrakkerfarmmobile.app.core.versionName
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseBackupError.SourceDatabaseFileNotFound
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseBackupError.UnableToWriteBackupDatabaseFile
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseLoadError.LoadedDatabaseFailedQueryCheck
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseLoadError.UnableToReadFromDatabaseSourceFile
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseLoadError.UnableToWriteLoadedDatabaseFile
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.files.AppDirectories
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.files.Files
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.AnimalTable
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Sql
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.time.LocalDateTime

sealed interface DatabaseLoadError {
    data class UnableToReadFromDatabaseSourceFile(
        val thrownError: Throwable? = null
    ) : DatabaseLoadError
    data class UnableToWriteLoadedDatabaseFile(
        val thrownError: Throwable? = null
    ) : DatabaseLoadError
    data class LoadedDatabaseFailedQueryCheck(
        val thrownError: Throwable? = null
    ) : DatabaseLoadError
    data class DatabaseValidityCheckFailed(
        val failure: DatabaseValidityCheckFailure,
        val selectedDatabaseUri: Uri?
    ) : DatabaseLoadError
}

sealed interface DatabaseBackupError {
    data object SourceDatabaseFileNotFound : DatabaseBackupError
    data class UnableToWriteBackupDatabaseFile(
        val thrownError: Throwable
    ) : DatabaseBackupError
}

sealed interface DatabaseValidityCheckResult

data object DatabaseValidityCheckPassed : DatabaseValidityCheckResult

sealed interface DatabaseValidityCheckFailure : DatabaseValidityCheckResult

data class DatabaseQueryCheckFailed(
    val failureMessage: String
) : DatabaseValidityCheckFailure

data class DatabaseVersionUnsupported(
    val databaseVersionString: String,
    val supportedDatabaseVersion: DatabaseVersion
) : DatabaseValidityCheckFailure

data class DatabaseVersionPatchRecommended(
    val databaseVersionString: String,
    val supportedDatabaseVersion: DatabaseVersion
) : DatabaseValidityCheckFailure

class DatabaseManager(context: Context) {

    companion object {

        @JvmStatic
        fun getInstance(context: Context): DatabaseManager {
            if (INSTANCE == null) {
                synchronized(INSTANCE_LOCK) {
                    if (INSTANCE == null) {
                        INSTANCE = DatabaseManager(context)
                    }
                }
            }
            return requireNotNull(INSTANCE)
        }

        const val PREFS_NAME_DATABASE_MANAGEMENT = "DATABASE_MANAGEMENT"
        const val PREFS_KEY_LAST_APP_VERSION_SEEN = "PREFS_KEY_LAST_APP_VERSION_SEEN"
        const val PREFS_KEY_LAST_SELECTED_DATABASE_URI = "LAST_SELECTED_DATABASE_URI"
        const val PREFS_KEY_ACCEPT_PATCH_VERSION_DIFFERENCES = "PREFS_KEY_ACCEPT_PATCH_VERSION_DIFFERENCES"

        private var INSTANCE: DatabaseManager? = null
        private val INSTANCE_LOCK = Any()

        private const val FILE_NAME_ATRKKR_SEED_DATABASE = "AnimalTrakker_V6.0.0_seed_database.sqlite"

        private const val DIRECTORY_PRELOAD_DATABASE = "preload-databases"
        private const val FILE_NAME_ATRKKR_WORKING_DATABASE = "animaltrakker_db.sqlite"
        private const val FILE_NAME_ATRKKR_WORKING_DATABASE_JOURNAL = "$FILE_NAME_ATRKKR_WORKING_DATABASE-journal"

        private const val REQUIRED_MAJOR_VERSION = 6
        private const val REQUIRED_MINOR_VERSION = 0
        private const val SUGGESTED_PATCH_VERSION = 0

        private val SUPPORTED_DATABASE_VERSION by lazy {
            DatabaseVersion(
                major = REQUIRED_MAJOR_VERSION,
                minor = REQUIRED_MINOR_VERSION,
                patch = SUGGESTED_PATCH_VERSION
            )
        }
    }

    private val appContext = context.applicationContext

    private val sharedPreferences by lazy {
        appContext.getSharedPreferences(PREFS_NAME_DATABASE_MANAGEMENT, Context.MODE_PRIVATE)
    }

    private val databaseFile: File get() = appContext.getDatabasePath(
        FILE_NAME_ATRKKR_WORKING_DATABASE
    )

    private val databasePreloadDirectory: File get() = appContext.filesDir.childAt(
        DIRECTORY_PRELOAD_DATABASE
    )

    init {
        if (isLastAppVersionSeenSameAsCurrentAppVersion()) {
            resetAcceptDBPatchVersionDifferences()
        }
        updateLastSeenAppVersion()
    }

    fun checkDatabaseValidity(): DatabaseValidityCheckResult {
        return createDatabaseHandler().use { dbh ->
            checkDatabaseValidity(
                sqliteDatabase = dbh.readableDatabase,
                ignorePatchVersion = false
            )
        }
    }

    fun isDatabaseFilePresent(): Boolean {
        return databaseFile.exists()
    }

    fun createDatabaseHandler(): DatabaseHandler {
        return DatabaseHandler.create(appContext, FILE_NAME_ATRKKR_WORKING_DATABASE)
    }

    val hasAcceptedDatabasePatchVersionDifferences: Boolean
        get() {
            return sharedPreferences.getBoolean(
                PREFS_KEY_ACCEPT_PATCH_VERSION_DIFFERENCES, false
            )
        }

    fun acceptDatabasePatchVersionDifferences() {
        sharedPreferences.edit().putBoolean(
            PREFS_KEY_ACCEPT_PATCH_VERSION_DIFFERENCES, true
        ).apply()
    }

    suspend fun loadSeedDatabase(ignorePatchVersion: Boolean): Result<Long, DatabaseLoadError> {
        return loadDatabase(ignorePatchVersion, selectedDatabaseUri = null) {
            preloadSeedDatabase()
        }
    }

    suspend fun loadDatabaseFromUri(uri: Uri, ignorePatchVersion: Boolean): Result<Long, DatabaseLoadError> {
        return loadDatabase(ignorePatchVersion = ignorePatchVersion, selectedDatabaseUri = uri) {
            preloadDatabaseFromUri(uri)
        }
    }

    suspend fun backupDatabaseToDocuments(): Result<File, DatabaseBackupError> {
        return withContext(Dispatchers.IO) {
            if (!databaseFile.exists()) {
                return@withContext Result.Failure(SourceDatabaseFileNotFound)
            }
            val dstFile = AppDirectories.databaseBackupsDirectory(appContext)
                .childAt(getDatabaseBackupFileName())
            try {
                databaseFile.copyTo(dstFile, overwrite = true)
                return@withContext Result.Success(dstFile)
            } catch(ex: Exception) {
                return@withContext Result.Failure(
                    when (ex) {
                        is NoSuchFileException -> SourceDatabaseFileNotFound
                        else -> UnableToWriteBackupDatabaseFile(ex)
                    }
                )
            }
        }
    }

    private fun checkDatabaseValidity(
        sqliteDatabase: SQLiteDatabase,
        ignorePatchVersion: Boolean
    ): DatabaseValidityCheckResult {
        if (!isDatabaseQueryable(sqliteDatabase)) {
            return DatabaseQueryCheckFailed(
                "Unable to execute basic queries against database."
            )
        }
        val databaseVersionString = queryAnimalTrakkerDBVersion(sqliteDatabase)
        val databaseVersion = DatabaseVersion.fromString(databaseVersionString)
            ?: return DatabaseQueryCheckFailed("Database version is invalid: \"${databaseVersionString}\"")
        if (databaseVersion.major != REQUIRED_MAJOR_VERSION ||
            databaseVersion.minor != REQUIRED_MINOR_VERSION) {
            return DatabaseVersionUnsupported(
                databaseVersionString,
                SUPPORTED_DATABASE_VERSION
            )
        }
        if (!ignorePatchVersion && databaseVersion.patch != SUGGESTED_PATCH_VERSION) {
            return DatabaseVersionPatchRecommended(
                databaseVersionString,
                SUPPORTED_DATABASE_VERSION
            )
        }
        return DatabaseValidityCheckPassed
    }

    private fun isDatabaseQueryable(sqliteDatabase: SQLiteDatabase): Boolean {
        try {
            sqliteDatabase.rawQuery(Sql.DB_PRESENCE_CHECK_SQL, arrayOf()).use { cursor ->
                return cursor.moveToFirst()
            }
        } catch (ex: Exception) {
            return false
        }
    }

    private fun queryAnimalTrakkerDBVersion(sqliteDatabase: SQLiteDatabase): String {
        sqliteDatabase.rawQuery(Sql.QUERY_ANIMALTRAKKER_DB_VERSION, arrayOf()).use { cursor ->
            return if (cursor.moveToFirst()) cursor.getString(0) else ""
        }
    }

    private suspend fun loadDatabase(
        ignorePatchVersion: Boolean,
        selectedDatabaseUri: Uri?,
        preloadDatabase: () -> Result<File, DatabaseLoadError>
    ): Result<Long, DatabaseLoadError> {
        return withContext(Dispatchers.IO) {
            when (val preloadDatabaseResult = preloadDatabase()) {
                is Result.Success -> {
                    val preloadTempDbFile = preloadDatabaseResult.data
                    try {
                        checkValidityAndFinishLoad(
                            preloadDbTempFile = preloadTempDbFile,
                            ignorePatchVersion = ignorePatchVersion,
                            selectedDatabaseUri = selectedDatabaseUri
                        )
                    } finally {
                        preloadTempDbFile.deleteSafely()
                    }
                }
                is Result.Failure -> {
                    Result.Failure(preloadDatabaseResult.error)
                }
            }
        }
    }

    /**
     * On success, returns a reference to a temp file.
     * Deletion of this file is the responsibility of
     * the calling chain once the file is no longer
     * needed.
     */
    private fun preloadDatabaseFromUri(uri: Uri): Result<File, DatabaseLoadError> {
        return try {
            appContext.contentResolver.openInputStream(uri)?.use { inputStream ->
                preloadDatabaseFromInputStream(inputStream)
            } ?: Result.Failure(UnableToReadFromDatabaseSourceFile())
        } catch (ex: Exception) {
            return Result.Failure(UnableToReadFromDatabaseSourceFile(ex))
        }
    }

    /**
     * On success, returns a reference to a temp file.
     * Deletion of this file is the responsibility of
     * the calling chain once the file is no longer
     * needed.
     */
    private fun preloadSeedDatabase(): Result<File, DatabaseLoadError> {
        return try {
            appContext.assets.open(FILE_NAME_ATRKKR_SEED_DATABASE).use { inputStream ->
                preloadDatabaseFromInputStream(inputStream)
            }
        } catch(ex: Exception) {
            Result.Failure(UnableToReadFromDatabaseSourceFile(ex))
        }
    }

    /**
     * On success, returns a reference to a temp file.
     * Deletion of this file is the responsibility of
     * the calling chain once the file is no longer
     * needed. On failure, this method attempts to
     * delete the temp file to clean it up.
     */
    private fun preloadDatabaseFromInputStream(inputStream: InputStream): Result<File, UnableToWriteLoadedDatabaseFile> {
        var preloadDatabaseTempFile: File? = null
        try {
            if(!databasePreloadDirectory.exists() && !databasePreloadDirectory.mkdir()) {
                return Result.Failure(UnableToWriteLoadedDatabaseFile())
            }
            preloadDatabaseTempFile = File.createTempFile("preload-database", null, databasePreloadDirectory)
            preloadDatabaseTempFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            return Result.Success(preloadDatabaseTempFile)
        } catch (ex: Exception) {
            preloadDatabaseTempFile?.deleteSafely()
            return Result.Failure(UnableToWriteLoadedDatabaseFile(ex))
        }
    }

    /**
     * [preloadDbTempFile] expected to be cleaned up
     * by caller in cases of failures.
     */
    private fun checkValidityAndFinishLoad(
        preloadDbTempFile: File,
        ignorePatchVersion: Boolean,
        selectedDatabaseUri: Uri?
    ): Result<Long, DatabaseLoadError> {
        return SQLiteDatabase.openDatabase(
            preloadDbTempFile,
            SQLiteDatabase.OpenParams.Builder()
                .addOpenFlags(SQLiteDatabase.OPEN_READONLY)
                .build()
        ).use { readonlyDatabase ->
            when (val checkValidityResult = checkDatabaseValidity(readonlyDatabase, ignorePatchVersion)) {
                is DatabaseValidityCheckPassed -> {
                    when (val finishLoadResult = finishDatabaseLoad(preloadDbTempFile)) {
                        is Result.Success -> {
                            if (!ignorePatchVersion) {
                                //We aren't specifically ignoring patch version
                                //differences, so reset tracking on load.
                                resetAcceptDBPatchVersionDifferences()
                            } else {
                                //User elected to ignore patch version differences
                                acceptDatabasePatchVersionDifferences()
                            }
                            queryAnimalCountPostDatabaseLoad()
                        }
                        is Result.Failure -> {
                            Result.Failure(finishLoadResult.error)
                        }
                    }
                }
                is DatabaseValidityCheckFailure -> {
                    Result.Failure(
                        DatabaseLoadError.DatabaseValidityCheckFailed(
                            failure = checkValidityResult,
                            selectedDatabaseUri = selectedDatabaseUri,
                        )
                    )
                }
            }
        }
    }

    private fun finishDatabaseLoad(preloadTempFile: File): Result<Unit, DatabaseLoadError> {
        try {
            databaseFile.outputStream().use { outputStream ->
                preloadTempFile.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            return Result.Success(Unit)
        } catch (ex: Exception) {
            return Result.Failure(UnableToWriteLoadedDatabaseFile(ex))
        }
    }

    private fun queryAnimalCountPostDatabaseLoad(): Result<Long, DatabaseLoadError> {
        try {
            createDatabaseHandler().use { databaseHandler ->
                databaseHandler.readableDatabase.rawQuery(
                    AnimalTable.Sql.QUERY_COUNT_ALL_ANIMALS,
                    emptyArray()
                ).use { cursor ->
                    if (!cursor.moveToFirst()) {
                        return Result.Failure(LoadedDatabaseFailedQueryCheck())
                    }
                    return Result.Success(cursor.getLong(0))
                }
            }
        } catch (ex: Exception) {
            return Result.Failure(LoadedDatabaseFailedQueryCheck(ex))
        }
    }

    private fun isLastAppVersionSeenSameAsCurrentAppVersion(): Boolean {
        val currentAppVersion = appContext.versionName
        val lastAppVersionSeen = sharedPreferences.getString(
            PREFS_KEY_LAST_APP_VERSION_SEEN, null
        )
        return currentAppVersion == lastAppVersionSeen
    }

    private fun updateLastSeenAppVersion() {
        sharedPreferences.edit()
            .putString(PREFS_KEY_LAST_APP_VERSION_SEEN, appContext.versionName)
            .apply()
    }

    private fun resetAcceptDBPatchVersionDifferences() {
        sharedPreferences.edit()
            .putBoolean(PREFS_KEY_ACCEPT_PATCH_VERSION_DIFFERENCES, false)
            .apply()
    }

    private fun getDatabaseBackupFileName(): String {
        val appVersion = appContext.versionName
        val timeStamp = LocalDateTime.now().format(Files.TIME_STAMP_FORMAT_IN_FILE_NAME)
        return buildString {
            append(timeStamp)
            append("_VER_${appVersion}.sqlite")
        }
    }

    private fun File.childAt(path: String): File {
        return File(this, path)
    }
}
