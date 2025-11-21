package com.weyr_associates.animaltrakkerfarmmobile.app.animal.evaluation.tissue

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.AnimalDialogs
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.add.AddAnimal
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.alert.AddAnimalAlert
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.alert.ShowAlertButtonPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.evaluation.tissue.TissueSampleViewModel.AnimalSpeciesMismatch
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.evaluation.tissue.TissueSampleViewModel.IncompleteDataEntry
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.evaluation.tissue.TissueSampleViewModel.InputEvent
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.evaluation.tissue.TissueSampleViewModel.PrintLabelRequestedError
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.evaluation.tissue.TissueSampleViewModel.PrintLabelRequestedEvent
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.evaluation.tissue.TissueSampleViewModel.UpdateDatabaseEvent
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.lookup.LookupAnimalInfo.AnimalInfoState
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.lookup.LookupAnimalInfoPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.core.ScannerButtonPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.collectLatestOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.observeOneTimeEventsOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.ItemSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.core.widget.TopButtonBar
import com.weyr_associates.animaltrakkerfarmmobile.app.device.DeviceConnectionStatePresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.device.baacode.BaacodeReaderConnection
import com.weyr_associates.animaltrakkerfarmmobile.app.device.eid.EIDReaderConnection
import com.weyr_associates.animaltrakkerfarmmobile.app.label.ExtractPrintLabelData
import com.weyr_associates.animaltrakkerfarmmobile.app.label.PrintLabel
import com.weyr_associates.animaltrakkerfarmmobile.app.label.PrintLabelData
import com.weyr_associates.animaltrakkerfarmmobile.app.permissions.RequiredPermissionsWatcher
import com.weyr_associates.animaltrakkerfarmmobile.app.reporting.errors.observeErrorReports
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.AnimalRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.DefaultSettingsRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.LaboratoryRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.SpeciesRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.TissueSampleContainerTypeRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.TissueSampleTypeRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.TissueTestRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.select.SelectAnimal
import com.weyr_associates.animaltrakkerfarmmobile.app.select.laboratorySelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.select.optionalDateSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.select.tissueSampleContainerTypeSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.select.tissueSampleTypeSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.select.tissueTestSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.ActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadDefaultIdTypeIds
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseManager
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ActivityTissueSampleBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalBasicInfo
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.Laboratory
import com.weyr_associates.animaltrakkerfarmmobile.model.TissueSampleContainerType
import com.weyr_associates.animaltrakkerfarmmobile.model.TissueSampleType
import com.weyr_associates.animaltrakkerfarmmobile.model.TissueTest
import java.time.LocalDate

class TissueSampleActivity : AppCompatActivity() {

    companion object {

        fun newIntent(
            context: Context,
            animalBasicInfo: AnimalBasicInfo,
            titleAddition: String? = null,
            defaultLabCompanyId: EntityId? = null,
            printLabelData: PrintLabelData? = null
        ) = Intent(context, TissueSampleActivity::class.java).apply {
                action = TissueSample.ACTION_TAKE_TISSUE_SAMPLE_FOR_ANIMAL
                putExtra(TissueSample.EXTRA_ANIMAL_BASIC_INFO, animalBasicInfo)
                titleAddition?.let { putExtra(TissueSample.EXTRA_TITLE_ADDITION, it) }
                defaultLabCompanyId?.let { putExtra(TissueSample.EXTRA_DEFAULT_LAB_COMPANY_ID, it) }
                printLabelData?.let { putExtra(TissueSample.EXTRA_PRINT_LABEL_DATA, it) }
            }
    }

    private var autoPrint = false

    private val viewModel by viewModels<TissueSampleViewModel> { ViewModelFactory(this) }

