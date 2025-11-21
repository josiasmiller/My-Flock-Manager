package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.IdLocationTable.Columns

object IdLocationTable : TableSpec<Columns> {

    const val NAME = "id_location_table"

    object Columns {
        val ID = Column.NotNull("id_idlocationid")
        val NAME = Column.NotNull("id_location_name")
        val ABBREVIATION = Column.NotNull("id_location_abbrev")
        val ORDER = Column.NotNull("id_location_display_order")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID
}
