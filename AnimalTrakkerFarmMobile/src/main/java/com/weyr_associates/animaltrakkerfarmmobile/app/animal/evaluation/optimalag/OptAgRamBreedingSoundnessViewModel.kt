package com.weyr_associates.animaltrakkerfarmmobile.app.animal.evaluation.optimalag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.evaluation.EvaluationAlerts
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.evaluation.EvaluationEditor
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.evaluation.EvaluationManager
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.lookup.AnimalInfoLookup
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.lookup.LookupAnimalInfo
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.lookup.LookupAnimalInfo.AnimalInfoState
import com.weyr_associates.animaltrakkerfarmmobile.app.core.ErrorReport
import com.weyr_associates.animaltrakkerfarmmobile.app.core.Result
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.requireAs
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.takeAs
import com.weyr_associates.animaltrakkerfarmmobile.app.label.ExtractPrintLabelData
import com.weyr_associates.animaltrakkerfarmmobile.app.label.PrintLabelData
import com.weyr_associates.animaltrakkerfarmmobile.app.model.summarizeForErrorReport
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.AnimalRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.EvaluationRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseHandler
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalAlert
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalBasicInfo
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.EvalTrait
import com.weyr_associates.animaltrakkerfarmmobile.model.EvalTraitOption
import com.weyr_associates.animaltrakkerfarmmobile.model.SavedEvaluation
import com.weyr_associates.animaltrakkerfarmmobile.model.Sex
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
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class OptAgRamBreedingSoundnessViewModel(
    private val databaseHandler: DatabaseHandler,
    private val animalRepo: AnimalRepository,
    private val evaluationRepo: EvaluationRepository,
    private val extractPrintLabelData: ExtractPrintLabelData
) : ViewModel(), LookupAnimalInfo {

    private val animalInfoLookup = AnimalInfoLookup(viewModelScope, animalRepo)

    sealed interface Event

    data class AnimalAlertEvent(
        val alerts: List<AnimalAlert>
    ) : Event

    data object UpdateDatabaseSuccess : Event

    data class AnimalSpeciesOrSexMismatch(
        val speciesName: String,
        val sexName: String
    ) : Event

    data class TakeTissueSamplesInfo(
        val animalBasicInfo: AnimalBasicInfo,
        val printLabelData: PrintLabelData
    )

    override val animalInfoState: StateFlow<AnimalInfoState> = animalInfoLookup.animalInfoState

    private val evaluationManager = EvaluationManager(viewModelScope)
    val evaluationEditor = evaluationManager as EvaluationEditor

    private val _tissueSamplesInfo = MutableStateFlow<TakeTissueSamplesInfo?>(null)
    val tissueSamplesInfo = _tissueSamplesInfo.asStateFlow()

    val canClearData = evaluationManager.canClearData

    private val _isUpdatingDatabase = MutableStateFlow(false)

    val canSaveToDatabase = combine(
        animalInfoState,
        evaluationManager.isEvaluationComplete,
        _isUpdatingDatabase
    ) { animalInfoState, isEvaluationComplete, isUpdatingDatabase ->
        animalInfoState is AnimalInfoState.Loaded && isEvaluationComplete && !isUpdatingDatabase
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    val canTakeTissueSamples = tissueSamplesInfo.mapLatest { it != null }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    private val _eventChannel = Channel<Event>()
    val events = _eventChannel.receiveAsFlow()

    private val errorReportChannel = Channel<ErrorReport>()
    val errorReportFlow = errorReportChannel.receiveAsFlow()

    init {
        evaluationManager.preselectCustomTraitToOptionId(
            EvalTrait.OPTION_TRAIT_ID_CUSTOM_SCROTAL_PALPATION,
            EvalTraitOption.ID_CUSTOM_SCROTAL_PALPATION_SATISFACTORY
        )
        viewModelScope.launch {
            evaluationManager.loadEvaluation(
                requireNotNull(
                    evaluationRepo.querySavedEvaluationById(SavedEvaluation.ID_OPTIMAL_AG_RAM_TEST)
                )
            )
        }
        viewModelScope.launch {
            animalInfoState.collectLatest { state ->
                when (state) {
                    is AnimalInfoState.Loaded -> {
                        _tissueSamplesInfo.value?.let { currentTissueSamplesInfo ->
                            if (currentTissueSamplesInfo.animalBasicInfo.id == state.animalBasicInfo.id) {
                                _tissueSamplesInfo.update { it?.copy(animalBasicInfo = state.animalBasicInfo) }
                            } else {
                                _tissueSamplesInfo.update { null }
                            }
                        }
                    }
                    else -> {
                        _tissueSamplesInfo.update { null }
                    }
                }
            }
        }
        viewModelScope.launch {
            animalInfoState.filter { it is AnimalInfoState.Loaded }
                .map { it.requireAs<AnimalInfoState.Loaded>().animalBasicInfo }
                .collectLatest {
                    clearData()
                    if (it.sexId != Sex.ID_SHEEP_RAM) {
                        _eventChannel.send(
                            AnimalSpeciesOrSexMismatch(
                                speciesName = it.speciesCommonName,
                                sexName = it.sexName
                            )
                        )
                    } else {
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

    override fun onCleared() {
        super.onCleared()
        databaseHandler.close()
    }

    private suspend fun executeUpdateDatabase() {
        val animalInfoLoaded = animalInfoState.value.takeAs<AnimalInfoState.Loaded>() ?: return
        val loadedEvaluation = evaluationManager.loadedEvaluation.value ?: return
        val animalInfo = animalInfoLoaded.animalBasicInfo
        val evaluationEntry = evaluationManager.extractEvaluationEntry() ?: return
        val ageInDays = animalInfo.ageInDays()

        try {

            _isUpdatingDatabase.update { true }

            val timeStamp = LocalDateTime.now()

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
            val printLabelDataResult = extractPrintLabelData.forOptimalAgRamBSETissueSamples(
                animalInfoLoaded.eidNumber,
                animalInfo,
                evaluationEntry
            )
            if (printLabelDataResult is Result.Success) {
                _tissueSamplesInfo.update {
                    TakeTissueSamplesInfo(
                        animalInfo,
                        printLabelDataResult.data
                    )
                }
            }
            clearData()
            _eventChannel.send(UpdateDatabaseSuccess)
        } catch(exception: Exception) {
            errorReportChannel.send(
                ErrorReport(
                    action = "Optimal Livestock Ram Breeding Soundness",
                    summary = buildString {
                        append("animalId=${animalInfo.id}, ")
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
