package com.weyr_associates.animaltrakkerfarmmobile.app.repository.database

import com.weyr_associates.animaltrakkerfarmmobile.app.repository.UnitOfMeasureRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseHandler
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.UnitsTable
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readAllItems
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readFirstItem
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.UnitOfMeasure
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UnitOfMeasureRepositoryImpl(private val databaseHandler: DatabaseHandler) : UnitOfMeasureRepository {
    override suspend fun queryUnitsOfMeasure(): List<UnitOfMeasure> {
        return withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.rawQuery(
                UnitsTable.Sql.QUERY_UNITS_OF_MEASURE,
                emptyArray()
            ).use { cursor ->
                cursor.readAllItems(UnitsTable::unitOfMeasureFromCursor)
            }
        }
    }

    override suspend fun queryUnitsOfMeasureByType(typeId: EntityId): List<UnitOfMeasure> {
        return withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.rawQuery(
                UnitsTable.Sql.QUERY_UNITS_OF_MEASURE_BY_TYPE,
                arrayOf(typeId.toString())
            ).use { cursor ->
                cursor.readAllItems(UnitsTable::unitOfMeasureFromCursor)
            }
        }
    }

    override suspend fun queryUnitOfMeasureById(id: EntityId): UnitOfMeasure? {
        return withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.rawQuery(
                UnitsTable.Sql.QUERY_UNIT_OF_MEASURE_BY_ID,
                arrayOf(id.toString())
            ).use { cursor ->
                cursor.readFirstItem(UnitsTable::unitOfMeasureFromCursor)
            }
        }
    }
}
