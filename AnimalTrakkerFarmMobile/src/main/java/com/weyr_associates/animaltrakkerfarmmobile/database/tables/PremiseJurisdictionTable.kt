package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.PremiseJurisdictionTable.Columns

object PremiseJurisdictionTable : TableSpec<Columns> {

    const val NAME = "premise_jurisdiction_table"

    object Columns {
        val ID = Column.NotNull("id_premisejurisdictionid")
        val NAME = Column.NotNull("premise_jurisdiction")
        val ORDER = Column.NotNull("premise_jurisdiction_display_order")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID
}
