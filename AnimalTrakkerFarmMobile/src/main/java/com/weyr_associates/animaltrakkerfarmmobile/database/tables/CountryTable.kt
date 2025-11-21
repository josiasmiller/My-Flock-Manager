package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.CountryTable.Columns
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec

object CountryTable : TableSpec<Columns> {

    const val NAME = "country_table"

    object Columns {
        val ID = Column.NotNull("id_countryid")
        val NAME = Column.NotNull("country_name")
        val ABBREVIATION = Column.NotNull("country_abbrev")
        val EID_PREFIX = Column.NotNull("country_eid_prefix")
        val ORDER = Column.NotNull("country_name_display_order")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")
    }

    override val name = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName = Columns.ID
}