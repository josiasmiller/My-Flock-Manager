package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.SexTable.Columns

object SexTable : TableSpec<Columns> {

    const val NAME = "sex_table"

    object Columns {
        val ID = Column.NotNull("id_sexid")
        val NAME = Column.NotNull("sex_name")
        val ABBREVIATION = Column.NotNull("sex_abbrev")
        val STANDARD = Column.NotNull("sex_standard")
        val STANDARD_ABBREVIATION = Column.NotNull("sex_abbrev_standard")
        val ORDER = Column.NotNull("sex_display_order")
        val SPECIES_ID = Column.NotNull("id_speciesid")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID
}
