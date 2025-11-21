package com.weyr_associates.animaltrakkerfarmmobile.app.repository.database

import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseHandler
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.BreedTable
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readAllItems
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readFirstItem
import com.weyr_associates.animaltrakkerfarmmobile.model.Breed
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.BreedRepository
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId

class BreedRepositoryImpl(private val databaseHandler: DatabaseHandler) : BreedRepository {

    companion object {

        private val SQL_QUERY_BREEDS get() =
            """SELECT * FROM ${BreedTable.NAME}
                WHERE ${BreedTable.Columns.SPECIES_ID} = ?
                ORDER BY ${BreedTable.Columns.ORDER}"""

        private val SQL_QUERY_BREED_BY_ID get() =
            """SELECT * FROM ${BreedTable.NAME}
                WHERE ${BreedTable.Columns.ID} = ?"""
    }

    override fun queryBreedsForSpecies(speciesId: EntityId): List<Breed> {
        return databaseHandler.readableDatabase.rawQuery(SQL_QUERY_BREEDS, arrayOf(speciesId.toString()))
            .use { it.readAllItems(BreedTable::breedFromCursor) }
    }

    override fun queryBreed(id: EntityId): Breed? {
        return databaseHandler.readableDatabase.rawQuery(SQL_QUERY_BREED_BY_ID, arrayOf(id.toString()))
            .use { it.readFirstItem(BreedTable::breedFromCursor) }
    }
}
