package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.GeneticHornTypeTable.Columns
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec

object GeneticHornTypeTable : TableSpec<Columns> {

    const val NAME = "genetic_horn_type_table"

    object Columns {
        val ID = Column.NotNull("id_genetichorntypeid")
        val REGISTRY_COMPANY_ID = Column.NotNull("id_registry_id_companyid")
        val HORN_TYPE = Column.NotNull("horn_type")
        val HORN_TYPE_ABBREVIATION = Column.NotNull("horn_type_abbrev")
        val ORDER = Column.NotNull("horn_type_display_order")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID
}
