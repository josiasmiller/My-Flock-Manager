package com.weyr_associates.animaltrakkerfarmmobile.app.animal.breeding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.alert.AnimalAlertEvent
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.alert.EmitsAnimalAlerts
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.alert.extractAnimalAlertEvents
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.lookup.AnimalInfoLookup
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.lookup.LookupAnimalInfo
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.lookup.LookupAnimalInfo.AnimalInfoState
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.coroutines.channel.eventEmitter
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.takeAs
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.AnimalRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.SpeciesRepository
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalBasicInfo
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.FemaleBreedingHistoryEntry
import com.weyr_associates.animaltrakkerfarmmobile.model.Sex
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DetailedLambingViewModel(
    private val animalRepo: AnimalRepository,
    private val speciesRepository: SpeciesRepository
) : ViewModel(), LookupAnimalInfo, EmitsAnimalAlerts {

    sealed interface Event

    data class AlertUnableToBirth(
        val reason: UnableToBirth.Reason
    ) : Event

    sealed interface BirthingStatus

    data object AbleToBirth : BirthingStatus

    data class UnableToBirth(
        val reason: Reason
    ) : BirthingStatus {
        enum class Reason {
            NO_ANIMAL,
            WRONG_SEX,
            TOO_YOUNG,
            IS_DEAD
        }
    }

    private val animalInfoLookup = AnimalInfoLookup(viewModelScope, animalRepo)
    private val animalAlertEventEmitter = eventEmitter<AnimalAlertEvent>()

    override val animalInfoState = animalInfoLookup.animalInfoState
    override val animalAlertsEvent = animalAlertEventEmitter.events

    private val refreshBreedingHistoryChannel = Channel<Unit>()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val birthingStatusShared: SharedFlow<BirthingStatus> = animalInfoState.mapLatest {
        if (it is AnimalInfoState.Loaded) {
            it.animalBasicInfo.birthingStatus()
        } else {
            UnableToBirth(UnableToBirth.Reason.NO_ANIMAL)
        }
    }.shareIn(viewModelScope, SharingStarted.Lazily, 1)

    val birthingStatus: StateFlow<BirthingStatus> = birthingStatusShared
        .stateIn(viewModelScope, SharingStarted.Lazily, UnableToBirth(UnableToBirth.Reason.NO_ANIMAL))

    @OptIn(ExperimentalCoroutinesApi::class)
    val femaleBreedingHistory: StateFlow<List<FemaleBreedingHistoryEntry>> = combine(
        animalInfoState, refreshBreedingHistoryChannel.receiveAsFlow()) { state, _ -> state }
            .mapLatest {
                when (val animalBasicInfo = it.takeAs<AnimalInfoState.Loaded>()?.animalBasicInfo) {
                    null -> emptyList()
                    else -> animalRepo.queryFemaleBreedingHistory(animalBasicInfo.id)
                }
            }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val canAddOffSpring: StateFlow<Boolean> = birthingStatus.mapLatest { it is AbleToBirth }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    private val eventEmitter = eventEmitter<Event>()
    val events = eventEmitter.events

    init {
        animalAlertEventEmitter.forwardFrom(
            animalInfoState.extractAnimalAlertEvents()
        )
        eventEmitter.forwardFrom(
            birthingStatusShared.filterIsInstance<UnableToBirth>()
                .filter { it.reason != UnableToBirth.Reason.NO_ANIMAL }
                .map { AlertUnableToBirth(it.reason) }
        )
        viewModelScope.launch {
            //Send first event so combine flow operator
            //will act on the first animal look up.
            refreshBreedingHistoryChannel.send(Unit)
        }
    }

    override fun lookupAnimalInfoById(animalId: EntityId) {
        animalInfoLookup.lookupAnimalInfoById(animalId)
    }

    override fun lookupAnimalInfoByEIDNumber(eidNumber: String) {
        animalInfoLookup.lookupAnimalInfoByEIDNumber(eidNumber)
    }

    fun refreshAnimalInfo() {
        val state = animalInfoLookup.animalInfoState.value.takeAs<AnimalInfoState.Loaded>()
        if (state != null) {
            animalInfoLookup.lookupAnimalInfoById(state.animalBasicInfo.id)
        }
    }

    override fun resetAnimalInfo() {
        animalInfoLookup.resetAnimalInfo()
    }

    fun refreshBreedingHistory() {
        viewModelScope.launch {
            refreshBreedingHistoryChannel.send(Unit)
        }
    }

    private suspend fun AnimalBasicInfo.birthingStatus(): BirthingStatus {
        if (sexId != Sex.ID_SHEEP_EWE) {
            return UnableToBirth(UnableToBirth.Reason.WRONG_SEX)
        } else if (isDead) {
            return UnableToBirth(UnableToBirth.Reason.IS_DEAD)
        } else {
            val speciesInfo = requireNotNull(speciesRepository.querySpeciesById(speciesId))
            if (ageInDays() < speciesInfo.earlyFemaleBreedingAgeDays +
                speciesInfo.earlyGestationLengthDays) {
                return UnableToBirth(UnableToBirth.Reason.TOO_YOUNG)
            }
        }
        return AbleToBirth
    }
}
