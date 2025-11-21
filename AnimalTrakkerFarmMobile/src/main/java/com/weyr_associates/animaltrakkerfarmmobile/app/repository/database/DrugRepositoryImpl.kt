package com.weyr_associates.animaltrakkerfarmmobile.app.repository.database

import android.content.ContentValues
import android.database.Cursor
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.DrugRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseHandler
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.ContactTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.ContactVeterinarianTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.DrugLotTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.DrugTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.DrugTypeTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.DrugWithdrawalTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.OffLabelDrugTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.SpeciesTable
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Sql
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getBoolean
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getOptEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getOptInt
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getOptString
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getString
import com.weyr_associates.animaltrakkerfarmmobile.database.core.insertWithPKOrThrow
import com.weyr_associates.animaltrakkerfarmmobile.database.core.put
import com.weyr_associates.animaltrakkerfarmmobile.database.core.putNull
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readAllItems
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readFirstItem
import com.weyr_associates.animaltrakkerfarmmobile.model.Drug
import com.weyr_associates.animaltrakkerfarmmobile.model.DrugApplicationInfo
import com.weyr_associates.animaltrakkerfarmmobile.model.DrugDosageSpec
import com.weyr_associates.animaltrakkerfarmmobile.model.DrugType
import com.weyr_associates.animaltrakkerfarmmobile.model.DrugWithdrawalSpec
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.OffLabelDrugDose
import com.weyr_associates.animaltrakkerfarmmobile.model.OffLabelDrugSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime

