package com.weyr_associates.animaltrakkerfarmmobile.app.animal.care.drugs.doses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weyr_associates.animaltrakkerfarmmobile.app.core.ErrorReport
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.coroutines.flow.combine10
import com.weyr_associates.animaltrakkerfarmmobile.app.model.summarizeForErrorReport
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.DrugRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadDefaultSpecies
import com.weyr_associates.animaltrakkerfarmmobile.model.Drug
import com.weyr_associates.animaltrakkerfarmmobile.model.DrugWithdrawalSpec
import com.weyr_associates.animaltrakkerfarmmobile.model.Species
import com.weyr_associates.animaltrakkerfarmmobile.model.UnitOfMeasure
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class AddDrugDoseViewModel(
    private val drugRepository: DrugRepository,
    private val loadDefaultSpecies: LoadDefaultSpecies
) : ViewModel() {

    enum class Field {
        OFFICIAL_DOSE,
        USER_DOSE,
        MEAT_WITHDRAWAL,
        USER_MEAT_WITHDRAWAL,
        MILK_WITHDRAWAL,
        USER_MILK_WITHDRAWAL,
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

    private val _officialDrugDose = MutableStateFlow("")
    var officialDrugDose: String
        get() = _officialDrugDose.value
        set(value) { _officialDrugDose.update { value} }

    private val _userDrugDose = MutableStateFlow("")
    var userDrugDose: String
        get() = _userDrugDose.value
        set(value) { _userDrugDose.update { value } }

    private val _meatWithdrawal = MutableStateFlow<Int?>(null)
    var meatWithdrawal: Int?
        get() = _meatWithdrawal.value
        set(value) { _meatWithdrawal.update { value } }

    private val _userMeatWithdrawal = MutableStateFlow<Int?>(null)
    var userMeatWithdrawal: Int?
        get() = _userMeatWithdrawal.value
        set(value) { _userMeatWithdrawal.update { value } }

    private val _selectedMeatWithdrawalUnits = MutableStateFlow<UnitOfMeasure?>(null)
    val selectedMeatWithdrawalUnits = _selectedMeatWithdrawalUnits.asStateFlow()

    private val _milkWithdrawal = MutableStateFlow<Int?>(null)
    var milkWithdrawal: Int?
        get() = _milkWithdrawal.value
        set(value) { _milkWithdrawal.update { value } }

    private val _userMilkWithdrawal = MutableStateFlow<Int?>(null)
    var userMilkWithdrawal: Int?
        get() = _userMilkWithdrawal.value
        set(value) { _userMilkWithdrawal.update { value } }

    private val _selectedMilkWithdrawalUnits = MutableStateFlow<UnitOfMeasure?>(null)
    val selectedMilkWithdrawalUnits = _selectedMilkWithdrawalUnits.asStateFlow()

    private val eventsChannel = Channel<Event>()
    val events = eventsChannel.receiveAsFlow()

    private val errorReportChannel = Channel<ErrorReport>()
    val errorReportFlow = errorReportChannel.receiveAsFlow()

    val canSave = combine10(
        _selectedDrug, _selectedSpeciesForDosage,
        _meatWithdrawal, _userMeatWithdrawal, _selectedMeatWithdrawalUnits,
        _milkWithdrawal, _userMilkWithdrawal, _selectedMilkWithdrawalUnits,
        _officialDrugDose, _userDrugDose
    ) { drug, speciesForDosage, meatWithdrawal, userMeatWithdrawal,
        meatWithdrawalUnits, withdrawalMilk, userMilkWithdrawal, milkWithdrawalUnits,
        officialDose, userDose ->

        val requiredValuesPresent = drug != null && speciesForDosage != null &&
                officialDose.isNotBlank() && userDose.isNotBlank()

        val isMeatWithdrawalExpected = meatWithdrawalUnits != null

        val isMeatWithdrawalValid = if (isMeatWithdrawalExpected)
            meatWithdrawal != null && 0 <= meatWithdrawal else meatWithdrawal == null

        val isMeatWithdrawalUserValid = if(isMeatWithdrawalExpected)
            userMeatWithdrawal == null ||
                    (meatWithdrawal != null && meatWithdrawal <= userMeatWithdrawal)
        else userMeatWithdrawal == null

        val isMilkWithdrawalExpected = milkWithdrawalUnits != null

        val isMilkWithdrawalValid = if (isMilkWithdrawalExpected)
            withdrawalMilk != null && 0 <= withdrawalMilk else withdrawalMilk == null

        val isMilkWithdrawalUserValid = if (isMilkWithdrawalExpected)
            userMilkWithdrawal == null ||
                    (withdrawalMilk != null && withdrawalMilk <= userMilkWithdrawal)
        else userMilkWithdrawal == null

        requiredValuesPresent && isMeatWithdrawalValid && isMeatWithdrawalUserValid &&
                isMilkWithdrawalValid && isMilkWithdrawalUserValid

    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    init {
        viewModelScope.launch {
            _selectedSpeciesForDosage.update { loadDefaultSpecies() }
        }
        viewModelScope.launch {
            selectedMeatWithdrawalUnits.collectLatest {
                if (it == null) {
                    meatWithdrawal = null
                    userMeatWithdrawal = null
                    eventsChannel.send(
                        FieldValueChanged(
                            Field.MEAT_WITHDRAWAL,
                            Field.USER_MEAT_WITHDRAWAL
                        )
                    )
                }
            }
        }
        viewModelScope.launch {
            selectedMilkWithdrawalUnits.collectLatest {
                if (it == null) {
                    milkWithdrawal = null
                    userMilkWithdrawal = null
                    eventsChannel.send(
                        FieldValueChanged(
                            Field.MILK_WITHDRAWAL,
                            Field.USER_MILK_WITHDRAWAL
                        )
                    )
                }
            }
        }
    }

    fun selectDrug(drug: Drug) {
        _selectedDrug.update { drug }
    }


    fun selectSpeciesForDosage(species: Species) {
        _selectedSpeciesForDosage.update { species }
    }

    fun selectMeatWithdrawalUnits(withdrawalUnits: UnitOfMeasure?) {
        if (withdrawalUnits == null || withdrawalUnits.type.id == UnitOfMeasure.Type.ID_TIME) {
            _selectedMeatWithdrawalUnits.update { withdrawalUnits }
        }
    }

    fun selectMilkWithdrawalUnits(withdrawalUnits: UnitOfMeasure?) {
        if (withdrawalUnits == null || withdrawalUnits.type.id == UnitOfMeasure.Type.ID_TIME) {
            _selectedMilkWithdrawalUnits.update { withdrawalUnits }
        }
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
        val officialDose = officialDrugDose.trim().takeIf { it.isNotBlank() } ?: return
        val userDose = userDrugDose.trim().takeIf { it.isNotBlank() } ?: ""

        val meatWithdrawalSpec = DrugWithdrawalSpec.create(
            withdrawal = meatWithdrawal,
            userWithdrawal = userMeatWithdrawal,
            withdrawalUnitsId = selectedMeatWithdrawalUnits.value?.id
        )

        val milkWithdrawalSpec = DrugWithdrawalSpec.create(
            withdrawal = milkWithdrawal,
            userWithdrawal = userMilkWithdrawal,
            withdrawalUnitsId = selectedMilkWithdrawalUnits.value?.id
        )

        withContext(Dispatchers.IO) {
            try {
                val drugDoseId = drugRepository.addDrugDose(
                    drugId = drugId,
                    speciesId = targetSpeciesId,
                    officialDrugDose = officialDose,
                    userDrugDose = userDose,
                    meatWithdrawalSpec = meatWithdrawalSpec,
                    milkWithdrawalSpec = milkWithdrawalSpec,
                    timeStamp = LocalDateTime.now()
                )
                eventsChannel.send(SaveSucceededEvent)
                clearData()
            } catch(ex: Exception) {
                errorReportChannel.send(
                    ErrorReport(
                        action = "Add Drug Dose",
                        summary = buildString {
                            append("drugId=${drugId}, ")
                            append("speciesId=${targetSpeciesId}, ")
                            append("officialDose=${officialDose}, ")
                            append("userDose=${userDose}, ")
                            append("meatWithdrawal={${meatWithdrawalSpec?.summarizeForErrorReport()}}, ")
                            append("milkWithdrawal={${milkWithdrawalSpec?.summarizeForErrorReport()}}")
                        },
                        error = ex
                    )
                )
            }
        }
    }

    private fun clearData() {
        meatWithdrawal = null
        userMeatWithdrawal = null
        milkWithdrawal = null
        userMilkWithdrawal = null
        officialDrugDose = ""
        userDrugDose = ""
        _selectedDrug.update { null }
        _selectedMeatWithdrawalUnits.update { null }
        _selectedMilkWithdrawalUnits.update { null }

        viewModelScope.launch {
            eventsChannel.send(
                FieldValueChanged()
            )
        }
    }
}
