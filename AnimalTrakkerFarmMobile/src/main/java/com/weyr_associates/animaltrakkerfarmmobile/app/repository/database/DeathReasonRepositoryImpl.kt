package com.weyr_associates.animaltrakkerfarmmobile.repository.database

import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseHandler
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.DeathReasonTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.DeathReasonTable.Sql.QUERY_DEATH_REASONS_DEFAULTS_ONLY
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.DeathReasonTable.Sql.QUERY_DEATH_REASONS_FOR_COMPANY_USER_AND_DEFAULTS
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.DeathReasonTable.Sql.QUERY_DEATH_REASONS_FOR_CONTACT_USER_AND_DEFAULTS
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readAllItems
import com.weyr_associates.animaltrakkerfarmmobile.model.DeathReason
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.UserType
import com.weyr_associates.animaltrakkerfarmmobile.repository.DeathReasonRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeathReasonRepositoryImpl(
    private val databaseHandler: DatabaseHandler
) : DeathReasonRepository {
    override suspend fun queryDefaultDeathReasons(): List<DeathReason> {
        return withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.rawQuery(
                QUERY_DEATH_REASONS_DEFAULTS_ONLY,
                emptyArray()
            ).use { cursor ->
                cursor.readAllItems(DeathReasonTable::deathReasonFromCursor)
            }
        }
    }

    override suspend fun queryDeathReasonsByUser(userId: EntityId, userType: UserType): List<DeathReason> {
        return withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.rawQuery(
                queryForDeathReasonsByUserIdForUserType(userType),
                arrayOf(userId.toString())
            ).use { cursor ->
                cursor.readAllItems(DeathReasonTable::deathReasonFromCursor)
            }
        }
    }

    private fun queryForDeathReasonsByUserIdForUserType(userType: UserType): String {
        return when (userType) {
            UserType.CONTACT -> QUERY_DEATH_REASONS_FOR_CONTACT_USER_AND_DEFAULTS
            UserType.COMPANY -> QUERY_DEATH_REASONS_FOR_COMPANY_USER_AND_DEFAULTS
        }
    }
}
