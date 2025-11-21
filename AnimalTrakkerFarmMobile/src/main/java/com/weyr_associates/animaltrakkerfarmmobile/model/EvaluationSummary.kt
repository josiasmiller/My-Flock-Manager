package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class EvaluationSummary(
    val animalEvaluationId: EntityId,
    val evaluationId: EntityId,
    val evaluationName: String,
    val evaluationTraits: List<Trait>
) : Parcelable {
    @Parcelize
    @Serializable
    data class Trait(
        val id: EntityId,
        val typeId: EntityId,
        val name: String,
        val value: String
    ) : Parcelable
}
