package com.weyr_associates.animaltrakkerfarmmobile.app.repository

import com.weyr_associates.animaltrakkerfarmmobile.model.DefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.ItemEntry
import kotlinx.coroutines.flow.Flow

interface DefaultSettingsRepository {
    suspend fun queryActiveDefaultSettings(): DefaultSettings
    fun activeDefaultSettings(): Flow<DefaultSettings>
    fun queryDefaultSettingsEntries(): List<ItemEntry>
    fun queryDefaultSettingsEntryById(id: EntityId): ItemEntry?
    fun queryStandardDefaultSettings(): DefaultSettings
    fun queryDefaultSettingsById(id: EntityId): DefaultSettings?

    fun updateNextTrichIdNumber(id: EntityId, nextTrichId: Int): Int
}
