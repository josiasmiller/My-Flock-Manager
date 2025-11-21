package com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.manage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.input.IdEntry
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.input.IdInput
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.input.asIdEntry
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.manage.ManageAnimalIdsViewModel.ValidationError.InvalidIdCombination
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.manage.ManageAnimalIdsViewModel.ValidationError.InvalidIdNumberFormat
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.manage.ManageAnimalIdsViewModel.ValidationError.ScannedEIDAlreadyUsed
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.trich.AutoIncrementNextTrichIdFeature
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.validation.EIDNumberAlreadyInUse
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.validation.IdInputCompleteness
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.validation.IdValidations
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.validation.IdsValidationError
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.lookup.AnimalInfoLookup
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.lookup.LookupAnimalInfo
import com.weyr_associates.animaltrakkerfarmmobile.app.core.ErrorReport
import com.weyr_associates.animaltrakkerfarmmobile.app.core.Result
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.takeAs
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.AnimalRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.IdTypeRepository
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalAlert
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.IdColor
import com.weyr_associates.animaltrakkerfarmmobile.model.IdLocation
import com.weyr_associates.animaltrakkerfarmmobile.model.IdType
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class ManageAnimalIdsViewModel(
    private val animalRepo: AnimalRepository,
    private val idTypeRepo: IdTypeRepository,
    private val idValidations: IdValidations,
    private val autoUpdateTrichId: AutoIncrementNextTrichIdFeature
) : ViewModel(), LookupAnimalInfo {

    private val animalInfoLookup = AnimalInfoLookup(viewModelScope, animalRepo)

    sealed interface Event

    data class AnimalAlertEvent(
        val alerts: List<AnimalAlert>
    ) : Event

    data class PromptForEIDUsage(val eidNumber: String) : Event

    data object IdNumberChanged : Event
    data object IdAdditionFailed : Event
    data object IdRemovalFailed : Event

    sealed interface ValidationError : Event {
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

    private val _idNumber = MutableStateFlow("")

    var idNumber: String
        get() = _idNumber.value
        set(value) {
            _idNumber.update { value }
        }

    private val _selectedIdType = MutableStateFlow<IdType?>(null)
    val selectedIdType = _selectedIdType.asStateFlow()

    private val _selectIdLocation = MutableStateFlow<IdLocation?>(null)
    val selectedIdLocation = _selectIdLocation.asStateFlow()

    private val _selectedIdColor = MutableStateFlow<IdColor?>(null)
    val selectedIdColor = _selectedIdColor.asStateFlow()

    override val animalInfoState: StateFlow<LookupAnimalInfo.AnimalInfoState> = animalInfoLookup.animalInfoState

    val canClearData: StateFlow<Boolean> = combine(
        _idNumber,
        _selectedIdType,
        _selectedIdColor,
        _selectIdLocation
    ) { idNumber, idType, idColor, idLocation ->
        idNumber.isNotEmpty() || idType != null || idColor != null || idLocation != null
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    val canAddId: StateFlow<Boolean> = combine(
        animalInfoState,
        _idNumber,
        _selectedIdType,
        _selectedIdColor,
        _selectIdLocation
    ) { animalInfoState, idNumber, idType, idColor, idLocation ->
        animalInfoState is LookupAnimalInfo.AnimalInfoState.Loaded &&
                idValidations.checkIdInputCompleteness(
                    IdInput(idNumber, idType, idColor, idLocation)
                ) == IdInputCompleteness.COMPLETE
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    private val _eventChannel = Channel<Event>()
    val events = _eventChannel.receiveAsFlow()

    private val errorReportChannel = Channel<ErrorReport>()
    val errorReportFlow = errorReportChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            autoUpdateTrichId.configureFromSettings()
        }
        viewModelScope.launch {
            animalInfoLookup.animalInfoState.filterIsInstance<LookupAnimalInfo.AnimalInfoState.Loaded>()
                .map { it.animalBasicInfo }
                .collectLatest {
                    if (it.alerts.isNotEmpty()) {
                        _eventChannel.send(AnimalAlertEvent(it.alerts))
                    }
                }
        }
    }

    fun selectIdType(idType: IdType) {
        _selectedIdType.update { idType }
        idNumber = ""
        if (autoUpdateTrichId.shouldAutoPopulateTrichNumber(idType)) {
            idNumber = autoUpdateTrichId.nextTrichNumber.toString()
        }
        viewModelScope.launch { _eventChannel.send(IdNumberChanged) }
    }

    fun selectIdLocation(idLocation: IdLocation) {
        _selectIdLocation.update { idLocation }
    }

    fun selectIdColor(idColor: IdColor) {
        _selectedIdColor.update { idColor }
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

    fun onEIDScanned(eidNumber: String) {
        viewModelScope.launch {
            if (animalInfoState.value is LookupAnimalInfo.AnimalInfoState.Loaded) {
                _eventChannel.send(PromptForEIDUsage(eidNumber))
            } else {
                animalInfoLookup.lookupAnimalInfoByEIDNumber(eidNumber)
            }
        }
    }

    fun setIdNumberFromEIDScan(eidNumber: String) {
        if (animalInfoState.value is LookupAnimalInfo.AnimalInfoState.Loaded) {
            viewModelScope.launch {
                val eidExists = idValidations.checkEIDsNotDuplicated.isEIDInUse(eidNumber)
                if (eidExists is Result.Failure<Unit, EIDNumberAlreadyInUse>) {
                    _eventChannel.send(ScannedEIDAlreadyUsed(eidExists.error))
                } else {
                    selectIdType(requireNotNull(idTypeRepo.queryForIdType(IdType.ID_TYPE_ID_EID)))
                    idNumber = eidNumber
                    _eventChannel.send(IdNumberChanged)
                }
            }
        }
    }

    fun clearData() {
        idNumber = ""
        _selectedIdType.update { null }
        _selectedIdColor.update { null }
        _selectIdLocation.update { null }
        viewModelScope.launch {
            _eventChannel.send(IdNumberChanged)
        }
    }

    fun addId() {
        if (canAddId.value) {
            viewModelScope.launch {
                executeAddId()
            }
        }
    }

    fun onIdEdited() {
        val animalInfo = animalInfoState.value
            .takeAs<LookupAnimalInfo.AnimalInfoState.Loaded>()
            ?.animalBasicInfo
            ?: return
        viewModelScope.launch {
            animalInfoLookup.lookupAnimalInfoById(animalInfo.id)
        }
    }

    fun removeId(id: EntityId, removeReasonId: EntityId) {
        val animalInfo = animalInfoState.value
            .takeAs<LookupAnimalInfo.AnimalInfoState.Loaded>()
            ?.animalBasicInfo
            ?: return
        if (animalInfo.ids.any { it.id == id }) {
            viewModelScope.launch {
                executeRemoveId(animalInfo.id, id, removeReasonId)
            }
        }
    }

    private suspend fun executeAddId() {
        val animalInfo = animalInfoState.value
            .takeAs<LookupAnimalInfo.AnimalInfoState.Loaded>()
            ?.animalBasicInfo
            ?: return
        val idInput = IdInput(
            number = idNumber,
            type = selectedIdType.value,
            color = selectedIdColor.value,
            location = selectedIdLocation.value
        )

        var idEntry = idInput.toEntry()

        val formatCheck = idValidations.checkIdNumberFormat(idEntry)

        if (formatCheck is Result.Failure) {
            _eventChannel.send(InvalidIdNumberFormat(formatCheck.error.idEntry))
            return
        }

        idEntry = idEntry.copy(isOfficial = idValidations.checkIdEntryIsOfficial(idEntry))

        val idEntryDuplicateEIDCheck = idValidations.checkEIDsNotDuplicated
            .withAdditionOf(idEntry)

        if (idEntryDuplicateEIDCheck is Result.Failure) {
            _eventChannel.send(InvalidIdCombination(idEntryDuplicateEIDCheck.error))
            return
        }

        val idEntryComboCheck = idValidations.checkIdCombinationValidity
            .whenAddingIdToAnimal(idEntry, animalInfo.ids.map { it.asIdEntry() })

        if (idEntryComboCheck is Result.Failure) {
            _eventChannel.send(InvalidIdCombination(idEntryComboCheck.error))
            return
        }
        val animalId = animalInfo.id
        try {
            val resultingId = animalRepo.addIdToAnimal(
                animalId,
                idEntry.type.id,
                idEntry.color.id,
                idEntry.location.id,
                idEntry.number,
                idEntry.isOfficial,
                LocalDateTime.now()
            )
            if (resultingId != EntityId.UNKNOWN) {
                autoUpdateTrichId.autoIncrementIfRequired(idEntry)
                animalInfoLookup.lookupAnimalInfoById(animalInfo.id)
                clearData()
            } else {
                _eventChannel.send(IdAdditionFailed)
            }
        } catch(ex: Exception) {
            postAddIdErrorReport(animalId, idEntry, ex)
        }
    }

    private suspend fun executeRemoveId(animalId: EntityId, id: EntityId, removeReasonId: EntityId) {
        try {
            val success = animalRepo.removeIdFromAnimal(id, removeReasonId, LocalDateTime.now())
            if (success) {
                animalInfoLookup.lookupAnimalInfoById(animalId)
            } else {
                _eventChannel.send(IdRemovalFailed)
            }
        } catch(ex: Exception) {
            postRemoveIdErrorReport(animalId, id, removeReasonId, ex)
        }
    }

    private suspend fun postAddIdErrorReport(
        animalId: EntityId,
        idEntry: IdEntry,
        exception: Exception
    ) {
        postErrorReport(
            ErrorReport(
                action = "Add Animal Id",
                summary = buildString {
                    append("animalId=${animalId}, ")
                    append("idType=${idEntry.type.id}, ")
                    append("idColor=${idEntry.color.id}, ")
                    append("idLocation=${idEntry.location.id}, ")
                    append("number=${idEntry.number}, ")
                    append("isOfficial=${idEntry.isOfficial}")
                },
                error = exception
            )
        )
    }

    private suspend fun postRemoveIdErrorReport(
        animalId: EntityId,
        id: EntityId,
        removeReasonId: EntityId,
        exception: Exception
    ) {
        postErrorReport(
            ErrorReport(
                action = "Remove Animal Id",
                summary = "animalId=${animalId}, id=${id}, removeReason=${removeReasonId}",
                error = exception
            )
        )
    }

    private suspend fun postErrorReport(errorReport: ErrorReport) {
        errorReportChannel.send(errorReport)
    }
}
