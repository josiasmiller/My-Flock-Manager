package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.CompanyLaboratoryTable.Columns
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec

object CompanyLaboratoryTable : TableSpec<Columns> {

    const val NAME = "company_laboratory_table"

    object Columns {
        val ID = Column.NotNull("id_companylaboratoryid")
        val COMPANY_ID = Column.NotNull("id_companyid")
        val LICENSE_NUMBER = Column.Nullable("lab_license_number")
        val ORDER = Column.NotNull("laboratory_display_order")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID
}
