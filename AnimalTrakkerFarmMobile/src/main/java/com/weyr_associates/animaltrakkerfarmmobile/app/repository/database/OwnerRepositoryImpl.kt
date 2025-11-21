package com.weyr_associates.animaltrakkerfarmmobile.app.repository.database

import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseHandler
import com.weyr_associates.animaltrakkerfarmmobile.database.sql.OwnerUnion
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readAllItems
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readFirstItem
import com.weyr_associates.animaltrakkerfarmmobile.model.Owner
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.OwnerRepository
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId

class OwnerRepositoryImpl(private val databaseHandler: DatabaseHandler) : OwnerRepository {

    override fun queryOwners(): List<Owner> {
        return databaseHandler.readableDatabase.rawQuery(OwnerUnion.Sql.SQL_QUERY_ALL_OWNERS, null)
            .use { it.readAllItems(OwnerUnion::ownerFromCursor) }
    }

    override fun queryOwner(ownerId: EntityId, typeCode: Int): Owner? {
        return databaseHandler.readableDatabase.rawQuery(
            OwnerUnion.Sql.SQL_QUERY_OWNER_BY_ID_AND_TYPE,
            arrayOf(ownerId.toString(), typeCode.toString())
        ).use { it.readFirstItem(OwnerUnion::ownerFromCursor) }
    }
}
