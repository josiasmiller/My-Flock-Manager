package com.weyr_associates.animaltrakkerfarmmobile.app.animal.care.drugs

import android.app.Activity
import android.content.Context
import android.content.Intent
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
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.care.drugs.AddDrugViewModel.Field
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.collectLatestOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.observeOneTimeEventsOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.ItemSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.reporting.errors.observeErrorReports
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.DefaultSettingsRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.DrugRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.SpeciesRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.select.drugTypeSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.select.optionalDateSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.select.optionalUnitsSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.select.speciesSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.select.veterinarianSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.ActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadDefaultSpecies
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadDefaultSpeciesId
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseManager
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ActivityAddDrugBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.DrugType
import com.weyr_associates.animaltrakkerfarmmobile.model.Species
import com.weyr_associates.animaltrakkerfarmmobile.model.UnitOfMeasure
import com.weyr_associates.animaltrakkerfarmmobile.model.VetContact
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class AddDrugActivity : AppCompatActivity() {

    companion object {
        fun newIntentToAdd(context: Context) = Intent(context, AddDrugActivity::class.java)
        fun newIntentToAddAndSelect(context: Context) = Intent(context, AddDrugActivity::class.java).apply {
            action = AddDrug.ACTION_ADD_DRUG_AND_SELECT
        }
        private const val REQUEST_KEY_MEAT_WITHDRAWAL_UNITS_SELECTION = "REQUEST_KEY_MEAT_WITHDRAWAL_UNITS_SELECTION"
        private const val REQUEST_KEY_MILK_WITHDRAWAL_UNITS_SELECTION = "REQUEST_KEY_MILK_WITHDRAWAL_UNITS_SELECTION"
    }

    private val binding by lazy {
        ActivityAddDrugBinding.inflate(layoutInflater)
    }

    private val viewModel by viewModels<AddDrugViewModel> {
        ViewModelFactory(this)
    }

    private lateinit var drugTypeSelectionPresenter: ItemSelectionPresenter<DrugType>
    private lateinit var speciesForDosagePresenter: ItemSelectionPresenter<Species>
    private lateinit var meatWithdrawalUnitsPresenter: ItemSelectionPresenter<UnitOfMeasure>
    private lateinit var milkWithdrawalUnitsPresenter: ItemSelectionPresenter<UnitOfMeasure>
    private lateinit var vetContactSelectionPresenter: ItemSelectionPresenter<VetContact>
    private lateinit var speciesForOffLabelDosagePresenter: ItemSelectionPresenter<Species>
    private lateinit var endOffLabelUseDateSelectionPresenter: ItemSelectionPresenter<LocalDate>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.buttonAdd.setOnClickListener {
            viewModel.save()
        }
        binding.inputTradeName.addTextChangedListener {
            viewModel.tradeDrugName = it.toString()
        }
        binding.inputGenericName.addTextChangedListener {
            viewModel.genericDrugName = it.toString()
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
        binding.switchIsRemovable.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setIsRemovable(isChecked)
        }
        binding.switchIsAddingOffLabelDose.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setIsAddingOffLabelDose(isChecked)
        }
        binding.inputOffLabelDose.addTextChangedListener {
            viewModel.offLabelDrugDose = it.toString()
        }
        binding.inputOffLabelNotes.addTextChangedListener {
            viewModel.offLabelDrugNotes = it.toString()
        }
        drugTypeSelectionPresenter = drugTypeSelectionPresenter(
            button = binding.spinnerDrugTypeSelection
        ) { drugType ->
            viewModel.selectDrugType(drugType)
        }.also {
            it.bindToFlow(this, lifecycleScope, viewModel.selectedDrugType)
        }
        speciesForDosagePresenter = speciesSelectionPresenter(
            button = binding.spinnerSpinnerSpeciesForDosage
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
        vetContactSelectionPresenter = veterinarianSelectionPresenter(
            button = binding.spinnerVetContactSelection
        ) { vetContact ->
            viewModel.selectVeterinarian(vetContact)
        }.also {
            it.bindToFlow(this, lifecycleScope, viewModel.selectedOffLabelVetContact)
        }
        speciesForOffLabelDosagePresenter = speciesSelectionPresenter(
            button = binding.spinnerOffLabelTargetSpecies
        ) { species ->
            viewModel.selectSpeciesForOffLabelDosage(species)
        }.also {
            it.bindToFlow(this, lifecycleScope, viewModel.selectedSpeciesForOffLabelDosage)
        }
        endOffLabelUseDateSelectionPresenter = optionalDateSelectionPresenter(
            button = binding.spinnerEndUseDateSelection
        ) { endDate ->
            viewModel.updateOffLabelEndUseDate(endDate)
        }.also {
            it.bindToFlow(this, lifecycleScope, viewModel.selectedOffLabelEndUseDate)
        }
        collectLatestOnStart(viewModel.canSave) { canSave ->
            binding.buttonAdd.isEnabled = canSave
        }
        collectLatestOnStart(viewModel.isDrugRemovable) { isRemovable ->
            binding.switchIsRemovable.isChecked = isRemovable
        }
        collectLatestOnStart(viewModel.isAddingOffLabelDose) { isAddingOffLabel ->
            binding.switchIsAddingOffLabelDose.isChecked = isAddingOffLabel
            binding.spinnerVetContactSelection.isEnabled = isAddingOffLabel
            binding.spinnerOffLabelTargetSpecies.isEnabled = isAddingOffLabel
            binding.spinnerEndUseDateSelection.isEnabled = isAddingOffLabel
            binding.inputOffLabelDose.isEnabled = isAddingOffLabel
            binding.inputOffLabelNotes.isEnabled = isAddingOffLabel
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

    private fun handleEvent(event: AddDrugViewModel.Event) {
        when (event) {
            is AddDrugViewModel.FieldValueChanged -> handleFieldValueChanged(event)
            is AddDrugViewModel.SaveSucceededEvent -> {
                Toast.makeText(
                    this,
                    R.string.toast_add_drug_succeeded,
                    Toast.LENGTH_SHORT
                ).show()
                if (intent.action == AddDrug.ACTION_ADD_DRUG_AND_SELECT) {
                    setResult(Activity.RESULT_OK, Intent().apply {
                        putExtra(AddDrug.EXTRA_RESULTING_DRUG, event.drug)
                    })
                    finish()
                } else {
                    binding.containerContentScroll.fullScroll(View.FOCUS_UP)
                }
            }
        }
    }

    private fun handleFieldValueChanged(event: AddDrugViewModel.FieldValueChanged) {
        if (event.isAffected(Field.TRADE_NAME)) {
            binding.inputTradeName.setText(viewModel.tradeDrugName)
        }
        if (event.isAffected(Field.GENERIC_NAME)) {
            binding.inputGenericName.setText(viewModel.genericDrugName)
        }
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
        if (event.isAffected(Field.OFF_LABEL_DOSE)) {
            binding.inputOffLabelDose.setText(
                viewModel.offLabelDrugDose
            )
        }
        if (event.isAffected(Field.OFF_LABEL_NOTES)) {
            binding.inputOffLabelNotes.setText(
                viewModel.offLabelDrugNotes
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
                        databaseHandler, ActiveDefaultSettings.from(appContext))
                )
            )
            val loadDefaultSpecies = LoadDefaultSpecies(
                loadDefaultSpeciesId = loadDefaultSpeciesId,
                speciesRepository = speciesRepo
            )
            @Suppress("UNCHECKED_CAST")
            return AddDrugViewModel(
                drugRepository,
                loadDefaultSpecies
            ) as T
        }
    }
}
