package com.weyr_associates.animaltrakkerfarmmobile.app.repository

import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.TissueTest

interface TissueTestRepository {
    fun queryTissueTests(): List<TissueTest>
    fun queryTissueTestById(id: EntityId): TissueTest?
}
