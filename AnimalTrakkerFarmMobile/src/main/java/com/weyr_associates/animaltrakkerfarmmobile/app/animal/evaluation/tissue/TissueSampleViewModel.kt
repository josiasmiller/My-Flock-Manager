package com.weyr_associates.animaltrakkerfarmmobile.app.animal.evaluation.tissue

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.lookup.AnimalInfoLookup
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.lookup.LookupAnimalInfo
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.lookup.LookupAnimalInfo.AnimalInfoState
import com.weyr_associates.animaltrakkerfarmmobile.app.core.ErrorReport
import com.weyr_associates.animaltrakkerfarmmobile.app.core.Result
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.coroutines.flow.combine7
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.requireAs
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.takeAs
import com.weyr_associates.animaltrakkerfarmmobile.app.label.ExtractPrintLabelData
import com.weyr_associates.animaltrakkerfarmmobile.app.label.ExtractPrintLabelDataError
import com.weyr_associates.animaltrakkerfarmmobile.app.label.PrintLabelData
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.AnimalRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.LaboratoryRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.SpeciesRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.TissueSampleContainerTypeRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.TissueSampleTypeRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.TissueTestRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseHandler
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalAlert
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalBasicInfo
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.Laboratory
import com.weyr_associates.animaltrakkerfarmmobile.model.Species
import com.weyr_associates.animaltrakkerfarmmobile.model.TissueSampleContainerType
import com.weyr_associates.animaltrakkerfarmmobile.model.TissueSampleType
import com.weyr_associates.animaltrakkerfarmmobile.model.TissueTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime

class TissueSampleViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val databaseHandler: DatabaseHandler,
    private val loadActiveDefaultSettings: LoadActiveDefaultSettings,
    private val speciesRepo: SpeciesRepository,
    private val animalRepo: AnimalRepository,
    private val laboratoryRepository: LaboratoryRepository,
    private val tissueSampleTypeRepository: TissueSampleTypeRepository,
    private val tissueTestRepository: TissueTestRepository,
    private val tissueSampleContainerTypeRepository: TissueSampleContainerTypeRepository,
    private val extractPrintLabelData: ExtractPrintLabelData
) : ViewModel(), LookupAnimalInfo {

    private val animalInfoLookup = AnimalInfoLookup(viewModelScope, animalRepo)

    sealed interface Event

    sealed interface InputEvent : Event {
        data object ContainerIdChanged : InputEvent
    }

    data object IncompleteDataEntry : Event

    data class AnimalAlertEvent(
        val alerts: List<AnimalAlert>
    ) : Event

    data class AnimalSpeciesMismatch(
        val defaultSpeciesName: String,
        val animalSpeciesName: String
    ) : Event

    data class PrintLabelRequestedError(
        val error: ExtractPrintLabelDataError
    ) : Event

    data class PrintLabelRequestedEvent(
        val printLabelData: PrintLabelData
    ) : Event

    sealed interface UpdateDatabaseEvent : Event {
        data object Success : UpdateDatabaseEvent
    }

    private lateinit var defaultSpecies: Species
    private var secondaryIdTypeId: EntityId? = null

    private val _eventFlow = MutableSharedFlow<Event>()
    private val _eventChannel = Channel<Event>()
    val events = _eventChannel.receiveAsFlow()

    private val errorReportChannel = Channel<ErrorReport>()
    val errorReportFlow = errorReportChannel.receiveAsFlow()

    private val _containerId = MutableStateFlow("")
    var containerId: String
        get() = _containerId.value
        set(value) { _containerId.update { value } }

    override val animalInfoState: StateFlow<AnimalInfoState> = animalInfoLookup.animalInfoState

    private val _selectedLaboratory = MutableStateFlow<Laboratory?>(null)
    val selectedLaboratory = _selectedLaboratory.asStateFlow()

    private val _selectedTissueSampleType = MutableStateFlow<TissueSampleType?>(null)
    val selectedTissueSampleType = _selectedTissueSampleType.asStateFlow()

    private val _selectedTissueTestType = MutableStateFlow<TissueTest?>(null)
    val selectedTissueTestType = _selectedTissueTestType.asStateFlow()

    private val _selectedSampleContainerType = MutableStateFlow<TissueSampleContainerType?>(null)
    val selectTissueContainerType = _selectedSampleContainerType.asStateFlow()

    private val _selectedSampleContainerExpirationDate = MutableStateFlow<LocalDate?>(null)
    val selectedSampleContainerExpirationDate = _selectedSampleContainerExpirationDate.asStateFlow()

    val canClearData = combine(_containerId, _selectedSampleContainerExpirationDate) { containerId, containerExpDate ->
        containerId.isNotEmpty() || containerExpDate != null
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    private val _isUpdatingDatabase = MutableStateFlow(false)
    
    private val dataSaved = merge(
        _eventFlow.filterIsInstance<UpdateDatabaseEvent.Success>(),
        animalInfoState.filterIsInstance<AnimalInfoState.Loaded>(),
        selectedLaboratory,
        selectedTissueSampleType,
        selectedTissueTestType,
        selectTissueContainerType,
        selectedSampleContainerExpirationDate,
        _containerId
    ).map {
        when (it) {
            is UpdateDatabaseEvent.Success -> true
            else -> false
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    val canSaveToDatabase = combine7(
        animalInfoState,
        dataSaved,
        _isUpdatingDatabase,
        _selectedLaboratory,
        _selectedTissueSampleType,
        _selectedTissueTestType,
        _selectedSampleContainerType
    ) { animalInfoState, dataSaved, isUpdatingDatabase, laboratory, sampleType, testType, containerType ->
        animalInfoState is AnimalInfoState.Loaded && !dataSaved && !isUpdatingDatabase &&
                laboratory != null && sampleType != null &&
                testType != null && containerType != null
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    //These are the same for now, but this might change.
    val canPrintLabel = canSaveToDatabase

    private val _canScanTSU = MutableStateFlow(true)
    val canScanTSU = _canScanTSU.asStateFlow()

    init {
        viewModelScope.launch {
            loadDefaults()
        }
        savedStateHandle.get<AnimalBasicInfo>(TissueSample.EXTRA_ANIMAL_BASIC_INFO)?.let {
            animalInfoLookup.loadAnimalInfo(it)
        }
        viewModelScope.launch {
            animalInfoState.filter { it is AnimalInfoState.Loaded }
                .map { it.requireAs<AnimalInfoState.Loaded>().animalBasicInfo }
                .collectLatest {
                    //TODO: Find a better way to gate this check.
                    if (!savedStateHandle.contains(TissueSample.EXTRA_ANIMAL_BASIC_INFO)) {
                        if (it.speciesId != defaultSpecies.id) {
                            postEvent(
                                AnimalSpeciesMismatch(
                                    defaultSpeciesName = defaultSpecies.commonName,
                                    it.speciesCommonName
                                )
                            )
                        } else {
                            if (it.alerts.isNotEmpty()) {
                                postEvent(AnimalAlertEvent(it.alerts))
                            }
                        }
                    }
                }
        }
    }

    fun selectLaboratory(laboratory: Laboratory) {
        _selectedLaboratory.update { laboratory }
    }

    fun selectTissueSampleType(tissueSampleType: TissueSampleType) {
        _selectedTissueSampleType.update { tissueSampleType }
    }

    fun selectTissueTestType(tissueTestType: TissueTest) {
        _selectedTissueTestType.update { tissueTestType }
    }

    fun selectTissueSampleContainerType(tissueSampleContainerType: TissueSampleContainerType) {
        _selectedSampleContainerType.update { tissueSampleContainerType }
    }

    fun selectContainerExpirationDate(containerExpirationDate: LocalDate?) {
        _selectedSampleContainerExpirationDate.update { containerExpirationDate }
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

    fun printLabel() {
        if (canPrintLabel.value) {
            executePrintLabel()
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

    fun onBaaCodeScanned(baaCode: String) {
        containerId = baaCode
        postEvent(InputEvent.ContainerIdChanged)
    }

    override fun onCleared() {
        databaseHandler.close()
        super.onCleared()
    }

    private suspend fun loadDefaults() {
        val defaults = withContext(Dispatchers.IO) {
            loadActiveDefaultSettings()
        }
        secondaryIdTypeId = defaults.idTypeIdSecondary
        val defaultSpeciesDeferred = viewModelScope.async {
            speciesRepo.querySpeciesById(defaults.speciesId)
        }
        val laboratoryDeferred = viewModelScope.async {
            val labCompanyId = savedStateHandle[TissueSample.EXTRA_DEFAULT_LAB_COMPANY_ID]
                ?: defaults.labCompanyId
            labCompanyId?.let {
                laboratoryRepository.queryLaboratoryByCompanyId(it)
            }
        }
        val tissueSampleTypeDeferred = viewModelScope.async {
            defaults.tissueSampleTypeId?.let {
                tissueSampleTypeRepository.queryTissueSampleTypeById(it)
            }
        }
        val tissueTestTypeDeferred = viewModelScope.async {
            defaults.tissueTestId?.let {
                tissueTestRepository.queryTissueTestById(it)
            }
        }
        val tissueSampleContainerTypeDeferred = viewModelScope.async {
            tissueSampleContainerTypeRepository.queryTissueSampleContainerTypeById(defaults.tissueSampleContainerTypeId)
        }
        awaitAll(
            defaultSpeciesDeferred,
            laboratoryDeferred,
            tissueSampleTypeDeferred,
            tissueTestTypeDeferred,
            tissueSampleContainerTypeDeferred
        )
        defaultSpecies = requireNotNull(defaultSpeciesDeferred.await())
        _selectedLaboratory.update { laboratoryDeferred.await() }
        _selectedTissueSampleType.update { tissueSampleTypeDeferred.await() }
        _selectedTissueTestType.update { tissueTestTypeDeferred.await() }
        _selectedSampleContainerType.update { tissueSampleContainerTypeDeferred.await() }
    }

    private fun executeClearData() {
        containerId = ""
        _selectedSampleContainerExpirationDate.update { null }
        postEvents(
            InputEvent.ContainerIdChanged
        )
    }

    private suspend fun updateDatabase() {

        val animalBasicInfo = animalInfoState.value
            .takeAs<AnimalInfoState.Loaded>()?.animalBasicInfo ?: return

        val selectedLaboratoryId = _selectedLaboratory.value?.id
        val selectedSampleTypeId = _selectedTissueSampleType.value?.id
        val selectedTestTypeId = _selectedTissueTestType.value?.id
        val selectedContainerTypeId = _selectedSampleContainerType.value?.id

        val containerId = containerId.trim()
        val containerExpDate = selectedSampleContainerExpirationDate.value

        if (selectedLaboratoryId == null ||
            selectedSampleTypeId == null ||
            selectedTestTypeId == null ||
            selectedContainerTypeId == null
        ) {
            postEvent(IncompleteDataEntry)
            return
        }

        try {

            _isUpdatingDatabase.update { true }

            withContext(Dispatchers.IO) {
                animalRepo.addTissueTestForAnimal(
                    animalBasicInfo.id,
                    selectedSampleTypeId,
                    selectedContainerTypeId,
                    containerId,
                    containerExpDate,
                    selectedTestTypeId,
                    selectedLaboratoryId,
                    LocalDateTime.now()
                )
            }
            clearData()
            postEvent(UpdateDatabaseEvent.Success)
        } catch(ex: Exception) {
            errorReportChannel.send(
                ErrorReport(
                    action = "Take Tissue Sample",
                    summary = buildString {
                        append("animalId=${animalBasicInfo.id}, ")
                        append("selectedSampleTypeId=${selectedSampleTypeId}, ")
                        append("selectedContainerTypeId=${selectedContainerTypeId}, ")
                        append("containerId=${containerId}, ")
                        append("containerExpDate=${containerExpDate}, ")
                        append("selectedTestTypeId=${selectedTestTypeId}, ")
                        append("selectedLaboratoryId=${selectedLaboratoryId}")
                    },
                    error = ex
                )
            )
        } finally {
            _isUpdatingDatabase.update { false }
        }
    }

    private fun executePrintLabel() {

        val extraPrintLabelData = savedStateHandle.get<PrintLabelData>(
            TissueSample.EXTRA_PRINT_LABEL_DATA
        )

        if (extraPrintLabelData != null) {
            postEvent(PrintLabelRequestedEvent(extraPrintLabelData))
            return
        }

        val animalLoadedState = animalInfoState.value
            .takeAs<AnimalInfoState.Loaded>() ?: return

        val scannedEid = animalLoadedState.eidNumber
        val animalBasicInfo = animalLoadedState.animalBasicInfo

        val result = scannedEid?.let {
            extractPrintLabelData.forStandardLabel(it, secondaryIdTypeId, animalBasicInfo.ids)
        } ?: extractPrintLabelData.forStandardLabel(secondaryIdTypeId, animalBasicInfo.ids)

        postEvent(
            when (result) {
                is Result.Failure -> PrintLabelRequestedError(result.error)
                is Result.Success -> PrintLabelRequestedEvent(result.data)
            }
        )
    }

    private fun postEvent(event: Event) {
        viewModelScope.launch {
            _eventFlow.emit(event)
            _eventChannel.send(event)
        }
    }

    private fun postEvents(vararg events: Event) {
        viewModelScope.launch {
            events.forEach {
                _eventFlow.emit(it)
                _eventChannel.send(it)
            }
        }
    }
}
