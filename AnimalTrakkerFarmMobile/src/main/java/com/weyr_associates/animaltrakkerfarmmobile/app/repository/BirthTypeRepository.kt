package com.weyr_associates.animaltrakkerfarmmobile.app.repository

import com.weyr_associates.animaltrakkerfarmmobile.model.BirthType

interface BirthTypeRepository {
    suspend fun queryBirthTypes(): List<BirthType>
}
