package com.weyr_associates.animaltrakkerfarmmobile.app.settings

import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class DefaultIdTypeIds(
    val primaryIdTypeId: EntityId,
    val secondaryIdTypeId: EntityId?,
    val tertiaryIdTypeId: EntityId?
)

class LoadDefaultIdTypeIds(
    private val loadActiveDefaults: LoadActiveDefaultSettings
) {
    suspend operator fun invoke(): DefaultIdTypeIds {
        return withContext(Dispatchers.IO) {
            loadActiveDefaults()
        }.let {
            DefaultIdTypeIds(
                it.idTypeIdPrimary,
                it.idTypeIdSecondary,
                it.idTypeIdTertiary
            )
        }
    }
}
