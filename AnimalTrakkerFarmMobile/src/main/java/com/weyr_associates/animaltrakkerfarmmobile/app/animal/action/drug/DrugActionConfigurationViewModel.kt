package com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.drug

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.coroutines.channel.EventEmitter
import com.weyr_associates.animaltrakkerfarmmobile.model.DrugLocation
import com.weyr_associates.animaltrakkerfarmmobile.model.DrugApplicationInfo
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.OffLabelDrugDose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DrugActionConfigurationViewModel(
    private val drugTypeId: EntityId,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    sealed interface Event

    data class DrugActionConfigured(
        val drugActionConfiguration: DrugAction.Configuration
    ) : Event

    private val _drugSelection = MutableStateFlow<DrugApplicationInfo?>(null)
    val drugSelection = _drugSelection.asStateFlow()

    private val _offLabelDoseSelection = MutableStateFlow<OffLabelDrugDose?>(null)
    val offLabelDrugDoseSelection = _offLabelDoseSelection.asStateFlow()

    private val _drugLocationSelection = MutableStateFlow<DrugLocation?>(null)
    val drugLocationSelection = _drugLocationSelection.asStateFlow()

    val canConfigure = combine(drugSelection, drugLocationSelection) { drug, drugLocation ->
        drug != null && drug.drugTypeId == drugTypeId && drugLocation != null
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val eventEmitter = EventEmitter<Event>(viewModelScope)
    val events = eventEmitter.events

    init {
        viewModelScope.launch {
            drugSelection.collectLatest {
                _offLabelDoseSelection.update { null }
            }
        }
        savedStateHandle.get<DrugAction.Configuration>(
            ConfigureDrugAction.EXTRA_DRUG_ACTION_CONFIGURATION
        )?.let { configuration ->
            _drugSelection.update { configuration.drugApplicationInfo }
            _offLabelDoseSelection.update { configuration.offLabelDrugDose }
            _drugLocationSelection.update { configuration.location }
        }
    }

    fun updateDrugSelection(drug: DrugApplicationInfo) {
        if (drug.drugTypeId == drugTypeId) {
            _drugSelection.update { drug }
        }
    }

    fun updateOffLabelDrugDoseSelection(offLabelDrugDose: OffLabelDrugDose?) {
        if (offLabelDrugDose == null || offLabelDrugDose.drugId == drugSelection.value?.drugId) {
            _offLabelDoseSelection.update { offLabelDrugDose }
        }
    }

    fun updateDrugLocationSelection(drugLocation: DrugLocation) {
        _drugLocationSelection.update { drugLocation }
    }

    fun configure() {
        if (canConfigure.value) {
            val drug = drugSelection.value ?: return
            val drugLocation = drugLocationSelection.value ?: return
            val offLabelDrugDose = offLabelDrugDoseSelection.value
            eventEmitter.emit(
                DrugActionConfigured(
                    DrugAction.Configuration(
                        drug,
                        drugLocation,
                        offLabelDrugDose
                    )
                )
            )
        }
    }
}
