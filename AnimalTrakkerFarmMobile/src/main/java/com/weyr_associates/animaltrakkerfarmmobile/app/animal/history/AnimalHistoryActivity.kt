package com.weyr_associates.animaltrakkerfarmmobile.app.animal.history

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayoutMediator
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.AnimalDialogs
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.add.AddAnimal
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.alert.AddAnimalAlert
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.alert.ShowAlertButtonPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.drug.AnimalDrugHistoryFragment
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.drug.AnimalDrugHistoryViewModelContract
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.evaluation.AnimalEvaluationHistoryFragment
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.evaluation.AnimalEvaluationsHistoryViewModelContract
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.notes.AnimalNotesFragment
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.notes.AnimalNotesViewModelContract
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.tissue.AnimalTissueSampleHistoryFragment
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.tissue.AnimalTissueSampleHistoryViewModelContract
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.tissue.AnimalTissueTestHistoryFragment
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.tissue.AnimalTissueTestHistoryViewModelContract
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.lookup.LookupAnimalInfo
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.lookup.LookupAnimalInfoPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.collectLatestOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.observeOneTimeEventsOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.widget.TopButtonBar
import com.weyr_associates.animaltrakkerfarmmobile.app.device.eid.EIDReaderConnection
import com.weyr_associates.animaltrakkerfarmmobile.app.permissions.RequiredPermissionsWatcher
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.AnimalRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.DefaultSettingsRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.select.SelectAnimal
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.ActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseManager
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ActivityAnimalHistoryBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.IdType

class AnimalHistoryActivity : AppCompatActivity() {

    companion object {
        fun newIntent(context: Context) =
            Intent(context, AnimalHistoryActivity::class.java)
    }

    private val binding by lazy {
        ActivityAnimalHistoryBinding.inflate(layoutInflater)
    }

    private val viewModel by viewModels<AnimalHistoryViewModel> {
        ViewModelFactory(this)
    }

    private lateinit var requiredPermissionsWatcher: RequiredPermissionsWatcher
    private lateinit var eidReaderConnection: EIDReaderConnection

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

    private val lookupAnimalInfoPresenter by lazy {
        LookupAnimalInfoPresenter(binding.lookupAnimalInfo).apply {
            addAnimalAlertLauncher = this@AnimalHistoryActivity.addAnimalAlertLauncher
            onAddAnimalWithEIDClicked = { eidNumber ->
                addAndSelectAnimalLauncher.launch(AddAnimal.Request(IdType.ID_TYPE_ID_EID, eidNumber))
            }
        }
    }

    private lateinit var animalHistoriesAdapter: AnimalHistoryTabsAdapter

