package com.weyr_associates.animaltrakkerfarmmobile.app.animal.death

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.lookup.AnimalInfoLookup
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.lookup.LookupAnimalInfo
import com.weyr_associates.animaltrakkerfarmmobile.app.core.ErrorReport
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.requireAs
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.takeAs
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.AnimalRepository
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalAlert
import com.weyr_associates.animaltrakkerfarmmobile.model.DeathReason
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime

class AnimalDeathViewModel(
    private val animalRepo: AnimalRepository
) : ViewModel(), LookupAnimalInfo {

    sealed interface Event

    data class AnimalAlertEvent(
        val alerts: List<AnimalAlert>
    ) : Event

    data object UpdateDatabaseSuccess : Event
    data object UpdateDatabaseFailure : Event

    private val animalInfoLookup = AnimalInfoLookup(viewModelScope, animalRepo)

    override val animalInfoState = animalInfoLookup.animalInfoState

    private val _animalDeathReason = MutableStateFlow<DeathReason?>(null)
    val animalDeathReason = _animalDeathReason.asStateFlow()

    private val _animalDeathDate = MutableStateFlow<LocalDate?>(LocalDate.now())
    val animalDeathDate = _animalDeathDate.asStateFlow()

    private val _isUpdatingDatabase = MutableStateFlow(false)

    override fun lookupAnimalInfoById(animalId: EntityId) {
        animalInfoLookup.lookupAnimalInfoById(animalId)
    }

    override fun lookupAnimalInfoByEIDNumber(eidNumber: String) {
        animalInfoLookup.lookupAnimalInfoByEIDNumber(eidNumber)
    }

    override fun resetAnimalInfo() {
        animalInfoLookup.resetAnimalInfo()
    }

    val canClearData = combine(
        animalInfoState,
        _isUpdatingDatabase,
        _animalDeathReason,
        _animalDeathDate
    ) { animalInfoState, isUpdatingDatabase, deathReason, deathDate ->
        animalInfoState is LookupAnimalInfo.AnimalInfoState.Loaded &&
                !animalInfoState.animalBasicInfo.isDead &&
                !isUpdatingDatabase &&
                (deathReason != null || deathDate != null)
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    val canSaveToDatabase = combine(
        animalInfoState,
        _isUpdatingDatabase,
        _animalDeathReason,
        _animalDeathDate
    ) { animalInfoState, isUpdatingDatabase, deathReason, deathDate ->
        animalInfoState is LookupAnimalInfo.AnimalInfoState.Loaded &&
                !animalInfoState.animalBasicInfo.isDead &&
                !isUpdatingDatabase &&
                deathReason != null && deathDate != null
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    private val eventChannel = Channel<Event>()
    val events = eventChannel.receiveAsFlow()

    private val errorReportChannel = Channel<ErrorReport>()
    val errorReportFlow = errorReportChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            animalInfoState.filter { it is LookupAnimalInfo.AnimalInfoState.Loaded }
                .map { it.requireAs<LookupAnimalInfo.AnimalInfoState.Loaded>().animalBasicInfo }
                .collectLatest {
                    if (it.alerts.isNotEmpty()) {
                        viewModelScope.launch {
                            eventChannel.send(AnimalAlertEvent(it.alerts))
                        }
                    }
                }
        }
    }

    fun updateDeathReason(deathReason: DeathReason) {
        _animalDeathReason.update { deathReason }
    }

    fun updateDeathDate(deathDate: LocalDate) {
        _animalDeathDate.update { deathDate }
    }

    fun clearData() {
        if (canClearData.value) {
            executeClearData()
        }
    }

    fun saveToDatabase() {
        if (canSaveToDatabase.value) {
            viewModelScope.launch {
                executeUpdateDatabase()
            }
        }
    }

    private fun executeClearData() {
        _animalDeathReason.update { null }
        _animalDeathDate.update { null }
    }

    private suspend fun executeUpdateDatabase() {

        val animalBasicInfo = animalInfoState.value.takeAs<LookupAnimalInfo.AnimalInfoState.Loaded>()
            ?.animalBasicInfo ?: return
        val animalId = animalBasicInfo.id
        val deathReason = _animalDeathReason.value ?: return
        val deathDate = _animalDeathDate.value ?: return

        withContext(Dispatchers.IO) {
            _isUpdatingDatabase.update { true }
            try {
                animalRepo.markAnimalDeceased(
                    animalId,
                    deathReason.id,
                    deathDate,
                    LocalDateTime.now()
                )
                executeClearData()
                eventChannel.send(UpdateDatabaseSuccess)
            } catch (ex: Exception) {
                postErrorReport(animalId, deathReason, deathDate, ex)
                eventChannel.send(UpdateDatabaseFailure)
            } finally {
                _isUpdatingDatabase.update { false }
            }
        }
    }

    private suspend fun postErrorReport(
        animalId: EntityId,
        deathReason: DeathReason,
        deathDate: LocalDate,
        error: Exception
    ) {
        errorReportChannel.send(
            ErrorReport(
                action = "Record Animal Death",
                summary = buildString {
                    append("animalId=${animalId}, ")
                    append("deathReasonId=${deathReason.id}, ")
                    append("deathDate=${deathDate}")
                },
                error = error
            )
        )
    }
}
