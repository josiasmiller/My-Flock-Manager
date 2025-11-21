package com.weyr_associates.animaltrakkerfarmmobile.app.repository.database

import android.database.Cursor
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.ContactRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseHandler
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.ContactTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.ContactVeterinarianTable
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readAllItems
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readFirstItem
import com.weyr_associates.animaltrakkerfarmmobile.model.Contact
import com.weyr_associates.animaltrakkerfarmmobile.model.VetContact

class ContactRepositoryImpl(private val databaseHandler: DatabaseHandler) : ContactRepository {

    companion object {
        private val SQL_QUERY_CONTACTS get() =
            """SELECT * FROM ${ContactTable.NAME}
                ORDER BY ${ContactTable.Columns.LAST_NAME} ASC"""

        private val SQL_QUERY_CONTACT_BY_ID get() =
            """SELECT * FROM ${ContactTable.NAME}
                WHERE ${ContactTable.Columns.ID} = ?"""

        private val SQL_QUERY_VETERINARIANS get() =
            """SELECT
                ${ContactVeterinarianTable.NAME}.${ContactVeterinarianTable.Columns.ID},
                ${ContactTable.NAME}.*
                FROM ${ContactTable.NAME}
                JOIN ${ContactVeterinarianTable.NAME}
                ON ${ContactTable.NAME}.${ContactTable.Columns.ID} = ${ContactVeterinarianTable.NAME}.${ContactVeterinarianTable.Columns.CONTACT_ID}
                ORDER BY ${ContactTable.Columns.LAST_NAME} ASC"""

        private fun vetContactFromCursor(cursor: Cursor): VetContact {
            return VetContact(
                id = cursor.getEntityId(ContactVeterinarianTable.Columns.ID),
                contact = ContactTable.contactFromCursor(cursor)
            )
        }
    }

    override fun queryContacts(): List<Contact> {
        return databaseHandler.readableDatabase.rawQuery(SQL_QUERY_CONTACTS, null)
            .use { it.readAllItems(ContactTable::contactFromCursor) }
    }

    override fun queryVeterinarians(): List<VetContact> {
        return databaseHandler.readableDatabase.rawQuery(SQL_QUERY_VETERINARIANS, null)
            .use { it.readAllItems(::vetContactFromCursor) }
    }

    override fun queryContact(id: Int): Contact? {
        return databaseHandler.readableDatabase.rawQuery(SQL_QUERY_CONTACT_BY_ID, arrayOf(id.toString()))
            .use { it.readFirstItem(ContactTable::contactFromCursor) }
    }
}
