package com.weyr_associates.animaltrakkerfarmmobile.app.animal.evaluation.configuration

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.collectLatestOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.observeOneTimeEventsOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.ItemSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.core.widget.TopButtonBar
import com.weyr_associates.animaltrakkerfarmmobile.app.reporting.errors.observeErrorReports
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.DefaultSettingsRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.EvaluationRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.select.optionalTraitSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.select.optionalUnitsSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.ActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadDefaultUserInfo
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseManager
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ActivityConfigureEvaluationBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.Trait
import com.weyr_associates.animaltrakkerfarmmobile.model.UnitOfMeasure
import kotlinx.coroutines.flow.map

class ConfigureEvaluationActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_KEY_SELECT_TRAIT_01 = "REQUEST_KEY_SELECT_TRAIT_01"
        private const val REQUEST_KEY_SELECT_TRAIT_02 = "REQUEST_KEY_SELECT_TRAIT_02"
        private const val REQUEST_KEY_SELECT_TRAIT_03 = "REQUEST_KEY_SELECT_TRAIT_03"
        private const val REQUEST_KEY_SELECT_TRAIT_04 = "REQUEST_KEY_SELECT_TRAIT_04"
        private const val REQUEST_KEY_SELECT_TRAIT_05 = "REQUEST_KEY_SELECT_TRAIT_05"
        private const val REQUEST_KEY_SELECT_TRAIT_06 = "REQUEST_KEY_SELECT_TRAIT_06"
        private const val REQUEST_KEY_SELECT_TRAIT_07 = "REQUEST_KEY_SELECT_TRAIT_07"
        private const val REQUEST_KEY_SELECT_TRAIT_08 = "REQUEST_KEY_SELECT_TRAIT_08"
        private const val REQUEST_KEY_SELECT_TRAIT_09 = "REQUEST_KEY_SELECT_TRAIT_09"
        private const val REQUEST_KEY_SELECT_TRAIT_10 = "REQUEST_KEY_SELECT_TRAIT_10"
        private const val REQUEST_KEY_SELECT_TRAIT_11 = "REQUEST_KEY_SELECT_TRAIT_11"
        private const val REQUEST_KEY_SELECT_TRAIT_12 = "REQUEST_KEY_SELECT_TRAIT_12"
        private const val REQUEST_KEY_SELECT_TRAIT_13 = "REQUEST_KEY_SELECT_TRAIT_13"
        private const val REQUEST_KEY_SELECT_TRAIT_14 = "REQUEST_KEY_SELECT_TRAIT_14"
        private const val REQUEST_KEY_SELECT_TRAIT_15 = "REQUEST_KEY_SELECT_TRAIT_15"
        private const val REQUEST_KEY_SELECT_TRAIT_16 = "REQUEST_KEY_SELECT_TRAIT_16"
        private const val REQUEST_KEY_SELECT_TRAIT_17 = "REQUEST_KEY_SELECT_TRAIT_17"
        private const val REQUEST_KEY_SELECT_TRAIT_18 = "REQUEST_KEY_SELECT_TRAIT_18"
        private const val REQUEST_KEY_SELECT_TRAIT_19 = "REQUEST_KEY_SELECT_TRAIT_19"
        private const val REQUEST_KEY_SELECT_TRAIT_20 = "REQUEST_KEY_SELECT_TRAIT_20"

        private const val REQUEST_KEY_SELECT_UNITS_11 = "REQUEST_KEY_SELECT_UNITS_11"
        private const val REQUEST_KEY_SELECT_UNITS_12 = "REQUEST_KEY_SELECT_UNITS_12"
        private const val REQUEST_KEY_SELECT_UNITS_13 = "REQUEST_KEY_SELECT_UNITS_13"
        private const val REQUEST_KEY_SELECT_UNITS_14 = "REQUEST_KEY_SELECT_UNITS_14"
        private const val REQUEST_KEY_SELECT_UNITS_15 = "REQUEST_KEY_SELECT_UNITS_15"
    }

    private val binding by lazy {
        ActivityConfigureEvaluationBinding.inflate(layoutInflater)
    }

    private val viewModel by viewModels<ConfigureEvaluationViewModel> {
        ViewModelFactory(this)
    }

    private lateinit var trait01Presenter: ItemSelectionPresenter<Trait>
    private lateinit var trait02Presenter: ItemSelectionPresenter<Trait>
    private lateinit var trait03Presenter: ItemSelectionPresenter<Trait>
    private lateinit var trait04Presenter: ItemSelectionPresenter<Trait>
    private lateinit var trait05Presenter: ItemSelectionPresenter<Trait>
    private lateinit var trait06Presenter: ItemSelectionPresenter<Trait>
    private lateinit var trait07Presenter: ItemSelectionPresenter<Trait>
    private lateinit var trait08Presenter: ItemSelectionPresenter<Trait>
    private lateinit var trait09Presenter: ItemSelectionPresenter<Trait>
    private lateinit var trait10Presenter: ItemSelectionPresenter<Trait>
    private lateinit var trait11Presenter: ItemSelectionPresenter<Trait>
    private lateinit var trait12Presenter: ItemSelectionPresenter<Trait>
    private lateinit var trait13Presenter: ItemSelectionPresenter<Trait>
    private lateinit var trait14Presenter: ItemSelectionPresenter<Trait>
    private lateinit var trait15Presenter: ItemSelectionPresenter<Trait>
    private lateinit var trait16Presenter: ItemSelectionPresenter<Trait>
    private lateinit var trait17Presenter: ItemSelectionPresenter<Trait>
    private lateinit var trait18Presenter: ItemSelectionPresenter<Trait>
    private lateinit var trait19Presenter: ItemSelectionPresenter<Trait>
    private lateinit var trait20Presenter: ItemSelectionPresenter<Trait>

    private lateinit var units11Presenter: ItemSelectionPresenter<UnitOfMeasure>
    private lateinit var units12Presenter: ItemSelectionPresenter<UnitOfMeasure>
    private lateinit var units13Presenter: ItemSelectionPresenter<UnitOfMeasure>
    private lateinit var units14Presenter: ItemSelectionPresenter<UnitOfMeasure>
    private lateinit var units15Presenter: ItemSelectionPresenter<UnitOfMeasure>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        with(binding) {
            with(buttonPanelTop) {
                show(TopButtonBar.UI_CLEAR_DATA or TopButtonBar.UI_ACTION_UPDATE_DATABASE)
                clearDataButton.setOnClickListener {
                    viewModel.clearData()
                }
                collectLatestOnStart(viewModel.canClearData) { canClear ->
                    clearDataButton.isEnabled = canClear
                }
                mainActionButton.setOnClickListener {
                    viewModel.saveToDatabase()
                }
                collectLatestOnStart(viewModel.canSaveToDatabase) { canSave ->
                    mainActionButton.isEnabled = canSave
                }
            }
            with(inputEvaluationName) {
                addTextChangedListener { viewModel.name = it.toString() }
            }
            with(toggleSummarizeInAlert) {
                setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateSummarizeInAlert(isChecked)
                }
                collectLatestOnStart(viewModel.summarizeInAlert) { shouldSummarize ->
                    isChecked = shouldSummarize
                }
            }
            with(evalTraitConfiguration01) {
                textTraitLabel.text = getString(
                    R.string.text_scored_trait_xx, "01"
                )
                trait01Presenter = optionalTraitSelectionPresenter(
                    traitTypeId = Trait.TYPE_ID_BASIC,
                    button = spinnerTraitSelection,
                    requestKey = REQUEST_KEY_SELECT_TRAIT_01
                ) { trait -> viewModel.updateTrait01(trait) }
                    .also { presenter ->
                        presenter.bindToFlow(
                            this@ConfigureEvaluationActivity,
                            lifecycleScope,
                            viewModel.trait01.map { it.trait }
                        )
                    }
                toggleTraitOptional.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateTrait01Optional(isChecked)
                }
                toggleTraitDeferred.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateTrait01Deferred(isChecked)
                }
                collectLatestOnStart(viewModel.trait01) { configuration ->
                    toggleTraitOptional.isChecked = configuration.isOptional
                    toggleTraitDeferred.isChecked = configuration.isDeferred
                }
            }
            with(evalTraitConfiguration02) {
                textTraitLabel.text = getString(
                    R.string.text_scored_trait_xx, "02"
                )
                trait02Presenter = optionalTraitSelectionPresenter(
                    traitTypeId = Trait.TYPE_ID_BASIC,
                    button = spinnerTraitSelection,
                    requestKey = REQUEST_KEY_SELECT_TRAIT_02
                ) { trait -> viewModel.updateTrait02(trait) }
                    .also { presenter ->
                        presenter.bindToFlow(
                            this@ConfigureEvaluationActivity,
                            lifecycleScope,
                            viewModel.trait02.map { it.trait }
                        )
                    }
                toggleTraitOptional.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateTrait02Optional(isChecked)
                }
                toggleTraitDeferred.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateTrait02Deferred(isChecked)
                }
                collectLatestOnStart(viewModel.trait02) { configuration ->
                    toggleTraitOptional.isChecked = configuration.isOptional
                    toggleTraitDeferred.isChecked = configuration.isDeferred
                }
            }
            with(evalTraitConfiguration03) {
                textTraitLabel.text = getString(
                    R.string.text_scored_trait_xx, "03"
                )
                trait03Presenter = optionalTraitSelectionPresenter(
                    traitTypeId = Trait.TYPE_ID_BASIC,
                    button = spinnerTraitSelection,
                    requestKey = REQUEST_KEY_SELECT_TRAIT_03
                ) { trait -> viewModel.updateTrait03(trait) }
                    .also { presenter ->
                        presenter.bindToFlow(
                            this@ConfigureEvaluationActivity,
                            lifecycleScope,
                            viewModel.trait03.map { it.trait }
                        )
                    }
                toggleTraitOptional.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateTrait03Optional(isChecked)
                }
                toggleTraitDeferred.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateTrait03Deferred(isChecked)
                }
                collectLatestOnStart(viewModel.trait03) { configuration ->
                    toggleTraitOptional.isChecked = configuration.isOptional
                    toggleTraitDeferred.isChecked = configuration.isDeferred
                }
            }
            with(evalTraitConfiguration04) {
                textTraitLabel.text = getString(
                    R.string.text_scored_trait_xx, "04"
                )
                trait04Presenter = optionalTraitSelectionPresenter(
                    traitTypeId = Trait.TYPE_ID_BASIC,
                    button = spinnerTraitSelection,
                    requestKey = REQUEST_KEY_SELECT_TRAIT_04
                ) { trait -> viewModel.updateTrait04(trait) }
                    .also { presenter ->
                        presenter.bindToFlow(
                            this@ConfigureEvaluationActivity,
                            lifecycleScope,
                            viewModel.trait04.map { it.trait }
                        )
                    }
                toggleTraitOptional.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateTrait04Optional(isChecked)
                }
                toggleTraitDeferred.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateTrait04Deferred(isChecked)
                }
                collectLatestOnStart(viewModel.trait04) { configuration ->
                    toggleTraitOptional.isChecked = configuration.isOptional
                    toggleTraitDeferred.isChecked = configuration.isDeferred
                }
            }
            with(evalTraitConfiguration05) {
                textTraitLabel.text = getString(
                    R.string.text_scored_trait_xx, "05"
                )
                trait05Presenter = optionalTraitSelectionPresenter(
                    traitTypeId = Trait.TYPE_ID_BASIC,
                    button = spinnerTraitSelection,
                    requestKey = REQUEST_KEY_SELECT_TRAIT_05
                ) { trait -> viewModel.updateTrait05(trait) }
                    .also { presenter ->
                        presenter.bindToFlow(
                            this@ConfigureEvaluationActivity,
                            lifecycleScope,
                            viewModel.trait05.map { it.trait }
                        )
                    }
                toggleTraitOptional.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateTrait05Optional(isChecked)
                }
                toggleTraitDeferred.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateTrait05Deferred(isChecked)
                }
                collectLatestOnStart(viewModel.trait05) { configuration ->
                    toggleTraitOptional.isChecked = configuration.isOptional
                    toggleTraitDeferred.isChecked = configuration.isDeferred
                }
            }
            with(evalTraitConfiguration06) {
                textTraitLabel.text = getString(
                    R.string.text_scored_trait_xx, "06"
                )
                trait06Presenter = optionalTraitSelectionPresenter(
                    traitTypeId = Trait.TYPE_ID_BASIC,
                    button = spinnerTraitSelection,
                    requestKey = REQUEST_KEY_SELECT_TRAIT_06
                ) { trait -> viewModel.updateTrait06(trait) }
                    .also { presenter ->
                        presenter.bindToFlow(
                            this@ConfigureEvaluationActivity,
                            lifecycleScope,
                            viewModel.trait06.map { it.trait }
                        )
                    }
                toggleTraitOptional.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateTrait06Optional(isChecked)
                }
                toggleTraitDeferred.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateTrait06Deferred(isChecked)
                }
                collectLatestOnStart(viewModel.trait06) { configuration ->
                    toggleTraitOptional.isChecked = configuration.isOptional
                    toggleTraitDeferred.isChecked = configuration.isDeferred
                }
            }
            with(evalTraitConfiguration07) {
                textTraitLabel.text = getString(
                    R.string.text_scored_trait_xx, "07"
                )
                trait07Presenter = optionalTraitSelectionPresenter(
                    traitTypeId = Trait.TYPE_ID_BASIC,
                    button = spinnerTraitSelection,
                    requestKey = REQUEST_KEY_SELECT_TRAIT_07
                ) { trait -> viewModel.updateTrait07(trait) }
                    .also { presenter ->
                        presenter.bindToFlow(
                            this@ConfigureEvaluationActivity,
                            lifecycleScope,
                            viewModel.trait07.map { it.trait }
                        )
                    }
                toggleTraitOptional.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateTrait07Optional(isChecked)
                }
                toggleTraitDeferred.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateTrait07Deferred(isChecked)
                }
                collectLatestOnStart(viewModel.trait07) { configuration ->
                    toggleTraitOptional.isChecked = configuration.isOptional
                    toggleTraitDeferred.isChecked = configuration.isDeferred
                }
            }
            with(evalTraitConfiguration08) {
                textTraitLabel.text = getString(
                    R.string.text_scored_trait_xx, "08"
                )
                trait08Presenter = optionalTraitSelectionPresenter(
                    traitTypeId = Trait.TYPE_ID_BASIC,
                    button = spinnerTraitSelection,
                    requestKey = REQUEST_KEY_SELECT_TRAIT_08
                ) { trait -> viewModel.updateTrait08(trait) }
                    .also { presenter ->
                        presenter.bindToFlow(
                            this@ConfigureEvaluationActivity,
                            lifecycleScope,
                            viewModel.trait08.map { it.trait }
                        )
                    }
                toggleTraitOptional.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateTrait08Optional(isChecked)
                }
                toggleTraitDeferred.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateTrait08Deferred(isChecked)
                }
                collectLatestOnStart(viewModel.trait08) { configuration ->
                    toggleTraitOptional.isChecked = configuration.isOptional
                    toggleTraitDeferred.isChecked = configuration.isDeferred
                }
            }
            with(evalTraitConfiguration09) {
                textTraitLabel.text = getString(
                    R.string.text_scored_trait_xx, "09"
                )
                trait09Presenter = optionalTraitSelectionPresenter(
                    traitTypeId = Trait.TYPE_ID_BASIC,
                    button = spinnerTraitSelection,
                    requestKey = REQUEST_KEY_SELECT_TRAIT_09
                ) { trait -> viewModel.updateTrait09(trait) }
                    .also { presenter ->
                        presenter.bindToFlow(
                            this@ConfigureEvaluationActivity,
                            lifecycleScope,
                            viewModel.trait09.map { it.trait }
                        )
                    }
                toggleTraitOptional.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateTrait09Optional(isChecked)
                }
                toggleTraitDeferred.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateTrait09Deferred(isChecked)
                }
                collectLatestOnStart(viewModel.trait09) { configuration ->
                    toggleTraitOptional.isChecked = configuration.isOptional
                    toggleTraitDeferred.isChecked = configuration.isDeferred
                }
            }
            with(evalTraitConfiguration10) {
                textTraitLabel.text = getString(
                    R.string.text_scored_trait_xx, "10"
                )
                trait10Presenter = optionalTraitSelectionPresenter(
                    traitTypeId = Trait.TYPE_ID_BASIC,
                    button = spinnerTraitSelection,
                    requestKey = REQUEST_KEY_SELECT_TRAIT_10
                ) { trait -> viewModel.updateTrait10(trait) }
                    .also { presenter ->
                        presenter.bindToFlow(
                            this@ConfigureEvaluationActivity,
                            lifecycleScope,
                            viewModel.trait10.map { it.trait }
                        )
                    }
                toggleTraitOptional.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateTrait10Optional(isChecked)
                }
                toggleTraitDeferred.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateTrait10Deferred(isChecked)
                }
                collectLatestOnStart(viewModel.trait10) { configuration ->
                    toggleTraitOptional.isChecked = configuration.isOptional
                    toggleTraitDeferred.isChecked = configuration.isDeferred
                }
            }
            with(evalTraitConfiguration11) {
                textTraitLabel.text = getString(
                    R.string.text_unit_trait_xx, "11"
                )
                trait11Presenter = optionalTraitSelectionPresenter(
                    traitTypeId = Trait.TYPE_ID_UNIT,
                    button = spinnerTraitSelection,
                    requestKey = REQUEST_KEY_SELECT_TRAIT_11
                ) { trait -> viewModel.updateTrait11(trait) }
                    .also { presenter ->
                        presenter.bindToFlow(
                            this@ConfigureEvaluationActivity,
                            lifecycleScope,
                            viewModel.trait11.map { it.trait }
                        )
                    }
                textUnitsLabel.isVisible = true
                spinnerUnitsSelection.isVisible = true
                units11Presenter = optionalUnitsSelectionPresenter(
                    button = spinnerUnitsSelection,
                    requestKey = REQUEST_KEY_SELECT_UNITS_11,
                    unitsTypeIdFrom = { viewModel.trait11.value.trait?.unitsTypeId ?: EntityId.UNKNOWN }
                ) { units -> viewModel.updateUnits11(units) }
                    .also { presenter ->
                        presenter.bindToFlow(
                            this@ConfigureEvaluationActivity,
                            lifecycleScope,
                            viewModel.trait11.map { it.units }
                        )
                    }
                toggleTraitOptional.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateTrait11Optional(isChecked)
                }
                toggleTraitDeferred.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateTrait11Deferred(isChecked)
                }
                collectLatestOnStart(viewModel.trait11) { configuration ->
                    spinnerUnitsSelection.isEnabled = configuration.trait != null
                    toggleTraitOptional.isChecked = configuration.isOptional
                    toggleTraitDeferred.isChecked = configuration.isDeferred
                }
            }
            with(evalTraitConfiguration12) {
                textTraitLabel.text = getString(
                    R.string.text_unit_trait_xx, "12"
                )
                trait12Presenter = optionalTraitSelectionPresenter(
                    traitTypeId = Trait.TYPE_ID_UNIT,
                    button = spinnerTraitSelection,
                    requestKey = REQUEST_KEY_SELECT_TRAIT_12
                ) { trait -> viewModel.updateTrait12(trait) }
                    .also { presenter ->
                        presenter.bindToFlow(
                            this@ConfigureEvaluationActivity,
                            lifecycleScope,
                            viewModel.trait12.map { it.trait }
                        )
                    }
                textUnitsLabel.isVisible = true
                spinnerUnitsSelection.isVisible = true
                units12Presenter = optionalUnitsSelectionPresenter(
                    button = spinnerUnitsSelection,
                    requestKey = REQUEST_KEY_SELECT_UNITS_12,
                    unitsTypeIdFrom = { viewModel.trait12.value.trait?.unitsTypeId ?: EntityId.UNKNOWN }
                ) { units -> viewModel.updateUnits12(units) }
                    .also { presenter ->
                        presenter.bindToFlow(
                            this@ConfigureEvaluationActivity,
                            lifecycleScope,
                            viewModel.trait12.map { it.units }
                        )
                    }
                toggleTraitOptional.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateTrait12Optional(isChecked)
                }
                toggleTraitDeferred.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateTrait12Deferred(isChecked)
                }
                collectLatestOnStart(viewModel.trait12) { configuration ->
                    spinnerUnitsSelection.isEnabled = configuration.trait != null
                    toggleTraitOptional.isChecked = configuration.isOptional
                    toggleTraitDeferred.isChecked = configuration.isDeferred
                }
            }
            with(evalTraitConfiguration13) {
                textTraitLabel.text = getString(
                    R.string.text_unit_trait_xx, "13"
                )
                trait13Presenter = optionalTraitSelectionPresenter(
                    traitTypeId = Trait.TYPE_ID_UNIT,
                    button = spinnerTraitSelection,
                    requestKey = REQUEST_KEY_SELECT_TRAIT_13
                ) { trait -> viewModel.updateTrait13(trait) }
                    .also { presenter ->
                        presenter.bindToFlow(
                            this@ConfigureEvaluationActivity,
                            lifecycleScope,
                            viewModel.trait13.map { it.trait }
                        )
                    }
                textUnitsLabel.isVisible = true
                spinnerUnitsSelection.isVisible = true
                units13Presenter = optionalUnitsSelectionPresenter(
                    button = spinnerUnitsSelection,
                    requestKey = REQUEST_KEY_SELECT_UNITS_13,
                    unitsTypeIdFrom = { viewModel.trait13.value.trait?.unitsTypeId ?: EntityId.UNKNOWN }
                ) { units -> viewModel.updateUnits13(units) }
                    .also { presenter ->
                        presenter.bindToFlow(
                            this@ConfigureEvaluationActivity,
                            lifecycleScope,
                            viewModel.trait13.map { it.units }
                        )
                    }
                toggleTraitOptional.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateTrait13Optional(isChecked)
                }
                toggleTraitDeferred.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateTrait13Deferred(isChecked)
                }
                collectLatestOnStart(viewModel.trait13) { configuration ->
                    spinnerUnitsSelection.isEnabled = configuration.trait != null
                    toggleTraitOptional.isChecked = configuration.isOptional
                    toggleTraitDeferred.isChecked = configuration.isDeferred
                }
            }
            with(evalTraitConfiguration14) {
                textTraitLabel.text = getString(
                    R.string.text_unit_trait_xx, "14"
                )
                trait14Presenter = optionalTraitSelectionPresenter(
                    traitTypeId = Trait.TYPE_ID_UNIT,
                    button = spinnerTraitSelection,
                    requestKey = REQUEST_KEY_SELECT_TRAIT_14
                ) { trait -> viewModel.updateTrait14(trait) }
                    .also { presenter ->
                        presenter.bindToFlow(
                            this@ConfigureEvaluationActivity,
                            lifecycleScope,
                            viewModel.trait14.map { it.trait }
                        )
                    }
                textUnitsLabel.isVisible = true
                spinnerUnitsSelection.isVisible = true
                units14Presenter = optionalUnitsSelectionPresenter(
                    button = spinnerUnitsSelection,
                    requestKey = REQUEST_KEY_SELECT_UNITS_14,
                    unitsTypeIdFrom = { viewModel.trait14.value.trait?.unitsTypeId ?: EntityId.UNKNOWN }
                ) { units -> viewModel.updateUnits14(units) }
                    .also { presenter ->
                        presenter.bindToFlow(
                            this@ConfigureEvaluationActivity,
                            lifecycleScope,
                            viewModel.trait14.map { it.units }
                        )
                    }
                toggleTraitOptional.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateTrait14Optional(isChecked)
                }
                toggleTraitDeferred.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateTrait14Deferred(isChecked)
                }
                collectLatestOnStart(viewModel.trait14) { configuration ->
                    spinnerUnitsSelection.isEnabled = configuration.trait != null
                    toggleTraitOptional.isChecked = configuration.isOptional
                    toggleTraitDeferred.isChecked = configuration.isDeferred
                }
            }
            with(evalTraitConfiguration15) {
                textTraitLabel.text = getString(
                    R.string.text_unit_trait_xx, "15"
                )
                trait15Presenter = optionalTraitSelectionPresenter(
                    traitTypeId = Trait.TYPE_ID_UNIT,
                    button = spinnerTraitSelection,
                    requestKey = REQUEST_KEY_SELECT_TRAIT_15
                ) { trait -> viewModel.updateTrait15(trait) }
                    .also { presenter ->
                        presenter.bindToFlow(
                            this@ConfigureEvaluationActivity,
                            lifecycleScope,
                            viewModel.trait15.map { it.trait }
                        )
                    }
                textUnitsLabel.isVisible = true
                spinnerUnitsSelection.isVisible = true
                units15Presenter = optionalUnitsSelectionPresenter(
                    button = spinnerUnitsSelection,
                    requestKey = REQUEST_KEY_SELECT_UNITS_15,
                    unitsTypeIdFrom = { viewModel.trait15.value.trait?.unitsTypeId ?: EntityId.UNKNOWN }
                ) { units -> viewModel.updateUnits15(units) }
                    .also { presenter ->
                        presenter.bindToFlow(
                            this@ConfigureEvaluationActivity,
                            lifecycleScope,
                            viewModel.trait15.map { it.units }
                        )
                    }
                toggleTraitOptional.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateTrait15Optional(isChecked)
                }
                toggleTraitDeferred.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateTrait15Deferred(isChecked)
                }
                collectLatestOnStart(viewModel.trait15) { configuration ->
                    spinnerUnitsSelection.isEnabled = configuration.trait != null
                    toggleTraitOptional.isChecked = configuration.isOptional
                    toggleTraitDeferred.isChecked = configuration.isDeferred
                }
            }
            with(evalTraitConfiguration16) {
                textTraitLabel.text = getString(
                    R.string.text_option_trait_xx, "16"
                )
                trait16Presenter = optionalTraitSelectionPresenter(
                    traitTypeId = Trait.TYPE_ID_OPTION,
                    button = spinnerTraitSelection,
                    requestKey = REQUEST_KEY_SELECT_TRAIT_16
                ) { trait -> viewModel.updateTrait16(trait) }
                    .also { presenter ->
                        presenter.bindToFlow(
                            this@ConfigureEvaluationActivity,
                            lifecycleScope,
                            viewModel.trait16.map { it.trait }
                        )
                    }
                toggleTraitOptional.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateTrait16Optional(isChecked)
                }
                toggleTraitDeferred.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateTrait16Deferred(isChecked)
                }
                collectLatestOnStart(viewModel.trait16) { configuration ->
                    toggleTraitOptional.isChecked = configuration.isOptional
                    toggleTraitDeferred.isChecked = configuration.isDeferred
                }
            }
            with(evalTraitConfiguration17) {
                textTraitLabel.text = getString(
                    R.string.text_option_trait_xx, "17"
                )
                trait17Presenter = optionalTraitSelectionPresenter(
                    traitTypeId = Trait.TYPE_ID_OPTION,
                    button = spinnerTraitSelection,
                    requestKey = REQUEST_KEY_SELECT_TRAIT_17
                ) { trait -> viewModel.updateTrait17(trait) }
                    .also { presenter ->
                        presenter.bindToFlow(
                            this@ConfigureEvaluationActivity,
                            lifecycleScope,
                            viewModel.trait17.map { it.trait }
                        )
                    }
                toggleTraitOptional.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateTrait17Optional(isChecked)
                }
                toggleTraitDeferred.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateTrait17Deferred(isChecked)
                }
                collectLatestOnStart(viewModel.trait17) { configuration ->
                    toggleTraitOptional.isChecked = configuration.isOptional
                    toggleTraitDeferred.isChecked = configuration.isDeferred
                }
            }
            with(evalTraitConfiguration18) {
                textTraitLabel.text = getString(
                    R.string.text_option_trait_xx, "18"
                )
                trait18Presenter = optionalTraitSelectionPresenter(
                    traitTypeId = Trait.TYPE_ID_OPTION,
                    button = spinnerTraitSelection,
                    requestKey = REQUEST_KEY_SELECT_TRAIT_18
                ) { trait -> viewModel.updateTrait18(trait) }
                    .also { presenter ->
                        presenter.bindToFlow(
                            this@ConfigureEvaluationActivity,
                            lifecycleScope,
                            viewModel.trait18.map { it.trait }
                        )
                    }
                toggleTraitOptional.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateTrait18Optional(isChecked)
                }
                toggleTraitDeferred.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateTrait18Deferred(isChecked)
                }
                collectLatestOnStart(viewModel.trait18) { configuration ->
                    toggleTraitOptional.isChecked = configuration.isOptional
                    toggleTraitDeferred.isChecked = configuration.isDeferred
                }
            }
            with(evalTraitConfiguration19) {
                textTraitLabel.text = getString(
                    R.string.text_option_trait_xx, "19"
                )
                trait19Presenter = optionalTraitSelectionPresenter(
                    traitTypeId = Trait.TYPE_ID_OPTION,
                    button = spinnerTraitSelection,
                    requestKey = REQUEST_KEY_SELECT_TRAIT_19
                ) { trait -> viewModel.updateTrait19(trait) }
                    .also { presenter ->
                        presenter.bindToFlow(
                            this@ConfigureEvaluationActivity,
                            lifecycleScope,
                            viewModel.trait19.map { it.trait }
                        )
                    }
                toggleTraitOptional.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateTrait19Optional(isChecked)
                }
                toggleTraitDeferred.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateTrait19Deferred(isChecked)
                }
                collectLatestOnStart(viewModel.trait19) { configuration ->
                    toggleTraitOptional.isChecked = configuration.isOptional
                    toggleTraitDeferred.isChecked = configuration.isDeferred
                }
            }
            with(evalTraitConfiguration20) {
                textTraitLabel.text = getString(
                    R.string.text_option_trait_xx, "20"
                )
                trait20Presenter = optionalTraitSelectionPresenter(
                    traitTypeId = Trait.TYPE_ID_OPTION,
                    button = spinnerTraitSelection,
                    requestKey = REQUEST_KEY_SELECT_TRAIT_20
                ) { trait -> viewModel.updateTrait20(trait) }
                    .also { presenter ->
                        presenter.bindToFlow(
                            this@ConfigureEvaluationActivity,
                            lifecycleScope,
                            viewModel.trait20.map { it.trait }
                        )
                    }
                toggleTraitOptional.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateTrait20Optional(isChecked)
                }
                toggleTraitDeferred.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateTrait20Deferred(isChecked)
                }
                collectLatestOnStart(viewModel.trait20) { configuration ->
                    toggleTraitOptional.isChecked = configuration.isOptional
                    toggleTraitDeferred.isChecked = configuration.isDeferred
                }
            }
        }
        observeOneTimeEventsOnStart(viewModel.events, ::handleEvent)
        observeErrorReports(viewModel.errorReportFlow)
    }

    private fun handleEvent(event: ConfigureEvaluationViewModel.Event) {
        when (event) {
            ConfigureEvaluationViewModel.EvaluationNameChanged -> {
                binding.inputEvaluationName.setText(viewModel.name)
            }
            ConfigureEvaluationViewModel.UpdateDatabaseSuccess -> {
                Toast.makeText(
                    this,
                    R.string.toast_add_evaluation_success,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private class ViewModelFactory(context: Context) : ViewModelProvider.Factory {

        private val appContext = context.applicationContext

        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return when (modelClass) {
                ConfigureEvaluationViewModel::class.java -> {
                    val databaseHandler = DatabaseManager.getInstance(appContext)
                        .createDatabaseHandler()
                    val evaluationRepo = EvaluationRepositoryImpl(databaseHandler)
                    val defSettingsRepo = DefaultSettingsRepositoryImpl(
                        databaseHandler, ActiveDefaultSettings.from(appContext)
                    )
                    val loadActiveDefaults = LoadActiveDefaultSettings(
                        activeDefaultSettings = ActiveDefaultSettings.from(appContext),
                        defSettingsRepo
                    )
                    val loadDefaultUserInfo = LoadDefaultUserInfo(loadActiveDefaults)
                    @Suppress("UNCHECKED_CAST")
                    ConfigureEvaluationViewModel(
                        evaluationRepo,
                        loadDefaultUserInfo
                    ) as T
                }
                else -> throw IllegalStateException("${modelClass.simpleName} is not supported.")
            }
        }
    }
}
