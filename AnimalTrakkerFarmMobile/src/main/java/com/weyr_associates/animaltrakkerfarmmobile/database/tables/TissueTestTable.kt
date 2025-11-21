package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import android.database.Cursor
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.TissueTestTable.Columns
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getInt
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getString
import com.weyr_associates.animaltrakkerfarmmobile.model.TissueTest

object TissueTestTable : TableSpec<Columns> {

    const val NAME = "tissue_test_table"

    fun tissueTestFrom(cursor: Cursor): TissueTest {
        return TissueTest(
            id = cursor.getEntityId(Columns.ID),
            name = cursor.getString(Columns.NAME),
            abbreviation = cursor.getString(Columns.ABBREVIATION),
            order = cursor.getInt(Columns.ORDER)
        )
    }

    object Columns {
        val ID = Column.NotNull("id_tissuetestid")
        val NAME = Column.NotNull("tissue_test_name")
        val ABBREVIATION = Column.NotNull("tissue_test_abbrev")
        val ORDER = Column.NotNull("tissue_test_display_order")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")
    }

    object Sql {
        val SQL_QUERY_TISSUE_TESTS get() =
            """SELECT * FROM ${NAME}
                ORDER BY ${TissueTestTable.Columns.ORDER}"""

        val SQL_QUERY_TISSUE_TEST_BY_ID get() =
            """SELECT * FROM ${NAME}
                WHERE ${TissueTestTable.Columns.ID} = ?"""
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID
}
