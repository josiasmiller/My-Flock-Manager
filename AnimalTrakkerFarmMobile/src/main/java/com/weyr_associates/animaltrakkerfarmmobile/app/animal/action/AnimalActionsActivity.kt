package com.weyr_associates.animaltrakkerfarmmobile.app.animal.action

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.AnimalDialogs
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.AnimalActionsFeature.ADMINISTER_DRUGS
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.AnimalActionsFeature.GENERAL_ANIMAL_CARE
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.AnimalActionsFeature.VACCINES_AND_DEWORMERS
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.care.ConfigureAnimalCare
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.care.hooves.EditHoofCheck
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.care.hooves.HoofCheckAction
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.care.horns.EditHornCheck
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.care.horns.HornCheckAction
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.care.shear.ShearAction
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.care.shoe.ShoeAction
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.care.wean.WeanAction
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.drug.ConfigureDrugAction
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.drug.DrugAction
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.menu.AnimalActionMenuBottomSheetDialog
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.menu.MenuOption
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.weight.WeightAction
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.add.AddAnimal
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.alert.AddAnimalAlert
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.alert.ShowAlertButtonPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.lookup.LookupAnimalInfo
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.lookup.LookupAnimalInfoPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.collectLatestOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.observeOneTimeEventsOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.ItemSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.core.widget.recyclerview.GridSpacerItemDecoration
import com.weyr_associates.animaltrakkerfarmmobile.app.device.eid.EIDReaderConnection
import com.weyr_associates.animaltrakkerfarmmobile.app.device.scale.ScaleDeviceConnection
import com.weyr_associates.animaltrakkerfarmmobile.app.permissions.RequiredPermissionsWatcher
import com.weyr_associates.animaltrakkerfarmmobile.app.reporting.errors.observeErrorReports
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.AnimalRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.DefaultSettingsRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.DrugRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.UnitOfMeasureRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.select.SelectAnimal
import com.weyr_associates.animaltrakkerfarmmobile.app.select.drugTypeSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.ActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseManager
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ActivityAnimalActionsBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.DrugType
import com.weyr_associates.animaltrakkerfarmmobile.model.IdType
import kotlinx.coroutines.flow.combine

class AnimalActionsActivity : AppCompatActivity() {

    companion object {
        fun newIntentToAdministerDrugs(context: Context) =
            Intent(context, AnimalActionsActivity::class.java).apply {
                putExtra(EXTRA_ANIMAL_ACTIONS_FEATURE, ADMINISTER_DRUGS as Parcelable)
            }

        fun newIntentToVaccinateAndDeworm(context: Context) =
            Intent(context, AnimalActionsActivity::class.java).apply {
                putExtra(EXTRA_ANIMAL_ACTIONS_FEATURE, VACCINES_AND_DEWORMERS as Parcelable)
            }

        fun newIntentForGeneralAnimalCare(context: Context) =
            Intent(context, AnimalActionsActivity::class.java).apply {
                putExtra(EXTRA_ANIMAL_ACTIONS_FEATURE, GENERAL_ANIMAL_CARE as Parcelable)
            }

        private const val EXTRA_ANIMAL_ACTIONS_FEATURE = "EXTRA_ANIMAL_ACTIONS_FEATURE"
        private const val REASON_SCAN_WEIGHT_FOR_ACTION_SET = "REASON_SCAN_WEIGHT_FOR_ACTION_SET"
    }

    private val animalActionsFeature: AnimalActionsFeature by lazy {
        requireNotNull(intent.getParcelableExtra(EXTRA_ANIMAL_ACTIONS_FEATURE))
    }

    private val binding by lazy {
        ActivityAnimalActionsBinding.inflate(layoutInflater)
    }

    private val viewModel by viewModels<AnimalActionsViewModel> {
        ViewModelFactory(this, animalActionsFeature)
    }

