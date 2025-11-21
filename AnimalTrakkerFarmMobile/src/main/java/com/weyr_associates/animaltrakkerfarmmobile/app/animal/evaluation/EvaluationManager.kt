package com.weyr_associates.animaltrakkerfarmmobile.app.animal.evaluation

import com.weyr_associates.animaltrakkerfarmmobile.app.animal.evaluation.EvaluationEditor.Event
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.evaluation.EvaluationEditor.FieldValueChanged
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.coroutines.flow.combine21
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.coroutines.flow.combine22
import com.weyr_associates.animaltrakkerfarmmobile.model.CustomEvalTrait
import com.weyr_associates.animaltrakkerfarmmobile.model.EvalTrait
import com.weyr_associates.animaltrakkerfarmmobile.model.EvalTraitOption
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.SavedEvaluation
import com.weyr_associates.animaltrakkerfarmmobile.model.UnitOfMeasure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EvaluationManager(private val coroutineScope: CoroutineScope) : EvaluationEditor {

    private var currentEvaluation: SavedEvaluation? = null
        set(value) {
            field = value
            _loadedEvaluation.update { field }
        }

    private val _loadedEvaluation = MutableStateFlow<SavedEvaluation?>(null)
    val loadedEvaluation = _loadedEvaluation.asStateFlow()

    val isEvaluationLoaded = loadedEvaluation.map { it != null }
        .stateIn(coroutineScope, SharingStarted.Lazily, false)

    private val customTraitPreselectedOptions = mutableMapOf<EntityId,EntityId>()

    fun loadEvaluation(evaluation: SavedEvaluation) {
        clearEvaluation()
        captureEvaluationFields(evaluation)
        currentEvaluation = evaluation
        preselectCustomTraitOptions()
        sendEvent(EvaluationEditor.EvaluationLoaded)
    }

    private val eventsChannel = Channel<Event>()

    override val events: Flow<Event>
        get() = eventsChannel.receiveAsFlow()

    //region Field Entry Requirements

    override var trait01Field: EvaluationField = EvaluationField(EvaluationFieldId.TRAIT_01)
        private set
    override var trait02Field: EvaluationField = EvaluationField(EvaluationFieldId.TRAIT_02)
        private set
    override var trait03Field: EvaluationField = EvaluationField(EvaluationFieldId.TRAIT_03)
        private set
    override var trait04Field: EvaluationField = EvaluationField(EvaluationFieldId.TRAIT_04)
        private set
    override var trait05Field: EvaluationField = EvaluationField(EvaluationFieldId.TRAIT_05)
        private set
    override var trait06Field: EvaluationField = EvaluationField(EvaluationFieldId.TRAIT_06)
        private set
    override var trait07Field: EvaluationField = EvaluationField(EvaluationFieldId.TRAIT_07)
        private set
    override var trait08Field: EvaluationField = EvaluationField(EvaluationFieldId.TRAIT_08)
        private set
    override var trait09Field: EvaluationField = EvaluationField(EvaluationFieldId.TRAIT_09)
        private set
    override var trait10Field: EvaluationField = EvaluationField(EvaluationFieldId.TRAIT_10)
        private set
    override var trait11Field: EvaluationField = EvaluationField(EvaluationFieldId.TRAIT_11)
        private set
    override var trait12Field: EvaluationField = EvaluationField(EvaluationFieldId.TRAIT_12)
        private set
    override var trait13Field: EvaluationField = EvaluationField(EvaluationFieldId.TRAIT_13)
        private set
    override var trait14Field: EvaluationField = EvaluationField(EvaluationFieldId.TRAIT_14)
        private set
    override var trait15Field: EvaluationField = EvaluationField(EvaluationFieldId.TRAIT_15)
        private set
    override var trait16Field: EvaluationField = EvaluationField(EvaluationFieldId.TRAIT_16)
        private set
    override var trait17Field: EvaluationField = EvaluationField(EvaluationFieldId.TRAIT_17)
        private set
    override var trait18Field: EvaluationField = EvaluationField(EvaluationFieldId.TRAIT_18)
        private set
    override var trait19Field: EvaluationField = EvaluationField(EvaluationFieldId.TRAIT_19)
        private set
    override var trait20Field: EvaluationField = EvaluationField(EvaluationFieldId.TRAIT_20)
        private set

    //endregion

    //region Field Units

    override val trait11Units: UnitOfMeasure
        get() = currentEvaluation?.trait11?.units ?: UnitOfMeasure.NONE
    override val trait12Units: UnitOfMeasure
        get() = currentEvaluation?.trait12?.units ?: UnitOfMeasure.NONE
    override val trait13Units: UnitOfMeasure
        get() = currentEvaluation?.trait13?.units ?: UnitOfMeasure.NONE
    override val trait14Units: UnitOfMeasure
        get() = currentEvaluation?.trait14?.units ?: UnitOfMeasure.NONE
    override val trait15Units: UnitOfMeasure
        get() = currentEvaluation?.trait15?.units ?: UnitOfMeasure.NONE

    //endregion

    //region Field Options

    override val trait16Options: List<EvalTraitOption>
        get() = currentEvaluation?.trait16?.options ?: emptyList()
    override val trait17Options: List<EvalTraitOption>
        get() = currentEvaluation?.trait17?.options ?: emptyList()
    override val trait18Options: List<EvalTraitOption>
        get() = currentEvaluation?.trait18?.options ?: emptyList()
    override val trait19Options: List<EvalTraitOption>
        get() = currentEvaluation?.trait19?.options ?: emptyList()
    override val trait20Options: List<EvalTraitOption>
        get() = currentEvaluation?.trait20?.options ?: emptyList()

    //endregion

    //region Field Values

    private val _trait01 = MutableStateFlow<Int?>(null)
    override val trait01: StateFlow<Int?>
        get() = _trait01.asStateFlow()

    private val _trait02 = MutableStateFlow<Int?>(null)
    override val trait02: StateFlow<Int?>
        get() = _trait02.asStateFlow()

    private val _trait03 = MutableStateFlow<Int?>(null)
    override val trait03: StateFlow<Int?>
        get() = _trait03.asStateFlow()

    private val _trait04 = MutableStateFlow<Int?>(null)
    override val trait04: StateFlow<Int?>
        get() = _trait04.asStateFlow()

    private val _trait05 = MutableStateFlow<Int?>(null)
    override val trait05: StateFlow<Int?>
        get() = _trait05.asStateFlow()

    private val _trait06 = MutableStateFlow<Int?>(null)
    override val trait06: StateFlow<Int?>
        get() = _trait06.asStateFlow()

    private val _trait07 = MutableStateFlow<Int?>(null)
    override val trait07: StateFlow<Int?>
        get() = _trait07.asStateFlow()

    private val _trait08 = MutableStateFlow<Int?>(null)
    override val trait08: StateFlow<Int?>
        get() = _trait08.asStateFlow()

    private val _trait09 = MutableStateFlow<Int?>(null)
    override val trait09: StateFlow<Int?>
        get() = _trait09.asStateFlow()

    private val _trait10 = MutableStateFlow<Int?>(null)
    override val trait10: StateFlow<Int?>
        get() = _trait10.asStateFlow()

    private val _trait11 = MutableStateFlow<Float?>(null)
    override val trait11: StateFlow<Float?>
        get() = _trait11.asStateFlow()

    private val _trait12 = MutableStateFlow<Float?>(null)
    override val trait12: StateFlow<Float?>
        get() = _trait12.asStateFlow()

    private val _trait13 = MutableStateFlow<Float?>(null)
    override val trait13: StateFlow<Float?>
        get() = _trait13.asStateFlow()

    private val _trait14 = MutableStateFlow<Float?>(null)
    override val trait14: StateFlow<Float?>
        get() = _trait14.asStateFlow()

    private val _trait15 = MutableStateFlow<Float?>(null)
    override val trait15: StateFlow<Float?>
        get() = _trait15.asStateFlow()

    private val _trait16 = MutableStateFlow<EvalTraitOption?>(null)
    override val trait16: StateFlow<EvalTraitOption?>
        get() = _trait16.asStateFlow()

    private val _trait17 = MutableStateFlow<EvalTraitOption?>(null)
    override val trait17: StateFlow<EvalTraitOption?>
        get() = _trait17.asStateFlow()

    private val _trait18 = MutableStateFlow<EvalTraitOption?>(null)
    override val trait18: StateFlow<EvalTraitOption?>
        get() = _trait18.asStateFlow()

    private val _trait19 = MutableStateFlow<EvalTraitOption?>(null)
    override val trait19: StateFlow<EvalTraitOption?>
        get() = _trait19.asStateFlow()

    private val _trait20 = MutableStateFlow<EvalTraitOption?>(null)
    override val trait20: StateFlow<EvalTraitOption?>
        get() = _trait20.asStateFlow()

    //endregion

    //region Evaluation States

    private val hasEvaluationDataEntered = combine21(
        isEvaluationLoaded,
        trait01, trait02, trait03, trait04, trait05,
        trait06, trait07, trait08, trait09, trait10,
        trait11, trait12, trait13, trait14, trait15,
        trait16, trait17, trait18, trait19, trait20,
    ) { isEvalLoaded, t01, t02, t03, t04, t05, t06, t07, t08, t09, t10,
        t11, t12, t13, t14, t15, t16, t17, t18, t19, t20 ->
        isEvalLoaded && (t01 != 0 || t02 != 0 || t03 != 0 || t04 != 0 ||
                t05 != 0 || t06 != 0 || t07 != 0 || t08 != 0 ||
                t09 != 0 || t10 != 0 || t11 != null || t12 != null ||
                t13 != null || t14 != null || t15 != null || t16 != null ||
                t17 != null || t18 != null || t19 != null || t20 != null)
    }.stateIn(coroutineScope, SharingStarted.Lazily, false)

    val canClearData = combine21(
        isEvaluationLoaded,
        trait01, trait02, trait03, trait04, trait05,
        trait06, trait07, trait08, trait09, trait10,
        trait11, trait12, trait13, trait14, trait15,
        trait16, trait17, trait18, trait19, trait20,
    ) { isEvalLoaded, t01, t02, t03, t04, t05, t06, t07, t08, t09, t10,
        t11, t12, t13, t14, t15, t16, t17, t18, t19, t20 ->
        isEvalLoaded && (t01 != null || t02 != null || t03 != null || t04 != null ||
                t05 != null || t06 != null || t07 != null || t08 != null ||
                t09 != null || t10 != null || t11 != null || t12 != null ||
                t13 != null || t14 != null || t15 != null ||
                isSetToValueAllowingClear(t16) ||
                isSetToValueAllowingClear(t17) ||
                isSetToValueAllowingClear(t18) ||
                isSetToValueAllowingClear(t19) ||
                isSetToValueAllowingClear(t20))
    }.stateIn(coroutineScope, SharingStarted.Lazily, false)
    
    val isEvaluationComplete: Flow<Boolean> = combine22(
        isEvaluationLoaded, hasEvaluationDataEntered,
        trait01, trait02, trait03, trait04, trait05,
        trait06, trait07, trait08, trait09, trait10,
        trait11, trait12, trait13, trait14, trait15,
        trait16, trait17, trait18, trait19, trait20,
    ) { isEvalLoaded, hasEvaluationDataEntered, t01, t02, t03, t04, t05, t06, t07, t08, t09, t10,
        t11, t12, t13, t14, t15, t16, t17, t18, t19, t20 ->
        isEvalLoaded && hasEvaluationDataEntered &&
                (t01 != null || trait01Field.traitEntry.isNotRequired) &&
                (t02 != null || trait02Field.traitEntry.isNotRequired) &&
                (t03 != null || trait03Field.traitEntry.isNotRequired) &&
                (t04 != null || trait04Field.traitEntry.isNotRequired) &&
                (t05 != null || trait05Field.traitEntry.isNotRequired) &&
                (t06 != null || trait06Field.traitEntry.isNotRequired) &&
                (t07 != null || trait07Field.traitEntry.isNotRequired) &&
                (t08 != null || trait08Field.traitEntry.isNotRequired) &&
                (t09 != null || trait09Field.traitEntry.isNotRequired) &&
                (t10 != null || trait10Field.traitEntry.isNotRequired) &&
                (t11 != null || trait11Field.traitEntry.isNotRequired) &&
                (t12 != null || trait12Field.traitEntry.isNotRequired) &&
                (t13 != null || trait13Field.traitEntry.isNotRequired) &&
                (t14 != null || trait14Field.traitEntry.isNotRequired) &&
                (t15 != null || trait15Field.traitEntry.isNotRequired) &&
                (t16 != null || trait16Field.traitEntry.isNotRequired) &&
                (t17 != null || trait17Field.traitEntry.isNotRequired) &&
                (t18 != null || trait18Field.traitEntry.isNotRequired) &&
                (t19 != null || trait19Field.traitEntry.isNotRequired) &&
                (t20 != null || trait20Field.traitEntry.isNotRequired)
    }.stateIn(coroutineScope, SharingStarted.Lazily, false)

    //endregion

    //region Field Manipulators

    override fun setTrait01(value: Int?) {
        _trait01.update { value.clampToScoreRange() }
    }

    override fun setTrait02(value: Int?) {
        _trait02.update { value.clampToScoreRange() }
    }

    override fun setTrait03(value: Int?) {
        _trait03.update { value.clampToScoreRange() }
    }

    override fun setTrait04(value: Int?) {
        _trait04.update { value.clampToScoreRange() }
    }

    override fun setTrait05(value: Int?) {
        _trait05.update { value.clampToScoreRange() }
    }

    override fun setTrait06(value: Int?) {
        _trait06.update { value.clampToScoreRange() }
    }

    override fun setTrait07(value: Int?) {
        _trait07.update { value.clampToScoreRange() }
    }

    override fun setTrait08(value: Int?) {
        _trait08.update { value.clampToScoreRange() }
    }

    override fun setTrait09(value: Int?) {
        _trait09.update { value.clampToScoreRange() }
    }

    override fun setTrait10(value: Int?) {
        _trait10.update { value.clampToScoreRange() }
    }

    override fun setTrait11(value: Float?) {
        _trait11.update { value }
    }

    override fun setTrait12(value: Float?) {
        _trait12.update { value }
    }

    override fun setTrait13(value: Float?) {
        _trait13.update { value }
    }

    override fun setTrait14(value: Float?) {
        _trait14.update { value }
    }

    override fun setTrait15(value: Float?) {
        _trait15.update { value }
    }

    override fun setTrait16(option: EvalTraitOption?) {
        _trait16.update { option }
    }

    override fun setTrait17(option: EvalTraitOption?) {
        _trait17.update { option }
    }

    override fun setTrait18(option: EvalTraitOption?) {
        _trait18.update { option }
    }

    override fun setTrait19(option: EvalTraitOption?) {
        _trait19.update { option }
    }

    override fun setTrait20(option: EvalTraitOption?) {
        _trait20.update { option }
    }

    fun preselectCustomTraitToOptionId(customTraitId: EntityId, optionId: EntityId) {
        customTraitPreselectedOptions[customTraitId] = optionId
    }

    fun clearPreselectedCustomTraitOptionIds() {
        customTraitPreselectedOptions.clear()
    }

    fun clearEvaluation() {
        setTrait01(null)
        setTrait02(null)
        setTrait03(null)
        setTrait04(null)
        setTrait05(null)
        setTrait06(null)
        setTrait07(null)
        setTrait08(null)
        setTrait09(null)
        setTrait10(null)
        setTrait11(null)
        setTrait12(null)
        setTrait13(null)
        setTrait14(null)
        setTrait15(null)
        preselectCustomTraitOptions()
        sendEvent(EvaluationEditor.FieldValuesCleared)
    }

    fun extractEvaluationEntry(): EvaluationEntries? {
        val evaluation = currentEvaluation ?: return null
        return EvaluationEntries(
            trait01Id = evaluation.trait01?.id,
            trait02Id = evaluation.trait02?.id,
            trait03Id = evaluation.trait03?.id,
            trait04Id = evaluation.trait04?.id,
            trait05Id = evaluation.trait05?.id,
            trait06Id = evaluation.trait06?.id,
            trait07Id = evaluation.trait07?.id,
            trait08Id = evaluation.trait08?.id,
            trait09Id = evaluation.trait09?.id,
            trait10Id = evaluation.trait10?.id,
            trait11Id = evaluation.trait11?.id,
            trait12Id = evaluation.trait12?.id,
            trait13Id = evaluation.trait13?.id,
            trait14Id = evaluation.trait14?.id,
            trait15Id = evaluation.trait15?.id,
            trait16Id = evaluation.trait16?.id,
            trait17Id = evaluation.trait17?.id,
            trait18Id = evaluation.trait18?.id,
            trait19Id = evaluation.trait19?.id,
            trait20Id = evaluation.trait20?.id,
            trait11UnitsId = evaluation.trait11?.units?.id,
            trait12UnitsId = evaluation.trait12?.units?.id,
            trait13UnitsId = evaluation.trait13?.units?.id,
            trait14UnitsId = evaluation.trait14?.units?.id,
            trait15UnitsId = evaluation.trait15?.units?.id,
            trait01Score = trait01.value,
            trait02Score = trait02.value,
            trait03Score = trait03.value,
            trait04Score = trait04.value,
            trait05Score = trait05.value,
            trait06Score = trait06.value,
            trait07Score = trait07.value,
            trait08Score = trait08.value,
            trait09Score = trait09.value,
            trait10Score = trait10.value,
            trait11Score = trait11.value,
            trait12Score = trait12.value,
            trait13Score = trait13.value,
            trait14Score = trait14.value,
            trait15Score = trait15.value,
            trait16OptionId = trait16.value?.id,
            trait17OptionId = trait17.value?.id,
            trait18OptionId = trait18.value?.id,
            trait19OptionId = trait19.value?.id,
            trait20OptionId = trait20.value?.id,
        )
    }
    
    //endregion

    private fun captureEvaluationFields(savedEvaluation: SavedEvaluation) {
        trait01Field = savedEvaluation.trait01.extractField(EvaluationFieldId.TRAIT_01)
        trait02Field = savedEvaluation.trait02.extractField(EvaluationFieldId.TRAIT_02)
        trait03Field = savedEvaluation.trait03.extractField(EvaluationFieldId.TRAIT_03)
        trait04Field = savedEvaluation.trait04.extractField(EvaluationFieldId.TRAIT_04)
        trait05Field = savedEvaluation.trait05.extractField(EvaluationFieldId.TRAIT_05)
        trait06Field = savedEvaluation.trait06.extractField(EvaluationFieldId.TRAIT_06)
        trait07Field = savedEvaluation.trait07.extractField(EvaluationFieldId.TRAIT_07)
        trait08Field = savedEvaluation.trait08.extractField(EvaluationFieldId.TRAIT_08)
        trait09Field = savedEvaluation.trait09.extractField(EvaluationFieldId.TRAIT_09)
        trait10Field = savedEvaluation.trait10.extractField(EvaluationFieldId.TRAIT_10)
        trait11Field = savedEvaluation.trait11.extractField(EvaluationFieldId.TRAIT_11)
        trait12Field = savedEvaluation.trait12.extractField(EvaluationFieldId.TRAIT_12)
        trait13Field = savedEvaluation.trait13.extractField(EvaluationFieldId.TRAIT_13)
        trait14Field = savedEvaluation.trait14.extractField(EvaluationFieldId.TRAIT_14)
        trait15Field = savedEvaluation.trait15.extractField(EvaluationFieldId.TRAIT_15)
        trait16Field = savedEvaluation.trait16.extractField(EvaluationFieldId.TRAIT_16)
        trait17Field = savedEvaluation.trait17.extractField(EvaluationFieldId.TRAIT_17)
        trait18Field = savedEvaluation.trait18.extractField(EvaluationFieldId.TRAIT_18)
        trait19Field = savedEvaluation.trait19.extractField(EvaluationFieldId.TRAIT_19)
        trait20Field = savedEvaluation.trait20.extractField(EvaluationFieldId.TRAIT_20)
    }

    private fun sendFieldValueChanged(field: EvaluationFieldId) {
        sendEvent(FieldValueChanged(field))
    }

    private fun sendEvent(event: Event) {
        coroutineScope.launch {
            eventsChannel.send(event)
        }
    }

    private fun preselectCustomTraitOptions() {
        val evaluation = currentEvaluation ?: return
        val customTraitSetters = listOf(
            Pair(evaluation.trait16, ::setTrait16),
            Pair(evaluation.trait17, ::setTrait17),
            Pair(evaluation.trait18, ::setTrait18),
            Pair(evaluation.trait19, ::setTrait19),
            Pair(evaluation.trait20, ::setTrait20)
        )
        for (customTraitSetter: Pair<CustomEvalTrait?, (EvalTraitOption?) -> Unit> in customTraitSetters) {
            val (customEvalTrait, optionSetter) = customTraitSetter
            val preselectedOptionId = customEvalTrait?.id?.let { customTraitPreselectedOptions[it] }
            customEvalTrait?.let {
                optionSetter.invoke(
                    customEvalTrait.options.firstOrNull {
                        it.id == preselectedOptionId
                    }
                )
            }
        }
    }

    private fun isSetToValueAllowingClear(selectedTraitOption: EvalTraitOption?): Boolean {
        val traitOption = selectedTraitOption ?: return false
        return traitOption.id != customTraitPreselectedOptions[traitOption.traitId]
    }
}

private fun Int?.clampToScoreRange(): Int? = when {
    this == null -> null
    this <= 0 -> null
    else -> coerceIn(1..5)
}

private fun EvalTrait?.extractField(id: EvaluationFieldId): EvaluationField {
    return EvaluationField(
        id = id,
        traitId = extractFieldId(),
        traitName = extractFieldName(),
        traitEntry = extractFieldEntry()
    )
}

private fun EvalTrait?.extractFieldEntry(): EvaluationField.Entry = when {
    this == null -> EvaluationField.Entry.UNCOLLECTED
    isEmpty || isDeferred -> EvaluationField.Entry.UNCOLLECTED
    isOptional -> EvaluationField.Entry.OPTIONAL
    else -> EvaluationField.Entry.REQUIRED
}

private fun EvalTrait?.extractFieldId(): EntityId? {
    return this?.id
}

private fun EvalTrait?.extractFieldName(): String {
    return this?.name ?: ""
}
