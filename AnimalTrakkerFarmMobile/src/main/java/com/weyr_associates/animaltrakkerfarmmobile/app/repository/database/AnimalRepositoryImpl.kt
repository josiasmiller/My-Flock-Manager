package com.weyr_associates.animaltrakkerfarmmobile.app.repository.database

import android.content.ContentValues
import android.database.Cursor
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.validation.IdFormat
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.coroutines.awaitAll
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.AnimalRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.DefaultSettingsRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.AnimalRepositoryImpl.Companion.QUALIFIER_TO_FILTER_SPECIAL_MARKER_ANIMALS
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseHandler
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Sql
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getBoolean
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getFloat
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getInt
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getLocalDate
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getLocalTime
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getOptEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getOptFloat
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getOptInt
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getOptLocalDate
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getOptLocalTime
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getOptString
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getString
import com.weyr_associates.animaltrakkerfarmmobile.database.core.insertWithPK
import com.weyr_associates.animaltrakkerfarmmobile.database.core.insertWithPKOrThrow
import com.weyr_associates.animaltrakkerfarmmobile.database.core.isNull
import com.weyr_associates.animaltrakkerfarmmobile.database.core.put
import com.weyr_associates.animaltrakkerfarmmobile.database.core.putNull
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readAllItems
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readFirstItem
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readItem
import com.weyr_associates.animaltrakkerfarmmobile.database.queries.AnimalLastEvaluationOfTrait
import com.weyr_associates.animaltrakkerfarmmobile.database.queries.queryAnimalBirthDate
import com.weyr_associates.animaltrakkerfarmmobile.database.queries.queryAnimalDeathDate
import com.weyr_associates.animaltrakkerfarmmobile.database.queries.queryAnimalIdHistory
import com.weyr_associates.animaltrakkerfarmmobile.database.queries.queryAnimalIds
import com.weyr_associates.animaltrakkerfarmmobile.database.queries.queryAnimalMovementHistory
import com.weyr_associates.animaltrakkerfarmmobile.database.queries.queryFemaleBreedingDetails
import com.weyr_associates.animaltrakkerfarmmobile.database.queries.queryLatestAnimalMovement
import com.weyr_associates.animaltrakkerfarmmobile.database.queries.queryMaleBreedingDetails
import com.weyr_associates.animaltrakkerfarmmobile.database.queries.queryOffspringCountBySexForDam
import com.weyr_associates.animaltrakkerfarmmobile.database.queries.queryOffspringCountBySexForSire
import com.weyr_associates.animaltrakkerfarmmobile.database.queries.queryOffspringOfDam
import com.weyr_associates.animaltrakkerfarmmobile.database.queries.queryOffspringOfSire
import com.weyr_associates.animaltrakkerfarmmobile.database.queries.querySexIdForAnimal
import com.weyr_associates.animaltrakkerfarmmobile.database.queries.querySpeciesById
import com.weyr_associates.animaltrakkerfarmmobile.database.queries.queryWeanedCountBySexForDam
import com.weyr_associates.animaltrakkerfarmmobile.database.sql.OwnerUnion
import com.weyr_associates.animaltrakkerfarmmobile.database.sql.readIdInfo
import com.weyr_associates.animaltrakkerfarmmobile.database.sql.readServiceType
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.AnimalAlertTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.AnimalBreedTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.AnimalDrugTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.AnimalEvaluationTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.AnimalFemaleBreedingTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.AnimalFlockPrefixTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.AnimalGeneticCharacteristicTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.AnimalIdInfoTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.AnimalLocationHistoryTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.AnimalMaleBreedingTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.AnimalNoteTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.AnimalOwnershipHistoryTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.AnimalRegistrationTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.AnimalTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.AnimalTissueSampleTakenTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.AnimalTissueTestRequestTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.BirthTypeTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.BreedTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.CompanyLaboratoryTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.CompanyTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.ContactTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.CustomEvalTraitsTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.DeathReasonTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.DrugLotTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.DrugTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.EvalTraitTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.FlockPrefixTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.GeneticCharacteristicCalculationMethodTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.GeneticCharacteristicTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.GeneticCoatColorTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.GeneticCodonTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.GeneticHornTypeTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.IdColorTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.IdLocationTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.IdRemoveReasonTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.IdTypeTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.PredefinedNoteTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.ScrapieFlockNumberTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.ServiceTypeTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.SexTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.SpeciesTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.TissueSampleTypeTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.TissueTestTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.UnitsTable
import com.weyr_associates.animaltrakkerfarmmobile.model.Animal
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalAlert
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalBasicInfo
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalBreed
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalBreeders
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalDeath
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalDetails
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalDrugEvent
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalEvaluation
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalGeneticCharacteristic
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalGeneticCharacteristics
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalLifetime
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalLocationEvent
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalMovement
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalName
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalNote
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalOwnership
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalParentage
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalRearing
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalWeight
import com.weyr_associates.animaltrakkerfarmmobile.model.BirthEvent
import com.weyr_associates.animaltrakkerfarmmobile.model.BirthType
import com.weyr_associates.animaltrakkerfarmmobile.model.BreedPart
import com.weyr_associates.animaltrakkerfarmmobile.model.Breeder
import com.weyr_associates.animaltrakkerfarmmobile.model.BreederInfo
import com.weyr_associates.animaltrakkerfarmmobile.model.BreedingSummary
import com.weyr_associates.animaltrakkerfarmmobile.model.CoatColorCharacteristic
import com.weyr_associates.animaltrakkerfarmmobile.model.Codon
import com.weyr_associates.animaltrakkerfarmmobile.model.CodonCharacteristic
import com.weyr_associates.animaltrakkerfarmmobile.model.DeathEvent
import com.weyr_associates.animaltrakkerfarmmobile.model.DeathReason
import com.weyr_associates.animaltrakkerfarmmobile.model.DrugWithdrawal
import com.weyr_associates.animaltrakkerfarmmobile.model.DrugWithdrawalAlert
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.EvalTrait
import com.weyr_associates.animaltrakkerfarmmobile.model.EvaluationSummary
import com.weyr_associates.animaltrakkerfarmmobile.model.EvaluationSummaryAlert
import com.weyr_associates.animaltrakkerfarmmobile.model.FemaleBreeding
import com.weyr_associates.animaltrakkerfarmmobile.model.FemaleBreedingDetails
import com.weyr_associates.animaltrakkerfarmmobile.model.FemaleBreedingHistoryEntry
import com.weyr_associates.animaltrakkerfarmmobile.model.Gap
import com.weyr_associates.animaltrakkerfarmmobile.model.GeneticCharacteristic
import com.weyr_associates.animaltrakkerfarmmobile.model.HornTypeCharacteristic
import com.weyr_associates.animaltrakkerfarmmobile.model.IdInfo
import com.weyr_associates.animaltrakkerfarmmobile.model.IdRemoveReason
import com.weyr_associates.animaltrakkerfarmmobile.model.IdType
import com.weyr_associates.animaltrakkerfarmmobile.model.MaleBreeding
import com.weyr_associates.animaltrakkerfarmmobile.model.MaleBreedingDetails
import com.weyr_associates.animaltrakkerfarmmobile.model.MovementEvent
import com.weyr_associates.animaltrakkerfarmmobile.model.MovementEvent.Chronology.AFTER_DEATH
import com.weyr_associates.animaltrakkerfarmmobile.model.MovementEvent.Chronology.BEFORE_BIRTH
import com.weyr_associates.animaltrakkerfarmmobile.model.MovementEvent.Chronology.IN_LIFE_TIME
import com.weyr_associates.animaltrakkerfarmmobile.model.Owner
import com.weyr_associates.animaltrakkerfarmmobile.model.ParentInfo
import com.weyr_associates.animaltrakkerfarmmobile.model.RearType
import com.weyr_associates.animaltrakkerfarmmobile.model.ServiceType
import com.weyr_associates.animaltrakkerfarmmobile.model.Sex
import com.weyr_associates.animaltrakkerfarmmobile.model.SexStandard
import com.weyr_associates.animaltrakkerfarmmobile.model.SireInfo
import com.weyr_associates.animaltrakkerfarmmobile.model.Species
import com.weyr_associates.animaltrakkerfarmmobile.model.TissueSampleEvent
import com.weyr_associates.animaltrakkerfarmmobile.model.TissueTestEvent
import com.weyr_associates.animaltrakkerfarmmobile.model.TransferReason
import com.weyr_associates.animaltrakkerfarmmobile.model.UnitOfMeasure
import com.weyr_associates.animaltrakkerfarmmobile.model.UserDefinedAlert
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class AnimalRepositoryImpl(
    private val databaseHandler: DatabaseHandler,
    private val defaultSettingsRepository: DefaultSettingsRepository
) : AnimalRepository {

    private val notifyTopicChanges = MutableSharedFlow<TopicChange>(
        extraBufferCapacity = TOPIC_NOTIFICATION_EXTRA_BUFFER_CAP
    )

    override suspend fun searchAnimalsByIdType(
        partialId: String,
        idTypeId: EntityId,
        speciesId: EntityId,
        sexStandard: SexStandard?
    ): List<AnimalBasicInfo> {
        return withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.rawQuery(
                SQL_SEARCH_ANIMAL_BASIC_INFO_BY_ID_TYPE.takeIf { sexStandard == null }
                    ?: SQL_SEARCH_ANIMAL_BASIC_INFO_BY_ID_TYPE_AND_SEX_STANDARD,
                appendOptionalSexStandardParam(
                    arrayOf(
                        speciesId.toString(),
                        idTypeId.toString(),
                        Sql.escapeWildcards(partialId)
                    ),
                    sexStandard
                )
            ).use { it.readAllItems(::animalBasicInfoFrom) }
        }
    }

    override suspend fun searchAnimalsByName(
        partialName: String,
        speciesId: EntityId,
        sexStandard: SexStandard?
    ): List<AnimalBasicInfo> {
        return withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.rawQuery(
                SQL_SEARCH_ANIMAL_BASIC_INFO_BY_NAME.takeIf { sexStandard == null}
                    ?: SQL_SEARCH_ANIMAL_BASIC_INFO_BY_NAME_AND_SEX_STANDARD,
                appendOptionalSexStandardParam(
                    arrayOf(
                        speciesId.toString(),
                        Sql.escapeWildcards(partialName)
                    ),
                    sexStandard
                )
            ).use { it.readAllItems(::animalBasicInfoFrom) }
        }
    }

    override suspend fun queryEIDExistence(eidNumber: String): Boolean {
        return withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.rawQuery(
                SQL_QUERY_EXISTENCE_OF_EID,
                arrayOf(eidNumber)
            ).use { cursor ->
                cursor.moveToFirst() && cursor.getBoolean(COLUMN_NAME_EID_EXISTS)
            }
        }
    }

    override suspend fun queryEIDExistenceForUpdate(idToUpdate: EntityId, eidNumber: String): Boolean {
        return withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.rawQuery(
                SQL_QUERY_EXISTENCE_OF_EID_EXCEPT_FOR_ID,
                arrayOf(eidNumber, idToUpdate.toString())
            ).use { cursor ->
                cursor.moveToFirst() && cursor.getBoolean(COLUMN_NAME_EID_EXISTS)
            }
        }
    }

    override fun queryAnimalCurrentOwnership(animalId: EntityId): AnimalOwnership? {
        return databaseHandler.readableDatabase.rawQuery(
            AnimalOwnershipHistoryTable.Sql.QUERY_CURRENT_ANIMAL_OWNERSHIP_BY_ANIMAL_ID,
            arrayOf(animalId.toString())
        ).use { cursor ->
            cursor.readFirstItem(AnimalOwnershipHistoryTable::animalOwnershipFromCursor)
        }
    }

    override fun queryAnimalOwnershipOnDate(animalId: EntityId, date: LocalDate): AnimalOwnership? {
        return databaseHandler.readableDatabase.rawQuery(
            AnimalOwnershipHistoryTable.Sql.QUERY_ANIMAL_OWNERSHIP_ON_DATE,
            arrayOf(animalId.toString(), Sql.formatDate(date))
        ).use { cursor ->
            cursor.readFirstItem(AnimalOwnershipHistoryTable::animalOwnershipFromCursor)
        }
    }

    override fun queryAnimalCurrentPremise(animalId: EntityId): EntityId? {
        return databaseHandler.readableDatabase.rawQuery(
            AnimalLocationHistoryTable.Sql.QUERY_ANIMAL_CURRENT_LOCATION_PREMISE_ID,
            arrayOf(animalId.toString())
        ).use { cursor ->
            cursor.takeIf { it.moveToFirst() }?.getOptEntityId(
                AnimalLocationHistoryTable.Columns.TO_PREMISE_ID
            )
        }
    }

    override fun animalCurrentPremiseId(animalId: EntityId): Flow<EntityId?> = createQueryFlowFor(
        pertinentTopics = topicChange(animalId) { add(Topic.Animal.LOCATION) },
        queryBlock = { queryAnimalCurrentPremise(animalId) }
    )

    override fun queryAnimalLatestMovement(animalId: EntityId): AnimalMovement? {
        return databaseHandler.readableDatabase.queryLatestAnimalMovement(animalId)
    }

    override fun animalLatestMovement(animalId: EntityId): Flow<AnimalMovement?> = createQueryFlowFor(
        pertinentTopics = topicChange(animalId) { add(Topic.Animal.LOCATION) },
        queryBlock = { queryAnimalLatestMovement(animalId) }
    )

    override fun queryAnimalMovementHistory(animalId: EntityId): List<AnimalMovement> {
        return databaseHandler.readableDatabase.queryAnimalMovementHistory(animalId)
    }

    override fun animalMovementHistory(animalId: EntityId): Flow<List<AnimalMovement>> = createQueryFlowFor(
        pertinentTopics = topicChange(animalId) { add(Topic.Animal.LOCATION) },
        queryBlock = { queryAnimalMovementHistory(animalId) }
    )

    override fun queryAnimalLocationTimeline(animalId: EntityId): List<AnimalLocationEvent> {

        //Look up movements based on animal location history and the animal's birth and death dates.
        val movements = databaseHandler.readableDatabase.queryAnimalMovementHistory(animalId)
        val birthDate = databaseHandler.readableDatabase.queryAnimalBirthDate(animalId)
        val deathDate = databaseHandler.readableDatabase.queryAnimalDeathDate(animalId)

        //Establish buckets into which we will filter movements
        //that occurred before/after death and during the animal's lifetime.
        //This will make it easier to establish issues with the movements later
        //when we map them to timeline events.
        val movementsAfterDeath = mutableListOf<AnimalMovement>()
        val movementsBeforeBirth = mutableListOf<AnimalMovement>()
        val movementsInLifetime = mutableListOf<AnimalMovement>()

        //Track movements related to animal birth and death dates if they align.
        var deathMovement: AnimalMovement? = null
        var birthMovement: AnimalMovement? = null

        movements.forEach { movement ->
            when {
                deathDate != null && movement.movementDate.isAfter(deathDate) -> {
                   movementsAfterDeath.add(movement)
                }
                deathDate != null && movement.movementDate.isEqual(deathDate) && movement.toPremise == null -> {
                    deathMovement?.let {
                        movementsAfterDeath.add(it)
                    }
                    deathMovement = movement
                }
                birthDate != null && movement.movementDate.isBefore(birthDate) -> {
                    movementsBeforeBirth.add(movement)
                }
                birthDate != null && movement.movementDate.isEqual(birthDate) && movement.fromPremise == null -> {
                    if (birthMovement == null) {
                        birthMovement = movement
                    } else {
                        movementsBeforeBirth.add(movement)
                    }
                }
                else -> {
                    movementsInLifetime.add(movement)
                }
            }
        }
        val events = mutableListOf<AnimalLocationEvent>()
        movementsAfterDeath.forEachIndexed { index, movement ->
            val movementEvent = MovementEvent(
                movement = movement,
                chronology = AFTER_DEATH
            )
            if (0 < index) {
                val nextMovement = movementsAfterDeath[index - 1]
                Gap.extractGapBetween(movement, nextMovement)
                    ?.let { events.add(it) }
            }
            events.add(movementEvent)
        }
        if (deathDate != null) {
            events.add(DeathEvent(deathDate, deathMovement?.fromPremise))
        }
        movementsInLifetime.forEachIndexed { index, movement ->
            val movementEvent = MovementEvent(
                movement = movement,
                chronology = IN_LIFE_TIME
            )
            val nextMovement = if (index == 0) {
                 deathMovement ?: movementsAfterDeath.lastOrNull()
            } else {
                movementsInLifetime[index - 1]
            }
            nextMovement?.let {
                Gap.extractGapBetween(movement, nextMovement)
                    ?.let { events.add(it) }
            }
            events.add(movementEvent)
        }
        if (birthDate != null) {
            events.add(BirthEvent(birthDate, birthMovement?.toPremise))
        }
        movementsBeforeBirth.forEachIndexed { index, movement ->
            val movementEvent = MovementEvent(
                movement = movement,
                chronology = BEFORE_BIRTH
            )
            val nextMovement = if (index == 0) {
                birthMovement ?: movementsInLifetime.lastOrNull()
                    ?: deathMovement ?: movementsAfterDeath.lastOrNull()
            } else {
                movementsBeforeBirth[index - 1]
            }
            nextMovement?.let {
                Gap.extractGapBetween(movement, nextMovement)
                    ?.let { events.add(it) }
            }
            events.add(movementEvent)
        }
        return events
    }

    override fun animalLocationTimeline(animalId: EntityId): Flow<List<AnimalLocationEvent>> = createQueryFlowFor(
        pertinentTopics = topicChange(animalId) {
            add(Topic.Animal.BIRTH_DATE)
            add(Topic.Animal.DEATH_DATE)
            add(Topic.Animal.LOCATION)
        },
        queryBlock = { queryAnimalLocationTimeline(animalId) }
    )

    override fun moveAnimalToPremise(
        animalId: EntityId,
        premiseId: EntityId,
        timeStamp: LocalDateTime
    ) {
        val movementDateString = Sql.formatDate(timeStamp)
        val timeStampString = Sql.formatDateTime(timeStamp)
        with(databaseHandler.writableDatabase) {
            beginTransaction()
            try {
                insertWithPKOrThrow(
                    AnimalLocationHistoryTable,
                    null,
                    ContentValues().apply {
                        put(AnimalLocationHistoryTable.Columns.ANIMAL_ID, animalId)
                        put(AnimalLocationHistoryTable.Columns.MOVEMENT_DATE, movementDateString)
                        put(
                            AnimalLocationHistoryTable.Columns.FROM_PREMISE_ID,
                            queryAnimalCurrentPremise(animalId)
                        )
                        put(AnimalLocationHistoryTable.Columns.TO_PREMISE_ID, premiseId)
                        put(AnimalLocationHistoryTable.Columns.CREATED, timeStampString)
                        put(AnimalLocationHistoryTable.Columns.MODIFIED, timeStampString)
                    }
                )
                setTransactionSuccessful()
                notifyTopicChanges.tryEmit(
                    topicChange(animalId) {
                        add(Topic.Animal.LOCATION)
                    }
                )
            } finally {
                endTransaction()
            }
        }
    }

    override fun queryAnimalName(animalId: EntityId): AnimalName? {
        return databaseHandler.readableDatabase.rawQuery(
            SQL_QUERY_ANIMAL_NAME,
            arrayOf(animalId.toString())
        ).use { cursor ->
            cursor.readFirstItem(::animalNameFromCursor)
        }
    }

    override fun queryAnimalIds(animalId: EntityId): List<IdInfo> {
        return databaseHandler.readableDatabase.queryAnimalIds(animalId)
    }

    override fun queryAnimalIdHistory(animalId: EntityId): List<IdInfo> {
        return databaseHandler.readableDatabase.queryAnimalIdHistory(animalId)
    }

    override fun animalIdHistory(animalId: EntityId): Flow<List<IdInfo>> = createQueryFlowFor(
        pertinentTopics = topicChange(animalId) { add(Topic.Animal.IDS) },
        queryBlock = { queryAnimalIdHistory(animalId) }
    )

    override fun queryAnimalFlockPrefix(animalId: EntityId, registryCompanyId: EntityId): String {
        return databaseHandler.readableDatabase.rawQuery(
            SQL_QUERY_FLOCK_PREFIX_FOR_ANIMAL,
            arrayOf(animalId.toString(), registryCompanyId.toString())
        ).use { cursor ->
            cursor.takeIf { it.moveToFirst() }
                ?.getString(FlockPrefixTable.Columns.PREFIX) ?: ""
        }
    }

    override suspend fun queryAnimalBasicInfoByAnimalId(animalId: EntityId): AnimalBasicInfo? {
        return withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.rawQuery(
                SQL_QUERY_ANIMAL_BASIC_INFO_BY_ANIMAL_ID,
                arrayOf(animalId.toString())
            ).use { cursor ->
                cursor.readFirstItem(::animalBasicInfoFrom)
            }
        }
    }

    override fun animalBasicInfoByAnimalId(animalId: EntityId): Flow<AnimalBasicInfo?> = createQueryFlowFor(
        pertinentTopics = topicChange(animalId) { add(Topic.Animal.INFO) },
        queryBlock = { queryAnimalBasicInfoByAnimalId(animalId) }
    )

    override suspend fun queryAnimalBasicInfoByEID(eidNumber: String): AnimalBasicInfo? {
        return withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.rawQuery(
                SQL_QUERY_ANIMAL_BASIC_INFO_BY_EID,
                arrayOf(eidNumber)
            ).use { cursor ->
                cursor.readFirstItem(::animalBasicInfoFrom)
            }
        }
    }

    override fun animalBasicInfoByEID(eidNumber: String): Flow<AnimalBasicInfo?> = channelFlow {
        val animalBasicInfo = withContext(Dispatchers.IO) {
            queryAnimalBasicInfoByEID(eidNumber).also { send(it) }
        }
        if (animalBasicInfo != null) {
            val pertinentTopics = topicChange(animalBasicInfo.id) { add(Topic.Animal.INFO) }
            notifyTopicChanges.filter { it.covers(pertinentTopics) }
                .collectLatest {
                    withContext(Dispatchers.IO) {
                        send(queryAnimalBasicInfoByAnimalId(animalBasicInfo.id))
                    }
                }
        }
    }

    override suspend fun queryAnimalDetailsByAnimalId(animalId: EntityId): AnimalDetails? {
        return withContext(Dispatchers.IO) {
            val basicInfoDeferred = async { queryAnimalBasicInfoByAnimalId(animalId) }
            val lifetimeDeferred = async { queryAnimalLifetime(animalId) }
            val rearingDeferred = async { queryAnimalRearing(animalId) }
            val parentageDeferred = async { queryAnimalParentage(animalId) }
            val breedersDeferred = async { queryAnimalBreeders(animalId) }
            val weightDeferred = async { queryAnimalLastEvaluationWeight(animalId) }
            awaitAll(
                basicInfoDeferred,
                lifetimeDeferred,
                rearingDeferred,
                parentageDeferred,
                breedersDeferred,
                weightDeferred
            ) { basicInfo, lifetime, rearing, parentage, breeders, weight ->
                when {
                    basicInfo != null && lifetime != null && rearing != null -> {
                        AnimalDetails(basicInfo, lifetime, rearing, parentage, breeders, weight)
                    }
                    else -> null
                }
            }
        }
    }

    override fun queryAnimalSexId(animalId: EntityId): EntityId? {
        return databaseHandler.readableDatabase.querySexIdForAnimal(animalId)
    }

    override fun queryAnimalSpeciesId(animalId: EntityId): EntityId? {
        return databaseHandler.readableDatabase.rawQuery(
            SQL_QUERY_ANIMAL_SPECIES_ID,
            arrayOf(animalId.toString())
        ).use { cursor ->
            cursor.readFirstItem {
                it.getEntityId(SpeciesTable.Columns.ID)
            }
        }
    }

    override fun queryAnimalSpecies(animalId: EntityId): Species? {
        return queryAnimalSpeciesId(animalId)?.let { speciesId ->
            databaseHandler.readableDatabase.querySpeciesById(speciesId)
        }
    }

    override suspend fun queryAnimalLifetime(animalId: EntityId): AnimalLifetime? {
        return withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.rawQuery(
                SQL_QUERY_ANIMAL_LIFETIME,
                arrayOf(animalId.toString())
            ).use { cursor ->
                cursor.readFirstItem(::animalLifetimeFromCursor)
            }
        }
    }

    override suspend fun queryAnimalRearing(animalId: EntityId): AnimalRearing? {
        return withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.rawQuery(
                SQL_QUERY_ANIMAL_REARING,
                arrayOf(animalId.toString())
            ).use { cursor ->
                cursor.readFirstItem(::animalRearingFromCursor)
            }
        }
    }

    override suspend fun queryAnimalParentage(animalId: EntityId): AnimalParentage? {
        return withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.rawQuery(
                SQL_QUERY_ANIMAL_PARENTAGE,
                arrayOf(animalId.toString())
            ).use { cursor ->
                cursor.readFirstItem(::animalParentageFromCursor)
            }
        }
    }

    override suspend fun queryAnimalBreeders(animalId: EntityId): AnimalBreeders? {
        return withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.rawQuery(
                SQL_QUERY_ANIMAL_BREEDERS,
                arrayOf(animalId.toString())
            ).use { cursor ->
                cursor.readFirstItem(::animalBreedersFromCursor)
            }
        }
    }

    override fun queryAnimalBreedInfo(animalId: EntityId): List<AnimalBreed> {
        return databaseHandler.readableDatabase.rawQuery(
            SQL_QUERY_ANIMAL_BREEDING_FOR_ANIMAL,
            arrayOf(animalId.toString())
        ).use { cursor ->
            cursor.readAllItems(::animalBreedFromCursor)
        }
    }

    override fun updateAnimalBreedInfo(
        animalId: EntityId,
        breedInfo: List<BreedPart>,
        timeStamp: LocalDateTime
    ) {
        val timeStampString = Sql.formatDateTime(timeStamp)
        databaseHandler.writableDatabase.run {
            beginTransaction()
            try {
                delete(
                    AnimalBreedTable.NAME,
                    "${AnimalBreedTable.Columns.ANIMAL_ID} = ?",
                    arrayOf(animalId.toString())
                )
                breedInfo.forEach {
                    insertWithPKOrThrow(
                        AnimalBreedTable,
                        null,
                        ContentValues().apply {
                            put(AnimalBreedTable.Columns.ANIMAL_ID, animalId)
                            put(AnimalBreedTable.Columns.BREED_ID, it.breedId)
                            put(AnimalBreedTable.Columns.BREED_PERCENTAGE, Sql.floatWithPrecision(it.percentage))
                            put(AnimalBreedTable.Columns.CREATED, timeStampString)
                            put(AnimalBreedTable.Columns.MODIFIED, timeStampString)
                        }
                    )
                }
                setTransactionSuccessful()
            } finally {
                endTransaction()
            }
        }
    }

    override suspend fun queryAnimalLastEvaluationWeight(animalId: EntityId): AnimalWeight? {
        return withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.rawQuery(
                AnimalLastEvaluationOfTrait.SQL_QUERY_ANIMAL_LAST_EVALUATION_OF_UNIT_TRAIT,
                arrayOf(animalId.toString(), EvalTrait.UNIT_TRAIT_ID_WEIGHT_RAW)
            ).use { cursor ->
                cursor.takeIf { it.moveToFirst() }?.readFirstItem(::animalWeightFromCursor)
            }
        }
    }

    override suspend fun queryAnimalNoteHistory(animalId: EntityId): List<AnimalNote> {
        return withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.rawQuery(
                SQL_QUERY_ANIMAL_NOTE_HISTORY,
                arrayOf(animalId.toString())
            ).use { cursor ->
                cursor.readAllItems(::animalNotesFromCursor)
                    .flatten()
            }
        }
    }

    override suspend fun queryAnimalDrugHistory(animalId: EntityId): List<AnimalDrugEvent> {
        return withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.rawQuery(
                SQL_QUERY_ANIMAL_DRUG_HISTORY,
                arrayOf(animalId.toString())
            ).use { cursor ->
                cursor.readAllItems(::animalDrugEventFromCursor)
            }
        }
    }

    override suspend fun queryAnimalTissueSampleHistory(animalId: EntityId): List<TissueSampleEvent> {
        return withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.rawQuery(
                SQL_QUERY_ANIMAL_TISSUE_SAMPLE_HISTORY,
                arrayOf(animalId.toString())
            ).use { cursor ->
                cursor.readAllItems(::tissueSampleEventFromCursor)
            }
        }
    }

    override suspend fun queryAnimalTissueTestHistory(animalId: EntityId): List<TissueTestEvent> {
        return withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.rawQuery(
                SQL_QUERY_ANIMAL_TISSUE_TEST_HISTORY,
                arrayOf(animalId.toString())
            ).use { cursor ->
                cursor.readAllItems(::tissueTestEventFromCursor)
            }
        }
    }

    override suspend fun queryAnimalEvaluationHistory(animalId: EntityId): List<AnimalEvaluation> {
        return withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.rawQuery(
                SQL_QUERY_ANIMAL_EVALUATIONS,
                arrayOf(animalId.toString())
            ).use { cursor ->
                cursor.readAllItems(::animalEvaluationFromCursor)
            }
        }
    }

    override suspend fun queryAnimalAlerts(animalId: EntityId): List<AnimalAlert> {
        return withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.rawQuery(
                SQL_QUERY_ANIMAL_ALERTS,
                arrayOf(animalId.toString())
            ).use { cursor ->
                cursor.readAllItems(::animalAlertFromCursor)
            }
        }
    }

    override suspend fun queryGeneticCharacteristics(animalId: EntityId): AnimalGeneticCharacteristics {
        return withContext(Dispatchers.IO) {
            val geneticCharacteristics = databaseHandler.readableDatabase.rawQuery(
                SQL_QUERY_GENETIC_CHARACTERISTICS,
                arrayOf(animalId.toString())
            ).use { cursor ->
                cursor.readAllItems(::animalCharacteristicFromCursor)
            }.filterNotNull()
            AnimalGeneticCharacteristics(animalId, geneticCharacteristics)
        }
    }

    override fun addCoatColorCharacteristicForAnimal(
        animalId: EntityId,
        valueId: EntityId,
        methodId: EntityId,
        timeStamp: LocalDateTime
    ) {
        val dateString = Sql.formatDate(timeStamp)
        val timeString = Sql.formatTime(timeStamp)
        val timeStampString = Sql.formatDateTime(timeStamp)
        databaseHandler.writableDatabase.run {
            beginTransaction()
            try {
                insertWithPKOrThrow(
                    AnimalGeneticCharacteristicTable,
                    null,
                    ContentValues().apply {
                        put(AnimalGeneticCharacteristicTable.Columns.ANIMAL_ID, animalId)
                        put(AnimalGeneticCharacteristicTable.Columns.TABLE_ID, GeneticCharacteristic.ID_COAT_COLOR)
                        put(AnimalGeneticCharacteristicTable.Columns.VALUE_ID, valueId)
                        put(AnimalGeneticCharacteristicTable.Columns.CALCULATION_ID, methodId)
                        put(AnimalGeneticCharacteristicTable.Columns.DATE, dateString)
                        put(AnimalGeneticCharacteristicTable.Columns.TIME, timeString)
                        put(AnimalGeneticCharacteristicTable.Columns.CREATED, timeStampString)
                        put(AnimalGeneticCharacteristicTable.Columns.MODIFIED, timeStampString)
                    }
                )
                setTransactionSuccessful()
            } finally {
                endTransaction()
            }
        }
    }

    override fun queryCodonForAnimal(animalId: EntityId, codon: Codon): CodonCharacteristic? {
        return databaseHandler.readableDatabase.rawQuery(
            SQL_QUERY_CODON_CHARACTERISTIC_FOR_ANIMAL,
            arrayOf(animalId.toString(), codon.id.toString())
        ).use { cursor ->
            val characteristicValueId = cursor.readFirstItem {
                it.getEntityId(AnimalGeneticCharacteristicTable.Columns.VALUE_ID)
            }
            characteristicValueId?.let { queryCodonCharacteristic(codon, it) }
        }
    }

    override fun addCodonCharacteristicForAnimal(
        animalId: EntityId,
        codon: Codon,
        valueId: EntityId,
        methodId: EntityId,
        timeStamp: LocalDateTime
    ) {
        val dateString = Sql.formatDate(timeStamp)
        val timeString = Sql.formatTime(timeStamp)
        val timeStampString = Sql.formatDateTime(timeStamp)
        databaseHandler.writableDatabase.run {
            beginTransaction()
            try {
                insertWithPKOrThrow(
                    AnimalGeneticCharacteristicTable,
                    null,
                    ContentValues().apply {
                        put(AnimalGeneticCharacteristicTable.Columns.ANIMAL_ID, animalId)
                        put(AnimalGeneticCharacteristicTable.Columns.TABLE_ID, codon.id)
                        put(AnimalGeneticCharacteristicTable.Columns.VALUE_ID, valueId)
                        put(AnimalGeneticCharacteristicTable.Columns.CALCULATION_ID, methodId)
                        put(AnimalGeneticCharacteristicTable.Columns.DATE, dateString)
                        put(AnimalGeneticCharacteristicTable.Columns.TIME, timeString)
                        put(AnimalGeneticCharacteristicTable.Columns.CREATED, timeStampString)
                        put(AnimalGeneticCharacteristicTable.Columns.MODIFIED, timeStampString)
                    }
                )
                setTransactionSuccessful()
            } finally {
                endTransaction()
            }
        }
    }

    override suspend fun queryBreedingSummaryForAnimal(animalId: EntityId): BreedingSummary? {
        return databaseHandler.readableDatabase.run {
            querySexIdForAnimal(animalId)?.let {
                when {
                    Sex.isOrWasMale(it) -> {
                        queryBreedingSummaryForSire(animalId)
                    }
                    Sex.isOrWasFemale(it) -> {
                        queryBreedingSummaryForDam(animalId)
                    }
                    else -> null
                }
            }
        }
    }

    override fun breedingSummaryForAnimal(animalId: EntityId): Flow<BreedingSummary?> {
        return databaseHandler.readableDatabase.run {
            querySexIdForAnimal(animalId)?.let {
                when {
                    Sex.isOrWasMale(it) -> {
                        breedingSummaryForSire(animalId)
                    }
                    Sex.isOrWasFemale(it) -> {
                        breedingSummaryForDam(animalId)
                    }
                    else -> flowOf(null)
                }
            } ?: flowOf(null)
        }
    }

    override suspend fun queryBreedingSummaryForSire(sireId: EntityId): BreedingSummary? {
        return queryBreedingSummaryForSire(sireId, defaultSettingsRepository.queryActiveDefaultSettings().registryCompanyId)
    }

    private suspend fun queryBreedingSummaryForSire(sireId: EntityId, registryCompanyId: EntityId?): BreedingSummary? {
        return databaseHandler.readableDatabase.run {
            awaitAll(
                withContext(Dispatchers.IO) { async { queryOffspringCountBySexForSire(sireId) } },
                withContext(Dispatchers.IO) { async { queryOffspringOfSire(sireId, registryCompanyId) } }
            ) { born, offspring -> BreedingSummary(born = born, weaned = emptyList(), offspring = offspring) }
        }
    }

    override fun breedingSummaryForSire(sireId: EntityId): Flow<BreedingSummary?> =
        defaultSettingsRepository.activeDefaultSettings().flatMapLatest { defaultSettings ->
            createQueryFlowFor(
                pertinentTopics = topicChange(sireId) { add(Topic.Animal.OFFSPRING) },
                queryBlock = { queryBreedingSummaryForSire(sireId, defaultSettings.registryCompanyId) }
            )
        }

    override suspend fun queryBreedingSummaryForDam(damId: EntityId): BreedingSummary? {
        return queryBreedingSummaryForDam(damId, defaultSettingsRepository.queryActiveDefaultSettings().registryCompanyId)
    }

    private suspend fun queryBreedingSummaryForDam(damId: EntityId, registryCompanyId: EntityId?): BreedingSummary? {
        return databaseHandler.readableDatabase.run {
            awaitAll(
                withContext(Dispatchers.IO) { async { queryOffspringCountBySexForDam(damId) } },
                withContext(Dispatchers.IO) { async { queryWeanedCountBySexForDam(damId) } },
                withContext(Dispatchers.IO) { async { queryOffspringOfDam(damId, registryCompanyId) } }
            ) { born, weaned, offspring ->
                BreedingSummary(born = born, weaned = weaned, offspring = offspring)
            }
        }
    }

    override fun breedingSummaryForDam(damId: EntityId): Flow<BreedingSummary?> =
        defaultSettingsRepository.activeDefaultSettings().flatMapLatest { defaultSettings ->
            createQueryFlowFor(
                pertinentTopics = topicChange(damId) { add(Topic.Animal.OFFSPRING) },
                queryBlock = { queryBreedingSummaryForDam(damId, defaultSettings.registryCompanyId) }
            )
        }

    override suspend fun queryBreedingDetailsForSire(sireId: EntityId): MaleBreedingDetails? {
        return databaseHandler.readableDatabase.queryMaleBreedingDetails(sireId)
    }

    override fun breedingDetailsForSire(sireId: EntityId): Flow<MaleBreedingDetails?> =
        createQueryFlowFor(
            pertinentTopics = topicChange(sireId) { add(Topic.Animal.BREEDING) },
            queryBlock = { queryBreedingDetailsForSire(sireId) }
        )

    override suspend fun queryBreedingDetailsForDam(damId: EntityId): FemaleBreedingDetails? {
        return databaseHandler.readableDatabase.queryFemaleBreedingDetails(damId)
    }

    override fun breedingDetailsForDam(damId: EntityId): Flow<FemaleBreedingDetails?> =
        createQueryFlowFor(
            pertinentTopics = topicChange(damId) { add(Topic.Animal.BREEDING) },
            queryBlock = { queryBreedingDetailsForDam(damId) }
        )

    override suspend fun queryFemaleBreedingHistory(animalId: EntityId): List<FemaleBreedingHistoryEntry> {
        return withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.rawQuery(
                SQL_QUERY_FEMALE_BREEDING_HISTORY,
                arrayOf(animalId.toString())
            ).use { cursor ->
                cursor.readAllItems(::femaleBreedingFromCursor)
            }
        }
    }

    override fun queryFemaleBreedingRecords(animalId: EntityId): List<FemaleBreeding> {
        return databaseHandler.readableDatabase.rawQuery(
            SQL_QUERY_ANIMAL_FEMALE_BREEDING,
            arrayOf(animalId.toString())
        ).use { cursor ->
            cursor.readAllItems(::animalFemaleBreedingFromCursor)
        }
    }

    override fun queryFemaleBreeding(femaleBreedingRecordId: EntityId): FemaleBreeding? {
        return databaseHandler.readableDatabase.rawQuery(
            SQL_QUERY_ANIMAL_FEMALE_BREEDING_BY_ID,
            arrayOf(femaleBreedingRecordId.toString())
        ).use { cursor ->
            cursor.readFirstItem(::animalFemaleBreedingFromCursor)
        }
    }

    override fun queryFemaleBreedingForBirthdate(animalId: EntityId, birthDate: LocalDate): FemaleBreeding? {
        val femaleBreedingHistory = queryFemaleBreedingRecords(animalId).takeIf { it.isNotEmpty() } ?: return null
        val speciesInfo = requireNotNull(queryAnimalSpecies(animalId))
        val recordWithMatchingBirthDate = femaleBreedingHistory.firstOrNull { it.birthingDate == birthDate }
        if (recordWithMatchingBirthDate != null) {
            return recordWithMatchingBirthDate
        }
        val recordWithinGestationRanges = femaleBreedingHistory.firstOrNull { femaleRecord ->
            femaleRecord.maleBreeding?.let { maleRecord ->
                val earliestGestation = maleRecord.dateIn.plusDays(speciesInfo.earlyGestationLengthDays.toLong())
                val latestGestation = maleRecord.dateOut?.plusDays(speciesInfo.lateGestationLengthDays.toLong())
                birthDate.isAfter(earliestGestation) && latestGestation?.let { birthDate.isBefore(it) } == true
            } == true
        }
        return recordWithinGestationRanges
    }

    override fun addFemaleBreedingRecordForBirthdate(
        animalId: EntityId,
        birthDate: LocalDate,
        birthTime: LocalTime?
    ): EntityId {
        val timeStampString = Sql.formatDateTime(LocalDateTime.now())
        val speciesInfo = requireNotNull(queryAnimalSpecies(animalId))
        return databaseHandler.writableDatabase.run {
            beginTransaction()
            try {
                val animalFemaleBreedingId = insertWithPKOrThrow(
                    AnimalFemaleBreedingTable,
                    null,
                    ContentValues().apply {
                        put(AnimalFemaleBreedingTable.Columns.ANIMAL_ID, animalId)
                        putNull(AnimalFemaleBreedingTable.Columns.MALE_BREEDING_ID)
                        putNull(AnimalFemaleBreedingTable.Columns.BIRTHING_NOTES)
                        put(AnimalFemaleBreedingTable.Columns.BIRTHING_DATE, Sql.formatDate(birthDate))
                        put(AnimalFemaleBreedingTable.Columns.BIRTHING_TIME, Sql.formatTime(birthTime ?: Sql.DEFAULT_TIME))
                        put(AnimalFemaleBreedingTable.Columns.GESTATION_LENGTH, speciesInfo.typicalGestationLengthDays)
                        put(AnimalFemaleBreedingTable.Columns.NUMBER_ANIMALS_BORN, 1)
                        put(AnimalFemaleBreedingTable.Columns.NUMBER_ANIMALS_WEANED, 0)
                        put(AnimalFemaleBreedingTable.Columns.CREATED, timeStampString)
                        put(AnimalFemaleBreedingTable.Columns.MODIFIED, timeStampString)
                    }
                )
                setTransactionSuccessful()
                animalFemaleBreedingId
            } finally {
                endTransaction()
            }
        }
    }

    override fun updateFemaleBreedingRecordBirthingDate(
        femaleBreedingRecordId: EntityId,
        birthingDate: LocalDate,
        birthingTime: LocalTime,
        timeStamp: LocalDateTime
    ) {
        databaseHandler.writableDatabase.run {
            beginTransaction()
            try {
                update(
                    AnimalFemaleBreedingTable.NAME,
                    ContentValues().apply {
                        put(AnimalFemaleBreedingTable.Columns.BIRTHING_DATE, Sql.formatDate(birthingDate))
                        put(AnimalFemaleBreedingTable.Columns.BIRTHING_TIME, Sql.formatTime(birthingTime))
                        put(AnimalFemaleBreedingTable.Columns.MODIFIED, Sql.formatDateTime(timeStamp))
                    },
                    "${AnimalFemaleBreedingTable.Columns.ID} = ?",
                    arrayOf(femaleBreedingRecordId.toString())
                )
                setTransactionSuccessful()
            } finally {
                endTransaction()
            }
        }
    }

    override fun updateFemaleBreedingRecordGestationLength(
        femaleBreedingRecordId: EntityId,
        gestationLength: Int,
        timeStamp: LocalDateTime
    ) {
        databaseHandler.writableDatabase.run {
            beginTransaction()
            try {
                update(
                    AnimalFemaleBreedingTable.NAME,
                    ContentValues().apply {
                        put(AnimalFemaleBreedingTable.Columns.GESTATION_LENGTH, gestationLength)
                        put(AnimalFemaleBreedingTable.Columns.MODIFIED, Sql.formatDateTime(timeStamp))
                    },
                    "${AnimalFemaleBreedingTable.Columns.ID} = ?",
                    arrayOf(femaleBreedingRecordId.toString())
                )
                setTransactionSuccessful()
            } finally {
                endTransaction()
            }
        }
    }

    override fun updateBirthingNotesForBirth(
        femaleBreedingRecordId: EntityId, sexAbbreviation: String, timeStamp: LocalDateTime
    ) {
        databaseHandler.writableDatabase.run {
            beginTransaction()
            try {
                execSQL(
                    SQL_PREPEND_BIRTHED_ANIMAL_SEX_ABBR_TO_BIRTHING_NOTES,
                    arrayOf(
                        femaleBreedingRecordId.toString(),
                        sexAbbreviation,
                        Sql.formatDateTime(timeStamp)
                    )
                )
                setTransactionSuccessful()
            } finally {
                endTransaction()
            }
        }
    }

    override fun updateBirthingNotesForFemaleBreedingRecord(
        femaleBreedingRecordId: EntityId,
        birthingNotes: String,
        timeStamp: LocalDateTime
    ) {
        databaseHandler.writableDatabase.run {
            beginTransaction()
            try {
                update(
                    AnimalFemaleBreedingTable.NAME,
                    ContentValues().apply {
                        put(AnimalFemaleBreedingTable.Columns.BIRTHING_NOTES, birthingNotes)
                        put(AnimalFemaleBreedingTable.Columns.MODIFIED, Sql.formatDateTime(timeStamp))
                    },
                    "${AnimalFemaleBreedingTable.Columns.ID} = ?",
                    arrayOf(femaleBreedingRecordId.toString())
                )
                setTransactionSuccessful()
            } finally {
                endTransaction()
            }
        }
    }

    override fun incrementNumberOfAnimalsBorn(femaleBreedingRecordId: EntityId, timeStamp: LocalDateTime) {
        databaseHandler.writableDatabase.run {
            beginTransaction()
            try {
                execSQL(
                    SQL_INCREMENT_FEMALE_BREEDING_NUMBER_BORN,
                    arrayOf(
                        femaleBreedingRecordId.toString(),
                        Sql.formatDateTime(timeStamp)
                    )
                )
                setTransactionSuccessful()
            } finally {
                endTransaction()
            }
        }
    }

    private data class MaleServiceRecord(
        val sireId: EntityId,
        val serviceTypeId: EntityId,
        val dateIn: LocalDate,
        val dateOut: LocalDate?
    ) {
        companion object {
            fun fromCursor(cursor: Cursor): MaleServiceRecord {
                return MaleServiceRecord(
                    sireId = cursor.getEntityId(AnimalMaleBreedingTable.Columns.ANIMAL_ID),
                    serviceTypeId = cursor.getEntityId(AnimalMaleBreedingTable.Columns.SERVICE_TYPE_ID),
                    dateIn = cursor.getLocalDate(AnimalMaleBreedingTable.Columns.DATE_IN),
                    dateOut = cursor.getOptLocalDate(AnimalMaleBreedingTable.Columns.DATE_OUT)
                )
            }
        }
    }

    override suspend fun querySireForOffspringFrom(animalId: EntityId): SireInfo {
        val animalSpecies = requireNotNull(queryAnimalSpecies(animalId))
        val maleServiceRecords = withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.rawQuery(
                SQL_QUERY_POTENTIAL_SIRES_BY_DAM_ID,
                arrayOf(animalId.toString())
            ).use { cursor ->
                cursor.readAllItems(MaleServiceRecord::fromCursor)
            }
        }
        val today = LocalDate.now()
        val likelySireRecord = maleServiceRecords.firstOrNull { record ->
            if (record.dateOut == null) {
                false
            } else {
                val earliestGestation =
                    record.dateIn.plusDays(animalSpecies.earlyGestationLengthDays.toLong())
                val latestGestation =
                    record.dateOut.plusDays(animalSpecies.lateGestationLengthDays.toLong())
                today.isAfter(earliestGestation) && today.isBefore(latestGestation)
            }
        }
        if (likelySireRecord != null) {
            return sireInfoFor(likelySireRecord.sireId, likelySireRecord.serviceTypeId)
        }
        return sireInfoForUnknownSire(animalSpecies)
    }

    private suspend fun sireInfoFor(sireId: EntityId, serviceTypeId: EntityId): SireInfo {
        val sireName = requireNotNull(queryAnimalName(sireId))
        val serviceType = requireNotNull(queryServiceType(serviceTypeId))
        return SireInfo(sireName, serviceType)
    }

    private suspend fun sireInfoForUnknownSire(species: Species): SireInfo {
        val sireName = requireNotNull(queryAnimalName(Animal.unknownSireIdForSpecies(species.id)))
        val serviceType = requireNotNull(queryServiceType(ServiceType.ID_NATURAL_COVER))
        return SireInfo(sireName, serviceType)
    }

    private suspend fun queryServiceType(serviceTypeId: EntityId): ServiceType? {
        return withContext(Dispatchers.IO) {
            databaseHandler.readableDatabase.rawQuery(
                ServiceTypeTable.Sql.QUERY_SERVICE_TYPE_BY_ID,
                arrayOf(serviceTypeId.toString())
            ).use { cursor ->
                cursor.readFirstItem(Cursor::readServiceType)
            }
        }
    }

    override fun addAnimal(
        animalName: String,
        breeding: List<BreedPart>,
        sexId: EntityId,
        damId: EntityId?,
        sireId: EntityId?,
        birthDate: LocalDate,
        birthTime: LocalTime?,
        birthOrder: Int?,
        birthTypeId: EntityId,
        rearTypeId: EntityId?,
        ownerId: EntityId,
        ownerType: Owner.Type,
        ownerPremiseId: EntityId,
        breederId: EntityId,
        breederTypeCode: Int,
        weight: Float?,
        weightUnitsId: EntityId?,
        timeStampOn: LocalDateTime
    ): EntityId {
        return databaseHandler.writableDatabase.run {
            val createdAtDateTimeString = Sql.formatDateTime(timeStampOn)
            val birthDateString = Sql.formatDate(birthDate)
            val birthTimeString = birthTime?.let { Sql.formatTime(birthTime) }
            beginTransaction()
            try {
                val animalId = insertWithPKOrThrow(
                    AnimalTable,
                    null,
                    ContentValues().apply {
                        put(AnimalTable.Columns.NAME, animalName)
                        put(AnimalTable.Columns.SEX_ID, sexId)
                        put(AnimalTable.Columns.BIRTH_DATE, birthDateString)
                        put(AnimalTable.Columns.BIRTH_TIME, birthTimeString)
                        put(AnimalTable.Columns.BIRTH_TYPE_ID, birthTypeId)
                        if (weight != null && weightUnitsId != null) {
                            put(AnimalTable.Columns.BIRTH_WEIGHT, Sql.floatWithPrecision(weight))
                            put(AnimalTable.Columns.BIRTH_WEIGH_UNITS_ID, weightUnitsId)
                        } else {
                            putNull(AnimalTable.Columns.BIRTH_WEIGHT)
                            putNull(AnimalTable.Columns.BIRTH_WEIGH_UNITS_ID)
                        }
                        put(AnimalTable.Columns.BIRTH_ORDER, birthOrder)
                        put(AnimalTable.Columns.REAR_TYPE_ID, rearTypeId)
                        putNull(AnimalTable.Columns.WEANED_DATE)
                        putNull(AnimalTable.Columns.DEATH_DATE)
                        putNull(AnimalTable.Columns.DEATH_REASON_ID)
                        put(
                            AnimalTable.Columns.SIRE_ID,
                            sireId ?: Animal.unknownSireIdForSpecies(
                                Sex.speciesIdFromSexId(sexId)
                            )
                        )
                        put(
                            AnimalTable.Columns.DAM_ID,
                            damId ?: Animal.unknownDamIdForSpecies(
                                Sex.speciesIdFromSexId(sexId)
                            )
                        )
                        putNull(AnimalTable.Columns.FOSTER_DAM_ID)
                        putNull(AnimalTable.Columns.SURROGATE_DAM_ID)
                        put(AnimalTable.Columns.IS_HAND_REARED, Sql.booleanValue(false))
                        putNull(AnimalTable.Columns.MANAGEMENT_GROUP_ID)
                        put(AnimalTable.Columns.CREATED, createdAtDateTimeString)
                        put(AnimalTable.Columns.MODIFIED, createdAtDateTimeString)
                    }
                )
                insertWithPKOrThrow(
                    AnimalOwnershipHistoryTable,
                    null,
                    ContentValues().apply {
                        put(AnimalOwnershipHistoryTable.Columns.ANIMAL_ID, animalId)
                        put(AnimalOwnershipHistoryTable.Columns.TRANSFER_DATE, birthDateString)
                        putNull(AnimalOwnershipHistoryTable.Columns.FROM_CONTACT_ID)
                        putNull(AnimalOwnershipHistoryTable.Columns.FROM_COMPANY_ID)
                        if (ownerType.isContact) {
                            put(AnimalOwnershipHistoryTable.Columns.TO_CONTACT_ID, ownerId)
                        } else {
                            putNull(AnimalOwnershipHistoryTable.Columns.TO_CONTACT_ID)
                        }
                        if (ownerType.isCompany) {
                            put(AnimalOwnershipHistoryTable.Columns.TO_COMPANY_ID, ownerId)
                        } else {
                            putNull(AnimalOwnershipHistoryTable.Columns.TO_COMPANY_ID)
                        }
                        put(
                            AnimalOwnershipHistoryTable.Columns.TRANSFER_REASON_ID,
                            TransferReason.ID_NATURAL_ADDITION
                        )
                        putNull(AnimalOwnershipHistoryTable.Columns.SALE_PRICE)
                        putNull(AnimalOwnershipHistoryTable.Columns.SALE_PRICE_UNITS_ID)
                        put(
                            AnimalOwnershipHistoryTable.Columns.CREATED,
                            createdAtDateTimeString
                        )
                        put(
                            AnimalOwnershipHistoryTable.Columns.MODIFIED,
                            createdAtDateTimeString
                        )
                    }
                )
                insertWithPKOrThrow(
                    AnimalLocationHistoryTable,
                    null,
                    ContentValues().apply {
                        put(AnimalLocationHistoryTable.Columns.ANIMAL_ID, animalId)
                        putNull(AnimalLocationHistoryTable.Columns.FROM_PREMISE_ID)
                        put(AnimalLocationHistoryTable.Columns.TO_PREMISE_ID, ownerPremiseId)
                        put(AnimalLocationHistoryTable.Columns.MOVEMENT_DATE, birthDateString)
                        put(AnimalLocationHistoryTable.Columns.CREATED, createdAtDateTimeString)
                        put(AnimalLocationHistoryTable.Columns.MODIFIED, createdAtDateTimeString)
                    }
                )
                breeding.forEach { breedPart ->
                    insertWithPKOrThrow(
                        AnimalBreedTable,
                        null,
                        ContentValues().apply {
                            put(AnimalBreedTable.Columns.ANIMAL_ID, animalId)
                            put(AnimalBreedTable.Columns.BREED_ID, breedPart.breedId)
                            put(AnimalBreedTable.Columns.BREED_PERCENTAGE, breedPart.percentage)
                            put(AnimalBreedTable.Columns.CREATED, createdAtDateTimeString)
                            put(AnimalBreedTable.Columns.MODIFIED, createdAtDateTimeString)
                        }
                    )
                }
                insertWithPKOrThrow(
                    AnimalRegistrationTable,
                    null,
                    ContentValues().apply {
                        put(AnimalRegistrationTable.Columns.ANIMAL_ID, animalId)
                        put(AnimalRegistrationTable.Columns.ANIMAL_NAME, animalName)
                        putNull(AnimalRegistrationTable.Columns.REGISTRATION_NUMBER)
                        putNull(AnimalRegistrationTable.Columns.ID_REGISTRY_COMPANY_ID)
                        putNull(AnimalRegistrationTable.Columns.ANIMAL_REGISTRATION_TYPE_ID)
                        putNull(AnimalRegistrationTable.Columns.FLOCK_BOOK_ID)
                        put(AnimalRegistrationTable.Columns.REGISTRATION_DATE, birthDateString)
                        putNull(AnimalRegistrationTable.Columns.REGISTRATION_DESCRIPTION)
                        if (breederTypeCode == Breeder.Type.CONTACT.code) {
                            put(AnimalRegistrationTable.Columns.BREEDER_CONTACT_ID, breederId)
                        } else {
                            putNull(AnimalRegistrationTable.Columns.BREEDER_CONTACT_ID)
                        }
                        if (breederTypeCode == Breeder.Type.COMPANY.code) {
                            put(AnimalRegistrationTable.Columns.BREEDER_COMPANY_ID, breederId)
                        } else {
                            putNull(AnimalRegistrationTable.Columns.BREEDER_COMPANY_ID)
                        }
                        put(AnimalRegistrationTable.Columns.CREATED, createdAtDateTimeString)
                        put(AnimalRegistrationTable.Columns.MODIFIED, createdAtDateTimeString)
                    }
                )
                setTransactionSuccessful()
                animalId
            } finally {
                endTransaction()
            }
        }
    }

    override fun assignAnimalToFlock(
        animalId: EntityId,
        flockPrefixId: EntityId,
        registryCompanyId: EntityId,
        timeStamp: LocalDateTime
    ) {
        databaseHandler.writableDatabase.run {
            beginTransaction()
            try {
                val timeStampString = Sql.formatDateTime(timeStamp)
                insertWithPKOrThrow(
                    AnimalFlockPrefixTable,
                    null,
                    ContentValues().apply {
                        put(AnimalFlockPrefixTable.Columns.ANIMAL_ID, animalId)
                        put(AnimalFlockPrefixTable.Columns.FLOCK_PREFIX_ID, flockPrefixId)
                        put(AnimalFlockPrefixTable.Columns.CREATED, timeStampString)
                        put(AnimalFlockPrefixTable.Columns.MODIFIED, timeStampString)
                    }
                )
                setTransactionSuccessful()
            } finally {
                endTransaction()
            }
        }
    }

    override fun updateBirthType(animalIds: List<EntityId>, birthTypeId: EntityId) {
        return databaseHandler.writableDatabase.run {
            beginTransaction()
            try {
                animalIds.forEach { animalId ->
                    update(
                        AnimalTable.NAME,
                        ContentValues().apply {
                            put(AnimalTable.Columns.BIRTH_TYPE_ID, birthTypeId)
                        },
                        "${AnimalTable.Columns.ID} = ?",
                        arrayOf(animalId.toString())
                    )
                }
                setTransactionSuccessful()
            } finally {
                endTransaction()
            }
        }
    }

    override suspend fun addNotesToAnimal(
        animalId: EntityId,
        customNote: String?,
        predefinedNoteIds: List<EntityId>,
        timeStamp: LocalDateTime
    ) {
        return databaseHandler.writableDatabase.run {
            val createdAtDateTimeString = Sql.formatDateTime(timeStamp)
            beginTransaction()
            try {
                predefinedNoteIds.forEach { predefinedNoteId ->
                    insertWithPKOrThrow(
                        AnimalNoteTable,
                        null,
                        ContentValues().apply {
                            put(AnimalNoteTable.Columns.ANIMAL_ID, animalId)
                            putNull(AnimalNoteTable.Columns.NOTE_TEXT)
                            put(AnimalNoteTable.Columns.PREDEFINED_NOTE_ID, predefinedNoteId)
                            put(AnimalNoteTable.Columns.NOTE_DATE, Sql.formatDate(timeStamp))
                            put(AnimalNoteTable.Columns.NOTE_TIME, Sql.formatTime(timeStamp))
                            put(AnimalNoteTable.Columns.CREATED, createdAtDateTimeString)
                            put(AnimalNoteTable.Columns.MODIFIED, createdAtDateTimeString)
                        }
                    )
                }
                if (!customNote.isNullOrBlank()) {
                    insertWithPKOrThrow(
                        AnimalNoteTable,
                        null,
                        ContentValues().apply {
                            put(AnimalNoteTable.Columns.ANIMAL_ID, animalId)
                            put(AnimalNoteTable.Columns.NOTE_TEXT, customNote)
                            putNull(AnimalNoteTable.Columns.PREDEFINED_NOTE_ID)
                            put(AnimalNoteTable.Columns.NOTE_DATE, Sql.formatDate(timeStamp))
                            put(AnimalNoteTable.Columns.NOTE_TIME, Sql.formatTime(timeStamp))
                            put(AnimalNoteTable.Columns.CREATED, createdAtDateTimeString)
                            put(AnimalNoteTable.Columns.MODIFIED, createdAtDateTimeString)
                        }
                    )
                }
                setTransactionSuccessful()
            } finally {
                endTransaction()
            }
        }
    }

    override suspend fun addAlertForAnimal(
        animalId: EntityId,
        alert: String,
        timeStamp: LocalDateTime
    ) {
        return databaseHandler.writableDatabase.run {
            insertWithPK(
                AnimalAlertTable,
                null,
                ContentValues().apply {
                    put(AnimalAlertTable.Columns.ANIMAL_ID, animalId)
                    put(AnimalAlertTable.Columns.ALERT_TYPE_ID, AnimalAlert.Type.USER_DEFINED.typeId)
                    put(AnimalAlertTable.Columns.ALERT_CONTENT, alert)
                    put(AnimalAlertTable.Columns.ALERT_DATE, Sql.formatDate(timeStamp))
                    put(AnimalAlertTable.Columns.ALERT_TIME, Sql.formatTime(timeStamp))
                    put(AnimalAlertTable.Columns.CREATED, Sql.formatDateTime(timeStamp))
                    put(AnimalAlertTable.Columns.MODIFIED, Sql.formatDateTime(timeStamp))
                }
            )
        }
    }

    override suspend fun addDrugWithdrawalAlertForAnimal(
        animalId: EntityId,
        drugWithdrawal: DrugWithdrawal,
        timeStamp: LocalDateTime
    ) {
        return databaseHandler.writableDatabase.run {
            insertWithPK(
                AnimalAlertTable,
                null,
                ContentValues().apply {
                    put(AnimalAlertTable.Columns.ANIMAL_ID, animalId)
                    put(AnimalAlertTable.Columns.ALERT_TYPE_ID, AnimalAlert.Type.DRUG_WITHDRAWAL.typeId)
                    put(AnimalAlertTable.Columns.ALERT_CONTENT, Json.encodeToString(drugWithdrawal))
                    put(AnimalAlertTable.Columns.ALERT_DATE, Sql.formatDate(timeStamp))
                    put(AnimalAlertTable.Columns.ALERT_TIME, Sql.formatTime(timeStamp))
                    put(AnimalAlertTable.Columns.CREATED, Sql.formatDateTime(timeStamp))
                    put(AnimalAlertTable.Columns.MODIFIED, Sql.formatDateTime(timeStamp))
                }
            )
        }
    }

    override suspend fun addEvaluationSummaryAlertForAnimal(
        animalId: EntityId,
        evaluationSummary: EvaluationSummary,
        timeStamp: LocalDateTime
    ) {
        return databaseHandler.writableDatabase.run {
            insertWithPK(
                AnimalAlertTable,
                null,
                ContentValues().apply {
                    put(AnimalAlertTable.Columns.ANIMAL_ID, animalId)
                    put(AnimalAlertTable.Columns.ALERT_TYPE_ID, AnimalAlert.Type.EVALUATION_SUMMARY.typeId)
                    put(AnimalAlertTable.Columns.ALERT_CONTENT, Json.encodeToString(evaluationSummary))
                    put(AnimalAlertTable.Columns.ALERT_DATE, Sql.formatDate(timeStamp))
                    put(AnimalAlertTable.Columns.ALERT_TIME, Sql.formatTime(timeStamp))
                    put(AnimalAlertTable.Columns.CREATED, Sql.formatDateTime(timeStamp))
                    put(AnimalAlertTable.Columns.MODIFIED, Sql.formatDateTime(timeStamp))
                }
            )
        }
    }

    override fun addIdToAnimal(
        animalId: EntityId,
        idTypeId: EntityId,
        idColorId: EntityId,
        idLocationId: EntityId,
        idNumber: String,
        isOfficial: Boolean,
        timeStampOn: LocalDateTime
    ): EntityId {
        return databaseHandler.writableDatabase.run {
            val createdAtDateTimeString = Sql.formatDateTime(timeStampOn)
            val scrapieFlockId: EntityId? = lookupScrapieFlockNumberId(idTypeId, idNumber)
            val animalIdInfoId = insertWithPKOrThrow(
                AnimalIdInfoTable,
                null,
                ContentValues().apply {
                    put(AnimalIdInfoTable.Columns.ANIMAL_ID, animalId)
                    put(AnimalIdInfoTable.Columns.ID_TYPE_ID, idTypeId)
                    put(AnimalIdInfoTable.Columns.MALE_ID_COLOR_ID, idColorId)
                    put(AnimalIdInfoTable.Columns.FEMALE_ID_COLOR_ID, idColorId)
                    put(AnimalIdInfoTable.Columns.ID_LOCATION_ID, idLocationId)
                    put(AnimalIdInfoTable.Columns.NUMBER, idNumber)
                    put(AnimalIdInfoTable.Columns.IS_OFFICIAL_ID, Sql.booleanValue(isOfficial))
                    put(AnimalIdInfoTable.Columns.DATE_ON, Sql.formatDate(timeStampOn))
                    put(AnimalIdInfoTable.Columns.TIME_ON, Sql.formatTime(timeStampOn))
                    putNull(AnimalIdInfoTable.Columns.DATE_OFF)
                    putNull(AnimalIdInfoTable.Columns.TIME_OFF)
                    putNull(AnimalIdInfoTable.Columns.REMOVE_REASON_ID)
                    put(AnimalIdInfoTable.Columns.SCRAPIE_FLOCK_ID, scrapieFlockId)
                    put(AnimalIdInfoTable.Columns.CREATED, createdAtDateTimeString)
                    put(AnimalIdInfoTable.Columns.MODIFIED, createdAtDateTimeString)
                }
            )
            animalIdInfoId
        }
    }

    override suspend fun updateIdOnAnimal(
        id: EntityId,
        typeId: EntityId,
        colorId: EntityId,
        locationId: EntityId,
        number: String,
        timeStamp: LocalDateTime
    ): Boolean {
        val scrapieFlockId: EntityId? = lookupScrapieFlockNumberId(typeId, number)
        return 0 < databaseHandler.writableDatabase.update(
            AnimalIdInfoTable.NAME,
            ContentValues().apply {
                put(AnimalIdInfoTable.Columns.ID_TYPE_ID, typeId)
                put(AnimalIdInfoTable.Columns.MALE_ID_COLOR_ID, colorId)
                put(AnimalIdInfoTable.Columns.FEMALE_ID_COLOR_ID, colorId)
                put(AnimalIdInfoTable.Columns.ID_LOCATION_ID, locationId)
                put(AnimalIdInfoTable.Columns.NUMBER, number)
                put(AnimalIdInfoTable.Columns.SCRAPIE_FLOCK_ID, scrapieFlockId)
                put(AnimalIdInfoTable.Columns.REMOVE_REASON_ID, IdRemoveReason.ID_CORRECT_TAG_DATA)
                put(AnimalIdInfoTable.Columns.MODIFIED, Sql.formatDateTime(timeStamp))
            },
            "${AnimalIdInfoTable.Columns.ID} = ?",
            arrayOf(id.toString())
        )
    }

    override suspend fun removeIdFromAnimal(
        id: EntityId,
        removeReasonId: EntityId,
        timeStamp: LocalDateTime
    ): Boolean {
        return 0 < databaseHandler.writableDatabase.update(
            AnimalIdInfoTable.NAME,
            ContentValues().apply {
                put(AnimalIdInfoTable.Columns.REMOVE_REASON_ID, removeReasonId)
                put(AnimalIdInfoTable.Columns.DATE_OFF, Sql.formatDate(timeStamp))
                put(AnimalIdInfoTable.Columns.TIME_OFF, Sql.formatTime(timeStamp))
                put(AnimalIdInfoTable.Columns.MODIFIED, Sql.formatDateTime(timeStamp))
            },
            "${AnimalIdInfoTable.Columns.ID} = ?",
            arrayOf(id.toString())
        )
    }

    private fun lookupScrapieFlockNumberId(idTypeId: EntityId, idNumber: String): EntityId? {
         return if (idTypeId == IdType.ID_TYPE_ID_FED) {
            val idSplits = idNumber.split(IdFormat.FEDERAL_SCRAPIE_SEPARATOR)
            val scrapieFlockIdPart = idSplits.takeIf { 1 < it.size }?.let { it[0] }
            scrapieFlockIdPart?.let {
                databaseHandler.readableDatabase.rawQuery(
                    ScrapieFlockNumberTable.Sql.QUERY_SCRAPIE_FLOCK_NUMBER_FROM_NUMBER,
                    arrayOf(it)
                ).use { cursor ->
                    cursor.readFirstItem(ScrapieFlockNumberTable::scrapieFlockNumberFromCursor)
                }?.id
            }
        } else null
    }

    override suspend fun addEvaluationForAnimal(
        animalId: EntityId,
        ageInDays: Long,
        timeStamp: LocalDateTime,
        trait01Id: EntityId?,
        trait01Score: Int?,
        trait02Id: EntityId?,
        trait02Score: Int?,
        trait03Id: EntityId?,
        trait03Score: Int?,
        trait04Id: EntityId?,
        trait04Score: Int?,
        trait05Id: EntityId?,
        trait05Score: Int?,
        trait06Id: EntityId?,
        trait06Score: Int?,
        trait07Id: EntityId?,
        trait07Score: Int?,
        trait08Id: EntityId?,
        trait08Score: Int?,
        trait09Id: EntityId?,
        trait09Score: Int?,
        trait10Id: EntityId?,
        trait10Score: Int?,
        trait11Id: EntityId?,
        trait11Score: Float?,
        trait11UnitsId: EntityId?,
        trait12Id: EntityId?,
        trait12Score: Float?,
        trait12UnitsId: EntityId?,
        trait13Id: EntityId?,
        trait13Score: Float?,
        trait13UnitsId: EntityId?,
        trait14Id: EntityId?,
        trait14Score: Float?,
        trait14UnitsId: EntityId?,
        trait15Id: EntityId?,
        trait15Score: Float?,
        trait15UnitsId: EntityId?,
        trait16Id: EntityId?,
        trait16OptionId: EntityId?,
        trait17Id: EntityId?,
        trait17OptionId: EntityId?,
        trait18Id: EntityId?,
        trait18OptionId: EntityId?,
        trait19Id: EntityId?,
        trait19OptionId: EntityId?,
        trait20Id: EntityId?,
        trait20OptionId: EntityId?
    ): EntityId {
        return databaseHandler.writableDatabase.run {
            val createdAtDateTimeString = Sql.formatDateTime(timeStamp)
            val animalEvaluationId = insertWithPKOrThrow(
                AnimalEvaluationTable,
                null,
                ContentValues().apply {
                    put(AnimalEvaluationTable.Columns.ANIMAL_ID, animalId)
                    put(AnimalEvaluationTable.Columns.AGE_IN_DAYS, ageInDays)
                    putNull(AnimalEvaluationTable.Columns.ANIMAL_RANK)
                    putNull(AnimalEvaluationTable.Columns.NUMBER_RANKED)
                    put(AnimalEvaluationTable.Columns.EVAL_DATE, Sql.formatDate(timeStamp))
                    put(AnimalEvaluationTable.Columns.EVAL_TIME, Sql.formatTime(timeStamp))
                    put(AnimalEvaluationTable.Columns.CREATED, createdAtDateTimeString)
                    put(AnimalEvaluationTable.Columns.MODIFIED, createdAtDateTimeString)
                    put(AnimalEvaluationTable.Columns.TRAIT_ID_01, trait01Id)
                    put(AnimalEvaluationTable.Columns.TRAIT_ID_02, trait02Id)
                    put(AnimalEvaluationTable.Columns.TRAIT_ID_03, trait03Id)
                    put(AnimalEvaluationTable.Columns.TRAIT_ID_04, trait04Id)
                    put(AnimalEvaluationTable.Columns.TRAIT_ID_05, trait05Id)
                    put(AnimalEvaluationTable.Columns.TRAIT_ID_06, trait06Id)
                    put(AnimalEvaluationTable.Columns.TRAIT_ID_07, trait07Id)
                    put(AnimalEvaluationTable.Columns.TRAIT_ID_08, trait08Id)
                    put(AnimalEvaluationTable.Columns.TRAIT_ID_09, trait09Id)
                    put(AnimalEvaluationTable.Columns.TRAIT_ID_10, trait10Id)
                    put(AnimalEvaluationTable.Columns.TRAIT_ID_11, trait11Id)
                    put(AnimalEvaluationTable.Columns.TRAIT_ID_12, trait12Id)
                    put(AnimalEvaluationTable.Columns.TRAIT_ID_13, trait13Id)
                    put(AnimalEvaluationTable.Columns.TRAIT_ID_14, trait14Id)
                    put(AnimalEvaluationTable.Columns.TRAIT_ID_15, trait15Id)
                    put(AnimalEvaluationTable.Columns.TRAIT_ID_16, trait16Id)
                    put(AnimalEvaluationTable.Columns.TRAIT_ID_17, trait17Id)
                    put(AnimalEvaluationTable.Columns.TRAIT_ID_18, trait18Id)
                    put(AnimalEvaluationTable.Columns.TRAIT_ID_19, trait19Id)
                    put(AnimalEvaluationTable.Columns.TRAIT_ID_20, trait20Id)
                    put(AnimalEvaluationTable.Columns.TRAIT_SCORE_01, trait01Score)
                    put(AnimalEvaluationTable.Columns.TRAIT_SCORE_02, trait02Score)
                    put(AnimalEvaluationTable.Columns.TRAIT_SCORE_03, trait03Score)
                    put(AnimalEvaluationTable.Columns.TRAIT_SCORE_04, trait04Score)
                    put(AnimalEvaluationTable.Columns.TRAIT_SCORE_05, trait05Score)
                    put(AnimalEvaluationTable.Columns.TRAIT_SCORE_06, trait06Score)
                    put(AnimalEvaluationTable.Columns.TRAIT_SCORE_07, trait07Score)
                    put(AnimalEvaluationTable.Columns.TRAIT_SCORE_08, trait08Score)
                    put(AnimalEvaluationTable.Columns.TRAIT_SCORE_09, trait09Score)
                    put(AnimalEvaluationTable.Columns.TRAIT_SCORE_10, trait10Score)
                    put(
                        AnimalEvaluationTable.Columns.TRAIT_SCORE_11,
                        Sql.optFloatForUnitEvalTrait(trait11Score)
                    )
                    put(
                        AnimalEvaluationTable.Columns.TRAIT_SCORE_12,
                        Sql.optFloatForUnitEvalTrait(trait12Score)
                    )
                    put(
                        AnimalEvaluationTable.Columns.TRAIT_SCORE_13,
                        Sql.optFloatForUnitEvalTrait(trait13Score)
                    )
                    put(
                        AnimalEvaluationTable.Columns.TRAIT_SCORE_14,
                        Sql.optFloatForUnitEvalTrait(trait14Score)
                    )
                    put(
                        AnimalEvaluationTable.Columns.TRAIT_SCORE_15,
                        Sql.optFloatForUnitEvalTrait(trait15Score)
                    )
                    put(AnimalEvaluationTable.Columns.TRAIT_SCORE_16, trait16OptionId)
                    put(AnimalEvaluationTable.Columns.TRAIT_SCORE_17, trait17OptionId)
                    put(AnimalEvaluationTable.Columns.TRAIT_SCORE_18, trait18OptionId)
                    put(AnimalEvaluationTable.Columns.TRAIT_SCORE_19, trait19OptionId)
                    put(AnimalEvaluationTable.Columns.TRAIT_SCORE_20, trait20OptionId)
                    put(AnimalEvaluationTable.Columns.TRAIT_UNITS_ID_11, trait11UnitsId)
                    put(AnimalEvaluationTable.Columns.TRAIT_UNITS_ID_12, trait12UnitsId)
                    put(AnimalEvaluationTable.Columns.TRAIT_UNITS_ID_13, trait13UnitsId)
                    put(AnimalEvaluationTable.Columns.TRAIT_UNITS_ID_14, trait14UnitsId)
                    put(AnimalEvaluationTable.Columns.TRAIT_UNITS_ID_15, trait15UnitsId)
                }
            )
            animalEvaluationId
        }
    }

    override suspend fun addTissueTestForAnimal(
        animalId: EntityId,
        tissueSampleTypeId: EntityId,
        tissueSampleContainerTypeId: EntityId,
        tissueSampleContainerId: String,
        tissueSampleContainerExpDate: LocalDate?,
        tissueTestId: EntityId,
        laboratoryId: EntityId,
        timeStampOn: LocalDateTime
    ): EntityId {
        return databaseHandler.writableDatabase.run {
            val createdAtDateTimeString = Sql.formatDateTime(timeStampOn)
            beginTransaction()
            try {
                val tissueSampleTakenId = insertWithPKOrThrow(
                    AnimalTissueSampleTakenTable,
                    null,
                    ContentValues().apply {
                        put(AnimalTissueSampleTakenTable.Columns.ANIMAL_ID, animalId)
                        put(AnimalTissueSampleTakenTable.Columns.SAMPLE_TYPE_ID, tissueSampleTypeId)
                        put(AnimalTissueSampleTakenTable.Columns.SAMPLE_DATE, Sql.formatDate(timeStampOn))
                        put(AnimalTissueSampleTakenTable.Columns.SAMPLE_TIME, Sql.formatTime(timeStampOn))
                        put(AnimalTissueSampleTakenTable.Columns.CONTAINER_TYPE_ID, tissueSampleContainerTypeId)
                        put(AnimalTissueSampleTakenTable.Columns.CONTAINER_ID, tissueSampleContainerId)
                        if (tissueSampleContainerExpDate != null) {
                            put(
                                AnimalTissueSampleTakenTable.Columns.CONTAINER_EXP_DATE,
                                Sql.formatDate(tissueSampleContainerExpDate)
                            )
                        } else {
                            putNull(AnimalTissueSampleTakenTable.Columns.CONTAINER_EXP_DATE)
                        }
                        put(AnimalTissueSampleTakenTable.Columns.CREATED, createdAtDateTimeString)
                        put(AnimalTissueSampleTakenTable.Columns.MODIFIED, createdAtDateTimeString)
                    }
                )
                insertWithPKOrThrow(
                    AnimalTissueTestRequestTable,
                    null,
                    ContentValues().apply {
                        put(AnimalTissueTestRequestTable.Columns.SAMPLE_TAKEN_ID, tissueSampleTakenId)
                        put(AnimalTissueTestRequestTable.Columns.TEST_ID, tissueTestId)
                        put(AnimalTissueTestRequestTable.Columns.LABORATORY_ID, laboratoryId)
                        putNull(AnimalTissueTestRequestTable.Columns.LABORATORY_ACCESSION_ID)
                        putNull(AnimalTissueTestRequestTable.Columns.TEST_RESULTS)
                        putNull(AnimalTissueTestRequestTable.Columns.TEST_RESULTS_DATE)
                        putNull(AnimalTissueTestRequestTable.Columns.TEST_RESULTS_TIME)
                        putNull(AnimalTissueTestRequestTable.Columns.ANIMAL_EXTERNAL_FILE_ID)
                        put(AnimalTissueTestRequestTable.Columns.CREATED, createdAtDateTimeString)
                        put(AnimalTissueTestRequestTable.Columns.MODIFIED, createdAtDateTimeString)
                    }
                )
                setTransactionSuccessful()
                tissueSampleTakenId
            } finally {
                endTransaction()
            }
        }
    }

    override fun markAnimalDeceased(
        animalId: EntityId,
        deathReasonId: EntityId,
        deathDate: LocalDate,
        timeStamp: LocalDateTime
    ) {
        val animalLastPremiseId = queryAnimalCurrentPremise(animalId)
        val deathDateString = Sql.formatDate(deathDate)
        val timeStampString = Sql.formatDateTime(timeStamp)
        with(databaseHandler.writableDatabase) {
            beginTransaction()
            try {
                update(
                    AnimalTable.NAME,
                    ContentValues().apply {
                        put(AnimalTable.Columns.DEATH_REASON_ID, deathReasonId)
                        put(AnimalTable.Columns.DEATH_DATE, deathDateString)
                        put(AnimalTable.Columns.MODIFIED, timeStampString)
                    },
                    "${AnimalTable.Columns.ID} = ?",
                    arrayOf(animalId.toString())
                )
                insertWithPK(
                    AnimalLocationHistoryTable,
                    null,
                    ContentValues().apply {
                        put(AnimalLocationHistoryTable.Columns.ANIMAL_ID, animalId)
                        put(AnimalLocationHistoryTable.Columns.MOVEMENT_DATE, deathDateString)
                        put(AnimalLocationHistoryTable.Columns.FROM_PREMISE_ID, animalLastPremiseId)
                        putNull(AnimalLocationHistoryTable.Columns.TO_PREMISE_ID)
                        put(AnimalLocationHistoryTable.Columns.CREATED, timeStampString)
                        put(AnimalLocationHistoryTable.Columns.MODIFIED, timeStampString)
                    }
                )
                delete(
                    AnimalAlertTable.NAME,
                    "${AnimalAlertTable.Columns.ANIMAL_ID} = ?",
                    arrayOf(animalId.toString())
                )
                setTransactionSuccessful()
                notifyTopicChanges.tryEmit(
                    topicChange(animalId) {
                        add(Topic.Animal.INFO)
                        add(Topic.Animal.LOCATION)
                    }
                )
            } finally {
                endTransaction()
            }
        }
    }

    override suspend fun recordAnimalWeight(
        animalId: EntityId,
        weight: Float,
        weightUnits: UnitOfMeasure,
        ageInDays: Long,
        timeStamp: LocalDateTime
    ) {
        with(databaseHandler.writableDatabase) {
            beginTransaction()
            try {
                val dateString = Sql.formatDate(timeStamp)
                val timeString = Sql.formatTime(timeStamp)
                val timeStampString = Sql.formatDateTime(timeStamp)
                insertWithPKOrThrow(
                    AnimalEvaluationTable,
                    null,
                    ContentValues().apply {
                        put(AnimalEvaluationTable.Columns.ANIMAL_ID, animalId)
                        put(AnimalEvaluationTable.Columns.AGE_IN_DAYS, ageInDays)
                        putNull(AnimalEvaluationTable.Columns.ANIMAL_RANK)
                        putNull(AnimalEvaluationTable.Columns.NUMBER_RANKED)
                        put(AnimalEvaluationTable.Columns.EVAL_DATE, dateString)
                        put(AnimalEvaluationTable.Columns.EVAL_TIME, timeString)
                        put(AnimalEvaluationTable.Columns.CREATED, timeStampString)
                        put(AnimalEvaluationTable.Columns.MODIFIED, timeStampString)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_ID_01)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_ID_02)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_ID_03)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_ID_04)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_ID_05)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_ID_06)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_ID_07)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_ID_08)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_ID_09)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_ID_10)
                        put(
                            AnimalEvaluationTable.Columns.TRAIT_ID_11,
                            EvalTrait.UNIT_TRAIT_ID_WEIGHT
                        )
                        putNull(AnimalEvaluationTable.Columns.TRAIT_ID_12)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_ID_13)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_ID_14)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_ID_15)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_ID_16)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_ID_17)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_ID_18)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_ID_19)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_ID_20)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_SCORE_01)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_SCORE_02)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_SCORE_03)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_SCORE_04)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_SCORE_05)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_SCORE_06)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_SCORE_07)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_SCORE_08)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_SCORE_09)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_SCORE_10)
                        put(
                            AnimalEvaluationTable.Columns.TRAIT_SCORE_11,
                            Sql.floatForUnitEvalTrait(weight)
                        )
                        putNull(AnimalEvaluationTable.Columns.TRAIT_SCORE_12)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_SCORE_13)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_SCORE_14)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_SCORE_15)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_SCORE_16)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_SCORE_17)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_SCORE_18)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_SCORE_19)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_SCORE_20)
                        put(AnimalEvaluationTable.Columns.TRAIT_UNITS_ID_11, weightUnits.id)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_UNITS_ID_12)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_UNITS_ID_13)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_UNITS_ID_14)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_UNITS_ID_15)
                    }
                )
                setTransactionSuccessful()
            } finally {
                endTransaction()
            }
        }
    }

    override fun recordBirthEvaluation(
        animalId: EntityId,
        lambEaseId: EntityId,
        suckReflexId: EntityId,
        birthWeight: Float?,
        birthWeightUnitsId: EntityId?,
        timeStamp: LocalDateTime
    ) {
        with(databaseHandler.writableDatabase) {
            beginTransaction()
            try {
                val dateString = Sql.formatDate(timeStamp)
                val timeString = Sql.formatTime(timeStamp)
                val timeStampString = Sql.formatDateTime(timeStamp)

                insertWithPKOrThrow(
                    AnimalEvaluationTable,
                    null,
                    ContentValues().apply {
                        put(AnimalEvaluationTable.Columns.ANIMAL_ID, animalId)
                        put(AnimalEvaluationTable.Columns.AGE_IN_DAYS, 1)
                        putNull(AnimalEvaluationTable.Columns.ANIMAL_RANK)
                        putNull(AnimalEvaluationTable.Columns.NUMBER_RANKED)
                        put(AnimalEvaluationTable.Columns.EVAL_DATE, dateString)
                        put(AnimalEvaluationTable.Columns.EVAL_TIME, timeString)
                        put(AnimalEvaluationTable.Columns.CREATED, timeStampString)
                        put(AnimalEvaluationTable.Columns.MODIFIED, timeStampString)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_ID_01)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_ID_02)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_ID_03)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_ID_04)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_ID_05)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_ID_06)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_ID_07)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_ID_08)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_ID_09)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_ID_10)
                        if (birthWeight != null && birthWeightUnitsId != null) {
                            put(
                                AnimalEvaluationTable.Columns.TRAIT_ID_11,
                                EvalTrait.UNIT_TRAIT_ID_WEIGHT
                            )
                        } else {
                            putNull(AnimalEvaluationTable.Columns.TRAIT_ID_11)
                        }
                        putNull(AnimalEvaluationTable.Columns.TRAIT_ID_12)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_ID_13)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_ID_14)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_ID_15)
                        put(AnimalEvaluationTable.Columns.TRAIT_ID_16, EvalTrait.TRAIT_ID_LAMB_EASE)
                        put(
                            AnimalEvaluationTable.Columns.TRAIT_ID_17,
                            EvalTrait.TRAIT_ID_SUCK_REFLEX
                        )
                        putNull(AnimalEvaluationTable.Columns.TRAIT_ID_18)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_ID_19)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_ID_20)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_SCORE_01)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_SCORE_02)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_SCORE_03)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_SCORE_04)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_SCORE_05)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_SCORE_06)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_SCORE_07)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_SCORE_08)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_SCORE_09)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_SCORE_10)
                        if (birthWeight != null && birthWeightUnitsId != null) {
                            put(
                                AnimalEvaluationTable.Columns.TRAIT_SCORE_11,
                                Sql.floatForUnitEvalTrait(birthWeight)
                            )
                        } else {
                            putNull(AnimalEvaluationTable.Columns.TRAIT_SCORE_11)
                        }
                        putNull(AnimalEvaluationTable.Columns.TRAIT_SCORE_12)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_SCORE_13)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_SCORE_14)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_SCORE_15)
                        put(AnimalEvaluationTable.Columns.TRAIT_SCORE_16, lambEaseId)
                        put(AnimalEvaluationTable.Columns.TRAIT_SCORE_17, suckReflexId)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_SCORE_18)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_SCORE_19)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_SCORE_20)
                        if (birthWeight != null && birthWeightUnitsId != null) {
                            put(AnimalEvaluationTable.Columns.TRAIT_UNITS_ID_11, birthWeightUnitsId)
                        } else {
                            putNull(AnimalEvaluationTable.Columns.TRAIT_UNITS_ID_11)
                        }
                        putNull(AnimalEvaluationTable.Columns.TRAIT_UNITS_ID_12)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_UNITS_ID_13)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_UNITS_ID_14)
                        putNull(AnimalEvaluationTable.Columns.TRAIT_UNITS_ID_15)
                    }
                )
                setTransactionSuccessful()
            } finally {
                endTransaction()
            }
        }
    }

    override suspend fun recordDrugAdministeredToAnimal(
        animalId: EntityId,
        drugLotId: EntityId,
        drugLocationId: EntityId,
        drugDosage: String,
        offLabelDrugDoseId: EntityId?,
        timeStamp: LocalDateTime
    ) {
        with(databaseHandler.writableDatabase) {
            beginTransaction()
            try {
                val dateString = Sql.formatDate(timeStamp)
                val timeString = Sql.formatTime(timeStamp)
                val timeStampString = Sql.formatDateTime(timeStamp)
                insertWithPKOrThrow(
                    AnimalDrugTable,
                    null,
                    ContentValues().apply {
                        put(AnimalDrugTable.Columns.ANIMAL_ID, animalId)
                        put(AnimalDrugTable.Columns.DRUG_LOT_ID, drugLotId)
                        put(AnimalDrugTable.Columns.LOCATION_ID, drugLocationId)
                        put(AnimalDrugTable.Columns.DOSAGE, drugDosage)
                        put(AnimalDrugTable.Columns.OFF_LABEL_DRUG_ID, offLabelDrugDoseId)
                        put(AnimalDrugTable.Columns.DATE_ON, dateString)
                        put(AnimalDrugTable.Columns.TIME_ON, timeString)
                        putNull(AnimalDrugTable.Columns.DATE_OFF)
                        putNull(AnimalDrugTable.Columns.TIME_OFF)
                        put(AnimalDrugTable.Columns.CREATED, timeStampString)
                        put(AnimalDrugTable.Columns.MODIFIED, timeStampString)
                    }
                )
                setTransactionSuccessful()
            } finally {
                endTransaction()
            }
        }
    }

    override suspend fun recordAnimalWeaned(animalId: EntityId, timeStamp: LocalDateTime) {
        val dateString = Sql.formatDate(timeStamp)
        val timeStampString = Sql.formatDateTime(timeStamp)
        with(databaseHandler.writableDatabase) {
            beginTransaction()
            try {
                update(
                    AnimalTable.NAME,
                    ContentValues().apply {
                        put(AnimalTable.Columns.WEANED_DATE, dateString)
                        put(AnimalTable.Columns.MODIFIED, timeStampString)
                    },
                    "${AnimalTable.Columns.ID} = ?",
                    arrayOf(animalId.toString())
                )
                val weanedDamId = rawQuery(
                    SQL_QUERY_DAM_ID_FOR_WEANING,
                    arrayOf(animalId.toString())
                ).use { cursor ->
                    cursor.readFirstItem(::weanFromDamIdFromCursor)
                }
                if (weanedDamId != null && weanedDamId.isValid && !Animal.isUnknownAnimal(weanedDamId)) {
                    val femaleBreedingExists = rawQuery(
                        SQL_QUERY_EXISTENCE_OF_FEMALE_BREEDING,
                        arrayOf(weanedDamId.toString())
                    ).use { it.moveToFirst() && it.getBoolean(COLUMN_FEMALE_BREEDING_EXISTS) }
                    if (!femaleBreedingExists) {
                        insertWithPK(
                            AnimalFemaleBreedingTable,
                            null,
                            ContentValues().apply {
                                put(AnimalFemaleBreedingTable.Columns.ANIMAL_ID, weanedDamId)
                                putNull(AnimalFemaleBreedingTable.Columns.MALE_BREEDING_ID)
                                putNull(AnimalFemaleBreedingTable.Columns.BIRTHING_NOTES)
                                putNull(AnimalFemaleBreedingTable.Columns.BIRTHING_DATE)
                                putNull(AnimalFemaleBreedingTable.Columns.BIRTHING_TIME)
                                putNull(AnimalFemaleBreedingTable.Columns.GESTATION_LENGTH)
                                put(AnimalFemaleBreedingTable.Columns.NUMBER_ANIMALS_BORN, 0)
                                put(AnimalFemaleBreedingTable.Columns.NUMBER_ANIMALS_WEANED, 0)
                                put(AnimalFemaleBreedingTable.Columns.CREATED, timeStampString)
                                put(AnimalFemaleBreedingTable.Columns.MODIFIED, timeStampString)
                            }
                        )
                    }
                    execSQL(
                        SQL_INCREMENT_FEMALE_BREEDING_NUMBER_WEANED,
                        arrayOf(weanedDamId.toString(), timeStampString)
                    )
                }
                setTransactionSuccessful()
            } finally {
                endTransaction()
            }
        }
    }

    override fun idsOfOffspringBornInYear(damId: EntityId, year: Int): List<EntityId> {
        return databaseHandler.readableDatabase.rawQuery(
            SQL_QUERY_IDS_OF_OFFSPRING_BORN_TO_DAM_IN_YEAR,
            arrayOf(damId.toString(), year.toString())
        ).use { cursor ->
            cursor.readAllItems { cursor.getEntityId(AnimalTable.Columns.ID) }
        }
    }

    override fun numberOfStillbornsForDamInYear(damId: EntityId, sexId: EntityId, year: Int): Int {
        return databaseHandler.readableDatabase.rawQuery(
            SQL_QUERY_STILLBORNS_OF_SEX_FROM_DAM_IN_YEAR,
            arrayOf(damId.toString(), sexId.toString(), year.toString())
        ).use { cursor ->
            cursor.takeIf { it.moveToFirst() }
                ?.getInt(Sql.Columns.COUNT) ?: 0
        }
    }

    private fun appendOptionalSexStandardParam(arguments: Array<String>, sexStandard: SexStandard?): Array<String> {
        return sexStandard?.let {
            buildList{ addAll(arguments); add(it.code) }.toTypedArray()
        } ?: arguments
    }

    private fun animalCharacteristicFromCursor(cursor: Cursor): AnimalGeneticCharacteristic? {
        val animalGeneticCharacteristicId = cursor.getEntityId(AnimalGeneticCharacteristicTable.Columns.ID)
        val characteristicId = cursor.getEntityId(AnimalGeneticCharacteristicTable.Columns.TABLE_ID)
        val characteristicName = cursor.getString(GeneticCharacteristicTable.Columns.TABLE_DISPLAY_NAME)
        val characteristicValueId = cursor.getEntityId(AnimalGeneticCharacteristicTable.Columns.VALUE_ID)
        val calculationMethodId = cursor.getEntityId(GeneticCharacteristicCalculationMethodTable.Columns.ID)
        val calculationMethodName = cursor.getString(GeneticCharacteristicCalculationMethodTable.Columns.NAME)
        val date = cursor.getLocalDate(AnimalGeneticCharacteristicTable.Columns.DATE)
        val time = cursor.getOptLocalTime(AnimalGeneticCharacteristicTable.Columns.TIME)
        val codonFromId = Codon.fromId(characteristicId)
        val characteristic: GeneticCharacteristic? = when {
            codonFromId != null -> {
                queryCodonCharacteristic(codonFromId, characteristicValueId)
            }
            characteristicId == GeneticCharacteristic.ID_COAT_COLOR -> {
                queryCoatColorCharacteristic(characteristicValueId, characteristicName)
            }
            characteristicId == GeneticCharacteristic.ID_HORN_TYPE -> {
                queryHornTypeCharacteristic(characteristicValueId, characteristicName)
            }
            else -> null
        }
        return characteristic?.let {
            AnimalGeneticCharacteristic(
                id = animalGeneticCharacteristicId,
                geneticCharacteristicId = characteristicId,
                geneticCharacteristicValueId = characteristicValueId,
                geneticCharacteristic = it,
                calculationMethod = GeneticCharacteristic.CalculationMethod(
                    id = calculationMethodId,
                    name = calculationMethodName
                ),
                date = date,
                time = time
            )
        }
    }

    private fun queryCodonCharacteristic(codon: Codon, id: EntityId): CodonCharacteristic? {
        val codonTable = GeneticCodonTable.from(codon)
        return databaseHandler.readableDatabase.rawQuery(
            sqlQueryForCodonCharacteristic(codonTable),
            arrayOf(id.toString())
        ).use { cursor ->
            cursor.readFirstItem {
                CodonCharacteristic(
                    id = cursor.getEntityId(codonTable.Columns.ID),
                    name = cursor.getString(GeneticCharacteristicTable.Columns.TABLE_DISPLAY_NAME),
                    codon = codon,
                    alleles = cursor.getString(codonTable.Columns.ALLELES)
                )
            }
        }
    }

    private fun queryCoatColorCharacteristic(id: EntityId, name: String): CoatColorCharacteristic? {
        return databaseHandler.readableDatabase.rawQuery(
            SQL_QUERY_COAT_COLOR_CHARACTERISTIC,
            arrayOf(id.toString())
        ).use { cursor ->
            cursor.readFirstItem {
                CoatColorCharacteristic(
                    id = cursor.getEntityId(GeneticCoatColorTable.Columns.ID),
                    name = name,
                    coatColor = cursor.getString(GeneticCoatColorTable.Columns.COLOR),
                    coatColorAbbreviation = cursor.getString(GeneticCoatColorTable.Columns.ABBREVIATION)
                )
            }
        }
    }

    private fun queryHornTypeCharacteristic(id: EntityId, name: String): HornTypeCharacteristic? {
        return databaseHandler.readableDatabase.rawQuery(
            SQL_QUERY_HORN_TYPE_CHARACTERISTIC,
            arrayOf(id.toString())
        ).use { cursor ->
            cursor.readFirstItem {
                HornTypeCharacteristic(
                    id = cursor.getEntityId(GeneticHornTypeTable.Columns.ID),
                    name = name,
                    hornType = cursor.getString(GeneticHornTypeTable.Columns.HORN_TYPE),
                    hornTypeAbbreviation = cursor.getString(GeneticHornTypeTable.Columns.HORN_TYPE_ABBREVIATION)
                )
            }
        }
    }

    private fun <T> createQueryFlowFor(pertinentTopics: TopicChange, queryBlock: suspend () -> T): Flow<T> = channelFlow {
        withContext(Dispatchers.IO) {
            send(queryBlock.invoke())
        }
        notifyTopicChanges.filter { it.covers(pertinentTopics) }
            .collectLatest {
                withContext(Dispatchers.IO) {
                    send(queryBlock.invoke())
                }
            }
    }

    companion object {

        private const val TOPIC_NOTIFICATION_EXTRA_BUFFER_CAP = 128

        private val COLUMN_NAME_EID_EXISTS = Column.NotNull("eid_exists")
        private val COLUMN_FEMALE_BREEDING_EXISTS = Column.NotNull("female_breeding_exists")

        private val SQL_QUERY_ANIMAL_NAME get() =
            """SELECT
                ${AnimalTable.Columns.ID},
                ${AnimalTable.Columns.NAME}
                FROM ${AnimalTable.NAME}
                WHERE ${AnimalTable.Columns.ID} = ?"""

        private val SQL_QUERY_FLOCK_PREFIX_FOR_ANIMAL get() =
            """SELECT
                ${FlockPrefixTable.NAME}.${FlockPrefixTable.Columns.PREFIX}
                FROM ${FlockPrefixTable.NAME}
                JOIN ${AnimalFlockPrefixTable.NAME} ON
                    ${FlockPrefixTable.NAME}.${FlockPrefixTable.Columns.ID} =
                    ${AnimalFlockPrefixTable.NAME}.${AnimalFlockPrefixTable.Columns.FLOCK_PREFIX_ID}
                WHERE ${AnimalFlockPrefixTable.NAME}.${AnimalFlockPrefixTable.Columns.ANIMAL_ID} = ?1
                AND ${FlockPrefixTable.NAME}.${FlockPrefixTable.Columns.REGISTRY_COMPANY_ID} = ?2"""

        private val SQL_QUERY_ANIMAL_SPECIES_ID get() =
            """SELECT 
                ${SpeciesTable.NAME}.${SpeciesTable.Columns.ID}
                FROM ${AnimalTable.NAME}
                JOIN ${SexTable.NAME} ON ${SexTable.NAME}.${SexTable.Columns.ID} = 
                    ${AnimalTable.NAME}.${AnimalTable.Columns.SEX_ID}
                JOIN ${SpeciesTable.NAME} ON ${SpeciesTable.NAME}.${SpeciesTable.Columns.ID} =
                    ${SexTable.NAME}.${SexTable.Columns.SPECIES_ID}
                WHERE ${AnimalTable.NAME}.${AnimalTable.Columns.ID} = ?
                LIMIT 1"""

        private val SQL_QUERY_ANIMAL_SPECIES get() =
            """SELECT *FROM ${AnimalTable.NAME}
                JOIN ${SexTable.NAME} ON ${SexTable.NAME}.${SexTable.Columns.ID} = 
                    ${AnimalTable.NAME}.${AnimalTable.Columns.SEX_ID}
                JOIN ${SpeciesTable.NAME} ON ${SpeciesTable.NAME}.${SpeciesTable.Columns.ID} =
                    ${SexTable.NAME}.${SexTable.Columns.SPECIES_ID}
                WHERE ${AnimalTable.NAME}.${AnimalTable.Columns.ID} = ?
                LIMIT 1"""

        private val SQL_QUERY_EXISTENCE_OF_EID get() =
            """SELECT EXISTS (
                SELECT 1 FROM ${AnimalIdInfoTable.NAME}
                WHERE ${AnimalIdInfoTable.Columns.ID_TYPE_ID} = '${IdType.ID_TYPE_ID_EID_RAW}'
                    AND ${AnimalIdInfoTable.Columns.NUMBER} = ?
                ) AS ${COLUMN_NAME_EID_EXISTS}"""

        private val SQL_QUERY_EXISTENCE_OF_EID_EXCEPT_FOR_ID get() =
            """SELECT EXISTS (
                SELECT 1 FROM ${AnimalIdInfoTable.NAME}
                WHERE ${AnimalIdInfoTable.Columns.ID_TYPE_ID} = '${IdType.ID_TYPE_ID_EID_RAW}'
                    AND ${AnimalIdInfoTable.Columns.NUMBER} = ?
                    AND ${AnimalIdInfoTable.Columns.ID} != ?
                ) AS ${COLUMN_NAME_EID_EXISTS}"""

        private val SQL_QUERY_EXISTENCE_OF_FEMALE_BREEDING get() =
            """SELECT EXISTS (
                SELECT 1 FROM ${AnimalFemaleBreedingTable.NAME}
                WHERE ${AnimalFemaleBreedingTable.Columns.ANIMAL_ID} = ?)
                AS ${COLUMN_FEMALE_BREEDING_EXISTS}"""

        private val SQL_PREPEND_BIRTHED_ANIMAL_SEX_ABBR_TO_BIRTHING_NOTES get() =
            """UPDATE ${AnimalFemaleBreedingTable.NAME}
                SET ${AnimalFemaleBreedingTable.Columns.BIRTHING_NOTES} =
                ?2 || COALESCE(${AnimalFemaleBreedingTable.Columns.BIRTHING_NOTES}, ' '),
                ${AnimalFemaleBreedingTable.Columns.MODIFIED} = ?3
                WHERE ${AnimalFemaleBreedingTable.Columns.ID} = ?1"""

        private val SQL_INCREMENT_FEMALE_BREEDING_NUMBER_BORN get() =
            """UPDATE ${AnimalFemaleBreedingTable.NAME}
                SET ${AnimalFemaleBreedingTable.Columns.NUMBER_ANIMALS_BORN} =
                ${AnimalFemaleBreedingTable.Columns.NUMBER_ANIMALS_BORN} + 1,
                ${AnimalFemaleBreedingTable.Columns.MODIFIED} = ?2
                WHERE ${AnimalFemaleBreedingTable.Columns.ID} = ?1"""

        private val SQL_INCREMENT_FEMALE_BREEDING_NUMBER_WEANED get() =
            """UPDATE ${AnimalFemaleBreedingTable.NAME}
                SET ${AnimalFemaleBreedingTable.Columns.NUMBER_ANIMALS_WEANED} =
                ${AnimalFemaleBreedingTable.Columns.NUMBER_ANIMALS_WEANED} + 1,
                ${AnimalFemaleBreedingTable.Columns.MODIFIED} = ?2
                WHERE ${AnimalFemaleBreedingTable.Columns.ANIMAL_ID} = ?1"""

        private val SQL_QUERY_DAM_ID_FOR_WEANING get() =
            """SELECT
                COALESCE(
                    ${AnimalTable.Columns.FOSTER_DAM_ID}, 
                    ${AnimalTable.Columns.SURROGATE_DAM_ID}, 
                    ${AnimalTable.Columns.DAM_ID}
                ) AS ${AnimalTable.Columns.DAM_ID}
                FROM ${AnimalTable.NAME}
                WHERE ${AnimalTable.Columns.ID} = ?"""

        private val SQL_QUERY_ANIMAL_NOTE_HISTORY get() =
            """SELECT
                ${AnimalNoteTable.NAME}.${AnimalNoteTable.Columns.ID},
                ${AnimalNoteTable.NAME}.${AnimalNoteTable.Columns.ANIMAL_ID},
                ${AnimalNoteTable.NAME}.${AnimalNoteTable.Columns.NOTE_TEXT},
                ${AnimalNoteTable.NAME}.${AnimalNoteTable.Columns.NOTE_DATE},
                ${AnimalNoteTable.NAME}.${AnimalNoteTable.Columns.NOTE_TIME},
                ${AnimalNoteTable.NAME}.${AnimalNoteTable.Columns.PREDEFINED_NOTE_ID},
                ${PredefinedNoteTable.NAME}.${PredefinedNoteTable.Columns.NOTE_TEXT}
                FROM ${AnimalNoteTable.NAME}
                LEFT OUTER JOIN ${PredefinedNoteTable.NAME}
                ON ${AnimalNoteTable.NAME}.${AnimalNoteTable.Columns.PREDEFINED_NOTE_ID} =
                    ${PredefinedNoteTable.NAME}.${PredefinedNoteTable.Columns.ID}
                WHERE ${AnimalNoteTable.NAME}.${AnimalNoteTable.Columns.ANIMAL_ID} = ?
                ORDER BY ${AnimalNoteTable.NAME}.${AnimalNoteTable.Columns.NOTE_DATE} DESC,
                    ${AnimalNoteTable.NAME}.${AnimalNoteTable.Columns.NOTE_TIME} DESC"""

        private val SQL_QUERY_ANIMAL_DRUG_HISTORY get() =
            """SELECT
                ${AnimalDrugTable.NAME}.${AnimalDrugTable.Columns.ID},
                ${AnimalDrugTable.NAME}.${AnimalDrugTable.Columns.ANIMAL_ID},
                ${AnimalDrugTable.NAME}.${AnimalDrugTable.Columns.DRUG_LOT_ID},
                ${AnimalDrugTable.NAME}.${AnimalDrugTable.Columns.DATE_ON},
                ${AnimalDrugTable.NAME}.${AnimalDrugTable.Columns.TIME_ON},
                COALESCE(${DrugTable.NAME}.${DrugTable.Columns.TRADE_NAME}, "Unknown Drug")
                    AS ${DrugTable.Columns.TRADE_NAME},
                COALESCE(${DrugLotTable.NAME}.${DrugLotTable.Columns.LOT}, "Unknown Lot")
                    AS ${DrugLotTable.Columns.LOT}
                FROM ${AnimalDrugTable.NAME}
                LEFT OUTER JOIN ${DrugLotTable.NAME}
                ON ${AnimalDrugTable.NAME}.${AnimalDrugTable.Columns.DRUG_LOT_ID} =
                    ${DrugLotTable.NAME}.${DrugLotTable.Columns.ID}
                LEFT OUTER JOIN ${DrugTable.NAME}
                ON ${DrugLotTable.NAME}.${DrugLotTable.Columns.DRUG_ID} =
                    ${DrugTable.NAME}.${DrugTable.Columns.ID}
                WHERE ${AnimalDrugTable.NAME}.${AnimalDrugTable.Columns.ANIMAL_ID} = ?
                ORDER BY ${AnimalDrugTable.NAME}.${AnimalDrugTable.Columns.DATE_ON} DESC,
                    ${AnimalDrugTable.NAME}.${AnimalDrugTable.Columns.TIME_ON} DESC"""

        private val SQL_QUERY_ANIMAL_TISSUE_SAMPLE_HISTORY get() =
            """SELECT
                ${AnimalTissueSampleTakenTable.NAME}.${AnimalTissueSampleTakenTable.Columns.ID},
                ${AnimalTissueSampleTakenTable.NAME}.${AnimalTissueSampleTakenTable.Columns.ANIMAL_ID},
                ${AnimalTissueSampleTakenTable.NAME}.${AnimalTissueSampleTakenTable.Columns.SAMPLE_TYPE_ID},
                ${AnimalTissueSampleTakenTable.NAME}.${AnimalTissueSampleTakenTable.Columns.SAMPLE_DATE},
                ${AnimalTissueSampleTakenTable.NAME}.${AnimalTissueSampleTakenTable.Columns.SAMPLE_TIME},
                COALESCE(${TissueSampleTypeTable.NAME}.${TissueSampleTypeTable.Columns.NAME}, "Unknown Tissue Sample Type") 
                    AS ${TissueSampleTypeTable.Columns.NAME}
                FROM ${AnimalTissueSampleTakenTable.NAME}
                LEFT OUTER JOIN ${TissueSampleTypeTable.NAME}
                ON ${AnimalTissueSampleTakenTable.NAME}.${AnimalTissueSampleTakenTable.Columns.SAMPLE_TYPE_ID} =
                    ${TissueSampleTypeTable.NAME}.${TissueSampleTypeTable.Columns.ID}
                WHERE ${AnimalTissueSampleTakenTable.NAME}.${AnimalTissueSampleTakenTable.Columns.ANIMAL_ID} = ?
                ORDER BY ${AnimalTissueSampleTakenTable.NAME}.${AnimalTissueSampleTakenTable.Columns.SAMPLE_DATE} DESC,
                    ${AnimalTissueSampleTakenTable.NAME}.${AnimalTissueSampleTakenTable.Columns.SAMPLE_TIME} DESC"""

        private val SQL_QUERY_ANIMAL_TISSUE_TEST_HISTORY get() =
            """SELECT
                ${AnimalTissueTestRequestTable.NAME}.${AnimalTissueTestRequestTable.Columns.ID},
                ${AnimalTissueSampleTakenTable.NAME}.${AnimalTissueSampleTakenTable.Columns.ANIMAL_ID},
                ${AnimalTissueTestRequestTable.NAME}.${AnimalTissueTestRequestTable.Columns.TEST_ID},
                COALESCE(${CompanyTable.NAME}.${CompanyTable.Columns.NAME}, "Unknown Laboratory")
                    AS ${CompanyTable.Columns.NAME},
                ${AnimalTissueSampleTakenTable.NAME}.${AnimalTissueSampleTakenTable.Columns.SAMPLE_DATE},
                ${AnimalTissueSampleTakenTable.NAME}.${AnimalTissueSampleTakenTable.Columns.SAMPLE_TIME},
                COALESCE(${TissueTestTable.NAME}.${TissueTestTable.Columns.NAME}, "Unknown Tissue Test")
                    AS ${TissueTestTable.Columns.NAME},
                ${AnimalTissueTestRequestTable.NAME}.${AnimalTissueTestRequestTable.Columns.LABORATORY_ACCESSION_ID},
                ${AnimalTissueTestRequestTable.NAME}.${AnimalTissueTestRequestTable.Columns.TEST_RESULTS_DATE},
                ${AnimalTissueTestRequestTable.NAME}.${AnimalTissueTestRequestTable.Columns.TEST_RESULTS}
                FROM ${AnimalTissueTestRequestTable.NAME}
                JOIN ${AnimalTissueSampleTakenTable.NAME}
                ON ${AnimalTissueTestRequestTable.NAME}.${AnimalTissueTestRequestTable.Columns.SAMPLE_TAKEN_ID} =
                    ${AnimalTissueSampleTakenTable.NAME}.${AnimalTissueSampleTakenTable.Columns.ID}
                LEFT OUTER JOIN ${TissueTestTable.NAME} 
                ON ${AnimalTissueTestRequestTable.NAME}.${AnimalTissueTestRequestTable.Columns.TEST_ID} =
                    ${TissueTestTable.NAME}.${TissueTestTable.Columns.ID}
                LEFT OUTER JOIN ${CompanyLaboratoryTable.NAME}
                ON ${AnimalTissueTestRequestTable.NAME}.${AnimalTissueTestRequestTable.Columns.LABORATORY_ID} =
                    ${CompanyLaboratoryTable.NAME}.${CompanyLaboratoryTable.Columns.ID}
                LEFT OUTER JOIN ${CompanyTable.NAME}
                ON ${CompanyLaboratoryTable.NAME}.${CompanyLaboratoryTable.Columns.COMPANY_ID} =
                    ${CompanyTable.NAME}.${CompanyTable.Columns.ID}
                WHERE ${AnimalTissueSampleTakenTable.NAME}.${AnimalTissueSampleTakenTable.Columns.ANIMAL_ID} = ?
                ORDER BY ${AnimalTissueSampleTakenTable.NAME}.${AnimalTissueSampleTakenTable.Columns.SAMPLE_DATE} DESC,
                    ${AnimalTissueSampleTakenTable.NAME}.${AnimalTissueSampleTakenTable.Columns.SAMPLE_TIME} DESC"""

        private val SQL_QUERY_ANIMAL_PARENTAGE get() =
            """WITH
                cte_animal_trait_ids AS (
                    SELECT 
                        ${AnimalTable.Columns.SIRE_ID}, 
                        ${AnimalTable.Columns.DAM_ID}, 
                        ${SexTable.Columns.SPECIES_ID}
                    FROM ${AnimalTable.NAME}
                    JOIN ${SexTable.NAME} ON ${AnimalTable.NAME}.${AnimalTable.Columns.SEX_ID} = 
                        ${SexTable.NAME}.${SexTable.Columns.ID}
                    WHERE ${AnimalTable.NAME}.${AnimalTable.Columns.ID} = ? 
                    LIMIT 1
                ),
                cte_animal_default_parent_ids AS (
                    SELECT 
                    CASE
                        WHEN ${SexTable.Columns.SPECIES_ID} == '${Species.ID_SHEEP_RAW}' THEN '${Animal.ID_UNKNOWN_SIRE_SHEEP_RAW}'
                        WHEN ${SexTable.Columns.SPECIES_ID} == '${Species.ID_GOAT_RAW}' THEN '${Animal.ID_UNKNOWN_SIRE_GOAT_RAW}'
                        WHEN ${SexTable.Columns.SPECIES_ID} == '${Species.ID_CATTLE_RAW}' THEN '${Animal.ID_UNKNOWN_SIRE_CATTLE_RAW}'
                        WHEN ${SexTable.Columns.SPECIES_ID} == '${Species.ID_HORSE_RAW}' THEN '${Animal.ID_UNKNOWN_SIRE_HORSE_RAW}'
                        WHEN ${SexTable.Columns.SPECIES_ID} == '${Species.ID_DONKEY_RAW}' THEN '${Animal.ID_UNKNOWN_SIRE_DONKEY_RAW}'
                        WHEN ${SexTable.Columns.SPECIES_ID} == '${Species.ID_PIG_RAW}' THEN '${Animal.ID_UNKNOWN_SIRE_PIG_RAW}'
                        ELSE '${EntityId.UNKNOWN_RAW}'
                    END AS _DEFAULT_${AnimalTable.Columns.SIRE_ID},
                    CASE
                        WHEN ${SexTable.Columns.SPECIES_ID} == '${Species.ID_SHEEP_RAW}' THEN '${Animal.ID_UNKNOWN_DAM_SHEEP_RAW}'
                        WHEN ${SexTable.Columns.SPECIES_ID} == '${Species.ID_GOAT_RAW}' THEN '${Animal.ID_UNKNOWN_DAM_GOAT_RAW}'
                        WHEN ${SexTable.Columns.SPECIES_ID} == '${Species.ID_CATTLE_RAW}' THEN '${Animal.ID_UNKNOWN_DAM_CATTLE_RAW}'
                        WHEN ${SexTable.Columns.SPECIES_ID} == '${Species.ID_HORSE_RAW}' THEN '${Animal.ID_UNKNOWN_DAM_HORSE_RAW}'
                        WHEN ${SexTable.Columns.SPECIES_ID} == '${Species.ID_DONKEY_RAW}' THEN '${Animal.ID_UNKNOWN_DAM_DONKEY_RAW}'
                        WHEN ${SexTable.Columns.SPECIES_ID} == '${Species.ID_PIG_RAW}' THEN '${Animal.ID_UNKNOWN_DAM_PIG_RAW}'
                        ELSE '${EntityId.UNKNOWN_RAW}'
                    END	AS _DEFAULT_${AnimalTable.Columns.DAM_ID}
                    FROM cte_animal_trait_ids
                    LIMIT 1
                ),
                cte_lookup_parent_ids AS (
                    SELECT
                        COALESCE(
                            ${AnimalTable.Columns.SIRE_ID}, 
                            (SELECT _DEFAULT_${AnimalTable.Columns.SIRE_ID} FROM cte_animal_default_parent_ids)
                        ) AS _LOOKUP_${AnimalTable.Columns.SIRE_ID},
                        COALESCE(
                            ${AnimalTable.Columns.DAM_ID},
                            (SELECT _DEFAULT_${AnimalTable.Columns.DAM_ID} FROM cte_animal_default_parent_ids)
                        ) AS _LOOKUP_${AnimalTable.Columns.DAM_ID}
                    FROM cte_animal_trait_ids
                    LIMIT 1
                ),
                cte_sire_flock_info AS (
                    SELECT
                        ${FlockPrefixTable.NAME}.${FlockPrefixTable.Columns.ID} AS _SIRE_${FlockPrefixTable.Columns.ID}, 
                        ${FlockPrefixTable.NAME}.${FlockPrefixTable.Columns.PREFIX} AS _SIRE_${FlockPrefixTable.Columns.PREFIX}
                    FROM ${FlockPrefixTable.NAME}
                    JOIN ${AnimalFlockPrefixTable.NAME}
                    ON ${FlockPrefixTable.NAME}.${FlockPrefixTable.Columns.ID} = 
                        ${AnimalFlockPrefixTable.NAME}.${AnimalFlockPrefixTable.Columns.FLOCK_PREFIX_ID}
                    WHERE ${AnimalFlockPrefixTable.NAME}.${AnimalFlockPrefixTable.Columns.ANIMAL_ID} = 
                        (SELECT ${AnimalTable.Columns.SIRE_ID} FROM cte_animal_trait_ids)
                    LIMIT 1
                ),
                cte_dam_flock_info AS (
                    SELECT
                        ${FlockPrefixTable.NAME}.${FlockPrefixTable.Columns.ID} AS _DAM_${FlockPrefixTable.Columns.ID}, 
                        ${FlockPrefixTable.NAME}.${FlockPrefixTable.Columns.PREFIX} AS _DAM_${FlockPrefixTable.Columns.PREFIX}
                    FROM ${FlockPrefixTable.NAME}
                    JOIN ${AnimalFlockPrefixTable.NAME}
                    ON ${FlockPrefixTable.NAME}.${FlockPrefixTable.Columns.ID} =
                        ${AnimalFlockPrefixTable.NAME}.${AnimalFlockPrefixTable.Columns.FLOCK_PREFIX_ID}
                    WHERE ${AnimalFlockPrefixTable.NAME}.${AnimalFlockPrefixTable.Columns.ANIMAL_ID} = 
                        (SELECT ${AnimalTable.Columns.DAM_ID} FROM cte_animal_trait_ids)
                    LIMIT 1
                ),
                cte_sire_owner_ids AS (
                    SELECT
                        ${AnimalOwnershipHistoryTable.Columns.ID},
                        CASE
                            WHEN ${AnimalOwnershipHistoryTable.Columns.TO_CONTACT_ID} IS NOT NULL
                            THEN ${Owner.Type.TYPE_ID_CONTACT}
                            WHEN ${AnimalOwnershipHistoryTable.Columns.TO_COMPANY_ID} IS NOT NULL
                            THEN ${Owner.Type.TYPE_ID_COMPANY}
                            ELSE -1
                        END AS _SIRE_${OwnerUnion.Columns.TYPE},
                        CASE
                            WHEN ${AnimalOwnershipHistoryTable.Columns.TO_CONTACT_ID} IS NOT NULL
                            THEN ${AnimalOwnershipHistoryTable.Columns.TO_CONTACT_ID}
                            WHEN ${AnimalOwnershipHistoryTable.Columns.TO_COMPANY_ID} IS NOT NULL
                            THEN ${AnimalOwnershipHistoryTable.Columns.TO_COMPANY_ID}
                            ELSE -1
                        END AS _SIRE_${OwnerUnion.Columns.ID}
                    FROM ${AnimalOwnershipHistoryTable.NAME} 
                    WHERE ${AnimalOwnershipHistoryTable.Columns.ANIMAL_ID} = 
                        (SELECT ${AnimalTable.Columns.SIRE_ID} FROM cte_animal_trait_ids) 
                    ORDER BY ${AnimalOwnershipHistoryTable.Columns.TRANSFER_DATE} DESC
                    LIMIT 1
                ),
                cte_dam_owner_ids AS (
                    SELECT
                        ${AnimalOwnershipHistoryTable.Columns.ID},
                        CASE
                            WHEN ${AnimalOwnershipHistoryTable.Columns.TO_CONTACT_ID} IS NOT NULL
                            THEN ${Owner.Type.TYPE_ID_CONTACT}
                            WHEN ${AnimalOwnershipHistoryTable.Columns.TO_COMPANY_ID} IS NOT NULL
                            THEN ${Owner.Type.TYPE_ID_COMPANY}
                            ELSE -1
                        END AS _DAM_${OwnerUnion.Columns.TYPE},
                        CASE
                            WHEN ${AnimalOwnershipHistoryTable.Columns.TO_CONTACT_ID} IS NOT NULL
                            THEN ${AnimalOwnershipHistoryTable.Columns.TO_CONTACT_ID}
                            WHEN ${AnimalOwnershipHistoryTable.Columns.TO_COMPANY_ID} IS NOT NULL
                            THEN ${AnimalOwnershipHistoryTable.Columns.TO_COMPANY_ID}
                            ELSE -1
                        END AS _DAM_${OwnerUnion.Columns.ID}
                    FROM ${AnimalOwnershipHistoryTable.NAME} 
                    WHERE ${AnimalOwnershipHistoryTable.Columns.ANIMAL_ID} = 
                        (SELECT ${AnimalTable.Columns.DAM_ID} FROM cte_animal_trait_ids) 
                    ORDER BY ${AnimalOwnershipHistoryTable.Columns.TRANSFER_DATE} DESC
                    LIMIT 1
                )
                SELECT
                    ${AnimalTable.Columns.SIRE_ID},
                    ${AnimalTable.Columns.DAM_ID},
                    _LOOKUP_${AnimalTable.Columns.SIRE_ID},
                    _LOOKUP_${AnimalTable.Columns.DAM_ID},
                    COALESCE(
                        (
                            SELECT ${AnimalTable.Columns.NAME}
                            FROM ${AnimalTable.NAME}
                            WHERE ${AnimalTable.Columns.ID} =
                                (SELECT _LOOKUP_${AnimalTable.Columns.SIRE_ID}
                                    FROM cte_lookup_parent_ids)
                        ),
                        CASE
                            WHEN ${AnimalTable.Columns.SIRE_ID} IS NOT NULL
                            THEN 'Missing Sire (' || ${AnimalTable.Columns.SIRE_ID} || ')'
                            ELSE 'Unspecified Sire'
                        END
                    ) AS _SIRE_${AnimalTable.Columns.NAME},
                    COALESCE(
                        (
                            SELECT ${AnimalTable.Columns.NAME}
                            FROM ${AnimalTable.NAME}
                            WHERE ${AnimalTable.Columns.ID} = 
                                (SELECT _LOOKUP_${AnimalTable.Columns.DAM_ID} 
                                    FROM cte_lookup_parent_ids)
                        ),
                        CASE 
                            WHEN ${AnimalTable.Columns.DAM_ID} IS NOT NULL
                            THEN 'Missing Dam (' || ${AnimalTable.Columns.DAM_ID} || ')' 
                            ELSE 'Unspecified Dam' 
                        END
                    ) AS _DAM_${AnimalTable.Columns.NAME},
                    _SIRE_${OwnerUnion.Columns.TYPE},
                    _SIRE_${OwnerUnion.Columns.ID},
                    CASE
                        WHEN _SIRE_${OwnerUnion.Columns.TYPE} == ${Owner.Type.TYPE_ID_CONTACT}
                        THEN (
                            SELECT ${ContactTable.Columns.FIRST_NAME} || ' ' || ${ContactTable.Columns.LAST_NAME}
                                AS _SIRE_${OwnerUnion.Columns.NAME}
                            FROM ${ContactTable.NAME} 
                            WHERE ${ContactTable.Columns.ID} = _SIRE_${OwnerUnion.Columns.ID})
                        WHEN _SIRE_${OwnerUnion.Columns.TYPE} == ${Owner.Type.TYPE_ID_COMPANY}
                        THEN (
                            SELECT ${CompanyTable.Columns.NAME} 
                            FROM ${CompanyTable.NAME} 
                            WHERE ${CompanyTable.Columns.ID} = _SIRE_${OwnerUnion.Columns.ID})
                        ELSE 'Unknown Sire Owner'
                    END AS _SIRE_${OwnerUnion.Columns.NAME},
                    _DAM_${OwnerUnion.Columns.TYPE},
                    _DAM_${OwnerUnion.Columns.ID},
                    CASE 
                        WHEN _DAM_${OwnerUnion.Columns.TYPE} == ${Owner.Type.TYPE_ID_CONTACT}
                        THEN (
                            SELECT ${ContactTable.Columns.FIRST_NAME} || ' ' || ${ContactTable.Columns.LAST_NAME} 
                            FROM ${ContactTable.NAME} 
                            WHERE ${ContactTable.Columns.ID} = _DAM_${OwnerUnion.Columns.ID})
                        WHEN _DAM_${OwnerUnion.Columns.TYPE} == ${Owner.Type.TYPE_ID_COMPANY}
                        THEN (
                            SELECT ${CompanyTable.Columns.NAME}
                            FROM ${CompanyTable.NAME} 
                            WHERE ${CompanyTable.Columns.ID} = _DAM_${OwnerUnion.Columns.ID})
                        ELSE 'Unknown Dam Owner'
                    END AS _DAM_${OwnerUnion.Columns.NAME},
                    _SIRE_${FlockPrefixTable.Columns.ID},
                    _SIRE_${FlockPrefixTable.Columns.PREFIX},
                    _DAM_${FlockPrefixTable.Columns.ID},
                    _DAM_${FlockPrefixTable.Columns.PREFIX}
                    FROM cte_animal_trait_ids
                    LEFT OUTER JOIN cte_lookup_parent_ids 
                    LEFT OUTER JOIN cte_sire_flock_info
                    LEFT OUTER JOIN cte_dam_flock_info
                    LEFT OUTER JOIN cte_sire_owner_ids
                    LEFT OUTER JOIN cte_dam_owner_ids"""

        val SQL_QUERY_ANIMAL_BREEDING_FOR_ANIMAL get() =
            """SELECT
                 ${AnimalBreedTable.NAME}.${AnimalBreedTable.Columns.ID},
                 ${AnimalBreedTable.NAME}.${AnimalBreedTable.Columns.ANIMAL_ID},
                 ${AnimalBreedTable.NAME}.${AnimalBreedTable.Columns.BREED_ID},
                 ${AnimalBreedTable.NAME}.${AnimalBreedTable.Columns.BREED_PERCENTAGE},
                 ${BreedTable.NAME}.${BreedTable.Columns.NAME},
                 ${BreedTable.NAME}.${BreedTable.Columns.ABBREVIATION}
                FROM ${AnimalBreedTable.NAME}
                JOIN ${BreedTable.NAME} ON
                    ${AnimalBreedTable.NAME}.${AnimalBreedTable.Columns.BREED_ID} =
                    ${BreedTable.NAME}.${BreedTable.Columns.ID}
                WHERE ${AnimalBreedTable.NAME}.${AnimalBreedTable.Columns.ANIMAL_ID} = ?
                ORDER BY ${AnimalBreedTable.NAME}.${AnimalBreedTable.Columns.BREED_PERCENTAGE} DESC"""

        private val SQL_QUERY_ANIMAL_BREEDERS get() =
            """WITH
                cte_animal_trait_ids AS (
                    SELECT
                        ${AnimalTable.Columns.ID},
                        ${AnimalTable.Columns.SIRE_ID}, 
                        ${AnimalTable.Columns.DAM_ID}, 
                        ${SexTable.Columns.SPECIES_ID}
                    FROM ${AnimalTable.NAME}
                    JOIN ${SexTable.NAME} ON ${AnimalTable.NAME}.${AnimalTable.Columns.SEX_ID} = 
                        ${SexTable.NAME}.${SexTable.Columns.ID}
                    WHERE ${AnimalTable.NAME}.${AnimalTable.Columns.ID} = ? 
                    LIMIT 1
                ),
                cte_animal_default_parent_ids AS (
                    SELECT 
                    CASE
                        WHEN ${SexTable.Columns.SPECIES_ID} == '${Species.ID_SHEEP_RAW}' THEN '${Animal.ID_UNKNOWN_SIRE_SHEEP_RAW}'
                        WHEN ${SexTable.Columns.SPECIES_ID} == '${Species.ID_GOAT_RAW}' THEN '${Animal.ID_UNKNOWN_SIRE_GOAT_RAW}'
                        WHEN ${SexTable.Columns.SPECIES_ID} == '${Species.ID_CATTLE_RAW}' THEN '${Animal.ID_UNKNOWN_SIRE_CATTLE_RAW}'
                        WHEN ${SexTable.Columns.SPECIES_ID} == '${Species.ID_HORSE_RAW}' THEN '${Animal.ID_UNKNOWN_SIRE_HORSE_RAW}'
                        WHEN ${SexTable.Columns.SPECIES_ID} == '${Species.ID_DONKEY_RAW}' THEN '${Animal.ID_UNKNOWN_SIRE_DONKEY_RAW}'
                        WHEN ${SexTable.Columns.SPECIES_ID} == '${Species.ID_PIG_RAW}' THEN '${Animal.ID_UNKNOWN_SIRE_PIG_RAW}'
                        ELSE '${EntityId.UNKNOWN_RAW}'
                    END AS _DEFAULT_${AnimalTable.Columns.SIRE_ID},
                    CASE
                        WHEN ${SexTable.Columns.SPECIES_ID} == '${Species.ID_SHEEP_RAW}' THEN '${Animal.ID_UNKNOWN_DAM_SHEEP_RAW}'
                        WHEN ${SexTable.Columns.SPECIES_ID} == '${Species.ID_GOAT_RAW}' THEN '${Animal.ID_UNKNOWN_DAM_GOAT_RAW}'
                        WHEN ${SexTable.Columns.SPECIES_ID} == '${Species.ID_CATTLE_RAW}' THEN '${Animal.ID_UNKNOWN_DAM_CATTLE_RAW}'
                        WHEN ${SexTable.Columns.SPECIES_ID} == '${Species.ID_HORSE_RAW}' THEN '${Animal.ID_UNKNOWN_DAM_HORSE_RAW}'
                        WHEN ${SexTable.Columns.SPECIES_ID} == '${Species.ID_DONKEY_RAW}' THEN '${Animal.ID_UNKNOWN_DAM_DONKEY_RAW}'
                        WHEN ${SexTable.Columns.SPECIES_ID} == '${Species.ID_PIG_RAW}' THEN '${Animal.ID_UNKNOWN_DAM_PIG_RAW}'
                        ELSE '${EntityId.UNKNOWN_RAW}'
                    END	AS _DEFAULT_${AnimalTable.Columns.DAM_ID}
                    FROM cte_animal_trait_ids
                    LIMIT 1
                ),
                cte_lookup_parent_ids AS (
                    SELECT 
                        COALESCE(
                            ${AnimalTable.Columns.SIRE_ID},
                            (SELECT _DEFAULT_${AnimalTable.Columns.SIRE_ID} FROM cte_animal_default_parent_ids)
                        ) AS _LOOKUP_${AnimalTable.Columns.SIRE_ID},
                        COALESCE(
                            ${AnimalTable.Columns.DAM_ID},
                            (SELECT _DEFAULT_${AnimalTable.Columns.DAM_ID} FROM cte_animal_default_parent_ids)
                        ) AS _LOOKUP_${AnimalTable.Columns.DAM_ID}
                    FROM cte_animal_trait_ids
                    LIMIT 1
                ),
                cte_animal_breeder_ids AS (
                    SELECT
                        ${AnimalRegistrationTable.Columns.ID},
                        CASE
                            WHEN ${AnimalRegistrationTable.Columns.BREEDER_CONTACT_ID} IS NOT NULL
                            THEN ${Breeder.TYPE_ID_CONTACT}
                            WHEN ${AnimalRegistrationTable.Columns.BREEDER_COMPANY_ID} IS NOT NULL
                            THEN ${Breeder.TYPE_ID_COMPANY}
                            ELSE -1
                        END AS ${AnimalRegistrationTable.Columns.BREEDER_TYPE_ID},
                        CASE
                            WHEN ${AnimalRegistrationTable.Columns.BREEDER_CONTACT_ID} IS NOT NULL
                            THEN ${AnimalRegistrationTable.Columns.BREEDER_CONTACT_ID}
                            WHEN ${AnimalRegistrationTable.Columns.BREEDER_COMPANY_ID} IS NOT NULL
                            THEN ${AnimalRegistrationTable.Columns.BREEDER_COMPANY_ID}
                            ELSE -1
                        END AS ${AnimalRegistrationTable.Columns.BREEDER_ID}
                    FROM ${AnimalRegistrationTable.NAME} 
                    WHERE ${AnimalRegistrationTable.Columns.ANIMAL_ID} = (SELECT ${AnimalTable.Columns.ID} FROM cte_animal_trait_ids)
                        AND (${AnimalRegistrationTable.Columns.BREEDER_CONTACT_ID} IS NOT NULL
                                OR ${AnimalRegistrationTable.Columns.BREEDER_COMPANY_ID} IS NOT NULL)
                    ORDER BY ${AnimalRegistrationTable.Columns.REGISTRATION_DATE} DESC
                    LIMIT 1
                ),
                cte_sire_breeder_ids AS (
                    SELECT
                        ${AnimalRegistrationTable.Columns.ID},
                        CASE
                            WHEN ${AnimalRegistrationTable.Columns.BREEDER_CONTACT_ID} IS NOT NULL
                            THEN ${Breeder.TYPE_ID_CONTACT}
                            WHEN ${AnimalRegistrationTable.Columns.BREEDER_COMPANY_ID} IS NOT NULL
                            THEN ${Breeder.TYPE_ID_COMPANY}
                            ELSE -1
                        END AS _SIRE_${AnimalRegistrationTable.Columns.BREEDER_TYPE_ID},
                        CASE
                            WHEN ${AnimalRegistrationTable.Columns.BREEDER_CONTACT_ID} IS NOT NULL
                            THEN ${AnimalRegistrationTable.Columns.BREEDER_CONTACT_ID}
                            WHEN ${AnimalRegistrationTable.Columns.BREEDER_COMPANY_ID} IS NOT NULL
                            THEN ${AnimalRegistrationTable.Columns.BREEDER_COMPANY_ID}
                            ELSE -1
                        END AS _SIRE_${AnimalRegistrationTable.Columns.BREEDER_ID}
                    FROM ${AnimalRegistrationTable.NAME} 
                    WHERE ${AnimalRegistrationTable.Columns.ANIMAL_ID} = (SELECT ${AnimalTable.Columns.SIRE_ID} FROM cte_animal_trait_ids)
                        AND (${AnimalRegistrationTable.Columns.BREEDER_CONTACT_ID} IS NOT NULL
                                OR ${AnimalRegistrationTable.Columns.BREEDER_COMPANY_ID} IS NOT NULL)
                    ORDER BY ${AnimalRegistrationTable.Columns.REGISTRATION_DATE} DESC
                    LIMIT 1
                ),
                cte_dam_breeder_ids AS (
                    SELECT
                        ${AnimalRegistrationTable.Columns.ID},
                        CASE
                            WHEN ${AnimalRegistrationTable.Columns.BREEDER_CONTACT_ID} IS NOT NULL
                            THEN ${Breeder.TYPE_ID_CONTACT}
                            WHEN ${AnimalRegistrationTable.Columns.BREEDER_COMPANY_ID} IS NOT NULL
                            THEN ${Breeder.TYPE_ID_COMPANY}
                            ELSE -1
                        END AS _DAM_${AnimalRegistrationTable.Columns.BREEDER_TYPE_ID},
                        CASE
                            WHEN ${AnimalRegistrationTable.Columns.BREEDER_CONTACT_ID} IS NOT NULL
                            THEN ${AnimalRegistrationTable.Columns.BREEDER_CONTACT_ID}
                            WHEN ${AnimalRegistrationTable.Columns.BREEDER_COMPANY_ID} IS NOT NULL
                            THEN ${AnimalRegistrationTable.Columns.BREEDER_COMPANY_ID}
                            ELSE -1
                        END AS _DAM_${AnimalRegistrationTable.Columns.BREEDER_ID}
                    FROM ${AnimalRegistrationTable.NAME} 
                    WHERE ${AnimalRegistrationTable.Columns.ANIMAL_ID} = (SELECT ${AnimalTable.Columns.DAM_ID} FROM cte_animal_trait_ids)
                        AND (${AnimalRegistrationTable.Columns.BREEDER_CONTACT_ID} IS NOT NULL
                            OR ${AnimalRegistrationTable.Columns.BREEDER_COMPANY_ID} IS NOT NULL)
                    ORDER BY ${AnimalRegistrationTable.Columns.REGISTRATION_DATE} DESC
                    LIMIT 1
                )
                SELECT
                    ${AnimalTable.Columns.ID},
                    ${AnimalTable.Columns.SIRE_ID},
                    ${AnimalTable.Columns.DAM_ID},
                    _LOOKUP_${AnimalTable.Columns.SIRE_ID},
                    _LOOKUP_${AnimalTable.Columns.DAM_ID},
                    ${AnimalRegistrationTable.Columns.BREEDER_TYPE_ID},
                    ${AnimalRegistrationTable.Columns.BREEDER_ID},
                    CASE
                        WHEN ${AnimalRegistrationTable.Columns.BREEDER_TYPE_ID} == ${Breeder.TYPE_ID_CONTACT}
                        THEN (
                            SELECT 
                                CASE 
                                    WHEN ${ContactTable.Columns.FIRST_NAME} <> ${ContactTable.Columns.LAST_NAME}
                                    THEN ${ContactTable.Columns.FIRST_NAME} || ' ' || ${ContactTable.Columns.LAST_NAME}
                                    ELSE ${ContactTable.Columns.LAST_NAME}
                                END
                            FROM ${ContactTable.NAME}
                            WHERE ${ContactTable.Columns.ID} = ${AnimalRegistrationTable.Columns.BREEDER_ID}
                        )
                        WHEN ${AnimalRegistrationTable.Columns.BREEDER_TYPE_ID} == ${Breeder.TYPE_ID_COMPANY}
                        THEN (
                            SELECT ${CompanyTable.Columns.NAME} 
                            FROM ${CompanyTable.NAME} 
                            WHERE ${CompanyTable.Columns.ID} = ${AnimalRegistrationTable.Columns.BREEDER_ID})
                        ELSE 'Unknown Breeder'
                    END AS ${AnimalRegistrationTable.Columns.BREEDER_NAME},
                    _SIRE_${AnimalRegistrationTable.Columns.BREEDER_TYPE_ID},
                    _SIRE_${AnimalRegistrationTable.Columns.BREEDER_ID},
                    CASE
                        WHEN _SIRE_${AnimalRegistrationTable.Columns.BREEDER_TYPE_ID} == ${Breeder.TYPE_ID_CONTACT}
                        THEN (
                            SELECT ${ContactTable.Columns.FIRST_NAME} || ' ' || ${ContactTable.Columns.LAST_NAME}
                            FROM ${ContactTable.NAME} 
                            WHERE ${ContactTable.Columns.ID} = _SIRE_${AnimalRegistrationTable.Columns.BREEDER_ID}
                        )
                        WHEN _SIRE_${AnimalRegistrationTable.Columns.BREEDER_TYPE_ID} == ${Breeder.TYPE_ID_COMPANY}
                        THEN (
                            SELECT ${CompanyTable.Columns.NAME} 
                            FROM ${CompanyTable.NAME} 
                            WHERE ${CompanyTable.Columns.ID} = _SIRE_${AnimalRegistrationTable.Columns.BREEDER_ID})
                        ELSE 'Unknown Sire Breeder'
                    END AS _SIRE_${AnimalRegistrationTable.Columns.BREEDER_NAME},
                    _DAM_${AnimalRegistrationTable.Columns.BREEDER_TYPE_ID},
                    _DAM_${AnimalRegistrationTable.Columns.BREEDER_ID},
                    CASE 
                        WHEN _DAM_${AnimalRegistrationTable.Columns.BREEDER_TYPE_ID} == ${Breeder.TYPE_ID_CONTACT}
                        THEN (
                            SELECT ${ContactTable.Columns.FIRST_NAME} || ' ' || ${ContactTable.Columns.LAST_NAME} 
                            FROM ${ContactTable.NAME} 
                            WHERE ${ContactTable.Columns.ID} = _DAM_${AnimalRegistrationTable.Columns.BREEDER_ID})
                        WHEN _DAM_${AnimalRegistrationTable.Columns.BREEDER_TYPE_ID} == ${Breeder.TYPE_ID_COMPANY}
                        THEN (
                            SELECT ${CompanyTable.Columns.NAME}
                            FROM ${CompanyTable.NAME} 
                            WHERE ${CompanyTable.Columns.ID} = _DAM_${AnimalRegistrationTable.Columns.BREEDER_ID})
                        ELSE 'Unknown Dam Breeder'
                    END AS _DAM_${AnimalRegistrationTable.Columns.BREEDER_NAME}
                    FROM cte_animal_trait_ids
                    LEFT OUTER JOIN cte_lookup_parent_ids
                    LEFT OUTER JOIN cte_animal_breeder_ids
                    LEFT OUTER JOIN cte_sire_breeder_ids
                    LEFT OUTER JOIN cte_dam_breeder_ids"""

        val SQL_QUERY_ANIMAL_ALERTS get() =
            """SELECT * FROM ${AnimalAlertTable.NAME}
                WHERE ${AnimalAlertTable.Columns.ANIMAL_ID} = ?
                ORDER BY ${AnimalAlertTable.Columns.ALERT_DATE} DESC,
                    ${AnimalAlertTable.Columns.ALERT_TIME} DESC"""

        val SQL_QUERY_GENETIC_CHARACTERISTICS get() =
            """SELECT * FROM ${AnimalGeneticCharacteristicTable.NAME}
                JOIN ${GeneticCharacteristicCalculationMethodTable.NAME}
                ON ${AnimalGeneticCharacteristicTable.NAME}.${AnimalGeneticCharacteristicTable.Columns.CALCULATION_ID} =
                    ${GeneticCharacteristicCalculationMethodTable.NAME}.${GeneticCharacteristicCalculationMethodTable.Columns.ID}
                JOIN ${GeneticCharacteristicTable.NAME}
                ON ${AnimalGeneticCharacteristicTable.NAME}.${AnimalGeneticCharacteristicTable.Columns.TABLE_ID} =
                    ${GeneticCharacteristicTable.NAME}.${GeneticCharacteristicTable.Columns.ID}
                WHERE ${AnimalGeneticCharacteristicTable.Columns.ANIMAL_ID} = ?
                ORDER BY ${AnimalGeneticCharacteristicTable.Columns.TABLE_ID},
                ${AnimalGeneticCharacteristicTable.Columns.DATE} DESC,
                ${AnimalGeneticCharacteristicTable.Columns.TIME} DESC"""

        val SQL_QUERY_CODON_CHARACTERISTIC_FOR_ANIMAL get() =
            """SELECT
                 ${AnimalGeneticCharacteristicTable.Columns.ID},
                 ${AnimalGeneticCharacteristicTable.Columns.ANIMAL_ID},
                 ${AnimalGeneticCharacteristicTable.Columns.TABLE_ID},
                 ${AnimalGeneticCharacteristicTable.Columns.VALUE_ID},
                 CASE
                    WHEN ${AnimalGeneticCharacteristicTable.Columns.CALCULATION_ID}
                        = '${GeneticCharacteristic.CalculationMethod.ID_DNA_RAW}' THEN 3
                    WHEN ${AnimalGeneticCharacteristicTable.Columns.CALCULATION_ID}
                        = '${GeneticCharacteristic.CalculationMethod.ID_PEDIGREE_RAW}' THEN 2
                    WHEN ${AnimalGeneticCharacteristicTable.Columns.CALCULATION_ID} 
                        = '${GeneticCharacteristic.CalculationMethod.ID_OBSERVATION_RAW}' THEN 1
                    ELSE 0
                 END AS ${Sql.Columns.PRIORITY}
                FROM ${AnimalGeneticCharacteristicTable.NAME}
                WHERE ${AnimalGeneticCharacteristicTable.Columns.ANIMAL_ID} = ?1
                AND ${AnimalGeneticCharacteristicTable.Columns.TABLE_ID} = ?2
                ORDER BY ${Sql.Columns.PRIORITY} DESC"""

        val SQL_QUERY_FEMALE_BREEDING_HISTORY get() =
            """SELECT
                ${AnimalFemaleBreedingTable.NAME}.${AnimalFemaleBreedingTable.Columns.ID},
                ${AnimalFemaleBreedingTable.NAME}.${AnimalFemaleBreedingTable.Columns.ANIMAL_ID},
                ${AnimalFemaleBreedingTable.NAME}.${AnimalFemaleBreedingTable.Columns.BIRTHING_NOTES},
                COALESCE(
                    ${AnimalFemaleBreedingTable.NAME}.${AnimalFemaleBreedingTable.Columns.BIRTHING_DATE},
                    ${AnimalMaleBreedingTable.NAME}.${AnimalMaleBreedingTable.Columns.DATE_OUT}
                ) AS EVENT_DATE,
                COALESCE(
                    ${AnimalFemaleBreedingTable.NAME}.${AnimalFemaleBreedingTable.Columns.BIRTHING_TIME},
                    ${AnimalMaleBreedingTable.NAME}.${AnimalMaleBreedingTable.Columns.TIME_OUT}
                ) AS EVENT_TIME
                FROM ${AnimalFemaleBreedingTable.NAME}
                LEFT JOIN ${AnimalMaleBreedingTable.NAME}
                ON ${AnimalFemaleBreedingTable.NAME}.${AnimalFemaleBreedingTable.Columns.MALE_BREEDING_ID} =
                    ${AnimalMaleBreedingTable.NAME}.${AnimalMaleBreedingTable.Columns.ID}
                WHERE ${AnimalFemaleBreedingTable.NAME}.${AnimalFemaleBreedingTable.Columns.ANIMAL_ID} = ?
                AND (
                        (
                            ${AnimalFemaleBreedingTable.NAME}.${AnimalFemaleBreedingTable.Columns.BIRTHING_DATE} IS NOT NULL
                            AND ${AnimalFemaleBreedingTable.NAME}.${AnimalFemaleBreedingTable.Columns.BIRTHING_TIME} IS NOT NULL
                        ) OR 
                        (
                            ${AnimalMaleBreedingTable.NAME}.${AnimalMaleBreedingTable.Columns.DATE_OUT} IS NOT NULL
                            AND ${AnimalMaleBreedingTable.NAME}.${AnimalMaleBreedingTable.Columns.TIME_OUT} IS NOT NULL
                        )
                    )
                AND ${AnimalFemaleBreedingTable.NAME}.${AnimalFemaleBreedingTable.Columns.BIRTHING_NOTES} IS NOT NULL
                AND ${AnimalFemaleBreedingTable.NAME}.${AnimalFemaleBreedingTable.Columns.BIRTHING_NOTES} <> ''
                ORDER BY EVENT_DATE DESC, EVENT_TIME DESC"""

        private val COLUMN_FEMALE_BREEDING_ANIMAL_ID = Column.NotNull("female_breeding_id_animalid")
        private val COLUMN_MALE_BREEDING_ANIMAL_ID = Column.NotNull("male_breeding_id_animal_id")

        private val SQL_QUERY_ANIMAL_FEMALE_BREEDING_BASE get() =
            """SELECT
                 ${AnimalFemaleBreedingTable.NAME}.${AnimalFemaleBreedingTable.Columns.ID},
                 ${AnimalFemaleBreedingTable.NAME}.${AnimalFemaleBreedingTable.Columns.ANIMAL_ID} AS ${COLUMN_FEMALE_BREEDING_ANIMAL_ID},
                 ${AnimalFemaleBreedingTable.NAME}.${AnimalFemaleBreedingTable.Columns.BIRTHING_DATE},
                 ${AnimalFemaleBreedingTable.NAME}.${AnimalFemaleBreedingTable.Columns.BIRTHING_TIME},
                 ${AnimalFemaleBreedingTable.NAME}.${AnimalFemaleBreedingTable.Columns.BIRTHING_NOTES},
                 ${AnimalFemaleBreedingTable.NAME}.${AnimalFemaleBreedingTable.Columns.NUMBER_ANIMALS_BORN},
                 ${AnimalFemaleBreedingTable.NAME}.${AnimalFemaleBreedingTable.Columns.NUMBER_ANIMALS_WEANED},
                 ${AnimalFemaleBreedingTable.NAME}.${AnimalFemaleBreedingTable.Columns.GESTATION_LENGTH},
                 ${AnimalMaleBreedingTable.NAME}.${AnimalMaleBreedingTable.Columns.ID},
                 ${AnimalMaleBreedingTable.NAME}.${AnimalMaleBreedingTable.Columns.ANIMAL_ID} AS ${COLUMN_MALE_BREEDING_ANIMAL_ID},
                 ${AnimalMaleBreedingTable.NAME}.${AnimalMaleBreedingTable.Columns.DATE_IN},
                 ${AnimalMaleBreedingTable.NAME}.${AnimalMaleBreedingTable.Columns.TIME_IN},
                 ${AnimalMaleBreedingTable.NAME}.${AnimalMaleBreedingTable.Columns.DATE_OUT},
                 ${AnimalMaleBreedingTable.NAME}.${AnimalMaleBreedingTable.Columns.TIME_OUT},
                 ${ServiceTypeTable.NAME}.${ServiceTypeTable.Columns.ID},
                 ${ServiceTypeTable.NAME}.${ServiceTypeTable.Columns.NAME},
                 ${ServiceTypeTable.NAME}.${ServiceTypeTable.Columns.ABBREVIATION},
                 ${ServiceTypeTable.NAME}.${ServiceTypeTable.Columns.ORDER}
                FROM ${AnimalFemaleBreedingTable.NAME}
                LEFT OUTER JOIN ${AnimalMaleBreedingTable.NAME} ON
                    ${AnimalFemaleBreedingTable.NAME}.${AnimalFemaleBreedingTable.Columns.MALE_BREEDING_ID} =
                    ${AnimalMaleBreedingTable.NAME}.${AnimalMaleBreedingTable.Columns.ID}
                LEFT OUTER JOIN ${ServiceTypeTable.NAME} ON
                    ${AnimalMaleBreedingTable.NAME}.${AnimalMaleBreedingTable.Columns.SERVICE_TYPE_ID} =
                    ${ServiceTypeTable.NAME}.${ServiceTypeTable.Columns.ID}"""

        private val SQL_QUERY_ANIMAL_FEMALE_BREEDING get() =
            """$SQL_QUERY_ANIMAL_FEMALE_BREEDING_BASE
                WHERE ${AnimalFemaleBreedingTable.NAME}.${AnimalFemaleBreedingTable.Columns.ANIMAL_ID} = ?"""

        private val SQL_QUERY_ANIMAL_FEMALE_BREEDING_BY_ID get() =
            """$SQL_QUERY_ANIMAL_FEMALE_BREEDING_BASE
                WHERE ${AnimalFemaleBreedingTable.NAME}.${AnimalFemaleBreedingTable.Columns.ID} = ?"""

        private val SQL_QUERY_POTENTIAL_SIRES_BY_DAM_ID get() =
            """SELECT
                ${AnimalMaleBreedingTable.NAME}.${AnimalMaleBreedingTable.Columns.ID},
                ${AnimalMaleBreedingTable.NAME}.${AnimalMaleBreedingTable.Columns.ANIMAL_ID},
                ${AnimalMaleBreedingTable.NAME}.${AnimalMaleBreedingTable.Columns.DATE_IN},
                ${AnimalMaleBreedingTable.NAME}.${AnimalMaleBreedingTable.Columns.DATE_OUT},
                ${AnimalMaleBreedingTable.NAME}.${AnimalMaleBreedingTable.Columns.SERVICE_TYPE_ID}
                FROM ${AnimalMaleBreedingTable.NAME}
                LEFT OUTER JOIN ${AnimalFemaleBreedingTable.NAME} ON
                    ${AnimalFemaleBreedingTable.NAME}.${AnimalFemaleBreedingTable.Columns.MALE_BREEDING_ID} = 
                        ${AnimalMaleBreedingTable.NAME}.${AnimalMaleBreedingTable.Columns.ID}
                INNER JOIN ${ServiceTypeTable.NAME} ON
                    ${AnimalMaleBreedingTable.NAME}.${AnimalMaleBreedingTable.Columns.SERVICE_TYPE_ID} = 
                        ${ServiceTypeTable.NAME}.${ServiceTypeTable.Columns.ID}
                WHERE ${AnimalFemaleBreedingTable.NAME}.${AnimalFemaleBreedingTable.Columns.ANIMAL_ID} = ?"""

        private val SQL_QUERY_IDS_OF_OFFSPRING_BORN_TO_DAM_IN_YEAR get() =
            """SELECT ${AnimalTable.Columns.ID}
                FROM ${AnimalTable.NAME}
                WHERE ${AnimalTable.Columns.DAM_ID} = ?1
                AND STRFTIME('%Y', ${AnimalTable.Columns.BIRTH_DATE}) = ?2"""

        private val SQL_QUERY_STILLBORNS_OF_SEX_FROM_DAM_IN_YEAR get() =
            """SELECT COUNT(*) AS ${Sql.Columns.COUNT}
                FROM ${AnimalTable.NAME}
                WHERE ${AnimalTable.Columns.DAM_ID} = ?1
                AND ${AnimalTable.Columns.SEX_ID} = ?2
                AND ${AnimalTable.Columns.DEATH_REASON_ID} = '${DeathReason.ID_DEATH_REASON_STILLBORN_RAW}'
                AND STRFTIME('%Y', ${AnimalTable.Columns.BIRTH_DATE}) = ?3"""

        private fun sqlQueryForCodonCharacteristic(codonTable: GeneticCodonTable): String {
            return """SELECT
                 ${codonTable.NAME}.${codonTable.Columns.ID},
                 ${codonTable.NAME}.${codonTable.Columns.ALLELES},
                 ${codonTable.NAME}.${codonTable.Columns.ORDER},
                 ${GeneticCharacteristicTable.NAME}.${GeneticCharacteristicTable.Columns.TABLE_DISPLAY_NAME}
                FROM ${codonTable.NAME}
                JOIN ${GeneticCharacteristicTable.NAME} ON
                    ${GeneticCharacteristicTable.NAME}.${GeneticCharacteristicTable.Columns.ID} = '${codonTable.codon.id.raw}'
                WHERE ${codonTable.Columns.ID} = ?"""
        }

        private val SQL_QUERY_HORN_TYPE_CHARACTERISTIC get() =
            """SELECT * FROM ${GeneticHornTypeTable.NAME}
                WHERE ${GeneticHornTypeTable.Columns.ID} = ?"""

        private val SQL_QUERY_COAT_COLOR_CHARACTERISTIC get() =
            """SELECT * FROM ${GeneticCoatColorTable.NAME}
                WHERE ${GeneticCoatColorTable.Columns.ID} = ?"""

        private val SQL_QUERY_ANIMAL_BREEDS_PARTITIONED_ANIMAL_ID get() =
            """SELECT
                ${AnimalBreedTable.NAME}.${AnimalBreedTable.Columns.ANIMAL_ID},
                ${AnimalBreedTable.NAME}.${AnimalBreedTable.Columns.BREED_ID},
                ${AnimalBreedTable.NAME}.${AnimalBreedTable.Columns.BREED_PERCENTAGE},
                ${BreedTable.NAME}.${BreedTable.Columns.NAME},
                ${BreedTable.NAME}.${BreedTable.Columns.ABBREVIATION},
                ${BreedTable.NAME}.${BreedTable.Columns.ORDER},
                ROW_NUMBER() OVER (
                    PARTITION BY ${AnimalBreedTable.NAME}.${AnimalBreedTable.Columns.ANIMAL_ID}
                    ORDER BY ${AnimalBreedTable.NAME}.${AnimalBreedTable.Columns.BREED_PERCENTAGE} DESC,
                    ${BreedTable.NAME}.${BreedTable.Columns.ORDER}
                ) breed_percentage_seq
            FROM ${AnimalBreedTable.NAME}
            INNER JOIN ${BreedTable.NAME}
                ON ${AnimalBreedTable.NAME}.${AnimalBreedTable.Columns.BREED_ID} 
                    = ${BreedTable.NAME}.${BreedTable.Columns.ID}"""

        /**
         * Uses of this base query for animal basic info MUST
         * include [QUALIFIER_TO_FILTER_SPECIAL_MARKER_ANIMALS]
         * in their WHERE clause to filter out animal_table
         * records that do not represent real animal data but
         * exists as marker objects/entities.  These marker
         * "animals" do not adhere to the data contract
         * for animal data.
         */
        private val SQL_QUERY_ANIMAL_BASIC_INFO get() =
            """SELECT ${AnimalTable.NAME}.${AnimalTable.Columns.ID},
                    ${AnimalTable.NAME}.${AnimalTable.Columns.NAME},
                    ${AnimalTable.NAME}.${AnimalTable.Columns.BIRTH_DATE},
                    ${AnimalTable.NAME}.${AnimalTable.Columns.WEANED_DATE},
                    ${AnimalTable.NAME}.${AnimalTable.Columns.DEATH_DATE},
				    ${AnimalIdInfoTable.NAME}.${AnimalIdInfoTable.Columns.NUMBER}, 
    				${FlockPrefixTable.NAME}.${FlockPrefixTable.Columns.PREFIX},
				    ${CompanyTable.NAME}.${CompanyTable.Columns.NAME},
				    ${ContactTable.Columns.FIRST_NAME} || ' ' || ${ContactTable.Columns.LAST_NAME} 
                        AS ${ContactTable.Columns.FULL_NAME_ALIAS},
                    ${AnimalIdInfoTable.NAME}.${AnimalIdInfoTable.Columns.ID},
                    ${IdTypeTable.NAME}.${IdTypeTable.Columns.ID},
				    ${IdTypeTable.NAME}.${IdTypeTable.Columns.NAME},
                    ${IdTypeTable.NAME}.${IdTypeTable.Columns.ABBREVIATION},
                    ${IdTypeTable.NAME}.${IdTypeTable.Columns.ORDER},
                    ${IdColorTable.NAME}.${IdColorTable.Columns.ID},
                    ${IdColorTable.NAME}.${IdColorTable.Columns.NAME},
                    ${IdColorTable.NAME}.${IdColorTable.Columns.ABBREVIATION},
                    ${IdColorTable.NAME}.${IdColorTable.Columns.ORDER},
                    ${IdLocationTable.NAME}.${IdLocationTable.Columns.ID},
                    ${IdLocationTable.NAME}.${IdLocationTable.Columns.NAME},
                    ${IdLocationTable.NAME}.${IdLocationTable.Columns.ABBREVIATION},
                    ${IdLocationTable.NAME}.${IdLocationTable.Columns.ORDER},
                    ${AnimalIdInfoTable.NAME}.${AnimalIdInfoTable.Columns.IS_OFFICIAL_ID},
                    ${AnimalIdInfoTable.NAME}.${AnimalIdInfoTable.Columns.DATE_ON},
                    ${AnimalIdInfoTable.NAME}.${AnimalIdInfoTable.Columns.TIME_ON},
                    ${AnimalIdInfoTable.NAME}.${AnimalIdInfoTable.Columns.DATE_OFF},
                    ${AnimalIdInfoTable.NAME}.${AnimalIdInfoTable.Columns.TIME_OFF},
                    ${AnimalIdInfoTable.NAME}.${AnimalIdInfoTable.Columns.REMOVE_REASON_ID},
                    /*
                        Note that the next columns use AnimalIdInfoTable.NAME as a qualifier
                        despite being from IdRemoveReasonTable. See the join on the
                        animal id info table and id remove reason table below.
                    */
                    ${AnimalIdInfoTable.NAME}.${IdRemoveReasonTable.Columns.ID},
                    ${AnimalIdInfoTable.NAME}.${IdRemoveReasonTable.Columns.REMOVE_REASON},
                    ${AnimalIdInfoTable.NAME}.${IdRemoveReasonTable.Columns.ORDER},
                    ${SpeciesTable.NAME}.${SpeciesTable.Columns.ID},
                    ${SpeciesTable.NAME}.${SpeciesTable.Columns.COMMON_NAME},
                    ${AnimalBreedTable.NAME}.${BreedTable.Columns.ID},
                    ${AnimalBreedTable.NAME}.${BreedTable.Columns.NAME},
                    ${AnimalBreedTable.NAME}.${BreedTable.Columns.ABBREVIATION},
                    ${AnimalBreedTable.NAME}.${AnimalBreedTable.Columns.BREED_PERCENTAGE},
                    ${SexTable.NAME}.${SexTable.Columns.ID},
                    ${SexTable.NAME}.${SexTable.Columns.NAME},
                    ${SexTable.NAME}.${SexTable.Columns.ABBREVIATION},
                    ${SexTable.NAME}.${SexTable.Columns.STANDARD},
                    ${SexTable.NAME}.${SexTable.Columns.STANDARD_ABBREVIATION}
                FROM ${AnimalTable.NAME}
                LEFT OUTER JOIN (
                    SELECT * FROM ${AnimalIdInfoTable.NAME}
                    LEFT OUTER JOIN ${IdRemoveReasonTable.NAME}
                        ON ${AnimalIdInfoTable.NAME}.${AnimalIdInfoTable.Columns.REMOVE_REASON_ID} =
                            ${IdRemoveReasonTable.NAME}.${IdRemoveReasonTable.Columns.ID}
                    WHERE ${AnimalIdInfoTable.NAME}.${AnimalIdInfoTable.Columns.DATE_OFF} IS NULL
                ) AS ${AnimalIdInfoTable.NAME}
                    ON ${AnimalTable.NAME}.${AnimalTable.Columns.ID} = ${AnimalIdInfoTable.NAME}.${AnimalIdInfoTable.Columns.ANIMAL_ID}
                LEFT OUTER JOIN ${IdColorTable.NAME} 
                    ON ${AnimalIdInfoTable.NAME}.${AnimalIdInfoTable.Columns.MALE_ID_COLOR_ID} = ${IdColorTable.NAME}.${IdColorTable.Columns.ID}
                LEFT OUTER JOIN ${IdLocationTable.NAME}
                    ON ${AnimalIdInfoTable.NAME}.${AnimalIdInfoTable.Columns.ID_LOCATION_ID} = ${IdLocationTable.NAME}.${IdLocationTable.Columns.ID}
                LEFT OUTER JOIN ${IdTypeTable.NAME}
                    ON ${AnimalIdInfoTable.NAME}.${AnimalIdInfoTable.Columns.ID_TYPE_ID} = ${IdTypeTable.NAME}.${IdTypeTable.Columns.ID}
                LEFT OUTER JOIN ${AnimalFlockPrefixTable.NAME}
                    ON ${AnimalTable.NAME}.${AnimalTable.Columns.ID} = ${AnimalFlockPrefixTable.NAME}.${AnimalFlockPrefixTable.Columns.ANIMAL_ID}
                LEFT OUTER JOIN ${FlockPrefixTable.NAME}
                    ON ${AnimalFlockPrefixTable.NAME}.${AnimalFlockPrefixTable.Columns.FLOCK_PREFIX_ID} = ${FlockPrefixTable.NAME}.${FlockPrefixTable.Columns.ID}
                LEFT OUTER JOIN (
                    ${AnimalOwnershipHistoryTable.Sql.SQL_CURRENT_ANIMAL_OWNERSHIP}
                ) AS animal_ownership_table
                    ON ${AnimalTable.NAME}.${AnimalTable.Columns.ID} = animal_ownership_table.${AnimalOwnershipHistoryTable.Columns.ANIMAL_ID}
                LEFT OUTER JOIN ${CompanyTable.NAME}
                    ON animal_ownership_table.${AnimalOwnershipHistoryTable.Columns.TO_COMPANY_ID} = ${CompanyTable.NAME}.${CompanyTable.Columns.ID}
                LEFT OUTER JOIN ${ContactTable.NAME}
                    ON animal_ownership_table.${AnimalOwnershipHistoryTable.Columns.TO_CONTACT_ID} = ${ContactTable.NAME}.${ContactTable.Columns.ID}
                INNER JOIN ${SexTable.NAME} 
                    ON ${AnimalTable.NAME}.${AnimalTable.Columns.SEX_ID} = ${SexTable.NAME}.${SexTable.Columns.ID}
                INNER JOIN ${SpeciesTable.NAME}
                    ON ${SexTable.NAME}.${SexTable.Columns.SPECIES_ID} = ${SpeciesTable.NAME}.${SpeciesTable.Columns.ID}
                INNER JOIN (
                    SELECT
                        ${AnimalBreedTable.Columns.ANIMAL_ID},
                        ${AnimalBreedTable.Columns.BREED_ID},
                        ${AnimalBreedTable.Columns.BREED_PERCENTAGE},
                        ${BreedTable.Columns.NAME},
                        ${BreedTable.Columns.ABBREVIATION}
                    FROM (
                        $SQL_QUERY_ANIMAL_BREEDS_PARTITIONED_ANIMAL_ID
                    )
                    WHERE breed_percentage_seq = 1 
                ) AS ${AnimalBreedTable.NAME}
                    ON ${AnimalTable.NAME}.${AnimalTable.Columns.ID} = ${AnimalBreedTable.NAME}.${AnimalBreedTable.Columns.ANIMAL_ID}"""

        private val ORDER_BY_FOR_ANIMAL_BASIC_INFO get() =
            """ORDER BY
                ${AnimalTable.NAME}.${AnimalTable.Columns.NAME} ASC,
                ${AnimalTable.NAME}.${AnimalTable.Columns.ID} ASC,
                ${IdTypeTable.NAME}.${IdTypeTable.Columns.NAME} ASC"""

        /**
         * This qualifier is required for all animal basic info queries to
         * filter about animal_table entries that do not adhere to the normal
         * data contract for real animals (animal entries used to track unknown
         * sires, dams, or other marker "objects" in genealogy trees, etc).
         */
        private val QUALIFIER_TO_FILTER_SPECIAL_MARKER_ANIMALS get() =
            """${AnimalTable.NAME}.${AnimalTable.Columns.BIRTH_DATE} IS NOT NULL"""

        private val QUALIFIER_FOR_ANIMAL_BASIC_INFO_BY_SPECIES get() =
            """${SexTable.NAME}.${SexTable.Columns.SPECIES_ID} = ?1"""

        private val QUALIFIER_FOR_ANIMAL_BASIC_INFO_BY_ID_TYPE get() =
            """${AnimalTable.NAME}.${AnimalTable.Columns.ID} IN (
                    SELECT ${AnimalIdInfoTable.Columns.ANIMAL_ID}
                    FROM ${AnimalIdInfoTable.NAME}
                    WHERE ${AnimalIdInfoTable.Columns.ID_TYPE_ID} = ?2
                    AND ${AnimalIdInfoTable.Columns.NUMBER}
                        LIKE '%' || ?3 || '%' ${Sql.ESCAPE_CLAUSE}
                )"""

        private val SQL_SEARCH_ANIMAL_BASIC_INFO_BY_ID_TYPE get() =
            """${SQL_QUERY_ANIMAL_BASIC_INFO}
                WHERE ${QUALIFIER_TO_FILTER_SPECIAL_MARKER_ANIMALS} 
                AND ${QUALIFIER_FOR_ANIMAL_BASIC_INFO_BY_SPECIES}
                AND ${QUALIFIER_FOR_ANIMAL_BASIC_INFO_BY_ID_TYPE}
                ${ORDER_BY_FOR_ANIMAL_BASIC_INFO}"""

        private val SQL_SEARCH_ANIMAL_BASIC_INFO_BY_ID_TYPE_AND_SEX_STANDARD get() =
            """${SQL_QUERY_ANIMAL_BASIC_INFO}
                WHERE ${QUALIFIER_TO_FILTER_SPECIAL_MARKER_ANIMALS} 
                AND ${QUALIFIER_FOR_ANIMAL_BASIC_INFO_BY_SPECIES}
                AND ${QUALIFIER_FOR_ANIMAL_BASIC_INFO_BY_ID_TYPE}
                AND ${SexTable.Columns.STANDARD_ABBREVIATION} = ?4
                ${ORDER_BY_FOR_ANIMAL_BASIC_INFO}"""

        private val QUALIFIER_FOR_ANIMAL_BASIC_INFO_BY_NAME get() =
            """${AnimalTable.Columns.NAME}
                LIKE '%' || ?2 || '%' ${Sql.ESCAPE_CLAUSE}"""

        private val SQL_SEARCH_ANIMAL_BASIC_INFO_BY_NAME get() =
            """${SQL_QUERY_ANIMAL_BASIC_INFO}
                WHERE ${QUALIFIER_TO_FILTER_SPECIAL_MARKER_ANIMALS}
                AND ${QUALIFIER_FOR_ANIMAL_BASIC_INFO_BY_SPECIES}
                AND ${QUALIFIER_FOR_ANIMAL_BASIC_INFO_BY_NAME}
                ${ORDER_BY_FOR_ANIMAL_BASIC_INFO}"""

        private val SQL_SEARCH_ANIMAL_BASIC_INFO_BY_NAME_AND_SEX_STANDARD get() =
            """${SQL_QUERY_ANIMAL_BASIC_INFO}
                WHERE ${QUALIFIER_TO_FILTER_SPECIAL_MARKER_ANIMALS}
                AND ${QUALIFIER_FOR_ANIMAL_BASIC_INFO_BY_SPECIES}
                AND ${QUALIFIER_FOR_ANIMAL_BASIC_INFO_BY_NAME}
                AND ${SexTable.Columns.STANDARD_ABBREVIATION} = ?3
                ${ORDER_BY_FOR_ANIMAL_BASIC_INFO}"""

        private val QUALIFIER_FOR_ANIMAL_BASIC_INFO_BY_ANIMAL_ID get() =
            """${AnimalTable.NAME}.${AnimalTable.Columns.ID} = ?"""

        private val SQL_QUERY_ANIMAL_BASIC_INFO_BY_ANIMAL_ID get() =
            """${SQL_QUERY_ANIMAL_BASIC_INFO}
                WHERE ${QUALIFIER_TO_FILTER_SPECIAL_MARKER_ANIMALS}
                AND ${QUALIFIER_FOR_ANIMAL_BASIC_INFO_BY_ANIMAL_ID}
                ${ORDER_BY_FOR_ANIMAL_BASIC_INFO}"""

        private val QUALIFIER_FOR_ANIMAL_BASIC_INFO_BY_EID get() =
            """${AnimalTable.NAME}.${AnimalTable.Columns.ID} IN (
                    SELECT ${AnimalIdInfoTable.Columns.ANIMAL_ID}
                    FROM ${AnimalIdInfoTable.NAME}
                    WHERE ${AnimalIdInfoTable.Columns.ID_TYPE_ID} = '${IdType.ID_TYPE_ID_EID_RAW}'
                    AND ${AnimalIdInfoTable.Columns.NUMBER} = ?
                )"""

        private val SQL_QUERY_ANIMAL_BASIC_INFO_BY_EID get() =
            """${SQL_QUERY_ANIMAL_BASIC_INFO}
                WHERE ${QUALIFIER_TO_FILTER_SPECIAL_MARKER_ANIMALS}
                AND ${QUALIFIER_FOR_ANIMAL_BASIC_INFO_BY_EID}
                ${ORDER_BY_FOR_ANIMAL_BASIC_INFO}"""

        private val SQL_QUERY_ANIMAL_LIFETIME get() =
            """SELECT
                ${AnimalTable.NAME}.${AnimalTable.Columns.BIRTH_DATE},
                ${AnimalTable.NAME}.${AnimalTable.Columns.DEATH_DATE},
                ${AnimalTable.NAME}.${AnimalTable.Columns.DEATH_REASON_ID},
                COALESCE(${DeathReasonTable.NAME}.${DeathReasonTable.Columns.REASON}, '${DeathReason.DEATH_REASON_MISSING}')
                    AS ${DeathReasonTable.Columns.REASON}
                FROM ${AnimalTable.NAME}
                LEFT OUTER JOIN ${DeathReasonTable.NAME}
                ON CASE WHEN ${AnimalTable.NAME}.${AnimalTable.Columns.DEATH_DATE} IS NOT NULL
                            AND ${AnimalTable.NAME}.${AnimalTable.Columns.DEATH_REASON_ID} IS NULL
                        THEN '${DeathReason.ID_DEATH_REASON_UNKNOWN_RAW}'
                        ELSE ${AnimalTable.NAME}.${AnimalTable.Columns.DEATH_REASON_ID}
                        END = ${DeathReasonTable.NAME}.${DeathReasonTable.Columns.ID}
                WHERE ${AnimalTable.NAME}.${AnimalTable.Columns.ID} = ?"""

        private val SQL_QUERY_ANIMAL_REARING get() =
            """SELECT
                ${AnimalTable.NAME}.${AnimalTable.Columns.BIRTH_ORDER},
                ${AnimalTable.NAME}.${AnimalTable.Columns.BIRTH_TYPE_ID},
                COALESCE(TABLE_BIRTH_TYPE.${BirthTypeTable.Columns.NAME}, '${BirthType.BIRTH_TYPE_MISSING}')
                    AS ${BirthTypeTable.Columns.NAME},
                ${AnimalTable.NAME}.${AnimalTable.Columns.REAR_TYPE_ID},
                COALESCE(TABLE_REAR_TYPE.${BirthTypeTable.Columns.NAME}, '${RearType.REAR_TYPE_MISSING}')
                    AS ${BirthTypeTable.Columns.REAR_TYPE_NAME}
                FROM ${AnimalTable.NAME}
                LEFT OUTER JOIN ${BirthTypeTable.NAME} AS TABLE_BIRTH_TYPE
                    ON CASE WHEN ${AnimalTable.NAME}.${AnimalTable.Columns.BIRTH_TYPE_ID} IS NOT NULL
                        THEN ${AnimalTable.NAME}.${AnimalTable.Columns.BIRTH_TYPE_ID}
                        ELSE '${BirthType.ID_UNKNOWN_RAW}'
                        END = TABLE_BIRTH_TYPE.${BirthTypeTable.Columns.ID}
                LEFT OUTER JOIN ${BirthTypeTable.NAME} AS TABLE_REAR_TYPE
                    ON CASE WHEN ${AnimalTable.NAME}.${AnimalTable.Columns.REAR_TYPE_ID} IS NOT NULL
                        THEN ${AnimalTable.NAME}.${AnimalTable.Columns.REAR_TYPE_ID}
                        ELSE '${RearType.ID_UNKNOWN_RAW}'
                        END = TABLE_REAR_TYPE.${BirthTypeTable.Columns.ID}
                WHERE ${AnimalTable.NAME}.${AnimalTable.Columns.ID} = ?"""

        private val SQL_QUERY_ANIMAL_EVALUATIONS get() = """
            SELECT
                ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.ID},
                ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.ANIMAL_ID},
                ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.ANIMAL_RANK},
                ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.NUMBER_RANKED},
                ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.EVAL_DATE},
                ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.EVAL_TIME},
                ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_ID_01} AS trait_1_id,
                ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_SCORE_01} AS trait_1_score,
                trait_1_name_table.${EvalTraitTable.Columns.NAME} AS trait_1_name,
                ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_ID_02} AS trait_2_id,
                ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_SCORE_02} AS trait_2_score,
                trait_2_name_table.${EvalTraitTable.Columns.NAME} AS trait_2_name,
                ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_ID_03} AS trait_3_id,
                ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_SCORE_03} AS trait_3_score,
                trait_3_name_table.${EvalTraitTable.Columns.NAME} AS trait_3_name,
                ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_ID_04} AS trait_4_id,
                ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_SCORE_04} AS trait_4_score,
                trait_4_name_table.${EvalTraitTable.Columns.NAME} AS trait_4_name,
                ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_ID_05} AS trait_5_id,
                ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_SCORE_05} AS trait_5_score,
                trait_5_name_table.${EvalTraitTable.Columns.NAME} AS trait_5_name,
                ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_ID_06} AS trait_6_id,
                ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_SCORE_06} AS trait_6_score,
                trait_6_name_table.${EvalTraitTable.Columns.NAME} AS trait_6_name,
                ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_ID_07} AS trait_7_id,
                ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_SCORE_07} AS trait_7_score,
                trait_7_name_table.${EvalTraitTable.Columns.NAME} AS trait_7_name,
                ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_ID_08} AS trait_8_id,
                ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_SCORE_08} AS trait_8_score,
                trait_8_name_table.${EvalTraitTable.Columns.NAME} AS trait_8_name,
                ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_ID_09} AS trait_9_id,
                ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_SCORE_09} AS trait_9_score,
                trait_9_name_table.${EvalTraitTable.Columns.NAME} AS trait_9_name,
                ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_ID_10} AS trait_10_id,
                ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_SCORE_10} AS trait_10_score,
                trait_10_name_table.${EvalTraitTable.Columns.NAME} AS trait_10_name, 
                ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_ID_11} AS trait_11_id,
                ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_SCORE_11} AS trait_11_score,
                trait_11_name_table.${EvalTraitTable.Columns.NAME} AS trait_11_name,
                ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_UNITS_ID_11} AS trait_11_units_id,
                trait_11_units_table.${UnitsTable.Columns.ABBREVIATION} AS trait_11_units_abbrev,
                ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_ID_12} AS trait_12_id,
                ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_SCORE_12} AS trait_12_score,
                trait_12_name_table.${EvalTraitTable.Columns.NAME} AS trait_12_name,
                ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_UNITS_ID_12} AS trait_12_units_id,
                trait_12_units_table.${UnitsTable.Columns.ABBREVIATION} AS trait_12_units_abbrev,
                ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_ID_13} AS trait_13_id,
                ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_SCORE_13} AS trait_13_score,
                trait_13_name_table.${EvalTraitTable.Columns.NAME} AS trait_13_name,
                ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_UNITS_ID_13} AS trait_13_units_id,
                trait_13_units_table.${UnitsTable.Columns.ABBREVIATION} AS trait_13_units_abbrev,
                ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_ID_14} AS trait_14_id,
                ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_SCORE_14} AS trait_14_score,
                trait_14_name_table.${EvalTraitTable.Columns.NAME} AS trait_14_name,
                ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_UNITS_ID_14} AS trait_14_units_id,
                trait_14_units_table.${UnitsTable.Columns.ABBREVIATION} AS trait_14_units_abbrev,
                ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_ID_15} AS trait_15_id,
                ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_SCORE_15} AS trait_15_score,
                trait_15_name_table.${EvalTraitTable.Columns.NAME} AS trait_15_name,
                ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_UNITS_ID_15} AS trait_15_units_id,
                trait_15_units_table.${UnitsTable.Columns.ABBREVIATION} AS trait_15_units_abbrev,
                ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_ID_16} AS trait_16_id,
                ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_SCORE_16} AS trait_16_option_id,
                trait_16_name_table.${EvalTraitTable.Columns.NAME} AS trait_16_name,
                trait_16_option_table.${CustomEvalTraitsTable.Columns.ITEM} AS trait_16_option_name,
                ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_ID_17} AS trait_17_id,
                ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_SCORE_17} AS trait_17_option_id,
                trait_17_name_table.${EvalTraitTable.Columns.NAME} AS trait_17_name,
                trait_17_option_table.${CustomEvalTraitsTable.Columns.ITEM} AS trait_17_option_name,
                ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_ID_18} AS trait_18_id,
                ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_SCORE_18} AS trait_18_option_id,
                trait_18_name_table.${EvalTraitTable.Columns.NAME} AS trait_18_name,
                trait_18_option_table.${CustomEvalTraitsTable.Columns.ITEM} AS trait_18_option_name,
                ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_ID_19} AS trait_19_id,
                ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_SCORE_19} AS trait_19_option_id,
                trait_19_name_table.${EvalTraitTable.Columns.NAME} AS trait_19_name,
                trait_19_option_table.${CustomEvalTraitsTable.Columns.ITEM} AS trait_19_option_name,
                ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_ID_20} AS trait_20_id,
                ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_SCORE_20} AS trait_20_option_id,
                trait_20_name_table.${EvalTraitTable.Columns.NAME} AS trait_20_name,
                trait_20_option_table.${CustomEvalTraitsTable.Columns.ITEM} AS trait_20_option_name
            FROM ${AnimalEvaluationTable.NAME}
            LEFT OUTER JOIN ${EvalTraitTable.NAME} AS trait_1_name_table
            ON ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_ID_01} = trait_1_name_table.${EvalTraitTable.Columns.ID}
            LEFT OUTER JOIN ${EvalTraitTable.NAME} AS trait_2_name_table
            ON ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_ID_02} = trait_2_name_table.${EvalTraitTable.Columns.ID}     
            LEFT OUTER JOIN ${EvalTraitTable.NAME} AS trait_3_name_table
            ON ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_ID_03} = trait_3_name_table.${EvalTraitTable.Columns.ID}     
            LEFT OUTER JOIN ${EvalTraitTable.NAME} AS trait_4_name_table
            ON ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_ID_04} = trait_4_name_table.${EvalTraitTable.Columns.ID}
            LEFT OUTER JOIN ${EvalTraitTable.NAME} AS trait_5_name_table
            ON ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_ID_05} = trait_5_name_table.${EvalTraitTable.Columns.ID}        
            LEFT OUTER JOIN ${EvalTraitTable.NAME} AS trait_6_name_table
            ON ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_ID_06} = trait_6_name_table.${EvalTraitTable.Columns.ID}        
            LEFT OUTER JOIN ${EvalTraitTable.NAME} AS trait_7_name_table
            ON ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_ID_07} = trait_7_name_table.${EvalTraitTable.Columns.ID}        
            LEFT OUTER JOIN ${EvalTraitTable.NAME} AS trait_8_name_table
            ON ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_ID_08} = trait_8_name_table.${EvalTraitTable.Columns.ID}        
            LEFT OUTER JOIN ${EvalTraitTable.NAME} AS trait_9_name_table
            ON ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_ID_09} = trait_9_name_table.${EvalTraitTable.Columns.ID}        
            LEFT OUTER JOIN ${EvalTraitTable.NAME} AS trait_10_name_table
            ON ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_ID_10} = trait_10_name_table.${EvalTraitTable.Columns.ID}        
            LEFT OUTER JOIN ${EvalTraitTable.NAME} AS trait_11_name_table
            ON ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_ID_11} = trait_11_name_table.${EvalTraitTable.Columns.ID}        
            LEFT OUTER JOIN ${UnitsTable.NAME} AS trait_11_units_table
            ON ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_UNITS_ID_11} = trait_11_units_table.${UnitsTable.Columns.ID}        
            LEFT OUTER JOIN ${EvalTraitTable.NAME} AS trait_12_name_table
            ON ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_ID_12} = trait_12_name_table.${EvalTraitTable.Columns.ID}       
            LEFT OUTER JOIN ${UnitsTable.NAME} AS trait_12_units_table
            ON ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_UNITS_ID_12} = trait_12_units_table.${UnitsTable.Columns.ID}       
            LEFT OUTER JOIN ${EvalTraitTable.NAME} AS trait_13_name_table
            ON ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_ID_13} = trait_13_name_table.${EvalTraitTable.Columns.ID}       
            LEFT OUTER JOIN ${UnitsTable.NAME} AS trait_13_units_table
            ON ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_UNITS_ID_13} = trait_13_units_table.${UnitsTable.Columns.ID}       
            LEFT OUTER JOIN ${EvalTraitTable.NAME} AS trait_14_name_table
            ON ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_ID_14} = trait_14_name_table.${EvalTraitTable.Columns.ID}       
            LEFT OUTER JOIN ${UnitsTable.NAME} AS trait_14_units_table
            ON ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_UNITS_ID_14} = trait_14_units_table.${UnitsTable.Columns.ID}       
            LEFT OUTER JOIN ${EvalTraitTable.NAME} AS trait_15_name_table
            ON ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_ID_15} = trait_15_name_table.${EvalTraitTable.Columns.ID}        
            LEFT OUTER JOIN ${UnitsTable.NAME} AS trait_15_units_table
            ON ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_UNITS_ID_15} = trait_15_units_table.${UnitsTable.Columns.ID}        
            LEFT OUTER JOIN ${EvalTraitTable.NAME} AS trait_16_name_table
            ON ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_ID_16} = trait_16_name_table.${EvalTraitTable.Columns.ID}               
            LEFT OUTER JOIN ${CustomEvalTraitsTable.NAME} AS trait_16_option_table
            ON ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_SCORE_16} = trait_16_option_table.${CustomEvalTraitsTable.Columns.ID}       
            LEFT OUTER JOIN ${EvalTraitTable.NAME} AS trait_17_name_table
            ON ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_ID_17} = trait_17_name_table.${EvalTraitTable.Columns.ID}        
            LEFT OUTER JOIN ${CustomEvalTraitsTable.NAME} AS trait_17_option_table
            ON ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_SCORE_17} = trait_17_option_table.${CustomEvalTraitsTable.Columns.ID}
            LEFT OUTER JOIN ${EvalTraitTable.NAME} AS trait_18_name_table
            ON ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_ID_18} = trait_18_name_table.${EvalTraitTable.Columns.ID}              
            LEFT OUTER JOIN ${CustomEvalTraitsTable.NAME} AS trait_18_option_table
            ON ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_SCORE_18} = trait_18_option_table.${CustomEvalTraitsTable.Columns.ID}        
            LEFT OUTER JOIN ${EvalTraitTable.NAME} AS trait_19_name_table
            ON ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_ID_19} = trait_19_name_table.${EvalTraitTable.Columns.ID}             
            LEFT OUTER JOIN ${CustomEvalTraitsTable.NAME} AS trait_19_option_table
            ON ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_SCORE_19} = trait_19_option_table.${CustomEvalTraitsTable.Columns.ID}       
            LEFT OUTER JOIN ${EvalTraitTable.NAME} AS trait_20_name_table
            ON ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_ID_20} = trait_20_name_table.${EvalTraitTable.Columns.ID}        
            LEFT OUTER JOIN ${CustomEvalTraitsTable.NAME} AS trait_20_option_table
            ON ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.TRAIT_SCORE_20} = trait_20_option_table.${CustomEvalTraitsTable.Columns.ID}        
            WHERE ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.ANIMAL_ID} = ?
            ORDER BY ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.EVAL_DATE} DESC, 
                ${AnimalEvaluationTable.NAME}.${AnimalEvaluationTable.Columns.EVAL_TIME} DESC"""

        private fun animalNameFromCursor(cursor: Cursor): AnimalName {
            return AnimalName(
                id = cursor.getEntityId(AnimalTable.Columns.ID),
                name = cursor.getString(AnimalTable.Columns.NAME)
            )
        }

        private fun animalBasicInfoFrom(cursor: Cursor): AnimalBasicInfo {
            val animalBasicInfo = AnimalBasicInfo(
                id = cursor.getEntityId(AnimalTable.Columns.ID),
                name = cursor.getString(AnimalTable.Columns.NAME),
                flockPrefix = cursor.getOptString(FlockPrefixTable.Columns.PREFIX),
                ownerName = when {
                    !cursor.isNull(CompanyTable.Columns.NAME) -> {
                        cursor.getString(CompanyTable.Columns.NAME)
                    }
                    !cursor.isNull(ContactTable.Columns.FULL_NAME_ALIAS) -> {
                        cursor.getString(ContactTable.Columns.FULL_NAME_ALIAS)
                    }
                    else -> null
                },
                speciesId = cursor.getEntityId(SpeciesTable.Columns.ID),
                speciesCommonName = cursor.getString(SpeciesTable.Columns.COMMON_NAME),
                breedId = cursor.getEntityId(BreedTable.Columns.ID),
                breedName = cursor.getString(BreedTable.Columns.NAME),
                breedAbbreviation = cursor.getString(BreedTable.Columns.ABBREVIATION),
                breedPercentage = cursor.getFloat(AnimalBreedTable.Columns.BREED_PERCENTAGE),
                sexId = cursor.getEntityId(SexTable.Columns.ID),
                sexName = cursor.getString(SexTable.Columns.NAME),
                sexAbbreviation = cursor.getString(SexTable.Columns.ABBREVIATION),
                sexStandardName = cursor.getString(SexTable.Columns.STANDARD),
                sexStandardAbbreviation = cursor.getString(SexTable.Columns.STANDARD_ABBREVIATION),
                birthDate = cursor.getOptLocalDate(AnimalTable.Columns.BIRTH_DATE),
                deathDate = cursor.getOptLocalDate(AnimalTable.Columns.DEATH_DATE),
                weanedDate = cursor.getOptLocalDate(AnimalTable.Columns.WEANED_DATE)
            )
            val ids = mutableListOf<IdInfo>().also {
                if (hasIdBasicInfoIn(cursor)) {
                    it.add(cursor.readItem(Cursor::readIdInfo))
                }
                while (cursor.moveToNext()) {
                    val animalId = cursor.getEntityId(
                        AnimalTable.Columns.ID
                    )
                    if (animalBasicInfo.id == animalId) {
                        if (hasIdBasicInfoIn(cursor)) {
                            it.add(cursor.readItem(Cursor::readIdInfo))
                        }
                    } else {
                        cursor.moveToPrevious()
                        break;
                    }
                }
            }
            return animalBasicInfo.copy(ids = ids)
        }

        private fun hasIdBasicInfoIn(cursor: Cursor): Boolean {
            return !cursor.isNull(AnimalIdInfoTable.Columns.ID) &&
                !cursor.isNull(AnimalIdInfoTable.Columns.NUMBER) &&
                !cursor.isNull(IdTypeTable.Columns.NAME) &&
                !cursor.isNull(IdColorTable.Columns.ABBREVIATION) &&
                !cursor.isNull(IdLocationTable.Columns.ABBREVIATION)
        }

        private fun animalWeightFromCursor(cursor: Cursor): AnimalWeight? {
            return AnimalWeight.from(
                animalId = cursor.getOptInt(AnimalLastEvaluationOfTrait.Columns.ANIMAL_ID),
                weight = cursor.getOptFloat(AnimalLastEvaluationOfTrait.Columns.TRAIT_VALUE),
                unitsId = cursor.getOptInt(AnimalLastEvaluationOfTrait.Columns.TRAIT_UNITS_ID),
                unitsName = cursor.getOptString(AnimalLastEvaluationOfTrait.Columns.TRAIT_UNITS_NAME),
                unitsAbbreviation = cursor.getOptString(AnimalLastEvaluationOfTrait.Columns.TRAIT_UNITS_ABBREV),
                weighedOn = cursor.getOptLocalDate(AnimalLastEvaluationOfTrait.Columns.TRAIT_EVAL_DATE)
            )
        }

        private fun animalNotesFromCursor(cursor: Cursor): List<AnimalNote> {

            val id = cursor.getEntityId(AnimalNoteTable.Columns.ID)
            val animalId = cursor.getInt(AnimalNoteTable.Columns.ANIMAL_ID)
            val noteText = cursor.getOptString(AnimalNoteTable.Columns.NOTE_TEXT)
            val predefinedNoteId = cursor.getOptEntityId(AnimalNoteTable.Columns.PREDEFINED_NOTE_ID)
            val predefinedNoteText = cursor.getOptString(PredefinedNoteTable.Columns.NOTE_TEXT)
            val noteDate = cursor.getLocalDate(AnimalNoteTable.Columns.NOTE_DATE)
            val noteTime = cursor.getLocalTime(AnimalNoteTable.Columns.NOTE_TIME)

            val individualNotes = mutableListOf<AnimalNote>()

            if (!noteText.isNullOrBlank()) {
                individualNotes.add(
                    AnimalNote(
                        id = id,
                        animalId = animalId,
                        noteText = noteText,
                        noteDate = noteDate,
                        noteTime = noteTime,
                        predefinedNoteId = null
                    )
                )
            }
            if (predefinedNoteId != null && predefinedNoteText != null) {
                individualNotes.add(
                    AnimalNote(
                        id = id,
                        animalId = animalId,
                        noteText = predefinedNoteText,
                        noteDate = noteDate,
                        noteTime = noteTime,
                        predefinedNoteId = predefinedNoteId
                    )
                )
            }

            return individualNotes
        }

        private fun animalDrugEventFromCursor(cursor: Cursor): AnimalDrugEvent {
            return AnimalDrugEvent(
                id = cursor.getEntityId(AnimalDrugTable.Columns.ID),
                animalId = cursor.getInt(AnimalDrugTable.Columns.ANIMAL_ID),
                drugId = cursor.getInt(AnimalDrugTable.Columns.DRUG_LOT_ID),
                eventDate = cursor.getLocalDate(AnimalDrugTable.Columns.DATE_ON),
                eventTime = cursor.getOptLocalTime(AnimalDrugTable.Columns.TIME_ON),
                tradeDrugName = cursor.getString(DrugTable.Columns.TRADE_NAME),
                drugLot = cursor.getString(DrugLotTable.Columns.LOT)
            )
        }

        private fun tissueSampleEventFromCursor(cursor: Cursor): TissueSampleEvent {
            return TissueSampleEvent(
                id = cursor.getEntityId(AnimalTissueSampleTakenTable.Columns.ID),
                animalId = cursor.getInt(AnimalTissueSampleTakenTable.Columns.ANIMAL_ID),
                tissueSampleTypeId = cursor.getInt(AnimalTissueSampleTakenTable.Columns.SAMPLE_TYPE_ID),
                tissueSampleName = cursor.getString(TissueSampleTypeTable.Columns.NAME),
                eventDate = cursor.getLocalDate(AnimalTissueSampleTakenTable.Columns.SAMPLE_DATE),
                eventTime = cursor.getOptLocalTime(AnimalTissueSampleTakenTable.Columns.SAMPLE_TIME)
            )
        }

        private fun tissueTestEventFromCursor(cursor: Cursor): TissueTestEvent {
            return TissueTestEvent(
                id = cursor.getEntityId(AnimalTissueTestRequestTable.Columns.ID),
                animalId = cursor.getInt(AnimalTissueSampleTakenTable.Columns.ANIMAL_ID),
                tissueTestTypeId = cursor.getInt(AnimalTissueTestRequestTable.Columns.TEST_ID),
                tissueTestName = cursor.getString(TissueTestTable.Columns.NAME),
                labCompanyName = cursor.getString(CompanyTable.Columns.NAME),
                eventDate = cursor.getLocalDate(AnimalTissueSampleTakenTable.Columns.SAMPLE_DATE),
                eventTime = cursor.getOptLocalTime(AnimalTissueSampleTakenTable.Columns.SAMPLE_TIME),
                labAscensionId = cursor.getOptString(AnimalTissueTestRequestTable.Columns.LABORATORY_ACCESSION_ID),
                tissueTestResults = cursor.getOptString(AnimalTissueTestRequestTable.Columns.TEST_RESULTS),
                tissueTestResultsDate = cursor.getOptLocalDate(AnimalTissueTestRequestTable.Columns.TEST_RESULTS_DATE)
            )
        }

        private fun animalLifetimeFromCursor(cursor: Cursor): AnimalLifetime {
            val deathDate = cursor.getOptLocalDate(AnimalTable.Columns.DEATH_DATE)
            return AnimalLifetime(
                birthDate = cursor.getOptLocalDate(AnimalTable.Columns.BIRTH_DATE),
                death = when (deathDate) {
                    null -> null
                    else -> AnimalDeath(
                        date = deathDate,
                        reasonId = cursor.getOptInt(AnimalTable.Columns.DEATH_REASON_ID),
                        reason = cursor.getOptString(DeathReasonTable.Columns.REASON)
                    )
                }
            )
        }

        private fun animalRearingFromCursor(cursor: Cursor): AnimalRearing {
            return AnimalRearing(
                birthOrder = cursor.getOptInt(AnimalTable.Columns.BIRTH_ORDER) ?: 0,
                birthTypeId = cursor.getInt(AnimalTable.Columns.BIRTH_TYPE_ID),
                birthType = cursor.getString(BirthTypeTable.Columns.NAME),
                rearTypeId = cursor.getOptInt(AnimalTable.Columns.REAR_TYPE_ID),
                rearType = cursor.getOptString(BirthTypeTable.Columns.REAR_TYPE_NAME)
            )
        }

        private fun animalParentageFromCursor(cursor: Cursor): AnimalParentage {
            return AnimalParentage(
                sireInfo = ParentInfo.from(
                    animalId = cursor.getOptEntityId(AnimalTable.Columns.SIRE_ID),
                    name = cursor.getOptString(Column.Nullable("_SIRE_${AnimalTable.Columns.NAME}")),
                    ownerType = cursor.getOptInt(Column.Nullable("_SIRE_${OwnerUnion.Columns.TYPE}"))?.let {
                        Owner.Type.fromCode(it)
                    },
                    ownerId = cursor.getOptInt(Column.Nullable("_SIRE_${OwnerUnion.Columns.ID}")),
                    ownerName = cursor.getOptString(Column.Nullable("_SIRE_${OwnerUnion.Columns.NAME}")),
                    flockPrefixId = cursor.getOptInt(Column.Nullable("_SIRE_${FlockPrefixTable.Columns.ID}")),
                    flockPrefix = cursor.getOptString(Column.Nullable("_SIRE_${FlockPrefixTable.Columns.PREFIX}"))
                ),
                damInfo = ParentInfo.from(
                    animalId = cursor.getOptEntityId(AnimalTable.Columns.DAM_ID),
                    name = cursor.getOptString(Column.Nullable("_DAM_${AnimalTable.Columns.NAME}")),
                    ownerType = cursor.getOptInt(Column.Nullable("_DAM_${OwnerUnion.Columns.TYPE}"))?.let {
                        Owner.Type.fromCode(it)
                    },
                    ownerId = cursor.getOptInt(Column.Nullable("_DAM_${OwnerUnion.Columns.ID}")),
                    ownerName = cursor.getOptString(Column.Nullable("_DAM_${OwnerUnion.Columns.NAME}")),
                    flockPrefixId = cursor.getOptInt(Column.Nullable("_DAM_${FlockPrefixTable.Columns.ID}")),
                    flockPrefix = cursor.getOptString(Column.Nullable("_DAM_${FlockPrefixTable.Columns.PREFIX}"))
                )
            )
        }

        private fun animalBreedersFromCursor(cursor: Cursor): AnimalBreeders {
            return AnimalBreeders(
                animalBreederInfo = BreederInfo.from(
                    animalId = cursor.getOptEntityId(AnimalTable.Columns.ID),
                    breederId = cursor.getOptEntityId(AnimalRegistrationTable.Columns.BREEDER_ID),
                    breederType = Breeder.Type.fromCode(cursor.getInt(AnimalRegistrationTable.Columns.BREEDER_TYPE_ID)),
                    breederName = cursor.getString(AnimalRegistrationTable.Columns.BREEDER_NAME)
                ),
                sireBreederInfo = BreederInfo.from(
                    animalId = cursor.getOptEntityId(AnimalTable.Columns.SIRE_ID),
                    breederId = cursor.getOptEntityId(Column.Nullable("_SIRE_${AnimalRegistrationTable.Columns.BREEDER_ID}")),
                    breederType = cursor.getOptInt(Column.Nullable("_SIRE_${AnimalRegistrationTable.Columns.BREEDER_TYPE_ID}"))?.let {
                        Breeder.Type.fromCode(it)
                    },
                    breederName = cursor.getOptString(Column.Nullable("_SIRE_${AnimalRegistrationTable.Columns.BREEDER_NAME}")),
                ),
                damBreederInfo = BreederInfo.from(
                    animalId = cursor.getOptEntityId(AnimalTable.Columns.DAM_ID),
                    breederId = cursor.getOptEntityId(Column.Nullable("_DAM_${AnimalRegistrationTable.Columns.BREEDER_ID}")),
                    breederType = cursor.getOptInt(Column.Nullable("_DAM_${AnimalRegistrationTable.Columns.BREEDER_TYPE_ID}"))?.let {
                        Breeder.Type.fromCode(it)
                    },
                    breederName = cursor.getOptString(Column.Nullable("_DAM_${AnimalRegistrationTable.Columns.BREEDER_NAME}")),
                )
            )
        }

        private fun traitIdColumnFor(traitNumber: Int): Column.Nullable {
            return Column.Nullable("trait_${traitNumber}_id")
        }

        private fun traitNameColumnFor(traitNumber: Int): Column.Nullable {
            return Column.Nullable("trait_${traitNumber}_name")
        }

        private fun traitScoreColumnFor(traitNumber: Int): Column.Nullable {
            return Column.Nullable("trait_${traitNumber}_score")
        }

        private fun traitUnitsIdColumnFor(traitNumber: Int): Column.Nullable {
            return Column.Nullable("trait_${traitNumber}_units_id")
        }

        private fun traitUnitsAbbrColumnFor(traitNumber: Int): Column.Nullable {
            return Column.Nullable("trait_${traitNumber}_units_abbrev")
        }

        private fun traitOptionIdColumnFor(traitNumber: Int): Column.Nullable {
            return Column.Nullable("trait_${traitNumber}_option_id")
        }

        private fun traitOptionNameColumnFor(traitNumber: Int): Column.Nullable {
            return Column.Nullable("trait_${traitNumber}_option_name")
        }

        private fun animalEvaluationFromCursor(cursor: Cursor): AnimalEvaluation {

            val entries = mutableListOf<AnimalEvaluation.Entry?>()

            fun captureScoredTraitFor(traitNumber: Int, cursor: Cursor): AnimalEvaluation.ScoreEntry? {
                val traitId = cursor.getOptEntityId(traitIdColumnFor(traitNumber))
                    .takeIf { it?.isValid == true } ?: return null
                val traitName = cursor.getOptString(traitNameColumnFor(traitNumber))
                    ?: "Unknown Trait: $traitId"
                val traitScore = cursor.getOptInt(traitScoreColumnFor(traitNumber)) ?: 0
                return AnimalEvaluation.ScoreEntry(traitId, traitName, traitScore)
            }

            fun captureUnitsTraitFor(traitNumber: Int, cursor: Cursor): AnimalEvaluation.UnitsEntry? {
                val traitId = cursor.getOptEntityId(traitIdColumnFor(traitNumber))
                    .takeIf { it?.isValid == true } ?: return null
                val traitName = cursor.getOptString(traitNameColumnFor(traitNumber))
                    ?: "Unknown Trait: $traitId"
                val traitScore = cursor.getOptFloat(traitScoreColumnFor(traitNumber))
                val unitsId = cursor.getOptInt(traitUnitsIdColumnFor(traitNumber))
                val unitsAbbr = cursor.getOptString(traitUnitsAbbrColumnFor(traitNumber))
                    ?: "???:$unitsId"
                return if (traitScore != null && unitsId != null) {
                    AnimalEvaluation.UnitsEntry(traitId, traitName, traitScore, unitsId, unitsAbbr)
                } else { null }
            }

            fun captureOptionTraitFor(traitNumber: Int, cursor: Cursor): AnimalEvaluation.OptionEntry? {
                val traitId = cursor.getOptEntityId(traitIdColumnFor(traitNumber))
                    .takeIf { it?.isValid == true } ?: return null
                val traitName = cursor.getOptString(traitNameColumnFor(traitNumber))
                    ?: "Unknown Trait: $traitId"
                val optionId = cursor.getOptInt(traitOptionIdColumnFor(traitNumber))
                val optionName = cursor.getOptString(traitOptionNameColumnFor(traitNumber))
                    ?: "Unknown Option: $optionId"
                return if (optionId != null) {
                    AnimalEvaluation.OptionEntry(traitId, traitName, optionId, optionName)
                } else { null }
            }

            for (traitNumber: Int in 1..10) {
                entries.add(captureScoredTraitFor(traitNumber, cursor))
            }
            for (traitNumber: Int in 11..15) {
                entries.add(captureUnitsTraitFor(traitNumber, cursor))
            }
            for (traitNumber: Int in 16..20) {
                entries.add(captureOptionTraitFor(traitNumber, cursor))
            }

            return AnimalEvaluation(
                id = cursor.getEntityId(AnimalEvaluationTable.Columns.ID),
                animalId = cursor.getInt(AnimalEvaluationTable.Columns.ANIMAL_ID),
                traits = entries.filterNotNull(),
                animalRank = AnimalEvaluation.Rank.from(
                    rank = cursor.getOptInt(AnimalEvaluationTable.Columns.ANIMAL_RANK),
                    numberRanked = cursor.getOptInt(AnimalEvaluationTable.Columns.NUMBER_RANKED),
                ),
                evalDate = cursor.getLocalDate(AnimalEvaluationTable.Columns.EVAL_DATE),
                evalTime = cursor.getLocalTime(AnimalEvaluationTable.Columns.EVAL_TIME)
            )
        }

        private fun animalAlertFromCursor(cursor: Cursor): AnimalAlert {
            val id = cursor.getEntityId(AnimalAlertTable.Columns.ID)
            val animalId = cursor.getInt(AnimalAlertTable.Columns.ANIMAL_ID)
            val alertDate = cursor.getLocalDate(AnimalAlertTable.Columns.ALERT_DATE)
            val alertTime = cursor.getLocalTime(AnimalAlertTable.Columns.ALERT_TIME)
            val alertContent = cursor.getString(AnimalAlertTable.Columns.ALERT_CONTENT)
            return when (val alertTypeId = cursor.getEntityId(AnimalAlertTable.Columns.ALERT_TYPE_ID)) {
                AnimalAlert.Type.USER_DEFINED.typeId -> {
                    UserDefinedAlert(
                        id = id,
                        animalId = animalId,
                        eventDate = alertDate,
                        eventTime = alertTime,
                        content = alertContent
                    )
                }
                AnimalAlert.Type.DRUG_WITHDRAWAL.typeId -> {
                    DrugWithdrawalAlert(
                        id = id,
                        animalId = animalId,
                        eventDate = alertDate,
                        eventTime = alertTime,
                        drugWithdrawal = Json.decodeFromString(alertContent)
                    )
                }
                AnimalAlert.Type.EVALUATION_SUMMARY.typeId -> {
                    EvaluationSummaryAlert(
                        id = id,
                        animalId = animalId,
                        eventDate = alertDate,
                        eventTime = alertTime,
                        evaluationSummary = Json.decodeFromString(alertContent)
                    )
                }
                else -> throw IllegalStateException("Unknown alert type id: $alertTypeId")
            }
        }

        private fun weanFromDamIdFromCursor(cursor: Cursor): EntityId? {
            return cursor.getOptEntityId(AnimalTable.Columns.DAM_ID)
        }

        private fun femaleBreedingFromCursor(cursor: Cursor): FemaleBreedingHistoryEntry {
            return FemaleBreedingHistoryEntry(
                femaleBreedingId = cursor.getEntityId(AnimalFemaleBreedingTable.Columns.ID),
                animalId = cursor.getEntityId(AnimalFemaleBreedingTable.Columns.ANIMAL_ID),
                birthingNotes = cursor.getOptString(AnimalFemaleBreedingTable.Columns.BIRTHING_NOTES),
                eventDate = cursor.getLocalDate(Column.NotNull("EVENT_DATE")),
                eventTime = cursor.getLocalTime(Column.NotNull("EVENT_TIME")),
            )
        }

        private fun animalBreedFromCursor(cursor: Cursor): AnimalBreed {
            return AnimalBreed(
                id = cursor.getEntityId(AnimalBreedTable.Columns.ID),
                animalId = cursor.getEntityId(AnimalBreedTable.Columns.ANIMAL_ID),
                breedId = cursor.getEntityId(AnimalBreedTable.Columns.BREED_ID),
                breedName = cursor.getString(BreedTable.Columns.NAME),
                breedAbbreviation = cursor.getString(BreedTable.Columns.ABBREVIATION),
                percentage = cursor.getFloat(AnimalBreedTable.Columns.BREED_PERCENTAGE)
            )
        }

        private fun animalFemaleBreedingFromCursor(cursor: Cursor): FemaleBreeding {
            return FemaleBreeding(
                id = cursor.getEntityId(AnimalFemaleBreedingTable.Columns.ID),
                animalId = cursor.getEntityId(COLUMN_FEMALE_BREEDING_ANIMAL_ID),
                birthingDate = cursor.getOptLocalDate(AnimalFemaleBreedingTable.Columns.BIRTHING_DATE),
                birthingTime = cursor.getOptLocalTime(AnimalFemaleBreedingTable.Columns.BIRTHING_TIME),
                birthingNotes = cursor.getOptString(AnimalFemaleBreedingTable.Columns.BIRTHING_NOTES) ?: "",
                numberOfAnimalsBorn = cursor.getInt(AnimalFemaleBreedingTable.Columns.NUMBER_ANIMALS_BORN),
                numberOfAnimalsWeaned = cursor.getInt(AnimalFemaleBreedingTable.Columns.NUMBER_ANIMALS_WEANED),
                gestationLength = cursor.getOptInt(AnimalFemaleBreedingTable.Columns.GESTATION_LENGTH),
                maleBreeding = cursor.takeIf { !it.isNull(AnimalMaleBreedingTable.Columns.ID) }?.let {
                    MaleBreeding(
                        id = cursor.getEntityId(AnimalMaleBreedingTable.Columns.ID),
                        animalId = cursor.getEntityId(COLUMN_MALE_BREEDING_ANIMAL_ID),
                        dateIn = cursor.getLocalDate(AnimalMaleBreedingTable.Columns.DATE_IN),
                        timeIn = cursor.getLocalTime(AnimalMaleBreedingTable.Columns.TIME_IN),
                        dateOut = cursor.getOptLocalDate(AnimalMaleBreedingTable.Columns.DATE_OUT),
                        timeOut = cursor.getOptLocalTime(AnimalMaleBreedingTable.Columns.TIME_OUT),
                        serviceType = cursor.readServiceType()
                    )
                }
            )
        }

        private fun animalMaleBreedingFromCursor(cursor: Cursor): MaleBreeding {
            return MaleBreeding(
                id = cursor.getEntityId(AnimalMaleBreedingTable.Columns.ID),
                animalId = cursor.getEntityId(AnimalMaleBreedingTable.Columns.ANIMAL_ID),
                dateIn = cursor.getLocalDate(AnimalMaleBreedingTable.Columns.DATE_IN),
                timeIn = cursor.getLocalTime(AnimalMaleBreedingTable.Columns.TIME_IN),
                dateOut = cursor.getOptLocalDate(AnimalMaleBreedingTable.Columns.DATE_OUT),
                timeOut = cursor.getOptLocalTime(AnimalMaleBreedingTable.Columns.TIME_OUT),
                serviceType = cursor.readServiceType()
            )
        }
    }
}
