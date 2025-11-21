package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import android.database.Cursor
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.StatesTable.Columns
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getInt
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getString
import com.weyr_associates.animaltrakkerfarmmobile.model.State

object StatesTable : TableSpec<Columns> {

    const val NAME = "state_table"

    fun stateFromCursor(cursor: Cursor): State = with (cursor) {
        State(
            id = getEntityId(Columns.ID),
            name = getString(Columns.NAME),
            abbreviation = getString(Columns.ABBREVIATION),
            countryId = getEntityId(Columns.COUNTRY_ID),
            order = getInt(Columns.ORDER)
        )
    }

    object Columns {
        val ID = Column.NotNull("id_stateid")
        val NAME = Column.NotNull("state_name")
        val ABBREVIATION = Column.NotNull("state_abbrev")
        val COUNTRY_ID = Column.NotNull("id_countryid")
        val ORDER = Column.NotNull("state_display_order")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID
}
