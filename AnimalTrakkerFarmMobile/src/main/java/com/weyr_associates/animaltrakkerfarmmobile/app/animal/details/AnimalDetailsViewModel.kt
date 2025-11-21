package com.weyr_associates.animaltrakkerfarmmobile.app.animal.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.alert.AnimalAlertsViewModelContract
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.breeding.details.AnimalBreedingDetailsViewModelContract
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.breeding.details.BreedingDetailsState
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.breeding.details.FemaleBreedingDetailsState
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.breeding.details.MaleBreedingDetailsState
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.breeding.details.NoBreedingDetailsState
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.breeding.summary.AnimalBreedingSummaryViewModelContract
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.drug.AnimalDrugHistoryViewModelContract
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.evaluation.AnimalEvaluationsHistoryViewModelContract
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.genetics.AnimalGeneticsViewModelContract
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.id.AnimalIdHistoryViewModelContract
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.location.AnimalLocationTimelineViewModelContract
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.notes.AnimalNotesViewModelContract
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.tissue.AnimalTissueSampleHistoryViewModelContract
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.tissue.AnimalTissueTestHistoryViewModelContract
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.AnimalRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.PremiseRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalAlert
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalDetails
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalDrugEvent
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalEvaluation
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalGeneticCharacteristic
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalLocationEvent
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalNote
import com.weyr_associates.animaltrakkerfarmmobile.model.BirthEvent
import com.weyr_associates.animaltrakkerfarmmobile.model.BreedingSummary
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.Gap
import com.weyr_associates.animaltrakkerfarmmobile.model.IdInfo
import com.weyr_associates.animaltrakkerfarmmobile.model.MovementEvent
import com.weyr_associates.animaltrakkerfarmmobile.model.Owner
import com.weyr_associates.animaltrakkerfarmmobile.model.Premise
import com.weyr_associates.animaltrakkerfarmmobile.model.Sex
import com.weyr_associates.animaltrakkerfarmmobile.model.TissueSampleEvent
import com.weyr_associates.animaltrakkerfarmmobile.model.TissueTestEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import kotlin.collections.map