    private val binding: ActivityTissueSampleBinding by lazy {
        ActivityTissueSampleBinding.inflate(layoutInflater)
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

    private val printLabelLauncher = registerForActivityResult(PrintLabel.Contract()) { success ->
        if (success) {
            startActivity(PrintLabel.newIntentToPrint(this, autoPrint))
        }
    }

    private val lookupAnimalInfoPresenter by lazy {
        LookupAnimalInfoPresenter(binding.lookupAnimalInfo).apply {
            addAnimalAlertLauncher = this@TissueSampleActivity.addAnimalAlertLauncher
            onAddAnimalWithEIDClicked = { eidNumber ->
                AnimalDialogs.promptToAddAnimalWithEID(
                    this@TissueSampleActivity, eidNumber, addAndSelectAnimalLauncher
                )
            }
        }
    }

    private lateinit var labNameSpinnerPresenter: ItemSelectionPresenter<Laboratory>

    private lateinit var sampleContainerTypeSpinnerPresenter: ItemSelectionPresenter<TissueSampleContainerType>

    private lateinit var sampleTypeSpinnerPresenter: ItemSelectionPresenter<TissueSampleType>

    private lateinit var sampleTestSpinnerPresenter: ItemSelectionPresenter<TissueTest>

    private lateinit var containerExpirationDatePresenter: ItemSelectionPresenter<LocalDate>

    private val requiredPermissionsWatcher = RequiredPermissionsWatcher(this)

    private lateinit var eidReaderConnection: EIDReaderConnection
    private lateinit var baaCodeReaderConnection: BaacodeReaderConnection
    private lateinit var baaCodeReaderStatePresenter: DeviceConnectionStatePresenter

    private val baacodeScannerButtonPresenter by lazy {
        ScannerButtonPresenter(binding.btnScanSampleContainer).apply {
            buttonText = getString(R.string.scan_sample_container_btn)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.hasExtra(TissueSample.EXTRA_TITLE_ADDITION)) {
            val titleString = getString(R.string.title_activity_tissue_sample)
            val titleAddition = intent.getStringExtra(TissueSample.EXTRA_TITLE_ADDITION)
            title = "$titleString - $titleAddition"
        }
        setContentView(binding.root)
        loadPreferences()

        with(binding.buttonPanelTop) {

            if (intent.action == TissueSample.ACTION_TAKE_TISSUE_SAMPLE_FOR_ANIMAL) {
                show(
                    TopButtonBar.UI_SHOW_ALERT or
                            TopButtonBar.UI_CLEAR_DATA or
                            TopButtonBar.UI_ACTION_UPDATE_DATABASE
                )
            } else {
                show(TopButtonBar.UI_ALL or TopButtonBar.UI_ACTION_UPDATE_DATABASE)
            }

            scanEIDButton.setOnClickListener { onScanEID() }
            lookupAnimalButton.setOnClickListener { onLookupAnimal() }
            clearDataButton.setOnClickListener { viewModel.clearData() }
            mainActionButton.setOnClickListener { viewModel.saveToDatabase() }
        }

        baaCodeReaderStatePresenter = DeviceConnectionStatePresenter(
            this@TissueSampleActivity, binding.imageBaacodeReaderStatus
        )

        labNameSpinnerPresenter = laboratorySelectionPresenter(
            binding.labNameSpinner
        ) {
            viewModel.selectLaboratory(it)
        }.also {
            it.bindToFlow(this, lifecycleScope, viewModel.selectedLaboratory)
        }

        sampleTypeSpinnerPresenter = tissueSampleTypeSelectionPresenter(
            binding.sampleTypeSpinner
        ) {
            viewModel.selectTissueSampleType(it)
        }.also {
            it.bindToFlow(this, lifecycleScope, viewModel.selectedTissueSampleType)
        }

        sampleTestSpinnerPresenter = tissueTestSelectionPresenter(
            binding.sampleTestSpinner
        ) {
            viewModel.selectTissueTestType(it)
        }.also {
            it.bindToFlow(this, lifecycleScope, viewModel.selectedTissueTestType)
        }

        sampleContainerTypeSpinnerPresenter = tissueSampleContainerTypeSelectionPresenter(
            binding.sampleContainerTypeSpinner
        ) {
            viewModel.selectTissueSampleContainerType(it)
        }.also {
            it.bindToFlow(this, lifecycleScope, viewModel.selectTissueContainerType)
        }

        containerExpirationDatePresenter = optionalDateSelectionPresenter(
            binding.containerExpDate
        ) { sampleExpirationDate ->
            viewModel.selectContainerExpirationDate(sampleExpirationDate)
        }.also {
            it.bindToFlow(this, lifecycleScope, viewModel.selectedSampleContainerExpirationDate)
        }

        collectLatestOnStart(viewModel.canClearData) { canClear ->
            binding.buttonPanelTop.clearDataButton.isEnabled = canClear
        }

        collectLatestOnStart(viewModel.canSaveToDatabase) { canSave ->
            binding.buttonPanelTop.mainActionButton.isEnabled = canSave
        }

        collectLatestOnStart(viewModel.canScanTSU) { canScan ->
            binding.btnScanSampleContainer.btnScan.isEnabled = canScan
        }

        collectLatestOnStart(viewModel.canPrintLabel) { canPrint ->
            binding.printLabelBtn.isEnabled = canPrint
        }

        observeOneTimeEventsOnStart(viewModel.events, ::handleEvent)
        observeErrorReports(viewModel.errorReportFlow)

        collectLatestOnStart(viewModel.animalInfoState) { animalInfoState ->
            updateTopBarAnimalActions(animalInfoState)
            updateAnimalInfoDisplay(animalInfoState)
        }

        binding.sampleContainerId.addTextChangedListener { viewModel.containerId = it.toString() }
        binding.sampleContainerId.setText(viewModel.containerId)

        binding.btnScanSampleContainer.btnScan.setOnClickListener { scanTSU() }
        binding.printLabelBtn.setOnClickListener { viewModel.printLabel() }

        eidReaderConnection = EIDReaderConnection(this, lifecycle)
            .also { lifecycle.addObserver(it) }

        collectLatestOnStart(eidReaderConnection.deviceConnectionState) { connectionState ->
            binding.buttonPanelTop.updateEIDReaderConnectionState(connectionState)
        }
        collectLatestOnStart(eidReaderConnection.isScanningForEID) { isScanning ->
            binding.buttonPanelTop.showScanningEID = isScanning
        }
        observeOneTimeEventsOnStart(eidReaderConnection.onEIDScanned, ::onEIDScanned)

        baaCodeReaderConnection = BaacodeReaderConnection(this, lifecycle)
            .also { lifecycle.addObserver(it) }

        collectLatestOnStart(baaCodeReaderConnection.deviceConnectionState) { connectionState ->
            baaCodeReaderStatePresenter.connectionState = connectionState
        }
        collectLatestOnStart(baaCodeReaderConnection.isScanningForBaacode) { isScanning ->
            baacodeScannerButtonPresenter.isScanning = isScanning
        }
        observeOneTimeEventsOnStart(baaCodeReaderConnection.onBaacodeScanned, ::onBAAScanned)

        lifecycle.addObserver(requiredPermissionsWatcher)
    }

    private fun onScanEID() {
        eidReaderConnection.toggleScanningEID()
    }

    private fun onLookupAnimal() {
        selectAnimalLauncher.launch(null)
    }

    private fun updateTopBarAnimalActions(animalInfoState: AnimalInfoState) {
        showAlertButtonPresenter.animalInfoState = animalInfoState
    }

    private fun updateAnimalInfoDisplay(animalInfoState: AnimalInfoState) {
        lookupAnimalInfoPresenter.animalInfoState = animalInfoState
    }

    private fun handlePrintLabelRequestedEvent(event: PrintLabelRequestedEvent) {
        printLabelLauncher.launch(PrintLabel.Request(event.printLabelData, autoPrint))
    }

    private fun loadPreferences() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(baseContext)
        autoPrint = preferences.getBoolean("autop", false)
    }

