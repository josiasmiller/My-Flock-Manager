package com.weyr_associates.animaltrakkerfarmmobile.database.sql

import android.database.Cursor
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.core.JoinType
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getInt
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getString
import com.weyr_associates.animaltrakkerfarmmobile.database.core.isNotNull
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.SpeciesTable
import com.weyr_associates.animaltrakkerfarmmobile.model.Species

private val SPECIES_MODEL_COLUMNS: SpeciesTable.Columns.() -> Array<Column> = {
    arrayOf(
        ID,
        COMMON_NAME,
        GENERIC_NAME,
        EARLY_FEMALE_BREEDING_AGE_IN_DAYS,
        EARLY_GESTATION_LENGTH_DAYS,
        LATE_GESTATION_LENGTH_DAYS,
        TYPICAL_GESTATION_LENGTH_DAYS
    )
}

fun projectionForSpeciesModel(
    tableQualifier: String? = null,
    columnQualifier: String? = null
): String {
    return SpeciesTable.project(tableQualifier, columnQualifier, SPECIES_MODEL_COLUMNS)
}

fun projectionForSpeciesModelIn(
    resultsIdentifier: String,
    columnQualifier: String? = null
): String {
    return SpeciesTable.projectIn(resultsIdentifier, columnQualifier, SPECIES_MODEL_COLUMNS)
}

fun fromForSpeciesModel(): String {
    return """
        |FROM ${SpeciesTable.NAME}
    """.trimMargin()
}

fun joinForSpeciesModel(
    joinType: JoinType,
    foreignKeyTableName: String,
    foreignKeyColumn: Column,
    modelQualifier: String? = null
): String {
    return """
        |${SpeciesTable.join(joinType, foreignKeyTableName, foreignKeyColumn, modelQualifier)}
    """.trimMargin()
}

fun Cursor.readSpecies(colQualifier: String? = null): Species {
    return Species(
        id = getEntityId(SpeciesTable.Columns.ID.qualifiedBy(colQualifier)),
        commonName = getString(SpeciesTable.Columns.COMMON_NAME.qualifiedBy(colQualifier)),
        genericName = getString(SpeciesTable.Columns.GENERIC_NAME.qualifiedBy(colQualifier)),
        earlyFemaleBreedingAgeDays = getInt(SpeciesTable.Columns.EARLY_FEMALE_BREEDING_AGE_IN_DAYS.qualifiedBy(colQualifier)),
        earlyGestationLengthDays = getInt(SpeciesTable.Columns.EARLY_GESTATION_LENGTH_DAYS.qualifiedBy(colQualifier)),
        lateGestationLengthDays = getInt(SpeciesTable.Columns.LATE_GESTATION_LENGTH_DAYS.qualifiedBy(colQualifier)),
        typicalGestationLengthDays = getInt(SpeciesTable.Columns.TYPICAL_GESTATION_LENGTH_DAYS.qualifiedBy(colQualifier))
    )
}

fun Cursor.readOptSpecies(colQualifier: String? = null): Species? {
    return takeIf { it.isNotNull(SpeciesTable.Columns.ID) }
        ?.readSpecies(colQualifier)
}
