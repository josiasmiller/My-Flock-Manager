package com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.edit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.preference.PreferenceManager
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.federal.SuggestDefaultScrapieFlockNumber
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.trich.AutoIncrementNextTrichIdFeature
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.input.IdInputSettings
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.validation.IdValidationErrorDialog
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.validation.IdValidations
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.collectLatestOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.observeOneTimeEventsOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.ItemSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.core.widget.TopButtonBar
import com.weyr_associates.animaltrakkerfarmmobile.app.device.eid.EIDReaderConnection
import com.weyr_associates.animaltrakkerfarmmobile.app.permissions.RequiredPermissionsWatcher
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.AnimalRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.DefaultSettingsRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.IdTypeRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.ScrapieFlockRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.select.idColorSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.select.idLocationSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.select.idTypeSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.ActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseManager
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ActivityEditAnimalIdBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalBasicInfo
import com.weyr_associates.animaltrakkerfarmmobile.model.IdInfo
import com.weyr_associates.animaltrakkerfarmmobile.model.IdColor
import com.weyr_associates.animaltrakkerfarmmobile.model.IdLocation
import com.weyr_associates.animaltrakkerfarmmobile.model.IdType

class EditAnimalIdActivity : AppCompatActivity() {

    companion object {
        @JvmStatic
        fun newIntent(context: Context, animalIdToEdit: IdInfo, animalInfo: AnimalBasicInfo) =
            Intent(context, EditAnimalIdActivity::class.java).apply {
                putExtra(EditAnimalId.EXTRA_ANIMAL_ID_TO_EDIT, animalIdToEdit)
                putExtra(EditAnimalId.EXTRA_ANIMAL_INFO, animalInfo)
            }
    }

    private val binding: ActivityEditAnimalIdBinding by lazy {
        ActivityEditAnimalIdBinding.inflate(layoutInflater)
    }

    private val viewModel: EditAnimalIdViewModel by viewModels {
        EditAnimalIdViewModelFactory(this@EditAnimalIdActivity)
    }

    private lateinit var idTypePresenter: ItemSelectionPresenter<IdType>
    private lateinit var idColorPresenter: ItemSelectionPresenter<IdColor>
    private lateinit var idLocationPresenter: ItemSelectionPresenter<IdLocation>

    private lateinit var eidReaderConnection: EIDReaderConnection
    private val requiredPermissionsWatcher = RequiredPermissionsWatcher(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        with(binding.buttonPanelTop) {
            show(
                TopButtonBar.UI_SCANNER_STATUS or
                        TopButtonBar.UI_SCAN_EID or
                        TopButtonBar.UI_CLEAR_DATA
            )
            scanEIDButton.setOnClickListener {
                scanEID()
            }
            clearDataButton.setOnClickListener {
                viewModel.clearData()
            }
        }

        with(binding.animalIdInfo) {
            textIdNumber.text = viewModel.originalId.number
            textIdTypeName.text = viewModel.originalId.type.name
            textIdColorName.text = viewModel.originalId.color.abbreviation
            textIdLocationName.text = viewModel.originalId.location.abbreviation
        }

        with(binding.inputAnimalId) {
            inputIdNumber.setText(viewModel.idNumber)
            inputIdNumber.addTextChangedListener {
                viewModel.idNumber = it.toString()
            }
            buttonAddId.setText(R.string.text_update_id)
            buttonAddId.setOnClickListener {
                viewModel.updateId()
            }
        }

        idTypePresenter = idTypeSelectionPresenter(
            button = binding.inputAnimalId.spinnerIdType
        ) { idType ->
            viewModel.selectIdType(idType)
        }.also {
            it.bindToFlow(this, lifecycleScope, viewModel.selectedIdType)
        }
        collectLatestOnStart(viewModel.selectedIdType) { idType ->
            with(binding.inputAnimalId.inputIdNumber) {
                isEnabled = idType != null
                IdInputSettings.applyTo(this, idType?.id)
            }
        }
        idColorPresenter = idColorSelectionPresenter(
            button = binding.inputAnimalId.spinnerIdColor
        ) { idColor ->
            viewModel.selectIdColor(idColor)
        }.also {
            it.bindToFlow(this, lifecycleScope, viewModel.selectedIdColor)
        }

        idLocationPresenter = idLocationSelectionPresenter(
            button = binding.inputAnimalId.spinnerIdLocation
        ) { idLocation ->
            viewModel.selectIdLocation(idLocation)
        }.also {
            it.bindToFlow(this, lifecycleScope, viewModel.selectedIdLocation)
        }

        collectLatestOnStart(viewModel.canClearData) { canClearData ->
            binding.buttonPanelTop.clearDataButton.isEnabled = canClearData
        }
        collectLatestOnStart(viewModel.canUpdateId) { canUpdateId ->
            binding.inputAnimalId.buttonAddId.isEnabled = canUpdateId
        }

        observeOneTimeEventsOnStart(viewModel.events, ::handleEvent)

        eidReaderConnection = EIDReaderConnection(this, lifecycle)
            .also { lifecycle.addObserver(it) }

        collectLatestOnStart(eidReaderConnection.deviceConnectionState) { connectionState ->
            binding.buttonPanelTop.updateEIDReaderConnectionState(connectionState)
        }
        collectLatestOnStart(eidReaderConnection.isScanningForEID) { isScanning ->
            binding.buttonPanelTop.showScanningEID = isScanning
        }
        observeOneTimeEventsOnStart(eidReaderConnection.onEIDScanned, ::onEIDScanned)

        lifecycle.addObserver(requiredPermissionsWatcher)
    }

