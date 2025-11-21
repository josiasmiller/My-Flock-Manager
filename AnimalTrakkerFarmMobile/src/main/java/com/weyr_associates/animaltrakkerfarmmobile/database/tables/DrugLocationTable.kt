package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import android.database.Cursor
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.DrugLocationTable.Columns
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getInt
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getString
import com.weyr_associates.animaltrakkerfarmmobile.model.DrugLocation

object DrugLocationTable : TableSpec<Columns> {

    const val NAME = "drug_location_table"

    fun drugLocationFromCursor(cursor: Cursor): DrugLocation {
        return DrugLocation(
            id = cursor.getEntityId(Columns.ID),
            name = cursor.getString(Columns.NAME),
            abbreviation = cursor.getString(Columns.ABBREVIATION),
            order = cursor.getInt(Columns.ORDER)
        )
    }

    object Columns {
        val ID = Column.NotNull("id_druglocationid")
        val NAME = Column.NotNull("drug_location_name")
        val ABBREVIATION = Column.NotNull("drug_location_abbrev")
        val ORDER = Column.NotNull("drug_location_display_order")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")
    }

    object Sql {
        const val QUERY_ALL_DRUG_LOCATIONS =
            """SELECT * FROM ${NAME}"""
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID
}
