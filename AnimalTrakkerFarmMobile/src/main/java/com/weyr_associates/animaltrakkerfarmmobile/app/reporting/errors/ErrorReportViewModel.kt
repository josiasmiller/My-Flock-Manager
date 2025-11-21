package com.weyr_associates.animaltrakkerfarmmobile.app.reporting.errors

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.weyr_associates.animaltrakkerfarmmobile.app.core.ErrorReport
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.coroutines.channel.sendIn
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.coroutines.rethrowIfCancellation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class ErrorReportViewModel(application: Application) : AndroidViewModel(application) {

    sealed interface Event

    data class SaveReportSucceeded(val file: File) : Event
    data class SaveReportFailed(val exception: Exception) : Event

    private val _canSaveReportToFile = MutableStateFlow(true)
    val canSaveReportToFile = _canSaveReportToFile.asStateFlow()

    private val eventChannel = Channel<Event>()
    val events = eventChannel.receiveAsFlow()

    fun saveErrorReportToFile(errorReport: ErrorReport) {
        if(canSaveReportToFile.value) {
            viewModelScope.launch(Dispatchers.IO) {
                executeSaveReportToFile(errorReport)
            }
        }
    }

    private fun executeSaveReportToFile(errorReport: ErrorReport) {
        try {
            _canSaveReportToFile.update { false }
            val errorReportFile = errorReport.writeAsErrorReport(getApplication())
            eventChannel.sendIn(viewModelScope, SaveReportSucceeded(errorReportFile))
        } catch(ex: Exception) {
            ex.rethrowIfCancellation()
            eventChannel.sendIn(viewModelScope, SaveReportFailed(ex))
        } finally {
            _canSaveReportToFile.update { true }
        }
    }
}
