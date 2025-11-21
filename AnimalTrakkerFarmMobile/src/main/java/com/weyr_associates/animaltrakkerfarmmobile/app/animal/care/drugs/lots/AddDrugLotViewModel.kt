package com.weyr_associates.animaltrakkerfarmmobile.app.animal.care.drugs.lots

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weyr_associates.animaltrakkerfarmmobile.app.core.ErrorReport
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.coroutines.flow.combine7
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.DrugRepository
import com.weyr_associates.animaltrakkerfarmmobile.model.Drug
import com.weyr_associates.animaltrakkerfarmmobile.model.UnitOfMeasure
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime

class AddDrugLotViewModel(
    private val drugRepository: DrugRepository
) : ViewModel() {

    enum class Field {
        LOT,
        COST,
        AMOUNT_PURCHASED,
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

    private val _drugLot = MutableStateFlow("")
    var drugLot: String
        get() = _drugLot.value
        set(value) { _drugLot.update { value } }

    private val _drugCost = MutableStateFlow<Float?>(null)
    var drugCost: Float?
        get() = _drugCost.value
        set(value) { _drugCost.update { value } }

    private val _selectedCurrency = MutableStateFlow<UnitOfMeasure?>(null)
    val selectedCurrency = _selectedCurrency.asStateFlow()

    private val _amountPurchased = MutableStateFlow("")
    var amountPurchased: String
        get() = _amountPurchased.value
        set(value) { _amountPurchased.update { value } }

    private val _selectedPurchaseDate = MutableStateFlow<LocalDate?>(null)
    val selectedPurchaseDate = _selectedPurchaseDate.asStateFlow()

    private val _selectedExpirationDate = MutableStateFlow<LocalDate?>(null)
    val selectedExpirationDate = _selectedExpirationDate.asStateFlow()

    private val eventsChannel = Channel<Event>()
    val events = eventsChannel.receiveAsFlow()

    private val errorReportChannel = Channel<ErrorReport>()
    val errorReportFlow = errorReportChannel.receiveAsFlow()

    val canSave = combine7(
        _selectedDrug,
        _drugLot, _drugCost, _selectedCurrency, _amountPurchased,
        _selectedPurchaseDate, _selectedExpirationDate
    ) { drugType, lot, cost, currency, amount,
        purchaseDate, expirationDate ->

        val requiredValuesPresent = drugType != null && lot.isNotBlank()

        val isPurchaseCostValid = (cost == null && currency == null) ||
                (cost != null && 0.0f < cost && currency != null)

        requiredValuesPresent && isPurchaseCostValid

    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun selectDrug(drug: Drug) {
        _selectedDrug.update { drug }
    }

    fun selectCurrency(currency: UnitOfMeasure) {
        if (currency.type.id == UnitOfMeasure.Type.ID_CURRENCY) {
            _selectedCurrency.update { currency }
        }
    }

    fun selectPurchaseDate(purchaseDate: LocalDate) {
        _selectedPurchaseDate.update { purchaseDate }
    }

    fun selectExpirationDate(expirationDate: LocalDate) {
        _selectedExpirationDate.update { expirationDate }
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
        val drugLot = drugLot.trim().takeIf { it.isNotBlank() } ?: return
        val expirationDate = selectedExpirationDate.value ?: return
        val drugCost = drugCost
        val costCurrencyId = selectedCurrency.value?.id
        val amountPurchased = amountPurchased.trim().takeIf { it.isNotBlank() } ?: ""
        val purchaseDate = selectedPurchaseDate.value

        withContext(Dispatchers.IO) {
            try {
                drugRepository.addDrugLot(
                    drugId = drugId,
                    drugLot = drugLot,
                    expirationDate = expirationDate,
                    cost = drugCost,
                    currencyUnitsId = costCurrencyId,
                    amountPurchased = amountPurchased,
                    purchaseDate = purchaseDate,
                    timeStamp = LocalDateTime.now()
                )
                eventsChannel.send(SaveSucceededEvent)
                clearData()
            } catch(ex: Exception) {
                errorReportChannel.send(
                    ErrorReport(
                        action = "Add Drug Lot",
                        summary = buildString {
                            append("drugId=${drugId}, ")
                            append("drugLot=${drugLot}, ")
                            append("expirationDate=${expirationDate}, ")
                            append("cost=${drugCost}, ")
                            append("currencyUnitsId=${costCurrencyId}, ")
                            append("amountPurchase=${amountPurchased}, ")
                            append("purchaseDate=${purchaseDate}")
                        },
                        error = ex
                    )
                )
            }
        }
    }

    private fun clearData() {
        drugLot = ""
        drugCost = null
        amountPurchased = ""
        _selectedDrug.update { null }
        _selectedCurrency.update { null }
        _selectedPurchaseDate.update { null }
        _selectedExpirationDate.update { null }
        viewModelScope.launch {
            eventsChannel.send(
                FieldValueChanged()
            )
        }
    }
}
