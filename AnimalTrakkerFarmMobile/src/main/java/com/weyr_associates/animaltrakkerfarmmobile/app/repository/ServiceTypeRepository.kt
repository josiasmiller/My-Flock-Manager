package com.weyr_associates.animaltrakkerfarmmobile.app.repository

import com.weyr_associates.animaltrakkerfarmmobile.model.ServiceType

interface ServiceTypeRepository {
    suspend fun queryAllServiceTypes(): List<ServiceType>
}
