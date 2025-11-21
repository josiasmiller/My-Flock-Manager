package com.weyr_associates.animaltrakkerfarmmobile.app.repository

import com.weyr_associates.animaltrakkerfarmmobile.model.Contact
import com.weyr_associates.animaltrakkerfarmmobile.model.VetContact

interface ContactRepository {
    fun queryContacts(): List<Contact>
    fun queryVeterinarians(): List<VetContact>
    fun queryContact(id: Int): Contact?
}
