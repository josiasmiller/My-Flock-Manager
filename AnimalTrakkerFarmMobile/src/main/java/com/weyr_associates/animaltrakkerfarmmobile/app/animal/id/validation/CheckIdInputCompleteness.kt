package com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.validation

import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.input.IdInput
import com.weyr_associates.animaltrakkerfarmmobile.model.IdColor
import com.weyr_associates.animaltrakkerfarmmobile.model.IdLocation
import com.weyr_associates.animaltrakkerfarmmobile.model.IdType
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId

enum class IdInputCompleteness {
    EMPTY,
    PARTIAL,
    COMPLETE;

    val isEmpty get() = this == EMPTY
    val isPartial get() = this == PARTIAL
    val isComplete get() = this == COMPLETE
}

class CheckIdInputCompleteness {

    companion object {
        val INSTANCE = CheckIdInputCompleteness()
    }

    operator fun invoke(
        idNumber: String?,
        idType: IdType?,
        idColor: IdColor?,
        idLocation: IdLocation?
    ) = invoke(
        idNumber,
        idType?.id,
        idColor?.id,
        idLocation?.id
    )

    operator fun invoke(idInput: IdInput) = invoke(
        idInput.number,
        idInput.type,
        idInput.color,
        idInput.location
    )

    operator fun invoke(
        idNumber: String?,
        idTypeId: EntityId?,
        idColorId: EntityId?,
        idLocationId: EntityId?
    ): IdInputCompleteness {

        val isComplete = !idNumber.isNullOrBlank() &&
                idTypeId != null &&
                idColorId != null &&
                idLocationId != null

        val isEmpty = idNumber.isNullOrBlank() &&
                idTypeId == null &&
                idColorId == null &&
                idLocationId == null

        return when {
            isComplete -> IdInputCompleteness.COMPLETE
            isEmpty -> IdInputCompleteness.EMPTY
            else -> IdInputCompleteness.PARTIAL
        }
    }
}
