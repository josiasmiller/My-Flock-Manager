package com.weyr_associates.animaltrakkerfarmmobile.app.repository

import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.Species

interface SpeciesRepository {
    suspend fun queryAllSpecies(): List<Species>
    suspend fun querySpeciesById(id: EntityId): Species?
}
