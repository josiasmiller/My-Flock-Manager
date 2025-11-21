package com.weyr_associates.animaltrakkerfarmmobile.database.queries

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readAllItems
import com.weyr_associates.animaltrakkerfarmmobile.database.sql.fromForSpeciesModel
import com.weyr_associates.animaltrakkerfarmmobile.database.sql.projectionForSpeciesModel
import com.weyr_associates.animaltrakkerfarmmobile.database.sql.readSpecies
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.SpeciesTable
import com.weyr_associates.animaltrakkerfarmmobile.model.Species

fun SQLiteDatabase.queryAllSpecies(): List<Species> {
    return rawQuery(QUERY_ALL_SPECIES, emptyArray()).use { cursor ->
        cursor.readAllItems(Cursor::readSpecies)
    }
}

private val QUERY_ALL_SPECIES = """
    |SELECT
    |${projectionForSpeciesModel()}
    |${fromForSpeciesModel()}
    |ORDER BY ${SpeciesTable.Columns.COMMON_NAME}
""".trimMargin()
