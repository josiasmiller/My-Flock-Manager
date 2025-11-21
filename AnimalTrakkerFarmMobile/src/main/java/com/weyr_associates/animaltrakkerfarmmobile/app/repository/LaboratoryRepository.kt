package com.weyr_associates.animaltrakkerfarmmobile.app.repository

import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.Laboratory

interface LaboratoryRepository {
    fun queryLaboratories(): List<Laboratory>
    fun queryLaboratoryById(id: EntityId): Laboratory?
    fun queryLaboratoryByCompanyId(companyId: EntityId): Laboratory?
}
