package com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.drug

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.DrugTypePresentation
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.drug.DrugActionConfigurationViewModel.DrugActionConfigured
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.collectLatestOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.observeOneTimeEventsOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.getEntityIdSet
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.takeAs
import com.weyr_associates.animaltrakkerfarmmobile.app.core.putExtra
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.ItemSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.select.drugForApplicationSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.select.drugLocationSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.select.optionalOffLabelDrugDoseSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ActivityDrugActionConfigurationBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.DrugApplicationInfo
import com.weyr_associates.animaltrakkerfarmmobile.model.DrugLocation
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.OffLabelDrugDose
import com.weyr_associates.animaltrakkerfarmmobile.model.OffLabelDrugSpec
import kotlinx.coroutines.Job
import java.util.UUID

class DrugActionConfigurationActivity : AppCompatActivity() {

    companion object {
        fun newIntentToConfigure(context: Context, drugTypeId: EntityId, excludedDrugIds: Set<EntityId>) =
            Intent(context, DrugActionConfigurationActivity::class.java).apply {
                action = ConfigureDrugAction.ACTION_CONFIGURE
                putExtra(ConfigureDrugAction.EXTRA_DRUG_TYPE_ID, drugTypeId)
                putExtra(ConfigureDrugAction.EXTRA_EXCLUDED_DRUG_IDS, excludedDrugIds)
            }

        fun newIntentToEdit(
            context: Context,
            actionId: UUID,
            configuration: DrugAction.Configuration,
            excludedDrugIds: Set<EntityId>
        ) = Intent(context, DrugActionConfigurationActivity::class.java).apply {
            action = ConfigureDrugAction.ACTION_EDIT
            putExtra(ConfigureDrugAction.EXTRA_DRUG_TYPE_ID, configuration.drugApplicationInfo.drugTypeId)
            putExtra(ConfigureDrugAction.EXTRA_EDIT_ACTION_ID, actionId)
            putExtra(ConfigureDrugAction.EXTRA_DRUG_ACTION_CONFIGURATION, configuration)
            putExtra(ConfigureDrugAction.EXTRA_EXCLUDED_DRUG_IDS, excludedDrugIds)
        }
    }

    private val binding by lazy {
        ActivityDrugActionConfigurationBinding.inflate(layoutInflater)
    }

    private val viewModel by viewModels<DrugActionConfigurationViewModel> {
        ViewModelFactory(drugTypeId)
    }

    private val actionId: UUID? by lazy {
        intent?.getSerializableExtra(ConfigureDrugAction.EXTRA_EDIT_ACTION_ID)?.takeAs<UUID>()
    }

    private val drugTypeId: EntityId by lazy {
        requireNotNull(intent?.getParcelableExtra(ConfigureDrugAction.EXTRA_DRUG_TYPE_ID)) {
            "${ConfigureDrugAction.EXTRA_DRUG_TYPE_ID} is a required intent extra."
        }
    }

    private val excludedDrugIds: Set<EntityId> by lazy {
        intent?.getEntityIdSet(ConfigureDrugAction.EXTRA_EXCLUDED_DRUG_IDS) ?: emptySet()
    }

    private lateinit var drugSelectionPresenter: ItemSelectionPresenter<DrugApplicationInfo>
    private lateinit var drugLocationSelectionPresenter: ItemSelectionPresenter<DrugLocation>

    private var offLabelDoseSelectionPresenter: ItemSelectionPresenter<OffLabelDrugDose>? = null
    private var offLabelPresenterJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val drugTypeName = DrugTypePresentation.nameForType(this, drugTypeId)
        title = resolveTitleFromAction(drugTypeName)
        binding.textDrugLabel.text = drugTypeName
        binding.buttonConfigureDrug.text = resolveConfigureButtonText()
        drugSelectionPresenter = drugForApplicationSelectionPresenter(
            drugTypeId = drugTypeId,
            excludedDrugIds = excludedDrugIds,
            button = binding.spinnerButtonSelectDrug,
            itemDisplayTextProvider = { it.name }
        ) { drug -> viewModel.updateDrugSelection(drug) }
            .also { it.bindToFlow(this, lifecycleScope, viewModel.drugSelection) }
        drugLocationSelectionPresenter = drugLocationSelectionPresenter(
            button = binding.spinnerButtonSelectDrugLocation
        ) { drugLocation -> viewModel.updateDrugLocationSelection(drugLocation) }
            .also { it.bindToFlow(this, lifecycleScope, viewModel.drugLocationSelection) }
        binding.buttonConfigureDrug.setOnClickListener {
            viewModel.configure()
        }
        collectLatestOnStart(viewModel.drugSelection) { selectedDrug ->
            binding.spinnerButtonSelectOffLabelDose.isEnabled = selectedDrug != null
            offLabelDoseSelectionPresenter = optionalOffLabelDrugDoseSelectionPresenter(
                drugId = selectedDrug?.drugId ?: EntityId.UNKNOWN,
                button = binding.spinnerButtonSelectOffLabelDose
            ) { offLabelDrugDose ->
                viewModel.updateOffLabelDrugDoseSelection(offLabelDrugDose)
            }.also {
                offLabelPresenterJob?.cancel()
                offLabelPresenterJob = it.bindToFlow(
                    this,
                    lifecycleScope,
                    viewModel.offLabelDrugDoseSelection
                )
            }
        }
        collectLatestOnStart(viewModel.canConfigure) { canConfigure ->
            binding.buttonConfigureDrug.isEnabled = canConfigure
        }
        observeOneTimeEventsOnStart(viewModel.events, ::handleEvent)
    }

    private fun resolveTitleFromAction(drugTypeName: String): String {
        return when (intent?.action) {
            ConfigureDrugAction.ACTION_EDIT -> {
                getString(
                    R.string.title_activity_drug_action_edit_configuration_format,
                    drugTypeName
                )
            }
            else -> {
                getString(
                    R.string.title_activity_drug_action_configuration_format,
                    drugTypeName
                )
            }
        }
    }

    private fun resolveConfigureButtonText(): String {
        return getString(when (intent?.action) {
            ConfigureDrugAction.ACTION_EDIT -> R.string.button_save
            else -> R.string.button_configure
        })
    }

    private fun handleEvent(event: DrugActionConfigurationViewModel.Event) {
        when (event) {
            is DrugActionConfigured -> {
                setResult(RESULT_OK, Intent().apply {
                    actionId?.let {
                        putExtra(ConfigureDrugAction.EXTRA_EDIT_ACTION_ID, it)
                    }
                    putExtra(
                        ConfigureDrugAction.EXTRA_DRUG_ACTION_CONFIGURATION,
                        event.drugActionConfiguration
                    )
                })
                finish()
            }
        }
    }

    private class ViewModelFactory(private val drugTypeId: EntityId) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return when (modelClass) {
                DrugActionConfigurationViewModel::class.java -> {
                    @Suppress("UNCHECKED_CAST")
                    DrugActionConfigurationViewModel(
                        drugTypeId = drugTypeId,
                        extras.createSavedStateHandle()
                    ) as T
                }
                else -> throw IllegalStateException("${modelClass.simpleName} is not supported.")
            }
        }
    }
}
