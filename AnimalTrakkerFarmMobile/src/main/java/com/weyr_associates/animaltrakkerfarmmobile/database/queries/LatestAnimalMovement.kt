package com.weyr_associates.animaltrakkerfarmmobile.database.queries

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Alias
import com.weyr_associates.animaltrakkerfarmmobile.database.core.JoinType
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Qualifier
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readFirstItem
import com.weyr_associates.animaltrakkerfarmmobile.database.sql.joinForPremiseModel
import com.weyr_associates.animaltrakkerfarmmobile.database.sql.projectionForPremiseModel
import com.weyr_associates.animaltrakkerfarmmobile.database.sql.readAnimalMovement
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.AnimalLocationHistoryTable
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalMovement
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId

fun SQLiteDatabase.queryLatestAnimalMovement(animalId: EntityId): AnimalMovement? {
    return rawQuery(QUERY_LATEST_ANIMAL_MOVEMENT, arrayOf(animalId.toString())).use { cursor ->
        cursor.readFirstItem(Cursor::readAnimalMovement)?.let {
            AnimalMovement(
                id = it.id,
                animalId = it.animalId,
                fromPremise = it.fromPremise,
                toPremise = it.toPremise,
                movementDate = it.movementDate
            )
        }
    }
}

private val QUERY_LATEST_ANIMAL_MOVEMENT =
    """SELECT
        |${AnimalLocationHistoryTable.project { allColumns() }},
        |${projectionForPremiseModel(Alias.TO, Qualifier.TO_PREMISE, includeNickname = false)},
        |${projectionForPremiseModel(Alias.FROM, Qualifier.FROM_PREMISE, includeNickname = false)}
        |FROM ${AnimalLocationHistoryTable.NAME}
        |${joinForPremiseModel(JoinType.OUTER_LEFT, AnimalLocationHistoryTable.NAME, AnimalLocationHistoryTable.Columns.TO_PREMISE_ID, Alias.TO)}
        |${joinForPremiseModel(JoinType.OUTER_LEFT, AnimalLocationHistoryTable.NAME, AnimalLocationHistoryTable.Columns.FROM_PREMISE_ID, Alias.FROM)}
        |WHERE ${AnimalLocationHistoryTable.NAME}.${AnimalLocationHistoryTable.Columns.ANIMAL_ID} = ?1
        |ORDER BY ${AnimalLocationHistoryTable.NAME}.${AnimalLocationHistoryTable.Columns.MOVEMENT_DATE} DESC
        |LIMIT 1""".trimMargin()
