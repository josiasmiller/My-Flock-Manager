package com.weyr_associates.animaltrakkerfarmmobile.app.repository

import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalAlert
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalBasicInfo
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalBreed
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalBreeders
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalDetails
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalDrugEvent
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalEvaluation
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
import com.weyr_associates.animaltrakkerfarmmobile.model.BreedPart
import com.weyr_associates.animaltrakkerfarmmobile.model.BreedingSummary
import com.weyr_associates.animaltrakkerfarmmobile.model.Codon
import com.weyr_associates.animaltrakkerfarmmobile.model.CodonCharacteristic
import com.weyr_associates.animaltrakkerfarmmobile.model.DrugWithdrawal
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.EvaluationSummary
import com.weyr_associates.animaltrakkerfarmmobile.model.FemaleBreeding
import com.weyr_associates.animaltrakkerfarmmobile.model.FemaleBreedingDetails
import com.weyr_associates.animaltrakkerfarmmobile.model.FemaleBreedingHistoryEntry
import com.weyr_associates.animaltrakkerfarmmobile.model.IdInfo
import com.weyr_associates.animaltrakkerfarmmobile.model.MaleBreedingDetails
import com.weyr_associates.animaltrakkerfarmmobile.model.Owner
import com.weyr_associates.animaltrakkerfarmmobile.model.SexStandard
import com.weyr_associates.animaltrakkerfarmmobile.model.SireInfo
import com.weyr_associates.animaltrakkerfarmmobile.model.Species
import com.weyr_associates.animaltrakkerfarmmobile.model.TissueSampleEvent
import com.weyr_associates.animaltrakkerfarmmobile.model.TissueTestEvent
import com.weyr_associates.animaltrakkerfarmmobile.model.UnitOfMeasure
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

interface AnimalRepository {
    suspend fun searchAnimalsByIdType(
        partialId: String,
        idTypeId: EntityId,
        speciesId: EntityId,
        sexStandard: SexStandard? = null
    ): List<AnimalBasicInfo>

    suspend fun searchAnimalsByName(
        partialName: String,
        speciesId: EntityId,
        sexStandard: SexStandard? = null
    ): List<AnimalBasicInfo>

    fun queryAnimalName(animalId: EntityId): AnimalName?

    fun queryAnimalIds(animalId: EntityId): List<IdInfo>

    fun queryAnimalIdHistory(animalId: EntityId): List<IdInfo>
    fun animalIdHistory(animalId: EntityId): Flow<List<IdInfo>>

    fun queryAnimalSexId(animalId: EntityId): EntityId?

    fun queryAnimalSpecies(animalId: EntityId): Species?

    fun queryAnimalFlockPrefix(animalId: EntityId, registryCompanyId: EntityId): String

    suspend fun queryAnimalBasicInfoByAnimalId(animalId: EntityId): AnimalBasicInfo?
    fun animalBasicInfoByAnimalId(animalId: EntityId): Flow<AnimalBasicInfo?>

    suspend fun queryAnimalBasicInfoByEID(eidNumber: String): AnimalBasicInfo?
    fun animalBasicInfoByEID(eidNumber: String): Flow<AnimalBasicInfo?>

    suspend fun queryAnimalDetailsByAnimalId(animalId: EntityId): AnimalDetails?

    fun queryAnimalSpeciesId(animalId: EntityId): EntityId?

    suspend fun queryEIDExistence(eidNumber: String): Boolean

    suspend fun queryEIDExistenceForUpdate(idToUpdate: EntityId, eidNumber: String): Boolean

    fun queryAnimalCurrentOwnership(animalId: EntityId): AnimalOwnership?

    fun queryAnimalOwnershipOnDate(animalId: EntityId, date: LocalDate): AnimalOwnership?

    fun queryAnimalCurrentPremise(animalId: EntityId): EntityId?
    fun animalCurrentPremiseId(animalId: EntityId): Flow<EntityId?>

    fun queryAnimalLatestMovement(animalId: EntityId): AnimalMovement?
    fun animalLatestMovement(animalId: EntityId): Flow<AnimalMovement?>

    fun queryAnimalMovementHistory(animalId: EntityId): List<AnimalMovement>
    fun animalMovementHistory(animalId: EntityId): Flow<List<AnimalMovement>>

    fun queryAnimalLocationTimeline(animalId: EntityId): List<AnimalLocationEvent>
    fun animalLocationTimeline(animalId: EntityId): Flow<List<AnimalLocationEvent>>

