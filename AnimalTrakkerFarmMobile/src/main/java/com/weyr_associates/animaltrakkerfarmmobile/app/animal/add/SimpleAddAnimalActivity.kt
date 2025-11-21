package com.weyr_associates.animaltrakkerfarmmobile.app.animal.add

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.preference.PreferenceManager
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.add.SimpleAddAnimalViewModel.Event
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.add.SimpleAddAnimalViewModel.InputEvent
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.add.SimpleAddAnimalViewModel.UpdateDatabaseEvent
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.add.SimpleAddAnimalViewModel.ValidationError
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.farm.BaseFarmTagOnEIDFeature
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.input.simple.IdEntryEditorPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.trich.AutoIncrementNextTrichIdFeature
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.validation.IdValidationErrorDialog
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.validation.IdValidations
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.collectLatestOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.observeOneTimeEventsOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.asFragmentResultListenerRegistrar
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.coroutines.flow.observeOneTimeEvents
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.ItemSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.core.widget.TopButtonBar
import com.weyr_associates.animaltrakkerfarmmobile.app.device.eid.EIDReaderConnection
import com.weyr_associates.animaltrakkerfarmmobile.app.permissions.RequiredPermissionsWatcher
import com.weyr_associates.animaltrakkerfarmmobile.app.reporting.errors.observeErrorReports
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.AnimalRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.BreedRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.DefaultSettingsRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.IdColorRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.IdLocationRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.IdTypeRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.OwnerRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.PremiseRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.ScrapieFlockRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.SexRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.select.ageMonthsSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.select.ageYearsSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.select.breedSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.select.ownerSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.select.sexSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.ActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadDefaultIdConfigs
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseManager
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ActivitySimpleAddAnimalBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.Breed
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.Owner
import com.weyr_associates.animaltrakkerfarmmobile.model.Sex
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SimpleAddAnimalActivity : AppCompatActivity() {

    companion object {
        fun newIntent(context: Context) = Intent(context, SimpleAddAnimalActivity::class.java)

        fun newIntentToAddAndSelect(
            context: Context,
            primaryIdType: EntityId,
            primaryIdNumber: String
        ): Intent {
            return Intent(context, SimpleAddAnimalActivity::class.java).apply {
                action = AddAnimal.ACTION_ADD_AND_SELECT
                putExtra(AddAnimal.EXTRA_PRIMARY_ID_TYPE_ID, primaryIdType)
                putExtra(AddAnimal.EXTRA_PRIMARY_ID_NUMBER, primaryIdNumber)
            }
        }

        fun startToAddAndSelect(
            activity: Activity,
            primaryIdType: EntityId,
            primaryIdNumber: String,
            requestCode: Int
        ) {
            activity.startActivityForResult(
                newIntentToAddAndSelect(
                    activity,
                    primaryIdType,
                    primaryIdNumber
                ),
                requestCode
            )
        }
    }

    private val viewModel: SimpleAddAnimalViewModel
            by viewModels<SimpleAddAnimalViewModel> {
                ViewModelFactory(this)
            }

    private lateinit var binding: ActivitySimpleAddAnimalBinding

    private lateinit var breedPresenter: ItemSelectionPresenter<Breed>
    private lateinit var ageYearsPresenter: ItemSelectionPresenter<Int>
    private lateinit var ageMonthsPresenter: ItemSelectionPresenter<Int>
    private lateinit var sexPresenter: ItemSelectionPresenter<Sex>
    private lateinit var ownerPresenter: ItemSelectionPresenter<Owner>

    private lateinit var idEntryEditorPresenter: IdEntryEditorPresenter

    private val requiredPermissionsWatcher = RequiredPermissionsWatcher(this)

    private var breedCollectionJob: Job? = null
    private var sexCollectionJob: Job? = null

    private lateinit var eidReaderConnection: EIDReaderConnection

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySimpleAddAnimalBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())
        setupTopButtonBar()
        setupInputFields()
        setupBreedAndSexSpinner()
        setupAgeSpinners()
        setupOwnerSpinner()
        bindToEvents()

        idEntryEditorPresenter = IdEntryEditorPresenter(
            binding.idEntryEditor,
            viewModel.idEntryEditor,
            this,
            this.asFragmentResultListenerRegistrar()
        )

        eidReaderConnection = EIDReaderConnection(this, lifecycle)
            .also { lifecycle.addObserver(it) }

        collectLatestOnStart(eidReaderConnection.deviceConnectionState) { connectionState ->
            binding.buttonPanelTop.updateEIDReaderConnectionState(connectionState)
        }
        collectLatestOnStart(eidReaderConnection.isScanningForEID) { isScanning ->
            binding.buttonPanelTop.showScanningEID = isScanning
        }
        observeOneTimeEventsOnStart(eidReaderConnection.onEIDScanned) { eidString ->
            onEIDScanned(eidString)
        }
        observeErrorReports(viewModel.errorReportFlow)
        lifecycle.addObserver(requiredPermissionsWatcher)
    }

    private fun setupTopButtonBar() {
        binding.buttonPanelTop.show(
            TopButtonBar.UI_SCANNER_STATUS or
                    TopButtonBar.UI_SCAN_EID or
                    TopButtonBar.UI_CLEAR_DATA or
                    TopButtonBar.UI_ACTION_UPDATE_DATABASE
        )
        binding.buttonPanelTop.scanEIDButton.setOnClickListener {
            eidReaderConnection.toggleScanningEID()
        }
        binding.buttonPanelTop.clearDataButton.setOnClickListener {
            viewModel.clearData()
        }
        binding.buttonPanelTop.mainActionButton.setOnClickListener {
            viewModel.saveToDatabase()
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.canClearData.collectLatest { canClear ->
                    binding.buttonPanelTop.clearDataButton.isEnabled = canClear
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.canSaveToDatabase.collectLatest { canSave ->
                    binding.buttonPanelTop.mainActionButton.isEnabled = canSave
                }
            }
        }
    }

    private fun setupInputFields() {
        with(binding.inputAnimalName) {
            setText(viewModel.animalName)
            addTextChangedListener {
                viewModel.animalName = it.toString()
            }
        }
    }

    private fun setupBreedAndSexSpinner() {
        //Reassign breed and sex presenters
        //whenever the species ID changes
        //so proper breed and sex options are available.
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.speciesId.collect { speciesId ->
                    breedCollectionJob?.cancel()
                    breedPresenter = breedSelectionPresenter(
                        speciesId = speciesId,
                        button = binding.animalBreedSpinner,
                        hintText = getString(R.string.hint_select_breed)
                    ) { breed -> viewModel.selectBreed(breed) }.also {
                        breedCollectionJob = it.bindToFlow(
                            this@SimpleAddAnimalActivity,
                            lifecycleScope,
                            viewModel.selectedBreed
                        )
                    }
                    sexCollectionJob?.cancel()
                    sexPresenter = sexSelectionPresenter(
                        speciesId = speciesId,
                        button = binding.animalSexSpinner,
                        hintText = getString(R.string.hint_select_sex)
                    ) { sex -> viewModel.selectSex(sex) }.also {
                        sexCollectionJob = it.bindToFlow(
                            this@SimpleAddAnimalActivity,
                            lifecycleScope,
                            viewModel.selectedSex
                        )
                    }
                }
            }
        }
    }

    private fun setupAgeSpinners() {
        ageYearsPresenter = ageYearsSelectionPresenter(
            button = binding.ageYearSpinner,
            hintText = getString(R.string.hint_select_age_years)
        ) { ageYears -> viewModel.selectAgeYears(ageYears) }.also {
            it.bindToFlow(this, lifecycleScope, viewModel.selectedAgeYears)
        }

        ageMonthsPresenter = ageMonthsSelectionPresenter(
            button = binding.ageMonthSpinner,
            hintText = getString(R.string.hint_select_age_months)
        ) { ageMonths -> viewModel.selectAgeMonths(ageMonths) }.also {
            it.bindToFlow(this, lifecycleScope, viewModel.selectedAgeMonths)
        }
    }

    private fun setupOwnerSpinner() {
        ownerPresenter = ownerSelectionPresenter(
            button = binding.ownerNameSpinner,
            hintText = getString(R.string.hint_select_owner)
        ) { owner -> viewModel.selectOwner(owner) }.also {
            it.bindToFlow(this, lifecycleScope, viewModel.selectedOwner)
        }
    }


    private fun bindToEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.observeOneTimeEvents {
                    handleEvent(it)
                }
            }
        }
    }

    // use EID reader to look up an animal
    private fun onEIDScanned(scannedEID: String) {
        viewModel.onEIDScanned(scannedEID)
        Log.i("in onEIDScanned ", "with EID of $scannedEID")
    }

    private fun handleEvent(event: Event) {
        when (event) {
            is InputEvent -> handleInputEvent(event)
            is ValidationError -> handleValidationError(event)
            is UpdateDatabaseEvent -> handleUpdateDatabaseEvent(event)
        }
    }

    private fun handleInputEvent(inputEvent: InputEvent) {
        when (inputEvent) {
            InputEvent.AnimalNameChanged -> {
                binding.inputAnimalName.setText(viewModel.animalName)
            }
        }
    }

    private fun handleValidationError(validationError: ValidationError) {
        when (validationError) {
            is ValidationError.ScannedEIDAlreadyUsed -> {
                IdValidationErrorDialog.showEIDAlreadyInUseError(
                    this,
                    validationError.error
                )
            }

            ValidationError.IncompleteAnimalEntry -> {
                showIncompleteAnimalEntryError()
            }

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

    private fun handleUpdateDatabaseEvent(event: UpdateDatabaseEvent) {
        when (event) {
            is UpdateDatabaseEvent.Success -> {
                if (intent?.action == AddAnimal.ACTION_ADD_AND_SELECT) {
                    //TODO: Pass this animal id as a long and not an int once IDs are sorted out as longs.
                    setResult(RESULT_OK, Intent().apply {
                        putExtra(AddAnimal.EXTRA_RESULTING_ANIMAL_ID, event.animalId)
                        putExtra(AddAnimal.EXTRA_RESULTING_ANIMAL_NAME, event.animalName)
                    })
                    finish()
                } else {
                    //TODO: Discuss what, if anything else, to show here.
                    Toast.makeText(
                        this,
                        getString(R.string.toast_add_animal_success, event.animalName),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            is UpdateDatabaseEvent.Error -> {
                //TODO: Discuss what, if anything else, to show here.
                Toast.makeText(
                    this,
                    getString(R.string.toast_add_animal_failure, event.animalName),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showIncompleteAnimalEntryError() {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_title_incomplete_animal_entry)
            .setMessage(R.string.dialog_message_incomplete_animal_entry)
            .setPositiveButton(R.string.ok) { _, _ -> /*NO-OP*/ }
            .create()
            .show()
    }
}

private class ViewModelFactory(context: Context) : ViewModelProvider.Factory {

    private val appContext = context.applicationContext

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        when(modelClass) {
            SimpleAddAnimalViewModel::class.java -> {
                val dbh = DatabaseManager.getInstance(appContext)
                    .createDatabaseHandler()
                val defSettingsRepo = DefaultSettingsRepositoryImpl(
                    dbh, ActiveDefaultSettings.from(appContext)
                )
                val animalRepo = AnimalRepositoryImpl(dbh, defSettingsRepo)
                val idTypeRepo = IdTypeRepositoryImpl(dbh)
                val idColorRepo = IdColorRepositoryImpl(dbh)
                val idLocationRepo = IdLocationRepositoryImpl(dbh)
                val scrapieFlockRepo = ScrapieFlockRepositoryImpl(dbh)
                val activeDefSettings = ActiveDefaultSettings(
                    PreferenceManager.getDefaultSharedPreferences(appContext)
                )
                val loadActiveDefaultSettings = LoadActiveDefaultSettings(
                    activeDefaultSettings = activeDefSettings,
                    defSettingsRepo
                )
                val loadDefaultIdConfigs = LoadDefaultIdConfigs(
                    loadActiveDefaultSettings,
                    idTypeRepository = idTypeRepo,
                    idColorRepository = idColorRepo,
                    idLocationRepository = idLocationRepo
                )

                @Suppress("UNCHECKED_CAST")
                return SimpleAddAnimalViewModel(
                    savedStateHandle = extras.createSavedStateHandle(),
                    databaseHandler = dbh,
                    loadActiveDefaultSettings = loadActiveDefaultSettings,
                    loadDefaultIdConfigs = loadDefaultIdConfigs,
                    animalRepo = animalRepo,
                    breedRepo = BreedRepositoryImpl(dbh),
                    sexRepo = SexRepositoryImpl(dbh),
                    ownerRepo = OwnerRepositoryImpl(dbh),
                    premiseRepo = PremiseRepositoryImpl(dbh),
                    idTypeRepo = idTypeRepo,
                    idColorRepo = idColorRepo,
                    idLocationRepo = idLocationRepo,
                    idValidations = IdValidations(animalRepo),
                    baseFarmTagOnEIDFeature = BaseFarmTagOnEIDFeature(
                        loadActiveDefaultSettings
                    ),
                    autoUpdateTrichId = AutoIncrementNextTrichIdFeature(
                        loadActiveDefaultSettings = loadActiveDefaultSettings,
                        defaultSettingsRepository = defSettingsRepo
                    ),
                    scrapieFlockRepository = scrapieFlockRepo
                ) as T
            }
            else -> {
                throw IllegalStateException("${modelClass.simpleName} is not supported.")
            }
        }
    }
}
