package com.weyr_associates.animaltrakkerfarmmobile.app.animal.premise

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.AnimalDialogs
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.add.AddAnimal
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.alert.AddAnimalAlert
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.alert.ShowAlertButtonPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.lookup.LookupAnimalInfo
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.lookup.LookupAnimalInfoPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.premise.MoveToPremiseViewModel.AnimalRequiredToBeAlive
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.collectLatestOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.observeOneTimeEventsOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.widget.recyclerview.GridSpacerItemDecoration
import com.weyr_associates.animaltrakkerfarmmobile.app.core.widget.recyclerview.PlacardViewHolder
import com.weyr_associates.animaltrakkerfarmmobile.app.device.eid.EIDReaderConnection
import com.weyr_associates.animaltrakkerfarmmobile.app.device.scale.ScaleDeviceConnection
import com.weyr_associates.animaltrakkerfarmmobile.app.permissions.RequiredPermissionsWatcher
import com.weyr_associates.animaltrakkerfarmmobile.app.reporting.errors.observeErrorReports
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.AnimalRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.DefaultSettingsRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.PremiseRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.select.SelectAnimal
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.ActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseManager
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ActivityMoveToPremiseBinding
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ItemPremisePlacardBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.IdType
import com.weyr_associates.animaltrakkerfarmmobile.model.Premise
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class MoveToPremiseActivity : AppCompatActivity() {

    companion object {
        fun newIntent(context: Context) = Intent(context, MoveToPremiseActivity::class.java)
    }

    private val viewModel by viewModels<MoveToPremiseViewModel> {
        ViewModelFactory(this)
    }

    private val binding: ActivityMoveToPremiseBinding by lazy {
        ActivityMoveToPremiseBinding.inflate(layoutInflater)
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

    private val showAlertButtonPresenter by lazy {
        ShowAlertButtonPresenter(this, binding.buttonPanelTop.showAlertButton)
    }

    private val lookupAnimalInfoPresenter by lazy {
        LookupAnimalInfoPresenter(binding.lookupAnimalInfo).apply {
            addAnimalAlertLauncher = this@MoveToPremiseActivity.addAnimalAlertLauncher
            displayAnimalPremise = true
            onAddAnimalWithEIDClicked = { eidNumber ->
                addAndSelectAnimalLauncher.launch(AddAnimal.Request(IdType.ID_TYPE_ID_EID, eidNumber))
            }
        }
    }

    private lateinit var eidReaderConnection: EIDReaderConnection
    private lateinit var scaleDeviceConnection: ScaleDeviceConnection
    private lateinit var requiredPermissionsWatcher: RequiredPermissionsWatcher

    private val premisesAdapter = PremiseAdapter(::onPremiseSelected)

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
                    viewModel.saveData()
                }
            }
        }
        with(binding.recyclerPremises) {
            layoutManager = GridLayoutManager(
                this@MoveToPremiseActivity, 2
            )
            addItemDecoration(
                GridSpacerItemDecoration(
                    this@MoveToPremiseActivity, 2
                )
            )
            adapter = premisesAdapter
        }
        collectLatestOnStart(viewModel.animalInfoState) {
            lookupAnimalInfoPresenter.animalInfoState = it
            showAlertButtonPresenter.animalInfoState = it
        }
        collectLatestOnStart(viewModel.currentPremise) {
            lookupAnimalInfoPresenter.animalPremise = it
        }
        collectLatestOnStart(eidReaderConnection.deviceConnectionState) { connectionState ->
            binding.buttonPanelTop.updateEIDReaderConnectionState(connectionState)
        }
        collectLatestOnStart(eidReaderConnection.isScanningForEID) { isScanning ->
            binding.buttonPanelTop.showScanningEID = isScanning
        }
        collectLatestOnStart(viewModel.canClearData) { canClear ->
            binding.buttonPanelTop.clearDataButton.isEnabled = canClear
        }
        collectLatestOnStart(viewModel.canSaveData) { canSave ->
            binding.buttonPanelTop.mainActionButton.isEnabled = canSave
        }
        observeOneTimeEventsOnStart(eidReaderConnection.onEIDScanned) { eidNumber ->
            viewModel.lookupAnimalInfoByEIDNumber(eidNumber)
        }
        observeOneTimeEventsOnStart(viewModel.animalAlertsEvent) { event ->
            AnimalDialogs.showAnimalAlert(this, event.alerts)
        }
        collectLatestOnStart(viewModel.availablePremises) { premises ->
            onAvailablePremisesChanged(premises)
        }
        collectLatestOnStart(viewModel.selectedPremise) { premise ->
            onSelectedPremiseChanged(premise)
        }
        collectLatestOnStart(viewModel.currentPremise) { premise ->
            onCurrentPremiseChanged(premise)
        }
        collectLatestOnStart(
            combine(
                viewModel.availablePremises.map { !it.isNullOrEmpty() },
                viewModel.alreadyMovedToday
            ) { hasAvailablePremises, alreadyMovedToday -> Pair(hasAvailablePremises, alreadyMovedToday)}
        ) { visibilityPair ->
            onUpdateViewConfiguration(visibilityPair.first, visibilityPair.second)
        }
        observeOneTimeEventsOnStart(viewModel.events, ::handleEvent)
        observeErrorReports(viewModel.errorReportFlow)
    }

    private fun onAvailablePremisesChanged(premises: List<Premise>?) {
        premisesAdapter.premises = premises ?: emptyList()
    }

    private fun onSelectedPremiseChanged(premise: Premise?) {
        premisesAdapter.selectedPremise = premise
    }

    private fun onCurrentPremiseChanged(premise: Premise?) {
        premisesAdapter.currentPremise = premise
    }

    private fun onUpdateViewConfiguration(hasAvailablePremises: Boolean, alreadyMovedToday: Boolean) {
        binding.recyclerPremises.isVisible = !alreadyMovedToday && hasAvailablePremises
        binding.containerNoAvailablePremises.isVisible = !alreadyMovedToday && !hasAvailablePremises
        binding.containerAnimalAlreadyMoved.isVisible = alreadyMovedToday
    }

    private fun onPremiseSelected(premise: Premise?) {
        viewModel.selectPremise(premise)
    }

    private fun handleEvent(event: MoveToPremiseViewModel.Event) {
        when (event) {
            AnimalRequiredToBeAlive -> {
                LookupAnimalInfo.Dialogs.showAnimalRequiredToBeAlive(this, viewModel)
            }
        }
    }

    private class ViewModelFactory(context: Context) : ViewModelProvider.Factory {

        private val appContext = context.applicationContext

        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return when (modelClass) {
                MoveToPremiseViewModel::class.java -> {
                    val databaseHandler = DatabaseManager.getInstance(appContext)
                        .createDatabaseHandler()
                    val defaultSettingsRepo = DefaultSettingsRepositoryImpl(
                        databaseHandler, ActiveDefaultSettings.from(appContext)
                    )
                    val animalRepo = AnimalRepositoryImpl(databaseHandler, defaultSettingsRepo)
                    val premiseRepo = PremiseRepositoryImpl(databaseHandler)
                    val loadDefaultSettings = LoadActiveDefaultSettings.from(appContext, databaseHandler)
                    @Suppress("UNCHECKED_CAST")
                    MoveToPremiseViewModel(
                        animalRepository = animalRepo,
                        premiseRepository = premiseRepo,
                        loadDefaultSettings = loadDefaultSettings
                    ) as T
                }
                else -> throw IllegalStateException("${modelClass.simpleName} is not supported.")
            }
        }
    }
}