    private fun handleEvent(event: TissueSampleViewModel.Event) {
        when (event) {
            InputEvent.ContainerIdChanged -> {
                binding.sampleContainerId.setText(viewModel.containerId)
            }
            IncompleteDataEntry -> {
                showIncompleteDataEntryError()
            }
            is AnimalSpeciesMismatch -> {
                showAnimalSpeciesMismatchPrompt(
                    event.defaultSpeciesName,
                    event.animalSpeciesName
                )
            }
            is PrintLabelRequestedEvent -> {
                handlePrintLabelRequestedEvent(event)
            }
            is PrintLabelRequestedError -> {
                showEIDRequiredToPrintError()
            }
            UpdateDatabaseEvent.Success -> {
                Toast.makeText(
                    this,
                    R.string.toast_add_tissue_test_success,
                    Toast.LENGTH_SHORT
                ).show()
            }
            is TissueSampleViewModel.AnimalAlertEvent -> {
                AnimalDialogs.showAnimalAlert(this, event.alerts)
            }
        }
    }

    private fun showIncompleteDataEntryError() {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_title_incomplete_tissue_sample_entry)
            .setMessage(R.string.dialog_message_incomplete_tissue_sample_entry)
            .setPositiveButton(R.string.ok) { _, _ -> /*NO-OP*/ }
            .create()
            .show()
    }

    private fun showAnimalSpeciesMismatchPrompt(defaultSpecies: String, currentSpecies: String) {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_title_animal_species_mismatch_prompt)
            .setMessage(
                getString(
                    R.string.dialog_message_animal_species_mismatch_prompt,
                    defaultSpecies,
                    currentSpecies
                )
            )
            .setPositiveButton(R.string.yes_label) { _, _ -> /*NO-OP*/ }
            .setNegativeButton(R.string.no_label) { _, _ -> viewModel.resetAnimalInfo() }
            .create()
            .show()
    }

    private fun showEIDRequiredToPrintError() {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_title_eid_required_to_print_label)
            .setMessage(R.string.dialog_message_eid_required_to_print_label)
            .setPositiveButton(R.string.ok) { _, _ -> /*NO-OP*/ }
            .create()
            .show()
    }

    private fun scanTSU() {
        baaCodeReaderConnection.toggleScanningBaacode()
    }

    private fun onEIDScanned(eidNumber: String) {
        viewModel.lookupAnimalInfoByEIDNumber(eidNumber)
    }

    // use BaaCode reader
    private fun onBAAScanned(baaCode: String) {
        viewModel.onBaaCodeScanned(baaCode)
    }
}

