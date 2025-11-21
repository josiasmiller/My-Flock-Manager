package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Laboratory(
    override val id: EntityId,
    val companyId: Int,
    override val name: String,
    val licenseNumber: String?,
    val order: Int
) : Parcelable, HasIdentity, HasName {
    companion object {
        const val LAB_COMPANY_ID_OPTIMAL_LIVESTOCK_RAW = "e76333d0-4787-4aa0-b816-0ddbf273a634" //LEGACY ID = 660
        val LAB_COMPANY_ID_OPTIMAL_LIVESTOCK = EntityId(LAB_COMPANY_ID_OPTIMAL_LIVESTOCK_RAW)
    }
}
