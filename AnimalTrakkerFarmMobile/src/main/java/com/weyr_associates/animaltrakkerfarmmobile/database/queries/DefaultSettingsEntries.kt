package com.weyr_associates.animaltrakkerfarmmobile.database.queries

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readAllItems
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readFirstItem
import com.weyr_associates.animaltrakkerfarmmobile.database.sql.readDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.database.sql.readDefaultSettingsEntry
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.DefaultSettingsTable
import com.weyr_associates.animaltrakkerfarmmobile.model.DefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.ItemEntry

fun SQLiteDatabase.queryDefaultSettingsEntryById(id: EntityId): ItemEntry? {
    return rawQuery(QUERY_DEFAULT_SETTINGS_ENTRY_BY_ID, arrayOf(id.toString())).use { cursor ->
        cursor.readFirstItem(Cursor::readDefaultSettingsEntry)
    }
}

fun SQLiteDatabase.queryDefaultSettingsEntries(): List<ItemEntry> {
    return rawQuery(QUERY_DEFAULT_SETTINGS_ENTRIES, null).use { cursor ->
        cursor.readAllItems(Cursor::readDefaultSettingsEntry)
    }
}

fun SQLiteDatabase.queryStandardDefaultSettings(): DefaultSettings {
    return rawQuery(QUERY_DEFAULT_SETTINGS_BY_ID, arrayOf(DefaultSettings.SETTINGS_ID_DEFAULT.toString())).use { cursor ->
        cursor.readFirstItem(Cursor::readDefaultSettings)
            ?: throw IllegalStateException("Standard default settings records found.")
    }
}

fun SQLiteDatabase.queryDefaultSettingsById(id: EntityId): DefaultSettings? {
    return rawQuery(QUERY_DEFAULT_SETTINGS_BY_ID, arrayOf(id.toString())).use { cursor ->
        cursor.readFirstItem(Cursor::readDefaultSettings)
    }
}

private val QUERY_DEFAULT_SETTINGS_ENTRY_BY_ID get() =
    """SELECT 
        |${DefaultSettingsTable.Columns.ID},
        |${DefaultSettingsTable.Columns.NAME}
        |FROM ${DefaultSettingsTable.NAME}
        |WHERE ${DefaultSettingsTable.Columns.ID} = ?""".trimMargin()

private val QUERY_DEFAULT_SETTINGS_ENTRIES get() =
    """SELECT
        |${DefaultSettingsTable.Columns.ID},
        |${DefaultSettingsTable.Columns.NAME}
        |FROM ${DefaultSettingsTable.NAME}
        |ORDER BY ${DefaultSettingsTable.Columns.NAME}
    """.trimMargin()

val QUERY_DEFAULT_SETTINGS_BY_ID get() =
    """SELECT * FROM ${DefaultSettingsTable.NAME}
        |WHERE ${DefaultSettingsTable.Columns.ID} = ?""".trimMargin()
