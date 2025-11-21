package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.ServiceTypeTable.Columns

object ServiceTypeTable : TableSpec<Columns>{

    const val NAME = "service_type_table"

    object Columns {
        val ID = Column.NotNull("id_servicetypeid")
        val NAME = Column.NotNull("service_type")
        val ABBREVIATION = Column.NotNull("service_abbrev")
        val ORDER = Column.NotNull("service_type_display_order")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")

        fun allColumns(): Array<Column> {
            return arrayOf(ID, NAME, ABBREVIATION, ORDER)
        }
    }

    object Sql {
        val QUERY_ALL_SERVICE_TYPES get() =
            """SELECT * FROM ${NAME}
                ORDER BY ${Columns.ORDER}"""

        val QUERY_SERVICE_TYPE_BY_ID get() =
            """SELECT * FROM ${NAME}
                WHERE ${Columns.ID} = ?"""
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID
}
