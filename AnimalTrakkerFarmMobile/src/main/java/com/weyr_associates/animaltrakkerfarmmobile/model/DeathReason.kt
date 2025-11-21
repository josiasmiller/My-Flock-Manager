package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DeathReason(
    override val id: EntityId,
    val reason: String,
    val userId: EntityId,
    val userType: UserType,
    val order: Int
) : Parcelable, HasIdentity {
    companion object {

        const val ID_DEATH_REASON_UNKNOWN_RAW = "da1178b9-e4b9-4b25-8578-987aa5a8ecd2" //LEGACY ID = 14
        const val ID_DEATH_REASON_STILLBORN_RAW = "9de9f67c-afc4-4d16-83d3-20c4337f4344" //LEGACY ID = 19

        val ID_DEATH_REASON_UNKNOWN = EntityId(ID_DEATH_REASON_UNKNOWN_RAW)
        val ID_DEATH_REASON_STILLBORN = EntityId(ID_DEATH_REASON_STILLBORN_RAW)

        const val DEATH_REASON_MISSING = "Death Reason Missing"
    }
}
