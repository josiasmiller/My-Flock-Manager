package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.PremiseTypeTable.Columns

object PremiseTypeTable : TableSpec<Columns> {

    const val NAME = "premise_type_table"

    object Columns {
        val ID = Column.NotNull("id_premisetypeid")
        val NAME = Column.NotNull("premise_type")
        val ORDER = Column.NotNull("premise_display_order")

        fun allColumns(): Array<Column> {
            return arrayOf(ID, NAME, ORDER)
        }
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName = Columns.ID
}