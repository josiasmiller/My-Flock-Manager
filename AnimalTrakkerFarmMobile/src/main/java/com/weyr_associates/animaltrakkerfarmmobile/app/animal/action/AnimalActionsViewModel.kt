package com.weyr_associates.animaltrakkerfarmmobile.app.animal.action

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.care.AnimalCareConfiguration
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.care.hooves.HoofCheckAction
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.care.horns.HornCheckAction
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.care.shear.ShearAction
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.care.shoe.ShoeAction
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.care.wean.WeanAction
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.drug.DrugAction
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.weight.WeightAction
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.alert.AnimalAlertEvent
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.alert.EmitsAnimalAlerts
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.alert.extractAnimalAlertEvents
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.lookup.AnimalInfoLookup
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.lookup.LookupAnimalInfo
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.lookup.LookupAnimalInfo.AnimalInfoState.Loaded
import com.weyr_associates.animaltrakkerfarmmobile.app.core.ErrorReport
import com.weyr_associates.animaltrakkerfarmmobile.app.core.Result
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.coroutines.channel.eventEmitter
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.requireAs
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.takeAs
import com.weyr_associates.animaltrakkerfarmmobile.app.model.summarizeForErrorReport
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.AnimalRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.UnitOfMeasureRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalBasicInfo
import com.weyr_associates.animaltrakkerfarmmobile.model.DrugType
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.HoofCheck
import com.weyr_associates.animaltrakkerfarmmobile.model.HornCheck
import com.weyr_associates.animaltrakkerfarmmobile.model.UnitOfMeasure
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.util.UUID

