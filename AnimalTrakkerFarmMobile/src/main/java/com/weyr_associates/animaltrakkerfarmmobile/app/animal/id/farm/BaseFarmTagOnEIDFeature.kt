package com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.farm

import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadActiveDefaultSettings

class BaseFarmTagOnEIDFeature(
    val loadActiveDefaultSettings: LoadActiveDefaultSettings
) {

    private var settings: Settings? = null

    fun extractFarmTagPrefixFromEID(eidString: String): String? {
        val settings = loadSettings()
        return if (settings.baseFarmTagOnEID) {
            return eidString.takeLast(settings.numberOfDigitsFromEID)
        } else { null }
    }

    private fun loadSettings(): Settings {
        return settings ?: loadActiveDefaultSettings().let {
            Settings(it.farmIdBasedOnEid, it.farmIdNumberDigitsFromEid)
        }.also { settings = it }
    }

    private data class Settings(
        val baseFarmTagOnEID: Boolean,
        val numberOfDigitsFromEID: Int
    ) {
        init {
            require(!baseFarmTagOnEID || 0 < numberOfDigitsFromEID) {
                "Number of digits extracted from EID for Farm Tag must be greater than 0"
            }
        }
    }
}
