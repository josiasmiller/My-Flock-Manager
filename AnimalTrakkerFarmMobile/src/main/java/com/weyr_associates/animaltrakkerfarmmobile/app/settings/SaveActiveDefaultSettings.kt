package com.weyr_associates.animaltrakkerfarmmobile.app.settings

import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId

class SaveActiveDefaultSettings(
    private val activeDefaultSettings: ActiveDefaultSettings
) {
    operator fun invoke(defaultSettingsId: EntityId) {
        activeDefaultSettings.saveActiveDefaultSettingsId(
            defaultSettingsId
        )
    }
}
