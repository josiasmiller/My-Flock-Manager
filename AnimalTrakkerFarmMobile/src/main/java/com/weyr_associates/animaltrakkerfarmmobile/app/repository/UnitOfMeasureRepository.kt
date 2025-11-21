package com.weyr_associates.animaltrakkerfarmmobile.app.repository

import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.UnitOfMeasure

interface UnitOfMeasureRepository {
    suspend fun queryUnitsOfMeasure(): List<UnitOfMeasure>
    suspend fun queryUnitsOfMeasureByType(typeId: EntityId): List<UnitOfMeasure>
    suspend fun queryUnitOfMeasureById(id: EntityId): UnitOfMeasure?
}
