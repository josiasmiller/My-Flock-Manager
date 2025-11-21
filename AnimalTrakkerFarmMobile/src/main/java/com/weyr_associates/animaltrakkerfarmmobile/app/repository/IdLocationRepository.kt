package com.weyr_associates.animaltrakkerfarmmobile.app.repository

import com.weyr_associates.animaltrakkerfarmmobile.model.IdLocation
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId

interface IdLocationRepository {
    fun queryIdLocations(): List<IdLocation>
    fun queryIdLocation(id: EntityId): IdLocation?
}
