package com.weyr_associates.animaltrakkerfarmmobile.app.repository.database

import com.weyr_associates.animaltrakkerfarmmobile.app.repository.TissueSampleContainerTypeRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseHandler
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.TissueSampleContainerTypeTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.TissueSampleContainerTypeTable.Sql.SQL_QUERY_TISSUE_SAMPLE_CONTAINER_TYPES
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.TissueSampleContainerTypeTable.Sql.SQL_QUERY_TISSUE_SAMPLE_CONTAINER_TYPE_BY_ID
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readAllItems
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readFirstItem
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.TissueSampleContainerType

class TissueSampleContainerTypeRepositoryImpl(private val databaseHandler: DatabaseHandler) : TissueSampleContainerTypeRepository {
    override fun queryTissueSampleContainerTypes(): List<TissueSampleContainerType> {
        return databaseHandler.readableDatabase.rawQuery(
            SQL_QUERY_TISSUE_SAMPLE_CONTAINER_TYPES,
            emptyArray()
        ).use { cursor ->
            return cursor.readAllItems(TissueSampleContainerTypeTable::tissueSampleContainerTypeFrom)
        }
    }

    override fun queryTissueSampleContainerTypeById(id: EntityId): TissueSampleContainerType? {
        return databaseHandler.readableDatabase.rawQuery(
            SQL_QUERY_TISSUE_SAMPLE_CONTAINER_TYPE_BY_ID,
            arrayOf(id.toString())
        ).use { cursor ->
            return cursor.readFirstItem(TissueSampleContainerTypeTable::tissueSampleContainerTypeFrom)
        }
    }
}
