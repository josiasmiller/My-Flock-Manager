package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import android.database.Cursor
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.BirthTypeTable.Columns
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getInt
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getString
import com.weyr_associates.animaltrakkerfarmmobile.model.BirthType
import com.weyr_associates.animaltrakkerfarmmobile.model.RearType

object BirthTypeTable : TableSpec<Columns> {

    const val NAME = "birth_type_table"

    fun birthTypeFromCursor(cursor: Cursor): BirthType {
        return BirthType(
            id = cursor.getEntityId(Columns.ID),
            name = cursor.getString(Columns.NAME),
            abbreviation = cursor.getString(Columns.ABBREVIATION),
            order = cursor.getInt(Columns.ORDER)
        )
    }

    fun rearTypeFromCursor(cursor: Cursor): RearType {
        return RearType(
            id = cursor.getEntityId(Columns.ID),
            name = cursor.getString(Columns.NAME),
            abbreviation = cursor.getString(Columns.ABBREVIATION),
            order = cursor.getInt(Columns.ORDER)
        )
    }

    object Columns {
        val ID = Column.NotNull("id_birthtypeid")
        val NAME = Column.NotNull("birth_type")
        val ABBREVIATION = Column.NotNull("birth_type_abbrev")
        val ORDER = Column.NotNull("birth_type_display_order")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")

        val REAR_TYPE_NAME = Column.NotNull("rear_type_name")
    }

    object Sql {
        val QUERY_ALL_BIRTH_TYPES get() =
            """SELECT * FROM ${NAME}
                ORDER BY ${BirthTypeTable.Columns.ORDER}"""
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID
}
