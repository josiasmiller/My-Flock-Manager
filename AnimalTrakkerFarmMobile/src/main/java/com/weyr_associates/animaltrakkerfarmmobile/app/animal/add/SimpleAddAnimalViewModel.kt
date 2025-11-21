package com.weyr_associates.animaltrakkerfarmmobile.app.animal.add

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.add.SimpleAddAnimalViewModel.ValidationError.IdEntryRequired
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.add.SimpleAddAnimalViewModel.ValidationError.IncompleteAnimalEntry
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.add.SimpleAddAnimalViewModel.ValidationError.InvalidIdCombination
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.add.SimpleAddAnimalViewModel.ValidationError.InvalidIdNumberFormat
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.add.SimpleAddAnimalViewModel.ValidationError.PartialIdEntry
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.farm.BaseFarmTagOnEIDFeature
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.federal.SuggestDefaultScrapieFlockNumber
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.input.IdEntry
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.input.simple.IdEntryEditor
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.input.simple.IdEntryManager
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.trich.AutoIncrementNextTrichIdFeature
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.validation.EIDNumberAlreadyInUse
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.validation.IdInputCompleteness.COMPLETE
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.validation.IdInputCompleteness.PARTIAL
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.validation.IdValidations
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.validation.IdsValidationError
import com.weyr_associates.animaltrakkerfarmmobile.app.core.ErrorReport
import com.weyr_associates.animaltrakkerfarmmobile.app.core.Result
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.AnimalRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.BreedRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.IdColorRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.IdLocationRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.IdTypeRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.OwnerRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.PremiseRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.ScrapieFlockRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.SexRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.IdConfigs
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadDefaultIdConfigs
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseHandler
import com.weyr_associates.animaltrakkerfarmmobile.model.BirthType
import com.weyr_associates.animaltrakkerfarmmobile.model.Breed
import com.weyr_associates.animaltrakkerfarmmobile.model.BreedPart
import com.weyr_associates.animaltrakkerfarmmobile.model.Breeder
import com.weyr_associates.animaltrakkerfarmmobile.model.DefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.IdType
import com.weyr_associates.animaltrakkerfarmmobile.model.Owner
import com.weyr_associates.animaltrakkerfarmmobile.model.Premise
import com.weyr_associates.animaltrakkerfarmmobile.model.Sex
import com.weyr_associates.animaltrakkerfarmmobile.model.animalBirthDateFrom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime

class SimpleAddAnimalViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val databaseHandler: DatabaseHandler,
    private val loadActiveDefaultSettings: LoadActiveDefaultSettings,
    private val loadDefaultIdConfigs: LoadDefaultIdConfigs,
    private val animalRepo: AnimalRepository,
    private val breedRepo: BreedRepository,
    private val sexRepo: SexRepository,
    private val ownerRepo: OwnerRepository,
    private val premiseRepo: PremiseRepository,
    private val idTypeRepo: IdTypeRepository,
    private val idColorRepo: IdColorRepository,
    private val idLocationRepo: IdLocationRepository,
    private val idValidations: IdValidations,
    private val baseFarmTagOnEIDFeature: BaseFarmTagOnEIDFeature,
    private val autoUpdateTrichId: AutoIncrementNextTrichIdFeature,
    private val scrapieFlockRepository: ScrapieFlockRepository
) : ViewModel() {

    sealed interface Event

    sealed interface InputEvent : Event {
        data object AnimalNameChanged : InputEvent
    }

    sealed interface ValidationError : Event {
        data object IncompleteAnimalEntry : ValidationError
        data object IdEntryRequired : ValidationError
        data object PartialIdEntry : ValidationError

        data class ScannedEIDAlreadyUsed(
            val error: EIDNumberAlreadyInUse
        ) : ValidationError

        data class InvalidIdNumberFormat(
            val idEntry: IdEntry
        ) : ValidationError

        data class InvalidIdCombination(
            val error: IdsValidationError
        ) : ValidationError
    }

    sealed interface UpdateDatabaseEvent : Event {
        data class Success(val animalName: String, val animalId: EntityId) : UpdateDatabaseEvent
        data class Error(val animalName: String) : UpdateDatabaseEvent
    }

    private var defaultOwner: Owner? = null
    private var defaultIdConfigs: IdConfigs? = null

    private var breederId = EntityId.UNKNOWN
    private var breederTypeId = 0
    private var birthTypeId = EntityId.UNKNOWN
    private var rearTypeId: EntityId? = null
    private var ownerPremiseId = EntityId.UNKNOWN

    private val _animalName = MutableStateFlow("")
    var animalName: String
        get() = _animalName.value
        set(value) { _animalName.update{ value } }

    private val _selectedBreed = MutableStateFlow<Breed?>(null)
    val selectedBreed: StateFlow<Breed?> = _selectedBreed.asStateFlow()

    private val _selectedAgeYears = MutableStateFlow(0)
    val selectedAgeYears = _selectedAgeYears.asStateFlow()

    private val _selectedAgeMonths = MutableStateFlow(0)
    val selectedAgeMonths = _selectedAgeMonths.asStateFlow()

    private val _selectedSex = MutableStateFlow<Sex?>(null)
    val selectedSex = _selectedSex.asStateFlow()

    private val _selectedOwner = MutableStateFlow<Owner?>(null)
    val selectedOwner = _selectedOwner.asStateFlow()

    private val _speciesId = MutableStateFlow(DefaultSettings.SPECIES_ID_DEFAULT)
    val speciesId = _speciesId.asStateFlow()

    private val idEntryManager = IdEntryManager(
        viewModelScope,
        idTypeRepo,
        idColorRepo,
        idLocationRepo,
        loadActiveDefaultSettings,
        baseFarmTagOnEIDFeature,
        SuggestDefaultScrapieFlockNumber(
            loadActiveDefaultSettings,
            scrapieFlockRepository,
        ),
        autoUpdateTrichId
    )
    val idEntryEditor: IdEntryEditor = object : IdEntryEditor by idEntryManager {}

    val canClearData = combine(
        _animalName,
        idEntryManager.canReset
    ) { animalName, canResetIds ->
        animalName.isNotBlank() || canResetIds
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    private val _isUpdatingDatabase = MutableStateFlow(false)

    val canSaveToDatabase = combine(
        _selectedOwner,
        _selectedBreed,
        _selectedSex,
        _isUpdatingDatabase
    ) { selectedOwner, selectedBreed, selectedSex, isUpdatingDatabase ->
        selectedOwner != null && selectedBreed != null && selectedSex != null && !isUpdatingDatabase
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    private val _eventChannel = Channel<Event>()
    val events = _eventChannel.receiveAsFlow()

    private val errorReportChannel = Channel<ErrorReport>()
    val errorReportFlow = errorReportChannel.receiveAsFlow()

    init {
        idEntryManager.idNumber1 = savedStateHandle[AddAnimal.EXTRA_PRIMARY_ID_NUMBER] ?: ""
        viewModelScope.launch {
            loadDefaults()
        }
    }

    fun selectBreed(breed: Breed) {
        _selectedBreed.update { breed }
    }

    fun selectAgeYears(years: Int) {
        _selectedAgeYears.update { years }
    }

    fun selectAgeMonths(months: Int) {
        _selectedAgeMonths.update { months }
    }

    fun selectOwner(owner: Owner) {
        _selectedOwner.update { owner }
    }

    fun selectSex(sex: Sex) {
        _selectedSex.update { sex }
    }

    fun onEIDScanned(eidString: String) {
        viewModelScope.launch {
            val eidExists = idValidations.checkEIDsNotDuplicated.isEIDInUse(eidString)
            if (eidExists is Result.Failure<Unit, EIDNumberAlreadyInUse>) {
                _eventChannel.send(ValidationError.ScannedEIDAlreadyUsed(eidExists.error))
            } else {
                idEntryManager.onEIDScanned(eidString)
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

    override fun onCleared() {
        databaseHandler.close()
        super.onCleared()
    }

    private suspend fun loadDefaults() {

        val defaults = withContext(Dispatchers.IO) {
            loadActiveDefaultSettings()
        }

        breederId = defaults.breederId ?: EntityId.UNKNOWN
        breederTypeId = defaults.breederType ?: Breeder.Type.CONTACT.code
        birthTypeId = defaults.birthTypeId
        rearTypeId = defaults.rearTypeId
        ownerPremiseId = defaults.ownerPremiseId

        autoUpdateTrichId.configureFromSettings(defaults)

        val eidIdTypeDeferred = viewModelScope.async {
            idTypeRepo.queryForIdType(IdType.ID_TYPE_ID_EID)
        }
        val breedDeferred = viewModelScope.async {
            breedRepo.queryBreed(defaults.breedId)
        }
        val ownerDeferred = viewModelScope.async {
            val ownerId = defaults.ownerId
            val ownerType = defaults.ownerType
            when {
                ownerId != null && ownerType != null -> {
                    ownerRepo.queryOwner(ownerId, ownerType)
                }
                else -> null
            }
        }
        val sexDeferred = viewModelScope.async {
            sexRepo.querySexById(defaults.sexId)
        }

        val idType1Deferred = viewModelScope.async {
            idTypeRepo.queryForIdType(
                savedStateHandle[AddAnimal.EXTRA_PRIMARY_ID_TYPE_ID]
                    ?: defaults.idTypeIdPrimary
            )
        }

        val idConfigsDeferred = viewModelScope.async {
            loadDefaultIdConfigs()
        }

        awaitAll(
            eidIdTypeDeferred,
            breedDeferred,
            ownerDeferred,
            sexDeferred,
            idType1Deferred,
            idConfigsDeferred
        )

        _selectedBreed.update { breedDeferred.await() }
        _selectedSex.update { sexDeferred.await() }
        _selectedOwner.update {
            ownerDeferred.await().also { defaultOwner = it }
        }

        defaultIdConfigs = idConfigsDeferred.await().let {
            it.copy(
                primary = it.primary.copy(
                    idType = requireNotNull(idType1Deferred.await())
                )
            )
        }.also { idEntryManager.applyConfigs(it) }
    }

    private fun executeClearData() {
        animalName = ""
        postEvents(InputEvent.AnimalNameChanged)
        idEntryManager.reset()
    }

    private suspend fun updateDatabase() {
        try {

            _isUpdatingDatabase.update { true }

            val selectedOwnerId = selectedOwner.value?.id
            val selectedOwnerTypeId = selectedOwner.value?.type
            val selectedBreedId = selectedBreed.value?.id
            val selectedSexId = selectedSex.value?.id

            if (selectedOwnerId == null ||
                selectedOwnerTypeId == null ||
                selectedBreedId == null ||
                selectedSexId == null
            ) {
                postEvent(IncompleteAnimalEntry)
                return
            }

            //Capture ID input values for each ID.

            val idInputs = idEntryManager.captureIdInputs()
            val idInput1 = idInputs.idInput1
            val idInput2 = idInputs.idInput2
            val idInput3 = idInputs.idInput3

            //Check to make sure that ID 1 is completely entered, and that ID 2 and ID 3
            //are either completely entered or left completely empty.

            val idInput1Completeness = idValidations.checkIdInputCompleteness(idInput1)
            val idInput2Completeness = idValidations.checkIdInputCompleteness(idInput2)
            val idInput3Completeness = idValidations.checkIdInputCompleteness(idInput3)

            if (COMPLETE != idInput1Completeness) {
                postEvent(IdEntryRequired)
                return
            }

            if (PARTIAL == idInput2Completeness) {
                postEvent(PartialIdEntry)
                return
            }

            if (PARTIAL == idInput3Completeness) {
                postEvent(PartialIdEntry)
                return
            }

            //Capture ID entry values as non-optional
            //in the case where complete entries have been made.

            var idEntry1 = idInput1.toEntry()
            var idEntry2 = idInput2.toOptEntry()
            var idEntry3 = idInput3.toOptEntry()

            //Check to make sure the ID number entries for each ID are
            //properly formatted for their associated ID Type.

            val idFormatCheck1 = idValidations.checkIdNumberFormat(idEntry1)
            val idFormatCheck2 = idEntry2?.let { idValidations.checkIdNumberFormat(it) }
            val idFormatCheck3 = idEntry3?.let { idValidations.checkIdNumberFormat(it) }

            listOf(idFormatCheck1, idFormatCheck2, idFormatCheck3).forEach { formatCheck ->
                if (formatCheck is Result.Failure) {
                    postEvent(InvalidIdNumberFormat(formatCheck.error.idEntry))
                    return
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
                postEvent(InvalidIdCombination(idEntryDuplicateEIDCheck.error))
                return
            }

            //Check to see if the combination of IDs violates the rules for which
            //and how many of each ID type can be on an animal at the same time.

            val idEntryComboCheck = idValidations.checkIdCombinationValidity
                .whenAddingAnimal(idEntries)

            if (idEntryComboCheck is Result.Failure) {
                postEvent(InvalidIdCombination(idEntryComboCheck.error))
                return
            }

            //Prepare data and some defaults before adding animal and ID entries to the database.

            //Capturing official ID number in lieu of a an entered name
            //requires that validation guarantees at least one official ID
            //above.
            val animalName = animalName.takeIf { it.isNotBlank() }
                ?: idEntries.first { it.isOfficial }.number

            val todayDateTime = LocalDateTime.now()

            val animalAgeYears = selectedAgeYears.value
            val animalAgeMonths = selectedAgeMonths.value
            val animalBirthDate = todayDateTime.animalBirthDateFrom(animalAgeYears, animalAgeMonths)

            //Look up the owner premise as the first physical or "both" premise for the selected owner
            //if it is not the default owner, otherwise use the default ownerPremiseId from active defaults.
            val ownerPremiseId = ownerPremiseId.takeIf { it.isValid && selectedOwnerId == defaultOwner?.id }
                ?: premiseRepo.queryPhysicalPremiseForOwner(selectedOwnerId, selectedOwnerTypeId)?.id
                ?: Premise.ID_PREMISE_UNKNOWN

            val birthTypeId = birthTypeId.takeIf { it.isValid } ?: BirthType.ID_UNKNOWN

            //Setup a transaction and add the animal and its IDs
            val animalId: EntityId = saveAnimalAndIdsToDatabase(
                animalName,
                selectedBreedId,
                selectedSexId,
                animalBirthDate,
                birthTypeId,
                rearTypeId,
                selectedOwnerId,
                selectedOwnerTypeId,
                ownerPremiseId,
                breederId,
                breederTypeId,
                idEntries,
                todayDateTime
            )

            val succeeded = animalId.isValid

            postEvent(
                if (succeeded) UpdateDatabaseEvent.Success(animalName, animalId)
                else UpdateDatabaseEvent.Error(animalName)
            )

            if (succeeded) {
                executeClearData()
            }

        } finally {
            _isUpdatingDatabase.update { false }
        }
    }

    private suspend fun saveAnimalAndIdsToDatabase(
        animalName: String,
        breedId: EntityId,
        sexId: EntityId,
        birthDate: LocalDate,
        birthTypeId: EntityId,
        rearTypeId: EntityId?,
        ownerId: EntityId,
        ownerType: Owner.Type,
        ownerPremiseId: EntityId,
        breederId: EntityId,
        breederTypeCode: Int,
        idEntries: List<IdEntry>,
        timeStamp: LocalDateTime
    ): EntityId {
        //Run the whole transaction from dispatch to IO thread to prevent
        // layered transactions from blocking each other.
        return withContext(Dispatchers.IO) {
            databaseHandler.writableDatabase.beginTransaction()
            try {
                //Add the animal
                val animalId = animalRepo.addAnimal(
                    animalName,
                    //TODO (Post-Wool-Growers) this is currently a single breed.
                    // Structure in database in place to handle breed percentages but not implemented yet
                    listOf(BreedPart(breedId, 100.0f)),
                    sexId,
                    null,
                    null,
                    birthDate,
                    null,
                    null,
                    birthTypeId,
                    rearTypeId,
                    ownerId,
                    ownerType,
                    ownerPremiseId,
                    breederId,
                    breederTypeCode,
                    null,
                    null,
                    timeStamp
                )
                //Add its IDs
                idEntries.forEach { idEntry ->
                    animalRepo.addIdToAnimal(
                        animalId,
                        idEntry.type.id,
                        idEntry.color.id,
                        idEntry.location.id,
                        idEntry.number,
                        idEntry.isOfficial,
                        timeStamp
                    )
                    //Update next trich number if any IDs require it
                    autoUpdateTrichId.autoIncrementIfRequired(idEntry)
                }
                databaseHandler.writableDatabase.setTransactionSuccessful()
                animalId
            } catch (ex: Exception) {
                postErrorReport(
                    ErrorReport(
                        action = "Simple Add Animal",
                        summary = buildString {
                            append("animalName=${animalName}, ")
                            append("breedPercentage=[${breedId}: 100%], ")
                            append("sexId=${sexId}, ")
                            append("damId=null, ")
                            append("sireId=null, ")
                            append("birthDate=${birthDate}, ")
                            append("birthTime=null, ")
                            append("birthOrder=null, ")
                            append("birthTypeId=${birthTypeId}, ")
                            append("rearTypeId=${rearTypeId}, ")
                            append("ownerId=${ownerId}, ")
                            append("ownerType=${ownerType}, ")
                            append("ownerPremiseId=${ownerPremiseId}, ")
                            append("breederId=${breederId}, ")
                            append("breederTypeCode=${breederTypeCode}, ")
                            append("weight=null, ")
                            append("weightUnitsId=null")
                        },
                        error = ex
                    )
                )
                EntityId.UNKNOWN //TODO: UUID-CONVERSION, discuss this use of unknown
            } finally {
                databaseHandler.writableDatabase.endTransaction()
            }
        }
    }

    private fun postEvent(event: Event) {
        viewModelScope.launch {
            _eventChannel.send(event)
        }
    }

    private fun postEvents(vararg events: Event) {
        viewModelScope.launch {
            events.forEach { _eventChannel.send(it) }
        }
    }

    private suspend fun postErrorReport(errorReport: ErrorReport) {
        errorReportChannel.send(errorReport)
    }
}
