package com.weyr_associates.animaltrakkerfarmmobile.app.repository

import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.PredefinedNote
import com.weyr_associates.animaltrakkerfarmmobile.model.UserType

interface PredefinedNoteRepository {
    suspend fun queryPredefinedNotesDefaultsOnly(): List<PredefinedNote>
    suspend fun queryPredefinedNotes(userId: EntityId, userType: UserType): List<PredefinedNote>
    suspend fun queryPredefinedNoteById(id: EntityId): PredefinedNote?
}
