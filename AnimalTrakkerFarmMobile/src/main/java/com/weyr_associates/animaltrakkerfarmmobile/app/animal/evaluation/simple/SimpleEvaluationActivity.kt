package com.weyr_associates.animaltrakkerfarmmobile.app.animal.evaluation.simple

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.AnimalDialogs
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.add.AddAnimal
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.alert.AddAnimalAlert
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.alert.ShowAlertButtonPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.evaluation.AnimalEvaluationPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.evaluation.EvaluationEditorPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.evaluation.EvaluationFieldId
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.lookup.LookupAnimalInfo
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.lookup.LookupAnimalInfo.AnimalInfoState
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.collectLatestOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.observeOneTimeEventsOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.ItemSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.core.widget.TopButtonBar
import com.weyr_associates.animaltrakkerfarmmobile.app.device.eid.EIDReaderConnection
import com.weyr_associates.animaltrakkerfarmmobile.app.device.scale.ScaleDeviceConnection
import com.weyr_associates.animaltrakkerfarmmobile.app.device.scale.ScaleScanResult
import com.weyr_associates.animaltrakkerfarmmobile.app.permissions.RequiredPermissionsWatcher
import com.weyr_associates.animaltrakkerfarmmobile.app.reporting.errors.observeErrorReports
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.AnimalRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.DefaultSettingsRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.EvaluationRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.select.SelectAnimal
import com.weyr_associates.animaltrakkerfarmmobile.app.select.optionalEvaluationItemSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.ActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseManager
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ActivitySimpleEvaluationBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.ItemEntry
import com.weyr_associates.animaltrakkerfarmmobile.model.SavedEvaluation
import kotlinx.coroutines.flow.map

class SimpleEvaluationActivity : AppCompatActivity() {

    companion object {

        @JvmStatic
        fun newIntent(context: Context, savedEvaluationId: EntityId, allowLoadEvaluation: Boolean = false): Intent {
            return Intent(context, SimpleEvaluationActivity::class.java).apply {
                putExtra(EXTRA_SAVED_EVALUATION_ID, savedEvaluationId)
                putExtra(EXTRA_ALLOW_LOAD_EVALUATION, allowLoadEvaluation)
            }
        }

        private const val EXTRA_SAVED_EVALUATION_ID = "EXTRA_SAVED_EVALUATION_ID"
        private const val EXTRA_ALLOW_LOAD_EVALUATION = "EXTRA_ALLOW_LOAD_EVALUATION"
    }

    private val savedEvaluationId: EntityId by lazy {
        intent?.getParcelableExtra<EntityId>(EXTRA_SAVED_EVALUATION_ID)
            .takeIf { it != null && it.isValid }
                ?: throw IllegalStateException("Saved evaluation ID must be provided.")
    }

    private val allowLoadEvaluation: Boolean by lazy {
        intent.getBooleanExtra(EXTRA_ALLOW_LOAD_EVALUATION, false)
    }

    private val viewModel: SimpleEvaluationViewModel by viewModels {
        ViewModelFactory(this, savedEvaluationId)
    }

    private val binding by lazy {
        ActivitySimpleEvaluationBinding.inflate(layoutInflater)
    }

