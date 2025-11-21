package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import android.database.Cursor
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.DrugTable.Columns
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getBoolean
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getString
import com.weyr_associates.animaltrakkerfarmmobile.model.Drug

object DrugTable : TableSpec<Columns> {

    const val NAME = "drug_table"

    fun drugFromCursor(cursor: Cursor): Drug {
        return Drug(
            id = cursor.getEntityId(Columns.ID),
            typeId = cursor.getEntityId(Columns.TYPE_ID),
            tradeName = cursor.getString(Columns.TRADE_NAME),
            genericName = cursor.getString(Columns.GENERIC_NAME),
            isRemovable = cursor.getBoolean(Columns.IS_REMOVABLE),
        )
    }

    object Columns {
        val ID = Column.NotNull("id_drugid")
        val TYPE_ID = Column.NotNull("id_drugtypeid")
        val TRADE_NAME = Column.NotNull("trade_drug_name")
        val GENERIC_NAME = Column.NotNull("generic_drug_name")
        val IS_REMOVABLE = Column.NotNull("drug_removable")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")
    }

    object Sql {

        val QUERY_ALL_DRUGS get() =
            """SELECT * FROM ${NAME}
                ORDER BY LOWER(${DrugTable.Columns.TRADE_NAME})"""

        val QUERY_DRUGS_BY_TYPE get() =
            """SELECT * FROM ${NAME}
                WHERE ${DrugTable.Columns.TYPE_ID} = ?
                ORDER BY LOWER(${DrugTable.Columns.TRADE_NAME})"""

        val QUERY_DRUG_BY_ID get() =
            """SELECT * FROM ${NAME}
                WHERE ${DrugTable.Columns.ID} = ?"""
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID
}
