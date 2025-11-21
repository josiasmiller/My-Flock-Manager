package com.weyr_associates.animaltrakkerfarmmobile.app.animal.breeding

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.AnimalDialogs
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.breeding.AddOffspringViewModel.ValidationError
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.farm.BaseFarmTagOnEIDFeature
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.input.simple.IdEntryEditorPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.trich.AutoIncrementNextTrichIdFeature
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.validation.IdValidationErrorDialog
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.validation.IdValidations
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.collectLatestOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.observeOneTimeEventsOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.asFragmentResultListenerRegistrar
import com.weyr_associates.animaltrakkerfarmmobile.app.core.ErrorReport
import com.weyr_associates.animaltrakkerfarmmobile.app.reporting.errors.ErrorReportDialog
import com.weyr_associates.animaltrakkerfarmmobile.app.reporting.errors.observeErrorReports
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.ItemSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.core.widget.TopButtonBar
import com.weyr_associates.animaltrakkerfarmmobile.app.device.eid.EIDReaderConnection
import com.weyr_associates.animaltrakkerfarmmobile.app.device.scale.ScaleDeviceConnection
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.AnimalRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.DefaultSettingsRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.EvaluationRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.FlockPrefixRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.IdColorRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.IdLocationRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.IdTypeRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.ScrapieFlockRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.SexRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.UnitOfMeasureRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.select.evalTraitOptionSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.select.geneticCoatColorSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.select.rearTypeItemSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.select.sexSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.ActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadDefaultIdConfigs
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadDefaultWeightUnits
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadDefaultWeightUnitsId
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseManager
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ActivityAddOffspringBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.EvalTrait
import com.weyr_associates.animaltrakkerfarmmobile.model.EvalTraitOption
import com.weyr_associates.animaltrakkerfarmmobile.model.GeneticCoatColor
import com.weyr_associates.animaltrakkerfarmmobile.model.RearType
import com.weyr_associates.animaltrakkerfarmmobile.model.Sex
import com.weyr_associates.animaltrakkerfarmmobile.model.Species
import com.weyr_associates.animaltrakkerfarmmobile.model.UnitOfMeasure
import kotlinx.coroutines.flow.combine

class AddOffspringActivity : AppCompatActivity() {

    companion object {

        fun newIntent(context: Context, damAnimalId: EntityId) =
            Intent(context, AddOffspringActivity::class.java)
                .putExtra(EXTRA_DAM_ANIMAL_ID, damAnimalId)

        private const val EXTRA_DAM_ANIMAL_ID = "EXTRA_DAM_ANIMAL_ID"
        private const val REQUEST_KEY_EVAL_OPTION_LAMB_EASE = "REQUEST_KEY_EVAL_OPTION_LAMB_EASE"
        private const val REQUEST_KEY_EVAL_OPTION_SUCK_REFLEX = "REQUEST_KEY_EVAL_OPTION_SUCK_REFLEX"
        private const val SCAN_WEIGHT_REASON_ADD_OFFSPRING = "SCAN_WEIGHT_REASON_ADD_OFFSPRING"
    }

    private val damAnimalId: EntityId by lazy {
        requireNotNull(intent.getParcelableExtra(EXTRA_DAM_ANIMAL_ID)) as EntityId
    }

    private val binding by lazy {
        ActivityAddOffspringBinding.inflate(layoutInflater)
    }

    private val viewModel by viewModels<AddOffspringViewModel> {
        Factory(this@AddOffspringActivity, damAnimalId)
    }

    private lateinit var rearTypeSelectionPresenter: ItemSelectionPresenter<RearType>
    private lateinit var sexTypeSelectionPresenter: ItemSelectionPresenter<Sex>
    private lateinit var lambEaseSelectionPresenter: ItemSelectionPresenter<EvalTraitOption>
    private lateinit var suckReflexSelectionPresenter: ItemSelectionPresenter<EvalTraitOption>
    private lateinit var coatColorSelectionPresenter: ItemSelectionPresenter<GeneticCoatColor>

