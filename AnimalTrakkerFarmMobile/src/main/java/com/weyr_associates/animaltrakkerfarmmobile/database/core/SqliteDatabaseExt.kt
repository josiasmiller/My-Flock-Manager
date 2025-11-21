package com.weyr_associates.animaltrakkerfarmmobile.database.core

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId

/**
 * Helper method to generate a PK value
 * to use in the actual insertion
 * into the database.
 *
 * @return The generated [EntityId] if
 * insertion is successful, or
 * [EntityId.UNKNOWN] in the case of
 * failure.
 */
fun SQLiteDatabase.insertWithPK(
    tableSpec: TableSpec<*>,
    nullColumnHack: String?,
    values: ContentValues
): EntityId = executeInsert(
    tableSpec,
    nullColumnHack,
    values,
    this::insert
)

/**
 * Helper method to generate a PK value
 * to use in the actual insertion
 * into the database.
 *
 * @return The generated [EntityId] if
 * insertion is successful, or
 * [EntityId.UNKNOWN] in the case of
 * failure.
 *
 * @throws [android.database.SQLException]
 * in the event of an error.
 */
fun SQLiteDatabase.insertWithPKOrThrow(
    tableSpec: TableSpec<*>,
    nullColumnHack: String?,
    values: ContentValues
): EntityId = executeInsert(
    tableSpec,
    nullColumnHack,
    values,
    this::insertOrThrow
)

private fun executeInsert(
    table: TableSpec<*>,
    nullColumnHack: String?,
    values: ContentValues,
    insertion: (String, String?, ContentValues) -> Long
): EntityId {
    val entityId = EntityId.generate()
    val recordId = insertion.invoke(
        table.name,
        nullColumnHack,
        values.apply {
            put(table.primaryKeyColumnName, entityId)
        }
    )
    return when (recordId) {
        -1L -> EntityId.UNKNOWN
        else -> entityId
    }
}
