package com.weyr_associates.animaltrakkerfarmmobile.database.queries

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readFirstItem
import com.weyr_associates.animaltrakkerfarmmobile.database.sql.fromForSexModel
import com.weyr_associates.animaltrakkerfarmmobile.database.sql.projectionForSexModel
import com.weyr_associates.animaltrakkerfarmmobile.database.sql.readSex
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.SexTable
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.Sex

fun SQLiteDatabase.querySexById(sexId: EntityId): Sex? {
    return rawQuery(QUERY_SEX_BY_ID, arrayOf(sexId.toString())).use { cursor ->
        cursor.readFirstItem(Cursor::readSex)
    }
}

private val QUERY_SEX_BY_ID =
    """SELECT
        |${projectionForSexModel()}
        |${fromForSexModel()}
        |WHERE ${SexTable.NAME}.${SexTable.Columns.ID} = ?1
    """.trimMargin()
