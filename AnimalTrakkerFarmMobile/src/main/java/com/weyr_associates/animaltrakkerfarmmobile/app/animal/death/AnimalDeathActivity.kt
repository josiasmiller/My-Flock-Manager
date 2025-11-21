package com.weyr_associates.animaltrakkerfarmmobile.app.animal.death

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.AnimalDialogs
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.add.AddAnimal
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.alert.AddAnimalAlert
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.alert.ShowAlertButtonPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.lookup.LookupAnimalInfo
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.lookup.LookupAnimalInfoPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.collectLatestOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.observeOneTimeEventsOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.ItemSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.core.widget.TopButtonBar
import com.weyr_associates.animaltrakkerfarmmobile.app.device.eid.EIDReaderConnection
import com.weyr_associates.animaltrakkerfarmmobile.app.permissions.RequiredPermissionsWatcher
import com.weyr_associates.animaltrakkerfarmmobile.app.reporting.errors.observeErrorReports
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.AnimalRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.DefaultSettingsRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.select.SelectAnimal
import com.weyr_associates.animaltrakkerfarmmobile.app.select.dateSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.select.deathReasonSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.ActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseManager
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ActivityAnimalDeathBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.DeathReason
import com.weyr_associates.animaltrakkerfarmmobile.model.IdType
import com.weyr_associates.animaltrakkerfarmmobile.model.SexStandard
import java.time.LocalDate

class AnimalDeathActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityAnimalDeathBinding.inflate(layoutInflater)
    }

    private val viewModel by viewModels<AnimalDeathViewModel> {
        ViewModelFactory(this)
    }

    private lateinit var selectAnimalLauncher: ActivityResultLauncher<SexStandard?>
    private lateinit var addScannedAnimalLauncher: ActivityResultLauncher<AddAnimal.Request>

    private lateinit var requiredPermissionsWatcher: RequiredPermissionsWatcher
    private lateinit var eidReaderConnection: EIDReaderConnection

    private val addAnimalAlertLauncher = registerForActivityResult(AddAnimalAlert.Contract()) { result ->
        if (result.success) { viewModel.lookupAnimalInfoById(result.animalId) }
    }

    private val showAlertButtonPresenter by lazy {
        ShowAlertButtonPresenter(this, binding.buttonPanelTop.showAlertButton)
    }

    private val lookupAnimalInfoPresenter by lazy {
        LookupAnimalInfoPresenter(binding.lookupAnimalInfo).apply {
            addAnimalAlertLauncher = this@AnimalDeathActivity.addAnimalAlertLauncher
            onAddAnimalWithEIDClicked = { eidNumber ->
                addScannedAnimalLauncher.launch(
                    AddAnimal.Request(IdType.ID_TYPE_ID_EID, eidNumber)
                )
            }
        }
    }

    private lateinit var deathReasonItemPresenter: ItemSelectionPresenter<DeathReason>
    private lateinit var deathDateItemPresenter: ItemSelectionPresenter<LocalDate>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        with(binding.buttonPanelTop) {
            show(TopButtonBar.UI_ALL or TopButtonBar.UI_ACTION_UPDATE_DATABASE)

            scanEIDButton.setOnClickListener {
                eidReaderConnection.toggleScanningEID()
            }

            lookupAnimalButton.setOnClickListener { selectAnimalLauncher.launch(null) }
            clearDataButton.setOnClickListener { viewModel.clearData() }
            mainActionButton.setOnClickListener { viewModel.saveToDatabase() }
        }

        selectAnimalLauncher = registerForActivityResult(SelectAnimal.Contract()) { animalId ->
            animalId?.let { viewModel.lookupAnimalInfoById(it) }
        }

        addScannedAnimalLauncher = registerForActivityResult(AddAnimal.Contract()) { result ->
            result?.let { viewModel.lookupAnimalInfoById(it.animalId) }
        }

        eidReaderConnection = EIDReaderConnection(this, lifecycle).also {
            lifecycle.addObserver(it)
        }
        requiredPermissionsWatcher = RequiredPermissionsWatcher(this).also {
            lifecycle.addObserver(it)
        }

        collectLatestOnStart(eidReaderConnection.isScanningForEID) {
            binding.buttonPanelTop.showScanningEID = it
        }
        collectLatestOnStart(eidReaderConnection.deviceConnectionState) {
            binding.buttonPanelTop.updateEIDReaderConnectionState(it)
        }
        observeOneTimeEventsOnStart(eidReaderConnection.onEIDScanned) { eidNumber ->
            viewModel.lookupAnimalInfoByEIDNumber(eidNumber)
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
            lookupAnimalInfoPresenter.animalInfoState = animalInfoState
            showAlertButtonPresenter.animalInfoState = animalInfoState
            when (animalInfoState) {
                is LookupAnimalInfo.AnimalInfoState.Loaded -> {
                    binding.containerAnimalDeathInputs.isVisible =
                        !animalInfoState.animalBasicInfo.isDead
                    binding.containerDeadAnimalNotice.isVisible =
                        animalInfoState.animalBasicInfo.isDead
                }
                else -> {
                    binding.containerAnimalDeathInputs.isGone = true
                    binding.containerDeadAnimalNotice.isGone = true
                }
            }
        }

        deathReasonItemPresenter = deathReasonSelectionPresenter(
            button = binding.spinnerAnimalDeathReason
        ) { deathReason ->
            viewModel.updateDeathReason(deathReason)
        }.also {
            it.bindToFlow(this, lifecycleScope, viewModel.animalDeathReason)
        }

        deathDateItemPresenter = dateSelectionPresenter(
            button = binding.spinnerAnimalDeathDate
        ) { deathDate ->
            viewModel.updateDeathDate(deathDate)
        }.also {
            it.bindToFlow(this, lifecycleScope, viewModel.animalDeathDate)
        }
    }

    private fun handleEvent(event: AnimalDeathViewModel.Event) {
        when (event) {
            is AnimalDeathViewModel.AnimalAlertEvent -> {
                AnimalDialogs.showAnimalAlert(this, event.alerts)
            }
            AnimalDeathViewModel.UpdateDatabaseSuccess -> {
                Toast.makeText(
                    this,
                    R.string.toast_mark_animal_deceased_success,
                    Toast.LENGTH_SHORT
                ).show()
            }
            AnimalDeathViewModel.UpdateDatabaseFailure -> {
                Toast.makeText(
                    this,
                    R.string.toast_mark_animal_deceased_failure,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

private class ViewModelFactory(context: Context) : ViewModelProvider.Factory {

    private val appContext = context.applicationContext

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            AnimalDeathViewModel::class.java -> {
                val databaseHandler = DatabaseManager.getInstance(appContext)
                    .createDatabaseHandler()
                val defaultSettingsRepo = DefaultSettingsRepositoryImpl(
                    databaseHandler, ActiveDefaultSettings.from(appContext)
                )
                val animalRepo = AnimalRepositoryImpl(databaseHandler, defaultSettingsRepo)
                @Suppress("UNCHECKED_CAST")
                AnimalDeathViewModel(
                    animalRepo = animalRepo
                ) as T
            }
            else -> throw IllegalStateException("${modelClass.simpleName} is not supported.")
        }
    }
}