    private lateinit var idEntryEditorPresenter: IdEntryEditorPresenter

    private lateinit var eidReaderConnection: EIDReaderConnection
    private lateinit var scaleDeviceConnection: ScaleDeviceConnection

    @SuppressLint("DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        eidReaderConnection = EIDReaderConnection(this, lifecycle)
            .also { lifecycle.addObserver(it) }
        scaleDeviceConnection = ScaleDeviceConnection(this, lifecycle)
            .also { lifecycle.addObserver(it) }
        with(binding.buttonPanelTop) {
            show(TopButtonBar.UI_SCANNER_STATUS or
                TopButtonBar.UI_SCAN_EID or TopButtonBar.UI_CLEAR_DATA or
                TopButtonBar.UI_ACTION_UPDATE_DATABASE
            )
            scanEIDButton.setOnClickListener {
                eidReaderConnection.toggleScanningEID()
            }
            clearDataButton.setOnClickListener {
                viewModel.clearData()
            }
            mainActionButton.setOnClickListener {
                viewModel.updateDatabase()
            }
        }
        rearTypeSelectionPresenter = rearTypeItemSelectionPresenter(
            binding.spinnerRearType
        ) { rearType ->
            viewModel.selectRearType(rearType)
        }.also {
            it.bindToFlow(this, lifecycleScope, viewModel.selectedRearType)
        }
        sexTypeSelectionPresenter = sexSelectionPresenter(
            Species.ID_SHEEP,
            button = binding.spinnerSex
        ) { sex ->
            viewModel.selectSex(sex)
        }.also {
            it.bindToFlow(this, lifecycleScope, viewModel.selectedSex)
        }
        lambEaseSelectionPresenter = evalTraitOptionSelectionPresenter(
            traitId = EvalTrait.TRAIT_ID_LAMB_EASE,
            button = binding.spinnerLambEase,
            requestKey = REQUEST_KEY_EVAL_OPTION_LAMB_EASE
        ) { lambEaseOption ->
            viewModel.selectLambEase(lambEaseOption)
        }.also {
            it.bindToFlow(this, lifecycleScope, viewModel.selectedLambEase)
        }
        suckReflexSelectionPresenter = evalTraitOptionSelectionPresenter(
            traitId = EvalTrait.TRAIT_ID_SUCK_REFLEX,
            button = binding.spinnerSuckReflex,
            requestKey = REQUEST_KEY_EVAL_OPTION_SUCK_REFLEX
        ) { suckReflexOption ->
            viewModel.selectSuckReflex(suckReflexOption)
        }.also {
            it.bindToFlow(this, lifecycleScope, viewModel.selectedSuckReflex)
        }
        coatColorSelectionPresenter = geneticCoatColorSelectionPresenter(
            button = binding.spinnerCoatColor
        ) { coatColor ->
            viewModel.selectCoatColor(coatColor)
        }.also {
            it.bindToFlow(this, lifecycleScope, viewModel.selectedCoatColor)
        }
        binding.buttonScanWeight.setOnClickListener {
            scaleDeviceConnection.scanWeight(SCAN_WEIGHT_REASON_ADD_OFFSPRING)
        }
        binding.checkIsStillBorn.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setStillBorn(isChecked)
        }
        binding.checkIsDamMarked.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setDamMarked(isChecked)
        }
        idEntryEditorPresenter = IdEntryEditorPresenter(
            binding.idEntryEditor,
            viewModel.idEntryEditor,
            this,
            this.asFragmentResultListenerRegistrar()
        )
        collectLatestOnStart(eidReaderConnection.deviceConnectionState) { state ->
            binding.buttonPanelTop.updateEIDReaderConnectionState(state)
        }
        collectLatestOnStart(eidReaderConnection.isScanningForEID) { isScanning ->
            binding.buttonPanelTop.showScanningEID = isScanning
        }
        collectLatestOnStart(viewModel.canClearData) { canClear ->
            binding.buttonPanelTop.clearDataButton.isEnabled = canClear
        }
        collectLatestOnStart(viewModel.canUpdateDatabase) { canUpdate ->
            binding.buttonPanelTop.mainActionButton.isEnabled = canUpdate
        }
        collectLatestOnStart(viewModel.sireAnimalName) { sireAnimalName ->
            binding.textSireName.text = sireAnimalName?.name
        }
        collectLatestOnStart(viewModel.damAnimalName) { damAnimalName ->
            binding.textDamName.text = damAnimalName?.name
        }
        collectLatestOnStart(viewModel.selectedServiceType) { serviceType ->
            binding.textServiceType.text = serviceType?.name
        }
        collectLatestOnStart(
            combine(viewModel.currentWeight, viewModel.weightUnits, ::Pair)
        ) { (currentWeight, weightUnits) ->
            updateWeightDisplay(currentWeight, weightUnits)
            binding.buttonEditWeight.setOnClickListener {
                AnimalDialogs.manuallyEnterAnimalWeight(this, currentWeight) { newWeight ->
                    viewModel.setWeight(newWeight)
                }
            }
        }
        observeOneTimeEventsOnStart(eidReaderConnection.onEIDScanned) { eidString ->
            viewModel.onEIDScanned(eidString)
        }
        observeOneTimeEventsOnStart(scaleDeviceConnection.onWeightScanned) { scanResult ->
            viewModel.setWeight(scanResult.weight)
        }
        collectLatestOnStart(viewModel.isStillBorn) { isStillBorn ->
            binding.checkIsStillBorn.isChecked = isStillBorn
        }
        collectLatestOnStart(viewModel.shouldMarkDam) { isDamMarked ->
            binding.checkIsDamMarked.isChecked = isDamMarked
        }
        collectLatestOnStart(viewModel.canMarkDam) { canMarkDam ->
            binding.checkIsDamMarked.isEnabled = canMarkDam
        }
        observeOneTimeEventsOnStart(viewModel.events, ::handleEvent)
        observeErrorReports(viewModel.errorReportFlow)
    }

    @SuppressLint("DefaultLocale")
    private fun updateWeightDisplay(weight: Float?, weightUnits: UnitOfMeasure?) {
        binding.textWeight.text = if (weight != null) {
            val weightString = String.format("%.2f", weight)
            if (weightUnits != null) {
                "$weightString ${weightUnits.abbreviation}"
            } else {
                weightString
            }
        } else {
            ""
        }
    }

    private fun handleEvent(event: AddOffspringViewModel.Event) {
        when (event) {
            is AddOffspringViewModel.ScannedEIDAlreadyUsed -> {
                IdValidationErrorDialog.showEIDAlreadyInUseError(
                    this,
                    event.error
                )
            }
            is ValidationError -> {
                handleValidationError(event)
            }
            is AddOffspringViewModel.UpdateDatabaseError -> {
                handleUpdateDatabaseError(event.addOffspringError)
            }
            AddOffspringViewModel.UpdateDatabaseSuccess -> {
                AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_title_add_offspring_success)
                    .setMessage(R.string.dialog_message_add_offspring_success)
                    .setPositiveButton(R.string.ok) { _, _ -> /* NO-OP */ }
                    .create()
                    .show()
            }
        }
    }

    private fun handleValidationError(validationError: ValidationError) {
        when (validationError) {
            ValidationError.IdEntryRequired -> {
                IdValidationErrorDialog.showIdEntryIsRequiredError(this)
            }

            ValidationError.PartialIdEntry -> {
                IdValidationErrorDialog.showPartialIdEntryError(this)
            }

            is ValidationError.InvalidIdNumberFormat -> {
                IdValidationErrorDialog.showIdNumberFormatError(this, validationError.idEntry)
            }

            is ValidationError.InvalidIdCombination -> {
                IdValidationErrorDialog.showIdCombinationError(this, validationError.error)
            }
        }
    }

    private fun handleUpdateDatabaseError(addOffspringError: AddOffspringError) {
        when (addOffspringError) {
            is AddOffspringFatalError -> {
                handleFatalError(addOffspringError.errorReport)
            }
            is BreedPercentageError -> {
                AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_title_add_offspring_breed_percentage_error)
                    .setMessage(addOffspringError.message)
                    .setPositiveButton(R.string.ok) { _, _ -> /* NO-OP */ }
                    .create()
                    .show()
            }
        }
    }

    private fun handleFatalError(errorReport: ErrorReport) {
        ErrorReportDialog.show(this, errorReport)
    }

    private class Factory(context: Context, private val damAnimalId: EntityId) : ViewModelProvider.Factory {

        private val appContext = context.applicationContext

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val databaseHandler = DatabaseManager.getInstance(appContext)
                .createDatabaseHandler()
            val defaultSettingsRepo = DefaultSettingsRepositoryImpl(
                databaseHandler, ActiveDefaultSettings.from(appContext)
            )
            val animalRepo = AnimalRepositoryImpl(databaseHandler, defaultSettingsRepo)
            val sexRepo = SexRepositoryImpl(databaseHandler)
            val unitsRepo = UnitOfMeasureRepositoryImpl(databaseHandler)
            val evaluationRepo = EvaluationRepositoryImpl(databaseHandler)
            val idTypeRepo = IdTypeRepositoryImpl(databaseHandler)
            val idColorRepo = IdColorRepositoryImpl(databaseHandler)
            val idLocationRepo = IdLocationRepositoryImpl(databaseHandler)
            val scrapieFlockRepo = ScrapieFlockRepositoryImpl(databaseHandler)
            val flockPrefixRepo = FlockPrefixRepositoryImpl(databaseHandler)
            val idValidations = IdValidations(animalRepo)
            val loadActiveDefaults = LoadActiveDefaultSettings.from(appContext, databaseHandler)
            val loadDefaultIdConfigs = LoadDefaultIdConfigs(
                loadActiveDefaults, idTypeRepo, idColorRepo, idLocationRepo
            )
            val weightUnitsIdLoader = LoadDefaultWeightUnitsId(loadActiveDefaults)
            val weightUnitsLoader = LoadDefaultWeightUnits(weightUnitsIdLoader, unitsRepo)
            val baseFarmTagOnEIDFeature = BaseFarmTagOnEIDFeature(loadActiveDefaults)
            val autoUpdateTrichIdFeature = AutoIncrementNextTrichIdFeature(
                loadActiveDefaultSettings = loadActiveDefaults,
                defaultSettingsRepository = defaultSettingsRepo
            )
            val addOffspring = AddOffspring(
                databaseHandler = databaseHandler,
                animalRepository = animalRepo,
                sexRepository = sexRepo,
                flockPrefixRepository = flockPrefixRepo,
                loadActiveDefaults = loadActiveDefaults
            )
            @Suppress("UNCHECKED_CAST")
            return AddOffspringViewModel(
                damAnimalId = damAnimalId,
                animalRepository = animalRepo,
                evaluationRepository = evaluationRepo,
                scrapieFlockRepository = scrapieFlockRepo,
                loadDefaultWeightUnits = weightUnitsLoader,
                loadDefaultIdConfigs = loadDefaultIdConfigs,
                idTypeRepository = idTypeRepo,
                idColorRepository = idColorRepo,
                idLocationRepository = idLocationRepo,
                idValidations = idValidations,
                loadActiveDefaultSettings = loadActiveDefaults,
                baseFarmTagOnEIDFeature = baseFarmTagOnEIDFeature,
                autoUpdateTrichId = autoUpdateTrichIdFeature,
                addOffspring = addOffspring
            ) as T
        }
    }
}