    fun moveAnimalToPremise(animalId: EntityId, premiseId: EntityId, timeStamp: LocalDateTime)

    suspend fun queryAnimalLifetime(animalId: EntityId): AnimalLifetime?

    suspend fun queryAnimalRearing(animalId: EntityId): AnimalRearing?

    suspend fun queryAnimalParentage(animalId: EntityId): AnimalParentage?

    suspend fun queryAnimalBreeders(animalId: EntityId): AnimalBreeders?

    fun queryAnimalBreedInfo(animalId: EntityId): List<AnimalBreed>

    fun updateAnimalBreedInfo(
        animalId: EntityId,
        breedInfo: List<BreedPart>,
        timeStamp: LocalDateTime
    )

    suspend fun queryAnimalLastEvaluationWeight(animalId: EntityId): AnimalWeight?

    suspend fun queryAnimalNoteHistory(animalId: EntityId): List<AnimalNote>

    suspend fun queryAnimalDrugHistory(animalId: EntityId): List<AnimalDrugEvent>

    suspend fun queryAnimalTissueSampleHistory(animalId: EntityId): List<TissueSampleEvent>

    suspend fun queryAnimalTissueTestHistory(animalId: EntityId): List<TissueTestEvent>

    suspend fun queryAnimalEvaluationHistory(animalId: EntityId): List<AnimalEvaluation>

    suspend fun queryAnimalAlerts(animalId: EntityId): List<AnimalAlert>

    suspend fun queryGeneticCharacteristics(animalId: EntityId): AnimalGeneticCharacteristics

    fun addCoatColorCharacteristicForAnimal(
        animalId: EntityId,
        valueId: EntityId,
        methodId: EntityId,
        timeStamp: LocalDateTime
    )

    fun queryCodonForAnimal(animalId: EntityId, codon: Codon): CodonCharacteristic?

    fun addCodonCharacteristicForAnimal(
        animalId: EntityId,
        codon: Codon,
        valueId: EntityId,
        methodId: EntityId,
        timeStamp: LocalDateTime
    )

    suspend fun queryBreedingSummaryForAnimal(animalId: EntityId): BreedingSummary?
    fun breedingSummaryForAnimal(animalId: EntityId): Flow<BreedingSummary?>

    suspend fun queryBreedingSummaryForSire(sireId: EntityId): BreedingSummary?
    fun breedingSummaryForSire(sireId: EntityId): Flow<BreedingSummary?>

    suspend fun queryBreedingSummaryForDam(damId: EntityId): BreedingSummary?
    fun breedingSummaryForDam(damId: EntityId): Flow<BreedingSummary?>

    suspend fun queryBreedingDetailsForSire(sireId: EntityId): MaleBreedingDetails?
    fun breedingDetailsForSire(sireId: EntityId): Flow<MaleBreedingDetails?>

    suspend fun queryBreedingDetailsForDam(damId: EntityId): FemaleBreedingDetails?
    fun breedingDetailsForDam(damId: EntityId): Flow<FemaleBreedingDetails?>

    suspend fun queryFemaleBreedingHistory(animalId: EntityId): List<FemaleBreedingHistoryEntry>

    fun queryFemaleBreedingRecords(animalId: EntityId): List<FemaleBreeding>

    fun queryFemaleBreeding(femaleBreedingRecordId: EntityId): FemaleBreeding?

    fun queryFemaleBreedingForBirthdate(animalId: EntityId, birthDate: LocalDate): FemaleBreeding?

    fun addFemaleBreedingRecordForBirthdate(
        animalId: EntityId,
        birthDate: LocalDate,
        birthTime: LocalTime? = null
    ): EntityId

    fun updateFemaleBreedingRecordBirthingDate(
        femaleBreedingRecordId: EntityId,
        birthingDate: LocalDate,
        birthingTime: LocalTime,
        timeStamp: LocalDateTime
    )

    fun updateFemaleBreedingRecordGestationLength(
        femaleBreedingRecordId: EntityId,
        gestationLength: Int,
        timeStamp: LocalDateTime
    )

    fun updateBirthingNotesForBirth(
        femaleBreedingRecordId: EntityId,
        sexAbbreviation: String,
        timeStamp: LocalDateTime
    )

