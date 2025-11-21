package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class UserType(val id: Int) : Parcelable {
    CONTACT(0),
    COMPANY(1);

    companion object {
        fun fromId(id: Int): UserType = when(id) {
            CONTACT.id -> CONTACT
            COMPANY.id -> COMPANY
            else -> throw IllegalArgumentException(
                "$id is not a valid code for ${Breeder.Type::class.simpleName}."
            )
        }
    }
}
