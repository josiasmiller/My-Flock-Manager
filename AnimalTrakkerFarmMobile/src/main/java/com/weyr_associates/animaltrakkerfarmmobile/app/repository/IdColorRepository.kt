package com.weyr_associates.animaltrakkerfarmmobile.app.repository

import com.weyr_associates.animaltrakkerfarmmobile.model.IdColor
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId

interface IdColorRepository {
    fun queryIdColors(): List<IdColor>
    fun queryIdColor(id: EntityId): IdColor?
}
