package com.weyr_associates.animaltrakkerfarmmobile.app.settings

import com.weyr_associates.animaltrakkerfarmmobile.app.repository.IdColorRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.IdLocationRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.IdTypeRepository
import com.weyr_associates.animaltrakkerfarmmobile.model.DefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.IdColor
import com.weyr_associates.animaltrakkerfarmmobile.model.IdLocation
import com.weyr_associates.animaltrakkerfarmmobile.model.IdType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class IdConfig(
    val idType: IdType,
    val idColor: IdColor?,
    val idLocation: IdLocation?
)

data class IdConfigs(
    val primary: IdConfig,
    val secondary: IdConfig?,
    val tertiary: IdConfig?
)

class LoadDefaultIdConfigs(
    private val loadActiveDefaultSettings: LoadActiveDefaultSettings,
    private val idTypeRepository: IdTypeRepository,
    private val idColorRepository: IdColorRepository,
    private val idLocationRepository: IdLocationRepository
) {
    suspend operator fun invoke(): IdConfigs {
        val defaultSettings = withContext(Dispatchers.IO) {
            loadActiveDefaultSettings()
        }

        val primaryIdTypeId = defaultSettings.idTypeIdPrimary
        val secondaryIdTypeId = defaultSettings.idTypeIdSecondary
        val tertiaryIdTypeId = defaultSettings.idTypeIdTertiary

        val primaryConfig = resolveConfigForIdType(defaultSettings, primaryIdTypeId)
        val secondaryConfig = resolveConfigForIdType(defaultSettings, secondaryIdTypeId)
        val tertiaryConfig = resolveConfigForIdType(defaultSettings, tertiaryIdTypeId)

        return IdConfigs(requireNotNull(primaryConfig), secondaryConfig, tertiaryConfig)
    }

    private fun resolveConfigForIdType(defaultSettings: DefaultSettings, idTypeId: EntityId?): IdConfig? {
        return idTypeId?.let {
            idTypeRepository.queryForIdType(idTypeId)
        }?.let { idType ->
            IdConfig(
                idType = idType,
                idColor = defaultSettings.defaultIdColorFromIdType(idTypeId)?.let {
                    idColorRepository.queryIdColor(it)
                },
                idLocation = defaultSettings.defaultIdLocationFromIdType(idTypeId)?.let {
                    idLocationRepository.queryIdLocation(it)
                }
            )
        }
    }
}