    private val requiredPermissionsWatcher = RequiredPermissionsWatcher(this)
    private lateinit var eidReaderConnection: EIDReaderConnection
    private lateinit var scaleDeviceConnection: ScaleDeviceConnection

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
            addAnimalAlertLauncher = this@SimpleEvaluationActivity.addAnimalAlertLauncher
            onAddAnimalWithEIDClicked = { eidNumber ->
                AnimalDialogs.promptToAddAnimalWithEID(
                    this@SimpleEvaluationActivity,
                    eidNumber,
                    addAndSelectAnimalLauncher
                )
            }
        }
    }

    private val evaluationEditorPresenter by lazy {
        EvaluationEditorPresenter(this, binding.animalEvaluation.evaluationEditor).also {
            it.onScanWeightForTrait = { evaluationFieldId ->
                scaleDeviceConnection.scanWeight(evaluationFieldId.name)
            }
            it.onEditWeightForTrait = { evaluationFieldId, currentWeight ->
                AnimalDialogs.manuallyEnterAnimalWeight(this, currentWeight) { newWeight ->
                    applyWeightToEvaluationField(evaluationFieldId, newWeight)
                }
            }
        }
    }

    private var selectEvaluationsPresenter: ItemSelectionPresenter<ItemEntry>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupTitle()

        eidReaderConnection = EIDReaderConnection(this, lifecycle)
            .also { lifecycle.addObserver(it) }

        scaleDeviceConnection = ScaleDeviceConnection(this, lifecycle)
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

        if (allowLoadEvaluation) {
            selectEvaluationsPresenter = optionalEvaluationItemSelectionPresenter(
                button = binding.spinnerEvaluationSelection
            ) { evalItem ->
                viewModel.selectEvaluation(evalItem)
            }.also {
                it.bindToFlow(this, lifecycleScope, viewModel.selectedEvaluation)
            }
            binding.buttonLoadEvaluation.setOnClickListener {
                viewModel.loadSelectedEvaluation()
            }
            collectLatestOnStart(viewModel.canLoadEvaluation) { canLoad ->
                binding.buttonLoadEvaluation.isEnabled = canLoad
            }
            collectLatestOnStart(
                viewModel.animalInfoState.map {
                    it is AnimalInfoState.Loaded
                }
            ) { showEvalSelection ->
                binding.containerSelectEvaluation.isVisible = showEvalSelection
            }
        }

        collectLatestOnStart(eidReaderConnection.isScanningForEID) {
            binding.buttonPanelTop.showScanningEID = it
        }
        collectLatestOnStart(eidReaderConnection.deviceConnectionState) {
            binding.buttonPanelTop.updateEIDReaderConnectionState(it)
        }
        observeOneTimeEventsOnStart(eidReaderConnection.onEIDScanned, ::onEIDScanned)
        observeOneTimeEventsOnStart(scaleDeviceConnection.onWeightScanned, ::onWeightScanned)
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

        evaluationEditorPresenter.bindToEditor(viewModel.evaluationEditor)

        lifecycle.addObserver(requiredPermissionsWatcher)
    }

    private fun setupTitle() {
        when (savedEvaluationId) {
            SavedEvaluation.ID_SIMPLE_SORT -> {
                setTitle(R.string.title_activity_simple_evaluation_sort_animal)
            }
            SavedEvaluation.ID_SIMPLE_LAMBING -> {
                setTitle(R.string.title_activity_simple_evaluation_simple_lambing)
            }
            SavedEvaluation.ID_SIMPLE_BIRTHS -> {
                setTitle(R.string.title_activity_simple_evaluation_simple_births)
            }
            SavedEvaluation.ID_OPTIMAL_LIVESTOCK_EWE_ULTRASOUND -> {
                setTitle(R.string.title_activity_opt_livestock_ewe_ultrasound)
            }
            else -> Unit
        }
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

    private fun handleEvent(event: SimpleEvaluationViewModel.Event) {
        when (event) {
            SimpleEvaluationViewModel.AnimalRequiredToBeAlive -> {
                LookupAnimalInfo.Dialogs.showAnimalRequiredToBeAlive(this, viewModel)
            }
            is SimpleEvaluationViewModel.AnimalSexMismatch -> {
                LookupAnimalInfo.Dialogs.showAnimalSexMismatch(
                    this,
                    event.requiredSex,
                    viewModel
                )
            }
            is SimpleEvaluationViewModel.AnimalSpeciesOrSexMismatch -> {
                LookupAnimalInfo.Dialogs.showAnimalSexOrSpeciesMismatch(
                    this,
                    event.requiredSexId,
                    event.sexName,
                    event.speciesName,
                    viewModel
                )
            }
            SimpleEvaluationViewModel.UpdateDatabaseSuccess -> {
                Toast.makeText(
                    this,
                    R.string.toast_evaluation_saved,
                    Toast.LENGTH_LONG
                ).show()
            }
            SimpleEvaluationViewModel.UpdateDatabaseError -> {
                Toast.makeText(
                    this,
                    R.string.toast_evaluation_save_error,
                    Toast.LENGTH_LONG
                ).show()
            }
            is SimpleEvaluationViewModel.AnimalAlertEvent -> {
                AnimalDialogs.showAnimalAlert(this, event.alerts)
            }
        }
    }

    private fun onEIDScanned(eidNumber: String) {
        viewModel.lookupAnimalInfoByEIDNumber(eidNumber)
    }

    private fun onWeightScanned(scanResult: ScaleScanResult) {
        val evaluationFieldId = EvaluationFieldId.optValueOf(scanResult.reason)
        if (evaluationFieldId != null) {
            applyWeightToEvaluationField(evaluationFieldId, scanResult.weight)
        }
    }

    private fun applyWeightToEvaluationField(evaluationFieldId: EvaluationFieldId, weight: Float?) {
        when(evaluationFieldId) {
            EvaluationFieldId.TRAIT_11 -> viewModel.evaluationEditor.setTrait11(weight)
            EvaluationFieldId.TRAIT_12 -> viewModel.evaluationEditor.setTrait12(weight)
            EvaluationFieldId.TRAIT_13 -> viewModel.evaluationEditor.setTrait13(weight)
            EvaluationFieldId.TRAIT_14 -> viewModel.evaluationEditor.setTrait14(weight)
            EvaluationFieldId.TRAIT_15 -> viewModel.evaluationEditor.setTrait15(weight)
            else -> Unit
        }
    }
}

private class ViewModelFactory(context: Context, private val savedEvaluationId: EntityId) : ViewModelProvider.Factory {

    private val appContext = context.applicationContext

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            SimpleEvaluationViewModel::class.java -> {
                val databaseHandler = DatabaseManager.getInstance(appContext)
                    .createDatabaseHandler()
                val defaultSettingsRepo = DefaultSettingsRepositoryImpl(
                    databaseHandler, ActiveDefaultSettings.from(appContext)
                )
                val animalRepo = AnimalRepositoryImpl(databaseHandler, defaultSettingsRepo)
                @Suppress("UNCHECKED_CAST")
                SimpleEvaluationViewModel(
                    databaseHandler = databaseHandler,
                    savedEvaluationId = savedEvaluationId,
                    animalRepo = animalRepo,
                    evaluationRepo = EvaluationRepositoryImpl(databaseHandler)
                ) as T
            }
            else -> throw IllegalStateException("${modelClass.simpleName} is not supported.")
        }
    }
}