private class PremiseAdapter(private val onSelected: (Premise) -> Unit) : RecyclerView.Adapter<PremiseViewHolder>() {

    var premises: List<Premise> = emptyList()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var selectedPremise: Premise? = null
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            val changed = value != field
            if (changed) {
                field = value
                notifyDataSetChanged()
            }
        }

    var currentPremise: Premise? = null
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            val changed = value != field
            if (changed) {
                field = value
                notifyDataSetChanged()
            }
        }

    override fun getItemCount(): Int {
        return premises.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PremiseViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return PremiseViewHolder(ItemPremisePlacardBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: PremiseViewHolder, position: Int) {
        val premise = premises[position]
        val isSelected = premise.id == selectedPremise?.id
        val isCurrent = premise.id == currentPremise?.id
        holder.bind(premise, isSelected, isCurrent, onSelected)
    }
}

private class PremiseViewHolder(private val binding: ItemPremisePlacardBinding) : PlacardViewHolder(binding.root) {
    @SuppressLint("SetTextI18n")
    fun bind(premise: Premise, isSelected: Boolean, isCurrent: Boolean, onSelected: (Premise) -> Unit) {
        binding.root.background = if (isSelected) backgroundDrawableActive
            else backgroundDrawableInactive
        binding.imageSelected.setImageDrawable(
            when {
                isCurrent -> checkBoxDrawableChecked
                isSelected -> radioDrawableChecked
                else -> radioBoxDrawableUnchecked
            }
        )
        binding.textPremiseNickname.text = premise.nickname
        binding.textPremiseNickname.isVisible = !premise.nickname.isNullOrBlank()
        val premiseAddress = premise.address
        val premiseGeoLocation = premise.geoLocation
        if (premiseAddress != null) {
            binding.textPremiseAddress.text = buildString {
                appendLine(premiseAddress.address1)
                premiseAddress.address2?.let { appendLine(it) }
                appendLine("${premiseAddress.city}, ${premiseAddress.state} ${premiseAddress.postCode}")
                append(premiseAddress.country)
            }
            binding.textPremiseAddress.isVisible = true
        } else if (premiseGeoLocation != null) {
            binding.textPremiseAddress.text = "(${premiseGeoLocation.latitude}, ${premiseGeoLocation.longitude})"
            binding.textPremiseAddress.isVisible = true
        } else {
            binding.textPremiseAddress.text = ""
            binding.textPremiseAddress.isVisible = false
        }
        binding.textPremiseNumber.text = premise.number
        binding.textPremiseNumber.isVisible = !premise.number.isNullOrBlank()
        binding.textPremiseJurisdiction.text = premise.jurisdiction?.name
        binding.textPremiseJurisdiction.isVisible = !premise.jurisdiction?.name.isNullOrBlank()
        binding.root.setOnClickListener { onSelected(premise) }
    }
}
