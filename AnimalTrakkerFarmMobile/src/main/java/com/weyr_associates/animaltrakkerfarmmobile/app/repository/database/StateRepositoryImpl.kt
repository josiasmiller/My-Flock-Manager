package com.weyr_associates.animaltrakkerfarmmobile.app.repository.database

import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseHandler
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.StatesTable
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readAllItems
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readFirstItem
import com.weyr_associates.animaltrakkerfarmmobile.model.State

class StateRepositoryImpl(private val databaseHandler: DatabaseHandler) :
    com.weyr_associates.animaltrakkerfarmmobile.app.repository.StateRepository {
    companion object {
        val SQL_QUERY_STATES get() =
            """SELECT * FROM ${StatesTable.NAME}
                ORDER BY ${StatesTable.Columns.ORDER}"""

        val SQL_QUERY_STATE_BY_ID get() =
            """SELECT * FROM ${StatesTable.NAME}
                where ${StatesTable.Columns.ID} = ?"""
    }

    override fun queryStates(): List<State> {
        return databaseHandler.readableDatabase.rawQuery(SQL_QUERY_STATES, null)
            ?.use { it.readAllItems(itemReader = StatesTable::stateFromCursor) } ?: emptyList()
    }

    override fun queryForState(id: Int): State? {
        return databaseHandler.readableDatabase.rawQuery(SQL_QUERY_STATE_BY_ID, arrayOf(id.toString()))
            ?.use { it.readFirstItem(itemReader = StatesTable::stateFromCursor) }
    }
}
