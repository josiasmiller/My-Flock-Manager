package com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.weight

import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.AnimalAction
import com.weyr_associates.animaltrakkerfarmmobile.model.UnitOfMeasure
import java.util.UUID

data class WeightAction(
    val weight: Float? = null,
    val units: UnitOfMeasure,
    val isScanning: Boolean = false,
    val isFixedInConfiguration: Boolean = false,
    override val actionId: UUID = UUID.randomUUID()
) : AnimalAction {
    override val isComplete: Boolean
        get() = weight != null
}
