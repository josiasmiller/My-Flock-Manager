package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.CompanyTable.Columns
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec

object CompanyTable : TableSpec<Columns> {

    const val NAME = "company_table"

    object Columns {
        val ID = Column.NotNull("id_companyid")
        val NAME = Column.NotNull("company")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID
}
