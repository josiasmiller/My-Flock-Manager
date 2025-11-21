package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.ContactVeterinarianTable.Columns
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec

object ContactVeterinarianTable : TableSpec<Columns> {

    const val NAME = "contact_veterinarian_table"

    object Columns {
        val ID = Column.NotNull("id_contactveterinarianid")
        val CONTACT_ID = Column.NotNull("id_contactid")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID
}
