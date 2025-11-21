package com.weyr_associates.animaltrakkerfarmmobile.app.settings

import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoadDefaultSpeciesId(
    private val loadActiveDefaults: LoadActiveDefaultSettings
) {
    suspend operator fun invoke(): EntityId {
        return withContext(Dispatchers.IO) {
            loadActiveDefaults().speciesId
        }
    }
}
