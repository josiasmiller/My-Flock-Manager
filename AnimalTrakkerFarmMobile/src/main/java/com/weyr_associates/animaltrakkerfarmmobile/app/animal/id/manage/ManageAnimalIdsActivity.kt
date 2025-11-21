package com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.manage

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.AnimalDialogs
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.add.AddAnimal
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.alert.AddAnimalAlert
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.alert.ShowAlertButtonPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.edit.EditAnimalId
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.input.IdInputSettings
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.trich.AutoIncrementNextTrichIdFeature
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.validation.IdValidationErrorDialog
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.validation.IdValidations
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.lookup.LookupAnimalInfo
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.lookup.LookupAnimalInfoPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.collectLatestOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.observeOneTimeEventsOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.hideKeyboard
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.takeAs
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.ItemSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.SelectItem
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.selectedItem
import com.weyr_associates.animaltrakkerfarmmobile.app.core.widget.TopButtonBar
import com.weyr_associates.animaltrakkerfarmmobile.app.core.widget.recyclerview.OutlineDividerDecoration
import com.weyr_associates.animaltrakkerfarmmobile.app.device.eid.EIDReaderConnection
import com.weyr_associates.animaltrakkerfarmmobile.app.permissions.RequiredPermissionsWatcher
import com.weyr_associates.animaltrakkerfarmmobile.app.reporting.errors.observeErrorReports
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.AnimalRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.DefaultSettingsRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.IdTypeRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.select.SelectAnimal
import com.weyr_associates.animaltrakkerfarmmobile.app.select.SelectIdRemoveReasonDialogFragment
import com.weyr_associates.animaltrakkerfarmmobile.app.select.idColorSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.select.idLocationSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.select.idTypeSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.ActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseManager
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ActivityManageAnimalIdsBinding
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ItemAnimalIdEditableBinding
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ViewNoAnimalIdsFoundBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.IdInfo
import com.weyr_associates.animaltrakkerfarmmobile.model.IdColor
import com.weyr_associates.animaltrakkerfarmmobile.model.IdLocation
import com.weyr_associates.animaltrakkerfarmmobile.model.IdRemoveReason
import com.weyr_associates.animaltrakkerfarmmobile.model.IdType

class ManageAnimalIdsActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_SELECTED_ID = "EXTRA_SELECTED_ANIMAL_ID"
        private const val TAG_FRAGMENT_SELECT_ID_REMOVE_REASON = "TAG_FRAGMENT_SELECT_ID_REMOVE_REASON"
    }

    private val viewModel: ManageAnimalIdsViewModel
        by viewModels { ManageAnimalIdsViewModelFactory(this) }

    private val binding: ActivityManageAnimalIdsBinding by lazy {
        ActivityManageAnimalIdsBinding.inflate(layoutInflater)
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

    private val editAnimalIdLauncher = registerForActivityResult(EditAnimalId.Contract()) { success ->
        if (success) { viewModel.onIdEdited() }
    }

    private val lookupAnimalInfoPresenter by lazy {
        LookupAnimalInfoPresenter(binding.lookupAnimalInfo).apply {
            displayAnimalIdInfo = false
            addAnimalAlertLauncher = this@ManageAnimalIdsActivity.addAnimalAlertLauncher
            onAddAnimalWithEIDClicked = { eidNumber ->
                AnimalDialogs.promptToAddAnimalWithEID(
                    this@ManageAnimalIdsActivity,
                    eidNumber,
                    addAndSelectAnimalLauncher
                )
            }
        }
    }

    private lateinit var idTypePresenter: ItemSelectionPresenter<IdType>
    private lateinit var idColorPresenter: ItemSelectionPresenter<IdColor>
    private lateinit var idLocationPresenter: ItemSelectionPresenter<IdLocation>

    private val animalIdsAdapter = EditAnimalIdsAdapter(::onEditId, ::onRemoveId)
    private val requiredPermissionsWatcher = RequiredPermissionsWatcher(this)

    private lateinit var eidReaderConnection: EIDReaderConnection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        with(binding.buttonPanelTop) {
            show(
                TopButtonBar.UI_SCANNER_STATUS or
                        TopButtonBar.UI_SCAN_EID or
                        TopButtonBar.UI_LOOKUP_ANIMAL or
                        TopButtonBar.UI_SHOW_ALERT or
                        TopButtonBar.UI_CLEAR_DATA
            )

            scanEIDButton.setOnClickListener { onScanEID() }
            lookupAnimalButton.setOnClickListener { onLookupAnimal() }
            clearDataButton.setOnClickListener { viewModel.clearData() }
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
        idLocationPresenter = idLocationSelectionPresenter(
            button = binding.inputAnimalId.spinnerIdLocation
        ) { idLocation ->
            viewModel.selectIdLocation(idLocation)
        }.also {
            it.bindToFlow(this, lifecycleScope, viewModel.selectedIdLocation)
        }

        idColorPresenter = idColorSelectionPresenter(
            button = binding.inputAnimalId.spinnerIdColor
        ) { idColor ->
            viewModel.selectIdColor(idColor)
        }.also {
            it.bindToFlow(this, lifecycleScope, viewModel.selectedIdColor)
        }

        with(binding.inputAnimalId) {
            inputIdNumber.setText(viewModel.idNumber)
            inputIdNumber.addTextChangedListener {
                viewModel.idNumber = it.toString()
            }
            buttonAddId.setOnClickListener {
                hideKeyboard()
                viewModel.addId()
            }
        }

        with(binding.recyclerAnimalIds) {
            adapter = animalIdsAdapter
            layoutManager = LinearLayoutManager(
                this@ManageAnimalIdsActivity,
                RecyclerView.VERTICAL,
                false
            )
            itemAnimator = null
            addItemDecoration(OutlineDividerDecoration(this@ManageAnimalIdsActivity))
        }

        collectLatestOnStart(viewModel.canClearData) { canClear ->
            binding.buttonPanelTop.clearDataButton.isEnabled = canClear
        }

        collectLatestOnStart(viewModel.canAddId) { canAdd ->
            binding.inputAnimalId.buttonAddId.isEnabled = canAdd
        }

        collectLatestOnStart(viewModel.animalInfoState) {
            updateTopBarAnimalActions(it)
            updateAnimalInfoDisplay(it)
        }

        observeOneTimeEventsOnStart(viewModel.events, ::handleEvent)
        observeErrorReports(viewModel.errorReportFlow)
        supportFragmentManager.setFragmentResultListener(
            SelectIdRemoveReasonDialogFragment.REQUEST_KEY_SELECT_ID_REMOVE_REASON,
            this
        ) { _, result ->
            val removeReason: IdRemoveReason = result.selectedItem()
            val idInfo: IdInfo = requireNotNull(
                result.getBundle(SelectItem.EXTRA_ASSOCIATED_DATA)
                    ?.getParcelable(EXTRA_SELECTED_ID)
            )
            onRemoveReasonSelected(idInfo, removeReason)
        }

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

    private fun onLookupAnimal() {
        selectAnimalLauncher.launch(null)
    }

    private fun onScanEID() {
        eidReaderConnection.toggleScanningEID()
    }

    private fun onEIDScanned(eidNumber: String) {
        viewModel.onEIDScanned(eidNumber)
    }

    private fun onEditId(idInfo: IdInfo) {
        val animalInfo = viewModel.animalInfoState.value.takeAs<LookupAnimalInfo.AnimalInfoState.Loaded>()
            ?.animalBasicInfo
            ?: return
        editAnimalIdLauncher.launch(
            EditAnimalId.Request(
                animalIdToEdit = idInfo,
                animalBasicInfo = animalInfo
            )
        )
    }

    private fun onRemoveId(idInfo: IdInfo) {
        promptForRemoveReason(idInfo)
    }

    private fun onRemoveReasonSelected(idInfo: IdInfo, removeReason: IdRemoveReason) {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_title_confirm_remove_animal_id)
            .setMessage(
                getString(
                    R.string.dialog_message_confirm_remove_animal_id,
                    idInfo.type.name,
                    idInfo.number,
                    removeReason.text
                )
            )
            .setPositiveButton(R.string.yes_label) { _, _ ->
                viewModel.removeId(idInfo.id, removeReason.id)
            }
            .setNegativeButton(R.string.no_label) { _, _ -> /*NO-OP*/ }
            .create()
            .show()
    }

    private fun updateTopBarAnimalActions(animalInfoState: LookupAnimalInfo.AnimalInfoState) {
        showAlertButtonPresenter.animalInfoState = animalInfoState
    }

    private fun updateAnimalInfoDisplay(animalInfoState: LookupAnimalInfo.AnimalInfoState) {
        lookupAnimalInfoPresenter.animalInfoState = animalInfoState
        when (animalInfoState) {
            LookupAnimalInfo.AnimalInfoState.Initial -> {
                binding.inputAnimalId.root.isGone = true
                binding.recyclerAnimalIds.isGone = true
            }
            is LookupAnimalInfo.AnimalInfoState.Loaded -> {
                binding.inputAnimalId.root.isVisible = true
                binding.recyclerAnimalIds.isVisible = true
                animalIdsAdapter.submitList(animalInfoState.animalBasicInfo.ids)
            }
            is LookupAnimalInfo.AnimalInfoState.NotFound -> {
                binding.inputAnimalId.root.isGone = true
                binding.recyclerAnimalIds.isGone = true
            }
        }
    }

    private fun promptForRemoveReason(idInfo: IdInfo) {
        SelectIdRemoveReasonDialogFragment.newInstance(
            associatedData = Bundle().apply {
                putParcelable(EXTRA_SELECTED_ID, idInfo)
            }
        ).show(supportFragmentManager, TAG_FRAGMENT_SELECT_ID_REMOVE_REASON)
    }

    private fun handleEvent(event: ManageAnimalIdsViewModel.Event) {
        when (event) {
            ManageAnimalIdsViewModel.IdNumberChanged -> {
                binding.inputAnimalId.inputIdNumber.setText(viewModel.idNumber)
            }
            is ManageAnimalIdsViewModel.PromptForEIDUsage -> {
                promptForEIDUsage(event.eidNumber)
            }
            is ManageAnimalIdsViewModel.ValidationError.ScannedEIDAlreadyUsed -> {
                IdValidationErrorDialog.showEIDAlreadyInUseError(this, event.error)
            }
            ManageAnimalIdsViewModel.IdAdditionFailed -> {
                showIdAdditionFailed()
            }
            ManageAnimalIdsViewModel.IdRemovalFailed -> {
                showIdRemovalFailed()
            }
            ManageAnimalIdsViewModel.ValidationError.IdEntryRequired -> {
                IdValidationErrorDialog.showIdEntryIsRequiredError(this)
            }
            ManageAnimalIdsViewModel.ValidationError.PartialIdEntry -> {
                IdValidationErrorDialog.showPartialIdEntryError(this)
            }
            is ManageAnimalIdsViewModel.ValidationError.InvalidIdNumberFormat -> {
                IdValidationErrorDialog.showIdNumberFormatError(this, event.idEntry)
            }
            is ManageAnimalIdsViewModel.ValidationError.InvalidIdCombination -> {
                IdValidationErrorDialog.showIdCombinationError(this, event.error)
            }
            is ManageAnimalIdsViewModel.AnimalAlertEvent -> {
                AnimalDialogs.showAnimalAlert(this, event.alerts)
            }
        }
    }

    private fun promptForEIDUsage(eidNumber: String) {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_title_prompt_scanned_eid_usage)
            .setMessage(getString(R.string.dialog_message_prompt_scanned_eid_usage, eidNumber))
            .setPositiveButton(R.string.text_lookup_animal) { _, _ ->
                viewModel.lookupAnimalInfoByEIDNumber(eidNumber)
            }
            .setNegativeButton(R.string.text_add_eid) { _, _ ->
                viewModel.setIdNumberFromEIDScan(eidNumber)
            }
            .create()
            .show()
    }

    private fun showIdAdditionFailed() {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_title_id_addition_failed)
            .setMessage(R.string.dialog_message_id_addition_failed)
    }

    private fun showIdRemovalFailed() {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_title_id_removal_failed)
            .setMessage(R.string.dialog_message_id_removal_failed)
            .setPositiveButton(R.string.ok) { _, _ -> /*NO-OP*/ }
            .create()
            .show()
    }
}

