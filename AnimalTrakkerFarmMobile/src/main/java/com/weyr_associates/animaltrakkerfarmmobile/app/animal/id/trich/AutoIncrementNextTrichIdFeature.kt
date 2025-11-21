package com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.trich

import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.input.IdEntry
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.DefaultSettingsRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.model.DefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.model.IdType
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AutoIncrementNextTrichIdFeature(
    private val loadActiveDefaultSettings: LoadActiveDefaultSettings,
    private val defaultSettingsRepository: DefaultSettingsRepository
) {

    var shouldAutoIncrementTrichNumber = false
        private set
    var nextTrichNumber = 0
        private set

    private var activeDefaultSettingsId = EntityId.UNKNOWN

    suspend fun configureFromSettings() {
        configureFromSettings(
            withContext(Dispatchers.IO) {
                loadActiveDefaultSettings()
            }
        )
    }

    fun configureFromSettings(defaults: DefaultSettings) {
        activeDefaultSettingsId = defaults.id
        shouldAutoIncrementTrichNumber = defaults.trichIdAutoIncrement
        nextTrichNumber = defaults.trichNextIdNumber ?: 0
    }

    fun shouldAutoPopulateTrichNumber(idType: IdType): Boolean {
        return idType.id == IdType.ID_TYPE_ID_TRICH &&
                shouldAutoIncrementTrichNumber
    }

    suspend fun autoIncrementIfRequired(idEntry: IdEntry) {
        if (idEntry.type.id == IdType.ID_TYPE_ID_TRICH &&
            shouldAutoIncrementTrichNumber) {
            val updatedTrichNumber = idEntry.number.toInt() + 1
            defaultSettingsRepository.updateNextTrichIdNumber(
                activeDefaultSettingsId,
                updatedTrichNumber
            )
            nextTrichNumber = updatedTrichNumber
        }
    }
}
