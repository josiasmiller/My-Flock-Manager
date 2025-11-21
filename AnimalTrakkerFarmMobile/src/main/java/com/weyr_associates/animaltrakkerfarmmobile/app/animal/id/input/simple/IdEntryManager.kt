package com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.input.simple

import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.farm.BaseFarmTagOnEIDFeature
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.federal.SuggestScrapieFlockNumber
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.input.IdInput
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.input.simple.IdEntryEditor.Event
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.input.simple.IdEntryEditor.IdEntryField
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.input.simple.IdEntryEditor.InputEvent
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.trich.AutoIncrementNextTrichIdFeature
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.validation.IdFormat
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.IdColorRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.IdLocationRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.IdTypeRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.IdConfigs
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.model.IdColor
import com.weyr_associates.animaltrakkerfarmmobile.model.IdLocation
import com.weyr_associates.animaltrakkerfarmmobile.model.IdType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class IdEntryManager(
    private val coroutineScope: CoroutineScope,
    private val idTypeRepository: IdTypeRepository,
    private val idColorRepository: IdColorRepository,
    private val idLocationRepository: IdLocationRepository,
    private val loadActiveDefaultSettings: LoadActiveDefaultSettings,
    private val baseFarmTagOnEIDFeature: BaseFarmTagOnEIDFeature,
    private val suggestScrapieFlockNumber: SuggestScrapieFlockNumber,
    private val autoUpdateTrichId: AutoIncrementNextTrichIdFeature
) : IdEntryEditor {

    private var eidIdType: IdType? = null
    private var eidIdColor: IdColor? = null
    private var eidIdLocation: IdLocation? = null

    private var farmIdType: IdType? = null
    private var farmIdColor: IdColor? = null
    private var farmIdLocation: IdLocation? = null

    private val _isEditable = MutableStateFlow(true)
    override val isEditable = _isEditable.asStateFlow()

    private val _idNumber1 = MutableStateFlow("")
    override var idNumber1: String
        get() = _idNumber1.value
        set(value) { _idNumber1.update { value } }

    private val _idNumber2 = MutableStateFlow("")
    override var idNumber2: String
        get() = _idNumber2.value
        set(value) { _idNumber2.update { value } }

    private val _idNumber3 = MutableStateFlow("")
    override var idNumber3: String
        get() = _idNumber3.value
        set(value) { _idNumber3.update { value } }

    private val _selectedIdType1 = MutableStateFlow<IdType?>(null)
    override val selectedIdType1 = _selectedIdType1.asStateFlow()

    private val _selectedIdColor1 = MutableStateFlow<IdColor?>(null)
    override val selectedIdColor1 = _selectedIdColor1.asStateFlow()

    private val _selectedIdLocation1 = MutableStateFlow<IdLocation?>(null)
    override val selectedIdLocation1 = _selectedIdLocation1.asStateFlow()

    private val _selectedIdType2 = MutableStateFlow<IdType?>(null)
    override val selectedIdType2 = _selectedIdType2.asStateFlow()

    private val _selectedIdColor2 = MutableStateFlow<IdColor?>(null)
    override val selectedIdColor2 = _selectedIdColor2.asStateFlow()

    private val _selectedIdLocation2 = MutableStateFlow<IdLocation?>(null)
    override val selectedIdLocation2 = _selectedIdLocation2.asStateFlow()

    private val _selectedIdType3 = MutableStateFlow<IdType?>(null)
    override val selectedIdType3 = _selectedIdType3.asStateFlow()

    private val _selectedIdColor3 = MutableStateFlow<IdColor?>(null)
    override val selectedIdColor3 = _selectedIdColor3.asStateFlow()

    private val _selectedIdLocation3 = MutableStateFlow<IdLocation?>(null)
    override val selectedIdLocation3 = _selectedIdLocation3.asStateFlow()

    val canReset = combine(_idNumber1, _idNumber2, _idNumber3) { id1, id2, id3 ->
        id1.isNotBlank() || id2.isNotBlank() || id3.isNotBlank()
    }.stateIn(coroutineScope, SharingStarted.Lazily, false)

    private val _events = MutableSharedFlow<Event>()
    override val events = _events.asSharedFlow()

    init {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                val defaultSettings = loadActiveDefaultSettings()
                eidIdColor = defaultSettings.eidColorMale?.let { idColorRepository.queryIdColor(it) }
                eidIdLocation = defaultSettings.eidIdLocation?.let { idLocationRepository.queryIdLocation(it) }
                farmIdColor = defaultSettings.farmIdColorMale?.let { idColorRepository.queryIdColor(it) }
                farmIdLocation = defaultSettings.farmIdLocation?.let { idLocationRepository.queryIdLocation(it) }
            }
        }
    }

    fun applyConfigs(idConfigs: IdConfigs) {
        selectIdType1(idConfigs.primary.idType)
        selectIdColor1(idConfigs.primary.idColor)
        selectIdLocation1(idConfigs.primary.idLocation)

        selectIdType2(idConfigs.secondary?.idType)
        selectIdColor2(idConfigs.secondary?.idColor)
        selectIdLocation2(idConfigs.secondary?.idLocation)

        selectIdType3(idConfigs.tertiary?.idType)
        selectIdColor3(idConfigs.tertiary?.idColor)
        selectIdLocation3(idConfigs.tertiary?.idLocation)
    }

    fun captureIdInputs(): IdInputs {
        val idInput1 = IdInput(
            idNumber1,
            selectedIdType1.value,
            selectedIdColor1.value,
            selectedIdLocation1.value
        )
        val idInput2 = IdInput(
            idNumber2,
            selectedIdType2.value,
            selectedIdColor2.value,
            _selectedIdLocation2.value
        )
        val idInput3 = IdInput(
            idNumber3,
            selectedIdType3.value,
            selectedIdColor3.value,
            selectedIdLocation3.value
        )
        return IdInputs(idInput1, idInput2, idInput3)
    }

    fun reset() {
        idNumber1 = ""
        idNumber2 = ""
        idNumber3 = ""
        postEvent(InputEvent.IdNumber1Changed)
        postEvent(InputEvent.IdNumber2Changed)
        postEvent(InputEvent.IdNumber3Changed)
    }

    fun clear() {
        idNumber1 = ""
        idNumber2 = ""
        idNumber3 = ""
        _selectedIdType1.update { null }
        _selectedIdType2.update { null }
        _selectedIdType3.update { null }
        _selectedIdColor1.update { null }
        _selectedIdColor2.update { null }
        _selectedIdColor3.update { null }
        _selectedIdLocation1.update { null }
        _selectedIdLocation2.update { null }
        _selectedIdLocation3.update { null }
        postEvent(InputEvent.IdNumber1Changed)
        postEvent(InputEvent.IdNumber2Changed)
        postEvent(InputEvent.IdNumber3Changed)
    }

    fun setIsEditable(isEditable: Boolean) {
        _isEditable.update { isEditable }
    }

    override fun selectIdType1(idType: IdType?) {
        _selectedIdType1.update { idType }
        onIdTypeChanged(idType, IdEntryField.FIRST)
    }

    override fun selectIdColor1(idColor: IdColor?) {
        _selectedIdColor1.update { idColor }
    }

    override fun selectIdLocation1(idLocation: IdLocation?) {
        _selectedIdLocation1.update { idLocation }
    }

    override fun selectIdType2(idType: IdType?) {
        _selectedIdType2.update { idType }
        onIdTypeChanged(idType, IdEntryField.SECOND)
    }

    override fun selectIdColor2(idColor: IdColor?) {
        _selectedIdColor2.update { idColor }
    }

    override fun selectIdLocation2(idLocation: IdLocation?) {
        _selectedIdLocation2.update { idLocation }
    }

    override fun selectIdType3(idType: IdType?) {
        _selectedIdType3.update { idType }
        onIdTypeChanged(idType, IdEntryField.THIRD)
    }

    override fun selectIdColor3(idColor: IdColor?) {
        _selectedIdColor3.update { idColor }
    }

    override fun selectIdLocation3(idLocation: IdLocation?) {
        _selectedIdLocation3.update { idLocation }
    }

    override fun onEIDScanned(eidString: String) {
        coroutineScope.launch {
            idNumber1 = eidString
            _selectedIdType1.update { loadEidIdType() }
            _selectedIdColor1.update { eidIdColor }
            _selectedIdLocation1.update { eidIdLocation }
            postEvent(InputEvent.IdNumber1Changed)
            val farmTagEIDNumber = baseFarmTagOnEIDFeature.extractFarmTagPrefixFromEID(eidString)
            if (!farmTagEIDNumber.isNullOrBlank()) {
                idNumber2 = farmTagEIDNumber
                _selectedIdType2.update { loadFarmIdType() }
                _selectedIdColor2.update { farmIdColor }
                _selectedIdLocation2.update { farmIdLocation }
                postEvent(InputEvent.IdNumber2Changed)
            }
        }
    }

    private fun onIdTypeChanged(idType: IdType?, idEntryField: IdEntryField) {
        clearIdNumberEntry(idEntryField)
        if (idType == null) {
            clearIdEntryField(idEntryField)
        } else {
            suggestScrapieFlockNumber(idType, idEntryField)
            checkAutoPopulateTrich(idType, idEntryField)
        }
    }

    private fun clearIdNumberEntry(idEntryField: IdEntryField) {
        updateIdNumber("", idEntryField)
    }

    private fun clearIdEntryField(idEntryField: IdEntryField) {
        clearIdNumberEntry(idEntryField)
        when (idEntryField) {
            IdEntryField.FIRST -> {
                _selectedIdColor1.update { null }
                _selectedIdLocation1.update { null }
            }
            IdEntryField.SECOND -> {
                _selectedIdColor2.update { null }
                _selectedIdLocation2.update { null }
            }
            IdEntryField.THIRD -> {
                _selectedIdColor3.update { null }
                _selectedIdLocation3.update { null }
            }
        }
    }

    private fun suggestScrapieFlockNumber(idType: IdType?, idEntryField: IdEntryField) {
        if (idType?.id != IdType.ID_TYPE_ID_FED) return
        coroutineScope.launch {
            val scrapieFlockNumber = withContext(Dispatchers.Default) {
                suggestScrapieFlockNumber.retrieveSuggestion()
            }
            if (!scrapieFlockNumber.isNullOrBlank()) {
                updateIdNumber(
                    idNumber = "${scrapieFlockNumber}${IdFormat.FEDERAL_SCRAPIE_SEPARATOR}",
                    idEntryField = idEntryField
                )
            }
        }
    }

    private fun checkAutoPopulateTrich(idType: IdType?, idEntryField: IdEntryField) {
        if (idType == null) return
        if (autoUpdateTrichId.shouldAutoPopulateTrichNumber(idType)) {
            val nextTrichNumberString = autoUpdateTrichId.nextTrichNumber.toString()
            updateIdNumber(nextTrichNumberString, idEntryField)
        }
    }

    private fun updateIdNumber(idNumber: String, idEntryField: IdEntryField) {
        when (idEntryField) {
            IdEntryField.FIRST -> {
                idNumber1 = idNumber
                postEvent(InputEvent.IdNumber1Changed)
            }
            IdEntryField.SECOND -> {
                idNumber2 = idNumber
                postEvent(InputEvent.IdNumber2Changed)
            }
            IdEntryField.THIRD -> {
                idNumber3 = idNumber
                postEvent(InputEvent.IdNumber3Changed)
            }
        }
    }

    private suspend fun loadEidIdType(): IdType {
        return eidIdType ?: requireNotNull(
            withContext(Dispatchers.IO) {
                idTypeRepository.queryForIdType(IdType.ID_TYPE_ID_EID)
            }
        ).also { eidIdType = it }
    }

    private suspend fun loadFarmIdType(): IdType {
        return farmIdType ?: requireNotNull(
            withContext(Dispatchers.IO) {
                idTypeRepository.queryForIdType(IdType.ID_TYPE_ID_FARM)
            }
        ).also { farmIdType = it }
    }

    private fun postEvent(event: Event) {
        coroutineScope.launch { _events.emit(event) }
    }
}
