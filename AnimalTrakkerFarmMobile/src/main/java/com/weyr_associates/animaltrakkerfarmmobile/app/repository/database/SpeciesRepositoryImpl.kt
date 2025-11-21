package com.weyr_associates.animaltrakkerfarmmobile.app.repository.database

import com.weyr_associates.animaltrakkerfarmmobile.app.repository.SpeciesRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseHandler
import com.weyr_associates.animaltrakkerfarmmobile.database.queries.queryAllSpecies
import com.weyr_associates.animaltrakkerfarmmobile.database.queries.querySpeciesById
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.Species
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SpeciesRepositoryImpl(private val databaseHandler: DatabaseHandler) : SpeciesRepository {

    override suspend fun queryAllSpecies(): List<Species> {
        return withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.queryAllSpecies()
        }
    }

    override suspend fun querySpeciesById(id: EntityId): Species? {
        return withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.querySpeciesById(id)
        }
    }
}
