package com.weyr_associates.animaltrakkerfarmmobile.app.repository

import com.weyr_associates.animaltrakkerfarmmobile.model.IdType
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId

interface IdTypeRepository {
    fun queryIdTypes(): List<IdType>
    fun queryIdTypesForSearch(): List<IdType>
    fun queryForIdType(id: EntityId): IdType?
}
