package com.weyr_associates.animaltrakkerfarmmobile.app.animal.premise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.alert.AnimalAlertEvent
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.alert.EmitsAnimalAlerts
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.alert.extractAnimalAlertEvents
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.lookup.AnimalInfoLookup
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.lookup.LookupAnimalInfo
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.lookup.LookupAnimalInfo.AnimalInfoState.Loaded
import com.weyr_associates.animaltrakkerfarmmobile.app.core.ClearsData
import com.weyr_associates.animaltrakkerfarmmobile.app.core.ErrorReport
import com.weyr_associates.animaltrakkerfarmmobile.app.core.SavesData
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.coroutines.channel.eventEmitter
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.coroutines.rethrowIfCancellation
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.requireAs
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.takeAs
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.AnimalRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.PremiseRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalMovement
import com.weyr_associates.animaltrakkerfarmmobile.model.DefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.Owner
import com.weyr_associates.animaltrakkerfarmmobile.model.Premise
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

class MoveToPremiseViewModel(
    private val animalRepository: AnimalRepository,
    private val premiseRepository: PremiseRepository,
    private val loadDefaultSettings: LoadActiveDefaultSettings
) : ViewModel(), LookupAnimalInfo, EmitsAnimalAlerts, ClearsData, SavesData {

    sealed interface Event
    data object AnimalRequiredToBeAlive : Event

    private val animalLookup = AnimalInfoLookup(viewModelScope, animalRepository)
    private val animalAlertEventEmitter = eventEmitter<AnimalAlertEvent>()

    override val animalInfoState = animalLookup.animalInfoState
    override val animalAlertsEvent: Flow<AnimalAlertEvent> =
        animalAlertEventEmitter.events

    private val eventsChannel = Channel<Event>()
    val events = eventsChannel.receiveAsFlow()

    private val errorReportChannel = Channel<ErrorReport>()
    val errorReportFlow = errorReportChannel.receiveAsFlow()

    val availablePremises: StateFlow<List<Premise>?> = flow<List<Premise>?> {
        val defaultSettings: DefaultSettings = loadDefaultSettings()
        val ownerId = defaultSettings.ownerId
        val ownerType = defaultSettings.ownerType?.let { Owner.Type.fromCode(it) }
        if (ownerId != null && ownerType != null) {
            emit(premiseRepository.queryPhysicalPremisesForOwner(ownerId, ownerType))
        } else { null }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _selectedPremise = MutableStateFlow<Premise?>(null)
    val selectedPremise = _selectedPremise.asStateFlow()

    val latestMovement: StateFlow<AnimalMovement?> = animalInfoState.flatMapLatest {
        it.takeAs<Loaded>()?.animalBasicInfo?.let { animalBasicInfo ->
            animalRepository.animalLatestMovement(animalBasicInfo.id)
        } ?: flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    val currentPremise: StateFlow<Premise?> = latestMovement.map { animalMovement ->
        animalMovement?.toPremise?.let { premise ->
            val defaultSettings: DefaultSettings = loadDefaultSettings()
            val ownerId = defaultSettings.ownerId
            val ownerType = defaultSettings.ownerType?.let { Owner.Type.fromCode(it) }
            when {
                ownerId != null && ownerType != null -> {
                    premise.copy(
                        nickname = premiseRepository.queryPremiseNickname(
                            premise.id,
                            ownerId,
                            ownerType
                        )
                    )
                }
                else -> premise
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    val alreadyMovedToday: StateFlow<Boolean> = latestMovement.map {
        it?.movementDate == LocalDate.now()
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    private val isSavingData = MutableStateFlow(false)

    override val canClearData = combine(
        selectedPremise, alreadyMovedToday
    ) { selectedPremise, alreadyMovedToday ->
        selectedPremise != null && !alreadyMovedToday
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    override val canSaveData = combine(
        animalInfoState, selectedPremise, currentPremise, alreadyMovedToday, isSavingData
    ) { animalInfoState, selectedPremise, currentPremise, alreadyMovedToday, isSavingData ->
        animalInfoState is Loaded &&
                selectedPremise != null &&
                selectedPremise.id != currentPremise?.id &&
                !alreadyMovedToday &&
                !isSavingData
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    init {
        animalAlertEventEmitter.forwardFrom(
            animalInfoState.extractAnimalAlertEvents()
        )
        viewModelScope.launch {
            animalInfoState.filter { it is Loaded }
                .map { it.requireAs<Loaded>().animalBasicInfo }
                .collectLatest {
                    if (it.isDead) {
                        viewModelScope.launch {
                            eventsChannel.send(AnimalRequiredToBeAlive)
                        }
                    }
                }
        }
    }

    fun selectPremise(premise: Premise?) {
        _selectedPremise.update { premise }
    }

    override fun lookupAnimalInfoById(animalId: EntityId) {
        animalLookup.lookupAnimalInfoById(animalId)
    }

    override fun lookupAnimalInfoByEIDNumber(eidNumber: String) {
        animalLookup.lookupAnimalInfoByEIDNumber(eidNumber)
    }

    override fun resetAnimalInfo() {
        animalLookup.resetAnimalInfo()
    }

    override fun clearData() {
        _selectedPremise.update { null }
    }

    override fun saveData() {
        val animalId = animalInfoState.value.takeAs<Loaded>()?.animalBasicInfo?.id ?: return
        val premiseId = selectedPremise.value?.id ?: return
        viewModelScope.launch(Dispatchers.IO) {
            isSavingData.update { true }
            try {
                animalRepository.moveAnimalToPremise(
                    animalId,
                    premiseId,
                    LocalDateTime.now()
                )
            } catch(ex: Exception) {
                ex.rethrowIfCancellation()
                errorReportChannel.send(
                    ErrorReport(
                        action = "Moving Animal to Premise",
                        summary = "animalId:${animalId},premiseId:${premiseId}",
                        error = ex
                    )
                )
            } finally {
                isSavingData.update { false }
            }
        }
    }
}
