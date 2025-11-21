package com.weyr_associates.animaltrakkerfarmmobile.database.sql

import android.database.Cursor
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.core.JoinType
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getInt
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getOptFloat
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getOptString
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getString
import com.weyr_associates.animaltrakkerfarmmobile.database.core.isNotNull
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.CountryTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.PremiseNicknameTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.PremiseTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.PremiseTypeTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.StatesTable
import com.weyr_associates.animaltrakkerfarmmobile.model.Premise

fun projectionForPremiseModel(
    tableQualifier: String? = null,
    columnQualifier: String? = null,
    includeNickname: Boolean = false
): String {
    val nicknameProjection = if (includeNickname) {
        PremiseNicknameTable.project(tableQualifier, columnQualifier) { arrayOf(NICKNAME) }
    } else {
        "NULL AS ${PremiseNicknameTable.Columns.NICKNAME.qualifiedBy(columnQualifier)}"
    }
    return """
        |${PremiseTable.project(tableQualifier, columnQualifier) { allColumns() }},
        |${PremiseTypeTable.project(tableQualifier, columnQualifier) { allColumns() }},
        |${StatesTable.project(tableQualifier, columnQualifier) { arrayOf(NAME) }},
        |${CountryTable.project(tableQualifier, columnQualifier) { arrayOf(NAME) }},
        |${projectionForPremiseJurisdictionModel(tableQualifier, columnQualifier)},
        |${nicknameProjection}""".trimMargin()
}

fun fromForPremiseModel(): String {
    return """
        |FROM ${PremiseTable.NAME}
        |${joinForPremiseModelSubtypes()}
    """.trimMargin()
}

fun joinForPremiseModel(
    joinType: JoinType,
    foreignKeyTableName: String,
    foreignKeyColumn: Column,
    premiseQualifier: String? = null
): String {
    return """
        |${PremiseTable.join(joinType, foreignKeyTableName, foreignKeyColumn, premiseQualifier)}
        |${joinForPremiseModelSubtypes(isOptional = joinType == JoinType.OUTER_LEFT, premiseQualifier)}
    """.trimMargin()
}

fun joinForPremiseModelSubtypes(isOptional: Boolean = false, premiseQualifier: String? = null): String {
    val premiseTableAliasName = PremiseTable.prefixWith(premiseQualifier)
    val premiseTypeJoinType = if (isOptional) JoinType.OUTER_LEFT else JoinType.INNER
    return """
        |${PremiseTypeTable.join(premiseTypeJoinType, premiseTableAliasName, PremiseTable.Columns.TYPE_ID, premiseQualifier)}
        |${StatesTable.join(JoinType.OUTER_LEFT, premiseTableAliasName, PremiseTable.Columns.STATE_ID, premiseQualifier)}
        |${CountryTable.join(JoinType.OUTER_LEFT, premiseTableAliasName, PremiseTable.Columns.COUNTRY_ID, premiseQualifier)}
        |${joinForPremiseJurisdictionModel(JoinType.OUTER_LEFT, premiseTableAliasName, PremiseTable.Columns.JURISDICTION_ID, premiseQualifier)}
    """.trimMargin()
}

fun Cursor.readPremise(colQualifier: String? = null): Premise {
    val address1 = getOptString(PremiseTable.Columns.ADDRESS1.qualifiedBy(colQualifier))
    val address2 = getOptString(PremiseTable.Columns.ADDRESS2.qualifiedBy(colQualifier))
    val city = getOptString(PremiseTable.Columns.CITY.qualifiedBy(colQualifier))
    val state = getOptString(StatesTable.Columns.NAME.qualifiedBy(colQualifier))
    val postCode = getOptString(PremiseTable.Columns.POSTCODE.qualifiedBy(colQualifier))
    val country = getOptString(CountryTable.Columns.NAME.qualifiedBy(colQualifier))
    val latitude = getOptFloat(PremiseTable.Columns.LATITUDE.qualifiedBy(colQualifier))
    val longitude = getOptFloat(PremiseTable.Columns.LONGITUDE.qualifiedBy(colQualifier))
    return Premise(
        id = getEntityId(PremiseTable.Columns.ID.qualifiedBy(colQualifier)),
        type = readPremiseType(colQualifier),
        number = getOptString(PremiseTable.Columns.PREMISE_NUMBER.qualifiedBy(colQualifier)),
        nickname = getOptString(PremiseNicknameTable.Columns.NICKNAME.qualifiedBy(colQualifier)),
        address = Premise.Address.from(
            address1,
            address2,
            city,
            state,
            postCode,
            country
        ),
        geoLocation = Premise.GeoLocation.from(latitude, longitude),
        jurisdiction = readOptPremiseJurisdiction(colQualifier)
    )
}

fun Cursor.readOptPremise(colQualifier: String? = null): Premise? {
    return takeIf { it.isNotNull(PremiseTable.Columns.ID.qualifiedBy(colQualifier)) }
        ?.readPremise(colQualifier)
}

fun Cursor.readPremiseType(colQualifier: String? = null): Premise.Type {
    return Premise.Type(
        id = getEntityId(PremiseTypeTable.Columns.ID.qualifiedBy(colQualifier)),
        name = getString(PremiseTypeTable.Columns.NAME.qualifiedBy(colQualifier)),
        order = getInt(PremiseTypeTable.Columns.ORDER.qualifiedBy(colQualifier))
    )
}

fun Cursor.readOptPremiseType(colQualifier: String? = null): Premise.Type? {
    return takeIf { it.isNotNull(PremiseTypeTable.Columns.ID.qualifiedBy(colQualifier)) }
        ?.readPremiseType(colQualifier)
}
