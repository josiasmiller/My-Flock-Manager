package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.GeneticCharacteristicTable.Columns
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec

object GeneticCharacteristicTable : TableSpec<Columns> {

    const val NAME = "genetic_characteristic_table"

    object Columns {
        val ID = Column.NotNull("id_geneticcharacteristicid")
        val TABLE_NAME = Column.NotNull("genetic_characteristic_table_name")
        val TABLE_DISPLAY_NAME = Column.NotNull("genetic_characteristic_table_display_name")
        val TABLE_DISPLAY_ORDER = Column.NotNull("genetic_characteristic_table_display_order")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID
}
