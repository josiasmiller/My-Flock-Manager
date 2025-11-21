package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class IdRemoveReason(
    override val id: EntityId,
    val text: String,
    val order: Int
) : Parcelable, HasIdentity {
    companion object {
        const val ID_CORRECT_TAG_DATA_RAW = "8658866c-2e00-4367-aea4-9a3998066cc0" //LEGACY ID = 8
        val ID_CORRECT_TAG_DATA = EntityId(ID_CORRECT_TAG_DATA_RAW)
    }
}
