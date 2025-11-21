package com.weyr_associates.animaltrakkerfarmmobile.app.repository

import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.Owner
import com.weyr_associates.animaltrakkerfarmmobile.model.Premise

interface PremiseRepository {
    suspend fun queryPremiseById(premiseId: EntityId): Premise?
    suspend fun queryPremiseNickname(premiseId: EntityId, ownerId: EntityId, ownerType: Owner.Type): String?
    suspend fun queryPhysicalPremiseForOwner(ownerId: EntityId, ownerType: Owner.Type): Premise?
    suspend fun queryPhysicalPremisesForOwner(ownerId: EntityId, ownerType: Owner.Type): List<Premise>
}
