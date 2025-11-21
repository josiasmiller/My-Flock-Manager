package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Owner is not uniquely identified by its id field,
 * but rather its id and type fields.
 */
@Parcelize
data class Owner(
    override val id: EntityId,
    val type: Type,
    override val name: String,
    val order: Int
) : Parcelable, HasIdentity, HasName {
    enum class Type(val code: Int) {

        CONTACT(0),
        COMPANY(1);

        companion object {

            const val TYPE_ID_CONTACT = 0
            const val TYPE_ID_COMPANY = 1

            fun fromCode(code: Int): Type = when(code) {
                CONTACT.code -> CONTACT
                COMPANY.code -> COMPANY
                else -> throw IllegalArgumentException(
                    "$code is not a valid code for ${Type::class.simpleName}."
                )
            }
        }
        val isContact: Boolean
            get() = this == CONTACT

        val isCompany: Boolean
            get() = this == COMPANY
    }
}
