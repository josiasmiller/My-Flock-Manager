package com.weyr_associates.animaltrakkerfarmmobile.app.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.weyr_associates.animaltrakkerfarmmobile.app.core.getEntityId
import com.weyr_associates.animaltrakkerfarmmobile.app.core.putEntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.DefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class ActiveDefaultSettings(private val preferences: SharedPreferences) {
    companion object {
        @JvmStatic
        fun from(context: Context): ActiveDefaultSettings {
            return ActiveDefaultSettings(PreferenceManager.getDefaultSharedPreferences(context))
        }

        const val PREFS_KEY_ACTIVE_DEFAULT_SETTINGS_ID = "ACTIVE_DEFAULT_SETTINGS_ID"
    }

    fun activeDefaultsSettingId(): Flow<EntityId> = callbackFlow {
        val sharedPrefsCallback = SharedPreferences.OnSharedPreferenceChangeListener { sharedPrefs, key ->
            if (key == PREFS_KEY_ACTIVE_DEFAULT_SETTINGS_ID) {
                trySend(loadActiveDefaultSettingsId())
            }
        }
        preferences.registerOnSharedPreferenceChangeListener(sharedPrefsCallback)
        send(loadActiveDefaultSettingsId())
        awaitClose {
            preferences.unregisterOnSharedPreferenceChangeListener(sharedPrefsCallback)
        }
    }

    fun loadActiveDefaultSettingsId(): EntityId {
        var defaultSettingsId: EntityId
        try {
            defaultSettingsId = preferences.getEntityId(
                PREFS_KEY_ACTIVE_DEFAULT_SETTINGS_ID,
                DefaultSettings.SETTINGS_ID_DEFAULT
            )
        } catch(ex: ClassCastException) {
            defaultSettingsId = DefaultSettings.SETTINGS_ID_DEFAULT
            saveActiveDefaultSettingsId(defaultSettingsId)
        }
        return defaultSettingsId
    }

    fun saveActiveDefaultSettingsId(id: EntityId) {
        preferences.edit()
            .putEntityId(PREFS_KEY_ACTIVE_DEFAULT_SETTINGS_ID, id)
            .apply()
    }
}
