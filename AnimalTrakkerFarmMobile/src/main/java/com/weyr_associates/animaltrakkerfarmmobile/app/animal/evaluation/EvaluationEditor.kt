package com.weyr_associates.animaltrakkerfarmmobile.app.animal.evaluation

import com.weyr_associates.animaltrakkerfarmmobile.model.EvalTraitOption
import com.weyr_associates.animaltrakkerfarmmobile.model.UnitOfMeasure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface EvaluationEditor {

    sealed interface Event

    data object EvaluationLoaded : Event

    data object FieldValuesCleared : Event

    data class FieldValueChanged(val field: EvaluationFieldId) : Event

    val events: Flow<Event>

    // Entry Requirements

    val trait01Field: EvaluationField
    val trait02Field: EvaluationField
    val trait03Field: EvaluationField
    val trait04Field: EvaluationField
    val trait05Field: EvaluationField
    val trait06Field: EvaluationField
    val trait07Field: EvaluationField
    val trait08Field: EvaluationField
    val trait09Field: EvaluationField
    val trait10Field: EvaluationField
    val trait11Field: EvaluationField
    val trait12Field: EvaluationField
    val trait13Field: EvaluationField
    val trait14Field: EvaluationField
    val trait15Field: EvaluationField
    val trait16Field: EvaluationField
    val trait17Field: EvaluationField
    val trait18Field: EvaluationField
    val trait19Field: EvaluationField
    val trait20Field: EvaluationField

    val trait11Units: UnitOfMeasure
    val trait12Units: UnitOfMeasure
    val trait13Units: UnitOfMeasure
    val trait14Units: UnitOfMeasure
    val trait15Units: UnitOfMeasure

    val trait16Options: List<EvalTraitOption>
    val trait17Options: List<EvalTraitOption>
    val trait18Options: List<EvalTraitOption>
    val trait19Options: List<EvalTraitOption>
    val trait20Options: List<EvalTraitOption>

    // Scored Traits

    val trait01: StateFlow<Int?>
    val trait02: StateFlow<Int?>
    val trait03: StateFlow<Int?>
    val trait04: StateFlow<Int?>
    val trait05: StateFlow<Int?>
    val trait06: StateFlow<Int?>
    val trait07: StateFlow<Int?>
    val trait08: StateFlow<Int?>
    val trait09: StateFlow<Int?>
    val trait10: StateFlow<Int?>

    fun setTrait01(value: Int?)
    fun setTrait02(value: Int?)
    fun setTrait03(value: Int?)
    fun setTrait04(value: Int?)
    fun setTrait05(value: Int?)
    fun setTrait06(value: Int?)
    fun setTrait07(value: Int?)
    fun setTrait08(value: Int?)
    fun setTrait09(value: Int?)
    fun setTrait10(value: Int?)

    // Units Traits

    val trait11: StateFlow<Float?>
    val trait12: StateFlow<Float?>
    val trait13: StateFlow<Float?>
    val trait14: StateFlow<Float?>
    val trait15: StateFlow<Float?>

    fun setTrait11(value: Float?)
    fun setTrait12(value: Float?)
    fun setTrait13(value: Float?)
    fun setTrait14(value: Float?)
    fun setTrait15(value: Float?)

    // Options Traits

    val trait16: StateFlow<EvalTraitOption?>
    val trait17: StateFlow<EvalTraitOption?>
    val trait18: StateFlow<EvalTraitOption?>
    val trait19: StateFlow<EvalTraitOption?>
    val trait20: StateFlow<EvalTraitOption?>

    fun setTrait16(option: EvalTraitOption?)
    fun setTrait17(option: EvalTraitOption?)
    fun setTrait18(option: EvalTraitOption?)
    fun setTrait19(option: EvalTraitOption?)
    fun setTrait20(option: EvalTraitOption?)
}
