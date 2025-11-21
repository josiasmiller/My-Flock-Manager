package com.weyr_associates.animaltrakkerfarmmobile.app.animal.note

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weyr_associates.animaltrakkerfarmmobile.app.core.ErrorReport
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.AnimalRepository
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.PredefinedNote
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class TakeNotesViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val animalRepo: AnimalRepository
) : ViewModel() {

    sealed interface Event

    data object CustomNoteTextChanged : Event
    data object DatabaseUpdateSucceeded : Event
    data object DatabaseUpdateFailed : Event

    data class Notes(val items: List<PredefinedNote>) {
        val ids = items.map { it.id }.toSet()
    }

    private val _customNoteText = MutableStateFlow("")
    var customNoteText: String
        get() = _customNoteText.value
        set(value) { _customNoteText.update { value } }

    private val _notes = MutableStateFlow(Notes(emptyList()))
    val notes = _notes.asStateFlow()

    private val eventsChannel = Channel<Event>()
    val events = eventsChannel.receiveAsFlow()

    private val errorReportChannel = Channel<ErrorReport>()
    val errorReportFlow = errorReportChannel.receiveAsFlow()

    val canClearData = combine(_customNoteText, _notes) { customNoteText, notes ->
        customNoteText.isNotBlank() || notes.items.isNotEmpty()
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    val canSaveData = combine(_customNoteText, _notes) { customNoteText, notes ->
        customNoteText.isNotBlank() || notes.items.isNotEmpty()
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun addNote(note: PredefinedNote) {
        _notes.update {
            if (it.ids.contains(note.id)) { it }
            else {
                val updatedItems = buildList {
                    addAll(it.items)
                    add(note)
                }
                it.copy(items = updatedItems)
            }
        }
    }

    fun replaceNote(id: EntityId, note: PredefinedNote) {
        _notes.update {
            val position = it.items.indexOfFirst { item -> item.id == id }
            if (position != -1 && !it.ids.contains(note.id)) {
                val updatedItems = it.items.toMutableList()
                    .apply { this[position] = note }
                it.copy(items = updatedItems)
            } else { it }
        }
    }

    fun clearNote(id: EntityId) {
        _notes.update {
            val position = it.items.indexOfFirst { item -> item.id == id }
            if (position != -1) {
                val updatedItems = it.items.toMutableList()
                    .apply { removeAt(position) }
                it.copy(items = updatedItems)
            } else { it }
        }
    }

    fun clearData() {
        if (canClearData.value) {
            _notes.update { Notes(items = emptyList()) }
            customNoteText = ""
            viewModelScope.launch {
                eventsChannel.send(CustomNoteTextChanged)
            }
        }
    }

    fun saveData() {
        if (canSaveData.value) {
            viewModelScope.launch {
                executeSaveData()
            }
        }
    }

    private suspend fun executeSaveData() {
        val animalId: EntityId = requireNotNull(
            savedStateHandle[TakeNotes.EXTRA_ANIMAL_ID]
        )
        val noteText = customNoteText.trim()
        val noteIds = notes.value.ids.toList()
        try {
            animalRepo.addNotesToAnimal(
                animalId,
                noteText,
                noteIds,
                LocalDateTime.now()
            )
            eventsChannel.send(DatabaseUpdateSucceeded)
        } catch(ex: Exception) {
            postSaveDataErrorReport(animalId, noteText, noteIds, ex)
        }
    }

    private suspend fun postSaveDataErrorReport(
        animalId: EntityId,
        noteText: String,
        noteIds: List<EntityId>,
        exception: Exception
    ) {
        errorReportChannel.send(
            ErrorReport(
                action = "Save Animal Notes",
                summary = "animalId=${animalId}, noteText=${noteText}, noteIds=[${noteIds.joinToString()}]",
                error = exception
            )
        )
    }
}
