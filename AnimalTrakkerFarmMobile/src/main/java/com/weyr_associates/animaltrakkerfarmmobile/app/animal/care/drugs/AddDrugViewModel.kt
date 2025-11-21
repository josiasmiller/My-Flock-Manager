package com.weyr_associates.animaltrakkerfarmmobile.app.animal.care.drugs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weyr_associates.animaltrakkerfarmmobile.app.core.ErrorReport
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.coroutines.flow.combine18
import com.weyr_associates.animaltrakkerfarmmobile.app.model.summarizeForErrorReport
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.DrugRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadDefaultSpecies
import com.weyr_associates.animaltrakkerfarmmobile.model.Drug
import com.weyr_associates.animaltrakkerfarmmobile.model.DrugType
import com.weyr_associates.animaltrakkerfarmmobile.model.DrugWithdrawalSpec
import com.weyr_associates.animaltrakkerfarmmobile.model.OffLabelDrugSpec
import com.weyr_associates.animaltrakkerfarmmobile.model.Species
import com.weyr_associates.animaltrakkerfarmmobile.model.UnitOfMeasure
import com.weyr_associates.animaltrakkerfarmmobile.model.VetContact
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
import java.time.LocalDate
import java.time.LocalDateTime

class AddDrugViewModel(
    private val drugRepository: DrugRepository,
    private val loadDefaultSpecies: LoadDefaultSpecies
) : ViewModel() {

    enum class Field {
        TRADE_NAME,
        GENERIC_NAME,
        OFFICIAL_DOSE,
        USER_DOSE,
        MEAT_WITHDRAWAL,
        USER_MEAT_WITHDRAWAL,
        MILK_WITHDRAWAL,
        USER_MILK_WITHDRAWAL,
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

    data class SaveSucceededEvent(val drug: Drug) : Event

    private val _selectedDrugType = MutableStateFlow<DrugType?>(null)
    val selectedDrugType = _selectedDrugType.asStateFlow()

    private val _isDrugRemovable = MutableStateFlow(false)
    val isDrugRemovable = _isDrugRemovable.asStateFlow()

    private val _tradeDrugName = MutableStateFlow("")
    var tradeDrugName: String
        get() = _tradeDrugName.value
        set(value) { _tradeDrugName.update { value } }

    private val _genericDrugName = MutableStateFlow("")
    var genericDrugName: String
        get() = _genericDrugName.value
        set(value) { _genericDrugName.update { value } }

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

    private val _isAddingOffLabelDose = MutableStateFlow(false)
    val isAddingOffLabelDose = _isAddingOffLabelDose.asStateFlow()

    private val _selectedOffLabelVetContact = MutableStateFlow<VetContact?>(null)
    val selectedOffLabelVetContact = _selectedOffLabelVetContact.asStateFlow()

    private val _selectedSpeciesForOffLabelDosage = MutableStateFlow<Species?>(null)
    val selectedSpeciesForOffLabelDosage = _selectedSpeciesForOffLabelDosage.asStateFlow()

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

    val canSave = combine18(
        _selectedDrugType, _tradeDrugName, _genericDrugName, _selectedSpeciesForDosage,
        _meatWithdrawal, _userMeatWithdrawal, _selectedMeatWithdrawalUnits,
        _milkWithdrawal, _userMilkWithdrawal, _selectedMilkWithdrawalUnits,
        _officialDrugDose, _userDrugDose, _isAddingOffLabelDose, _selectedOffLabelVetContact,
        _selectedSpeciesForOffLabelDosage, _offLabelDrugDose, _offLabelDrugNotes, _selectedOffLabelEndUseDate
    ) { drugType, tradeName, genericName, speciesForDosage, meatWithdrawal, userMeatWithdrawal,
        meatWithdrawalUnits, withdrawalMilk, userMilkWithdrawal, milkWithdrawalUnits,
        officialDose, userDose, isAddingOffLabelDose, offLabelVetContact, offLabelSpecies, offLabelDose,
        offLabelNotes, offLabelUseEndDate ->

        val requiredValuesPresent = drugType != null && speciesForDosage != null &&
                tradeName.isNotBlank() && genericName.isNotBlank() &&
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

        val offLabelDoseValuesAreValid = !isAddingOffLabelDose ||
            (offLabelVetContact != null && offLabelSpecies != null && offLabelDose.isNotBlank())

        requiredValuesPresent && offLabelDoseValuesAreValid &&
                isMeatWithdrawalValid && isMeatWithdrawalUserValid &&
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
        viewModelScope.launch {
            isAddingOffLabelDose.collectLatest {
                if (!it) {
                    _selectedOffLabelVetContact.update { null }
                    _selectedSpeciesForOffLabelDosage.update { null }
                    _selectedOffLabelEndUseDate.update { null }
                    offLabelDrugDose = ""
                    offLabelDrugNotes = ""
                    eventsChannel.send(
                        FieldValueChanged(
                            setOf(
                                Field.OFF_LABEL_DOSE,
                                Field.OFF_LABEL_NOTES
                            )
                        )
                    )
                }
            }
        }
    }

    fun selectDrugType(drugType: DrugType) {
        _selectedDrugType.update { drugType }
    }

    fun setIsRemovable(isRemovable: Boolean) {
        _isDrugRemovable.update { isRemovable }
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

    fun setIsAddingOffLabelDose(isOffLabel: Boolean) {
        _isAddingOffLabelDose.update { isOffLabel }
    }

    fun selectVeterinarian(veterinarian: VetContact) {
        _selectedOffLabelVetContact.update { veterinarian }
    }

    fun selectSpeciesForOffLabelDosage(species: Species) {
        _selectedSpeciesForOffLabelDosage.update { species }
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

        val drugTypeId = selectedDrugType.value?.id ?: return
        val tradeName = tradeDrugName.trim().takeIf { it.isNotBlank() } ?: return
        val genericName = genericDrugName.trim().takeIf { it.isNotBlank() } ?: return
        val targetSpeciesId = selectedSpeciesForDosage.value?.id ?: return
        val officialDose = officialDrugDose.trim().takeIf { it.isNotBlank() } ?: return
        val userDose = userDrugDose.trim().takeIf { it.isNotBlank() } ?: ""
        val isRemovable = isDrugRemovable.value
        val isAddingOffLabelDose = isAddingOffLabelDose.value
        val vetContactId = selectedOffLabelVetContact.value?.id
        val offLabelSpeciesId = selectedSpeciesForOffLabelDosage.value?.id
        val offLabelDrugDosage = offLabelDrugDose
        val offLabelDrugNotes = offLabelDrugNotes
        val offLabelUseEndDate = selectedOffLabelEndUseDate.value

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

        val offLabelDrugSpec = if (isAddingOffLabelDose) {
            OffLabelDrugSpec.create(
                veterinarianContactId = vetContactId,
                speciesId = offLabelSpeciesId,
                drugDosage = offLabelDrugDosage,
                useStartDate = LocalDate.now(),
                useEndDate = offLabelUseEndDate,
                note = offLabelDrugNotes
            ) ?: return
        } else null

        withContext(Dispatchers.IO) {
            try {
                val drugId = drugRepository.addDrug(
                    drugTypeId = drugTypeId,
                    tradeDrugName = tradeName,
                    genericDrugName = genericName,
                    speciesId = targetSpeciesId,
                    officialDrugDose = officialDose,
                    userDrugDose = userDose,
                    isRemovable = isRemovable,
                    meatWithdrawalSpec = meatWithdrawalSpec,
                    milkWithdrawalSpec = milkWithdrawalSpec,
                    offLabelDrugSpec = offLabelDrugSpec,
                    timeStamp = LocalDateTime.now()
                )
                val addedDrug = drugRepository.queryDrugById(drugId)
                eventsChannel.send(SaveSucceededEvent(drug = requireNotNull(addedDrug)))
                clearData()
            } catch(ex: Exception) {
                errorReportChannel.send(
                    ErrorReport(
                        action = "Add Drug",
                        summary = buildString {
                            append("drugTypeId=${drugTypeId}, ")
                            append("tradeDrugName=${tradeName}, ")
                            append("genericDrugName=${genericName}, ")
                            append("speciesId=${targetSpeciesId}, ")
                            append("officialDrugDose=${officialDose}, ")
                            append("userDrugDose=${userDose}, ")
                            append("isRemovable=${isRemovable}, ")
                            append("meatWithdrawal={${meatWithdrawalSpec?.summarizeForErrorReport()}}, ")
                            append("milkWithdrawal={${milkWithdrawalSpec?.summarizeForErrorReport()}}, ")
                            append("offLabelDrugSpec={${offLabelDrugSpec?.summarizeForErrorReport()}}")
                        },
                        error = ex
                    )
                )
            }
        }
    }

    private fun clearData() {
        tradeDrugName = ""
        genericDrugName = ""
        meatWithdrawal = null
        userMeatWithdrawal = null
        milkWithdrawal = null
        userMilkWithdrawal = null
        officialDrugDose = ""
        userDrugDose = ""
        _selectedDrugType.update { null }
        _isDrugRemovable.update { false }
        _selectedMeatWithdrawalUnits.update { null }
        _selectedMilkWithdrawalUnits.update { null }
        _isAddingOffLabelDose.update { false }
        _selectedOffLabelVetContact.update { null }
        _selectedSpeciesForOffLabelDosage.update { null }
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
