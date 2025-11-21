package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.PremiseNicknameTable.Columns
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec

object PremiseNicknameTable : TableSpec<Columns> {

    const val NAME = "premise_nickname_table"

    object Columns {
        val ID = Column.NotNull("id_premisenicknameid")
        val PREMISE_ID = Column.NotNull("id_premiseid")
        val NICKNAME = Column.NotNull("premise_nickname")
        val CONTACT_ID = Column.Nullable("id_contactid")
        val COMPANY_ID = Column.Nullable("id_companyid")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")
    }

    override val name = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName = Columns.ID
}
