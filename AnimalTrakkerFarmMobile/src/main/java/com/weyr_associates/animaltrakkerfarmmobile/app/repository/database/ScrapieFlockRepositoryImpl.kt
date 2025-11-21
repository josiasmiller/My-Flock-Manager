package com.weyr_associates.animaltrakkerfarmmobile.app.repository.database

import com.weyr_associates.animaltrakkerfarmmobile.app.repository.ScrapieFlockRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseHandler
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.ScrapieFlockNumberTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.ScrapieFlockOwnerTable
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readFirstItem
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.Owner
import com.weyr_associates.animaltrakkerfarmmobile.model.ScrapieFlockNumber

class ScrapieFlockRepositoryImpl(val databaseHandler: DatabaseHandler) : ScrapieFlockRepository {

    override fun queryActiveScrapieFlockNumberForOwner(
        ownerId: EntityId,
        ownerType: Owner.Type
    ): ScrapieFlockNumber? {
        return databaseHandler.readableDatabase.rawQuery(
            when (ownerType) {
                Owner.Type.CONTACT -> Sql.QUERY_ACTIVE_SCRAPIE_FLOCK_NUMBER_FOR_CONTACT_OWNER
                Owner.Type.COMPANY -> Sql.QUERY_ACTIVE_SCRAPIE_FLOCK_NUMBER_FOR_COMPANY_OWNER
            },
            arrayOf(ownerId.toString())
        ).use { cursor ->
            cursor.readFirstItem(ScrapieFlockNumberTable::scrapieFlockNumberFromCursor)
        }
    }

    override fun queryActiveScrapieFlockNumberFromNumber(scrapieFlockNumber: String): ScrapieFlockNumber? {
        return databaseHandler.readableDatabase.rawQuery(
            ScrapieFlockNumberTable.Sql.QUERY_SCRAPIE_FLOCK_NUMBER_FROM_NUMBER,
            arrayOf(scrapieFlockNumber)
        ).use { cursor ->
            cursor.readFirstItem(ScrapieFlockNumberTable::scrapieFlockNumberFromCursor)
        }
    }


    private object Sql {
        val QUERY_ACTIVE_SCRAPIE_FLOCK_NUMBER_FOR_CONTACT_OWNER get() =
            """SELECT ${ScrapieFlockNumberTable.NAME}.*
                FROM ${ScrapieFlockNumberTable.NAME}
                JOIN ${ScrapieFlockOwnerTable.NAME} ON
	                ${ScrapieFlockNumberTable.NAME}.${ScrapieFlockNumberTable.Columns.ID} = 
                    ${ScrapieFlockOwnerTable.NAME}.${ScrapieFlockOwnerTable.Columns.SCRAPIE_FLOCK_ID}
                WHERE ${ScrapieFlockOwnerTable.NAME}.${ScrapieFlockOwnerTable.Columns.OWNER_CONTACT_ID} = ?
                AND ${ScrapieFlockOwnerTable.NAME}.${ScrapieFlockOwnerTable.Columns.OWNER_COMPANY_ID} IS NULL
                AND ${ScrapieFlockOwnerTable.NAME}.${ScrapieFlockOwnerTable.Columns.END_USE_DATE} IS NULL
                LIMIT 1"""

        val QUERY_ACTIVE_SCRAPIE_FLOCK_NUMBER_FOR_COMPANY_OWNER get() =
            """SELECT ${ScrapieFlockNumberTable.NAME}.*
                FROM ${ScrapieFlockNumberTable.NAME}
                JOIN ${ScrapieFlockOwnerTable.NAME} ON
	                ${ScrapieFlockNumberTable.NAME}.${ScrapieFlockNumberTable.Columns.ID} = 
                    ${ScrapieFlockOwnerTable.NAME}.${ScrapieFlockOwnerTable.Columns.SCRAPIE_FLOCK_ID}
                WHERE ${ScrapieFlockOwnerTable.NAME}.${ScrapieFlockOwnerTable.Columns.OWNER_COMPANY_ID} = ?
                AND ${ScrapieFlockOwnerTable.NAME}.${ScrapieFlockOwnerTable.Columns.OWNER_CONTACT_ID} IS NULL
                AND ${ScrapieFlockOwnerTable.NAME}.${ScrapieFlockOwnerTable.Columns.END_USE_DATE} IS NULL
                LIMIT 1"""
    }
}
