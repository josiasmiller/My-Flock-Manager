package com.weyr_associates.animaltrakkerfarmmobile.app.repository

import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.FlockPrefix
import com.weyr_associates.animaltrakkerfarmmobile.model.Owner

interface FlockPrefixRepository {
    fun queryFlockPrefixById(id: EntityId): FlockPrefix?
    fun queryFlockPrefixByOwner(
        ownerId: EntityId,
        ownerType: Owner.Type,
        registryCompanyId: EntityId
    ): FlockPrefix?
}
