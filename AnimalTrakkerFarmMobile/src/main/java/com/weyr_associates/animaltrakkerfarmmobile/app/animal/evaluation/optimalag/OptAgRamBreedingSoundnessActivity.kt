package com.weyr_associates.animaltrakkerfarmmobile.app.animal.evaluation.optimalag

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.AnimalDialogs
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.add.AddAnimal
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.alert.AddAnimalAlert
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.alert.ShowAlertButtonPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.evaluation.AnimalEvaluationPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.evaluation.EvaluationEditorPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.evaluation.tissue.TissueSampleActivity
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.lookup.LookupAnimalInfo.AnimalInfoState
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.collectLatestOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.observeOneTimeEventsOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.widget.TopButtonBar
import com.weyr_associates.animaltrakkerfarmmobile.app.device.eid.EIDReaderConnection
import com.weyr_associates.animaltrakkerfarmmobile.app.label.ExtractPrintLabelData
import com.weyr_associates.animaltrakkerfarmmobile.app.permissions.RequiredPermissionsWatcher
import com.weyr_associates.animaltrakkerfarmmobile.app.reporting.errors.observeErrorReports
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.AnimalRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.DefaultSettingsRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.EvaluationRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.select.SelectAnimal
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.ActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadDefaultIdTypeIds
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseManager
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ActivityOptAgRamBreedingSoundnessBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.Laboratory

class OptAgRamBreedingSoundnessActivity : AppCompatActivity() {

    private val viewModel: OptAgRamBreedingSoundnessViewModel by viewModels {
        ViewModelFactory(this)
    }

    private val binding by lazy {
        ActivityOptAgRamBreedingSoundnessBinding.inflate(layoutInflater)
    }

    private val showAlertButtonPresenter by lazy {
        ShowAlertButtonPresenter(this, binding.buttonPanelTop.showAlertButton)
    }

    private val selectAnimalLauncher = registerForActivityResult(SelectAnimal.Contract()) { animalId ->
        animalId?.let { viewModel.lookupAnimalInfoById(animalId) }
    }

    private val addAnimalAlertLauncher = registerForActivityResult(AddAnimalAlert.Contract()) { result ->
        if (result.success) { viewModel.lookupAnimalInfoById(result.animalId) }
    }

    private val addAndSelectAnimalLauncher = registerForActivityResult(AddAnimal.Contract()) { result ->
        result?.let { viewModel.lookupAnimalInfoById(result.animalId) }
    }

    private val animalEvaluationPresenter by lazy {
        AnimalEvaluationPresenter(binding.animalEvaluation).apply {
            addAnimalAlertLauncher = this@OptAgRamBreedingSoundnessActivity.addAnimalAlertLauncher
            onAddAnimalWithEIDClicked = { eidNumber ->
                AnimalDialogs.promptToAddAnimalWithEID(
                    this@OptAgRamBreedingSoundnessActivity,
                    eidNumber,
                    addAndSelectAnimalLauncher
                )
            }
        }
    }

    private val evaluationEditorPresenter by lazy {
        EvaluationEditorPresenter(this, binding.animalEvaluation.evaluationEditor)
    }

    private val requiredPermissionsWatcher = RequiredPermissionsWatcher(this)
    private lateinit var eidReaderConnection: EIDReaderConnection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        eidReaderConnection = EIDReaderConnection(this, lifecycle)
            .also { lifecycle.addObserver(it) }

        with(binding.buttonPanelTop) {
            show(TopButtonBar.UI_ALL or TopButtonBar.UI_ACTION_UPDATE_DATABASE)

            scanEIDButton.setOnClickListener {
                eidReaderConnection.toggleScanningEID()
            }

            lookupAnimalButton.setOnClickListener { onLookupAnimal() }
            clearDataButton.setOnClickListener { viewModel.clearData() }
            mainActionButton.setOnClickListener { viewModel.saveToDatabase() }
        }

        collectLatestOnStart(eidReaderConnection.isScanningForEID) {
            binding.buttonPanelTop.showScanningEID = it
        }
        collectLatestOnStart(eidReaderConnection.deviceConnectionState) {
            binding.buttonPanelTop.updateEIDReaderConnectionState(it)
        }
        observeOneTimeEventsOnStart(eidReaderConnection.onEIDScanned, ::onEIDScanned)

        binding.buttonTakeTissueSamples.setOnClickListener {
            val takeTissueSamplesInfo = viewModel.tissueSamplesInfo.value ?: return@setOnClickListener
            startActivity(
                TissueSampleActivity.newIntent(
                    this@OptAgRamBreedingSoundnessActivity,
                    takeTissueSamplesInfo.animalBasicInfo,
                    titleAddition = getString(R.string.title_activity_opt_ag_ram_bse),
                    defaultLabCompanyId = Laboratory.LAB_COMPANY_ID_OPTIMAL_LIVESTOCK,
                    printLabelData = takeTissueSamplesInfo.printLabelData
                )
            )
        }

