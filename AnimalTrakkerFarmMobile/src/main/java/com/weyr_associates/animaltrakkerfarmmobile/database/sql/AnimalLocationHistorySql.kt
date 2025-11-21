package com.weyr_associates.animaltrakkerfarmmobile.database.sql

import android.database.Cursor
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Qualifier
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Sql
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getLocalDate
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.AnimalLocationHistoryTable
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalMovement

fun Cursor.readAnimalMovement(colQualifier: String? = null): AnimalMovement {
    return AnimalMovement(
        id = getEntityId(AnimalLocationHistoryTable.Columns.ID.qualifiedBy(colQualifier)),
        animalId = getEntityId(AnimalLocationHistoryTable.Columns.ANIMAL_ID.qualifiedBy(colQualifier)),
        toPremise = readOptPremise(Sql.applyPrefix(Qualifier.TO_PREMISE, colQualifier)),
        fromPremise = readOptPremise(Sql.applyPrefix(Qualifier.FROM_PREMISE, colQualifier)),
        movementDate = getLocalDate(AnimalLocationHistoryTable.Columns.MOVEMENT_DATE)
    )
}
