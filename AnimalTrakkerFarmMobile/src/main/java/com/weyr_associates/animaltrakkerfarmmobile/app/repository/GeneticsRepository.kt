package com.weyr_associates.animaltrakkerfarmmobile.app.repository

import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.GeneticCoatColor

interface GeneticsRepository {
    suspend fun queryCoatColorsByRegistry(registryCompanyId: EntityId): List<GeneticCoatColor>
}
