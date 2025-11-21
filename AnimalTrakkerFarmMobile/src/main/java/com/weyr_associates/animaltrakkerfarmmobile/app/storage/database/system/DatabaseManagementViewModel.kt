package com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weyr_associates.animaltrakkerfarmmobile.app.core.ErrorReport
import com.weyr_associates.animaltrakkerfarmmobile.app.core.Result
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DatabaseManagementViewModel(
    private val databaseManager: DatabaseManager
) : ViewModel() {

    companion object {
        private const val DELAY_MILLIS_PREVENT_UI_FLASH = 1000L
    }

    sealed interface State {
        data object Idle : State
        sealed interface Working : State {
            data object LoadingDatabase : Working
            data object SavingDatabase : Working
        }
    }

    sealed interface Event {
        data object NoDatabaseToBackup : Event
        data class DatabaseLoadSucceeded(
            val numberOfAnimals: Long
        ) : Event
        data class DatabaseBackupSucceeded(
            val backupFileName: String
        ) : Event

        data class DatabaseLoadFailed(
            val error: DatabaseLoadError
        ) : Event

        data class DatabaseBackupFailed(
            val error: DatabaseBackupError
        ) : Event
    }

    private val _state = MutableStateFlow<State>(State.Idle)
    val state = _state.asStateFlow()

    private val eventsChannel = Channel<Event>()
    val events = eventsChannel.receiveAsFlow()

    private val errorReportChannel = Channel<ErrorReport>()
    val errorReportFlow = errorReportChannel.receiveAsFlow()

    fun loadSeedDatabase(ignorePatchVersion: Boolean = false) {
        loadDatabase { databaseManager.loadSeedDatabase(ignorePatchVersion) }
    }

    fun loadDatabaseFrom(uri: Uri, ignorePatchVersion: Boolean = false) {
        loadDatabase { databaseManager.loadDatabaseFromUri(uri, ignorePatchVersion) }
    }

    fun saveDatabaseToDocuments() {
        if (state.value == State.Idle) {
            viewModelScope.launch {
                if (!databaseManager.isDatabaseFilePresent()) {
                    eventsChannel.send(Event.NoDatabaseToBackup)
                    return@launch
                }
                _state.update { State.Working.SavingDatabase }
                val deferredNoFlash = async { delay(DELAY_MILLIS_PREVENT_UI_FLASH) }
                val deferredResult = async {
                    databaseManager.backupDatabaseToDocuments()
                }
                awaitAll(deferredNoFlash, deferredResult)
                val result = deferredResult.await()
                eventsChannel.send(
                    when (result) {
                        is Result.Success -> Event.DatabaseBackupSucceeded(result.data.name)
                        is Result.Failure -> Event.DatabaseBackupFailed(result.error)
                    }
                )
                _state.update { State.Idle }
            }
        }
    }

    fun saveDatabaseToUSB() {
        /*NO-OP - Will be implemented in future work*/
    }

    private fun loadDatabase(databaseLoader: suspend () -> Result<Long, DatabaseLoadError>) {
        if (state.value == State.Idle) {
            viewModelScope.launch {
                _state.update { State.Working.LoadingDatabase }
                val deferredNoFlash = async { delay(DELAY_MILLIS_PREVENT_UI_FLASH) }
                val deferredResult = async { databaseLoader.invoke() }
                awaitAll(deferredNoFlash, deferredResult)
                val result = deferredResult.await()
                eventsChannel.send(
                    when (result) {
                        is Result.Success -> Event.DatabaseLoadSucceeded(result.data)
                        is Result.Failure -> Event.DatabaseLoadFailed(result.error)
                    }
                )
                _state.update { State.Idle }
            }
        }
    }
}
