package com.weyr_associates.animaltrakkerfarmmobile.app.repository

import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.Sex

interface SexRepository {
    fun querySexesForSpeciesId(speciesId: EntityId): List<Sex>
    fun querySexById(id: EntityId): Sex?
}
