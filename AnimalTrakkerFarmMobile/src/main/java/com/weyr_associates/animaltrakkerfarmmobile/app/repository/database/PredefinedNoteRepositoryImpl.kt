package com.weyr_associates.animaltrakkerfarmmobile.app.repository.database

import com.weyr_associates.animaltrakkerfarmmobile.app.repository.PredefinedNoteRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseHandler
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.PredefinedNoteTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.PredefinedNoteTable.Sql.QUERY_PREDEFINED_NOTES_DEFAULTS_ONLY
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.PredefinedNoteTable.Sql.QUERY_PREDEFINED_NOTES_FOR_COMPANY_USER_AND_DEFAULTS
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.PredefinedNoteTable.Sql.QUERY_PREDEFINED_NOTES_FOR_CONTACT_USER_AND_DEFAULTS
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.PredefinedNoteTable.Sql.QUERY_PREDEFINED_NOTE_BY_ID
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readAllItems
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readFirstItem
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.PredefinedNote
import com.weyr_associates.animaltrakkerfarmmobile.model.UserType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PredefinedNoteRepositoryImpl(private val databaseHandler: DatabaseHandler) : PredefinedNoteRepository {

    override suspend fun queryPredefinedNotesDefaultsOnly(): List<PredefinedNote> {
        return withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.rawQuery(
                QUERY_PREDEFINED_NOTES_DEFAULTS_ONLY,
                emptyArray()
            ).use { cursor ->
                cursor.readAllItems(PredefinedNoteTable::predefinedNoteFromCursor)
            }
        }
    }

    override suspend fun queryPredefinedNotes(userId: EntityId, userType: UserType): List<PredefinedNote> {
        return withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.rawQuery(
                queryForPredefinedNotesByUserIdForUserType(userType),
                arrayOf(userId.toString())
            ).use { cursor ->
                cursor.readAllItems(PredefinedNoteTable::predefinedNoteFromCursor)
            }
        }
    }

    override suspend fun queryPredefinedNoteById(id: EntityId): PredefinedNote? {
        return withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.rawQuery(
                QUERY_PREDEFINED_NOTE_BY_ID,
                arrayOf(id.toString())
            ).use { cursor ->
                cursor.readFirstItem(PredefinedNoteTable::predefinedNoteFromCursor)
            }
        }
    }

    private fun queryForPredefinedNotesByUserIdForUserType(userType: UserType): String {
        return when (userType) {
            UserType.CONTACT -> QUERY_PREDEFINED_NOTES_FOR_CONTACT_USER_AND_DEFAULTS
            UserType.COMPANY -> QUERY_PREDEFINED_NOTES_FOR_COMPANY_USER_AND_DEFAULTS
        }
    }
}