    private fun scanEID() {
        eidReaderConnection.toggleScanningEID()
    }

    private fun onEIDScanned(eidNumber: String) {
        viewModel.onEIDScanned(eidNumber)
    }

    private fun handleEvent(event: EditAnimalIdViewModel.Event) {
        when (event) {
            EditAnimalIdViewModel.IdNumberChanged -> {
                binding.inputAnimalId.inputIdNumber.setText(viewModel.idNumber)
            }
            is EditAnimalIdViewModel.ValidationError.ScannedEIDAlreadyUsed -> {
                IdValidationErrorDialog.showEIDAlreadyInUseError(this, event.error)
            }
            EditAnimalIdViewModel.IdUpdateSucceeded -> {
                setResult(RESULT_OK)
                finish()
            }
            EditAnimalIdViewModel.IdUpdateFailed -> {
                AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_title_animal_id_update_failed)
                    .setMessage(R.string.dialog_message_animal_id_update_failed)
                    .setPositiveButton(R.string.ok) { _, _ -> /*NO-OP*/ }
                    .create()
                    .show()
            }
            EditAnimalIdViewModel.ValidationError.IdEntryRequired -> {
                IdValidationErrorDialog.showIdEntryIsRequiredError(this)
            }
            EditAnimalIdViewModel.ValidationError.PartialIdEntry -> {
                IdValidationErrorDialog.showPartialIdEntryError(this)
            }
            is EditAnimalIdViewModel.ValidationError.InvalidIdNumberFormat -> {
                IdValidationErrorDialog.showIdNumberFormatError(this, event.idEntry)
            }
            is EditAnimalIdViewModel.ValidationError.InvalidIdCombination -> {
                IdValidationErrorDialog.showIdCombinationError(this, event.error)
            }
        }
    }

    private class EditAnimalIdViewModelFactory(context: Context) : ViewModelProvider.Factory {

        private val appContext = context.applicationContext

        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return when (modelClass) {
                EditAnimalIdViewModel::class.java -> {
                    val databaseHandler = DatabaseManager.getInstance(appContext)
                        .createDatabaseHandler()
                    val defSettingsRepo = DefaultSettingsRepositoryImpl(
                        databaseHandler, ActiveDefaultSettings.from(appContext)
                    )
                    val activeDefaultSettings = ActiveDefaultSettings(
                        PreferenceManager.getDefaultSharedPreferences(appContext)
                    )
                    val loadActiveDefaultSettings = LoadActiveDefaultSettings(
                        activeDefaultSettings = activeDefaultSettings,
                        defaultSettingsRepo = defSettingsRepo
                    )
                    val animalRepository = AnimalRepositoryImpl(databaseHandler, defSettingsRepo)
                    val idTypeRepository = IdTypeRepositoryImpl(databaseHandler)
                    val scrapieFlockRepository = ScrapieFlockRepositoryImpl(databaseHandler)
                    @Suppress("UNCHECKED_CAST")
                    EditAnimalIdViewModel(
                        extras.createSavedStateHandle(),
                        animalRepository,
                        idTypeRepository,
                        IdValidations(animalRepository),
                        SuggestDefaultScrapieFlockNumber(
                            loadActiveDefaultSettings,
                            scrapieFlockRepository
                        ),
                        AutoIncrementNextTrichIdFeature(
                            loadActiveDefaultSettings = loadActiveDefaultSettings,
                            defaultSettingsRepository = defSettingsRepo
                        )
                    ) as T
                }
                else -> {
                    throw IllegalStateException("${modelClass.simpleName} is not supported.")
                }
            }
        }
    }
}