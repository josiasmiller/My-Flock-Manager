package com.weyr_associates.animaltrakkerfarmmobile.app.repository.database

import com.weyr_associates.animaltrakkerfarmmobile.app.repository.TissueTestRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseHandler
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.TissueTestTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.TissueTestTable.Sql.SQL_QUERY_TISSUE_TESTS
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.TissueTestTable.Sql.SQL_QUERY_TISSUE_TEST_BY_ID
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readAllItems
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readFirstItem
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.TissueTest

class TissueTestRepositoryImpl(private val databaseHandler: DatabaseHandler) : TissueTestRepository {

    override fun queryTissueTests(): List<TissueTest> {
        return databaseHandler.readableDatabase.rawQuery(
            SQL_QUERY_TISSUE_TESTS,
            emptyArray()
        ).use { cursor ->
            cursor.readAllItems(TissueTestTable::tissueTestFrom)
        }
    }

    override fun queryTissueTestById(id: EntityId): TissueTest? {
        return databaseHandler.readableDatabase.rawQuery(
            SQL_QUERY_TISSUE_TEST_BY_ID,
            arrayOf(id.toString())
        ).use { cursor ->
            cursor.readFirstItem(TissueTestTable::tissueTestFrom)
        }
    }
}
