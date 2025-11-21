package com.weyr_associates.animaltrakkerfarmmobile.app.animal.breeding

import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.input.IdEntry
import com.weyr_associates.animaltrakkerfarmmobile.app.core.ErrorReport
import com.weyr_associates.animaltrakkerfarmmobile.app.core.Result
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.AnimalRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.FlockPrefixRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.SexRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseHandler
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalBreed
import com.weyr_associates.animaltrakkerfarmmobile.model.BirthType
import com.weyr_associates.animaltrakkerfarmmobile.model.Breed
import com.weyr_associates.animaltrakkerfarmmobile.model.BreedPart
import com.weyr_associates.animaltrakkerfarmmobile.model.Codon
import com.weyr_associates.animaltrakkerfarmmobile.model.CodonCalculation
import com.weyr_associates.animaltrakkerfarmmobile.model.Company
import com.weyr_associates.animaltrakkerfarmmobile.model.DeathReason
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.FemaleBreeding
import com.weyr_associates.animaltrakkerfarmmobile.model.FlockPrefix
import com.weyr_associates.animaltrakkerfarmmobile.model.GeneticCharacteristic.CalculationMethod
import com.weyr_associates.animaltrakkerfarmmobile.model.IdType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

sealed interface AddOffspringError

data class BreedPercentageError(
    val message: String,
    val breeding: List<AnimalBreed>
) : AddOffspringError

data class AddOffspringFatalError(
    val errorReport: ErrorReport
) : AddOffspringError

