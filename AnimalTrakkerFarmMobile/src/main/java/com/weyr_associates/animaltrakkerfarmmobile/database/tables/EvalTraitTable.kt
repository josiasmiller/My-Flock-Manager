package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import android.database.Cursor
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.EvalTraitTable.Columns
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getInt
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getOptEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getString
import com.weyr_associates.animaltrakkerfarmmobile.model.Trait

object EvalTraitTable : TableSpec<Columns> {

    const val NAME = "evaluation_trait_table"

    fun traitFromCursor(cursor: Cursor): Trait {
        return Trait(
            id = cursor.getEntityId(Columns.ID),
            name = cursor.getString(Columns.NAME),
            typeId = cursor.getEntityId(Columns.TYPE_ID),
            unitsTypeId = cursor.getOptEntityId(Columns.UNITS_TYPE_ID),
            order = cursor.getInt(Columns.ORDER)
        )
    }

    object Columns {
        val ID = Column.NotNull("id_evaluationtraitid")
        val NAME = Column.NotNull("trait_name")
        val TYPE_ID = Column.NotNull("id_evaluationtraittypeid")
        val UNITS_TYPE_ID = Column.Nullable("id_unitstypeid")
        val ORDER = Column.NotNull("evaluation_trait_display_order")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")
    }

    object Sql {
        val QUERY_TRAITS_BY_TYPE get() =
            """SELECT * FROM ${NAME}
                WHERE ${Columns.TYPE_ID} = ?
                ORDER BY ${Columns.ORDER}"""
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID
}
