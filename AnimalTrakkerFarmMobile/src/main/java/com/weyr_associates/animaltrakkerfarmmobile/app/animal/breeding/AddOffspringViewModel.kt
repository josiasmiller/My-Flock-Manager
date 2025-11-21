package com.weyr_associates.animaltrakkerfarmmobile.app.animal.breeding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.breeding.AddOffspringViewModel.ValidationError.IdEntryRequired
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.breeding.AddOffspringViewModel.ValidationError.InvalidIdCombination
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.breeding.AddOffspringViewModel.ValidationError.InvalidIdNumberFormat
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.breeding.AddOffspringViewModel.ValidationError.PartialIdEntry
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.farm.BaseFarmTagOnEIDFeature
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.federal.SuggestAnimalOwnerScrapieFlockNumber
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.input.IdEntry
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.input.simple.IdEntryEditor
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.input.simple.IdEntryManager
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.input.simple.IdInputs
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.trich.AutoIncrementNextTrichIdFeature
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.validation.EIDNumberAlreadyInUse
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.validation.IdInputCompleteness.COMPLETE
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.validation.IdInputCompleteness.PARTIAL
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.validation.IdValidations
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.validation.IdsValidationError
import com.weyr_associates.animaltrakkerfarmmobile.app.core.ErrorReport
import com.weyr_associates.animaltrakkerfarmmobile.app.core.Result
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.coroutines.awaitAll
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.coroutines.flow.combine7
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.coroutines.flow.combine9
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.AnimalRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.EvaluationRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.IdColorRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.IdLocationRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.IdTypeRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.ScrapieFlockRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.IdConfigs
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadDefaultIdConfigs
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadDefaultWeightUnits
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalName
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.EvalTrait
import com.weyr_associates.animaltrakkerfarmmobile.model.EvalTraitOption
import com.weyr_associates.animaltrakkerfarmmobile.model.GeneticCoatColor
import com.weyr_associates.animaltrakkerfarmmobile.model.IdType
import com.weyr_associates.animaltrakkerfarmmobile.model.RearType
import com.weyr_associates.animaltrakkerfarmmobile.model.ServiceType
import com.weyr_associates.animaltrakkerfarmmobile.model.Sex
import com.weyr_associates.animaltrakkerfarmmobile.model.Species
import com.weyr_associates.animaltrakkerfarmmobile.model.UnitOfMeasure
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class AddOffspringViewModel(
    private val damAnimalId: EntityId,
    private val animalRepository: AnimalRepository,
    private val evaluationRepository: EvaluationRepository,
    private val scrapieFlockRepository: ScrapieFlockRepository,
    private val loadActiveDefaultSettings: LoadActiveDefaultSettings,
    private val loadDefaultWeightUnits: LoadDefaultWeightUnits,
    private val loadDefaultIdConfigs: LoadDefaultIdConfigs,
    private val idTypeRepository: IdTypeRepository,
    private val idColorRepository: IdColorRepository,
    private val idLocationRepository: IdLocationRepository,
    private val idValidations: IdValidations,
    private val baseFarmTagOnEIDFeature: BaseFarmTagOnEIDFeature,
    private val autoUpdateTrichId: AutoIncrementNextTrichIdFeature,
    private val addOffspring: AddOffspring
) : ViewModel() {

    sealed interface Event

    data class ScannedEIDAlreadyUsed(
        val error: EIDNumberAlreadyInUse
    ) : Event

    sealed interface ValidationError : Event {

        data object IdEntryRequired : ValidationError
        data object PartialIdEntry : ValidationError

        data class InvalidIdNumberFormat(
            val idEntry: IdEntry
        ) : ValidationError

        data class InvalidIdCombination(
            val error: IdsValidationError
        ) : ValidationError
    }

    data object UpdateDatabaseSuccess : Event

    sealed interface UpdateDatabaseFailure : Event

    data class UpdateDatabaseError(
        val addOffspringError: AddOffspringError
    ) : UpdateDatabaseFailure

    private var defaultIdConfigs: IdConfigs? = null
    private val idEntryManager = IdEntryManager(
        viewModelScope,
        idTypeRepository,
        idColorRepository,
        idLocationRepository,
        loadActiveDefaultSettings,
        baseFarmTagOnEIDFeature,
        SuggestAnimalOwnerScrapieFlockNumber(
            damAnimalId,
            animalRepository,
            scrapieFlockRepository
        ),
        autoUpdateTrichId
    )
    val idEntryEditor: IdEntryEditor = object : IdEntryEditor by idEntryManager {}

    private val _sireAnimalName = MutableStateFlow<AnimalName?>(null)
    val sireAnimalName = _sireAnimalName.asStateFlow()

    private val _damAnimalName = MutableStateFlow<AnimalName?>(null)
    val damAnimalName = _damAnimalName.asStateFlow()

    private val _selectedServiceType = MutableStateFlow<ServiceType?>(null)
    val selectedServiceType = _selectedServiceType.asStateFlow()

    private val _selectedRearType = MutableStateFlow<RearType?>(null)
    val selectedRearType = _selectedRearType.asStateFlow()

    private val _selectedSex = MutableStateFlow<Sex?>(null)
    val selectedSex = _selectedSex.asStateFlow()

    private var defaultLambEase: EvalTraitOption? = null
    private val _selectedLambEase = MutableStateFlow<EvalTraitOption?>(null)
    val selectedLambEase = _selectedLambEase.asStateFlow()

    private var defaultSuckReflex: EvalTraitOption? = null
    private val _selectedSuckReflex = MutableStateFlow<EvalTraitOption?>(null)
    val selectedSuckReflex = _selectedSuckReflex.asStateFlow()

    private val _selectedCoatColor = MutableStateFlow<GeneticCoatColor?>(null)
    val selectedCoatColor = _selectedCoatColor.asStateFlow()

    private val _currentWeight = MutableStateFlow<Float?>(null)
    val currentWeight = _currentWeight.asStateFlow()

    private val _weightUnits = MutableStateFlow<UnitOfMeasure?>(null)
    val weightUnits = _weightUnits.asStateFlow()

    private val _isStillborn = MutableStateFlow(false)
    val isStillBorn = _isStillborn.asStateFlow()

    private val _shouldMarkDam = MutableStateFlow(false)
    val shouldMarkDam = _shouldMarkDam.asStateFlow()

    val canMarkDam = combine(
        idEntryManager.selectedIdType1,
        idEntryManager.selectedIdType2,
        idEntryManager.selectedIdType3
    ) { idType1, idType2, idType3 ->
        idType1?.id == IdType.ID_TYPE_ID_PAINT ||
        idType2?.id == IdType.ID_TYPE_ID_PAINT ||
        idType3?.id == IdType.ID_TYPE_ID_PAINT
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    val canClearData = combine9(
        selectedRearType, selectedSex,
        selectedLambEase, selectedSuckReflex, selectedCoatColor,
        currentWeight, isStillBorn, shouldMarkDam, idEntryManager.canReset
    ) { rearType, sex, lambEase, suckReflex,
        coatColor, weight, isStillBorn, shouldMarkDam, canResetIds ->
        rearType != null || sex != null ||
                (lambEase != null && lambEase != defaultLambEase) ||
                (suckReflex != null && suckReflex != defaultSuckReflex) ||
                coatColor != null || weight != null ||
                isStillBorn || shouldMarkDam || canResetIds
    }

    private val _isUpdatingDatabase = MutableStateFlow(false)

    val canUpdateDatabase = combine7(
        _isUpdatingDatabase, selectedServiceType,
        selectedRearType, selectedSex, selectedLambEase,
        selectedSuckReflex, selectedCoatColor
    ) { isUpdatingDb, serviceType, rearType, sex, lambEase, suckReflex, coatColor ->
        !isUpdatingDb && serviceType != null && rearType != null && sex != null &&
        lambEase != null && suckReflex != null && coatColor != null
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    private val _eventsChannel = Channel<Event>()
    val events = _eventsChannel.receiveAsFlow()

    private val _errorReportChannel = Channel<ErrorReport>()
    val errorReportFlow = _errorReportChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            val sireInfoDeferred = async {
                animalRepository.querySireForOffspringFrom(damAnimalId)
            }
            val damNameDeferred = async {
                animalRepository.queryAnimalName(damAnimalId)
            }
            val defaultLambEaseDeferred = async {
                evaluationRepository.queryEvalTraitOptionById(
                    EvalTraitOption.ID_LAMB_EASE_UNASSISTED
                )
            }
            val defaultSuckReflexDeferred = async {
                evaluationRepository.queryEvalTraitOptionById(
                    EvalTraitOption.ID_SUCK_REFLEX_UNTESTED
                )
            }
            val weightUnitsDeferred = async {
                withContext(Dispatchers.IO) {
                    loadDefaultWeightUnits()
                }
            }
            val defaultIdConfigsDeferred = async {
                withContext(Dispatchers.IO) {
                    loadDefaultIdConfigs()
                }
            }
            awaitAll(
                sireInfoDeferred,
                damNameDeferred,
                weightUnitsDeferred,
                defaultLambEaseDeferred,
                defaultSuckReflexDeferred,
                defaultIdConfigsDeferred
            ) { sireInfo, damName, weightUnits, lambEase, suckReflex, idConfigs ->
                _sireAnimalName.update { sireInfo.sireName }
                _damAnimalName.update { damName }
                _weightUnits.update { weightUnits }
                _selectedServiceType.update { sireInfo.serviceType }
                defaultLambEase = lambEase
                _selectedLambEase.update { defaultLambEase }
                defaultSuckReflex = suckReflex
                _selectedSuckReflex.update { defaultSuckReflex }
                defaultIdConfigs = idConfigs
                defaultIdConfigs?.let { idEntryManager.applyConfigs(it) }
            }
        }
        viewModelScope.launch {
            isStillBorn.collectLatest { isStillborn ->
                with(idEntryManager) {
                    if (isStillborn) {
                        clear()
                    } else {
                        defaultIdConfigs?.let {
                            applyConfigs(it)
                        }
                    }
                    setIsEditable(!isStillborn)
                }
            }
        }
        viewModelScope.launch {
            canMarkDam.collectLatest { canMark ->
                if (!canMark) {
                    _shouldMarkDam.update { false }
                }
            }
        }
    }

    fun selectRearType(rearType: RearType) {
        _selectedRearType.update { rearType }
    }

    fun selectSex(sex: Sex) {
        if (sex.species.id == Species.ID_SHEEP) {
            _selectedSex.update { sex }
        }
    }

    fun selectLambEase(lambEase: EvalTraitOption) {
        if (lambEase.traitId == EvalTrait.TRAIT_ID_LAMB_EASE) {
            _selectedLambEase.update { lambEase }
        }
    }

    fun selectSuckReflex(suckReflex: EvalTraitOption) {
        if (suckReflex.traitId == EvalTrait.TRAIT_ID_SUCK_REFLEX) {
            _selectedSuckReflex.update { suckReflex }
        }
    }

    fun selectCoatColor(coatColor: GeneticCoatColor) {
        _selectedCoatColor.update { coatColor }
    }

    fun setWeight(weight: Float?) {
        _currentWeight.update { weight }
    }

    fun setStillBorn(stillBorn: Boolean) {
        _isStillborn.update { stillBorn }
    }

    fun setDamMarked(damMarked: Boolean) {
        _shouldMarkDam.update { damMarked }
    }

    fun onEIDScanned(eidString: String) {
        viewModelScope.launch {
            val eidExists = idValidations.checkEIDsNotDuplicated.isEIDInUse(eidString)
            if (eidExists is Result.Failure<Unit, EIDNumberAlreadyInUse>) {
                postEvent(ScannedEIDAlreadyUsed(eidExists.error))
            } else {
               idEntryManager.onEIDScanned(eidString)
            }
        }
    }

    fun clearData() {
        _selectedRearType.update { null }
        _selectedSex.update { null }
        _selectedLambEase.update { defaultLambEase }
        _selectedSuckReflex.update { defaultSuckReflex }
        _selectedCoatColor.update { null }
        _currentWeight.update { null }
        _isStillborn.update { false }
        _shouldMarkDam.update { false }
        idEntryManager.reset()
    }

    fun updateDatabase() {
        if (!_isUpdatingDatabase.value) {
            viewModelScope.launch {
                executeUpdateDatabase()
            }
        }
    }

    private suspend fun executeUpdateDatabase() {
        val sireId = sireAnimalName.value?.id ?: return
        val sexId = selectedSex.value?.id ?: return
        val sexAbbr = selectedSex.value?.abbreviation ?: return
        val rearTypeId = selectedRearType.value?.id ?: return
        val birthWeight = currentWeight.value
        val birthWeightUnitsId = weightUnits.value?.id
        val lambEaseId = selectedLambEase.value?.id ?: return
        val suckReflexId = selectedSuckReflex.value?.id ?: return
        val coatColorId = selectedCoatColor.value?.id ?: return
        val isStillborn = isStillBorn.value
        val shouldMarkDam = shouldMarkDam.value

        val idInputs = idEntryManager.captureIdInputs()

        val idEntries = when (val idValidationResult = validateIdInputs(idInputs, isStillborn)) {
            is Result.Failure -> {
                postEvent(idValidationResult.error)
                return
            }
            is Result.Success -> {
                idValidationResult.data
            }
        }

        _isUpdatingDatabase.update { true }

        try {
            val result = withContext(Dispatchers.IO) {
                addOffspring(
                    damAnimalId,
                    sireId,
                    sexId,
                    sexAbbr,
                    rearTypeId,
                    birthWeight,
                    birthWeightUnitsId,
                    lambEaseId,
                    suckReflexId,
                    coatColorId,
                    isStillborn,
                    shouldMarkDam,
                    idEntries,
                    LocalDateTime.now()
                )
            }
            if (result is Result.Failure) {
                postEvent(UpdateDatabaseError(result.error))
            } else {
                clearData()
                postEvent(UpdateDatabaseSuccess)
            }
        } catch (ex: Exception) {
            postErrorReport(
                ErrorReport(
                    action = "Add Offspring",
                    summary = "ViewModel Update Database",
                    error = ex
                )
            )
        }
        finally {
            _isUpdatingDatabase.update { false }
        }
    }

    private suspend fun validateIdInputs(idInputs: IdInputs, isStillborn: Boolean): Result<List<IdEntry>, ValidationError> {

        // If the lamb is stillborn, pass validations, but don't return any id entries.
        // Ignore the inputs and return empty list.

        if (isStillborn) {
            return Result.Success(emptyList())
        }

        //Check to make sure that ID 1 is completely entered, and that ID 2 and ID 3
        //are either completely entered or left completely empty.

        val idInput1Completeness = idValidations.checkIdInputCompleteness(idInputs.idInput1)
        val idInput2Completeness = idValidations.checkIdInputCompleteness(idInputs.idInput2)
        val idInput3Completeness = idValidations.checkIdInputCompleteness(idInputs.idInput3)

        if (COMPLETE != idInput1Completeness) {
            return Result.Failure(IdEntryRequired)
        }

        if (PARTIAL == idInput2Completeness) {
            return Result.Failure(PartialIdEntry)
        }

        if (PARTIAL == idInput3Completeness) {
            return Result.Failure(PartialIdEntry)
        }

        //Capture ID entry values as non-optional
        //in the case where complete entries have been made.

        var idEntry1 = idInputs.idInput1.toEntry()
        var idEntry2 = idInputs.idInput2.toOptEntry()
        var idEntry3 = idInputs.idInput3.toOptEntry()

        //Check to make sure the ID number entries for each ID are
        //properly formatted for their associated ID Type.

        val idFormatCheck1 = idValidations.checkIdNumberFormat(idEntry1)
        val idFormatCheck2 = idEntry2?.let { idValidations.checkIdNumberFormat(it) }
        val idFormatCheck3 = idEntry3?.let { idValidations.checkIdNumberFormat(it) }

        listOf(idFormatCheck1, idFormatCheck2, idFormatCheck3).forEach { formatCheck ->
            if (formatCheck is Result.Failure) {
                return Result.Failure(InvalidIdNumberFormat(formatCheck.error.idEntry))
            }
        }

        //Determine whether or not each ID entry should be considered an official ID.

        idEntry1 = idEntry1.copy(isOfficial = idValidations.checkIdEntryIsOfficial(idEntry1))
        idEntry2 = idEntry2?.let { it.copy(isOfficial = idValidations.checkIdEntryIsOfficial(it)) }
        idEntry3 = idEntry3?.let { it.copy(isOfficial = idValidations.checkIdEntryIsOfficial(it)) }

        //Create list of actual id entries that will be added to the animal

        val idEntries = buildList {
            add(idEntry1)
            idEntry2?.let { add(it) }
            idEntry3?.let { add(it) }
        }

        //Check to see if any EID entries duplicate EIDs amongst themselves
        //or against any existing EIDs in the database.

        val idEntryDuplicateEIDCheck = idValidations.checkEIDsNotDuplicated
            .withAdditionOf(idEntries)

        if (idEntryDuplicateEIDCheck is Result.Failure) {
            return Result.Failure(InvalidIdCombination(idEntryDuplicateEIDCheck.error))
        }

        //Check to see if the combination of IDs violates the rules for which
        //and how many of each ID type can be on an animal at the same time.

        val idEntryComboCheck = idValidations.checkIdCombinationValidity
            .whenAddingOffspring(idEntries)

        if (idEntryComboCheck is Result.Failure) {
            return Result.Failure(InvalidIdCombination(idEntryComboCheck.error))
        }

        return Result.Success(idEntries)
    }

    private fun postEvent(event: Event) {
        viewModelScope.launch {
            _eventsChannel.send(event)
        }
    }

    private fun postErrorReport(errorReport: ErrorReport) {
        viewModelScope.launch {
            _errorReportChannel.send(errorReport)
        }
    }
}
