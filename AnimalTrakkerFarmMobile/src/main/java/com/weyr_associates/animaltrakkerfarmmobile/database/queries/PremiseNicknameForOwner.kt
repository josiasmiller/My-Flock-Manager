package com.weyr_associates.animaltrakkerfarmmobile.database.queries

import android.database.sqlite.SQLiteDatabase
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getString
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.PremiseNicknameTable
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.Owner

fun SQLiteDatabase.queryPremiseNicknameForOwner(
    premiseId: EntityId,
    ownerId: EntityId,
    ownerType: Owner.Type
): String? {
    return rawQuery(
        when (ownerType) {
            Owner.Type.CONTACT -> QUERY_PREMISE_NICKNAME_FOR_CONTACT
            Owner.Type.COMPANY -> QUERY_PREMISE_NICKNAME_FOR_COMPANY
        },
        arrayOf(premiseId.toString(), ownerId.toString())
    ).use { cursor ->
        cursor.takeIf { it.moveToFirst() }
            ?.getString(PremiseNicknameTable.Columns.NICKNAME)
    }
}

private val QUERY_PREMISE_NICKNAME_FOR_CONTACT get() =
    """SELECT *
        |FROM ${PremiseNicknameTable.NAME}
        |WHERE ${PremiseNicknameTable.Columns.PREMISE_ID} = ?1
        |AND ${PremiseNicknameTable.Columns.CONTACT_ID} = ?2""".trimMargin()

private val QUERY_PREMISE_NICKNAME_FOR_COMPANY get() =
    """SELECT *
        |FROM ${PremiseNicknameTable.NAME}
        |WHERE ${PremiseNicknameTable.Columns.PREMISE_ID} = ?1
        |AND ${PremiseNicknameTable.Columns.COMPANY_ID} = ?2""".trimMargin()
