package com.weyr_associates.animaltrakkerfarmmobile.app.repository.database

import android.database.Cursor
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.IdColorRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseHandler
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readAllItems
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readFirstItem
import com.weyr_associates.animaltrakkerfarmmobile.database.sql.readIdColor
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.IdColorTable
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.IdColor

class IdColorRepositoryImpl(private val databaseHandler: DatabaseHandler) : IdColorRepository {

    companion object {
        private val SQL_QUERY_ID_COLORS =
            """SELECT * FROM ${IdColorTable.NAME}
                ORDER BY ${IdColorTable.Columns.ORDER}"""

        private val SQL_QUERY_ID_COLOR_BY_ID =
            """SELECT * FROM ${IdColorTable.NAME}
                WHERE ${IdColorTable.Columns.ID} = ?"""
    }

    override fun queryIdColors(): List<IdColor> {
        return databaseHandler.readableDatabase.rawQuery(SQL_QUERY_ID_COLORS, null)
            .use { it.readAllItems(Cursor::readIdColor) }
    }

    override fun queryIdColor(id: EntityId): IdColor? {
        return databaseHandler.readableDatabase.rawQuery(SQL_QUERY_ID_COLOR_BY_ID, arrayOf(id.toString()))
            .use { it.readFirstItem(Cursor::readIdColor) }
    }
}