private class ViewModelFactory(context: Context) : ViewModelProvider.Factory {

    private val appContext = context.applicationContext

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return when (modelClass) {
            TissueSampleViewModel::class.java -> {
                val databaseHandler = DatabaseManager.getInstance(appContext)
                    .createDatabaseHandler()
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(appContext)
                val activeDefSettings = ActiveDefaultSettings(sharedPreferences)
                val defSettingsRepo = DefaultSettingsRepositoryImpl(
                    databaseHandler, ActiveDefaultSettings.from(appContext)
                )
                val loadActiveDefaults = LoadActiveDefaultSettings(
                    activeDefaultSettings = activeDefSettings,
                    defSettingsRepo
                )
                val loadDefaultIdTypeIds = LoadDefaultIdTypeIds(loadActiveDefaults)
                val animalRepo = AnimalRepositoryImpl(databaseHandler, defSettingsRepo)
                val speciesRepo = SpeciesRepositoryImpl(databaseHandler)
                @Suppress("UNCHECKED_CAST")
                TissueSampleViewModel(
                    extras.createSavedStateHandle(),
                    databaseHandler,
                    loadActiveDefaultSettings = loadActiveDefaults,
                    animalRepo = animalRepo,
                    speciesRepo = speciesRepo,
                    laboratoryRepository = LaboratoryRepositoryImpl(databaseHandler),
                    tissueSampleTypeRepository = TissueSampleTypeRepositoryImpl(databaseHandler),
                    tissueTestRepository = TissueTestRepositoryImpl(databaseHandler),
                    tissueSampleContainerTypeRepository = TissueSampleContainerTypeRepositoryImpl(databaseHandler),
                    extractPrintLabelData = ExtractPrintLabelData(sharedPreferences, loadDefaultIdTypeIds)
                ) as T
            }
            else -> throw IllegalStateException("${modelClass.simpleName} is not supported.")
        }
    }
}
