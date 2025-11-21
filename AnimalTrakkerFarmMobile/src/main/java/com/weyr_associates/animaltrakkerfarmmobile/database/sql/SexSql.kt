package com.weyr_associates.animaltrakkerfarmmobile.database.sql

import android.database.Cursor
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.core.JoinType
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getInt
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getString
import com.weyr_associates.animaltrakkerfarmmobile.database.core.isNotNull
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.SexTable
import com.weyr_associates.animaltrakkerfarmmobile.model.Sex

private val SEX_MODEL_COLUMNS: SexTable.Columns.() -> Array<Column> = {
    arrayOf(
        ID,
        NAME,
        ABBREVIATION,
        STANDARD,
        STANDARD_ABBREVIATION,
        ORDER
    )
}

fun projectionForSexModel(
    tableQualifier: String? = null,
    columnQualifier: String? = null
): String {
    return """
        |${SexTable.project(tableQualifier, columnQualifier, SEX_MODEL_COLUMNS)},
        |${projectionForSpeciesModel(tableQualifier, columnQualifier)}
    """.trimMargin()
}

fun projectionForSexModelIn(
    resultsIdentifier: String,
    columnQualifier: String? = null
): String {
    return """
        |${SexTable.projectIn(resultsIdentifier, columnQualifier, SEX_MODEL_COLUMNS)},
        |${projectionForSpeciesModelIn(resultsIdentifier, columnQualifier)}
    """.trimMargin()
}

fun fromForSexModel(): String {
    return """
        |FROM ${SexTable.NAME}
        |${joinForSexModelSubtypes()}
    """.trimMargin()
}

fun joinForSexModel(
    joinType: JoinType,
    foreignKeyTableName: String,
    foreignKeyColumn: Column,
    modelQualifier: String? = null
): String {
    return """
        |${SexTable.join(joinType, foreignKeyTableName, foreignKeyColumn, modelQualifier)}
        |${joinForSexModelSubtypes(isOptional = joinType == JoinType.OUTER_LEFT, modelQualifier)}
    """.trimMargin()
}

fun joinForSexModelSubtypes(
    isOptional: Boolean = false,
    modelQualifier: String? = null
): String {
    val modelTableAliasName = SexTable.prefixWith(modelQualifier)
    val submodelJoinType = if (isOptional) JoinType.OUTER_LEFT else JoinType.INNER
    return """
        |${joinForSpeciesModel(submodelJoinType, modelTableAliasName, SexTable.Columns.SPECIES_ID, modelQualifier)}
        """.trimIndent()
}

fun Cursor.readSex(colQualifier: String? = null): Sex {
    return Sex(
        id = getEntityId(SexTable.Columns.ID.qualifiedBy(colQualifier)),
        name = getString(SexTable.Columns.NAME.qualifiedBy(colQualifier)),
        abbreviation = getString(SexTable.Columns.ABBREVIATION.qualifiedBy(colQualifier)),
        standard = getString(SexTable.Columns.STANDARD.qualifiedBy(colQualifier)),
        standardAbbreviation = getString(SexTable.Columns.STANDARD_ABBREVIATION.qualifiedBy(colQualifier)),
        order = getInt(SexTable.Columns.ORDER.qualifiedBy(colQualifier)),
        species = readSpecies(colQualifier)
    )
}

fun Cursor.readOptSex(colQualifier: String? = null): Sex? {
    return takeIf { it.isNotNull(SexTable.Columns.ID) }
        ?.readSex(colQualifier)
}
