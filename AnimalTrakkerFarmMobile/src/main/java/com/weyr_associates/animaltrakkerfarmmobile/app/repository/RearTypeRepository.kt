package com.weyr_associates.animaltrakkerfarmmobile.app.repository

import com.weyr_associates.animaltrakkerfarmmobile.model.RearType

interface RearTypeRepository {
    suspend fun queryAllRearTypes(): List<RearType>
}