class DrugRepositoryImpl(private val databaseHandler: DatabaseHandler) : DrugRepository {
    override suspend fun queryDrugTypes(): List<DrugType> {
        return withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.rawQuery(
                DrugTypeTable.Sql.QUERY_ALL_DRUG_TYPES,
                emptyArray()
            ).use { cursor ->
                cursor.readAllItems(DrugTypeTable::drugTypeFromCursor)
            }
        }
    }

    override suspend fun queryAllDrugs(): List<Drug> {
        return withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.rawQuery(
                DrugTable.Sql.QUERY_ALL_DRUGS,
                emptyArray()
            ).use { cursor ->
                cursor.readAllItems(DrugTable::drugFromCursor)
            }
        }
    }

    override suspend fun queryDrugsByType(drugTypeId: EntityId): List<Drug> {
        return withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.rawQuery(
                DrugTable.Sql.QUERY_DRUGS_BY_TYPE,
                arrayOf(drugTypeId.toString())
            ).use { cursor ->
                cursor.readAllItems(DrugTable::drugFromCursor)
            }
        }
    }

    override suspend fun queryDrugById(drugId: EntityId): Drug? {
        return withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.rawQuery(
                DrugTable.Sql.QUERY_DRUG_BY_ID,
                arrayOf(drugId.toString())
            ).use { cursor ->
                cursor.readFirstItem(DrugTable::drugFromCursor)
            }
        }
    }

    override suspend fun queryAvailableDrugsByType(drugTypeId: EntityId): List<DrugApplicationInfo> {
        return withContext(Dispatchers.IO) {
            val drugApplicationInfo = databaseHandler.readableDatabase.rawQuery(
                SQL_QUERY_AVAILABLE_DRUGS_BY_TYPE,
                arrayOf(drugTypeId.toString())
            ).use { cursor ->
                cursor.readAllItems(::drugApplicationInfoFromCursor)
            }
            drugApplicationInfo.map {
                it.copy(drugDosageSpecs = queryDrugDosageSpecs(it.drugId))
            }
        }
    }

    override suspend fun queryDrugDosageSpecForSpecies(drugId: EntityId, speciesId: EntityId): DrugDosageSpec? {
        return withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.rawQuery(
                SQL_QUERY_DRUG_DOSAGE_SPEC_FOR_DRUG_FOR_SPECIES,
                arrayOf(drugId.toString(), speciesId.toString())
            ).use { cursor ->
                cursor.readFirstItem(::drugDosageSpecFromCursor)
            }
        }
    }

    override suspend fun queryOffLabelDrugDoses(
        drugId: EntityId,
        currentDate: LocalDate
    ): List<OffLabelDrugDose> {
        return withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.rawQuery(
                SQL_QUERY_OFF_LABEL_DOSES_FOR_DRUG,
                arrayOf(drugId.toString(), Sql.formatDate(currentDate))
            ).use { cursor ->
                cursor.readAllItems(::offLabelDrugDoseFromCursor)
            }
        }
    }

    override suspend fun queryOffLabelDrugDose(offLabelDrugDoseId: EntityId): OffLabelDrugDose? {
        return withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.rawQuery(
                SQL_QUERY_OFF_LABEL_DOSE,
                arrayOf(offLabelDrugDoseId.toString())
            ).use { cursor ->
                cursor.readFirstItem(::offLabelDrugDoseFromCursor)
            }
        }
    }

    override suspend fun addDrug(
        drugTypeId: EntityId,
        tradeDrugName: String,
        genericDrugName: String,
        isRemovable: Boolean,
        speciesId: EntityId,
        officialDrugDose: String,
        userDrugDose: String,
        meatWithdrawalSpec: DrugWithdrawalSpec?,
        milkWithdrawalSpec: DrugWithdrawalSpec?,
        offLabelDrugSpec: OffLabelDrugSpec?,
        timeStamp: LocalDateTime
    ): EntityId {
        return databaseHandler.writableDatabase.run {
            val timeStampString = Sql.formatDateTime(timeStamp)
            beginTransaction()
            try {
                val drugId = insertWithPKOrThrow(
                    DrugTable,
                    null,
                    ContentValues().apply {
                        put(DrugTable.Columns.TYPE_ID, drugTypeId)
                        put(DrugTable.Columns.TRADE_NAME, tradeDrugName)
                        put(DrugTable.Columns.GENERIC_NAME, genericDrugName)
                        put(DrugTable.Columns.IS_REMOVABLE, Sql.booleanValue(isRemovable))
                        put(DrugTable.Columns.CREATED, timeStampString)
                        put(DrugTable.Columns.MODIFIED, timeStampString)
                    }
                )
                insertWithPKOrThrow(
                    DrugWithdrawalTable,
                    null,
                    ContentValues().apply {
                        put(DrugWithdrawalTable.Columns.DRUG_ID, drugId)
                        put(DrugWithdrawalTable.Columns.SPECIES_ID, speciesId)
                        put(DrugWithdrawalTable.Columns.OFFICIAL_DRUG_DOSAGE, officialDrugDose)
                        put(DrugWithdrawalTable.Columns.USER_DRUG_DOSAGE, userDrugDose)
                        if (meatWithdrawalSpec != null) {
                            put(DrugWithdrawalTable.Columns.MEAT_WITHDRAWAL, meatWithdrawalSpec.withdrawal)
                            put(DrugWithdrawalTable.Columns.USER_MEAT_WITHDRAWAL, meatWithdrawalSpec.userWithdrawal)
                            put(DrugWithdrawalTable.Columns.MEAT_WITHDRAWAL_UNITS_ID, meatWithdrawalSpec.withdrawalUnitsId)
                        } else {
                            putNull(DrugWithdrawalTable.Columns.MEAT_WITHDRAWAL)
                            putNull(DrugWithdrawalTable.Columns.USER_MEAT_WITHDRAWAL)
                            putNull(DrugWithdrawalTable.Columns.MEAT_WITHDRAWAL_UNITS_ID)
                        }
                        if (milkWithdrawalSpec != null) {
                            put(DrugWithdrawalTable.Columns.MILK_WITHDRAWAL, milkWithdrawalSpec.withdrawal)
                            put(DrugWithdrawalTable.Columns.USER_MILK_WITHDRAWAL, milkWithdrawalSpec.userWithdrawal)
                            put(DrugWithdrawalTable.Columns.MILK_WITHDRAWAL_UNITS_ID, milkWithdrawalSpec.withdrawalUnitsId)
                        } else {
                            putNull(DrugWithdrawalTable.Columns.MILK_WITHDRAWAL)
                            putNull(DrugWithdrawalTable.Columns.USER_MILK_WITHDRAWAL)
                            putNull(DrugWithdrawalTable.Columns.MILK_WITHDRAWAL_UNITS_ID)
                        }
                        put(DrugWithdrawalTable.Columns.CREATED, timeStampString)
                        put(DrugWithdrawalTable.Columns.MODIFIED, timeStampString)
                    }
                )
                if (offLabelDrugSpec != null) {
                    insertWithPKOrThrow(
                        OffLabelDrugTable,
                        null,
                        ContentValues().apply {
                            put(OffLabelDrugTable.Columns.DRUG_ID, drugId)
                            put(OffLabelDrugTable.Columns.SPECIES_ID, speciesId)
                            put(OffLabelDrugTable.Columns.OFF_LABEL_VET_CONTACT_ID, offLabelDrugSpec.veterinarianContactId)
                            put(OffLabelDrugTable.Columns.OFF_LABEL_DRUG_DOSAGE, offLabelDrugSpec.drugDosage)
                            put(OffLabelDrugTable.Columns.OFF_LABEL_USE_START, Sql.formatDate(offLabelDrugSpec.useStartDate))
                            if (offLabelDrugSpec.useEndDate != null) {
                                put(OffLabelDrugTable.Columns.OFF_LABEL_USE_END, Sql.formatDate(offLabelDrugSpec.useEndDate))
                            } else {
                                putNull(OffLabelDrugTable.Columns.OFF_LABEL_USE_END)
                            }
                            put(OffLabelDrugTable.Columns.OFF_LABEL_NOTE, offLabelDrugSpec.note)
                            put(OffLabelDrugTable.Columns.CREATED, timeStampString)
                            put(OffLabelDrugTable.Columns.MODIFIED, timeStampString)
                        }
                    )
                }
                setTransactionSuccessful()
                drugId
            } finally {
                endTransaction()
            }
        }
    }

    override suspend fun addDrugDose(
        drugId: EntityId,
        speciesId: EntityId,
        officialDrugDose: String,
        userDrugDose: String,
        meatWithdrawalSpec: DrugWithdrawalSpec?,
        milkWithdrawalSpec: DrugWithdrawalSpec?,
        timeStamp: LocalDateTime
    ): EntityId {
        return databaseHandler.writableDatabase.run {
            val timeStampString = Sql.formatDateTime(timeStamp)
            beginTransaction()
            try {
                val drugDoseId = insertWithPKOrThrow(
                    DrugWithdrawalTable,
                    null,
                    ContentValues().apply {
                        put(DrugWithdrawalTable.Columns.DRUG_ID, drugId)
                        put(DrugWithdrawalTable.Columns.SPECIES_ID, speciesId)
                        put(DrugWithdrawalTable.Columns.OFFICIAL_DRUG_DOSAGE, officialDrugDose)
                        put(DrugWithdrawalTable.Columns.USER_DRUG_DOSAGE, userDrugDose)
                        if (meatWithdrawalSpec != null) {
                            put(DrugWithdrawalTable.Columns.MEAT_WITHDRAWAL, meatWithdrawalSpec.withdrawal)
                            put(DrugWithdrawalTable.Columns.USER_MEAT_WITHDRAWAL, meatWithdrawalSpec.userWithdrawal)
                            put(DrugWithdrawalTable.Columns.MEAT_WITHDRAWAL_UNITS_ID, meatWithdrawalSpec.withdrawalUnitsId)
                        } else {
                            putNull(DrugWithdrawalTable.Columns.MEAT_WITHDRAWAL)
                            putNull(DrugWithdrawalTable.Columns.USER_MEAT_WITHDRAWAL)
                            putNull(DrugWithdrawalTable.Columns.MEAT_WITHDRAWAL_UNITS_ID)
                        }
                        if (milkWithdrawalSpec != null) {
                            put(DrugWithdrawalTable.Columns.MILK_WITHDRAWAL, milkWithdrawalSpec.withdrawal)
                            put(DrugWithdrawalTable.Columns.USER_MILK_WITHDRAWAL, milkWithdrawalSpec.userWithdrawal)
                            put(DrugWithdrawalTable.Columns.MILK_WITHDRAWAL_UNITS_ID, milkWithdrawalSpec.withdrawalUnitsId)
                        } else {
                            putNull(DrugWithdrawalTable.Columns.MILK_WITHDRAWAL)
                            putNull(DrugWithdrawalTable.Columns.USER_MILK_WITHDRAWAL)
                            putNull(DrugWithdrawalTable.Columns.MILK_WITHDRAWAL_UNITS_ID)
                        }
                        put(DrugWithdrawalTable.Columns.CREATED, timeStampString)
                        put(DrugWithdrawalTable.Columns.MODIFIED, timeStampString)
                    }
                )
                setTransactionSuccessful()
                drugDoseId
            } finally {
                endTransaction()
            }
        }
    }

    override suspend fun addOffLabelDrugDose(
        drugId: EntityId,
        speciesId: EntityId,
        veterinarianContactId: EntityId,
        drugDosage: String,
        notes: String?,
        useStartDate: LocalDate,
        useEndDate: LocalDate?,
        timeStamp: LocalDateTime
    ): EntityId {
        return databaseHandler.writableDatabase.run {
            val timeStampString = Sql.formatDateTime(timeStamp)
            beginTransaction()
            try {
                val drugDoseId = insertWithPKOrThrow(
                    OffLabelDrugTable,
                    null,
                    ContentValues().apply {
                        put(OffLabelDrugTable.Columns.DRUG_ID, drugId)
                        put(OffLabelDrugTable.Columns.SPECIES_ID, speciesId)
                        put(OffLabelDrugTable.Columns.OFF_LABEL_VET_CONTACT_ID, veterinarianContactId)
                        put(OffLabelDrugTable.Columns.OFF_LABEL_DRUG_DOSAGE, drugDosage)
                        put(OffLabelDrugTable.Columns.OFF_LABEL_USE_START, Sql.formatDate(useStartDate))
                        if (useEndDate != null) {
                            put(OffLabelDrugTable.Columns.OFF_LABEL_USE_END, Sql.formatDate(useEndDate))
                        } else {
                            putNull(OffLabelDrugTable.Columns.OFF_LABEL_USE_END)
                        }
                        put(OffLabelDrugTable.Columns.OFF_LABEL_NOTE, notes)
                        put(OffLabelDrugTable.Columns.CREATED, timeStampString)
                        put(OffLabelDrugTable.Columns.MODIFIED, timeStampString)
                    }
                )
                setTransactionSuccessful()
                drugDoseId
            } finally {
                endTransaction()
            }
        }
    }

    override suspend fun addDrugLot(
        drugId: EntityId,
        drugLot: String,
        expirationDate: LocalDate,
        cost: Float?,
        currencyUnitsId: EntityId?,
        amountPurchased: String?,
        purchaseDate: LocalDate?,
        timeStamp: LocalDateTime
    ): EntityId {
        return databaseHandler.writableDatabase.run {
            val timeStampString = Sql.formatDateTime(timeStamp)
            beginTransaction()
            try {
                val drugLotId = insertWithPKOrThrow(
                    DrugLotTable,
                    null,
                    ContentValues().apply {
                        put(DrugLotTable.Columns.DRUG_ID, drugId)
                        put(DrugLotTable.Columns.LOT, drugLot)
                        put(DrugLotTable.Columns.EXPIRATION_DATE, Sql.formatDate(expirationDate))
                        cost?.let {
                            put(DrugLotTable.Columns.COST, it)
                        } ?: putNull(DrugLotTable.Columns.COST)
                        currencyUnitsId?.let {
                            put(DrugLotTable.Columns.COST_UNITS_ID, it)
                        } ?: putNull(DrugLotTable.Columns.COST_UNITS_ID)
                        amountPurchased?.let {
                            put(DrugLotTable.Columns.AMOUNT_PURCHASED, it)
                        } ?: putNull(DrugLotTable.Columns.AMOUNT_PURCHASED)
                        purchaseDate?.let {
                            put(DrugLotTable.Columns.PURCHASE_DATE, Sql.formatDate(it))
                        } ?: putNull(DrugLotTable.Columns.PURCHASE_DATE)
                        putNull(DrugLotTable.Columns.DISPOSE_DATE)
                        put(DrugLotTable.Columns.IS_GONE, Sql.booleanValue(false))
                        put(DrugLotTable.Columns.CREATED, timeStampString)
                        put(DrugLotTable.Columns.MODIFIED, timeStampString)
                    }
                )
                setTransactionSuccessful()
                drugLotId
            } finally {
                endTransaction()
            }
        }
    }

    private suspend fun queryDrugDosageSpecs(drugId: EntityId): List<DrugDosageSpec> {
        return withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.rawQuery(
                SQL_QUERY_DRUG_DOSAGE_SPECS_FOR_DRUG,
                arrayOf(drugId.toString())
            ).use { cursor ->
                cursor.readAllItems(::drugDosageSpecFromCursor)
            }
        }
    }

    companion object {

        val SQL_QUERY_AVAILABLE_DRUGS_BY_TYPE get() =
            """SELECT * FROM ${DrugLotTable.NAME}
                JOIN ${DrugTable.NAME}
                ON ${DrugLotTable.NAME}.${DrugLotTable.Columns.DRUG_ID} =
                    ${DrugTable.NAME}.${DrugTable.Columns.ID}
                WHERE ${DrugTable.Columns.TYPE_ID} = ?
                AND ${DrugLotTable.Columns.IS_GONE} = 0"""

        val SQL_QUERY_DRUG_DOSAGE_SPECS_FOR_DRUG get() =
            """SELECT * FROM ${DrugWithdrawalTable.NAME}
                JOIN ${DrugTable.NAME} ON ${DrugTable.NAME}.${DrugTable.Columns.ID}
                    = ${DrugWithdrawalTable.NAME}.${DrugWithdrawalTable.Columns.DRUG_ID}
                JOIN ${SpeciesTable.NAME} ON ${SpeciesTable.NAME}.${SpeciesTable.Columns.ID}
                    = ${DrugWithdrawalTable.NAME}.${DrugWithdrawalTable.Columns.SPECIES_ID}
                WHERE ${DrugWithdrawalTable.NAME}.${DrugWithdrawalTable.Columns.DRUG_ID} = ?"""

        val SQL_QUERY_DRUG_DOSAGE_SPEC_FOR_DRUG_FOR_SPECIES get() =
            """SELECT * FROM ${DrugWithdrawalTable.NAME}
                JOIN ${DrugTable.NAME} ON ${DrugTable.NAME}.${DrugTable.Columns.ID}
                    = ${DrugWithdrawalTable.NAME}.${DrugWithdrawalTable.Columns.DRUG_ID}
                JOIN ${SpeciesTable.NAME} ON ${SpeciesTable.NAME}.${SpeciesTable.Columns.ID}
                    = ${DrugWithdrawalTable.NAME}.${DrugWithdrawalTable.Columns.SPECIES_ID}
                WHERE ${DrugWithdrawalTable.NAME}.${DrugWithdrawalTable.Columns.DRUG_ID} = ?
                AND ${DrugWithdrawalTable.NAME}.${DrugWithdrawalTable.Columns.SPECIES_ID} = ?"""

        val SQL_QUERY_OFF_LABEL_DOSES_BASE get() =
            """SELECT
                    ${OffLabelDrugTable.NAME}.${OffLabelDrugTable.Columns.ID},
                    ${OffLabelDrugTable.NAME}.${OffLabelDrugTable.Columns.DRUG_ID},
                    ${OffLabelDrugTable.NAME}.${OffLabelDrugTable.Columns.SPECIES_ID},
                    ${OffLabelDrugTable.NAME}.${OffLabelDrugTable.Columns.OFF_LABEL_VET_CONTACT_ID},
                    ${OffLabelDrugTable.NAME}.${OffLabelDrugTable.Columns.OFF_LABEL_DRUG_DOSAGE},
                    ${DrugTable.NAME}.${DrugTable.Columns.TRADE_NAME},
                    ${SpeciesTable.NAME}.${SpeciesTable.Columns.COMMON_NAME},
                    ${ContactTable.NAME}.${ContactTable.Columns.LAST_NAME}
                FROM ${OffLabelDrugTable.NAME}
                JOIN ${DrugTable.NAME} 
                ON ${OffLabelDrugTable.NAME}.${OffLabelDrugTable.Columns.DRUG_ID}
                    = ${DrugTable.NAME}.${DrugTable.Columns.ID}
                JOIN ${SpeciesTable.NAME}
                ON ${OffLabelDrugTable.NAME}.${OffLabelDrugTable.Columns.SPECIES_ID}
                    = ${SpeciesTable.NAME}.${SpeciesTable.Columns.ID}
                JOIN ${ContactVeterinarianTable.NAME}
                ON ${OffLabelDrugTable.NAME}.${OffLabelDrugTable.Columns.OFF_LABEL_VET_CONTACT_ID}
                    = ${ContactVeterinarianTable.NAME}.${ContactVeterinarianTable.Columns.ID}
                JOIN ${ContactTable.NAME}
                ON ${ContactVeterinarianTable.NAME}.${ContactVeterinarianTable.Columns.CONTACT_ID}
                    = ${ContactTable.NAME}.${ContactTable.Columns.ID}"""

        val SQL_QUERY_OFF_LABEL_DOSES_FOR_DRUG get() =
                """$SQL_QUERY_OFF_LABEL_DOSES_BASE
                    WHERE ${OffLabelDrugTable.NAME}.${OffLabelDrugTable.Columns.DRUG_ID} = ?1
                    AND ${OffLabelDrugTable.NAME}.${OffLabelDrugTable.Columns.OFF_LABEL_USE_START} <= ?2
                    AND (${OffLabelDrugTable.NAME}.${OffLabelDrugTable.Columns.OFF_LABEL_USE_END} IS NULL
                    OR ?2 <= ${OffLabelDrugTable.NAME}.${OffLabelDrugTable.Columns.OFF_LABEL_USE_END})"""

        val SQL_QUERY_OFF_LABEL_DOSE get() =
            """$SQL_QUERY_OFF_LABEL_DOSES_BASE
                WHERE ${OffLabelDrugTable.Columns.ID} = ?"""

        fun drugApplicationInfoFromCursor(cursor: Cursor): DrugApplicationInfo {
            return DrugApplicationInfo(
                drugId = cursor.getEntityId(DrugLotTable.Columns.DRUG_ID),
                drugTypeId = cursor.getEntityId(DrugTable.Columns.TYPE_ID),
                tradeDrugName = cursor.getString(DrugTable.Columns.TRADE_NAME),
                genericDrugName = cursor.getString(DrugTable.Columns.GENERIC_NAME),
                drugLotId = cursor.getEntityId(DrugLotTable.Columns.ID),
                lot = cursor.getOptString(DrugLotTable.Columns.LOT),
                isGone = cursor.getBoolean(DrugLotTable.Columns.IS_GONE),
                drugDosageSpecs = emptyList()
            )
        }

        fun drugDosageSpecFromCursor(cursor: Cursor): DrugDosageSpec {
            val meatWithdrawal = cursor.getOptInt(DrugWithdrawalTable.Columns.MEAT_WITHDRAWAL)
            val userMeatWithdrawal = cursor.getOptInt(DrugWithdrawalTable.Columns.USER_MEAT_WITHDRAWAL)
            val meatWithdrawalUnitsId = cursor.getOptEntityId(DrugWithdrawalTable.Columns.MEAT_WITHDRAWAL_UNITS_ID)

            val meatWithdrawalSpec = DrugWithdrawalSpec.create(
                withdrawal = meatWithdrawal,
                userWithdrawal = userMeatWithdrawal,
                withdrawalUnitsId = meatWithdrawalUnitsId
            )

            val milkWithdrawal = cursor.getOptInt(DrugWithdrawalTable.Columns.MILK_WITHDRAWAL)
            val userMilkWithdrawal = cursor.getOptInt(DrugWithdrawalTable.Columns.USER_MILK_WITHDRAWAL)
            val milkWithdrawalUnitsId = cursor.getOptEntityId(DrugWithdrawalTable.Columns.MILK_WITHDRAWAL_UNITS_ID)

            val milkWithdrawalSpec = DrugWithdrawalSpec.create(
                withdrawal = milkWithdrawal,
                userWithdrawal = userMilkWithdrawal,
                withdrawalUnitsId = milkWithdrawalUnitsId
            )

            return DrugDosageSpec(
                id = cursor.getEntityId(DrugWithdrawalTable.Columns.ID),
                drugId = cursor.getEntityId(DrugWithdrawalTable.Columns.DRUG_ID),
                drugTradeName = cursor.getString(DrugTable.Columns.TRADE_NAME),
                drugGenericName = cursor.getString(DrugTable.Columns.GENERIC_NAME),
                speciesId = cursor.getEntityId(DrugWithdrawalTable.Columns.SPECIES_ID),
                speciesName = cursor.getString(SpeciesTable.Columns.COMMON_NAME),
                officialDrugDosage = cursor.getString(DrugWithdrawalTable.Columns.OFFICIAL_DRUG_DOSAGE),
                userDrugDosage = cursor.getOptString(DrugWithdrawalTable.Columns.USER_DRUG_DOSAGE),
                meatWithdrawalSpec = meatWithdrawalSpec,
                milkWithdrawalSpec = milkWithdrawalSpec
            )
        }

        private fun offLabelDrugDoseFromCursor(cursor: Cursor): OffLabelDrugDose {
            return OffLabelDrugDose(
                id = cursor.getEntityId(OffLabelDrugTable.Columns.ID),
                drugId = cursor.getEntityId(OffLabelDrugTable.Columns.DRUG_ID),
                drugTradeName = cursor.getString(DrugTable.Columns.TRADE_NAME),
                speciesId = cursor.getEntityId(OffLabelDrugTable.Columns.SPECIES_ID),
                speciesName = cursor.getString(SpeciesTable.Columns.COMMON_NAME),
                vetContactId = cursor.getEntityId(OffLabelDrugTable.Columns.OFF_LABEL_VET_CONTACT_ID),
                vetLastName = cursor.getString(ContactTable.Columns.LAST_NAME),
                drugDose = cursor.getString(OffLabelDrugTable.Columns.OFF_LABEL_DRUG_DOSAGE)
            )
        }
    }
}
