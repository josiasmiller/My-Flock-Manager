package com.weyr_associates.animaltrakkerfarmmobile.database.sql

import android.database.Cursor
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.core.JoinType
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getInt
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getString
import com.weyr_associates.animaltrakkerfarmmobile.database.core.isNotNull
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.PremiseJurisdictionTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.PremiseJurisdictionTable.Columns
import com.weyr_associates.animaltrakkerfarmmobile.model.PremiseJurisdiction

fun projectionForPremiseJurisdictionModel(
    tableQualifier: String? = null,
    columnQualifier: String? = null
): String {
    return PremiseJurisdictionTable.project(tableQualifier, columnQualifier) {
        arrayOf(ID, NAME, ORDER)
    }
}

fun joinForPremiseJurisdictionModel(
    joinType: JoinType,
    foreignKeyTableName: String,
    foreignKeyColumn: Column,
    premiseJurisdictionQualifier: String? = null
): String {
    return PremiseJurisdictionTable.join(
        joinType = joinType,
        tableAliasPrefix = premiseJurisdictionQualifier,
        foreignTableName = foreignKeyTableName,
        foreignColumn = foreignKeyColumn
    )
}

fun Cursor.readPremiseJurisdiction(colQualifier: String? = null): PremiseJurisdiction {
    return PremiseJurisdiction(
        id = getEntityId(Columns.ID.qualifiedBy(colQualifier)),
        name = getString(Columns.NAME.qualifiedBy(colQualifier)),
        order = getInt(Columns.ORDER.qualifiedBy(colQualifier))
    )
}

fun Cursor.readOptPremiseJurisdiction(colQualifier: String? = null): PremiseJurisdiction? {
    return takeIf { it.isNotNull(Columns.ID.qualifiedBy(colQualifier)) }
        ?.readPremiseJurisdiction(colQualifier)
}
