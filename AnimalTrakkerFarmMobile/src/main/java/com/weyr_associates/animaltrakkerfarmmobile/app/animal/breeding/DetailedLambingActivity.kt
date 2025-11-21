package com.weyr_associates.animaltrakkerfarmmobile.app.animal.breeding

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.AnimalDialogs
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.add.AddAnimal
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.alert.AddAnimalAlert
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.alert.ShowAlertButtonPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.breeding.DetailedLambingViewModel.AbleToBirth
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.breeding.DetailedLambingViewModel.AlertUnableToBirth
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.breeding.DetailedLambingViewModel.BirthingStatus
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.breeding.DetailedLambingViewModel.Event
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.breeding.DetailedLambingViewModel.UnableToBirth
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.lookup.LookupAnimalInfo
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.lookup.LookupAnimalInfoPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.collectLatestOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.observeOneTimeEventsOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.widget.TopButtonBar
import com.weyr_associates.animaltrakkerfarmmobile.app.device.eid.EIDReaderConnection
import com.weyr_associates.animaltrakkerfarmmobile.app.model.formatForDisplay
import com.weyr_associates.animaltrakkerfarmmobile.app.model.itemCallbackUsingIdentityAndContent
import com.weyr_associates.animaltrakkerfarmmobile.app.permissions.RequiredPermissionsWatcher
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.AnimalRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.DefaultSettingsRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.SpeciesRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.select.SelectAnimal
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.ActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseManager
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ActivityDetailedLambingBinding
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ItemFemaleBreedingBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.FemaleBreedingHistoryEntry
import com.weyr_associates.animaltrakkerfarmmobile.model.IdType
import kotlinx.coroutines.flow.combine

class DetailedLambingActivity : AppCompatActivity() {

    companion object {
        fun newIntent(context: Context) = Intent(context, DetailedLambingActivity::class.java)
        private const val REQUEST_CODE_ADD_OFFSPRING = 10
    }

    private val binding by lazy {
        ActivityDetailedLambingBinding.inflate(layoutInflater)
    }

    private val viewModel by viewModels<DetailedLambingViewModel> {
        ViewModelFactory(this)
    }

    private val femaleBreedingHistoryAdapter = FemaleBreedingHistoryAdapter()

    private lateinit var requiredPermissionsWatcher: RequiredPermissionsWatcher
    private lateinit var eidReaderConnection: EIDReaderConnection

