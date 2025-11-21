package com.weyr_associates.animaltrakkerfarmmobile.app.settings

import com.weyr_associates.animaltrakkerfarmmobile.app.repository.UnitOfMeasureRepository
import com.weyr_associates.animaltrakkerfarmmobile.model.UnitOfMeasure

class LoadDefaultWeightUnits(
    private val loadDefaultWeightUnitsId: LoadDefaultWeightUnitsId,
    private val unitsRepository: UnitOfMeasureRepository
) {
    suspend operator fun invoke(): UnitOfMeasure? {
        return unitsRepository.queryUnitOfMeasureById(loadDefaultWeightUnitsId())
    }
}
