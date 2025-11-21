package com.weyr_associates.animaltrakkerfarmmobile.app.repository

import com.weyr_associates.animaltrakkerfarmmobile.model.IdRemoveReason

interface IdRemoveReasonRepository {
    suspend fun queryIdRemoveReasons(): List<IdRemoveReason>
}
