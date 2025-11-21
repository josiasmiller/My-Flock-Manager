package com.weyr_associates.animaltrakkerfarmmobile.app.repository

import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.Owner

interface OwnerRepository {
    fun queryOwners(): List<Owner>
    fun queryOwner(ownerId: EntityId, typeId: Int): Owner?
}
