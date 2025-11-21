package com.weyr_associates.animaltrakkerfarmmobile.app.animal.evaluation

import android.app.Activity
import android.text.TextWatcher
import android.widget.RatingBar
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.weyr_associates.animaltrakkerfarmmobile.app.core.FragmentResultListenerRegistrar
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.collectLatestOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.launchRepeatingOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.asFragmentResultListenerRegistrar
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.coroutines.flow.observeOneTimeEvents
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.ItemSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.select.optionalEvalTraitOptionSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ItemEvalTraitUnitsBinding
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ViewEvaluationEditorBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.EvalTraitOption
import com.weyr_associates.animaltrakkerfarmmobile.model.UnitOfMeasure
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EvaluationEditorPresenter(
    private val binding: ViewEvaluationEditorBinding,
    private val lifecycleOwner: LifecycleOwner,
    private val fragmentResultListenerRegistrar: FragmentResultListenerRegistrar
) {

    constructor(activity: FragmentActivity, binding: ViewEvaluationEditorBinding)
        : this(binding, activity, activity.asFragmentResultListenerRegistrar())

    constructor(fragment: Fragment, binding: ViewEvaluationEditorBinding)
        : this(binding, fragment, fragment.asFragmentResultListenerRegistrar())

    private val lifecycleScope: LifecycleCoroutineScope
        get() = lifecycleOwner.lifecycleScope

    // Collects editor events
    private var collectEventsJob: Job? = null

    // Text watcher for units traits using edit text
    private var textWatcher11: TextWatcher? = null
    private var textWatcher12: TextWatcher? = null
    private var textWatcher13: TextWatcher? = null
    private var textWatcher14: TextWatcher? = null
    private var textWatcher15: TextWatcher? = null

    // Jobs for collecting unit trait values when
    // those values are weights and are not edited
    // with edit texts.
    private var trait11Job: Job? = null
    private var trait12Job: Job? = null
    private var trait13Job: Job? = null
    private var trait14Job: Job? = null
    private var trait15Job: Job? = null

    // Jobs for collecting options trait values
    // so that their item selection presenters
    // are kept up to date following changes.
    private var trait16BindingJob: Job? = null
    private var trait17BindingJob: Job? = null
    private var trait18BindingJob: Job? = null
    private var trait19BindingJob: Job? = null
    private var trait20BindingJob: Job? = null

    private var trait16Presenter: ItemSelectionPresenter<EvalTraitOption>? = null
    private var trait17Presenter: ItemSelectionPresenter<EvalTraitOption>? = null
    private var trait18Presenter: ItemSelectionPresenter<EvalTraitOption>? = null
    private var trait19Presenter: ItemSelectionPresenter<EvalTraitOption>? = null
    private var trait20Presenter: ItemSelectionPresenter<EvalTraitOption>? = null

    var onScanWeightForTrait: (EvaluationFieldId) -> Unit = {}
    var onEditWeightForTrait: (EvaluationFieldId, Float?) -> Unit = { _, _ -> }

    fun bindToEditor(editor: EvaluationEditor) {
        unbind()
        collectEvents(editor)
        bindToOptionsTraits(editor)
        configureTraitDisplays(editor)
    }

    fun unbind() {
        cancelAllJobs()
        dereferenceAllJobs()
        dereferenceAllPresenters()
        removeAllListeners()
    }

    private fun collectEvents(editor: EvaluationEditor) {
        collectEventsJob?.cancel()
        collectEventsJob = lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                editor.events.observeOneTimeEvents {
                    handleEvent(editor, it)
                }
            }
        }
    }

    private fun bindToOptionsTraits(editor: EvaluationEditor) {
        trait16Presenter = optionalEvalTraitOptionSelectionPresenter(
            fragmentResultListenerRegistrar,
            { editor.trait16Options },
            button = binding.inputTrait16.spinnerEvalTraitOptions,
            requestKey = REQUEST_KEY_SELECT_TRAIT_16_OPTION
        ) {
            editor.setTrait16(it)
        }.also {
            trait16BindingJob?.cancel()
            trait16BindingJob = it.bindToFlow(lifecycleOwner, lifecycleScope, editor.trait16)
        }

        trait17Presenter = optionalEvalTraitOptionSelectionPresenter(
            fragmentResultListenerRegistrar,
            { editor.trait17Options },
            button = binding.inputTrait17.spinnerEvalTraitOptions,
            requestKey = REQUEST_KEY_SELECT_TRAIT_17_OPTION
        ) {
            editor.setTrait17(it)
        }.also {
            trait17BindingJob?.cancel()
            trait17BindingJob = it.bindToFlow(lifecycleOwner, lifecycleScope, editor.trait17)
        }

        trait18Presenter = optionalEvalTraitOptionSelectionPresenter(
            fragmentResultListenerRegistrar,
            { editor.trait18Options },
            button = binding.inputTrait18.spinnerEvalTraitOptions,
            requestKey = REQUEST_KEY_SELECT_TRAIT_18_OPTION
        ) {
            editor.setTrait18(it)
        }.also {
            trait18BindingJob?.cancel()
            trait18BindingJob = it.bindToFlow(lifecycleOwner, lifecycleScope, editor.trait18)
        }

        trait19Presenter = optionalEvalTraitOptionSelectionPresenter(
            fragmentResultListenerRegistrar,
            { editor.trait19Options },
            button = binding.inputTrait19.spinnerEvalTraitOptions,
            requestKey = REQUEST_KEY_SELECT_TRAIT_19_OPTION
        ) {
            editor.setTrait19(it)
        }.also {
            trait19BindingJob?.cancel()
            trait19BindingJob = it.bindToFlow(lifecycleOwner, lifecycleScope, editor.trait19)
        }

        trait20Presenter = optionalEvalTraitOptionSelectionPresenter(
            fragmentResultListenerRegistrar,
            { editor.trait20Options },
            button = binding.inputTrait20.spinnerEvalTraitOptions,
            requestKey = REQUEST_KEY_SELECT_TRAIT_20_OPTION
        ) {
            editor.setTrait20(it)
        }.also {
            trait20BindingJob?.cancel()
            trait20BindingJob = it.bindToFlow(lifecycleOwner, lifecycleScope, editor.trait20)
        }
    }

    private fun handleEvent(editor: EvaluationEditor, event: EvaluationEditor.Event) {
        when (event) {
            EvaluationEditor.EvaluationLoaded -> {
                configureTraitDisplays(editor)
            }
            EvaluationEditor.FieldValuesCleared -> {
                handleFieldsCleared(editor)
            }
            is EvaluationEditor.FieldValueChanged -> {
                handleFieldValueChanged(editor, event.field)
            }
        }
    }

    private fun handleFieldValueChanged(editor: EvaluationEditor, field: EvaluationFieldId) {
        when (field) {
            EvaluationFieldId.TRAIT_01,
            EvaluationFieldId.TRAIT_02,
            EvaluationFieldId.TRAIT_03,
            EvaluationFieldId.TRAIT_04,
            EvaluationFieldId.TRAIT_05,
            EvaluationFieldId.TRAIT_06,
            EvaluationFieldId.TRAIT_07,
            EvaluationFieldId.TRAIT_08,
            EvaluationFieldId.TRAIT_09,
            EvaluationFieldId.TRAIT_10,
            EvaluationFieldId.TRAIT_11,
            EvaluationFieldId.TRAIT_12,
            EvaluationFieldId.TRAIT_13,
            EvaluationFieldId.TRAIT_14,
            EvaluationFieldId.TRAIT_15 -> {
                updateTraitValue(editor, field)
            }
            EvaluationFieldId.TRAIT_16,
            EvaluationFieldId.TRAIT_17,
            EvaluationFieldId.TRAIT_18,
            EvaluationFieldId.TRAIT_19,
            EvaluationFieldId.TRAIT_20 -> Unit //NO-OP for options which are bound through Flows.
        }
    }

    private fun handleFieldsCleared(editor: EvaluationEditor) {
        updateTraitValue(editor, EvaluationFieldId.TRAIT_01)
        updateTraitValue(editor, EvaluationFieldId.TRAIT_02)
        updateTraitValue(editor, EvaluationFieldId.TRAIT_03)
        updateTraitValue(editor, EvaluationFieldId.TRAIT_04)
        updateTraitValue(editor, EvaluationFieldId.TRAIT_05)
        updateTraitValue(editor, EvaluationFieldId.TRAIT_06)
        updateTraitValue(editor, EvaluationFieldId.TRAIT_07)
        updateTraitValue(editor, EvaluationFieldId.TRAIT_08)
        updateTraitValue(editor, EvaluationFieldId.TRAIT_09)
        updateTraitValue(editor, EvaluationFieldId.TRAIT_10)
        updateTraitValue(editor, EvaluationFieldId.TRAIT_11)
        updateTraitValue(editor, EvaluationFieldId.TRAIT_12)
        updateTraitValue(editor, EvaluationFieldId.TRAIT_13)
        updateTraitValue(editor, EvaluationFieldId.TRAIT_14)
        updateTraitValue(editor, EvaluationFieldId.TRAIT_15)
        //Options traits are bound to value flows, do not update here.
    }

    private fun configureTraitDisplays(editor: EvaluationEditor) {
        configureTraitVisibility(editor)
        configureTraitNames(editor)
        configureTraitUnits(editor)
        configureScoredTraitsHandlers(editor)
        configureUnitsTraitsHandlers(editor)
    }

    private fun configureTraitVisibility(editor: EvaluationEditor) {

        // Show traits only if we are collecting them.

        binding.inputTrait01.root.isGone = editor.trait01Field.traitEntry.isUncollected
        binding.inputTrait02.root.isGone = editor.trait02Field.traitEntry.isUncollected
        binding.inputTrait03.root.isGone = editor.trait03Field.traitEntry.isUncollected
        binding.inputTrait04.root.isGone = editor.trait04Field.traitEntry.isUncollected
        binding.inputTrait05.root.isGone = editor.trait05Field.traitEntry.isUncollected
        binding.inputTrait06.root.isGone = editor.trait06Field.traitEntry.isUncollected
        binding.inputTrait07.root.isGone = editor.trait07Field.traitEntry.isUncollected
        binding.inputTrait08.root.isGone = editor.trait08Field.traitEntry.isUncollected
        binding.inputTrait09.root.isGone = editor.trait09Field.traitEntry.isUncollected
        binding.inputTrait10.root.isGone = editor.trait10Field.traitEntry.isUncollected
        binding.inputTrait11.root.isGone = editor.trait11Field.traitEntry.isUncollected
        binding.inputTrait12.root.isGone = editor.trait12Field.traitEntry.isUncollected
        binding.inputTrait13.root.isGone = editor.trait13Field.traitEntry.isUncollected
        binding.inputTrait14.root.isGone = editor.trait14Field.traitEntry.isUncollected
        binding.inputTrait15.root.isGone = editor.trait15Field.traitEntry.isUncollected
        binding.inputTrait16.root.isGone = editor.trait16Field.traitEntry.isUncollected
        binding.inputTrait17.root.isGone = editor.trait17Field.traitEntry.isUncollected
        binding.inputTrait18.root.isGone = editor.trait18Field.traitEntry.isUncollected
        binding.inputTrait19.root.isGone = editor.trait19Field.traitEntry.isUncollected
        binding.inputTrait20.root.isGone = editor.trait20Field.traitEntry.isUncollected
    }

    private fun configureTraitNames(editor: EvaluationEditor) {
        binding.inputTrait01.textEvalTraitName.text = editor.trait01Field.traitName
        binding.inputTrait02.textEvalTraitName.text = editor.trait02Field.traitName
        binding.inputTrait03.textEvalTraitName.text = editor.trait03Field.traitName
        binding.inputTrait04.textEvalTraitName.text = editor.trait04Field.traitName
        binding.inputTrait05.textEvalTraitName.text = editor.trait05Field.traitName
        binding.inputTrait06.textEvalTraitName.text = editor.trait06Field.traitName
        binding.inputTrait07.textEvalTraitName.text = editor.trait07Field.traitName
        binding.inputTrait08.textEvalTraitName.text = editor.trait08Field.traitName
        binding.inputTrait09.textEvalTraitName.text = editor.trait09Field.traitName
        binding.inputTrait10.textEvalTraitName.text = editor.trait10Field.traitName
        binding.inputTrait11.textEvalTraitName.text = editor.trait11Field.traitName
        binding.inputTrait12.textEvalTraitName.text = editor.trait12Field.traitName
        binding.inputTrait13.textEvalTraitName.text = editor.trait13Field.traitName
        binding.inputTrait14.textEvalTraitName.text = editor.trait14Field.traitName
        binding.inputTrait15.textEvalTraitName.text = editor.trait15Field.traitName
        binding.inputTrait16.textEvalTraitName.text = editor.trait16Field.traitName
        binding.inputTrait17.textEvalTraitName.text = editor.trait17Field.traitName
        binding.inputTrait18.textEvalTraitName.text = editor.trait18Field.traitName
        binding.inputTrait19.textEvalTraitName.text = editor.trait19Field.traitName
        binding.inputTrait20.textEvalTraitName.text = editor.trait20Field.traitName
    }

    private fun configureTraitUnits(editor: EvaluationEditor) {
        binding.inputTrait11.textEvalTraitUnits.text = editor.trait11Units.abbreviation
        binding.inputTrait12.textEvalTraitUnits.text = editor.trait12Units.abbreviation
        binding.inputTrait13.textEvalTraitUnits.text = editor.trait13Units.abbreviation
        binding.inputTrait14.textEvalTraitUnits.text = editor.trait14Units.abbreviation
        binding.inputTrait15.textEvalTraitUnits.text = editor.trait15Units.abbreviation
    }

    private fun configureScoredTraitsHandlers(editor: EvaluationEditor) {

        binding.inputTrait01.ratingEvalTraitValue.setOnTraitScoreChangedListener { _, score ->
            editor.setTrait01(score)
        }
        binding.inputTrait02.ratingEvalTraitValue.setOnTraitScoreChangedListener { _, score ->
            editor.setTrait02(score)
        }
        binding.inputTrait03.ratingEvalTraitValue.setOnTraitScoreChangedListener { _, score ->
            editor.setTrait03(score)
        }
        binding.inputTrait04.ratingEvalTraitValue.setOnTraitScoreChangedListener { _, score ->
            editor.setTrait04(score)
        }
        binding.inputTrait05.ratingEvalTraitValue.setOnTraitScoreChangedListener { _, score ->
            editor.setTrait05(score)
        }
        binding.inputTrait06.ratingEvalTraitValue.setOnTraitScoreChangedListener { _, score ->
            editor.setTrait06(score)
        }
        binding.inputTrait07.ratingEvalTraitValue.setOnTraitScoreChangedListener { _, score ->
            editor.setTrait07(score)
        }
        binding.inputTrait08.ratingEvalTraitValue.setOnTraitScoreChangedListener { _, score ->
            editor.setTrait08(score)
        }
        binding.inputTrait09.ratingEvalTraitValue.setOnTraitScoreChangedListener { _, score ->
            editor.setTrait09(score)
        }
        binding.inputTrait10.ratingEvalTraitValue.setOnTraitScoreChangedListener { _, score ->
            editor.setTrait10(score)
        }
    }

    private fun configureUnitsTraitsHandlers(editor: EvaluationEditor) {
        removeAllTextWatchers()
        textWatcher11 = binding.inputTrait11.inputEvalTraitValue.addTextChangedListener {
            editor.setTrait11(it.toString().toFloatOrNull())
        }
        textWatcher12 = binding.inputTrait12.inputEvalTraitValue.addTextChangedListener {
            editor.setTrait12(it.toString().toFloatOrNull())
        }
        textWatcher13 = binding.inputTrait13.inputEvalTraitValue.addTextChangedListener {
            editor.setTrait13(it.toString().toFloatOrNull())
        }
        textWatcher14 = binding.inputTrait14.inputEvalTraitValue.addTextChangedListener {
            editor.setTrait14(it.toString().toFloatOrNull())
        }
        textWatcher15 = binding.inputTrait15.inputEvalTraitValue.addTextChangedListener {
            editor.setTrait15(it.toString().toFloatOrNull())
        }

        // Cancel any coroutine job watching unit traits values
        // so if the new trait is not weight based edit texts
        // will work properly without values being reposted
        // as they are entered.
        trait11Job?.cancel()
        trait12Job?.cancel()
        trait13Job?.cancel()
        trait14Job?.cancel()
        trait15Job?.cancel()

        // Setup controls and capture resulting coroutine
        // jobs if value changes are monitored by collecting flows.
        trait11Job = binding.inputTrait11.setupInputControls(
            editor.trait11Field, editor.trait11, editor.trait11Units
        )
        trait12Job = binding.inputTrait12.setupInputControls(
            editor.trait12Field, editor.trait12, editor.trait12Units
        )
        trait13Job = binding.inputTrait13.setupInputControls(
            editor.trait13Field, editor.trait13, editor.trait13Units
        )
        trait14Job = binding.inputTrait14.setupInputControls(
            editor.trait14Field, editor.trait14, editor.trait14Units
        )
        trait15Job = binding.inputTrait15.setupInputControls(
            editor.trait15Field, editor.trait15, editor.trait15Units
        )
    }

    private fun ItemEvalTraitUnitsBinding.setupInputControls(
        field: EvaluationField,
        currentValue: StateFlow<Float?>,
        units: UnitOfMeasure): Job? {
        setupClickListenersForWeights(this, field.id, currentValue)
        val useWeightControls = units.type.isWeight
        inputEvalTraitValue.isGone = useWeightControls
        buttonScanWeight.isVisible = useWeightControls
        buttonEditWeight.isVisible = useWeightControls
        displayEvalTraitValue.isVisible = useWeightControls
        // If we are editing weight using means other than
        // the edit text, watch for value changes to update displays.
        return if (useWeightControls) {
            lifecycleOwner.collectLatestOnStart(currentValue) { value ->
                val displayString = unitTraitDisplayString(value, units)
                inputEvalTraitValue.setText(displayString)
                displayEvalTraitValue.text = displayString
            }
        } else null
    }

    private fun setupClickListenersForWeights(
        binding: ItemEvalTraitUnitsBinding,
        evalFieldId: EvaluationFieldId,
        currentWeight: StateFlow<Float?>
    ) {
        binding.buttonScanWeight.setOnClickListener {
            onScanWeightForTrait.invoke(evalFieldId)
        }
        binding.buttonEditWeight.setOnClickListener {
            onEditWeightForTrait.invoke(evalFieldId, currentWeight.value)
        }
    }

    private fun updateTraitValue(editor: EvaluationEditor, field: EvaluationFieldId) {
        when (field) {
            EvaluationFieldId.TRAIT_01 -> updateScoredTrait(
                binding.inputTrait01.ratingEvalTraitValue, editor.trait01.value ?: 0
            )
            EvaluationFieldId.TRAIT_02 -> updateScoredTrait(
                binding.inputTrait02.ratingEvalTraitValue, editor.trait02.value ?: 0
            )
            EvaluationFieldId.TRAIT_03 -> updateScoredTrait(
                binding.inputTrait03.ratingEvalTraitValue, editor.trait03.value ?: 0
            )
            EvaluationFieldId.TRAIT_04 -> updateScoredTrait(
                binding.inputTrait04.ratingEvalTraitValue, editor.trait04.value ?: 0
            )
            EvaluationFieldId.TRAIT_05 -> updateScoredTrait(
                binding.inputTrait05.ratingEvalTraitValue, editor.trait05.value ?: 0
            )
            EvaluationFieldId.TRAIT_06 -> updateScoredTrait(
                binding.inputTrait06.ratingEvalTraitValue, editor.trait06.value ?: 0
            )
            EvaluationFieldId.TRAIT_07 -> updateScoredTrait(
                binding.inputTrait07.ratingEvalTraitValue, editor.trait07.value ?: 0
            )
            EvaluationFieldId.TRAIT_08 -> updateScoredTrait(
                binding.inputTrait08.ratingEvalTraitValue, editor.trait08.value ?: 0
            )
            EvaluationFieldId.TRAIT_09 -> updateScoredTrait(
                binding.inputTrait09.ratingEvalTraitValue, editor.trait09.value ?: 0
            )
            EvaluationFieldId.TRAIT_10 -> updateScoredTrait(
                binding.inputTrait10.ratingEvalTraitValue, editor.trait10.value ?: 0
            )
            EvaluationFieldId.TRAIT_11 -> updateUnitsTrait(
                binding.inputTrait11,
                editor.trait11.value,
                editor.trait11Units
            )
            EvaluationFieldId.TRAIT_12 -> updateUnitsTrait(
                binding.inputTrait12,
                editor.trait12.value,
                editor.trait12Units
            )
            EvaluationFieldId.TRAIT_13 -> updateUnitsTrait(
                binding.inputTrait13,
                editor.trait13.value,
                editor.trait13Units
            )
            EvaluationFieldId.TRAIT_14 -> updateUnitsTrait(
                binding.inputTrait14,
                editor.trait14.value,
                editor.trait14Units
            )
            EvaluationFieldId.TRAIT_15 -> updateUnitsTrait(
                binding.inputTrait15,
                editor.trait15.value,
                editor.trait15Units
            )
            EvaluationFieldId.TRAIT_16 -> updateOptionsTrait(
                trait16Presenter,
                editor.trait16.value
            )
            EvaluationFieldId.TRAIT_17 -> updateOptionsTrait(
                trait17Presenter,
                editor.trait17.value
            )
            EvaluationFieldId.TRAIT_18 -> updateOptionsTrait(
                trait18Presenter,
                editor.trait18.value
            )
            EvaluationFieldId.TRAIT_19 -> updateOptionsTrait(
                trait19Presenter,
                editor.trait19.value
            )
            EvaluationFieldId.TRAIT_20 -> updateOptionsTrait(
                trait20Presenter,
                editor.trait20.value
            )
        }
    }

    private fun updateScoredTrait(ratingBar: RatingBar, score: Int) {
        ratingBar.rating = score.toFloat()
    }

    private fun updateUnitsTrait(binding: ItemEvalTraitUnitsBinding, value: Float?, units: UnitOfMeasure) {
        val displayString = unitTraitDisplayString(value, units)
        binding.inputEvalTraitValue.setText(displayString)
        binding.displayEvalTraitValue.text = displayString
    }

    private fun unitTraitDisplayString(value: Float?, units: UnitOfMeasure): String {
        //TODO: Use units if necessary for Float -> String conversion.
        return value?.toString() ?: ""
    }

    private fun updateOptionsTrait(
        presenter: ItemSelectionPresenter<EvalTraitOption>?,
        option: EvalTraitOption?
    ) {
        presenter?.displaySelectedItem(option)
    }

    private fun cancelAllJobs() {
        listOf(
            collectEventsJob,
            trait11Job,
            trait12Job,
            trait13Job,
            trait14Job,
            trait15Job,
            trait16BindingJob,
            trait17BindingJob,
            trait18BindingJob,
            trait19BindingJob,
            trait20BindingJob,
        ).forEach { it?.cancel() }
    }

    private fun dereferenceAllJobs() {
        collectEventsJob = null
        trait11Job = null
        trait12Job = null
        trait13Job = null
        trait14Job = null
        trait15Job = null
        trait16BindingJob = null
        trait17BindingJob = null
        trait18BindingJob = null
        trait19BindingJob = null
        trait20BindingJob = null
    }

    private fun dereferenceAllPresenters() {
        trait16Presenter = null
        trait17Presenter = null
        trait18Presenter = null
        trait19Presenter = null
        trait20Presenter = null
    }

    private fun removeAllListeners() {
        removeAllTextWatchers()
        unsetAllClickListeners()
    }

    private fun removeAllTextWatchers() {
        textWatcher11?.let { binding.inputTrait11.inputEvalTraitValue.removeTextChangedListener(it) }
        textWatcher12?.let { binding.inputTrait12.inputEvalTraitValue.removeTextChangedListener(it) }
        textWatcher13?.let { binding.inputTrait13.inputEvalTraitValue.removeTextChangedListener(it) }
        textWatcher14?.let { binding.inputTrait14.inputEvalTraitValue.removeTextChangedListener(it) }
        textWatcher15?.let { binding.inputTrait15.inputEvalTraitValue.removeTextChangedListener(it) }
        textWatcher11 = null
        textWatcher12 = null
        textWatcher13 = null
        textWatcher14 = null
        textWatcher15 = null
    }

    private fun unsetAllClickListeners() {
        listOf(
            binding.inputTrait01,
            binding.inputTrait02,
            binding.inputTrait03,
            binding.inputTrait04,
            binding.inputTrait05,
            binding.inputTrait06,
            binding.inputTrait07,
            binding.inputTrait08,
            binding.inputTrait09,
            binding.inputTrait10
        ).forEach {
            it.ratingEvalTraitValue.setOnTraitScoreChangedListener(null)
        }
        listOf(
            binding.inputTrait11,
            binding.inputTrait12,
            binding.inputTrait13,
            binding.inputTrait14,
            binding.inputTrait15
        ).forEach {
            it.buttonScanWeight.setOnClickListener(null)
            it.buttonEditWeight.setOnClickListener(null)
        }
        listOf(
            binding.inputTrait16,
            binding.inputTrait17,
            binding.inputTrait18,
            binding.inputTrait19,
            binding.inputTrait20
        ).forEach {
            it.spinnerEvalTraitOptions.setOnClickListener(null)
        }
    }

    private fun RatingBar.setOnTraitScoreChangedListener(listener: ((RatingBar?, Int) -> Unit)?) {
        if (listener == null) {
            onRatingBarChangeListener = null
        } else {
            setOnRatingBarChangeListener { ratingBar, value, fromUser ->
                if (fromUser) {
                    listener(ratingBar, value.toInt())
                }
            }
        }
    }

    companion object {
        private const val REQUEST_KEY_SELECT_TRAIT_16_OPTION = "REQUEST_KEY_SELECT_TRAIT_16_OPTION"
        private const val REQUEST_KEY_SELECT_TRAIT_17_OPTION = "REQUEST_KEY_SELECT_TRAIT_17_OPTION"
        private const val REQUEST_KEY_SELECT_TRAIT_18_OPTION = "REQUEST_KEY_SELECT_TRAIT_18_OPTION"
        private const val REQUEST_KEY_SELECT_TRAIT_19_OPTION = "REQUEST_KEY_SELECT_TRAIT_19_OPTION"
        private const val REQUEST_KEY_SELECT_TRAIT_20_OPTION = "REQUEST_KEY_SELECT_TRAIT_20_OPTION"
    }
}
