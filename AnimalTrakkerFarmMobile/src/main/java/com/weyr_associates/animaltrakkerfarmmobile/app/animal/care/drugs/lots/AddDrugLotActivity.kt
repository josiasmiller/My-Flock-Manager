package com.weyr_associates.animaltrakkerfarmmobile.app.animal.care.drugs.lots

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.care.drugs.AddDrug
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.care.drugs.lots.AddDrugLotViewModel.Field
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.collectLatestOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.observeOneTimeEventsOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.ItemSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.reporting.errors.observeErrorReports
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.DrugRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.select.dateSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.select.drugSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.select.unitsSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseManager
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ActivityAddDrugLotBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.Drug
import com.weyr_associates.animaltrakkerfarmmobile.model.UnitOfMeasure
import java.time.LocalDate

class AddDrugLotActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_KEY_CURRENCY_SELECTION = "REQUEST_KEY_CURRENCY_SELECTION"
        private const val REQUEST_KEY_PURCHASE_DATE_SELECTION = "REQUEST_KEY_PURCHASE_DATE_SELECTION"
        private const val REQUEST_KEY_EXPIRATION_DATE_SELECTION = "REQUEST_KEY_EXPIRATION_DATE_SELECTION"
    }

    private val binding by lazy {
        ActivityAddDrugLotBinding.inflate(layoutInflater)
    }

    private val viewModel by viewModels<AddDrugLotViewModel> {
        ViewModelFactory(this)
    }

    private lateinit var drugSelectionPresenter: ItemSelectionPresenter<Drug>
    private lateinit var currencySelectionPresenter: ItemSelectionPresenter<UnitOfMeasure>
    private lateinit var purchaseDateSelectionPresenter: ItemSelectionPresenter<LocalDate>
    private lateinit var expirationDateSelectionPresenter: ItemSelectionPresenter<LocalDate>

    private val addAndSelectDrugLauncher = registerForActivityResult(AddDrug.Contact()) { drug ->
        drug?.let { viewModel.selectDrug(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.buttonAdd.setOnClickListener {
            viewModel.save()
        }
        binding.buttonAddDrug.setOnClickListener {
            addAndSelectDrugLauncher.launch(Unit)
        }
        binding.inputDrugLot.addTextChangedListener {
            viewModel.drugLot = it.toString()
        }
        binding.inputAmountPurchased.addTextChangedListener {
            viewModel.amountPurchased = it.toString()
        }
        binding.inputCost.addTextChangedListener {
            viewModel.drugCost = it.toString().toFloatOrNull()
        }
        drugSelectionPresenter = drugSelectionPresenter(
            button = binding.spinnerDrugSelection
        ) { drug ->
            viewModel.selectDrug(drug)
        }.also {
            it.bindToFlow(this, lifecycleScope, viewModel.selectedDrug)
        }
        currencySelectionPresenter = unitsSelectionPresenter(
            button = binding.spinnerCurrencySelection,
            requestKey = REQUEST_KEY_CURRENCY_SELECTION,
            unitsTypeId = UnitOfMeasure.Type.ID_CURRENCY
        ) { currency ->
            viewModel.selectCurrency(currency)
        }.also {
            it.bindToFlow(this, lifecycleScope, viewModel.selectedCurrency)
        }
        purchaseDateSelectionPresenter = dateSelectionPresenter(
            button = binding.spinnerPurchaseDate,
            requestKey = REQUEST_KEY_PURCHASE_DATE_SELECTION
        ) { purchaseDate ->
            viewModel.selectPurchaseDate(purchaseDate)
        }.also {
            it.bindToFlow(this, lifecycleScope, viewModel.selectedPurchaseDate)
        }
        expirationDateSelectionPresenter = dateSelectionPresenter(
            button = binding.spinnerExpirationDate,
            requestKey = REQUEST_KEY_EXPIRATION_DATE_SELECTION
        ) { expirationDate ->
            viewModel.selectExpirationDate(expirationDate)
        }.also {
            it.bindToFlow(this, lifecycleScope, viewModel.selectedExpirationDate)
        }
        collectLatestOnStart(viewModel.canSave) { canSave ->
            binding.buttonAdd.isEnabled = canSave
        }
        observeOneTimeEventsOnStart(viewModel.events, ::handleEvent)
        observeErrorReports(viewModel.errorReportFlow)
    }

    private fun handleEvent(event: AddDrugLotViewModel.Event) {
        when (event) {
            is AddDrugLotViewModel.FieldValueChanged -> handleFieldValueChanged(event)
            AddDrugLotViewModel.SaveSucceededEvent -> {
                Toast.makeText(
                    this,
                    R.string.toast_add_drug_succeeded,
                    Toast.LENGTH_SHORT
                ).show()
                binding.containerContentScroll.fullScroll(View.FOCUS_UP)
            }
        }
    }

    private fun handleFieldValueChanged(event: AddDrugLotViewModel.FieldValueChanged) {
        if (event.isAffected(Field.LOT)) {
            binding.inputDrugLot.setText(viewModel.drugLot)
        }
        if (event.isAffected(Field.COST)) {
            binding.inputCost.setText(viewModel.drugCost?.toString() ?: "")
        }
        if (event.isAffected(Field.AMOUNT_PURCHASED)) {
            binding.inputAmountPurchased.setText(viewModel.amountPurchased)
        }
    }

    private class ViewModelFactory(context: Context) : ViewModelProvider.Factory {

        private val appContext = context.applicationContext

        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            val databaseHandler = DatabaseManager.getInstance(appContext)
                .createDatabaseHandler()
            val drugRepository = DrugRepositoryImpl(databaseHandler)
            @Suppress("UNCHECKED_CAST")
            return AddDrugLotViewModel(
                drugRepository
            ) as T
        }
    }
}
