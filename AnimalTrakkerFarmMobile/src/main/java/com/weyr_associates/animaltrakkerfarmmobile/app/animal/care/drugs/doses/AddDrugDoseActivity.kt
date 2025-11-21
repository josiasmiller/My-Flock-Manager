package com.weyr_associates.animaltrakkerfarmmobile.app.animal.care.drugs.doses

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
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.care.drugs.doses.AddDrugDoseViewModel.Field
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.collectLatestOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.observeOneTimeEventsOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.ItemSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.reporting.errors.observeErrorReports
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.DefaultSettingsRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.DrugRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.SpeciesRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.select.drugSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.select.optionalUnitsSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.select.speciesSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.ActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadDefaultSpecies
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadDefaultSpeciesId
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseManager
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ActivityAddDrugDoseBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.Drug
import com.weyr_associates.animaltrakkerfarmmobile.model.Species
import com.weyr_associates.animaltrakkerfarmmobile.model.UnitOfMeasure
import kotlinx.coroutines.flow.map

class AddDrugDoseActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_KEY_MEAT_WITHDRAWAL_UNITS_SELECTION = "REQUEST_KEY_MEAT_WITHDRAWAL_UNITS_SELECTION"
        private const val REQUEST_KEY_MILK_WITHDRAWAL_UNITS_SELECTION = "REQUEST_KEY_MILK_WITHDRAWAL_UNITS_SELECTION"
    }

    private val binding by lazy {
        ActivityAddDrugDoseBinding.inflate(layoutInflater)
    }

    private val viewModel by viewModels<AddDrugDoseViewModel> {
        ViewModelFactory(this)
    }

    private lateinit var drugSelectionPresenter: ItemSelectionPresenter<Drug>
    private lateinit var speciesForDosagePresenter: ItemSelectionPresenter<Species>
    private lateinit var meatWithdrawalUnitsPresenter: ItemSelectionPresenter<UnitOfMeasure>
    private lateinit var milkWithdrawalUnitsPresenter: ItemSelectionPresenter<UnitOfMeasure>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.buttonAdd.setOnClickListener {
            viewModel.save()
        }
        binding.inputOfficialDose.addTextChangedListener {
            viewModel.officialDrugDose = it.toString()
        }
        binding.inputUserDose.addTextChangedListener {
            viewModel.userDrugDose = it.toString()
        }
        binding.inputMeatWithdrawal.addTextChangedListener {
            viewModel.meatWithdrawal = it.toString().toIntOrNull()
        }
        binding.inputUserMeatWithdrawal.addTextChangedListener {
            viewModel.userMeatWithdrawal = it.toString().toIntOrNull()
        }
        binding.inputMilkWithdrawal.addTextChangedListener {
            viewModel.milkWithdrawal = it.toString().toIntOrNull()
        }
        binding.inputUserMilkWithdrawal.addTextChangedListener {
            viewModel.userMilkWithdrawal = it.toString().toIntOrNull()
        }
        drugSelectionPresenter = drugSelectionPresenter(
            button = binding.spinnerDrugSelection
        ) { drug ->
            viewModel.selectDrug(drug)
        }.also {
            it.bindToFlow(this, lifecycleScope, viewModel.selectedDrug)
        }
        speciesForDosagePresenter = speciesSelectionPresenter(
            button = binding.spinnerSpeciesForDosage
        ) { species ->
            viewModel.selectSpeciesForDosage(species)
        }.also {
            it.bindToFlow(this, lifecycleScope, viewModel.selectedSpeciesForDosage)
        }
        meatWithdrawalUnitsPresenter = optionalUnitsSelectionPresenter(
            button = binding.spinnerMeatWithdrawalUnitsSelection,
            requestKey = REQUEST_KEY_MEAT_WITHDRAWAL_UNITS_SELECTION,
            unitsTypeId = UnitOfMeasure.Type.ID_TIME
        ) { units ->
            viewModel.selectMeatWithdrawalUnits(units)
        }.also {
            it.bindToFlow(this, lifecycleScope, viewModel.selectedMeatWithdrawalUnits)
        }
        milkWithdrawalUnitsPresenter = optionalUnitsSelectionPresenter(
            button = binding.spinnerMilkWithdrawalUnitsSelection,
            requestKey = REQUEST_KEY_MILK_WITHDRAWAL_UNITS_SELECTION,
            unitsTypeId = UnitOfMeasure.Type.ID_TIME
        ) { units ->
            viewModel.selectMilkWithdrawalUnits(units)
        }.also {
            it.bindToFlow(this, lifecycleScope, viewModel.selectedMilkWithdrawalUnits)
        }
        collectLatestOnStart(viewModel.canSave) { canSave ->
            binding.buttonAdd.isEnabled = canSave
        }
        collectLatestOnStart(viewModel.selectedMeatWithdrawalUnits.map { it != null }) { expectsWithdrawal ->
            binding.inputMeatWithdrawal.isEnabled = expectsWithdrawal
            binding.inputUserMeatWithdrawal.isEnabled = expectsWithdrawal
        }
        collectLatestOnStart(viewModel.selectedMilkWithdrawalUnits.map { it != null }) { expectsWithdrawal ->
            binding.inputMilkWithdrawal.isEnabled = expectsWithdrawal
            binding.inputUserMilkWithdrawal.isEnabled = expectsWithdrawal
        }
        observeOneTimeEventsOnStart(viewModel.events, ::handleEvent)
        observeErrorReports(viewModel.errorReportFlow)
    }

    private fun handleEvent(event: AddDrugDoseViewModel.Event) {
        when (event) {
            is AddDrugDoseViewModel.FieldValueChanged -> handleFieldValueChanged(event)
            AddDrugDoseViewModel.SaveSucceededEvent -> {
                Toast.makeText(
                    this,
                    R.string.toast_add_drug_dose_succeeded,
                    Toast.LENGTH_SHORT
                ).show()
                binding.containerContentScroll.fullScroll(View.FOCUS_UP)
            }
        }
    }

    private fun handleFieldValueChanged(event: AddDrugDoseViewModel.FieldValueChanged) {
        if (event.isAffected(Field.OFFICIAL_DOSE)) {
            binding.inputOfficialDose.setText(viewModel.officialDrugDose)
        }
        if (event.isAffected(Field.USER_DOSE)) {
            binding.inputUserDose.setText(viewModel.userDrugDose)
        }
        if (event.isAffected(Field.MEAT_WITHDRAWAL)) {
            binding.inputMeatWithdrawal.setText(
                viewModel.meatWithdrawal?.toString() ?: ""
            )
        }
        if (event.isAffected(Field.USER_MEAT_WITHDRAWAL)) {
            binding.inputUserMeatWithdrawal.setText(
                viewModel.userMeatWithdrawal?.toString() ?: ""
            )
        }
        if (event.isAffected(Field.MILK_WITHDRAWAL)) {
            binding.inputMilkWithdrawal.setText(
                viewModel.milkWithdrawal?.toString() ?: ""
            )
        }
        if (event.isAffected(Field.USER_MILK_WITHDRAWAL)) {
            binding.inputUserMilkWithdrawal.setText(
                viewModel.userMilkWithdrawal?.toString() ?: ""
            )
        }
    }

    private class ViewModelFactory(context: Context) : ViewModelProvider.Factory {

        private val appContext = context.applicationContext

        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            val databaseHandler = DatabaseManager.getInstance(appContext)
                .createDatabaseHandler()
            val drugRepository = DrugRepositoryImpl(databaseHandler)
            val speciesRepo = SpeciesRepositoryImpl(databaseHandler)
            val loadDefaultSpeciesId = LoadDefaultSpeciesId(
                LoadActiveDefaultSettings(
                    ActiveDefaultSettings.from(appContext),
                    DefaultSettingsRepositoryImpl(
                        databaseHandler, ActiveDefaultSettings.from(appContext)
                    )
                )
            )
            val loadDefaultSpecies = LoadDefaultSpecies(
                loadDefaultSpeciesId = loadDefaultSpeciesId,
                speciesRepository = speciesRepo
            )
            @Suppress("UNCHECKED_CAST")
            return AddDrugDoseViewModel(
                drugRepository,
                loadDefaultSpecies
            ) as T
        }
    }
}
