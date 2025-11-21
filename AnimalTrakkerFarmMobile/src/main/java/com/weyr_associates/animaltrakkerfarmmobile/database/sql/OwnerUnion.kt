package com.weyr_associates.animaltrakkerfarmmobile.database.sql

import android.database.Cursor
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getInt
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getString
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.CompanyTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.ContactTable
import com.weyr_associates.animaltrakkerfarmmobile.model.Owner

object OwnerUnion {

    fun ownerFromCursor(cursor: Cursor): Owner {
        return Owner(
            id = cursor.getEntityId(Columns.ID),
            type = Owner.Type.fromCode(cursor.getInt(Columns.TYPE)),
            name = cursor.getString(Columns.NAME),
            order = cursor.getInt(Columns.ORDER)
        )
    }

    object Sql {
        val SQL_QUERY_ALL_OWNERS get() =
            """SELECT ${Columns.ID},
                ${Columns.TYPE},
                ${Columns.NAME},
                ROW_NUMBER() OVER (
                    ORDER BY ${Columns.TYPE} DESC,
                        company_display_order ASC,
                        contact_display_order ASC
                ) AS ${Columns.ORDER}
                FROM (
                    SELECT ${CompanyTable.Columns.ID} AS ${Columns.ID},
                        CAST(1 AS INTEGER) AS ${Columns.TYPE},
                        ${CompanyTable.Columns.NAME} AS ${Columns.NAME},
                        ROW_NUMBER() OVER (
                            ORDER BY ${CompanyTable.Columns.NAME} ASC
                        ) AS company_display_order,
                        NULL as contact_display_order
                    FROM company_table
                    UNION
                    SELECT ${ContactTable.Columns.ID} AS ${Columns.ID},
                        CAST(0 AS INTEGER) AS ${Columns.TYPE},
                        ${ContactTable.Columns.FIRST_NAME} ||
                        ' ' ||
                        ${ContactTable.Columns.LAST_NAME} AS ${Columns.NAME},
                        NULL AS company_display_order,
                        ROW_NUMBER() OVER (
                            ORDER BY ${ContactTable.Columns.LAST_NAME} ASC,
                                ${ContactTable.Columns.FIRST_NAME} ASC
                        ) AS contact_display_order
                    FROM ${ContactTable.NAME}
                )"""

        val SQL_QUERY_OWNER_BY_ID_AND_TYPE get() =
                    """SELECT * FROM ($SQL_QUERY_ALL_OWNERS)
                        WHERE ${Columns.ID} = ? AND ${Columns.TYPE} = ?"""
    }

    object Columns {
        val ID = Column.NotNull("id_ownerid")
        val TYPE = Column.NotNull("owner_type")
        val NAME = Column.NotNull("owner_name")
        val ORDER = Column.NotNull("owner_display_order")
    }
}