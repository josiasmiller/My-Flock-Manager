package com.weyr_associates.animaltrakkerfarmmobile.app.repository.database

import com.weyr_associates.animaltrakkerfarmmobile.app.repository.GeneticsRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseHandler
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.GeneticCoatColorTable
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readAllItems
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.GeneticCoatColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeneticsRepositoryImpl(private val databaseHandler: DatabaseHandler) : GeneticsRepository {
    override suspend fun queryCoatColorsByRegistry(registryCompanyId: EntityId): List<GeneticCoatColor> {
        return withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.rawQuery(
                GeneticCoatColorTable.Sql.QUERY_COAT_COLORS_BY_REGISTRY_COMPANY_ID,
                arrayOf(registryCompanyId.toString())
            ).use { cursor ->
                cursor.readAllItems(GeneticCoatColorTable::geneticCoatColorFromCursor)
            }
        }
    }
}
