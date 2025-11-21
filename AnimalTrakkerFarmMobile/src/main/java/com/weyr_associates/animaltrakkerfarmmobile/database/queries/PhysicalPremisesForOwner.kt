package com.weyr_associates.animaltrakkerfarmmobile.database.queries

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readAllItems
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readFirstItem
import com.weyr_associates.animaltrakkerfarmmobile.database.sql.fromForPremiseModel
import com.weyr_associates.animaltrakkerfarmmobile.database.sql.projectionForPremiseModel
import com.weyr_associates.animaltrakkerfarmmobile.database.sql.readPremise
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.CompanyPremiseTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.ContactPremiseTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.PremiseNicknameTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.PremiseTable
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.Owner
import com.weyr_associates.animaltrakkerfarmmobile.model.Premise

fun SQLiteDatabase.queryPhysicalPremiseForOwner(ownerId: EntityId, ownerType: Owner.Type): Premise? {
    return rawQuery(
        when (ownerType) {
            Owner.Type.CONTACT -> QUERY_CONTACT_CURRENT_PHYSICAL_PREMISE
            Owner.Type.COMPANY -> QUERY_COMPANY_CURRENT_PHYSICAL_PREMISE
        },
        arrayOf(ownerId.toString())
    ).use { cursor ->
        cursor.readFirstItem(Cursor::readPremise)
    }
}

fun SQLiteDatabase.queryPhysicalPremisesForOwner(ownerId: EntityId, ownerType: Owner.Type): List<Premise> {
    return rawQuery(
        when (ownerType) {
            Owner.Type.CONTACT -> QUERY_CONTACT_PHYSICAL_PREMISES
            Owner.Type.COMPANY -> QUERY_COMPANY_PHYSICAL_PREMISES
        },
        arrayOf(ownerId.toString())
    ).use { cursor -> cursor.readAllItems(Cursor::readPremise) }
}

private val QUERY_CONTACT_CURRENT_PHYSICAL_PREMISE get() =
    """$QUERY_CONTACT_PHYSICAL_PREMISES
        |LIMIT 1""".trimMargin()

private val QUERY_CONTACT_PHYSICAL_PREMISES get() =
    """SELECT
        |${projectionForPremiseModel(includeNickname = true)}
        |${fromForPremiseModel()}
        |INNER JOIN ${ContactPremiseTable.NAME}
        |    ON ${ContactPremiseTable.NAME}.${ContactPremiseTable.Columns.PREMISE_ID} =
        |        ${PremiseTable.NAME}.${PremiseTable.Columns.ID}
        |LEFT OUTER JOIN ${PremiseNicknameTable.NAME}
        |    ON ${PremiseTable.NAME}.${PremiseTable.Columns.ID} =
        |        ${PremiseNicknameTable.NAME}.${PremiseNicknameTable.Columns.PREMISE_ID} AND
        |        ${PremiseNicknameTable.NAME}.${PremiseNicknameTable.Columns.CONTACT_ID} = ?1
        |WHERE ${ContactPremiseTable.NAME}.${ContactPremiseTable.Columns.CONTACT_ID} = ?1
        |AND (${PremiseTable.NAME}.${PremiseTable.Columns.TYPE_ID} = '${Premise.Type.ID_PHYSICAL_RAW}' OR
        |    ${PremiseTable.NAME}.${PremiseTable.Columns.TYPE_ID} = '${Premise.Type.ID_BOTH_RAW}')
        |AND ${ContactPremiseTable.NAME}.${ContactPremiseTable.Columns.USAGE_END} IS NULL
        |ORDER BY ${ContactPremiseTable.NAME}.${ContactPremiseTable.Columns.USAGE_START} DESC""".trimMargin()

private val QUERY_COMPANY_CURRENT_PHYSICAL_PREMISE get() =
    """$QUERY_COMPANY_PHYSICAL_PREMISES
        |LIMIT 1""".trimMargin()

private val QUERY_COMPANY_PHYSICAL_PREMISES get() =
    """SELECT
        |${projectionForPremiseModel(includeNickname = true)}
        |${fromForPremiseModel()}
        |INNER JOIN ${CompanyPremiseTable.NAME}
        |    ON ${CompanyPremiseTable.NAME}.${CompanyPremiseTable.Columns.PREMISE_ID} =
        |        ${PremiseTable.NAME}.${PremiseTable.Columns.ID}
        |LEFT OUTER JOIN ${PremiseNicknameTable.NAME}
        |    ON ${PremiseTable.NAME}.${PremiseTable.Columns.ID} =
        |        ${PremiseNicknameTable.NAME}.${PremiseNicknameTable.Columns.PREMISE_ID} AND
        |        ${PremiseNicknameTable.NAME}.${PremiseNicknameTable.Columns.COMPANY_ID} = ?1
        |WHERE ${CompanyPremiseTable.NAME}.${CompanyPremiseTable.Columns.COMPANY_ID} = ?1
        |AND (${PremiseTable.NAME}.${PremiseTable.Columns.TYPE_ID} = '${Premise.Type.ID_PHYSICAL_RAW}' OR
        |    ${PremiseTable.NAME}.${PremiseTable.Columns.TYPE_ID} = '${Premise.Type.ID_BOTH_RAW}')
        |AND ${CompanyPremiseTable.NAME}.${CompanyPremiseTable.Columns.USAGE_END} IS NULL
        |ORDER BY ${CompanyPremiseTable.NAME}.${CompanyPremiseTable.Columns.USAGE_START} DESC""".trimMargin()
