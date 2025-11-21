package com.weyr_associates.animaltrakkerfarmmobile.database.queries

import android.database.sqlite.SQLiteDatabase
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getOptLocalDate
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readFirstItem
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.AnimalTable
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import java.time.LocalDate

fun SQLiteDatabase.queryAnimalDeathDate(animalId: EntityId): LocalDate? {
    return rawQuery(QUERY_ANIMAL_DEATH_DATE, arrayOf(animalId.toString()))
        .use { cursor ->
            cursor.readFirstItem {
                it.getOptLocalDate(AnimalTable.Columns.DEATH_DATE)
            }
        }
}

private val QUERY_ANIMAL_DEATH_DATE get() =
    """SELECT ${AnimalTable.Columns.DEATH_DATE}
        |FROM ${AnimalTable.NAME}
        |WHERE ${AnimalTable.Columns.ID} = ?1
        |LIMIT 1""".trimMargin()