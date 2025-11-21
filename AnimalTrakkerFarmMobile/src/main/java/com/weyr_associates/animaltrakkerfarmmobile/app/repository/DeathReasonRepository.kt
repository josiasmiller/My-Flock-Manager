package com.weyr_associates.animaltrakkerfarmmobile.repository

import com.weyr_associates.animaltrakkerfarmmobile.model.DeathReason
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.UserType

interface DeathReasonRepository {
    suspend fun queryDefaultDeathReasons(): List<DeathReason>
    suspend fun queryDeathReasonsByUser(userId: EntityId, userType: UserType): List<DeathReason>
}