class AnimalActionsViewModel(
    private val animalActionsFeature: AnimalActionsFeature,
    private val animalRepo: AnimalRepository,
    private val unitsRepo: UnitOfMeasureRepository,
    private val loadDefaultSettings: LoadActiveDefaultSettings,
    private val trackAnimalActions: TrackAnimalActions
) : ViewModel(), LookupAnimalInfo, EmitsAnimalAlerts {

    sealed interface Event

    data object AnimalRequiredToBeAlive : Event

    data class ConfigureAnimalCareEvent(
        val configuration: AnimalCareConfiguration
    ) : Event

    data class AddDrugConfigurationEvent(
        val drugTypeId: EntityId,
        val excludedDrugIds: Set<EntityId>
    ) : Event

    data class EditDrugConfigurationEvent(
        val actionId: UUID,
        val configuration: DrugAction.Configuration,
        val excludedDrugIds: Set<EntityId>
    ) : Event

    sealed interface UpdateDatabaseEvent : Event {
        data object Success : UpdateDatabaseEvent
        data object AnimalDoesNotExist : UpdateDatabaseEvent
        data class NoDrugDosageValidForAnimal(
            val error: NoValidDrugDosageForSpecies
        ) : UpdateDatabaseEvent
        data class InvalidDrugDosageForAnimal(
            val error: DrugDoseAnimalSpeciesMismatch
        ) : UpdateDatabaseEvent
        data object Error : UpdateDatabaseEvent
    }

    private var weightUnits: UnitOfMeasure? = null

    private val animalInfoLookup = AnimalInfoLookup(viewModelScope, animalRepo)
    private val animalAlertEventEmitter = eventEmitter<AnimalAlertEvent>()
    private val internalEventsFlow = MutableSharedFlow<Event>()
    private val isUpdatingDatabase = MutableStateFlow(false)

    private val eventsChannel = Channel<Event>()
    val events = eventsChannel.receiveAsFlow()

    private val errorReportChannel = Channel<ErrorReport>()
    val errorReportFlow = errorReportChannel.receiveAsFlow()

    override val animalInfoState = animalInfoLookup.animalInfoState

    override val animalAlertsEvent: Flow<AnimalAlertEvent> =
        animalAlertEventEmitter.events

    val animalInfo = animalInfoState.map { it.takeAs<Loaded>()?.animalBasicInfo }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val animalWeight = animalInfoState.flatMapConcat {
        flow {
            emit(null)
            if (it is Loaded) {
                emit(animalRepo.queryAnimalLastEvaluationWeight(it.animalBasicInfo.id))
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val speciesIdForDrugDoses = combine(
        flow { emit(withContext(Dispatchers.IO) { loadDefaultSettings() }.speciesId) },
        animalInfoState.map { it.takeAs<Loaded>()?.animalBasicInfo?.speciesId }
    ) { defaultSpeciesId, selectedAnimalSpeciesId ->
        selectedAnimalSpeciesId ?: defaultSpeciesId
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val _actions = MutableStateFlow<ActionSet?>(null)
    val actions = _actions.asStateFlow()

    val isConfigured = actions.map {
        it?.let { actionSet ->
            when (animalActionsFeature) {
                AnimalActionsFeature.GENERAL_ANIMAL_CARE -> {
                    actionSet.isAnimalCareConfigured
                }
                AnimalActionsFeature.ADMINISTER_DRUGS,
                    AnimalActionsFeature.VACCINES_AND_DEWORMERS -> {
                        actionSet.areDrugsConfigured
                }
            }
        } ?: false
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    val canAddDewormer = actions.map { it?.canAddDewormers ?: false }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val canAddVaccine = actions.map { it?.canAddVaccines ?: false }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val canClearData = actions.map { actionSet ->
        val actionSetIsConfigured = actionSet?.isConfigured ?: false
        val canClearVaccines = actionSet?.vaccines
            ?.any { it.isComplete != it.configuration.autoApplyDrug } ?: false
        val canClearDewormers = actionSet?.dewormers
            ?.any { it.isComplete != it.configuration.autoApplyDrug } ?: false
        val canClearOtherDrugs = actionSet?.otherDrugs
            ?.any { it.isComplete != it.configuration.autoApplyDrug } ?: false
        val canClearAnyDrugs = canClearVaccines || canClearDewormers || canClearOtherDrugs
        val canClearWeight = actionSet?.weight?.isComplete ?: false
        val canClearHooves = actionSet?.hoofCheck?.isComplete ?: false
        val canClearHorns = actionSet?.hornCheck?.isComplete ?: false
        val canClearShoeing = actionSet?.shoeing?.isComplete ?: false
        val canClearShearing = actionSet?.shearing?.isComplete ?: false
        val canClearWeaning = actionSet?.weaning?.isComplete ?: false
        val canClearAnyData = canClearAnyDrugs || canClearWeight ||
                canClearHooves || canClearHorns || canClearShoeing ||
                canClearShearing || canClearWeaning
        actionSetIsConfigured && canClearAnyData
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    private val dataSaved = merge(
        internalEventsFlow.filterIsInstance<UpdateDatabaseEvent.Success>(),
        animalInfoState.filterIsInstance<Loaded>()
            .map { it.animalBasicInfo.id }.distinctUntilChanged(),
        actions
    ).map {
        when (it) {
            is UpdateDatabaseEvent.Success -> true
            else -> false
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    val canSaveToDatabase = combine(
        animalInfoState,
        isUpdatingDatabase,
        dataSaved,
        actions
    ) { animalInfoState, isUpdatingDatabase, dataSaved, actionSet ->
        val actionSetIsConfigured = actionSet?.isAnimalCareConfigured ?: false
        val animalInfoLoaded = animalInfoState is Loaded
        val hasCompletedVaccines = actionSet?.vaccines?.any { it.isComplete } ?: false
        val hasCompletedDewormers = actionSet?.dewormers?.any { it.isComplete } ?: false
        val hasCompletedOtherDrugs = actionSet?.otherDrugs?.any { it.isComplete } ?: false
        val hasCompletedHoofCheck = actionSet?.hoofCheck?.isComplete ?: false
        val hasCompletedHornCheck = actionSet?.hornCheck?.isComplete ?: false
        val hasCompletedShoeing = actionSet?.shoeing?.isComplete ?: false
        val hasCompletedShearing = actionSet?.shearing?.isComplete ?: false
        val hasCompletedWeaning = actionSet?.weaning?.isComplete ?: false
        val hasCompletedWeight = actionSet?.weight?.isComplete ?: false
        val hasCompletedAnimalCare = hasCompletedHoofCheck || hasCompletedHornCheck ||
                hasCompletedShoeing || hasCompletedShearing || hasCompletedWeaning ||
                hasCompletedWeight
        !dataSaved && !isUpdatingDatabase && animalInfoLoaded && actionSetIsConfigured &&
                (hasCompletedVaccines || hasCompletedDewormers || hasCompletedOtherDrugs || hasCompletedAnimalCare)
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    init {
        viewModelScope.launch {
            _actions.update { initializeActionSet() }
        }
        viewModelScope.launch {
            animalInfo.collectLatest { animalInfo ->
                _actions.update {
                    it?.copy(
                       weaning = it.weaning?.copy(
                           isActionable = animalInfo.canBeWeaned(),
                           isComplete = animalInfo.canBeWeaned() && it.weaning.isComplete
                       )
                    )
                }
            }
        }
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
        animalAlertEventEmitter.forwardFrom(
            animalInfoState.extractAnimalAlertEvents()
        )
        viewModelScope.launch {
            speciesIdForDrugDoses.collectLatest { speciesId ->
                _actions.update { currentActions ->
                    currentActions?.copy(
                        targetSpeciesId = speciesId,
                        dewormers = currentActions.dewormers.map { it.copy(targetSpeciesId = speciesId) },
                        vaccines = currentActions.vaccines.map { it.copy(targetSpeciesId = speciesId) },
                        otherDrugs = currentActions.otherDrugs.map { it.copy(targetSpeciesId = speciesId) }
                    )
                }
            }
        }
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

    fun onConfigureAnimalCare() {
        _actions.value?.let { actionSet ->
            viewModelScope.launch {
                eventsChannel.send(
                    ConfigureAnimalCareEvent(
                        AnimalCareConfiguration(
                            hooves = actionSet.hoofCheck != null,
                            horns = actionSet.hornCheck != null,
                            shoe = actionSet.shoeing != null,
                            shear = actionSet.shearing != null,
                            wean = actionSet.weaning != null,
                            weight = actionSet.weight != null
                        )
                    )
                )
            }
        }
    }

    fun onAnimalCareConfigured(configuration: AnimalCareConfiguration) {
        viewModelScope.launch {
            _actions.update { actionSet ->
                (actionSet ?: ActionSet(targetSpeciesId = speciesIdForDrugDoses.value)).let {
                    it.copy(
                        hoofCheck = if (configuration.hooves)
                            it.hoofCheck?.copy() ?: HoofCheckAction() else null,
                        hornCheck = if (configuration.horns)
                            it.hornCheck?.copy() ?: HornCheckAction() else null,
                        shoeing = if (configuration.shoe)
                            it.shoeing?.copy() ?: ShoeAction() else null,
                        shearing = if (configuration.shear)
                            it.shearing?.copy() ?: ShearAction() else null,
                        weaning = if (configuration.wean) it.weaning?.copy() ?: WeanAction(
                            isActionable = animalInfo.value.canBeWeaned()
                        ) else null,
                        weight = if (configuration.weight)
                            it.weight?.copy() ?: WeightAction(units = queryWeightUnits()) else null
                    )
                }
            }
        }
    }

    fun onConfigureDrugAction(drugTypeId: EntityId) {
        val excludedDrugIds = collectExcludedDrugIdsForDrugType(drugTypeId)
        viewModelScope.launch {
            eventsChannel.send(
                AddDrugConfigurationEvent(
                    drugTypeId = drugTypeId,
                    excludedDrugIds = excludedDrugIds
                )
            )
        }
    }

    fun onDrugActionConfigured(configuration: DrugAction.Configuration) {
        _actions.update {
            it?.addDrugAction(configuration)
        }
    }

    fun onEditDrugAction(drugAction: DrugAction) {
        _actions.value?.let { actionSet ->
            if (actionSet.containsDrugAction(drugAction)) {
                val excludedDrugIds = collectExcludedDrugIdsForDrugType(
                    drugAction.configuration.drugApplicationInfo.drugTypeId
                ).filter { it != drugAction.configuration.drugApplicationInfo.id }.toSet()
                viewModelScope.launch {
                    eventsChannel.send(
                        EditDrugConfigurationEvent(
                            actionId = drugAction.actionId,
                            configuration = drugAction.configuration,
                            excludedDrugIds = excludedDrugIds
                        )
                    )
                }
            }
        }
    }

    fun onDrugActionConfigurationEdited(actionId: UUID, configuration: DrugAction.Configuration) {
        _actions.update {
            it?.updateDrugAction(
                actionId,
                configuration
            )
        }
    }

    fun onRemoveDrugAction(drugAction: DrugAction) {
        _actions.update { it?.removeDrugAction(drugAction) }
    }

    fun onAnimalActionActivated(action: AnimalAction) {
        when (action) {
            is DrugAction -> {
                _actions.update { actionSet ->
                    actionSet?.markDrugAppliedForAction(action, !action.isDrugApplied)
                }
            }
            is ShoeAction -> {
                _actions.update { actionSet ->
                    actionSet?.copy(
                        shoeing = action.copy(isComplete = !action.isComplete)
                    )
                }
            }
            is ShearAction -> {
                _actions.update { actionSet ->
                    actionSet?.copy(
                        shearing = action.copy(isComplete = !action.isComplete)
                    )
                }
            }
            is WeanAction -> {
                _actions.update { actionSet ->
                    val canBeWeaned = animalInfo.value.canBeWeaned()
                    actionSet?.copy(
                        weaning = action.copy(
                            isComplete = canBeWeaned && !action.isComplete
                        )
                    )
                }
            }
        }
    }

    fun onWeightValueEntered(weight: Float?) {
        _actions.update {
            it?.copy(weight = it.weight?.copy(weight = weight))
        }
    }

    fun onHoofCheckCompleted(result: HoofCheck) {
        _actions.update {
            it?.copy(hoofCheck = it.hoofCheck?.copy(hoofCheck = result))
        }
    }

    fun onHornCheckCompleted(result: HornCheck) {
        _actions.update {
            it?.copy(hornCheck = it.hornCheck?.copy(hornCheck = result))
        }
    }

    fun onClearAction(action: AnimalAction) {
        when (action) {
            is DrugAction -> {
                _actions.update {
                    it?.markDrugAppliedForAction(action, action.configuration.autoApplyDrug)
                }
            }
            is HoofCheckAction -> {
                _actions.update {
                    it?.copy(hoofCheck = it.hoofCheck?.copy(hoofCheck = null))
                }
            }
            is HornCheckAction -> {
                _actions.update {
                    it?.copy(hornCheck = it.hornCheck?.copy(hornCheck = null))
                }
            }
            is ShoeAction -> {
                _actions.update {
                    it?.copy(shoeing = it.shoeing?.copy(isComplete = false))
                }
            }
            is ShearAction -> {
                _actions.update {
                    it?.copy(shearing = it.shearing?.copy(isComplete = false))
                }
            }
            is WeanAction -> {
                _actions.update {
                    it?.copy(weaning = it.weaning?.copy(isComplete = false))
                }
            }
            is WeightAction -> {
                _actions.update {
                    it?.copy(weight = it.weight?.copy(weight = null))
                }
            }
        }
    }

    fun onRemoveAction(action: AnimalAction) {
        when (action) {
            is DrugAction -> {
                _actions.update {
                    it?.removeDrugAction(action)
                }
            }
            is HoofCheckAction -> {
                _actions.update {
                    it?.copy(hoofCheck = null)
                }
            }
            is HornCheckAction -> {
                _actions.update {
                    it?.copy(hornCheck = null)
                }
            }
            is ShoeAction -> {
                _actions.update {
                    it?.copy(shoeing = null)
                }
            }
            is ShearAction -> {
                _actions.update {
                    it?.copy(shearing = null)
                }
            }
            is WeanAction -> {
                _actions.update {
                    it?.copy(weaning = null)
                }
            }
            is WeightAction -> {
                _actions.update {
                    it?.copy(weight = null)
                }
            }
        }
    }

    fun clearData() {
        if (canClearData.value) {
            executeClearData()
        }
    }

    fun saveToDatabase() {
        if (canSaveToDatabase.value) {
            viewModelScope.launch {
                updateDatabase()
            }
        }
    }

    private suspend fun initializeActionSet(): ActionSet {
        return when (animalActionsFeature) {
            AnimalActionsFeature.VACCINES_AND_DEWORMERS,
                AnimalActionsFeature.ADMINISTER_DRUGS -> {
                    ActionSet(
                        weight = WeightAction(
                            isFixedInConfiguration = true,
                            units = queryWeightUnits()
                        ),
                        targetSpeciesId = speciesIdForDrugDoses.value
                    )
                }
            else -> ActionSet(targetSpeciesId = speciesIdForDrugDoses.value)
        }
    }

    private suspend fun queryWeightUnits(): UnitOfMeasure {
        return weightUnits ?: requireNotNull(
            unitsRepo.queryUnitOfMeasureById(
                withContext(Dispatchers.IO) {
                    loadDefaultSettings().weightUnitsId
                }
            ).also { weightUnits = it }
        )
    }

    private fun AnimalBasicInfo?.canBeWeaned(): Boolean = this == null || !isWeaned

    private fun collectExcludedDrugIdsForDrugType(drugTypeId: EntityId): Set<EntityId> {
        return when (drugTypeId) {
            DrugType.ID_VACCINE -> actions.value?.vaccines
            DrugType.ID_DEWORMER -> actions.value?.dewormers
            else -> actions.value?.otherDrugs
        }?.map {
            it.configuration.drugApplicationInfo.id
        }?.toSet() ?: emptySet()
    }

    private fun executeClearData() {
        _actions.update {
            it?.resetAllActions()
        }
    }

    private suspend fun updateDatabase() {

        val animalInfo = animalInfoLookup.animalInfoState.value
            .takeAs<Loaded>()
            ?.animalBasicInfo ?: return

        val actionSet = actions.value ?: return

        try {

            isUpdatingDatabase.value = true

            val result = withContext(Dispatchers.IO) {
                trackAnimalActions(
                    animalId = animalInfo.id,
                    animalInfo.ageInDays(),
                    actions = actionSet,
                    timeStamp = LocalDateTime.now()
                )
            }

            when (result) {
                is Result.Success -> {
                    animalInfoLookup.lookupAnimalInfoById(animalInfo.id)
                    executeClearData()
                    postEvent(UpdateDatabaseEvent.Success)
                }
                is Result.Failure -> {
                    eventsChannel.send(
                        when (val error = result.error) {
                            AnimalDoesNotExist -> {
                                UpdateDatabaseEvent.AnimalDoesNotExist
                            }
                            is NoValidDrugDosageForSpecies -> {
                                UpdateDatabaseEvent.NoDrugDosageValidForAnimal(error)
                            }
                            is DrugDoseAnimalSpeciesMismatch -> {
                                UpdateDatabaseEvent.InvalidDrugDosageForAnimal(error)
                            }
                            is FatalError -> {
                                UpdateDatabaseEvent.Error
                            }
                        }
                    )
                }
            }
        } catch(ex: Exception) {
            errorReportChannel.send(
                ErrorReport(
                    action = "Animal Actions",
                    summary = actionSet.summarizeForErrorReport(),
                    error = ex
                )
            )
        } finally {
            isUpdatingDatabase.value = false
        }
    }

    private fun postEvent(event: Event) {
        viewModelScope.launch {
            internalEventsFlow.emit(event)
            eventsChannel.send(event)
        }
    }
}
