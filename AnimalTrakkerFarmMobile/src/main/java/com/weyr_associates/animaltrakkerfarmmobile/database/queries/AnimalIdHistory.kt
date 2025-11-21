package com.weyr_associates.animaltrakkerfarmmobile.database.queries

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readAllItems
import com.weyr_associates.animaltrakkerfarmmobile.database.sql.readIdInfo
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.AnimalIdInfoTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.IdColorTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.IdLocationTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.IdRemoveReasonTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.IdTypeTable
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.IdInfo

fun SQLiteDatabase.queryAnimalIdHistory(animalId: EntityId): List<IdInfo> {
    return rawQuery(QUERY_ANIMAL_ID_HISTORY, arrayOf(animalId.toString())).use { cursor ->
        cursor.readAllItems(Cursor::readIdInfo)
    }
}

private val QUERY_ANIMAL_ID_HISTORY get() =
    """SELECT * FROM ${AnimalIdInfoTable.NAME}
        JOIN ${IdColorTable.NAME} 
            ON ${AnimalIdInfoTable.NAME}.${AnimalIdInfoTable.Columns.MALE_ID_COLOR_ID} = ${IdColorTable.NAME}.${IdColorTable.Columns.ID}
        JOIN ${IdLocationTable.NAME}
            ON ${AnimalIdInfoTable.NAME}.${AnimalIdInfoTable.Columns.ID_LOCATION_ID} = ${IdLocationTable.NAME}.${IdLocationTable.Columns.ID}
        JOIN ${IdTypeTable.NAME}
            ON ${AnimalIdInfoTable.NAME}.${AnimalIdInfoTable.Columns.ID_TYPE_ID} = ${IdTypeTable.NAME}.${IdTypeTable.Columns.ID}
        LEFT OUTER JOIN ${IdRemoveReasonTable.NAME}
            ON ${AnimalIdInfoTable.NAME}.${AnimalIdInfoTable.Columns.REMOVE_REASON_ID} = ${IdRemoveReasonTable.NAME}.${IdRemoveReasonTable.Columns.ID}
        WHERE ${AnimalIdInfoTable.Columns.ANIMAL_ID} = ?
        ORDER BY (
                ${AnimalIdInfoTable.NAME}.${AnimalIdInfoTable.Columns.DATE_OFF} IS NULL OR
                ${AnimalIdInfoTable.NAME}.${AnimalIdInfoTable.Columns.REMOVE_REASON_ID} IS NULL
            ) DESC, 
            ${AnimalIdInfoTable.NAME}.${AnimalIdInfoTable.Columns.DATE_ON} DESC,
            ${IdTypeTable.NAME}.${IdTypeTable.Columns.ORDER} ASC,
            ${AnimalIdInfoTable.NAME}.${AnimalIdInfoTable.Columns.TIME_ON} DESC"""