    private val backConfirmationHandler = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            showExitConfirmationDialog()
        }
    }

    private val lookupAnimalInfoPresenter by lazy {
        LookupAnimalInfoPresenter(binding.lookupAnimalInfo).apply {
            displayAnimalWeight = true
            displayFlockAndBreed = false
            addAnimalAlertLauncher = this@AnimalActionsActivity.addAnimalAlertLauncher
            onAddAnimalWithEIDClicked = { eidNumber ->
                addAndSelectAnimalLauncher.launch(AddAnimal.Request(IdType.ID_TYPE_ID_EID, eidNumber))
            }
        }
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

    private val configureAnimalCareLauncher = registerForActivityResult(ConfigureAnimalCare.Contract()) { result ->
        result?.let { viewModel.onAnimalCareConfigured(result) }
    }

    private val configureDrugActionLauncher = registerForActivityResult(ConfigureDrugAction.ConfigureContract()) { result ->
        result?.let { viewModel.onDrugActionConfigured(result) }
    }

    private val editDrugActionLauncher = registerForActivityResult(ConfigureDrugAction.EditContract()) { result ->
        result?.let { viewModel.onDrugActionConfigurationEdited(it.actionId, it.configuration) }
    }

    private val editHoofCheckLauncher = registerForActivityResult(EditHoofCheck.Contract()) { result ->
        if (result != null) { viewModel.onHoofCheckCompleted(result) }
    }

    private val editHornCheckLauncher = registerForActivityResult(EditHornCheck.Contract()) { result ->
        if (result != null) { viewModel.onHornCheckCompleted(result) }
    }

    private lateinit var eidReaderConnection: EIDReaderConnection
    private lateinit var scaleDeviceConnection: ScaleDeviceConnection
    private lateinit var requiredPermissionsWatcher: RequiredPermissionsWatcher

    private lateinit var actionsAdapter: AnimalActionAdapter

    private lateinit var drugTypeSelectionPresenter: ItemSelectionPresenter<DrugType>
    private var selectedDrugType: DrugType? = null

    private data class ActionsUpdate(val isConfigured: Boolean, val actions: ActionSet?)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        eidReaderConnection = EIDReaderConnection(this, lifecycle)
            .also { lifecycle.addObserver(it) }
        scaleDeviceConnection = ScaleDeviceConnection(this, lifecycle)
            .also { lifecycle.addObserver(it) }
        requiredPermissionsWatcher = RequiredPermissionsWatcher(this)
            .also { lifecycle.addObserver(it) }
        with(binding) {
            with(buttonPanelTop) {
                scanEIDButton.setOnClickListener {
                    eidReaderConnection.toggleScanningEID()
                }
                lookupAnimalButton.setOnClickListener {
                    selectAnimalLauncher.launch(null)
                }
                clearDataButton.setOnClickListener {
                    viewModel.clearData()
                }
                mainActionButton.setOnClickListener {
                    viewModel.saveToDatabase()
                }
            }
            buttonAddVaccine.setOnClickListener {
                viewModel.onConfigureDrugAction(DrugType.ID_VACCINE)
            }
            buttonAddDewormer.setOnClickListener {
                viewModel.onConfigureDrugAction(DrugType.ID_DEWORMER)
            }
        }
        actionsAdapter = AnimalActionAdapter(
            onActionActivated = ::onAnimalActionActivated,
            onActionMenuActivated = ::onAnimalActionMenuActivated,
            onActionMenuOptionTriggered = ::onAnimalActionMenuOptionTriggered
        )
        with(binding.recyclerActionItems) {
            layoutManager = GridLayoutManager(
                this@AnimalActionsActivity, 2
            )
            addItemDecoration(
                GridSpacerItemDecoration(
                    this@AnimalActionsActivity, 2
                )
            )
            adapter = actionsAdapter
        }
        collectLatestOnStart(viewModel.animalInfoState) {
            lookupAnimalInfoPresenter.animalInfoState = it
            showAlertButtonPresenter.animalInfoState = it
        }
        collectLatestOnStart(viewModel.animalWeight) { animalWeight ->
            lookupAnimalInfoPresenter.animalWeight = animalWeight
        }
        collectLatestOnStart(
            combine(viewModel.isConfigured, viewModel.actions) { isConfigured, actions ->
                ActionsUpdate(isConfigured, actions)
            },
            ::updateActionsDisplay
        )
        collectLatestOnStart(eidReaderConnection.deviceConnectionState) { connectionState ->
            binding.buttonPanelTop.updateEIDReaderConnectionState(connectionState)
        }
        collectLatestOnStart(eidReaderConnection.isScanningForEID) { isScanning ->
            binding.buttonPanelTop.showScanningEID = isScanning
        }
        collectLatestOnStart(viewModel.canClearData) { canClear ->
            binding.buttonPanelTop.clearDataButton.isEnabled = canClear
        }
        collectLatestOnStart(viewModel.canSaveToDatabase) { canSave ->
            binding.buttonPanelTop.mainActionButton.isEnabled = canSave
        }
        collectLatestOnStart(viewModel.canAddDewormer) { canAddDewormer ->
            binding.buttonAddDewormer.isEnabled = canAddDewormer
        }
        collectLatestOnStart(viewModel.canAddVaccine) { canAddVaccine ->
            binding.buttonAddVaccine.isEnabled = canAddVaccine
        }
        observeOneTimeEventsOnStart(eidReaderConnection.onEIDScanned) { eidNumber ->
            viewModel.lookupAnimalInfoByEIDNumber(eidNumber)
        }
        observeOneTimeEventsOnStart(scaleDeviceConnection.onWeightScanned) { scanResult ->
            viewModel.onWeightValueEntered(scanResult.weight)
        }
        observeOneTimeEventsOnStart(viewModel.animalAlertsEvent) { event ->
            AnimalDialogs.showAnimalAlert(this, event.alerts)
        }
        observeOneTimeEventsOnStart(viewModel.events, ::handleEvent)
        observeErrorReports(viewModel.errorReportFlow)

        drugTypeSelectionPresenter = drugTypeSelectionPresenter(
            button = binding.spinnerDrugTypeSelection,
            excludedDrugTypeIds = setOf(DrugType.ID_VACCINE, DrugType.ID_DEWORMER)
        ) { drugType ->
            selectedDrugType = drugType
            drugTypeSelectionPresenter.displaySelectedItem(drugType)
        }

        binding.buttonAddOtherDrug.setOnClickListener {
            selectedDrugType?.let { drugType ->
                viewModel.onConfigureDrugAction(drugType.id)
            }
        }

        binding.buttonConfigureAnimalCare.setOnClickListener {
            viewModel.onConfigureAnimalCare()
        }

        onBackPressedDispatcher.addCallback(this, backConfirmationHandler)

        configureDisplayForAction()
    }

    private fun configureDisplayForAction() {
        when (animalActionsFeature) {
            VACCINES_AND_DEWORMERS -> {
                title = getString(R.string.title_activity_vaccine_deworm)
                binding.textConfigurationRequired.text = getString(
                    R.string.text_vaccine_dewormer_configuration_required
                )
                binding.containerAnimalCare.isVisible = false
                binding.containerAddOtherDrugs.isVisible = false
                binding.containerAddVaccinesDewormers.isVisible = true
            }
            ADMINISTER_DRUGS -> {
                title = getString(R.string.title_activity_give_drugs)
                binding.textConfigurationRequired.text = getString(
                    R.string.text_administer_drugs_configuration_required
                )
                binding.containerAnimalCare.isVisible = false
                binding.containerAddOtherDrugs.isVisible = true
                binding.containerAddVaccinesDewormers.isVisible = false
            }
            GENERAL_ANIMAL_CARE -> {
                title = getString(R.string.title_activity_general_animal_care)
                binding.textConfigurationRequired.text = getString(
                    R.string.text_animal_care_configuration_required
                )
                binding.containerAnimalCare.isVisible = true
                binding.containerAddOtherDrugs.isVisible = false
                binding.containerAddVaccinesDewormers.isVisible = false
            }
        }
    }

    private fun onAnimalActionActivated(action: AnimalAction) {
        when (action) {
            is WeightAction -> {
                scaleDeviceConnection.scanWeight(REASON_SCAN_WEIGHT_FOR_ACTION_SET)
            }
            is HoofCheckAction -> {
                editHoofCheckLauncher.launch(action.hoofCheck)
            }
            is HornCheckAction -> {
                editHornCheckLauncher.launch(action.hornCheck)
            }
            else -> viewModel.onAnimalActionActivated(action)
        }
    }

    private fun onAnimalActionMenuActivated(action: AnimalAction) {
        when (action) {
            is DrugAction -> showDrugActionMenuBottomSheet(action)
            is WeightAction -> showWeightActionMenuBottomSheet(action)
            is HoofCheckAction -> showHoofCheckActionMenuBottomSheet(action)
            is HornCheckAction -> showHornCheckActionMenuBottomSheet(action)
            else -> showGeneralActionMenuBottomSheet(action)
        }
    }

    private fun onAnimalActionMenuOptionTriggered(action: AnimalAction, menuOption: MenuOption) {
        if (action is WeightAction && menuOption == MenuOption.EDIT) {
            showManualWeightEntryDialog(action)
        }
    }

    private fun showDrugActionMenuBottomSheet(drugAction: DrugAction) {
        AnimalActionMenuBottomSheetDialog(
            this,
            title = drugAction.configuration.drugApplicationInfo.name,
            subtitle = drugAction.configuration.location.name,
            menuOptions = listOf(MenuOption.EDIT, MenuOption.DELETE)
        ) { menuOption ->
            when (menuOption) {
                MenuOption.EDIT -> {
                    viewModel.onEditDrugAction(drugAction)
                }
                MenuOption.DELETE -> {
                    viewModel.onRemoveDrugAction(drugAction)
                }
                else -> Unit
            }
        }.show()
    }

    private fun showWeightActionMenuBottomSheet(weightAction: WeightAction) {
        val menuOptions = if (weightAction.isFixedInConfiguration)
            listOf(MenuOption.EDIT, MenuOption.CLEAR) else
                listOf(MenuOption.EDIT, MenuOption.CLEAR, MenuOption.DELETE)
        AnimalActionMenuBottomSheetDialog(
            this,
            title = getString(R.string.text_weight),
            subtitle = "",
            menuOptions = menuOptions
        ) { menuOption ->
            when (menuOption) {
                MenuOption.EDIT -> {
                    showManualWeightEntryDialog(weightAction)
                }
                MenuOption.CLEAR -> {
                    viewModel.onClearAction(weightAction)
                }
                MenuOption.DELETE -> {
                    viewModel.onRemoveAction(weightAction)
                }
            }
        }.show()
    }

    private fun showManualWeightEntryDialog(weightAction: WeightAction) {
        AnimalDialogs.manuallyEnterAnimalWeight(this, weightAction.weight) { weight ->
            viewModel.onWeightValueEntered(weight)
        }
    }

    private fun showHoofCheckActionMenuBottomSheet(hoofCheckAction: HoofCheckAction) {
        AnimalActionMenuBottomSheetDialog(
            this,
            title = getString(R.string.text_hoof_check),
            subtitle = "",
            menuOptions = listOf(MenuOption.EDIT, MenuOption.CLEAR, MenuOption.DELETE)
        ) { menuOption ->
            when (menuOption) {
                MenuOption.EDIT -> {
                    editHoofCheckLauncher.launch(hoofCheckAction.hoofCheck)
                }
                MenuOption.CLEAR -> {
                    viewModel.onClearAction(hoofCheckAction)
                }
                MenuOption.DELETE -> {
                    viewModel.onRemoveAction(hoofCheckAction)
                }
            }
        }.show()
    }

    private fun showHornCheckActionMenuBottomSheet(action: HornCheckAction) {
        AnimalActionMenuBottomSheetDialog(
            this,
            title = getString(R.string.text_horn_check),
            subtitle = "",
            menuOptions = listOf(MenuOption.EDIT, MenuOption.CLEAR, MenuOption.DELETE)
        ) { menuOption ->
            when (menuOption) {
                MenuOption.EDIT -> {
                    editHornCheckLauncher.launch(action.hornCheck)
                }
                MenuOption.CLEAR -> {
                    viewModel.onClearAction(action)
                }
                MenuOption.DELETE -> {
                    viewModel.onRemoveAction(action)
                }
            }
        }.show()
    }

    private fun showGeneralActionMenuBottomSheet(action: AnimalAction) {
        AnimalActionMenuBottomSheetDialog(
            this,
            title = nameForAnimalAction(action),
            subtitle = "",
            menuOptions = listOf(MenuOption.CLEAR, MenuOption.DELETE)
        ) { menuOption ->
            when (menuOption) {
                MenuOption.CLEAR -> {
                    viewModel.onClearAction(action)
                }
                MenuOption.DELETE -> {
                    viewModel.onRemoveAction(action)
                }
                else -> Unit
            }
        }.show()
    }

    private fun nameForAnimalAction(action: AnimalAction): String {
        return when (action) {
            is DrugAction -> { action.configuration.drugApplicationInfo.name }
            is HoofCheckAction -> { getString(R.string.text_hoof_check) }
            is HornCheckAction -> { getString(R.string.text_horn_check) }
            is ShoeAction -> { getString(R.string.text_shoe) }
            is ShearAction -> { getString(R.string.text_shear) }
            is WeanAction -> { getString(R.string.text_wean) }
            is WeightAction -> { getString(R.string.text_weight) }
            else -> { getString(R.string.text_action) }
        }
    }

    private fun updateActionsDisplay(actionsUpdate: ActionsUpdate) {
        binding.containerConfigurationRequired.isVisible = !actionsUpdate.isConfigured
        binding.recyclerActionItems.isVisible = actionsUpdate.isConfigured
        actionsAdapter.updateActionSet(actionsUpdate.actions) {
            binding.recyclerActionItems.invalidateItemDecorations()
        }
    }

    private fun handleEvent(event: AnimalActionsViewModel.Event) {
        when (event) {
            AnimalActionsViewModel.AnimalRequiredToBeAlive -> {
                LookupAnimalInfo.Dialogs.showAnimalRequiredToBeAlive(this, viewModel)
            }
            is AnimalActionsViewModel.ConfigureAnimalCareEvent -> {
                configureAnimalCareLauncher.launch(event.configuration)
            }
            is AnimalActionsViewModel.AddDrugConfigurationEvent -> {
                configureDrugActionLauncher.launch(
                    ConfigureDrugAction.ConfigureRequest(
                        event.drugTypeId,
                        event.excludedDrugIds
                    )
                )
            }
            is AnimalActionsViewModel.EditDrugConfigurationEvent -> {
                editDrugActionLauncher.launch(
                    ConfigureDrugAction.EditRequest(
                        event.actionId,
                        event.configuration,
                        event.excludedDrugIds
                    )
                )
            }
            AnimalActionsViewModel.UpdateDatabaseEvent.Success -> {
                Toast.makeText(
                    this,
                    when (animalActionsFeature) {
                        VACCINES_AND_DEWORMERS -> R.string.toast_administer_vaccine_dewormer_success
                        ADMINISTER_DRUGS -> R.string.toast_administer_drugs_success
                        GENERAL_ANIMAL_CARE -> R.string.toast_general_animal_care_success
                    },
                    Toast.LENGTH_SHORT
                ).show()
            }
            AnimalActionsViewModel.UpdateDatabaseEvent.Error -> {
                Toast.makeText(
                    this,
                    when (animalActionsFeature) {
                        VACCINES_AND_DEWORMERS -> R.string.toast_administer_vaccine_dewormer_error
                        ADMINISTER_DRUGS -> R.string.toast_administer_drugs_error
                        GENERAL_ANIMAL_CARE -> R.string.toast_general_animal_care_error
                    },
                    Toast.LENGTH_SHORT
                ).show()
            }
            AnimalActionsViewModel.UpdateDatabaseEvent.AnimalDoesNotExist -> {
                AnimalDialogs.showAnimalNotFound(this)
            }
            is AnimalActionsViewModel.UpdateDatabaseEvent.NoDrugDosageValidForAnimal -> {
                val speciesDosageMissing = event.error
                AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_title_no_dosage_for_species)
                    .setMessage(
                        getString(
                            R.string.dialog_message_no_dosage_for_species,
                            speciesDosageMissing.speciesName,
                            speciesDosageMissing.drugName
                        )
                    )
                    .setPositiveButton(R.string.ok) { _, _ -> }
                    .create()
                    .show()
            }
            is AnimalActionsViewModel.UpdateDatabaseEvent.InvalidDrugDosageForAnimal -> {
                val speciesDosageInvalid = event.error
                AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_title_invalid_dosage_for_species)
                    .setMessage(
                        getString(
                            R.string.dialog_message_invalid_dosage_for_species,
                            speciesDosageInvalid.drugName,
                            speciesDosageInvalid.drugSpeciesName,
                            speciesDosageInvalid.animalSpeciesName
                        )
                    )
                    .setPositiveButton(R.string.ok) { _, _ -> }
                    .create()
                    .show()
            }
        }
    }

    private fun showExitConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_title_confirm_exit)
            .setMessage(R.string.dialog_message_confirm_exit)
            .setPositiveButton(R.string.yes_label) { _, _ ->
                backConfirmationHandler.isEnabled = false
                super.onBackPressed()
            }
            .setNegativeButton(R.string.no_label, null)
            .create()
            .show()
    }

    private class ViewModelFactory(
        context: Context,
        private val animalActionsFeature: AnimalActionsFeature
    ) : ViewModelProvider.Factory {

        private val appContext = context.applicationContext

        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return when (modelClass) {
                AnimalActionsViewModel::class.java -> {
                    val databaseHandler = DatabaseManager.getInstance(appContext)
                        .createDatabaseHandler()
                    val defaultSettingsRepo = DefaultSettingsRepositoryImpl(
                        databaseHandler, ActiveDefaultSettings.from(appContext)
                    )
                    val animalRepo = AnimalRepositoryImpl(databaseHandler, defaultSettingsRepo)
                    val drugRepo = DrugRepositoryImpl(databaseHandler)
                    val unitsRepo = UnitOfMeasureRepositoryImpl(databaseHandler)
                    val activeDefaultSettings = ActiveDefaultSettings(
                        PreferenceManager.getDefaultSharedPreferences(appContext)
                    )
                    val loadDefaultSettings = LoadActiveDefaultSettings(
                        activeDefaultSettings = activeDefaultSettings,
                        defaultSettingsRepo = defaultSettingsRepo
                    )
                    val trackAnimalActions = TrackAnimalActions(
                        databaseHandler = databaseHandler,
                        animalRepository = animalRepo,
                        drugRepository = drugRepo
                    )
                    @Suppress("UNCHECKED_CAST")
                    AnimalActionsViewModel(
                        animalActionsFeature = animalActionsFeature,
                        animalRepo = animalRepo,
                        unitsRepo = unitsRepo,
                        loadDefaultSettings = loadDefaultSettings,
                        trackAnimalActions = trackAnimalActions
                    ) as T
                }
                else -> throw IllegalStateException("${modelClass.simpleName} is not supported.")
            }
        }
    }
}
