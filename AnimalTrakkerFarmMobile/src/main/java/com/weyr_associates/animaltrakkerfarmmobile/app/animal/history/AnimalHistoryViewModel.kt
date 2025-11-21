package com.weyr_associates.animaltrakkerfarmmobile.app.animal.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.alert.AnimalAlertEvent
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.alert.EmitsAnimalAlerts
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.alert.extractAnimalAlertEvents
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.drug.AnimalDrugHistoryViewModelContract
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.evaluation.AnimalEvaluationsHistoryViewModelContract
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.notes.AnimalNotesViewModelContract
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.tissue.AnimalTissueSampleHistoryViewModelContract
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.tissue.AnimalTissueTestHistoryViewModelContract
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.lookup.AnimalInfoLookup
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.lookup.LookupAnimalInfo
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.coroutines.channel.eventEmitter
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.AnimalRepository
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalDrugEvent
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalEvaluation
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalNote
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.TissueSampleEvent
import com.weyr_associates.animaltrakkerfarmmobile.model.TissueTestEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

class AnimalHistoryViewModel(
    private val animalRepository: AnimalRepository
) : ViewModel(), LookupAnimalInfo, EmitsAnimalAlerts,
    AnimalNotesViewModelContract, AnimalDrugHistoryViewModelContract,
    AnimalTissueSampleHistoryViewModelContract, AnimalTissueTestHistoryViewModelContract,
    AnimalEvaluationsHistoryViewModelContract {

    sealed interface Event

    private val animalInfoLookup = AnimalInfoLookup(viewModelScope, animalRepository)
    private val animalAlertEventEmitter = eventEmitter<AnimalAlertEvent>()

    override val animalInfoState: StateFlow<LookupAnimalInfo.AnimalInfoState> =
        animalInfoLookup.animalInfoState

    override val animalAlertsEvent: Flow<AnimalAlertEvent> =
        animalAlertEventEmitter.events

    override val animalNoteHistory: StateFlow<List<AnimalNote>> = animalInfoState.mapLatest { animalInfoState ->
        when (animalInfoState) {
            is LookupAnimalInfo.AnimalInfoState.Loaded -> {
                animalRepository.queryAnimalNoteHistory(animalInfoState.animalBasicInfo.id)
            }
            else -> emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    override val animalDrugHistory: StateFlow<List<AnimalDrugEvent>> = animalInfoState.mapLatest { animalInfoState ->
        when (animalInfoState) {
            is LookupAnimalInfo.AnimalInfoState.Loaded -> {
                animalRepository.queryAnimalDrugHistory(animalInfoState.animalBasicInfo.id)
            }
            else -> emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    override val tissueSampleEventHistory: StateFlow<List<TissueSampleEvent>> = animalInfoState.mapLatest { animalInfoState ->
        when (animalInfoState) {
            is LookupAnimalInfo.AnimalInfoState.Loaded -> {
                animalRepository.queryAnimalTissueSampleHistory(animalInfoState.animalBasicInfo.id)
            }
            else -> emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    override val tissueTestEventHistory: StateFlow<List<TissueTestEvent>> = animalInfoState.mapLatest { animalInfoState ->
        when (animalInfoState) {
            is LookupAnimalInfo.AnimalInfoState.Loaded -> {
                animalRepository.queryAnimalTissueTestHistory(animalInfoState.animalBasicInfo.id)
            }
            else -> emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    override val animalEvaluationsHistory: StateFlow<List<AnimalEvaluation>?> = animalInfoState.mapLatest { animalInfoState ->
        when (animalInfoState) {
            is LookupAnimalInfo.AnimalInfoState.Loaded -> {
                animalRepository.queryAnimalEvaluationHistory(animalInfoState.animalBasicInfo.id)
            }
            else -> emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        animalAlertEventEmitter.forwardFrom(
            animalInfoState.extractAnimalAlertEvents()
        )
    }

    override fun lookupAnimalInfoById(animalId: EntityId) {
        animalInfoLookup.lookupAnimalInfoById(animalId)
    }

    override fun lookupAnimalInfoByEIDNumber(eidNumber: String) {
        animalInfoLookup.lookupAnimalInfoByEIDNumber(eidNumber)
    }

    override fun resetAnimalInfo() {
        animalInfoLookup.resetAnimalInfo()
    }
}
