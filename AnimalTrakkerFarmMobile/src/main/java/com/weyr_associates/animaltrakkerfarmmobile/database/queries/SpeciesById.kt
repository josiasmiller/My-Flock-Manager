package com.weyr_associates.animaltrakkerfarmmobile.database.queries

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readFirstItem
import com.weyr_associates.animaltrakkerfarmmobile.database.sql.fromForSpeciesModel
import com.weyr_associates.animaltrakkerfarmmobile.database.sql.projectionForSpeciesModel
import com.weyr_associates.animaltrakkerfarmmobile.database.sql.readSpecies
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.SpeciesTable
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.Species

fun SQLiteDatabase.querySpeciesById(speciesId: EntityId): Species? {
    return rawQuery(QUERY_SPECIES_BY_ID, arrayOf(speciesId.toString())).use { cursor ->
        cursor.readFirstItem(Cursor::readSpecies)
    }
}

private val QUERY_SPECIES_BY_ID = """
    |SELECT
    |${projectionForSpeciesModel()}
    |${fromForSpeciesModel()}
    |WHERE ${SpeciesTable.Columns.ID} = ?1
""".trimMargin()
