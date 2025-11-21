package com.weyr_associates.animaltrakkerfarmmobile.app.repository.database

import android.content.ContentValues
import android.database.Cursor
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.EvaluationRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseHandler
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.CustomEvalTraitsTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.EvalTraitTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.SavedEvaluationTable
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Sql
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.UnitsTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.UnitsTypeTable
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getBoolean
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getInt
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getOptBoolean
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getOptEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getOptString
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getString
import com.weyr_associates.animaltrakkerfarmmobile.database.core.insertWithPKOrThrow
import com.weyr_associates.animaltrakkerfarmmobile.database.core.isNull
import com.weyr_associates.animaltrakkerfarmmobile.database.core.put
import com.weyr_associates.animaltrakkerfarmmobile.database.core.putNull
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readAllItems
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readFirstItem
import com.weyr_associates.animaltrakkerfarmmobile.model.BasicEvalTrait
import com.weyr_associates.animaltrakkerfarmmobile.model.CustomEvalTrait
import com.weyr_associates.animaltrakkerfarmmobile.model.EvalTraitConfig
import com.weyr_associates.animaltrakkerfarmmobile.model.EvalTraitOption
import com.weyr_associates.animaltrakkerfarmmobile.model.EvaluationConfiguration
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.ItemEntry
import com.weyr_associates.animaltrakkerfarmmobile.model.SavedEvaluation
import com.weyr_associates.animaltrakkerfarmmobile.model.Trait
import com.weyr_associates.animaltrakkerfarmmobile.model.UnitOfMeasure
import com.weyr_associates.animaltrakkerfarmmobile.model.UnitsEvalTrait
import com.weyr_associates.animaltrakkerfarmmobile.model.UserType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class EvaluationRepositoryImpl(private val databaseHandler: DatabaseHandler) : EvaluationRepository {

    override suspend fun queryTraitsByType(typeId: EntityId): List<Trait> {
        return withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.rawQuery(
                EvalTraitTable.Sql.QUERY_TRAITS_BY_TYPE,
                arrayOf(typeId.toString())
            ).use { cursor ->
                cursor.readAllItems(EvalTraitTable::traitFromCursor)
            }
        }
    }

    override suspend fun querySavedEvaluationsForUser(
        userId: EntityId,
        userType: UserType
    ): List<ItemEntry> {
        return withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.rawQuery(
                when(userType) {
                    UserType.CONTACT -> SavedEvaluationTable.Sql.QUERY_SAVED_EVALUATIONS_FOR_CONTACT_USER
                    UserType.COMPANY -> SavedEvaluationTable.Sql.QUERY_SAVED_EVALUATIONS_FOR_COMPANY_USER
                },
                arrayOf(userId.toString())
            ).use { cursor ->
                cursor.readAllItems(SavedEvaluationTable::itemEntryForSavedEvaluationFromCursor)
            }
        }
    }

    override suspend fun querySavedEvaluationById(id: EntityId): SavedEvaluation? {
        return withContext(Dispatchers.IO) {

            val scoredTraitCursorDeferred = async {
                val scoredTraitsQuery = createQueryForSavedEvaluationScoredTraitsById()
                return@async databaseHandler.readableDatabase.rawQuery(
                    scoredTraitsQuery,
                    arrayOf(id.toString())
                )
            }

            val unitsTraitCursorDeferred = async {
                val unitsTraitsQuery = createQueryForSavedEvaluationUnitsTraitsById()
                return@async databaseHandler.readableDatabase.rawQuery(
                    unitsTraitsQuery,
                    arrayOf(id.toString())
                )
            }

            val customTraitCursorDeferred = async {
                val customTraitsQuery = createQueryForSavedEvaluationCustomTraitsById()
                return@async databaseHandler.readableDatabase.rawQuery(
                    customTraitsQuery,
                    arrayOf(id.toString())
                )
            }

            awaitAll(scoredTraitCursorDeferred, unitsTraitCursorDeferred)

            val scoredTraitCursor = scoredTraitCursorDeferred.await()
            val unitsTraitCursor = unitsTraitCursorDeferred.await()
            val customTraitCursor = customTraitCursorDeferred.await()

            scoredTraitCursor.use {
                unitsTraitCursor.use {
                    customTraitCursor.use {
                        savedEvaluationFromCursors(
                            scoredTraitCursor,
                            unitsTraitCursor,
                            customTraitCursor,
                            ::queryCustomTraitOptionsByCustomTraitId
                        )
                    }
                }
            }
        }
    }

    override suspend fun queryEvalTraitOptionsForTrait(traitId: EntityId): List<EvalTraitOption> {
        return withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.rawQuery(
                QUERY_CUSTOM_TRAIT_OPTIONS_BY_TRAIT_ID,
                arrayOf(traitId.toString())
            ).use { cursor ->
                cursor.readAllItems(::evalTraitOptionFromCursor)
            }
        }
    }

    override suspend fun queryEvalTraitOptionById(evalTraitOptionId: EntityId): EvalTraitOption? {
        return withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.rawQuery(
                QUERY_CUSTOM_TRAIT_OPTION_BY_ID,
                arrayOf(evalTraitOptionId.toString())
            ).use { cursor ->
                cursor.readFirstItem(::evalTraitOptionFromCursor)
            }
        }
    }

    override suspend fun saveEvaluationConfigurationForUser(
        userId: EntityId,
        userType: UserType,
        configuration: EvaluationConfiguration,
        timeStamp: LocalDateTime
    ) {
        val timeStampString = Sql.formatDateTime(timeStamp)
        return databaseHandler.writableDatabase.run {
            beginTransaction()
            try {
                insertWithPKOrThrow(
                    SavedEvaluationTable,
                    null,
                    ContentValues().apply {
                        put(SavedEvaluationTable.Columns.NAME, configuration.name)
                        put(SavedEvaluationTable.Columns.SUMMARIZE_IN_ALERT, configuration.saveSummaryAsAlert)
                        when(userType) {
                            UserType.COMPANY -> {
                                put(SavedEvaluationTable.Columns.COMPANY_ID, userId)
                                putNull(SavedEvaluationTable.Columns.CONTACT_ID)
                            }
                            UserType.CONTACT -> {
                                put(SavedEvaluationTable.Columns.CONTACT_ID, userId)
                                putNull(SavedEvaluationTable.Columns.COMPANY_ID)
                            }
                        }
                        putTraitConfig(
                            SavedEvaluationTable.Columns.TRAIT_01_ID,
                            SavedEvaluationTable.Columns.TRAIT_01_OPTIONAL,
                            SavedEvaluationTable.Columns.TRAIT_01_DEFERRED,
                            null,
                            configuration.trait01
                        )
                        putTraitConfig(
                            SavedEvaluationTable.Columns.TRAIT_02_ID,
                            SavedEvaluationTable.Columns.TRAIT_02_OPTIONAL,
                            SavedEvaluationTable.Columns.TRAIT_02_DEFERRED,
                            null,
                            configuration.trait02
                        )
                        putTraitConfig(
                            SavedEvaluationTable.Columns.TRAIT_03_ID,
                            SavedEvaluationTable.Columns.TRAIT_03_OPTIONAL,
                            SavedEvaluationTable.Columns.TRAIT_03_DEFERRED,
                            null,
                            configuration.trait03
                        )
                        putTraitConfig(
                            SavedEvaluationTable.Columns.TRAIT_04_ID,
                            SavedEvaluationTable.Columns.TRAIT_04_OPTIONAL,
                            SavedEvaluationTable.Columns.TRAIT_04_DEFERRED,
                            null,
                            configuration.trait04
                        )
                        putTraitConfig(
                            SavedEvaluationTable.Columns.TRAIT_05_ID,
                            SavedEvaluationTable.Columns.TRAIT_05_OPTIONAL,
                            SavedEvaluationTable.Columns.TRAIT_05_DEFERRED,
                            null,
                            configuration.trait05
                        )
                        putTraitConfig(
                            SavedEvaluationTable.Columns.TRAIT_06_ID,
                            SavedEvaluationTable.Columns.TRAIT_06_OPTIONAL,
                            SavedEvaluationTable.Columns.TRAIT_06_DEFERRED,
                            null,
                            configuration.trait06
                        )
                        putTraitConfig(
                            SavedEvaluationTable.Columns.TRAIT_07_ID,
                            SavedEvaluationTable.Columns.TRAIT_07_OPTIONAL,
                            SavedEvaluationTable.Columns.TRAIT_07_DEFERRED,
                            null,
                            configuration.trait07
                        )
                        putTraitConfig(
                            SavedEvaluationTable.Columns.TRAIT_08_ID,
                            SavedEvaluationTable.Columns.TRAIT_08_OPTIONAL,
                            SavedEvaluationTable.Columns.TRAIT_08_DEFERRED,
                            null,
                            configuration.trait08
                        )
                        putTraitConfig(
                            SavedEvaluationTable.Columns.TRAIT_09_ID,
                            SavedEvaluationTable.Columns.TRAIT_09_OPTIONAL,
                            SavedEvaluationTable.Columns.TRAIT_09_DEFERRED,
                            null,
                            configuration.trait09
                        )
                        putTraitConfig(
                            SavedEvaluationTable.Columns.TRAIT_10_ID,
                            SavedEvaluationTable.Columns.TRAIT_10_OPTIONAL,
                            SavedEvaluationTable.Columns.TRAIT_10_DEFERRED,
                            null,
                            configuration.trait10
                        )
                        putTraitConfig(
                            SavedEvaluationTable.Columns.TRAIT_11_ID,
                            SavedEvaluationTable.Columns.TRAIT_11_OPTIONAL,
                            SavedEvaluationTable.Columns.TRAIT_11_DEFERRED,
                            SavedEvaluationTable.Columns.TRAIT_11_UNITS_ID,
                            configuration.trait11
                        )
                        putTraitConfig(
                            SavedEvaluationTable.Columns.TRAIT_12_ID,
                            SavedEvaluationTable.Columns.TRAIT_12_OPTIONAL,
                            SavedEvaluationTable.Columns.TRAIT_12_DEFERRED,
                            SavedEvaluationTable.Columns.TRAIT_12_UNITS_ID,
                            configuration.trait12
                        )
                        putTraitConfig(
                            SavedEvaluationTable.Columns.TRAIT_13_ID,
                            SavedEvaluationTable.Columns.TRAIT_13_OPTIONAL,
                            SavedEvaluationTable.Columns.TRAIT_13_DEFERRED,
                            SavedEvaluationTable.Columns.TRAIT_13_UNITS_ID,
                            configuration.trait13
                        )
                        putTraitConfig(
                            SavedEvaluationTable.Columns.TRAIT_14_ID,
                            SavedEvaluationTable.Columns.TRAIT_14_OPTIONAL,
                            SavedEvaluationTable.Columns.TRAIT_14_DEFERRED,
                            SavedEvaluationTable.Columns.TRAIT_14_UNITS_ID,
                            configuration.trait14
                        )
                        putTraitConfig(
                            SavedEvaluationTable.Columns.TRAIT_15_ID,
                            SavedEvaluationTable.Columns.TRAIT_15_OPTIONAL,
                            SavedEvaluationTable.Columns.TRAIT_15_DEFERRED,
                            SavedEvaluationTable.Columns.TRAIT_15_UNITS_ID,
                            configuration.trait15
                        )
                        putTraitConfig(
                            SavedEvaluationTable.Columns.TRAIT_16_ID,
                            SavedEvaluationTable.Columns.TRAIT_16_OPTIONAL,
                            SavedEvaluationTable.Columns.TRAIT_16_DEFERRED,
                            null,
                            configuration.trait16
                        )
                        putTraitConfig(
                            SavedEvaluationTable.Columns.TRAIT_17_ID,
                            SavedEvaluationTable.Columns.TRAIT_17_OPTIONAL,
                            SavedEvaluationTable.Columns.TRAIT_17_DEFERRED,
                            null,
                            configuration.trait17
                        )
                        putTraitConfig(
                            SavedEvaluationTable.Columns.TRAIT_18_ID,
                            SavedEvaluationTable.Columns.TRAIT_18_OPTIONAL,
                            SavedEvaluationTable.Columns.TRAIT_18_DEFERRED,
                            null,
                            configuration.trait18
                        )
                        putTraitConfig(
                            SavedEvaluationTable.Columns.TRAIT_19_ID,
                            SavedEvaluationTable.Columns.TRAIT_19_OPTIONAL,
                            SavedEvaluationTable.Columns.TRAIT_19_DEFERRED,
                            null,
                            configuration.trait19
                        )
                        putTraitConfig(
                            SavedEvaluationTable.Columns.TRAIT_20_ID,
                            SavedEvaluationTable.Columns.TRAIT_20_OPTIONAL,
                            SavedEvaluationTable.Columns.TRAIT_20_DEFERRED,
                            null,
                            configuration.trait20
                        )
                        put(SavedEvaluationTable.Columns.CREATED, timeStampString)
                        put(SavedEvaluationTable.Columns.MODIFIED, timeStampString)
                    }
                )
                setTransactionSuccessful()
            } finally {
                endTransaction()
            }
        }
    }

    private suspend fun queryCustomTraitOptionsByCustomTraitId(customTraitId: EntityId): List<EvalTraitOption> {
        return withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.rawQuery(
                QUERY_CUSTOM_TRAIT_OPTIONS_BY_TRAIT_ID,
                arrayOf(customTraitId.toString())
            ).use { cursor ->
                cursor.readAllItems(::evalTraitOptionFromCursor)
            }
        }
    }

    private fun ContentValues.putTraitConfig(
        traitIdCol: Column.Nullable,
        traitOptionalCol: Column.Nullable,
        traitDeferredCol: Column.Nullable,
        traitUnitsCol: Column.Nullable?,
        traitConfig: EvalTraitConfig
    ) {
        if (traitConfig.isConfigurationComplete) {
            put(traitIdCol, requireNotNull(traitConfig.trait).id)
        } else {
            putNull(traitIdCol)
        }
        put(traitOptionalCol, traitConfig.isConfigurationComplete && traitConfig.isOptional)
        put(traitDeferredCol, traitConfig.isConfigurationComplete && traitConfig.isDeferred)
        if (traitUnitsCol != null) {
            if (traitConfig.isConfigurationComplete) {
                put(traitUnitsCol, requireNotNull(traitConfig.units).id)
            } else {
                putNull(traitUnitsCol)
            }
        }
    }

    companion object {

        val QUERY_CUSTOM_TRAIT_OPTIONS_BY_TRAIT_ID get() =
            """SELECT
                ${CustomEvalTraitsTable.Columns.ID},
                ${CustomEvalTraitsTable.Columns.ITEM},
                ${CustomEvalTraitsTable.Columns.TRAIT_ID},
                ${CustomEvalTraitsTable.Columns.ORDER}
               FROM ${CustomEvalTraitsTable.NAME}
               WHERE ${CustomEvalTraitsTable.Columns.TRAIT_ID} = ?
               ORDER BY ${CustomEvalTraitsTable.Columns.ORDER}"""

        val QUERY_CUSTOM_TRAIT_OPTION_BY_ID get() =
            """SELECT * FROM ${CustomEvalTraitsTable.NAME}
                WHERE ${CustomEvalTraitsTable.Columns.ID} = ?"""

        private const val TRAIT_ID_SCORED_TRAITS_FIRST = 1
        private const val TRAIT_ID_SCORED_TRAITS_LAST = 10

        private const val TRAIT_ID_UNITS_TRAITS_FIRST = 11
        private const val TRAIT_ID_UNITS_TRAITS_LAST = 15

        private const val TRAIT_ID_CUSTOM_TRAITS_FIRST = 16
        private const val TRAIT_ID_CUSTOM_TRAITS_LAST = 20

        private const val TRAIT_ID_FMT_SPECIFIER = "%02d"

        // Table Aliases
        private const val FMT_TABLE_TRAIT_XX = "table_trait_${TRAIT_ID_FMT_SPECIFIER}"
        private const val FMT_TABLE_TRAIT_UNITS_XX = "table_trait_units_${TRAIT_ID_FMT_SPECIFIER}"
        private const val FMT_TABLE_TRAIT_UNIT_TYPE_XX = "table_trait_unit_type_${TRAIT_ID_FMT_SPECIFIER}"

        // Original column names
        private const val FMT_TRAIT_NAME_XX = "trait_name${TRAIT_ID_FMT_SPECIFIER}"
        private const val FMT_TRAIT_UNITS_XX = "trait_units${TRAIT_ID_FMT_SPECIFIER}"
        private const val FMT_TRAIT_NAME_XX_OPTIONAL = "trait_name${TRAIT_ID_FMT_SPECIFIER}_optional"
        private const val FMT_TRAIT_NAME_XX_DEFERRED = "trait_name${TRAIT_ID_FMT_SPECIFIER}_deferred"

        // Column aliases for dynamic query creation
        private const val FMT_TRAIT_XX_ID = "trait_${TRAIT_ID_FMT_SPECIFIER}_id"
        private const val FMT_TRAIT_XX_NAME = "trait_${TRAIT_ID_FMT_SPECIFIER}_name"
        private const val FMT_TRAIT_XX_TYPE_ID = "trait_${TRAIT_ID_FMT_SPECIFIER}_type_id"
        private const val FMT_TRAIT_XX_UNITS_ID = "trait_${TRAIT_ID_FMT_SPECIFIER}_units_id"
        private const val FMT_TRAIT_XX_UNITS_NAME = "trait_${TRAIT_ID_FMT_SPECIFIER}_units_name"
        private const val FMT_TRAIT_XX_UNITS_ABBR = "trait_${TRAIT_ID_FMT_SPECIFIER}_units_abbr"
        private const val FMT_TRAIT_XX_UNITS_TYPE_ID = "trait_${TRAIT_ID_FMT_SPECIFIER}_units_type_id"
        private const val FMT_TRAIT_XX_UNIT_TYPE_ID = "trait_${TRAIT_ID_FMT_SPECIFIER}_unit_type_id"
        private const val FMT_TRAIT_XX_UNIT_TYPE_NAME = "trait_${TRAIT_ID_FMT_SPECIFIER}_unit_type_name"
        private const val FMT_TRAIT_XX_OPTIONAL = "trait_${TRAIT_ID_FMT_SPECIFIER}_optional"
        private const val FMT_TRAIT_XX_DEFERRED = "trait_${TRAIT_ID_FMT_SPECIFIER}_deferred"

        //region SCORED TRAITS QUERY BUILDERS

        private fun createQueryForSavedEvaluationScoredTraitsById() = buildString {
            appendLine("SELECT")
            append(projectionForScoreTraitDataForEvaluationMainQuery())
            appendLine("FROM (")
            append(subQueryForEvaluationScoredTraitsById())
            appendLine(") AS ${SavedEvaluationTable.NAME}")
            append(savedEvaluationJoinsForScoredTraits())
        }

        private fun projectionForScoreTraitDataForEvaluationMainQuery() = buildString {
            appendLine("${SavedEvaluationTable.Columns.ID},")
            appendLine("${SavedEvaluationTable.Columns.NAME},")
            appendLine("${SavedEvaluationTable.Columns.SUMMARIZE_IN_ALERT},")
            appendLine("${SavedEvaluationTable.Columns.CONTACT_ID},")
            appendLine("${SavedEvaluationTable.Columns.COMPANY_ID},")
            for (traitId: Int in TRAIT_ID_SCORED_TRAITS_FIRST .. TRAIT_ID_SCORED_TRAITS_LAST) {
                append(projectionForScoreTraitDataForEvaluationMainQuery(traitId))
                if (traitId != TRAIT_ID_SCORED_TRAITS_LAST) {
                    append(",")
                }
                appendLine()
            }
        }

        private fun projectionForScoreTraitDataForEvaluationMainQuery(traitId: Int) = buildString {
            appendLine("${FMT_TABLE_TRAIT_XX.format(traitId)}.${FMT_TRAIT_XX_ID.format(traitId)},")
            appendLine("${FMT_TRAIT_XX_NAME.format(traitId)},")
            appendLine("${FMT_TRAIT_XX_TYPE_ID.format(traitId)},")
            appendLine("${FMT_TRAIT_XX_OPTIONAL.format(traitId)},")
            append(FMT_TRAIT_XX_DEFERRED.format(traitId))
        }

        private fun subQueryForEvaluationScoredTraitsById(): String {
            return subQueryForEvaluationTraitsByIdRange(
                TRAIT_ID_SCORED_TRAITS_FIRST,
                TRAIT_ID_SCORED_TRAITS_LAST
            )
        }

        private fun savedEvaluationJoinsForScoredTraits() = buildString {
            for(traitId: Int in TRAIT_ID_SCORED_TRAITS_FIRST .. TRAIT_ID_SCORED_TRAITS_LAST) {
                append(joinForScoredTrait(traitId))
            }
        }

        private fun joinForScoredTrait(traitId: Int) = buildString {
            appendLine("LEFT OUTER JOIN (")
            appendLine("SELECT")
            appendLine("${EvalTraitTable.Columns.ID} AS ${FMT_TRAIT_XX_ID.format(traitId)},")
            appendLine("${EvalTraitTable.Columns.NAME} AS ${FMT_TRAIT_XX_NAME.format(traitId)},")
            appendLine("${EvalTraitTable.Columns.TYPE_ID} AS ${FMT_TRAIT_XX_TYPE_ID.format(traitId)}")
            appendLine("FROM ${EvalTraitTable.NAME}")
            appendLine(") AS ${FMT_TABLE_TRAIT_XX.format(traitId)}")
            appendLine("ON ${SavedEvaluationTable.NAME}.${FMT_TRAIT_XX_ID.format(traitId)} =")
            appendLine("${FMT_TABLE_TRAIT_XX.format(traitId)}.${FMT_TRAIT_XX_ID.format(traitId)}")
        }

        //endregion

        //region UNITS TRAITS QUERY BUILDERS

        private fun createQueryForSavedEvaluationUnitsTraitsById() = buildString {
            appendLine("SELECT")
            append(projectionForUnitsTraitDataForEvaluationMainQuery())
            appendLine("FROM (")
            append(subQueryForEvaluationUnitsTraitsById())
            appendLine(") AS ${SavedEvaluationTable.NAME}")
            append(savedEvaluationJoinsForUnitsTraits())
        }

        private fun projectionForUnitsTraitDataForEvaluationMainQuery() = buildString {
            appendLine("${SavedEvaluationTable.Columns.ID},")
            appendLine("${SavedEvaluationTable.Columns.NAME},")
            appendLine("${SavedEvaluationTable.Columns.SUMMARIZE_IN_ALERT},")
            appendLine("${SavedEvaluationTable.Columns.CONTACT_ID},")
            appendLine("${SavedEvaluationTable.Columns.COMPANY_ID},")
            for (traitId: Int in TRAIT_ID_UNITS_TRAITS_FIRST .. TRAIT_ID_UNITS_TRAITS_LAST) {
                append(projectionForUnitsTraitDataForEvaluationMainQuery(traitId))
                if (traitId != TRAIT_ID_UNITS_TRAITS_LAST) {
                    append(",")
                }
                appendLine()
            }
        }

        private fun projectionForUnitsTraitDataForEvaluationMainQuery(traitId: Int) = buildString {
            appendLine("${FMT_TABLE_TRAIT_XX.format(traitId)}.${FMT_TRAIT_XX_ID.format(traitId)},")
            appendLine("${FMT_TRAIT_XX_NAME.format(traitId)},")
            appendLine("${FMT_TRAIT_XX_TYPE_ID.format(traitId)},")
            appendLine("${FMT_TABLE_TRAIT_UNITS_XX.format(traitId)}.${FMT_TRAIT_XX_UNITS_ID.format(traitId)},")
            appendLine("${FMT_TRAIT_XX_UNITS_NAME.format(traitId)},")
            appendLine("${FMT_TRAIT_XX_UNITS_ABBR.format(traitId)},")
            appendLine("${FMT_TABLE_TRAIT_UNIT_TYPE_XX.format(traitId)}.${FMT_TRAIT_XX_UNIT_TYPE_ID.format(traitId)},")
            appendLine("${FMT_TRAIT_XX_UNIT_TYPE_NAME.format(traitId)},")
            appendLine("${FMT_TRAIT_XX_OPTIONAL.format(traitId)},")
            append(FMT_TRAIT_XX_DEFERRED.format(traitId))
        }

        private fun subQueryForEvaluationUnitsTraitsById(): String {
            return subQueryForEvaluationTraitsByIdRange(
                TRAIT_ID_UNITS_TRAITS_FIRST,
                TRAIT_ID_UNITS_TRAITS_LAST
            )
        }

        private fun savedEvaluationJoinsForUnitsTraits() = buildString {
            for(traitId: Int in TRAIT_ID_UNITS_TRAITS_FIRST .. TRAIT_ID_UNITS_TRAITS_LAST) {
                append(joinForUnitsTrait(traitId))
            }
        }

        private fun joinForUnitsTrait(traitId: Int) = buildString {
            appendLine("LEFT OUTER JOIN (")
            appendLine("SELECT")
            appendLine("${EvalTraitTable.Columns.ID} AS ${FMT_TRAIT_XX_ID.format(traitId)},")
            appendLine("${EvalTraitTable.Columns.NAME} AS ${FMT_TRAIT_XX_NAME.format(traitId)},")
            appendLine("${EvalTraitTable.Columns.TYPE_ID} AS ${FMT_TRAIT_XX_TYPE_ID.format(traitId)}")
            appendLine("FROM ${EvalTraitTable.NAME}")
            appendLine(") AS ${FMT_TABLE_TRAIT_XX.format(traitId)}")
            appendLine("ON ${SavedEvaluationTable.NAME}.${FMT_TRAIT_XX_ID.format(traitId)} =")
            appendLine("${FMT_TABLE_TRAIT_XX.format(traitId)}.${FMT_TRAIT_XX_ID.format(traitId)}")
            appendLine("LEFT OUTER JOIN (")
            appendLine("SELECT")
            appendLine("${UnitsTable.Columns.ID} AS ${FMT_TRAIT_XX_UNITS_ID.format(traitId)},")
            appendLine("${UnitsTable.Columns.NAME} AS ${FMT_TRAIT_XX_UNITS_NAME.format(traitId)},")
            appendLine("${UnitsTable.Columns.ABBREVIATION} AS ${FMT_TRAIT_XX_UNITS_ABBR.format(traitId)},")
            appendLine("${UnitsTable.Columns.TYPE_ID} AS ${FMT_TRAIT_XX_UNITS_TYPE_ID.format(traitId)}")
            appendLine("FROM ${UnitsTable.NAME}")
            appendLine(") AS ${FMT_TABLE_TRAIT_UNITS_XX.format(traitId)}")
            appendLine("ON ${SavedEvaluationTable.NAME}.${FMT_TRAIT_XX_UNITS_ID.format(traitId)} =")
            appendLine("${FMT_TABLE_TRAIT_UNITS_XX.format(traitId)}.${FMT_TRAIT_XX_UNITS_ID.format(traitId)}")
            appendLine("LEFT OUTER JOIN (")
            appendLine("SELECT")
            appendLine("${UnitsTypeTable.Columns.ID} AS ${FMT_TRAIT_XX_UNIT_TYPE_ID.format(traitId)},")
            appendLine("${UnitsTypeTable.Columns.NAME} AS ${FMT_TRAIT_XX_UNIT_TYPE_NAME.format(traitId)}")
            appendLine("FROM ${UnitsTypeTable.NAME}")
            appendLine(") AS ${FMT_TABLE_TRAIT_UNIT_TYPE_XX.format(traitId)}")
            appendLine("ON ${FMT_TABLE_TRAIT_UNITS_XX.format(traitId)}.${FMT_TRAIT_XX_UNITS_TYPE_ID.format(traitId)} =")
            appendLine("${FMT_TABLE_TRAIT_UNIT_TYPE_XX.format(traitId)}.${FMT_TRAIT_XX_UNIT_TYPE_ID.format(traitId)}")
        }

        //endregion

        //region CUSTOM TRAITS QUERY BUILDERS

        private fun createQueryForSavedEvaluationCustomTraitsById() = buildString {
            appendLine("SELECT")
            append(projectionForCustomTraitDataForEvaluationMainQuery())
            appendLine("FROM (")
            append(subQueryForEvaluationCustomTraitsById())
            appendLine(") AS ${SavedEvaluationTable.NAME}")
            append(savedEvaluationJoinsForCustomTraits())
        }

        private fun projectionForCustomTraitDataForEvaluationMainQuery() = buildString {
            appendLine("${SavedEvaluationTable.Columns.ID},")
            appendLine("${SavedEvaluationTable.Columns.NAME},")
            appendLine("${SavedEvaluationTable.Columns.SUMMARIZE_IN_ALERT},")
            appendLine("${SavedEvaluationTable.Columns.CONTACT_ID},")
            appendLine("${SavedEvaluationTable.Columns.COMPANY_ID},")
            for (traitId: Int in TRAIT_ID_CUSTOM_TRAITS_FIRST .. TRAIT_ID_CUSTOM_TRAITS_LAST) {
                append(projectionForCustomTraitDataForEvaluationMainQuery(traitId))
                if (traitId != TRAIT_ID_CUSTOM_TRAITS_LAST) {
                    append(",")
                }
                appendLine()
            }
        }

        private fun projectionForCustomTraitDataForEvaluationMainQuery(traitId: Int) = buildString {
            appendLine("${FMT_TABLE_TRAIT_XX.format(traitId)}.${FMT_TRAIT_XX_ID.format(traitId)},")
            appendLine("${FMT_TRAIT_XX_NAME.format(traitId)},")
            appendLine("${FMT_TRAIT_XX_TYPE_ID.format(traitId)},")
            appendLine("${FMT_TRAIT_XX_OPTIONAL.format(traitId)},")
            append(FMT_TRAIT_XX_DEFERRED.format(traitId))
        }

        private fun subQueryForEvaluationCustomTraitsById(): String {
            return subQueryForEvaluationTraitsByIdRange(
                TRAIT_ID_CUSTOM_TRAITS_FIRST,
                TRAIT_ID_CUSTOM_TRAITS_LAST
            )
        }

        private fun savedEvaluationJoinsForCustomTraits() = buildString {
            for(traitId: Int in TRAIT_ID_CUSTOM_TRAITS_FIRST .. TRAIT_ID_CUSTOM_TRAITS_LAST) {
                append(joinForCustomTrait(traitId))
            }
        }

        private fun joinForCustomTrait(traitId: Int) = buildString {
            appendLine("LEFT OUTER JOIN (")
            appendLine("SELECT")
            appendLine("${EvalTraitTable.Columns.ID} AS ${FMT_TRAIT_XX_ID.format(traitId)},")
            appendLine("${EvalTraitTable.Columns.NAME} AS ${FMT_TRAIT_XX_NAME.format(traitId)},")
            appendLine("${EvalTraitTable.Columns.TYPE_ID} AS ${FMT_TRAIT_XX_TYPE_ID.format(traitId)}")
            appendLine("FROM ${EvalTraitTable.NAME}")
            appendLine(") AS ${FMT_TABLE_TRAIT_XX.format(traitId)}")
            appendLine("ON ${SavedEvaluationTable.NAME}.${FMT_TRAIT_XX_ID.format(traitId)} =")
            appendLine("${FMT_TABLE_TRAIT_XX.format(traitId)}.${FMT_TRAIT_XX_ID.format(traitId)}")
        }

        //endregion

        //region QUERY BUILDER HELPERS

        private fun subQueryForEvaluationTraitsByIdRange(traitIdRangeStart: Int, traitIdRangeEnd: Int) = buildString {
            require(traitIdRangeStart in 1..traitIdRangeEnd)
            appendLine("SELECT")
            appendLine("${SavedEvaluationTable.Columns.ID},")
            appendLine("${SavedEvaluationTable.Columns.NAME},")
            appendLine("${SavedEvaluationTable.Columns.SUMMARIZE_IN_ALERT},")
            appendLine("${SavedEvaluationTable.Columns.CONTACT_ID},")
            appendLine("${SavedEvaluationTable.Columns.COMPANY_ID},")
            for(traitId: Int in traitIdRangeStart .. traitIdRangeEnd) {
                append(projectionForTraitDataForEvaluationSubQuery(traitId))
                if (traitId != traitIdRangeEnd) {
                    append(",")
                }
                appendLine()
            }
            appendLine("FROM ${SavedEvaluationTable.NAME}")
            appendLine("WHERE ${SavedEvaluationTable.Columns.ID} = ?")
        }

        private fun projectionForTraitDataForEvaluationSubQuery(traitId: Int) = buildString {
            appendLine("${FMT_TRAIT_NAME_XX.format(traitId)} AS ${FMT_TRAIT_XX_ID.format(traitId)},")
            // Because there is not a units columns for each trait ID, so we gate those IDs known to have them here.
            if (traitId in TRAIT_ID_UNITS_TRAITS_FIRST .. TRAIT_ID_UNITS_TRAITS_LAST) {
                appendLine("${FMT_TRAIT_UNITS_XX.format(traitId)} AS ${FMT_TRAIT_XX_UNITS_ID.format(traitId)},")
            }
            appendLine("${FMT_TRAIT_NAME_XX_OPTIONAL.format(traitId)} AS ${FMT_TRAIT_XX_OPTIONAL.format(traitId)},")
            append("${FMT_TRAIT_NAME_XX_DEFERRED.format(traitId)} AS ${FMT_TRAIT_XX_DEFERRED.format(traitId)}")
        }

        //endregion

        //region SAVED EVALUATION AND TRAIT READERS

        private suspend fun savedEvaluationFromCursors(
            scoredTraitCursor: Cursor,
            unitsTraitCursor: Cursor,
            customTraitCursor: Cursor,
            customTraitOptionLookup: suspend (EntityId) -> List<EvalTraitOption>
        ): SavedEvaluation? {
            scoredTraitCursor.moveToFirst()
            unitsTraitCursor.moveToFirst()
            customTraitCursor.moveToFirst()
            return if(scoredTraitCursor.isFirst) {
                val contactId = scoredTraitCursor.getOptEntityId(SavedEvaluationTable.Columns.CONTACT_ID)
                val companyId = scoredTraitCursor.getOptEntityId(SavedEvaluationTable.Columns.COMPANY_ID)
                SavedEvaluation(
                    id = scoredTraitCursor.getEntityId(SavedEvaluationTable.Columns.ID),
                    name = scoredTraitCursor.getString(SavedEvaluationTable.Columns.NAME),
                    summarizeInAlert = scoredTraitCursor.getBoolean(SavedEvaluationTable.Columns.SUMMARIZE_IN_ALERT),
                    userId = when {
                        contactId != null && contactId.isValid -> contactId
                        companyId != null && companyId.isValid -> companyId
                        else -> throw IllegalStateException("Unable to determine user id. Contact and company are both 0.")
                    },
                    userType = when {
                        contactId?.isValid == true -> UserType.CONTACT
                        companyId?.isValid == true -> UserType.COMPANY
                        else -> throw IllegalStateException("Unable to determine user type. Contact and company are both 0.")
                    },
                    trait01 = scoredTraitCursor.scoredTraitFromCursor(1),
                    trait02 = scoredTraitCursor.scoredTraitFromCursor(2),
                    trait03 = scoredTraitCursor.scoredTraitFromCursor(3),
                    trait04 = scoredTraitCursor.scoredTraitFromCursor(4),
                    trait05 = scoredTraitCursor.scoredTraitFromCursor(5),
                    trait06 = scoredTraitCursor.scoredTraitFromCursor(6),
                    trait07 = scoredTraitCursor.scoredTraitFromCursor(7),
                    trait08 = scoredTraitCursor.scoredTraitFromCursor(8),
                    trait09 = scoredTraitCursor.scoredTraitFromCursor(9),
                    trait10 = scoredTraitCursor.scoredTraitFromCursor(10),
                    trait11 = unitsTraitCursor.unitsTraitFromCursor(11),
                    trait12 = unitsTraitCursor.unitsTraitFromCursor(12),
                    trait13 = unitsTraitCursor.unitsTraitFromCursor(13),
                    trait14 = unitsTraitCursor.unitsTraitFromCursor(14),
                    trait15 = unitsTraitCursor.unitsTraitFromCursor(15),
                    trait16 = customTraitCursor.customTraitFromCursor(16, customTraitOptionLookup),
                    trait17 = customTraitCursor.customTraitFromCursor(17, customTraitOptionLookup),
                    trait18 = customTraitCursor.customTraitFromCursor(18, customTraitOptionLookup),
                    trait19 = customTraitCursor.customTraitFromCursor(19, customTraitOptionLookup),
                    trait20 = customTraitCursor.customTraitFromCursor(20, customTraitOptionLookup),
                )
            } else { null }
        }

        private fun idColumnFor(traitId: Int): Column.Nullable {
            return Column.Nullable(FMT_TRAIT_XX_ID.format(traitId))
        }

        private fun nameColumnFor(traitId: Int): Column.Nullable {
            return Column.Nullable(FMT_TRAIT_XX_NAME.format(traitId))
        }

        private fun typeColumnFor(traitId: Int): Column.Nullable {
            return Column.Nullable(FMT_TRAIT_XX_TYPE_ID.format(traitId))
        }

        private fun optionalColumnFor(traitId: Int): Column.Nullable {
            return Column.Nullable(FMT_TRAIT_XX_OPTIONAL.format(traitId))
        }

        private fun deferredColumnFor(traitId: Int): Column.Nullable {
            return Column.Nullable(FMT_TRAIT_XX_DEFERRED.format(traitId))
        }

        private fun unitsIdColumnFor(traitId: Int): Column.Nullable {
            return Column.Nullable(FMT_TRAIT_XX_UNITS_ID.format(traitId))
        }

        private fun unitsNameColumnFor(traitId: Int): Column.Nullable {
            return Column.Nullable(FMT_TRAIT_XX_UNITS_NAME.format(traitId))
        }

        private fun unitsAbbrColumnFor(traitId: Int): Column.Nullable {
            return Column.Nullable(FMT_TRAIT_XX_UNITS_ABBR.format(traitId))
        }

        private fun unitsTypeColumnFor(traitId: Int): Column.Nullable {
            return Column.Nullable(FMT_TRAIT_XX_UNIT_TYPE_ID.format(traitId))
        }

        private fun unitTypeIdColumnFor(traitId: Int): Column.Nullable {
            return Column.Nullable(FMT_TRAIT_XX_UNIT_TYPE_ID.format(traitId))
        }

        private fun unitTypeNameColumnFor(traitId: Int): Column.Nullable {
            return Column.Nullable(FMT_TRAIT_XX_UNIT_TYPE_NAME.format(traitId))
        }

        private fun Cursor.scoredTraitFromCursor(traitId: Int): BasicEvalTrait? {
            if (!isFirst) {
                return null
            }
            val idColumn = idColumnFor(traitId)
            if (isNull(idColumn)) {
                return null
            }
            return BasicEvalTrait.from(
                id = getOptEntityId(idColumn),
                name = getOptString(nameColumnFor(traitId)),
                typeId = getOptEntityId(typeColumnFor(traitId)),
                isOptional = getOptBoolean(optionalColumnFor(traitId)),
                isDeferred = getOptBoolean(deferredColumnFor(traitId))
            )
        }

        private fun Cursor.unitsTraitFromCursor(traitId: Int): UnitsEvalTrait? {
            if (isBeforeFirst || isAfterLast) {
                return null
            }
            val idColumn = idColumnFor(traitId)
            val unitsIdColumn = unitsIdColumnFor(traitId)
            val unitsTypeIdColumnName = unitsTypeColumnFor(traitId)
            if (isNull(idColumn) || isNull(unitsIdColumn) || isNull(unitsTypeIdColumnName)) {
                return null
            }
            return UnitsEvalTrait.from(
                id = getOptEntityId(idColumn),
                name = getOptString(nameColumnFor(traitId)),
                typeId = getOptEntityId(typeColumnFor(traitId)),
                units = unitOfMeasureFromCursor(traitId),
                isOptional = getOptBoolean(optionalColumnFor(traitId)),
                isDeferred = getOptBoolean(deferredColumnFor(traitId))
            )
        }

        private fun Cursor.unitOfMeasureFromCursor(traitId: Int): UnitOfMeasure? {
            return UnitOfMeasure.from(
                id = getOptEntityId(unitsIdColumnFor(traitId)),
                name = getOptString(unitsNameColumnFor(traitId)),
                abbreviation = getOptString(unitsAbbrColumnFor(traitId)),
                type = UnitOfMeasure.Type.from(
                    id = getOptEntityId(unitTypeIdColumnFor(traitId)),
                    name = getOptString(unitTypeNameColumnFor(traitId))
                )
            )
        }

        private suspend fun Cursor.customTraitFromCursor(
            traitId: Int,
            optionsLookup: suspend (EntityId) -> List<EvalTraitOption>
        ): CustomEvalTrait? {
            if (!isFirst) {
                return null
            }
            val idColumn = idColumnFor(traitId)
            if (isNull(idColumn)) {
                return null
            }
            val customTraitId = getOptEntityId(idColumn)
            return CustomEvalTrait.from(
                id = customTraitId,
                name = getOptString(nameColumnFor(traitId)),
                typeId = getOptEntityId(typeColumnFor(traitId)),
                isOptional = getOptBoolean(optionalColumnFor(traitId)),
                isDeferred = getOptBoolean(deferredColumnFor(traitId)),
                options = customTraitId?.let { optionsLookup(it) }
            )
        }

        private fun evalTraitOptionFromCursor(cursor: Cursor): EvalTraitOption {
            return EvalTraitOption(
                id = cursor.getEntityId(CustomEvalTraitsTable.Columns.ID),
                traitId = cursor.getEntityId(CustomEvalTraitsTable.Columns.TRAIT_ID),
                name = cursor.getString(CustomEvalTraitsTable.Columns.ITEM),
                order = cursor.getInt(CustomEvalTraitsTable.Columns.ORDER)
            )
        }

        //endregion
    }
}
