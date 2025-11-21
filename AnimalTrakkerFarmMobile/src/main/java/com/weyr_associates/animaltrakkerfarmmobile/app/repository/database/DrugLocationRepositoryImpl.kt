package com.weyr_associates.animaltrakkerfarmmobile.app.repository.database

import com.weyr_associates.animaltrakkerfarmmobile.app.repository.DrugLocationRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseHandler
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.DrugLocationTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.DrugLocationTable.drugLocationFromCursor
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readAllItems
import com.weyr_associates.animaltrakkerfarmmobile.model.DrugLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DrugLocationRepositoryImpl(private val databaseHandler: DatabaseHandler) : DrugLocationRepository {
    override suspend fun queryAllDrugLocations(): List<DrugLocation> {
        return withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.rawQuery(
                DrugLocationTable.Sql.QUERY_ALL_DRUG_LOCATIONS,
                arrayOf()
            )?.use { cursor ->
                cursor.readAllItems(::drugLocationFromCursor)
            } ?: emptyList()
        }
    }
}
