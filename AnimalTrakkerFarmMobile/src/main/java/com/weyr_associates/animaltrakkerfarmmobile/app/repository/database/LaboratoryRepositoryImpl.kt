package com.weyr_associates.animaltrakkerfarmmobile.app.repository.database

import android.database.Cursor
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.LaboratoryRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseHandler
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.CompanyLaboratoryTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.CompanyTable
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getInt
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getOptString
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getString
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readAllItems
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readFirstItem
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.Laboratory

class LaboratoryRepositoryImpl(private val databaseHandler: DatabaseHandler) : LaboratoryRepository {

    companion object {
        private val SQL_BASE_QUERY_LABORATORY_TABLE get() =
            """SELECT ${CompanyLaboratoryTable.NAME}.${CompanyLaboratoryTable.Columns.ID}, 
                    ${CompanyTable.NAME}.${CompanyTable.Columns.ID}, 
                    ${CompanyTable.NAME}.${CompanyTable.Columns.NAME}, 
                    ${CompanyLaboratoryTable.NAME}.${CompanyLaboratoryTable.Columns.LICENSE_NUMBER},
                    ${CompanyLaboratoryTable.NAME}.${CompanyLaboratoryTable.Columns.ORDER}
                FROM ${CompanyLaboratoryTable.NAME}
                INNER JOIN ${CompanyTable.NAME}
                    ON ${CompanyTable.NAME}.${CompanyTable.Columns.ID} = ${CompanyLaboratoryTable.NAME}.${CompanyLaboratoryTable.Columns.COMPANY_ID}"""

        val SQL_QUERY_LABORATORIES get() =
            """$SQL_BASE_QUERY_LABORATORY_TABLE
                ORDER BY ${CompanyLaboratoryTable.NAME}.${CompanyLaboratoryTable.Columns.ORDER}"""

        val SQL_QUERY_LABORATORY_BY_ID get() =
            """$SQL_BASE_QUERY_LABORATORY_TABLE
                WHERE ${CompanyLaboratoryTable.NAME}.${CompanyLaboratoryTable.Columns.ID} = ?"""

        val SQL_QUERY_LABORATORY_BY_COMPANY_ID get() =
            """$SQL_BASE_QUERY_LABORATORY_TABLE
                WHERE ${CompanyTable.NAME}.${CompanyTable.Columns.ID} = ?"""

        fun laboratoryFrom(cursor: Cursor): Laboratory {
            return Laboratory(
                id = cursor.getEntityId(CompanyLaboratoryTable.Columns.ID),
                companyId = cursor.getInt(CompanyLaboratoryTable.Columns.COMPANY_ID),
                name = cursor.getString(CompanyTable.Columns.NAME),
                licenseNumber = cursor.getOptString(CompanyLaboratoryTable.Columns.LICENSE_NUMBER),
                order = cursor.getInt(CompanyLaboratoryTable.Columns.ORDER)
            )
        }
    }

    override fun queryLaboratories(): List<Laboratory> {
        return databaseHandler.readableDatabase.rawQuery(
            SQL_QUERY_LABORATORIES,
            emptyArray()
        ).use { cursor ->
            return cursor.readAllItems(::laboratoryFrom)
        }
    }

    override fun queryLaboratoryById(id: EntityId): Laboratory? {
        return databaseHandler.readableDatabase.rawQuery(
            SQL_QUERY_LABORATORY_BY_ID,
            arrayOf(id.toString())
        ).use { cursor ->
            cursor.readFirstItem(::laboratoryFrom)
        }
    }

    override fun queryLaboratoryByCompanyId(companyId: EntityId): Laboratory? {
        return databaseHandler.readableDatabase.rawQuery(
            SQL_QUERY_LABORATORY_BY_COMPANY_ID,
            arrayOf(companyId.toString())
        ).use { cursor ->
            cursor.readFirstItem(::laboratoryFrom)
        }
    }
}
