package com.weyr_associates.animaltrakkerfarmmobile.app.animal.evaluation

import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId

data class EvaluationEntries(
    val trait01Id: EntityId?,
    val trait01Score: Int?,
    val trait02Id: EntityId?,
    val trait02Score: Int?,
    val trait03Id: EntityId?,
    val trait03Score: Int?,
    val trait04Id: EntityId?,
    val trait04Score: Int?,
    val trait05Id: EntityId?,
    val trait05Score: Int?,
    val trait06Id: EntityId?,
    val trait06Score: Int?,
    val trait07Id: EntityId?,
    val trait07Score: Int?,
    val trait08Id: EntityId?,
    val trait08Score: Int?,
    val trait09Id: EntityId?,
    val trait09Score: Int?,
    val trait10Id: EntityId?,
    val trait10Score: Int?,
    val trait11Id: EntityId?,
    val trait11Score: Float?,
    val trait11UnitsId: EntityId?,
    val trait12Id: EntityId?,
    val trait12Score: Float?,
    val trait12UnitsId: EntityId?,
    val trait13Id: EntityId?,
    val trait13Score: Float?,
    val trait13UnitsId: EntityId?,
    val trait14Id: EntityId?,
    val trait14Score: Float?,
    val trait14UnitsId: EntityId?,
    val trait15Id: EntityId?,
    val trait15Score: Float?,
    val trait15UnitsId: EntityId?,
    val trait16Id: EntityId?,
    val trait16OptionId: EntityId?,
    val trait17Id: EntityId?,
    val trait17OptionId: EntityId?,
    val trait18Id: EntityId?,
    val trait18OptionId: EntityId?,
    val trait19Id: EntityId?,
    val trait19OptionId: EntityId?,
    val trait20Id: EntityId?,
    val trait20OptionId: EntityId?
) {
    fun extractScoreForTraitId(traitId: EntityId): Int? = when (traitId) {
        trait01Id -> trait01Score
        trait02Id -> trait02Score
        trait03Id -> trait03Score
        trait04Id -> trait04Score
        trait05Id -> trait05Score
        trait06Id -> trait06Score
        trait07Id -> trait07Score
        trait08Id -> trait08Score
        trait09Id -> trait09Score
        trait10Id -> trait10Score
        else -> null
    }

    fun extractScoreForUnitTraitId(unitsTraitId: EntityId): Float? = when (unitsTraitId) {
        trait11Id -> trait11Score
        trait12Id -> trait12Score
        trait13Id -> trait13Score
        trait14Id -> trait14Score
        trait15Id -> trait15Score
        else -> null
    }

    fun extractOptionIdForOptionTraitId(optionTraitId: EntityId): EntityId? = when (optionTraitId) {
        trait16Id -> trait16OptionId
        trait17Id -> trait17OptionId
        trait18Id -> trait18OptionId
        trait19Id -> trait19OptionId
        trait20Id -> trait20OptionId
        else -> null
    }
}
