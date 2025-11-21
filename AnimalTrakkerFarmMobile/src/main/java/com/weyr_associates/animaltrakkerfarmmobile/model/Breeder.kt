package com.weyr_associates.animaltrakkerfarmmobile.model

object Breeder {

    const val TYPE_ID_CONTACT = 0
    const val TYPE_ID_COMPANY = 1

    enum class Type(val code: Int) {
        CONTACT(TYPE_ID_CONTACT),
        COMPANY(TYPE_ID_COMPANY);

        companion object {
            fun fromCode(code: Int): Type = when(code) {
                CONTACT.code -> CONTACT
                COMPANY.code -> COMPANY
                else -> throw IllegalArgumentException(
                    "$code is not a valid code for ${Type::class.simpleName}."
                )
            }
        }
    }
}
