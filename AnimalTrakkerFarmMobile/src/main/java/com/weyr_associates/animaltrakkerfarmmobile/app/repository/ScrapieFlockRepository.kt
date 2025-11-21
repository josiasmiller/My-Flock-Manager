package com.weyr_associates.animaltrakkerfarmmobile.app.repository

import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.Owner
import com.weyr_associates.animaltrakkerfarmmobile.model.ScrapieFlockNumber

interface ScrapieFlockRepository {
    fun queryActiveScrapieFlockNumberForOwner(ownerId: EntityId, ownerType: Owner.Type): ScrapieFlockNumber?
    fun queryActiveScrapieFlockNumberFromNumber(scrapieFlockNumber: String): ScrapieFlockNumber?
}