class AddOffspring(
    private val databaseHandler: DatabaseHandler,
    private val animalRepository: AnimalRepository,
    private val sexRepository: SexRepository,
    private val flockPrefixRepository: FlockPrefixRepository,
    private val loadActiveDefaults: LoadActiveDefaultSettings
) {
    operator fun invoke(
        damId: EntityId,
        sireId: EntityId,
        sexId: EntityId,
        sexAbbreviation: String,
        rearTypeId: EntityId,
        birthWeight: Float?,
        birthWeightUnitsId: EntityId?,
        lambEaseId: EntityId,
        suckReflexId: EntityId,
        coatColorId: EntityId,
        isStillborn: Boolean,
        shouldMarkDam: Boolean,
        animalIds: List<IdEntry>,
        timeStamp: LocalDateTime
    ): Result<Unit, AddOffspringError> {
        with(databaseHandler.writableDatabase) {
            try {

                beginTransaction()

                val birthDate = timeStamp.toLocalDate()
                val birthTime = timeStamp.toLocalTime()
                val defaultSettings = loadActiveDefaults()
                val flockPrefixId = defaultSettings.flockPrefixId
                val defaultFlockPrefixId = defaultSettings.flockPrefixId
                val registryCompanyId = defaultSettings.registryCompanyId ?: Company.ID_GENERIC
                val numberDigitsForFarmTag = defaultSettings.farmIdNumberDigitsFromEid
                val damOwner = requireNotNull(animalRepository.queryAnimalCurrentOwnership(damId))
                val damPremiseId = animalRepository.queryAnimalCurrentPremise(damId)
                val previousOffspring = animalRepository.idsOfOffspringBornInYear(damId, birthDate.year)
                val birthOrder = previousOffspring.count() + 1
                val birthTypeId = BirthType.mapOffspringCountToBirthType(birthOrder)
                val breeding = when (val breedStatisticsResult = resolveBreedStatistics(damId, sireId)) {
                    is Result.Failure -> return Result.Failure(breedStatisticsResult.error)
                    is Result.Success -> breedStatisticsResult.data
                }
                val animalName = resolveOffspringName(
                    damId,
                    registryCompanyId,
                    isStillborn,
                    sexId,
                    animalIds,
                    numberDigitsForFarmTag,
                    timeStamp
                )
                val offspringAnimalId = animalRepository.addAnimal(
                    animalName,
                    breeding,
                    sexId,
                    damId,
                    sireId,
                    birthDate,
                    birthTime,
                    birthOrder,
                    birthTypeId,
                    rearTypeId,
                    damOwner.ownerId,
                    damOwner.ownerType,
                    damPremiseId.takeIf { it != EntityId.UNKNOWN }
                        ?: defaultSettings.ownerPremiseId,
                    requireNotNull(defaultSettings.breederId),
                    requireNotNull(defaultSettings.breederType),
                    birthWeight,
                    birthWeightUnitsId,
                    timeStamp
                )
                if (previousOffspring.isNotEmpty()) {
                    animalRepository.updateBirthType(previousOffspring, birthTypeId)
                }
                calculateCodon136(sireId, damId).let { codonValueId ->
                    animalRepository.addCodonCharacteristicForAnimal(
                        offspringAnimalId,
                        Codon.CODE_136,
                        codonValueId,
                        CalculationMethod.ID_PEDIGREE,
                        timeStamp
                    )
                }
                calculateCodon141(sireId, damId).let { codonValueId ->
                    animalRepository.addCodonCharacteristicForAnimal(
                        offspringAnimalId,
                        Codon.CODE_141,
                        codonValueId,
                        CalculationMethod.ID_PEDIGREE,
                        timeStamp
                    )
                }
                calculateCodon154(sireId, damId).let { codonValueId ->
                    animalRepository.addCodonCharacteristicForAnimal(
                        offspringAnimalId,
                        Codon.CODE_154,
                        codonValueId,
                        CalculationMethod.ID_PEDIGREE,
                        timeStamp
                    )
                }
                calculateCodon171(sireId, damId).let { codonValueId ->
                    animalRepository.addCodonCharacteristicForAnimal(
                        offspringAnimalId,
                        Codon.CODE_171,
                        codonValueId,
                        CalculationMethod.ID_PEDIGREE,
                        timeStamp
                    )
                }
                animalRepository.addCoatColorCharacteristicForAnimal(
                    offspringAnimalId,
                    coatColorId,
                    CalculationMethod.ID_OBSERVATION,
                    timeStamp
                )
                if (!isStillborn) {
                    animalIds.forEach { idEntry ->
                        animalRepository.addIdToAnimal(
                            offspringAnimalId,
                            idEntry.type.id,
                            idEntry.color.id,
                            idEntry.location.id,
                            idEntry.number,
                            idEntry.isOfficial,
                            timeStamp
                        )
                    }
                    if (shouldMarkDam) {
                        val paintTags = animalIds.filter { it.type.id == IdType.ID_TYPE_ID_PAINT }
                        if (paintTags.isNotEmpty()) {
                            val damIds = animalRepository.queryAnimalIds(damId)
                            if (damIds.none { it.type.id == IdType.ID_TYPE_ID_PAINT }) {
                                paintTags.forEach { idEntry ->
                                    animalRepository.addIdToAnimal(
                                        damId,
                                        idEntry.type.id,
                                        idEntry.color.id,
                                        idEntry.location.id,
                                        idEntry.number,
                                        idEntry.isOfficial,
                                        timeStamp
                                    )
                                }
                            }
                        }
                    }
                } else {
                    animalRepository.markAnimalDeceased(
                        offspringAnimalId,
                        DeathReason.ID_DEATH_REASON_STILLBORN,
                        timeStamp.toLocalDate(),
                        timeStamp
                    )
                }
                var femaleBreedingRecord = animalRepository.queryFemaleBreedingForBirthdate(damId, birthDate)
                val femaleBreedingRecordId = if (femaleBreedingRecord != null) {
                    if (femaleBreedingRecord.birthingDate == null) {
                        animalRepository.updateFemaleBreedingRecordBirthingDate(
                            femaleBreedingRecord.id, birthDate, birthTime, timeStamp
                        )
                    }
                    if (femaleBreedingRecord.gestationLength == null) {
                        val maleDateIn = femaleBreedingRecord.maleBreeding?.dateIn
                        val gestationLength = if (maleDateIn != null) {
                            ChronoUnit.DAYS.between(maleDateIn, birthDate).toInt()
                        } else {
                            requireNotNull(animalRepository.queryAnimalSpecies(damId))
                                .typicalGestationLengthDays
                        }
                        animalRepository.updateFemaleBreedingRecordGestationLength(
                            femaleBreedingRecord.id, gestationLength, timeStamp
                        )
                    }
                    animalRepository.incrementNumberOfAnimalsBorn(femaleBreedingRecord.id, timeStamp)
                    femaleBreedingRecord.id
                } else {
                    animalRepository.addFemaleBreedingRecordForBirthdate(damId, birthDate, birthTime)
                }
                femaleBreedingRecord = animalRepository.queryFemaleBreeding(femaleBreedingRecordId)
                if (femaleBreedingRecord != null) {
                    val sexAbbrForNotes = if (isStillborn) "S" else sexAbbreviation
                    animalRepository.updateBirthingNotesForFemaleBreedingRecord(
                        femaleBreedingRecord.id, "${femaleBreedingRecord.birthingNotes}${sexAbbrForNotes}", timeStamp
                    )
                }
                val offspringFlockPrefix = resolveOffspringFlockPrefix(
                    damId, registryCompanyId, birthDate, femaleBreedingRecord, defaultFlockPrefixId
                )
                if (offspringFlockPrefix != null) {
                    animalRepository.assignAnimalToFlock(
                        offspringAnimalId,
                        offspringFlockPrefix.id,
                        offspringFlockPrefix.registryCompanyId,
                        timeStamp
                    )
                }
                animalRepository.recordBirthEvaluation(
                    offspringAnimalId,
                    lambEaseId,
                    suckReflexId,
                    birthWeight,
                    birthWeightUnitsId,
                    timeStamp
                )
                setTransactionSuccessful()
            } catch(ex: Exception) {
                return Result.Failure(
                    AddOffspringFatalError(
                        ErrorReport(
                            action = "Add Offspring",
                            summary = "Use case execution threw exception",
                            error = ex
                        )
                    )
                )
            }
            finally {
                databaseHandler.writableDatabase.endTransaction()
            }
        }
        return Result.Success(Unit)
    }

    private fun resolveOffspringName(
        damId: EntityId,
        registryCompanyId: EntityId?,
        isStillborn: Boolean,
        sexId: EntityId,
        animalIds: List<IdEntry>,
        numberDigitsForFarmTag: Int,
        timeStamp: LocalDateTime
    ): String {
        val damName = animalRepository.queryAnimalName(damId)?.name ?: "Unknown"
        val sex = requireNotNull(sexRepository.querySexById(sexId))
        val flockPrefix = registryCompanyId?.let {
            animalRepository.queryAnimalFlockPrefix(damId, registryCompanyId)
        } ?: ""
        val year = timeStamp.year
        val yearAndFlockPrefix = if (flockPrefix.isNotEmpty()) "${year}-${flockPrefix}" else "$year"
        return if (isStillborn) {
            val previousStillborns = animalRepository.numberOfStillbornsForDamInYear(damId, sexId, year)
            "$yearAndFlockPrefix ${damName}-Stillborn-${sex.name}-${previousStillborns + 1}"
        } else {
            val tagNumberForName = animalIds.idNumberForDefaultName()
            val tagNumberPartForName = if (0 < numberDigitsForFarmTag)
                tagNumberForName.take(numberDigitsForFarmTag) else tagNumberForName
            "$yearAndFlockPrefix ${damName}-${sex.name}-${tagNumberPartForName}"
        }
    }

    private fun resolveBreedStatistics(damId: EntityId, sireId: EntityId): Result<List<BreedPart>, AddOffspringError> {
        val sireBreeding = animalRepository.queryAnimalBreedInfo(sireId)
        val damBreeding = animalRepository.queryAnimalBreedInfo(damId)
        val sirePercentageTotal = sireBreeding.sumOf { it.percentage.toDouble() }.toFloat()
        val damPercentageTotal = damBreeding.sumOf { it.percentage.toDouble() }.toFloat()

        if (100.0f < sirePercentageTotal) {
            return Result.Failure(
                BreedPercentageError(
                    message = "Sire breed parts total more than 100%",
                    breeding = sireBreeding
                )
            )
        }

        if (100.0f < damPercentageTotal) {
            return Result.Failure(
                BreedPercentageError(
                    "Dam breed parts total more than 100%",
                    breeding = damBreeding
                )
            )
        }

        val relevantBreeds = mutableSetOf<EntityId>()
            .apply { addAll(sireBreeding.map { it.breedId }) }
            .apply { addAll(damBreeding.map { it.breedId })}

        val breeding = relevantBreeds.map { breedId ->
            val sirePercentage = (sireBreeding.filter { it.breedId == breedId }
                .sumOf { it.percentage.toDouble() } / 2.0).toFloat()
            val damPercentage = (damBreeding.filter { it.breedId == breedId }
                .sumOf { it.percentage.toDouble() } / 2.0).toFloat()
            val combinedPercentage = sirePercentage + damPercentage
            BreedPart(
                breedId,
                combinedPercentage
            )
        }.toMutableList()

        val breedingPercentageTotal = breeding.sumOf { it.percentage.toDouble() }.toFloat()

        if (breedingPercentageTotal < 100.0f) {
            breeding.add(
                BreedPart(
                    breedId = Breed.ID_UNKNOWN_BREED,
                    percentage = (100.0 - breedingPercentageTotal.toDouble()).toFloat()
                )
            )
        }

        return Result.Success(breeding.toList())
    }

    private fun resolveOffspringFlockPrefix(
        damId: EntityId,
        registryCompanyId: EntityId,
        birthDate: LocalDate,
        femaleBreedingRecord: FemaleBreeding?,
        defaultFlockPrefixId: EntityId?
    ): FlockPrefix? {
        return if (Company.registryCompanyUsesBreedingDateToDetermineFlockPrefix(registryCompanyId)) {
            resolveOffspringFlockPrefixByOwnershipOnDate(
                damId, registryCompanyId, femaleBreedingRecord?.maleBreeding?.dateIn ?: birthDate
            )
        } else {
            resolveOffspringFlockPrefixByOwnershipOnDate(
                damId, registryCompanyId, birthDate
            )
        } ?: resolveOffspringFlockPrefixByDefaults(defaultFlockPrefixId)
    }

    private fun resolveOffspringFlockPrefixByOwnershipOnDate(
        damId: EntityId,
        registryCompanyId: EntityId,
        ownershipDate: LocalDate
    ): FlockPrefix? {
        val ownership = animalRepository.queryAnimalOwnershipOnDate(damId, ownershipDate)
        return ownership?.let {
            flockPrefixRepository.queryFlockPrefixByOwner(
                it.ownerId, it.ownerType, registryCompanyId
            )
        }
    }

    private fun resolveOffspringFlockPrefixByDefaults(defaultFlockPrefixId: EntityId?): FlockPrefix? {
        return defaultFlockPrefixId?.let { flockPrefixRepository.queryFlockPrefixById(it) }
    }

    private fun calculateCodon136(sireId: EntityId, damId: EntityId): EntityId {
        //	Calculate codon136 value based on sire and dam if possible
        //  In the current AnimalTrakker Database the coding is set up like this
        val sireCodonId = animalRepository.queryCodonForAnimal(sireId, Codon.CODE_136)
        val damCodonId = animalRepository.queryCodonForAnimal(damId, Codon.CODE_136)
        return CodonCalculation.calculateCodon136(sireCodonId?.id, damCodonId?.id)
    }

    private fun calculateCodon141(sireId: EntityId, damId: EntityId): EntityId {
        //	Calculate codon141 value based on sire and dam if possible
        //  In the current AnimalTrakker Database the coding is set up like this
        val sireCodonId = animalRepository.queryCodonForAnimal(sireId, Codon.CODE_141)
        val damCodonId = animalRepository.queryCodonForAnimal(damId, Codon.CODE_141)
        return CodonCalculation.calculateCodon141(sireCodonId?.id, damCodonId?.id)
    }

    private fun calculateCodon154(sireId: EntityId, damId: EntityId): EntityId {
        //	Calculate codon154 value based on sire and dam if possible
        //  In the current AnimalTrakker Database the coding is set up like this
        val sireCodonId = animalRepository.queryCodonForAnimal(sireId, Codon.CODE_154)
        val damCodonId = animalRepository.queryCodonForAnimal(damId, Codon.CODE_154)
        return CodonCalculation.calculateCodon154(sireCodonId?.id, damCodonId?.id)
    }

    private fun calculateCodon171(sireId: EntityId, damId: EntityId): EntityId {
        //	Calculate codon171 value based on sire and dam if possible
        //  In the current AnimalTrakker Database the coding is set up like this
        val sireCodonId = animalRepository.queryCodonForAnimal(sireId, Codon.CODE_171)
        val damCodonId = animalRepository.queryCodonForAnimal(damId, Codon.CODE_171)
        return CodonCalculation.calculateCodon171(sireCodonId?.id, damCodonId?.id)
    }

    private fun List<IdEntry>.idNumberForDefaultName(): String {
        val tagForName = firstOrNull { it.type.id == IdType.ID_TYPE_ID_FARM }
            ?: firstOrNull { it.isOfficial }
            ?: firstOrNull()
        return requireNotNull(tagForName).number
    }
}
