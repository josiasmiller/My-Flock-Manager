package com.weyr_associates.animaltrakkerfarmmobile.database.queries

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readFirstItem
import com.weyr_associates.animaltrakkerfarmmobile.database.sql.fromForPremiseModel
import com.weyr_associates.animaltrakkerfarmmobile.database.sql.projectionForPremiseModel
import com.weyr_associates.animaltrakkerfarmmobile.database.sql.readPremise
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.PremiseTable
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.Premise

fun SQLiteDatabase.queryPremiseById(premiseId: EntityId): Premise? {
    return rawQuery(QUERY_PREMISE_BY_ID, arrayOf(premiseId.toString())).use { cursor ->
        cursor.readFirstItem(Cursor::readPremise)
    }
}

private val QUERY_PREMISE_BY_ID =
    """SELECT
        |${projectionForPremiseModel(includeNickname = false)}
        |${fromForPremiseModel()}
        |WHERE ${PremiseTable.NAME}.${PremiseTable.Columns.ID} = ?1""".trimMargin()
