package com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.federal.SuggestScrapieFlockNumber
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.trich.AutoIncrementNextTrichIdFeature
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.input.IdEntry
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.input.IdInput
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.input.asIdEntry
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.validation.EIDNumberAlreadyInUse
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.validation.IdFormat
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.validation.IdValidations
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.validation.IdsValidationError
import com.weyr_associates.animaltrakkerfarmmobile.app.core.Result
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.AnimalRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.IdTypeRepository
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalBasicInfo
import com.weyr_associates.animaltrakkerfarmmobile.model.IdInfo
import com.weyr_associates.animaltrakkerfarmmobile.model.IdColor
import com.weyr_associates.animaltrakkerfarmmobile.model.IdLocation
import com.weyr_associates.animaltrakkerfarmmobile.model.IdType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class EditAnimalIdViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val animalRepository: AnimalRepository,
    private val idTypeRepository: IdTypeRepository,
    private val idValidations: IdValidations,
    private val suggestScrapieFlockNumber: SuggestScrapieFlockNumber,
    private val autoUpdateTrichId: AutoIncrementNextTrichIdFeature
) : ViewModel() {

    sealed interface Event

    data object IdNumberChanged : Event
    data object IdUpdateSucceeded : Event
    data object IdUpdateFailed : Event

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

    val originalId: IdInfo by lazy {
        requireNotNull(savedStateHandle.get<IdInfo>(EditAnimalId.EXTRA_ANIMAL_ID_TO_EDIT))
    }

    val animalInfo: AnimalBasicInfo by lazy {
        requireNotNull(savedStateHandle.get<AnimalBasicInfo>(EditAnimalId.EXTRA_ANIMAL_INFO))
    }

    private val _idNumber = MutableStateFlow("")
    var idNumber: String
        get() = _idNumber.value
        set(value) {
            _idNumber.update { value }
        }

    private val _selectedIdType = MutableStateFlow<IdType?>(null)
    val selectedIdType = _selectedIdType.asStateFlow()

    private val _selectedIdColor = MutableStateFlow<IdColor?>(null)
    val selectedIdColor = _selectedIdColor.asStateFlow()

    private val _selectedIdLocation = MutableStateFlow<IdLocation?>(null)
    val selectedIdLocation = _selectedIdLocation.asStateFlow()

    val canUpdateId = combine(
        _idNumber,
        selectedIdType,
        selectedIdColor,
        selectedIdLocation
    ) { idNumber, idType, idColor, idLocation ->
        (idNumber.isNotBlank() && idNumber != originalId.number) ||
                (idType != null && idType.id != originalId.type.id) ||
                (idColor != null && idColor.id != originalId.color.id) ||
                (idLocation != null && idLocation.id != originalId.location.id)
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    val canClearData = canUpdateId

    private val _eventsChannel = Channel<Event>()
    val events = _eventsChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            autoUpdateTrichId.configureFromSettings()
        }
        resetToOriginalIdInfo()
    }

    fun selectIdType(idType: IdType) {
        _selectedIdType.update { idType }
        viewModelScope.launch {
            idNumber = suggestScrapieFlockNumber(idType) ?: ""
            if (autoUpdateTrichId.shouldAutoPopulateTrichNumber(idType)) {
                idNumber = autoUpdateTrichId.nextTrichNumber.toString()
            }
            _eventsChannel.send(IdNumberChanged)
        }
    }

    fun selectIdColor(idColor: IdColor) {
        _selectedIdColor.update { idColor }
    }

    fun selectIdLocation(idLocation: IdLocation) {
        _selectedIdLocation.update { idLocation }
    }

    fun updateId() {
        if (canUpdateId.value) {
            viewModelScope.launch {
                executeUpdateId()
            }
        }
    }

    fun clearData() {
        resetToOriginalIdInfo()
    }

    fun onEIDScanned(eidNumber: String) {
        val acceptScannedEID = suspend {
            selectIdType(requireNotNull(idTypeRepository.queryForIdType(IdType.ID_TYPE_ID_EID)))
            idNumber = eidNumber
            _eventsChannel.send(IdNumberChanged)
        }
        val matchesOriginalId = originalId.type.id == IdType.ID_TYPE_ID_EID &&
                originalId.number == eidNumber
        viewModelScope.launch {
            if (matchesOriginalId) {
                acceptScannedEID()
            } else {
                val eidExists = idValidations.checkEIDsNotDuplicated.isEIDInUse(eidNumber)
                if (eidExists is Result.Failure<Unit, EIDNumberAlreadyInUse>) {
                    _eventsChannel.send(
                        ValidationError.ScannedEIDAlreadyUsed(eidExists.error)
                    )
                } else {
                    acceptScannedEID()
                }
            }
        }
    }

    private suspend fun suggestScrapieFlockNumber(idType: IdType?): String? {
        if (idType?.id != IdType.ID_TYPE_ID_FED) return null
        val scrapieFlockNumber = withContext(Dispatchers.Default) {
            suggestScrapieFlockNumber.retrieveSuggestion()
        }
        return scrapieFlockNumber.takeIf { !scrapieFlockNumber.isNullOrBlank() }?.let {
            "${scrapieFlockNumber}${IdFormat.FEDERAL_SCRAPIE_SEPARATOR}"
        }
    }

    private fun resetToOriginalIdInfo() {
        selectIdType(IdType(originalId.type.id, originalId.type.name, originalId.type.abbreviation, 0))
        selectIdColor(
            IdColor(
                originalId.color.id,
                originalId.color.name,
                originalId.color.abbreviation,
                0
            )
        )
        selectIdLocation(
            IdLocation(
                originalId.location.id,
                originalId.location.name,
                originalId.location.abbreviation,
                0
            )
        )
        idNumber = originalId.number
        viewModelScope.launch { _eventsChannel.send(IdNumberChanged) }
    }

    private suspend fun executeUpdateId() {
        val idInput = IdInput(
            number = idNumber,
            type = selectedIdType.value,
            color = selectedIdColor.value,
            location = selectedIdLocation.value
        )

        var idEntry = idInput.toEntry(id = originalId.id)

        val formatCheck = idValidations.checkIdNumberFormat(idEntry)

        if (formatCheck is Result.Failure) {
            _eventsChannel.send(
                ValidationError.InvalidIdNumberFormat(
                    formatCheck.error.idEntry
                )
            )
            return
        }

        idEntry = idEntry.copy(isOfficial = idValidations.checkIdEntryIsOfficial(idEntry))

        val idEntryDuplicateEIDCheck = idValidations.checkEIDsNotDuplicated
            .withUpdateOf(idEntry)

        if (idEntryDuplicateEIDCheck is Result.Failure) {
            _eventsChannel.send(
                ValidationError.InvalidIdCombination(
                    idEntryDuplicateEIDCheck.error
                )
            )
            return
        }

        val idEntryComboCheck = idValidations.checkIdCombinationValidity
            .whenUpdatingIdOnAnimal(idEntry, animalInfo.ids.map { it.asIdEntry() })

        if (idEntryComboCheck is Result.Failure) {
            _eventsChannel.send(
                ValidationError.InvalidIdCombination(
                    idEntryComboCheck.error
                )
            )
            return
        }

        val success = animalRepository.updateIdOnAnimal(
            id = originalId.id,
            typeId = idEntry.type.id,
            colorId = idEntry.color.id,
            locationId = idEntry.location.id,
            number = idEntry.number,
            LocalDateTime.now()
        )
        if (success) {
            autoUpdateTrichId.autoIncrementIfRequired(idEntry)
        }
        _eventsChannel.send(
            if (success) IdUpdateSucceeded
            else IdUpdateFailed
        )
    }
}