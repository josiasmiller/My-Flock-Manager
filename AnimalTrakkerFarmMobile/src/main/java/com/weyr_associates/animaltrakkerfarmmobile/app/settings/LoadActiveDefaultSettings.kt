package com.weyr_associates.animaltrakkerfarmmobile.app.settings

import android.content.Context
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.DefaultSettingsRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.DefaultSettingsRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseHandler
import com.weyr_associates.animaltrakkerfarmmobile.model.DefaultSettings

class LoadActiveDefaultSettings(
    private val activeDefaultSettings: ActiveDefaultSettings,
    private val defaultSettingsRepo: DefaultSettingsRepository
) {
    companion object {
        fun from(context: Context, databaseHandler: DatabaseHandler): LoadActiveDefaultSettings {
            return LoadActiveDefaultSettings(
                ActiveDefaultSettings.from(context.applicationContext),
                DefaultSettingsRepositoryImpl(
                    databaseHandler, ActiveDefaultSettings.from(context.applicationContext)
                )
            )
        }
    }

    operator fun invoke(): DefaultSettings {
        val defaultSettingsId = activeDefaultSettings.loadActiveDefaultSettingsId()
        return requireNotNull(
            defaultSettingsRepo.queryDefaultSettingsById(defaultSettingsId) ?:
                fallbackOnStandardDefaults()
        )
    }

    private fun fallbackOnStandardDefaults(): DefaultSettings {
        return defaultSettingsRepo.queryStandardDefaultSettings().also {
            activeDefaultSettings.saveActiveDefaultSettingsId(it.id)
        }
    }
}
