package com.weyr_associates.animaltrakkerfarmmobile.app.repository.database

import android.database.Cursor
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseHandler
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.IdTypeTable
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readAllItems
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readFirstItem
import com.weyr_associates.animaltrakkerfarmmobile.model.IdType
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.IdTypeRepository
import com.weyr_associates.animaltrakkerfarmmobile.database.sql.readIdType
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId

class IdTypeRepositoryImpl(private val databaseHandler: DatabaseHandler) : IdTypeRepository {

    companion object {
        val SQL_QUERY_ID_TYPES get() =
            """SELECT * FROM ${IdTypeTable.NAME}
                WHERE ${IdTypeTable.Columns.ID} != '${IdType.ID_TYPE_ID_NAME_RAW}'
                ORDER BY ${IdTypeTable.Columns.ORDER}"""

        val SQL_QUERY_ID_TYPES_FOR_SEARCH get() =
            """SELECT * FROM ${IdTypeTable.NAME}
                ORDER BY ${IdTypeTable.Columns.ORDER}"""

        val SQL_QUERY_ID_TYPE_BY_ID get() =
            """SELECT * FROM ${IdTypeTable.NAME}
                WHERE ${IdTypeTable.Columns.ID} = ?"""
    }

    override fun queryIdTypes(): List<IdType> {
        return databaseHandler.readableDatabase.rawQuery(SQL_QUERY_ID_TYPES, null)
            .use { it.readAllItems(itemReader = Cursor::readIdType) }
    }

    override fun queryIdTypesForSearch(): List<IdType> {
        return databaseHandler.readableDatabase.rawQuery(SQL_QUERY_ID_TYPES_FOR_SEARCH, null)
            .use { it.readAllItems(itemReader = Cursor::readIdType) }
    }

    override fun queryForIdType(id: EntityId): IdType? {
        return databaseHandler.readableDatabase.rawQuery(SQL_QUERY_ID_TYPE_BY_ID, arrayOf(id.toString()))
            .use { it.readFirstItem(itemReader = Cursor::readIdType) }
    }
}
