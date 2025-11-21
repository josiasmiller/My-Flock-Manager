package com.weyr_associates.animaltrakkerfarmmobile.app.settings

import com.weyr_associates.animaltrakkerfarmmobile.app.repository.DefaultSettingsRepository
import com.weyr_associates.animaltrakkerfarmmobile.model.ItemEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoadActiveDefaultSettingsInfo(
    private val activeDefaultSettings: ActiveDefaultSettings,
    private val defaultSettingsRepo: DefaultSettingsRepository
) {
    suspend operator fun invoke(): ItemEntry {
        val defaultSettingsId = activeDefaultSettings.loadActiveDefaultSettingsId()
        return withContext(Dispatchers.IO) {
            requireNotNull(
                defaultSettingsRepo.queryDefaultSettingsEntryById(defaultSettingsId)
            )
        }
    }
}
