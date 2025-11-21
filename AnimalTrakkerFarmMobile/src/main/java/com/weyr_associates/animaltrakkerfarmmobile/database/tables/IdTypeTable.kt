package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.IdTypeTable.Columns

object IdTypeTable : TableSpec<Columns> {

    const val NAME = "id_type_table"

    object Columns {
        val ID = Column.NotNull("id_idtypeid")
        val NAME = Column.NotNull("id_type_name")
        val ABBREVIATION = Column.NotNull("id_type_abbrev")
        val ORDER = Column.NotNull("id_type_display_order")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID
}
