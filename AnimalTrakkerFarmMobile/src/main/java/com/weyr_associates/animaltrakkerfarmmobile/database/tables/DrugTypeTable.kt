package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import android.database.Cursor
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.DrugTypeTable.Columns
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getInt
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getString
import com.weyr_associates.animaltrakkerfarmmobile.model.DrugType

object DrugTypeTable : TableSpec<Columns> {

    const val NAME = "drug_type_table"

    fun drugTypeFromCursor(cursor: Cursor): DrugType {
        return DrugType(
            id = cursor.getEntityId(Columns.ID),
            name = cursor.getString(Columns.NAME),
            order = cursor.getInt(Columns.ORDER)
        )
    }

    object Columns {
        val ID = Column.NotNull("id_drugtypeid")
        val NAME = Column.NotNull("drug_type")
        val ORDER = Column.NotNull("drug_type_display_order")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")
    }

    object Sql {
        val QUERY_ALL_DRUG_TYPES get() =
            """SELECT * FROM ${NAME}
                ORDER BY ${Columns.ORDER}"""
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID
}
