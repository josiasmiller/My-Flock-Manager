package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.UnitsTypeTable.Columns

object UnitsTypeTable : TableSpec<Columns> {

    const val NAME = "units_type_table"

    object Columns {
        val ID = Column.NotNull("id_unitstypeid")
        val NAME = Column.NotNull("unit_type_name")
        val ORDER = Column.NotNull("units_type_display_order")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID
}
