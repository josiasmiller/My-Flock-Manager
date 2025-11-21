package com.weyr_associates.animaltrakkerfarmmobile.app.repository.database

import android.database.Cursor
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseHandler
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readAllItems
import com.weyr_associates.animaltrakkerfarmmobile.database.sql.readIdRemoveReason
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.IdRemoveReasonTable
import com.weyr_associates.animaltrakkerfarmmobile.model.IdRemoveReason
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class IdRemoveReasonRepositoryImpl(private val databaseHandler: DatabaseHandler) :
    com.weyr_associates.animaltrakkerfarmmobile.app.repository.IdRemoveReasonRepository {
    override suspend fun queryIdRemoveReasons(): List<IdRemoveReason> {
        return withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.rawQuery(
                IdRemoveReasonTable.Sql.QUERY_ALL_ID_REMOVE_REASONS, emptyArray()
            ).use { cursor ->
                cursor.readAllItems(Cursor::readIdRemoveReason)
            }
        }
    }
}
