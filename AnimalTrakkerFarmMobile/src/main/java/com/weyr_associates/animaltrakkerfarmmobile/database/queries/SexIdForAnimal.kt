package com.weyr_associates.animaltrakkerfarmmobile.database.queries

import android.database.sqlite.SQLiteDatabase
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readFirstItem
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.AnimalTable
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId

fun SQLiteDatabase.querySexIdForAnimal(animalId: EntityId): EntityId? {
    return rawQuery(QUERY_SEX_ID_FOR_ANIMAL, arrayOf(animalId.toString())).use { cursor ->
        cursor.readFirstItem { it.getEntityId(AnimalTable.Columns.SEX_ID) }
    }
}

private val QUERY_SEX_ID_FOR_ANIMAL get() = """
    |SELECT
    |${AnimalTable.Columns.SEX_ID}
    |FROM ${AnimalTable.NAME}
    |WHERE ${AnimalTable.Columns.ID} = ?1
    |LIMIT 1
""".trimMargin()
