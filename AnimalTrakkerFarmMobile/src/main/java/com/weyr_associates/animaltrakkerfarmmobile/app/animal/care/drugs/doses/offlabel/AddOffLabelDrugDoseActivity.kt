package com.weyr_associates.animaltrakkerfarmmobile.app.animal.care.drugs.doses.offlabel

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
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.care.drugs.doses.offlabel.AddOffLabelDrugDoseViewModel.Field
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.collectLatestOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.observeOneTimeEventsOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.ItemSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.reporting.errors.observeErrorReports
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.DefaultSettingsRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.DrugRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.SpeciesRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.select.drugSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.select.optionalDateSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.select.speciesSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.select.veterinarianSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.ActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadDefaultSpecies
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadDefaultSpeciesId
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseManager
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ActivityAddOffLabelDrugDoseBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.Drug
import com.weyr_associates.animaltrakkerfarmmobile.model.Species
import com.weyr_associates.animaltrakkerfarmmobile.model.VetContact
import java.time.LocalDate

class AddOffLabelDrugDoseActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityAddOffLabelDrugDoseBinding.inflate(layoutInflater)
    }

    private val viewModel by viewModels<AddOffLabelDrugDoseViewModel> {
        ViewModelFactory(this)
    }

    private lateinit var drugSelectionPresenter: ItemSelectionPresenter<Drug>
    private lateinit var speciesForDosagePresenter: ItemSelectionPresenter<Species>
    private lateinit var vetContactSelectionPresenter: ItemSelectionPresenter<VetContact>
    private lateinit var speciesForOffLabelDosagePresenter: ItemSelectionPresenter<Species>
    private lateinit var endOffLabelUseDateSelectionPresenter: ItemSelectionPresenter<LocalDate>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.buttonAdd.setOnClickListener {
            viewModel.save()
        }
        binding.inputOffLabelDose.addTextChangedListener {
            viewModel.offLabelDrugDose = it.toString()
        }
        binding.inputOffLabelNotes.addTextChangedListener {
            viewModel.offLabelDrugNotes = it.toString()
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
            viewModel.selectSpeciesForDosage(species)
        }.also {
            it.bindToFlow(this, lifecycleScope, viewModel.selectedSpeciesForDosage)
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
        observeOneTimeEventsOnStart(viewModel.events, ::handleEvent)
        observeErrorReports(viewModel.errorReportFlow)
    }

    private fun handleEvent(event: AddOffLabelDrugDoseViewModel.Event) {
        when (event) {
            is AddOffLabelDrugDoseViewModel.FieldValueChanged -> handleFieldValueChanged(event)
            is AddOffLabelDrugDoseViewModel.SaveSucceededEvent -> {
                Toast.makeText(
                    this,
                    R.string.toast_add_drug_succeeded,
                    Toast.LENGTH_SHORT
                ).show()
                binding.containerContentScroll.fullScroll(View.FOCUS_UP)
            }
        }
    }

    private fun handleFieldValueChanged(event: AddOffLabelDrugDoseViewModel.FieldValueChanged) {
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
                        databaseHandler, ActiveDefaultSettings.from(appContext)
                    )
                )
            )
            val loadDefaultSpecies = LoadDefaultSpecies(
                loadDefaultSpeciesId = loadDefaultSpeciesId,
                speciesRepository = speciesRepo
            )
            @Suppress("UNCHECKED_CAST")
            return AddOffLabelDrugDoseViewModel(
                drugRepository,
                loadDefaultSpecies
            ) as T
        }
    }
}
