package com.weyr_associates.animaltrakkerfarmmobile.app.repository.database

import com.weyr_associates.animaltrakkerfarmmobile.app.repository.PremiseRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseHandler
import com.weyr_associates.animaltrakkerfarmmobile.database.queries.queryPhysicalPremiseForOwner
import com.weyr_associates.animaltrakkerfarmmobile.database.queries.queryPhysicalPremisesForOwner
import com.weyr_associates.animaltrakkerfarmmobile.database.queries.queryPremiseById
import com.weyr_associates.animaltrakkerfarmmobile.database.queries.queryPremiseNicknameForOwner
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.Owner
import com.weyr_associates.animaltrakkerfarmmobile.model.Premise
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PremiseRepositoryImpl(private val databaseHandler: DatabaseHandler) : PremiseRepository {

    override suspend fun queryPremiseById(premiseId: EntityId): Premise? {
        return withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.queryPremiseById(premiseId)
        }
    }

    override suspend fun queryPremiseNickname(premiseId: EntityId, ownerId: EntityId, ownerType: Owner.Type): String? {
        return withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.queryPremiseNicknameForOwner(premiseId, ownerId, ownerType)
        }
    }

    override suspend fun queryPhysicalPremiseForOwner(ownerId: EntityId, ownerType: Owner.Type): Premise? {
        return withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.queryPhysicalPremiseForOwner(ownerId, ownerType)
        }
    }

    override suspend fun queryPhysicalPremisesForOwner(ownerId: EntityId, ownerType: Owner.Type): List<Premise> {
        return withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.queryPhysicalPremisesForOwner(ownerId, ownerType)
        }
    }
}
