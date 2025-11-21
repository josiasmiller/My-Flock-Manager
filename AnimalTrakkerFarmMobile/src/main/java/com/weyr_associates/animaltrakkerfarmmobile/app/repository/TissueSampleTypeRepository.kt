package com.weyr_associates.animaltrakkerfarmmobile.app.repository

import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.TissueSampleType

interface TissueSampleTypeRepository {
    fun queryTissueSampleTypes(): List<TissueSampleType>
    fun queryTissueSampleTypeById(id: EntityId): TissueSampleType?
}
