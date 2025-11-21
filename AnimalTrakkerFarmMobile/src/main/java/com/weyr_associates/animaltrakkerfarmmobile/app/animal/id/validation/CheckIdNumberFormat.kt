package com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.validation

import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.input.IdEntry
import com.weyr_associates.animaltrakkerfarmmobile.model.IdType
import com.weyr_associates.animaltrakkerfarmmobile.app.core.Result
import com.weyr_associates.animaltrakkerfarmmobile.app.core.Result.Failure
import com.weyr_associates.animaltrakkerfarmmobile.app.core.Result.Success

data class IdNumberFormatError(val idEntry: IdEntry) : IdValidationError

class CheckIdNumberFormat {
    operator fun invoke(idEntry: IdEntry): Result<Unit, IdNumberFormatError> {
        return if (isNumberFormatValid(idEntry)) Success(Unit) else
            Failure(IdNumberFormatError(idEntry))
    }

    private fun isNumberFormatValid(idEntry: IdEntry): Boolean {
        return when (idEntry.type.id) {
            IdType.ID_TYPE_ID_EID -> IdFormat.isEIDFormat(idEntry.number)
            IdType.ID_TYPE_ID_TRICH -> IdFormat.isTrichIdFormat(idEntry.number)
            IdType.ID_TYPE_ID_FARM -> IdFormat.isFarmIdFormat(idEntry.number)
            IdType.ID_TYPE_ID_FED,
                IdType.ID_TYPE_ID_FED_CANADIAN -> IdFormat.isFederalScrapieIdFormat(idEntry.number)
            IdType.ID_TYPE_ID_FREEZE_BRAND -> IdFormat.isFreezeBrandIdFormat(idEntry.number)
            else -> true
        }
    }
}
