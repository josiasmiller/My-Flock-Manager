package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.CompanyPremiseTable.Columns
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec

object CompanyPremiseTable : TableSpec<Columns> {

    const val NAME = "company_premise_table"

    object Columns {
        val ID = Column.NotNull("id_companypremiseid")
        val COMPANY_ID = Column.NotNull("id_companyid")
        val PREMISE_ID = Column.NotNull("id_premiseid")
        val USAGE_START = Column.NotNull("start_premise_use")
        val USAGE_END = Column.Nullable("end_premise_use")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID
}