class AnimalDetailsViewModel(
    private val animalId: EntityId,
    private val animalRepository: AnimalRepository,
    private val premiseRepository: PremiseRepository,
    private val loadActiveDefaultSettings: LoadActiveDefaultSettings
) : ViewModel(),
    AnimalGeneticsViewModelContract,
    AnimalAlertsViewModelContract,
    AnimalNotesViewModelContract,
    AnimalDrugHistoryViewModelContract,
    AnimalTissueSampleHistoryViewModelContract,
    AnimalTissueTestHistoryViewModelContract,
    AnimalEvaluationsHistoryViewModelContract,
    AnimalLocationTimelineViewModelContract,
    AnimalIdHistoryViewModelContract,
    AnimalBreedingSummaryViewModelContract,
    AnimalBreedingDetailsViewModelContract
{
    sealed interface AnimalDetailsState
    data object AnimalDetailsLoading : AnimalDetailsState
    data object AnimalDetailsNotFound : AnimalDetailsState
    data class AnimalDetailsLoaded(val animalDetails: AnimalDetails) : AnimalDetailsState

    private val premiseNicknameCache = mutableMapOf<EntityId, String?>()

    val animalDetails = flow {
        emit(AnimalDetailsLoading)
        animalRepository.queryAnimalDetailsByAnimalId(animalId)?.let { animalDetails ->
            emit(AnimalDetailsLoaded(animalDetails))
        } ?: emit(AnimalDetailsNotFound)
    }.stateIn(viewModelScope, SharingStarted.Lazily, AnimalDetailsLoading)

    override val animalGenetics: StateFlow<List<AnimalGeneticCharacteristic>?> = animalDetails.mapLatest { animalDetailsState ->
        mapFromAnimalDetailsState(animalDetailsState) { animalId -> animalRepository.queryGeneticCharacteristics(animalId).geneticCharacteristics }
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    override val animalAlerts: StateFlow<List<AnimalAlert>?> = animalDetails.mapLatest { animalDetailsState ->
        mapFromAnimalDetailsState(animalDetailsState, animalRepository::queryAnimalAlerts)
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    override val animalNoteHistory: StateFlow<List<AnimalNote>?> = animalDetails.mapLatest { animalDetailsState ->
        mapFromAnimalDetailsState(animalDetailsState, animalRepository::queryAnimalNoteHistory)
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    override val animalDrugHistory: StateFlow<List<AnimalDrugEvent>?> = animalDetails.mapLatest { animalDetailsState ->
        mapFromAnimalDetailsState(animalDetailsState, animalRepository::queryAnimalDrugHistory)
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    override val tissueSampleEventHistory: StateFlow<List<TissueSampleEvent>?> = animalDetails.mapLatest { animalDetailsState ->
        mapFromAnimalDetailsState(animalDetailsState, animalRepository::queryAnimalTissueSampleHistory)
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    override val tissueTestEventHistory: StateFlow<List<TissueTestEvent>?> = animalDetails.mapLatest { animalDetailsState ->
        mapFromAnimalDetailsState(animalDetailsState, animalRepository::queryAnimalTissueTestHistory)
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    override val animalEvaluationsHistory: StateFlow<List<AnimalEvaluation>?> = animalDetails.mapLatest { animalDetailsState ->
        mapFromAnimalDetailsState(animalDetailsState, animalRepository::queryAnimalEvaluationHistory)
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    override val animalLocationTimeline: StateFlow<List<AnimalLocationEvent>?> = animalRepository.animalLocationTimeline(animalId)
        .map { movements -> lookupNicknamesForLocationEventPremises(movements) }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    override val animalIdHistory: StateFlow<List<IdInfo>?> = animalRepository.animalIdHistory(animalId)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    override val animalBreedingSummary: StateFlow<BreedingSummary?> = animalRepository.breedingSummaryForAnimal(animalId)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    override val breedingDetailsState: StateFlow<BreedingDetailsState> =
        animalDetails.mapLatest { state ->
            when (state) {
                AnimalDetailsLoading -> NoBreedingDetailsState
                AnimalDetailsNotFound -> NoBreedingDetailsState
                is AnimalDetailsLoaded -> {
                    val animalId = state.animalDetails.id
                    val sexId = animalRepository.queryAnimalSexId(animalId)
                    when {
                        sexId == null -> NoBreedingDetailsState
                        Sex.isOrWasMale(sexId) -> MaleBreedingDetailsState(animalId)
                        Sex.isOrWasFemale(sexId) -> FemaleBreedingDetailsState(animalId)
                        else -> NoBreedingDetailsState
                    }
                }

            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, NoBreedingDetailsState) as StateFlow<BreedingDetailsState>

    private suspend fun <T> mapFromAnimalDetailsState(
        animalDetailsState: AnimalDetailsState,
        producer: suspend (animalId: EntityId) -> List<T>
    ): List<T>? {
        return when (animalDetailsState) {
            AnimalDetailsLoading -> null
            AnimalDetailsNotFound -> emptyList()
            is AnimalDetailsLoaded -> producer.invoke(
                animalDetailsState.animalDetails.id
            )
        }
    }

    private suspend fun lookupNicknamesForLocationEventPremises(locationEvents: List<AnimalLocationEvent>): List<AnimalLocationEvent> {
        val defaults = loadActiveDefaultSettings()
        val ownerId = defaults.ownerId
        val ownerType = defaults.ownerType?.let { Owner.Type.fromCode(it) }
        return if (ownerId != null && ownerType != null) {
            locationEvents.map { locationEvent ->
                when (locationEvent) {
                    is MovementEvent -> {
                        val fromNickname = lookupPremiseNickname(ownerId, ownerType, locationEvent.movement.fromPremise)
                        val toNickName = lookupPremiseNickname(ownerId, ownerType, locationEvent.movement.toPremise)
                        locationEvent.applyNicknames(fromNickname, toNickName)
                    }
                    is BirthEvent -> {
                        val premiseNickname = lookupPremiseNickname(ownerId, ownerType, locationEvent.premise)
                        locationEvent.copy(premise = locationEvent.premise?.copy(nickname = premiseNickname))
                    }
                    is Gap -> {
                        val fromNicknamePrev = lookupPremiseNickname(ownerId, ownerType, locationEvent.previousMovement.fromPremise)
                        val toNickNamePrev = lookupPremiseNickname(ownerId, ownerType, locationEvent.previousMovement.toPremise)
                        val fromNicknameNext = lookupPremiseNickname(ownerId, ownerType, locationEvent.previousMovement.fromPremise)
                        val toNickNameNext = lookupPremiseNickname(ownerId, ownerType, locationEvent.previousMovement.toPremise)
                        locationEvent.copy(
                            previousMovement = locationEvent.previousMovement.applyNicknames(fromNicknamePrev, toNickNamePrev),
                            nextMovement = locationEvent.nextMovement.applyNicknames(fromNicknameNext, toNickNameNext)
                        )
                    }
                    else -> locationEvent
                }
            }
        } else {
            locationEvents
        }
    }

    private suspend fun lookupPremiseNickname(ownerId: EntityId, ownerType: Owner.Type, premise: Premise?): String? {
        return premise?.let {
            premiseNicknameCache.getOrPut(premise.id) {
                withContext(Dispatchers.IO) {
                    premiseRepository.queryPremiseNickname(
                        premise.id,
                        ownerId,
                        ownerType
                    )
                }
            }
        }
    }
}
