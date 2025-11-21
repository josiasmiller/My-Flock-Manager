package com.weyr_associates.animaltrakkerfarmmobile.app.repository.database

import com.weyr_associates.animaltrakkerfarmmobile.app.repository.RearTypeRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseHandler
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.BirthTypeTable
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readAllItems
import com.weyr_associates.animaltrakkerfarmmobile.model.RearType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RearTypeRepositoryImpl(private val databaseHandler: DatabaseHandler) : RearTypeRepository {
    override suspend fun queryAllRearTypes(): List<RearType> {
        return withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.rawQuery(
                //At present birth and rear type
                //are represented by the same table
                BirthTypeTable.Sql.QUERY_ALL_BIRTH_TYPES,
                emptyArray()
            ).use { cursor ->
                cursor.readAllItems(BirthTypeTable::rearTypeFromCursor)
            }
        }
    }
}
