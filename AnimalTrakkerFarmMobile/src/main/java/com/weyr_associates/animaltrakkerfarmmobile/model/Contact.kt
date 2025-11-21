package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Contact(
    override val id: EntityId,
    val firstName: String,
    val middleName: String?,
    val lastName: String,
    val titleId: EntityId?
) : Parcelable, HasIdentity, HasName {

    override val name: String
        get() = firstAndLastName

    val firstAndLastName: String
        get() = "$firstName $lastName"
}

@Parcelize
data class VetContact(
    override val id: EntityId,
    val contact: Contact
) : Parcelable, HasIdentity, HasName {
    override val name: String
        get() = contact.name
}