    fun updateBirthingNotesForFemaleBreedingRecord(
        femaleBreedingRecordId: EntityId,
        birthingNotes: String,
        timeStamp: LocalDateTime
    )

    fun incrementNumberOfAnimalsBorn(
        femaleBreedingRecordId: EntityId,
        timeStamp: LocalDateTime
    )

    suspend fun querySireForOffspringFrom(animalId: EntityId): SireInfo

    fun addAnimal(
        animalName: String,
        breeding: List<BreedPart>,
        sexId: EntityId,
        damId: EntityId? = null,
        sireId: EntityId? = null,
        birthDate: LocalDate,
        birthTime: LocalTime? = null,
        birthOrder: Int? = null,
        birthTypeId: EntityId,
        rearTypeId: EntityId?,
        ownerId: EntityId,
        ownerType: Owner.Type,
        ownerPremiseId: EntityId,
        breederId: EntityId,
        breederTypeCode: Int,
        weight: Float? = null,
        weightUnitsId: EntityId? = null,
        timeStampOn: LocalDateTime
    ): EntityId

    fun assignAnimalToFlock(
        animalId: EntityId,
        flockPrefixId: EntityId,
        registryCompanyId: EntityId,
        timeStamp: LocalDateTime
    )

    fun updateBirthType(animalIds: List<EntityId>, birthTypeId: EntityId)

    suspend fun addNotesToAnimal(
        animalId: EntityId,
        customNote: String?,
        predefinedNoteIds: List<EntityId>,
        timeStamp: LocalDateTime
    )

    suspend fun addAlertForAnimal(
        animalId: EntityId,
        alert: String,
        timeStamp: LocalDateTime
    )

    suspend fun addDrugWithdrawalAlertForAnimal(
        animalId: EntityId,
        drugWithdrawal: DrugWithdrawal,
        timeStamp: LocalDateTime
    )

    suspend fun addEvaluationSummaryAlertForAnimal(
        animalId: EntityId,
        evaluationSummary: EvaluationSummary,
        timeStamp: LocalDateTime
    )

    fun addIdToAnimal(
        animalId: EntityId,
        idTypeId: EntityId,
        idColorId: EntityId,
        idLocationId: EntityId,
        idNumber: String,
        isOfficial: Boolean,
        timeStampOn: LocalDateTime
    ): EntityId

    suspend fun updateIdOnAnimal(
        id: EntityId,
        typeId: EntityId,
        colorId: EntityId,
        locationId: EntityId,
        number: String,
        timeStamp: LocalDateTime
    ): Boolean

    suspend fun removeIdFromAnimal(
        id: EntityId,
        removeReasonId: EntityId,
        timeStamp: LocalDateTime
    ): Boolean

    suspend fun addEvaluationForAnimal(
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
    ): EntityId

    suspend fun addTissueTestForAnimal(
        animalId: EntityId,
        tissueSampleTypeId: EntityId,
        tissueSampleContainerTypeId: EntityId,
        tissueSampleContainerId: String,
        tissueSampleContainerExpDate: LocalDate?,
        tissueTestId: EntityId,
        laboratoryId: EntityId,
        timeStampOn: LocalDateTime
    ): EntityId

    fun markAnimalDeceased(
        animalId: EntityId,
        deathReasonId: EntityId,
        deathDate: LocalDate,
        timeStamp: LocalDateTime
    )

    suspend fun recordAnimalWeight(
        animalId: EntityId,
        weight: Float,
        weightUnits: UnitOfMeasure,
        ageInDays: Long,
        timeStamp: LocalDateTime
    )

    fun recordBirthEvaluation(
        animalId: EntityId,
        lambEaseId: EntityId,
        suckReflexId: EntityId,
        birthWeight: Float?,
        birthWeightUnitsId: EntityId?,
        timeStamp: LocalDateTime
    )

    suspend fun recordDrugAdministeredToAnimal(
        animalId: EntityId,
        drugLotId: EntityId,
        drugLocationId: EntityId,
        drugDosage: String,
        offLabelDrugDoseId: EntityId?,
        timeStamp: LocalDateTime
    )

    suspend fun recordAnimalWeaned(
        animalId: EntityId,
        timeStamp: LocalDateTime
    )

    fun idsOfOffspringBornInYear(damId: EntityId, year: Int): List<EntityId>
    fun numberOfStillbornsForDamInYear(damId: EntityId, sexId: EntityId, year: Int): Int
}
