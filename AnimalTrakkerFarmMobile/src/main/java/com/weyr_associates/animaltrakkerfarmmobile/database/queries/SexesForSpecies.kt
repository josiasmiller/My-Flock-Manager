package com.weyr_associates.animaltrakkerfarmmobile.database.queries

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readAllItems
import com.weyr_associates.animaltrakkerfarmmobile.database.sql.fromForSexModel
import com.weyr_associates.animaltrakkerfarmmobile.database.sql.projectionForSexModel
import com.weyr_associates.animaltrakkerfarmmobile.database.sql.readSex
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.SpeciesTable
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.Sex

fun SQLiteDatabase.querySexesForSpecies(speciesId: EntityId): List<Sex> {
    return rawQuery(QUERY_SEXES_BY_SPECIES_ID, arrayOf(speciesId.toString())).use { cursor ->
        cursor.readAllItems(Cursor::readSex)
    }
}

private val QUERY_SEXES_BY_SPECIES_ID =
    """SELECT
        |${projectionForSexModel()}
        |${fromForSexModel()}
        |WHERE ${SpeciesTable.NAME}.${SpeciesTable.Columns.ID} = ?1
    """.trimMargin()
