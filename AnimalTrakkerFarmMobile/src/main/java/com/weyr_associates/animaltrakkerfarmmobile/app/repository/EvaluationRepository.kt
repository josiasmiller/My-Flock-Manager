package com.weyr_associates.animaltrakkerfarmmobile.app.repository

import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.EvalTraitOption
import com.weyr_associates.animaltrakkerfarmmobile.model.EvaluationConfiguration
import com.weyr_associates.animaltrakkerfarmmobile.model.ItemEntry
import com.weyr_associates.animaltrakkerfarmmobile.model.SavedEvaluation
import com.weyr_associates.animaltrakkerfarmmobile.model.Trait
import com.weyr_associates.animaltrakkerfarmmobile.model.UserType
import java.time.LocalDateTime

interface EvaluationRepository {
    suspend fun queryTraitsByType(typeId: EntityId): List<Trait>
    suspend fun querySavedEvaluationsForUser(userId: EntityId, userType: UserType): List<ItemEntry>
    suspend fun querySavedEvaluationById(id: EntityId): SavedEvaluation?
    suspend fun queryEvalTraitOptionsForTrait(traitId: EntityId): List<EvalTraitOption>
    suspend fun queryEvalTraitOptionById(evalTraitOptionId: EntityId): EvalTraitOption?
    suspend fun saveEvaluationConfigurationForUser(
        userId: EntityId,
        userType: UserType,
        configuration: EvaluationConfiguration,
        timeStamp: LocalDateTime
    )
}
