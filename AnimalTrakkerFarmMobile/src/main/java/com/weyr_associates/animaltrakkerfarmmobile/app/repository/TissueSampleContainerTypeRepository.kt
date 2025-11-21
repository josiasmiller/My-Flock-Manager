package com.weyr_associates.animaltrakkerfarmmobile.app.repository

import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.TissueSampleContainerType

interface TissueSampleContainerTypeRepository {
    fun queryTissueSampleContainerTypes(): List<TissueSampleContainerType>
    fun queryTissueSampleContainerTypeById(id: EntityId): TissueSampleContainerType?
}
