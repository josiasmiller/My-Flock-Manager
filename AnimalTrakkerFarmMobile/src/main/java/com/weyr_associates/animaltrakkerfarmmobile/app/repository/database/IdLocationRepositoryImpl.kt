package com.weyr_associates.animaltrakkerfarmmobile.app.repository.database

import android.database.Cursor
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseHandler
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.IdLocationTable
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readAllItems
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readFirstItem
import com.weyr_associates.animaltrakkerfarmmobile.model.IdLocation
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.IdLocationRepository
import com.weyr_associates.animaltrakkerfarmmobile.database.sql.readIdLocation
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId

class IdLocationRepositoryImpl(private val databaseHandler: DatabaseHandler) : IdLocationRepository {

    companion object {
        val SQL_QUERY_ID_LOCATIONS get() =
            """SELECT * FROM ${IdLocationTable.NAME}
                ORDER BY ${IdLocationTable.Columns.ORDER}"""

        val SQL_QUERY_ID_LOCATION_BY_ID get() =
            """SELECT * FROM ${IdLocationTable.NAME}
                WHERE ${IdLocationTable.Columns.ID} = ?"""
    }

    override fun queryIdLocations(): List<IdLocation> {
        return databaseHandler.readableDatabase.rawQuery(SQL_QUERY_ID_LOCATIONS, null)
            .use { it.readAllItems(Cursor::readIdLocation) }
    }

    override fun queryIdLocation(id: EntityId): IdLocation? {
        return databaseHandler.readableDatabase.rawQuery(SQL_QUERY_ID_LOCATION_BY_ID, arrayOf(id.toString()))
            .use { it.readFirstItem(Cursor::readIdLocation) }
    }
}
