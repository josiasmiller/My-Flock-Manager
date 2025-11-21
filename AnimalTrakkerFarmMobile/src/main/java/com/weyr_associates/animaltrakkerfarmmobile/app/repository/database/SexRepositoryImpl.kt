package com.weyr_associates.animaltrakkerfarmmobile.app.repository.database

import com.weyr_associates.animaltrakkerfarmmobile.app.repository.SexRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseHandler
import com.weyr_associates.animaltrakkerfarmmobile.database.queries.querySexById
import com.weyr_associates.animaltrakkerfarmmobile.database.queries.querySexesForSpecies
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.Sex

class SexRepositoryImpl(private val databaseHandler: DatabaseHandler) : SexRepository {

    override fun querySexesForSpeciesId(speciesId: EntityId): List<Sex> {
        return databaseHandler.readableDatabase.querySexesForSpecies(speciesId)
    }

    override fun querySexById(id: EntityId): Sex? {
        return databaseHandler.readableDatabase.querySexById(id)
    }
}
