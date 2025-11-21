package com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.input

import com.weyr_associates.animaltrakkerfarmmobile.model.IdColor
import com.weyr_associates.animaltrakkerfarmmobile.model.IdLocation
import com.weyr_associates.animaltrakkerfarmmobile.model.IdType
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId

data class IdInput(
    val number: String?,
    val type: IdType?,
    val color: IdColor?,
    val location: IdLocation?
) {
    fun toEntry(id: EntityId = EntityId.UNKNOWN): IdEntry {
        return IdEntry(
            id = id,
            number = requireNotNull(
                number?.takeIf { it.isNotBlank() }?.trim()
            ),
            type = requireNotNull(type),
            color = requireNotNull(color),
            location = requireNotNull(location)
        )
    }

    fun toOptEntry(): IdEntry? = when {
            !number.isNullOrBlank() &&
                    type != null &&
                    color != null &&
                    location != null -> toEntry()
            else -> null
        }
}
