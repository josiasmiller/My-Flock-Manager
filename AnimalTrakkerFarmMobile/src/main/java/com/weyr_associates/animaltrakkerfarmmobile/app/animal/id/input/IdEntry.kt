package com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.input

import com.weyr_associates.animaltrakkerfarmmobile.model.IdInfo
import com.weyr_associates.animaltrakkerfarmmobile.model.IdColor
import com.weyr_associates.animaltrakkerfarmmobile.model.IdLocation
import com.weyr_associates.animaltrakkerfarmmobile.model.IdType
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId

data class IdEntry(
    val id: EntityId = EntityId.UNKNOWN,
    val number: String,
    val type: IdType,
    val color: IdColor,
    val location: IdLocation,
    val isOfficial: Boolean = false,
)

fun IdInfo.asIdEntry(): IdEntry {
    return IdEntry(
        id = id,
        number = number,
        type = type,
        color = color,
        location = location,
        isOfficial = isOfficial
    )
}
