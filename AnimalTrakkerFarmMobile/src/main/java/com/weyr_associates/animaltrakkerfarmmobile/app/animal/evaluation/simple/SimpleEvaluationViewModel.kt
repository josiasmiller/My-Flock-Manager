package com.weyr_associates.animaltrakkerfarmmobile.app.animal.evaluation.simple

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.evaluation.EvaluationAlerts
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.evaluation.EvaluationEditor
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.evaluation.EvaluationManager
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.lookup.AnimalInfoLookup
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.lookup.LookupAnimalInfo
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.lookup.LookupAnimalInfo.AnimalInfoState
import com.weyr_associates.animaltrakkerfarmmobile.app.core.ErrorReport
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.requireAs
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.takeAs
import com.weyr_associates.animaltrakkerfarmmobile.app.model.summarizeForErrorReport
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.AnimalRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.EvaluationRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseHandler
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalAlert
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalBasicInfo
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.EvalTrait
import com.weyr_associates.animaltrakkerfarmmobile.model.EvalTraitOption
import com.weyr_associates.animaltrakkerfarmmobile.model.ItemEntry
import com.weyr_associates.animaltrakkerfarmmobile.model.SavedEvaluation
import com.weyr_associates.animaltrakkerfarmmobile.model.Sex
import com.weyr_associates.animaltrakkerfarmmobile.model.SexStandard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
import java.time.LocalDateTime

