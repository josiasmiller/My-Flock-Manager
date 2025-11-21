package com.weyr_associates.animaltrakkerfarmmobile.app.animal.care.drugs.doses.offlabel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weyr_associates.animaltrakkerfarmmobile.app.core.ErrorReport
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.DrugRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadDefaultSpecies
import com.weyr_associates.animaltrakkerfarmmobile.model.Drug
import com.weyr_associates.animaltrakkerfarmmobile.model.Species
import com.weyr_associates.animaltrakkerfarmmobile.model.VetContact
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
import java.time.LocalDate
import java.time.LocalDateTime

class AddOffLabelDrugDoseViewModel(
    private val drugRepository: DrugRepository,
    private val loadDefaultSpecies: LoadDefaultSpecies
) : ViewModel() {

    enum class Field {
        OFF_LABEL_DOSE,
        OFF_LABEL_NOTES
    }

    sealed interface Event

    data class FieldValueChanged(
        private val affectedFields: Set<Field>
    ) : Event {
        constructor(vararg affectedFields: Field)
                : this(affectedFields.toSet())
        fun isAffected(field: Field): Boolean {
            return affectedFields.isEmpty() || affectedFields.contains(field)
        }
    }

    data object SaveSucceededEvent : Event

    private val _selectedDrug = MutableStateFlow<Drug?>(null)
    val selectedDrug = _selectedDrug.asStateFlow()

    private val _selectedSpeciesForDosage = MutableStateFlow<Species?>(null)
    val selectedSpeciesForDosage = _selectedSpeciesForDosage.asStateFlow()

    private val _selectedOffLabelVetContact = MutableStateFlow<VetContact?>(null)
    val selectedOffLabelVetContact = _selectedOffLabelVetContact.asStateFlow()

    private val _offLabelDrugDose = MutableStateFlow("")
    var offLabelDrugDose: String
        get() = _offLabelDrugDose.value
        set(value) { _offLabelDrugDose.update { value } }

    private val _offLabelDrugNotes = MutableStateFlow("")
    var offLabelDrugNotes: String
        get() = _offLabelDrugNotes.value
        set(value) { _offLabelDrugNotes.update { value } }

    private val _selectedOffLabelEndUseDate = MutableStateFlow<LocalDate?>(null)
    val selectedOffLabelEndUseDate = _selectedOffLabelEndUseDate.asStateFlow()

    private val eventsChannel = Channel<Event>()
    val events = eventsChannel.receiveAsFlow()

    private val errorReportChannel = Channel<ErrorReport>()
    val errorReportFlow = errorReportChannel.receiveAsFlow()

    val canSave = combine(
        _selectedDrug, _selectedSpeciesForDosage,
        _selectedOffLabelVetContact, _offLabelDrugDose
    ) { drug, speciesForDosage, offLabelVetContact, offLabelDose ->
        drug != null && speciesForDosage != null &&
                offLabelVetContact != null && offLabelDose.isNotBlank()

    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    init {
        viewModelScope.launch {
            _selectedSpeciesForDosage.update { loadDefaultSpecies() }
        }
    }

    fun selectDrug(drug: Drug) {
        _selectedDrug.update { drug }
    }

    fun selectSpeciesForDosage(species: Species) {
        _selectedSpeciesForDosage.update { species }
    }

    fun selectVeterinarian(veterinarian: VetContact) {
        _selectedOffLabelVetContact.update { veterinarian }
    }

    fun updateOffLabelEndUseDate(endDate: LocalDate?) {
        _selectedOffLabelEndUseDate.update { endDate }
    }

    fun save() {
        if (canSave.value) {
            viewModelScope.launch {
                executeSave()
            }
        }
    }

    private suspend fun executeSave() {

        val drugId = selectedDrug.value?.id ?: return
        val targetSpeciesId = selectedSpeciesForDosage.value?.id ?: return
        val vetContactId = selectedOffLabelVetContact.value?.id ?: return
        val offLabelDrugDosage = offLabelDrugDose
        val offLabelDrugNotes = offLabelDrugNotes
        val offLabelUseStartDate = LocalDate.now()
        val offLabelUseEndDate = selectedOffLabelEndUseDate.value

        withContext(Dispatchers.IO) {
            try {
                drugRepository.addOffLabelDrugDose(
                    drugId = drugId,
                    speciesId = targetSpeciesId,
                    veterinarianContactId = vetContactId,
                    drugDosage = offLabelDrugDosage,
                    useStartDate = offLabelUseStartDate,
                    useEndDate = offLabelUseEndDate,
                    notes = offLabelDrugNotes,
                    timeStamp = LocalDateTime.now()
                )
                eventsChannel.send(SaveSucceededEvent)
                clearData()
            } catch(ex: Exception) {
                errorReportChannel.send(
                    ErrorReport(
                        action = "Add Off Label Drug Dose",
                        summary = buildString {
                            append("drugId=${drugId}, ")
                            append("speciesId=${targetSpeciesId}, ")
                            append("veterinarianContactId=${vetContactId}, ")
                            append("drugDosage=${offLabelDrugDosage}, ")
                            append("useStartDate=${offLabelUseStartDate}, ")
                            append("useEndDate=${offLabelUseEndDate}, ")
                            append("notes=${offLabelDrugNotes}")
                        },
                        error = ex
                    )
                )
            }
        }
    }

    private fun clearData() {
        _selectedDrug.update { null }
        _selectedSpeciesForDosage.update { null }
        _selectedOffLabelVetContact.update { null }
        _selectedOffLabelEndUseDate.update { null }
        offLabelDrugDose = ""
        offLabelDrugNotes = ""

        viewModelScope.launch {
            eventsChannel.send(
                FieldValueChanged()
            )
        }
    }
}
