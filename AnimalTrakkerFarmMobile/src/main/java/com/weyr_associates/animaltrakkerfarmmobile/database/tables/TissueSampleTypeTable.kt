package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import android.database.Cursor
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.TissueSampleTypeTable.Columns
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getInt
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getString
import com.weyr_associates.animaltrakkerfarmmobile.model.TissueSampleType

object TissueSampleTypeTable : TableSpec<Columns> {

    const val NAME = "tissue_sample_type_table"

    fun tissueSampleTypeFrom(cursor: Cursor): TissueSampleType {
        return TissueSampleType(
            id = cursor.getEntityId(Columns.ID),
            name = cursor.getString(Columns.NAME),
            abbreviation = cursor.getString(Columns.ABBREVIATION),
            order = cursor.getInt(Columns.ORDER)
        )
    }

    object Columns {
        val ID = Column.NotNull("id_tissuesampletypeid")
        val NAME = Column.NotNull("tissue_sample_type_name")
        val ABBREVIATION = Column.NotNull("tissue_sample_type_abbrev")
        val ORDER = Column.NotNull("tissue_sample_type_display_order")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")
    }

    object Sql {
        val SQL_QUERY_TISSUE_SAMPLE_TYPES get() =
            """SELECT * FROM ${NAME}
                ORDER BY ${TissueSampleTypeTable.Columns.ORDER}"""

        val SQL_QUERY_TISSUE_SAMPLE_TYPE_BY_ID get() =
            """SELECT * FROM ${NAME}
                WHERE ${TissueSampleTypeTable.Columns.ID} = ?"""
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID
}