        collectLatestOnStart(viewModel.canClearData) { canClear ->
            binding.buttonPanelTop.clearDataButton.isEnabled = canClear
        }

        collectLatestOnStart(viewModel.canSaveToDatabase) { canSave ->
            binding.buttonPanelTop.mainActionButton.isEnabled = canSave
        }

        observeOneTimeEventsOnStart(viewModel.events, ::handleEvent)
        observeErrorReports(viewModel.errorReportFlow)

        collectLatestOnStart(viewModel.animalInfoState) { animalInfoState ->
            updateTopBarAnimalActions(animalInfoState)
            updateAnimalInfoDisplay(animalInfoState)
        }

        collectLatestOnStart(viewModel.tissueSamplesInfo) { tissueSamplesInfo ->
            binding.textTissueSamplePrintLabel.text = tissueSamplesInfo?.printLabelData?.labelText
        }

        collectLatestOnStart(viewModel.canTakeTissueSamples) { canTakeSamples ->
            binding.buttonTakeTissueSamples.isEnabled = canTakeSamples
        }

        evaluationEditorPresenter.bindToEditor(viewModel.evaluationEditor)

        lifecycle.addObserver(requiredPermissionsWatcher)
    }

    private fun onLookupAnimal() {
        selectAnimalLauncher.launch(null)
    }

    private fun updateTopBarAnimalActions(animalInfoState: AnimalInfoState) {
        showAlertButtonPresenter.animalInfoState = animalInfoState
    }

    private fun updateAnimalInfoDisplay(animalInfoState: AnimalInfoState) {
        animalEvaluationPresenter.animalInfoState = animalInfoState
    }

    private fun handleEvent(event: OptAgRamBreedingSoundnessViewModel.Event) {
        when (event) {
            is OptAgRamBreedingSoundnessViewModel.AnimalSpeciesOrSexMismatch -> {
                AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_title_opt_ag_ram_bse_species_sex_mismatch)
                    .setMessage(
                        getString(
                            R.string.dialog_message_opt_ag_ram_bse_species_sex_mismatch,
                            event.speciesName,
                            event.sexName
                        )
                    )
                    .setPositiveButton(R.string.ok) { _, _ -> /*NO-OP*/ }
                    .setOnDismissListener { viewModel.resetAnimalInfo() }
                    .create()
                    .show()
            }
            OptAgRamBreedingSoundnessViewModel.UpdateDatabaseSuccess -> {
                Toast.makeText(
                    this,
                    R.string.toast_evaluation_saved,
                    Toast.LENGTH_LONG
                ).show()
            }
            is OptAgRamBreedingSoundnessViewModel.AnimalAlertEvent -> {
                AnimalDialogs.showAnimalAlert(this, event.alerts)
            }
        }
    }

    private fun onEIDScanned(eidNumber: String) {
        viewModel.lookupAnimalInfoByEIDNumber(eidNumber)
    }
}

private class ViewModelFactory(context: Context) : ViewModelProvider.Factory {

    private val appContext = context.applicationContext

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            OptAgRamBreedingSoundnessViewModel::class.java -> {
                val databaseHandler = DatabaseManager.getInstance(appContext)
                    .createDatabaseHandler()
                val defaultSettingsRepo = DefaultSettingsRepositoryImpl(
                    databaseHandler, ActiveDefaultSettings.from(appContext)
                )
                val animalRepo = AnimalRepositoryImpl(databaseHandler, defaultSettingsRepo)
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(appContext)
                val activeDefaultSettings = ActiveDefaultSettings(sharedPreferences)
                val loadActiveDefaultSettings = LoadActiveDefaultSettings(
                    activeDefaultSettings,
                    defaultSettingsRepo
                )
                val loadDefaultIdTypeIds = LoadDefaultIdTypeIds(loadActiveDefaultSettings)
                @Suppress("UNCHECKED_CAST")
                OptAgRamBreedingSoundnessViewModel(
                    databaseHandler = databaseHandler,
                    animalRepo = animalRepo,
                    evaluationRepo = EvaluationRepositoryImpl(databaseHandler),
                    extractPrintLabelData = ExtractPrintLabelData(
                        sharedPreferences,
                        loadDefaultIdTypeIds
                    )
                ) as T
            }
            else -> throw IllegalStateException("${modelClass.simpleName} is not supported.")
        }
    }
}
