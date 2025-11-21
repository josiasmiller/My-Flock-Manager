package com.weyr_associates.animaltrakkerfarmmobile.database.sql

import android.database.Cursor
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getBoolean
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getLocalDate
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getLocalTime
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getOptLocalDate
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getOptLocalTime
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getString
import com.weyr_associates.animaltrakkerfarmmobile.database.core.isNotNull
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.AnimalIdInfoTable
import com.weyr_associates.animaltrakkerfarmmobile.model.IdInfo
import com.weyr_associates.animaltrakkerfarmmobile.model.IdInfo.IdRemoval
import com.weyr_associates.animaltrakkerfarmmobile.model.IdRemoveReason

fun Cursor.readIdInfo(colQualifier: String? = null): IdInfo {
    val dateOff = getOptLocalDate(AnimalIdInfoTable.Columns.DATE_OFF.qualifiedBy(colQualifier))
    val timeOff = getOptLocalTime(AnimalIdInfoTable.Columns.TIME_OFF.qualifiedBy(colQualifier))
    val removeReason = readOptIdRemoveReason(colQualifier)
    return IdInfo(
        id = getEntityId(AnimalIdInfoTable.Columns.ID.qualifiedBy(colQualifier)),
        number = getString(AnimalIdInfoTable.Columns.NUMBER.qualifiedBy(colQualifier)),
        type = readIdType(colQualifier),
        color = readIdColor(colQualifier),
        location = readIdLocation(colQualifier),
        isOfficial = getBoolean(AnimalIdInfoTable.Columns.IS_OFFICIAL_ID.qualifiedBy(colQualifier)),
        corrected = removeReason?.id == IdRemoveReason.ID_CORRECT_TAG_DATA,
        dateOn = getLocalDate(AnimalIdInfoTable.Columns.DATE_ON.qualifiedBy(colQualifier)),
        timeOn = getLocalTime(AnimalIdInfoTable.Columns.TIME_ON.qualifiedBy(colQualifier)),
        removal = IdRemoval.from(dateOff, timeOff, removeReason)
    )
}

fun Cursor.readOptIdInfo(colQualifier: String? = null): IdInfo? {
    return takeIf { it.isNotNull(AnimalIdInfoTable.Columns.ID.qualifiedBy(colQualifier)) }
        ?.readIdInfo(colQualifier)
}