    private val animalHistoryFragmentFactory = object : AnimalHistoryFragmentFactory {
        override fun createAnimalNotesFragment(): AnimalNotesFragment {
            return AnimalHistoryNotesFragment()
        }
        override fun createAnimalDrugHistoryFragment(): AnimalDrugHistoryFragment {
            return AnimalHistoryDrugsFragment()
        }
        override fun createAnimalTissueSampleHistoryFragment(): AnimalTissueSampleHistoryFragment {
            return AnimalHistoryTissueSamplesFragment()
        }
        override fun createAnimalTissueTestSampleFragment(): AnimalTissueTestHistoryFragment {
            return AnimalHistoryTissueTestsFragment()
        }
        override fun createAnimalEvaluationHistoryFragment(): AnimalEvaluationHistoryFragment {
            return AnimalHistoryEvaluationsFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        animalHistoriesAdapter = AnimalHistoryTabsAdapter(this, animalHistoryFragmentFactory)
        eidReaderConnection = EIDReaderConnection(this, lifecycle)
            .also { lifecycle.addObserver(it) }
        requiredPermissionsWatcher = RequiredPermissionsWatcher(this)
            .also { lifecycle.addObserver(it) }
        with(binding) {
            with(buttonPanelTop) {
                show(
                    TopButtonBar.UI_SCANNER_STATUS or
                    TopButtonBar.UI_SCAN_EID or
                    TopButtonBar.UI_LOOKUP_ANIMAL or
                    TopButtonBar.UI_SHOW_ALERT
                )
                scanEIDButton.setOnClickListener {
                    eidReaderConnection.toggleScanningEID()
                }
                lookupAnimalButton.setOnClickListener {
                    selectAnimalLauncher.launch(null)
                }
            }
            with(pagerAnimalHistories) {
                adapter = animalHistoriesAdapter
                isUserInputEnabled = false
            }
            TabLayoutMediator(
                binding.tabsAnimalHistories,
                binding.pagerAnimalHistories,
                true,
                false,
                animalHistoriesAdapter
            ).attach()
        }
        collectLatestOnStart(viewModel.animalInfoState) {
            lookupAnimalInfoPresenter.animalInfoState = it
            showAlertButtonPresenter.animalInfoState = it
            binding.containerAnimalHistories.isVisible =
                it is LookupAnimalInfo.AnimalInfoState.Loaded
        }
        collectLatestOnStart(eidReaderConnection.deviceConnectionState) { connectionState ->
            binding.buttonPanelTop.updateEIDReaderConnectionState(connectionState)
        }
        collectLatestOnStart(eidReaderConnection.isScanningForEID) { isScanning ->
            binding.buttonPanelTop.showScanningEID = isScanning
        }
        observeOneTimeEventsOnStart(eidReaderConnection.onEIDScanned) { eidNumber ->
            viewModel.lookupAnimalInfoByEIDNumber(eidNumber)
        }
        observeOneTimeEventsOnStart(viewModel.animalAlertsEvent) { event ->
            AnimalDialogs.showAnimalAlert(this, event.alerts)
        }
    }

    class AnimalHistoryNotesFragment : AnimalNotesFragment() {
        override val viewModel: AnimalNotesViewModelContract
            by activityViewModels<AnimalHistoryViewModel> {
                ViewModelFactory(requireContext())
            }
    }

    class AnimalHistoryDrugsFragment : AnimalDrugHistoryFragment() {
        override val viewModel: AnimalDrugHistoryViewModelContract
            by activityViewModels<AnimalHistoryViewModel> {
                ViewModelFactory(requireContext())
            }
    }

    class AnimalHistoryTissueSamplesFragment : AnimalTissueSampleHistoryFragment() {
        override val viewModel: AnimalTissueSampleHistoryViewModelContract
            by activityViewModels<AnimalHistoryViewModel> {
                ViewModelFactory(requireContext())
            }
    }

    class AnimalHistoryTissueTestsFragment : AnimalTissueTestHistoryFragment() {
        override val viewModel: AnimalTissueTestHistoryViewModelContract
            by activityViewModels<AnimalHistoryViewModel> {
                ViewModelFactory(requireContext())
            }
    }

    class AnimalHistoryEvaluationsFragment : AnimalEvaluationHistoryFragment() {
        override val viewModel: AnimalEvaluationsHistoryViewModelContract
            by activityViewModels<AnimalHistoryViewModel> {
                ViewModelFactory(requireContext())
            }
    }

    private class ViewModelFactory(context: Context) : ViewModelProvider.Factory {

        private val appContext = context.applicationContext

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return when (modelClass) {
                AnimalHistoryViewModel::class.java -> {
                    val databaseHandler = DatabaseManager.getInstance(appContext)
                        .createDatabaseHandler()
                    val defaultSettingsRepo = DefaultSettingsRepositoryImpl(
                        databaseHandler, ActiveDefaultSettings.from(appContext)
                    )
                    val animalRepo = AnimalRepositoryImpl(databaseHandler, defaultSettingsRepo)
                    @Suppress("UNCHECKED_CAST")
                    AnimalHistoryViewModel(animalRepo) as T
                }
                else -> throw IllegalStateException("${modelClass.simpleName} is not supported.")
            }
        }
    }
}
