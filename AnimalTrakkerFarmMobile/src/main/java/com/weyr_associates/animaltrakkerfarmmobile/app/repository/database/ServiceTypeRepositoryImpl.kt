package com.weyr_associates.animaltrakkerfarmmobile.app.repository.database

import android.database.Cursor
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.ServiceTypeRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseHandler
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.ServiceTypeTable
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readAllItems
import com.weyr_associates.animaltrakkerfarmmobile.database.sql.readServiceType
import com.weyr_associates.animaltrakkerfarmmobile.model.ServiceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ServiceTypeRepositoryImpl(private val databaseHandler: DatabaseHandler) : ServiceTypeRepository {

    override suspend fun queryAllServiceTypes(): List<ServiceType> {
        return withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.rawQuery(
                ServiceTypeTable.Sql.QUERY_ALL_SERVICE_TYPES,
                emptyArray()
            ).use { cursor ->
                cursor.readAllItems(Cursor::readServiceType)
            }
        }
    }
}
