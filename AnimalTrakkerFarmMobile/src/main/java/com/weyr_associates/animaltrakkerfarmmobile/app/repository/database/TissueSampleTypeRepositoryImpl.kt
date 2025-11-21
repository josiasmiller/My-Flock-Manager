package com.weyr_associates.animaltrakkerfarmmobile.app.repository.database

import com.weyr_associates.animaltrakkerfarmmobile.app.repository.TissueSampleTypeRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseHandler
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.TissueSampleTypeTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.TissueSampleTypeTable.Sql.SQL_QUERY_TISSUE_SAMPLE_TYPES
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.TissueSampleTypeTable.Sql.SQL_QUERY_TISSUE_SAMPLE_TYPE_BY_ID
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readAllItems
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readFirstItem
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.TissueSampleType

class TissueSampleTypeRepositoryImpl(private val databaseHandler: DatabaseHandler) : TissueSampleTypeRepository {

    override fun queryTissueSampleTypes(): List<TissueSampleType> {
        return databaseHandler.readableDatabase.rawQuery(
            SQL_QUERY_TISSUE_SAMPLE_TYPES, emptyArray()
        ).use { cursor ->
            return cursor.readAllItems(TissueSampleTypeTable::tissueSampleTypeFrom)
        }
    }

    override fun queryTissueSampleTypeById(id: EntityId): TissueSampleType? {
        return databaseHandler.readableDatabase.rawQuery(
            SQL_QUERY_TISSUE_SAMPLE_TYPE_BY_ID, arrayOf(id.toString())
        ).use { cursor ->
            return cursor.readFirstItem(TissueSampleTypeTable::tissueSampleTypeFrom)
        }
    }
}