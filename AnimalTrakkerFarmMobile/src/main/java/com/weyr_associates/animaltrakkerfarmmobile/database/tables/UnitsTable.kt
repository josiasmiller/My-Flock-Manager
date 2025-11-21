package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import android.database.Cursor
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.UnitsTable.Columns
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getInt
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getString
import com.weyr_associates.animaltrakkerfarmmobile.model.UnitOfMeasure

object UnitsTable : TableSpec<Columns> {

    const val NAME = "units_table"

    fun unitOfMeasureFromCursor(cursor: Cursor): UnitOfMeasure {
        return UnitOfMeasure(
            id = cursor.getEntityId(Columns.ID),
            name = cursor.getString(Columns.NAME),
            abbreviation = cursor.getString(Columns.ABBREVIATION),
            type = UnitOfMeasure.Type(
                id = cursor.getEntityId(UnitsTypeTable.Columns.ID),
                name = cursor.getString(UnitsTypeTable.Columns.NAME)
            ),
            order = cursor.getInt(Columns.ORDER)
        )
    }

    object Columns {
        val ID = Column.NotNull("id_unitsid")
        val NAME = Column.NotNull("units_name")
        val ABBREVIATION = Column.NotNull("units_abbrev")
        val TYPE_ID = Column.NotNull("id_unitstypeid")
        val ORDER = Column.NotNull("units_display_order")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")
    }

    object Sql {
        val QUERY_UNITS_OF_MEASURE get() =
            """SELECT * FROM ${NAME}
                JOIN ${UnitsTypeTable.NAME} ON
                    ${UnitsTypeTable.NAME}.${UnitsTypeTable.Columns.ID} =
                    ${NAME}.${Columns.TYPE_ID}
                ORDER BY ${Columns.ORDER}"""

        val QUERY_UNITS_OF_MEASURE_BY_TYPE get() =
            """SELECT * FROM ${NAME}
                JOIN ${UnitsTypeTable.NAME} ON
                    ${UnitsTypeTable.NAME}.${UnitsTypeTable.Columns.ID} =
                    ${NAME}.${Columns.TYPE_ID}
                WHERE ${NAME}.${Columns.TYPE_ID} = ?
                ORDER BY ${Columns.ORDER}"""

        val QUERY_UNIT_OF_MEASURE_BY_ID get() =
            """SELECT * FROM ${NAME}
                JOIN ${UnitsTypeTable.NAME} ON
                    ${UnitsTypeTable.NAME}.${UnitsTypeTable.Columns.ID} =
                    ${NAME}.${Columns.TYPE_ID}
                WHERE ${Columns.ID} = ?"""
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID
}
