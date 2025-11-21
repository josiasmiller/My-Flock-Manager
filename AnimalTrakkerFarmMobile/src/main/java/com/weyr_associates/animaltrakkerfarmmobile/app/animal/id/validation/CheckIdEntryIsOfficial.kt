package com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.validation

import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.input.IdEntry
import com.weyr_associates.animaltrakkerfarmmobile.model.IdType
import java.util.regex.Pattern

class CheckIdEntryIsOfficial {

    companion object {
        val EID_COUNTRY_CODE_PATTERN_UNOFFICIAL = Pattern.compile("9\\d\\d").toRegex()
    }

    operator fun invoke(idEntry: IdEntry): Boolean {
        return when (idEntry.type.id) {
            IdType.ID_TYPE_ID_EID -> {
                IdFormat.isEIDFormat(idEntry.number) &&
                        IdFormat.extractEIDCountryCode(idEntry.number)?.let {
                            !EID_COUNTRY_CODE_PATTERN_UNOFFICIAL.matches(it)
                        } ?: false
            }
            IdType.ID_TYPE_ID_FED,
            IdType.ID_TYPE_ID_FED_CANADIAN,
            IdType.ID_TYPE_ID_BANGS,
            IdType.ID_TYPE_ID_NUES -> true
            else -> false
        }
    }
}
