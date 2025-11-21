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

fun SQLiteDatabase.queryAnimalIds(animalId: EntityId): List<IdInfo> {
    return rawQuery(QUERY_ANIMAL_IDS, arrayOf(animalId.toString())).use { cursor ->
        cursor.readAllItems(Cursor::readIdInfo)
    }
}

private val QUERY_ANIMAL_IDS get() =
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
        AND ${AnimalIdInfoTable.Columns.DATE_OFF} IS NULL
        AND ${AnimalIdInfoTable.Columns.REMOVE_REASON_ID} IS NULL
        ORDER BY ${IdTypeTable.NAME}.${IdTypeTable.Columns.ORDER} DESC"""
