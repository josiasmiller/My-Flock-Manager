package com.weyr_associates.animaltrakkerfarmmobile.app.repository.database

import com.weyr_associates.animaltrakkerfarmmobile.app.repository.BirthTypeRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseHandler
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.BirthTypeTable
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readAllItems
import com.weyr_associates.animaltrakkerfarmmobile.model.BirthType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BirthTypeRepositoryImpl(private val databaseHandler: DatabaseHandler) : BirthTypeRepository {

    override suspend fun queryBirthTypes(): List<BirthType> {
        return withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.rawQuery(
                BirthTypeTable.Sql.QUERY_ALL_BIRTH_TYPES,
                emptyArray()
            ).use { cursor ->
                cursor.readAllItems(BirthTypeTable::birthTypeFromCursor)
            }
        }
    }
}