private class ManageAnimalIdsViewModelFactory(context: Context) : ViewModelProvider.Factory {

    private val appContext = context.applicationContext

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return when (modelClass) {
            ManageAnimalIdsViewModel::class.java -> {
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
                val animalRepo = AnimalRepositoryImpl(databaseHandler, defSettingsRepo)
                val idTypeRepo = IdTypeRepositoryImpl(databaseHandler)
                @Suppress("UNCHECKED_CAST")
                ManageAnimalIdsViewModel(
                    animalRepo = animalRepo,
                    idTypeRepo = idTypeRepo,
                    idValidations = IdValidations(animalRepo),
                    autoUpdateTrichId = AutoIncrementNextTrichIdFeature(
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

private class EditAnimalIdsAdapter(
    private val onEditId: (IdInfo) -> Unit,
    private val onRemoveId: (IdInfo) -> Unit
) : ListAdapter<IdInfo, EditAnimalIdViewHolder>(
    IdInfo.Differ
) {

    companion object {
        private const val VIEW_TYPE_NO_IDS = 0
        private const val VIEW_TYPE_EDIT_ID = 1
    }

    override fun getItemCount(): Int {
        return currentList.size.takeIf { 0 < it } ?: 1
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            currentList.isEmpty() -> VIEW_TYPE_NO_IDS
            else -> VIEW_TYPE_EDIT_ID
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditAnimalIdViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_NO_IDS -> NoAnimalIdsViewHolder(
                ViewNoAnimalIdsFoundBinding.inflate(layoutInflater, parent, false)
            )
            else -> AnimalIdViewHolder(
                ItemAnimalIdEditableBinding.inflate(layoutInflater, parent, false),
                onEditId,
                onRemoveId
            )
        }
    }

    override fun onBindViewHolder(holder: EditAnimalIdViewHolder, position: Int) {
        if (currentList.isNotEmpty()) {
            holder.takeAs<AnimalIdViewHolder>()?.bind(currentList[position])
        }
    }
}

private abstract class EditAnimalIdViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

private class NoAnimalIdsViewHolder(binding: ViewNoAnimalIdsFoundBinding) : EditAnimalIdViewHolder(binding.root)

private class AnimalIdViewHolder(
    private val binding: ItemAnimalIdEditableBinding,
    private val onEditId: (IdInfo) -> Unit,
    private val onRemoveId: (IdInfo) -> Unit
) : EditAnimalIdViewHolder(binding.root) {
    fun bind(item: IdInfo) {
        binding.textIdTypeName.text = item.type.name
        binding.textIdNumber.text = item.number
        binding.textIdColorName.text = item.color.name
        binding.textIdLocationName.text = item.location.name
        with(binding.editControls) {
            buttonEditId.setOnClickListener { onEditId(item) }
            buttonRemoveId.setOnClickListener { onRemoveId(item) }
        }
    }
}
