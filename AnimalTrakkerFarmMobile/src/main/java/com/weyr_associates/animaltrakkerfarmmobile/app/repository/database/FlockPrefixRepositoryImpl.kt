package com.weyr_associates.animaltrakkerfarmmobile.app.repository.database

import com.weyr_associates.animaltrakkerfarmmobile.app.repository.FlockPrefixRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseHandler
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.FlockPrefixTable
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readFirstItem
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.FlockPrefix
import com.weyr_associates.animaltrakkerfarmmobile.model.Owner

class FlockPrefixRepositoryImpl(private val databaseHandler: DatabaseHandler) : FlockPrefixRepository {

    override fun queryFlockPrefixById(id: EntityId): FlockPrefix? {
        return databaseHandler.readableDatabase.rawQuery(
            FlockPrefixTable.Sql.QUERY_FLOCK_PREFIX_BY_ID,
            arrayOf(id.toString())
        ).use { cursor ->
            cursor.readFirstItem(FlockPrefixTable::flockPrefixFromCursor)
        }
    }

    override fun queryFlockPrefixByOwner(
        ownerId: EntityId,
        ownerType: Owner.Type,
        registryCompanyId: EntityId
    ): FlockPrefix? {
        return databaseHandler.readableDatabase.rawQuery(
            FlockPrefixTable.Sql.QUERY_FLOCK_PREFIX_BY_OWNER,
            arrayOf(ownerId.toString(), ownerType.toString(), registryCompanyId.toString())
        ).use { cursor ->
            cursor.readFirstItem(FlockPrefixTable::flockPrefixFromCursor)
        }
    }
}
