package com.weyr_associates.animaltrakkerfarmmobile.app.repository.database

import android.content.ContentValues
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.DefaultSettingsRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.ActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseHandler
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Sql
import com.weyr_associates.animaltrakkerfarmmobile.database.core.put
import com.weyr_associates.animaltrakkerfarmmobile.database.queries.queryDefaultSettingsById
import com.weyr_associates.animaltrakkerfarmmobile.database.queries.queryDefaultSettingsEntries
import com.weyr_associates.animaltrakkerfarmmobile.database.queries.queryDefaultSettingsEntryById
import com.weyr_associates.animaltrakkerfarmmobile.database.queries.queryStandardDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.DefaultSettingsTable
import com.weyr_associates.animaltrakkerfarmmobile.model.DefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.ItemEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime

class DefaultSettingsRepositoryImpl(
    private val databaseHandler: DatabaseHandler,
    private val activeDefaultSettings: ActiveDefaultSettings
) : DefaultSettingsRepository {

    override suspend fun queryActiveDefaultSettings(): DefaultSettings {
        return queryDefaultSettingsById(activeDefaultSettings.loadActiveDefaultSettingsId())
            ?: queryStandardDefaultSettings()
    }

    override fun activeDefaultSettings(): Flow<DefaultSettings> = activeDefaultSettings.activeDefaultsSettingId()
        .map { queryDefaultSettingsById(it) ?: queryStandardDefaultSettings() }

    override fun queryDefaultSettingsEntries(): List<ItemEntry> {
        return databaseHandler.readableDatabase.queryDefaultSettingsEntries()
    }

    override fun queryDefaultSettingsEntryById(id: EntityId): ItemEntry? {
        return databaseHandler.readableDatabase.queryDefaultSettingsEntryById(id)
    }

    override fun queryStandardDefaultSettings(): DefaultSettings {
        return databaseHandler.readableDatabase.queryStandardDefaultSettings()
    }

    override fun queryDefaultSettingsById(id: EntityId): DefaultSettings? {
        return databaseHandler.readableDatabase.queryDefaultSettingsById(id)
    }

    override fun updateNextTrichIdNumber(id: EntityId, nextTrichId: Int): Int {
        return databaseHandler.writableDatabase.update(
            DefaultSettingsTable.NAME,
            ContentValues().apply {
                put(DefaultSettingsTable.Columns.TRICH_TAG_NEXT_TAG_NUMBER, nextTrichId)
                put(DefaultSettingsTable.Columns.MODIFIED, Sql.formatDateTime(LocalDateTime.now()))
            },
            "${DefaultSettingsTable.Columns.ID} = ?",
            arrayOf(id.toString())
        )
    }
}
