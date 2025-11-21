package com.weyr_associates.animaltrakkerfarmmobile.database.sql

import android.database.Cursor
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Sql
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getInt
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getOptLocalDate
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getOptLocalTime
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getOptString
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getString
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.AnimalRegistrationTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.AnimalTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.FlockPrefixTable
import com.weyr_associates.animaltrakkerfarmmobile.model.BreedingSummary

fun Cursor.readBreedingSummaryTotalForSex(colQualifier: String? = null): BreedingSummary.Total {
    return BreedingSummary.TotalBySex(
        sex = readSex(colQualifier),
        value = getInt(Sql.Columns.COUNT.qualifiedBy(colQualifier))
    )
}

fun Cursor.readBreedingSummaryOffspring(colQualifier: String? = null): BreedingSummary.Offspring {
    return BreedingSummary.Offspring(
        animalId = getEntityId(AnimalTable.Columns.ID.qualifiedBy(colQualifier)),
        name = getString(AnimalTable.Columns.NAME.qualifiedBy(colQualifier)),
        sex = readSex(colQualifier),
        birthDate = getOptLocalDate(AnimalTable.Columns.BIRTH_DATE.qualifiedBy(colQualifier)),
        birthTime = getOptLocalTime(AnimalTable.Columns.BIRTH_TIME.qualifiedBy(colQualifier)),
        registrationNumber = getOptString(AnimalRegistrationTable.Columns.REGISTRATION_NUMBER.qualifiedBy(colQualifier)),
        flockPrefix = getOptString(FlockPrefixTable.Columns.PREFIX.qualifiedBy(colQualifier))
    )
}