class SimpleEvaluationViewModel(
    private val databaseHandler: DatabaseHandler,
    private val savedEvaluationId: EntityId,
    private val animalRepo: AnimalRepository,
    private val evaluationRepo: EvaluationRepository
) : ViewModel(), LookupAnimalInfo {

    private val animalInfoLookup = AnimalInfoLookup(viewModelScope, animalRepo)

    sealed interface Event

    data class AnimalAlertEvent(
        val alerts: List<AnimalAlert>
    ) : Event

    data object UpdateDatabaseSuccess : Event
    data object UpdateDatabaseError : Event

    data object AnimalRequiredToBeAlive : Event

    data class AnimalSexMismatch(
        val requiredSex: SexStandard,
        val sexName: String
    ) : Event

    data class AnimalSpeciesOrSexMismatch(
        val requiredSexId: EntityId,
        val speciesName: String,
        val sexName: String
    ) : Event

    override val animalInfoState: StateFlow<AnimalInfoState> = animalInfoLookup.animalInfoState

    private val evaluationManager = EvaluationManager(viewModelScope)
    val evaluationEditor = evaluationManager as EvaluationEditor

    val canClearData = evaluationManager.canClearData

    private val _isUpdatingDatabase = MutableStateFlow(false)

    val canSaveToDatabase = combine(
        animalInfoState,
        evaluationManager.isEvaluationComplete,
        _isUpdatingDatabase
    ) { animalInfoState, isEvaluationComplete, isUpdatingDatabase ->
        animalInfoState is AnimalInfoState.Loaded && isEvaluationComplete && !isUpdatingDatabase
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    private val _eventChannel = Channel<Event>()
    val events = _eventChannel.receiveAsFlow()

    private val errorReportChannel = Channel<ErrorReport>()
    val errorReportFlow = errorReportChannel.receiveAsFlow()

    private val _selectedEvaluation = MutableStateFlow<ItemEntry?>(null)
    val selectedEvaluation = _selectedEvaluation.asStateFlow()

    val canLoadEvaluation = combine(
        evaluationManager.loadedEvaluation,
        selectedEvaluation
    ) { loadedEval, selectedEval ->
        selectedEval != null && selectedEval.id != loadedEval?.id
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    init {
        viewModelScope.launch {
            evaluationManager.loadEvaluation(
                requireNotNull(
                    evaluationRepo.querySavedEvaluationById(savedEvaluationId)
                )
            )
        }
        viewModelScope.launch {
            evaluationManager.loadedEvaluation.collectLatest { loadedEval ->
                if (loadedEval != null) {
                    _selectedEvaluation.update {
                        ItemEntry(loadedEval.id, loadedEval.name)
                    }
                    onEvaluationLoaded(loadedEval)
                }
            }
        }
        viewModelScope.launch {
            animalInfoState.filter { it is AnimalInfoState.Loaded }
                .map { it.requireAs<AnimalInfoState.Loaded>().animalBasicInfo }
                .collectLatest {
                    clearData()
                    if(checkAnimalCanBeEvaluated(it)) {
                        if (it.alerts.isNotEmpty()) {
                            _eventChannel.send(AnimalAlertEvent(it.alerts))
                        }
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

    fun clearData() {
        if (canClearData.value) {
            evaluationManager.clearEvaluation()
        }
    }

    fun saveToDatabase() {
        if (canSaveToDatabase.value) {
            viewModelScope.launch {
                executeUpdateDatabase()
            }
        }
    }

    fun selectEvaluation(itemEntry: ItemEntry?) {
        _selectedEvaluation.update {
            itemEntry ?: evaluationManager.loadedEvaluation.value?.let {
                ItemEntry(it.id, it.name)
            }
        }
    }

    fun loadSelectedEvaluation() {
        val selectedEval = selectedEvaluation.value
        if (canLoadEvaluation.value && selectedEval != null) {
            viewModelScope.launch {
                evaluationRepo.querySavedEvaluationById(selectedEval.id)?.let {
                    evaluationManager.loadEvaluation(it)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        databaseHandler.close()
    }

    private fun onEvaluationLoaded(evaluation: SavedEvaluation) {
        evaluationManager.clearPreselectedCustomTraitOptionIds()
        preselectCustomTraitOptionIds(evaluation)
        //Resets the evaluation data to apply the new pre-selections.
        evaluationManager.clearEvaluation()
    }

    private fun preselectCustomTraitOptionIds(evaluation: SavedEvaluation) {
        when (evaluation.id) {
            SavedEvaluation.ID_SIMPLE_SORT ->
                evaluationManager.preselectCustomTraitToOptionId(
                    EvalTrait.OPTION_TRAIT_ID_SIMPLE_SORT,
                    EvalTraitOption.ID_SIMPLE_SORT_KEEP
                )
            SavedEvaluation.ID_OPTIMAL_LIVESTOCK_EWE_ULTRASOUND ->
                evaluationManager.preselectCustomTraitToOptionId(
                    EvalTrait.OPTION_TRAIT_ID_PREGNANCY_STATUS,
                    EvalTraitOption.ID_PREGNANCY_STATUS_PREGNANT
                )
        }
    }

    private fun checkAnimalCanBeEvaluated(animalBasicInfo: AnimalBasicInfo): Boolean {
        when (savedEvaluationId) {
            SavedEvaluation.ID_SIMPLE_SORT,
            SavedEvaluation.ID_SUCK_REFLEX -> {
                if (animalBasicInfo.isDead) {
                    viewModelScope.launch {
                        _eventChannel.send(AnimalRequiredToBeAlive)
                    }
                    return false
                }
            }
            SavedEvaluation.ID_SIMPLE_BIRTHS -> {
                if (!Sex.isFemale(animalBasicInfo.sexId)) {
                    viewModelScope.launch {
                        _eventChannel.send(
                            AnimalSexMismatch(
                                requiredSex = SexStandard.FEMALE,
                                animalBasicInfo.sexStandardName
                            )
                        )
                    }
                    return false
                }
            }
            SavedEvaluation.ID_SIMPLE_LAMBING -> {
                if (Sex.ID_SHEEP_EWE != animalBasicInfo.sexId) {
                    viewModelScope.launch {
                        _eventChannel.send(
                            AnimalSpeciesOrSexMismatch(
                                Sex.ID_SHEEP_EWE,
                                animalBasicInfo.speciesCommonName,
                                animalBasicInfo.sexName
                            )
                        )
                    }
                    return false
                }
            }
            SavedEvaluation.ID_OPTIMAL_LIVESTOCK_EWE_ULTRASOUND -> {
                if (animalBasicInfo.isDead) {
                    viewModelScope.launch {
                        _eventChannel.send(AnimalRequiredToBeAlive)
                    }
                    return false
                }
                else if (Sex.ID_SHEEP_EWE != animalBasicInfo.sexId) {
                    viewModelScope.launch {
                        _eventChannel.send(
                            AnimalSpeciesOrSexMismatch(
                                Sex.ID_SHEEP_EWE,
                                animalBasicInfo.speciesCommonName,
                                animalBasicInfo.sexName
                            )
                        )
                    }
                    return false
                }
            }
        }
        return true
    }

    private suspend fun executeUpdateDatabase() {

        val animalInfoLoaded = animalInfoState.value.takeAs<AnimalInfoState.Loaded>() ?: return
        val animalInfo = animalInfoLoaded.animalBasicInfo
        val loadedEvaluation = evaluationManager.loadedEvaluation.value ?: return
        val evaluationEntry = evaluationManager.extractEvaluationEntry() ?: return
        val ageInDays = animalInfo.ageInDays()
        val timeStamp = LocalDateTime.now()

        try {

            _isUpdatingDatabase.update { true }

            withContext(Dispatchers.IO) {
                val animalEvaluationId = animalRepo.addEvaluationForAnimal(
                    animalId = animalInfo.id,
                    ageInDays = ageInDays,
                    timeStamp = timeStamp,
                    trait01Id = evaluationEntry.trait01Id,
                    trait02Id = evaluationEntry.trait02Id,
                    trait03Id = evaluationEntry.trait03Id,
                    trait04Id = evaluationEntry.trait04Id,
                    trait05Id = evaluationEntry.trait05Id,
                    trait06Id = evaluationEntry.trait06Id,
                    trait07Id = evaluationEntry.trait07Id,
                    trait08Id = evaluationEntry.trait08Id,
                    trait09Id = evaluationEntry.trait09Id,
                    trait10Id = evaluationEntry.trait10Id,
                    trait11Id = evaluationEntry.trait11Id,
                    trait12Id = evaluationEntry.trait12Id,
                    trait13Id = evaluationEntry.trait13Id,
                    trait14Id = evaluationEntry.trait14Id,
                    trait15Id = evaluationEntry.trait15Id,
                    trait16Id = evaluationEntry.trait16Id,
                    trait17Id = evaluationEntry.trait17Id,
                    trait18Id = evaluationEntry.trait18Id,
                    trait19Id = evaluationEntry.trait19Id,
                    trait20Id = evaluationEntry.trait20Id,
                    trait01Score = evaluationEntry.trait01Score,
                    trait02Score = evaluationEntry.trait02Score,
                    trait03Score = evaluationEntry.trait03Score,
                    trait04Score = evaluationEntry.trait04Score,
                    trait05Score = evaluationEntry.trait05Score,
                    trait06Score = evaluationEntry.trait06Score,
                    trait07Score = evaluationEntry.trait07Score,
                    trait08Score = evaluationEntry.trait08Score,
                    trait09Score = evaluationEntry.trait09Score,
                    trait10Score = evaluationEntry.trait10Score,
                    trait11Score = evaluationEntry.trait11Score,
                    trait12Score = evaluationEntry.trait12Score,
                    trait13Score = evaluationEntry.trait13Score,
                    trait14Score = evaluationEntry.trait14Score,
                    trait15Score = evaluationEntry.trait15Score,
                    trait16OptionId = evaluationEntry.trait16OptionId,
                    trait17OptionId = evaluationEntry.trait17OptionId,
                    trait18OptionId = evaluationEntry.trait18OptionId,
                    trait19OptionId = evaluationEntry.trait19OptionId,
                    trait20OptionId = evaluationEntry.trait20OptionId,
                    trait11UnitsId = evaluationEntry.trait11UnitsId,
                    trait12UnitsId = evaluationEntry.trait12UnitsId,
                    trait13UnitsId = evaluationEntry.trait13UnitsId,
                    trait14UnitsId = evaluationEntry.trait14UnitsId,
                    trait15UnitsId = evaluationEntry.trait15UnitsId
                )

                val evaluationSummary = EvaluationAlerts.evaluationSummaryFor(
                    animalEvaluationId,
                    loadedEvaluation,
                    evaluationEntry
                )

                if (evaluationSummary != null) {
                    animalRepo.addEvaluationSummaryAlertForAnimal(
                        animalInfo.id,
                        evaluationSummary,
                        timeStamp
                    )
                    animalInfoLookup.lookupAnimalInfoById(animalInfo.id)
                }
            }
            clearData()
            _eventChannel.send(UpdateDatabaseSuccess)
        } catch(exception: Exception) {
            errorReportChannel.send(
                ErrorReport(
                    action = "Simple Animal Evaluation",
                    summary = buildString {
                        append("animalId=${animalInfo.id}, ")
                        append("evaluationId=${loadedEvaluation.id}, ")
                        append(evaluationEntry.summarizeForErrorReport())
                    },
                    error = exception
                )
            )
        } finally {
            _isUpdatingDatabase.update { false }
        }
    }
}
