package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AnimalRearing(
    val birthOrder: Int,
    val birthTypeId: Int,
    val birthType: String,
    val rearTypeId: Int?,
    val rearType: String?
) : Parcelable {
    companion object {
        fun from(
            birthOrder: Int?,
            birthTypeId: Int?,
            birthType: String?,
            rearTypeId: Int?,
            rearType: String?
        ): AnimalRearing? {
            return if (
                birthOrder == null ||
                birthTypeId == null ||
                birthType == null
            ) {
                null
            } else {
                AnimalRearing(
                    birthOrder,
                    birthTypeId,
                    birthType,
                    rearTypeId,
                    rearType
                )
            }
        }
    }
}