    private val lookupAnimalInfoPresenter by lazy {
        LookupAnimalInfoPresenter(binding.lookupAnimalInfo).apply {
            addAnimalAlertLauncher = this@DetailedLambingActivity.addAnimalAlertLauncher
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

    private val addAndSelectAnimalLauncher = registerForActivityResult(AddAnimal.Contract()) { result ->
        result?.let { viewModel.lookupAnimalInfoById(result.animalId) }
    }

    private val addAnimalAlertLauncher = registerForActivityResult(AddAnimalAlert.Contract()) { result ->
        if (result.success) { viewModel.lookupAnimalInfoById(result.animalId) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        with(binding.buttonPanelTop) {
            show(TopButtonBar.UI_SCANNER_STATUS or
                    TopButtonBar.UI_SCAN_EID or
                    TopButtonBar.UI_LOOKUP_ANIMAL or
                    TopButtonBar.UI_SHOW_ALERT
            )

            scanEIDButton.setOnClickListener {
                eidReaderConnection.toggleScanningEID()
            }

            lookupAnimalButton.setOnClickListener { selectAnimalLauncher.launch(null) }
        }

        eidReaderConnection = EIDReaderConnection(this, lifecycle).also {
            lifecycle.addObserver(it)
        }

        requiredPermissionsWatcher = RequiredPermissionsWatcher(this).also {
            lifecycle.addObserver(it)
        }

        with(binding.recyclerFemaleBreedingHistory) {
            adapter = femaleBreedingHistoryAdapter
            layoutManager = LinearLayoutManager(
                this@DetailedLambingActivity,
                RecyclerView.VERTICAL,
                false
            )
            addItemDecoration(
                DividerItemDecoration(
                    this@DetailedLambingActivity,
                    DividerItemDecoration.VERTICAL
                )
            )
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

        observeOneTimeEventsOnStart(viewModel.animalAlertsEvent) { event ->
            AnimalDialogs.showAnimalAlert(this, event.alerts)
        }

        observeOneTimeEventsOnStart(viewModel.events, ::handleEvent)

        collectLatestOnStart(viewModel.animalInfoState) { animalInfoState ->
            lookupAnimalInfoPresenter.animalInfoState = animalInfoState
            showAlertButtonPresenter.animalInfoState = animalInfoState
            if (animalInfoState is LookupAnimalInfo.AnimalInfoState.Loaded) {
                binding.buttonAddOffspring.setOnClickListener {
                    startAddOffspring(animalInfoState.animalBasicInfo.id)
                }
            } else {
                binding.buttonAddOffspring.setOnClickListener(null)
            }
        }

        collectLatestOnStart(
            combine(viewModel.femaleBreedingHistory, viewModel.birthingStatus, ::Pair)
        ) { (birthingHistory, birthingStatus) -> updateBreedingHistoryDisplay(birthingStatus, birthingHistory) }

        collectLatestOnStart(viewModel.femaleBreedingHistory) { femaleBreedingHistory ->
            femaleBreedingHistoryAdapter.submitList(femaleBreedingHistory)
        }

        collectLatestOnStart(viewModel.canAddOffSpring) { canAddOffspring ->
            binding.buttonAddOffspring.isEnabled = canAddOffspring
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_ADD_OFFSPRING) {
            viewModel.refreshAnimalInfo()
            viewModel.refreshBreedingHistory()
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun handleEvent(event: Event) {
        when (event) {
            is AlertUnableToBirth -> {
                AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_title_unable_to_birth)
                    .setMessage(messageForUnableToBirthReason(event.reason))
                    .setPositiveButton(R.string.ok) { _, _ -> /*NO-OP*/ }
                    .create()
                    .show()
            }
        }
    }

    private fun updateBreedingHistoryDisplay(birthingStatus: BirthingStatus, birthingHistory: List<FemaleBreedingHistoryEntry>) {
        when (birthingStatus) {
            AbleToBirth -> {
                if (birthingHistory.isNotEmpty()) {
                    femaleBreedingHistoryAdapter.submitList(birthingHistory)
                    binding.recyclerFemaleBreedingHistory.isVisible = true
                    binding.textContentAreaMessage.text = ""
                    binding.textContentAreaMessage.isVisible = false
                } else {
                    femaleBreedingHistoryAdapter.submitList(emptyList())
                    binding.recyclerFemaleBreedingHistory.isVisible = false
                    binding.textContentAreaMessage.setText(R.string.text_no_breeding_history)
                    binding.textContentAreaMessage.isVisible = true
                }
            }
            is UnableToBirth -> {
                femaleBreedingHistoryAdapter.submitList(emptyList())
                binding.recyclerFemaleBreedingHistory.isVisible = false
                binding.textContentAreaMessage.setText(
                    messageForUnableToBirthReason(birthingStatus.reason)
                )
                binding.textContentAreaMessage.isVisible = true
            }
        }
    }

    private fun messageForUnableToBirthReason(reason: UnableToBirth.Reason): Int {
        return when (reason) {
            UnableToBirth.Reason.NO_ANIMAL -> R.string.text_unable_to_birth_reason_no_animal
            UnableToBirth.Reason.WRONG_SEX -> R.string.text_unable_to_birth_reason_wrong_sex
            UnableToBirth.Reason.TOO_YOUNG -> R.string.text_unable_to_birth_reason_too_young
            UnableToBirth.Reason.IS_DEAD -> R.string.text_unable_to_birth_reason_dead
        }
    }

    private fun startAddOffspring(damAnimalId: EntityId) {
        //Starting for result as a cheap way to trigger refreshes of the birthing history
        startActivityForResult(AddOffspringActivity.newIntent(this, damAnimalId), REQUEST_CODE_ADD_OFFSPRING)
    }

    private class FemaleBreedingHistoryAdapter : ListAdapter<FemaleBreedingHistoryEntry, FemaleBreedingViewHolder>(
        itemCallbackUsingIdentityAndContent()
    ) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FemaleBreedingViewHolder {
            return FemaleBreedingViewHolder(
                ItemFemaleBreedingBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: FemaleBreedingViewHolder, position: Int) {
            holder.bind(currentList[position])
        }
    }

    private class FemaleBreedingViewHolder(
        private val binding: ItemFemaleBreedingBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(femaleBreedingHistoryEntry: FemaleBreedingHistoryEntry) {
            binding.textBirthingDate.text = femaleBreedingHistoryEntry.eventDate.formatForDisplay()
            binding.textBirthingTime.text = femaleBreedingHistoryEntry.eventTime.formatForDisplay()
            @SuppressLint("SetTextI18n")
            binding.textBirthingNotes.text = femaleBreedingHistoryEntry.birthingNotes
        }
    }

    private class ViewModelFactory(context: Context) : ViewModelProvider.Factory {

        private val appContext = context.applicationContext

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return when (modelClass) {
                DetailedLambingViewModel::class.java -> {
                    val databaseHandler = DatabaseManager.getInstance(appContext)
                        .createDatabaseHandler()
                    val defaultSettingsRepo = DefaultSettingsRepositoryImpl(
                        databaseHandler, ActiveDefaultSettings.from(appContext)
                    )
                    val animalRepo = AnimalRepositoryImpl(databaseHandler, defaultSettingsRepo)
                    val speciesRepo = SpeciesRepositoryImpl(databaseHandler)
                    @Suppress("UNCHECKED_CAST")
                    DetailedLambingViewModel(
                        animalRepo = animalRepo,
                        speciesRepository = speciesRepo
                    ) as T
                }
                else -> throw IllegalStateException("${modelClass.simpleName} is not supported.")
            }
        }
    }
}
