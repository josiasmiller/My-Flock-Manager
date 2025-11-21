package com.weyr_associates.animaltrakkerfarmmobile.app.animal.action

import com.weyr_associates.animaltrakkerfarmmobile.app.core.Result
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.AnimalRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.DrugRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseHandler
import com.weyr_associates.animaltrakkerfarmmobile.model.DrugWithdrawal
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.Hoof
import com.weyr_associates.animaltrakkerfarmmobile.model.Hooves
import com.weyr_associates.animaltrakkerfarmmobile.model.Horn
import com.weyr_associates.animaltrakkerfarmmobile.model.Horns
import com.weyr_associates.animaltrakkerfarmmobile.model.PredefinedNote
import com.weyr_associates.animaltrakkerfarmmobile.model.UnitOfMeasure
import com.weyr_associates.animaltrakkerfarmmobile.model.hasAll
import com.weyr_associates.animaltrakkerfarmmobile.model.hasNone
import kotlinx.coroutines.CancellationException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

sealed interface TrackAnimalActionsError

data object AnimalDoesNotExist : TrackAnimalActionsError

data class NoValidDrugDosageForSpecies(
    val drugId: EntityId,
    val drugName: String,
    val speciesId: EntityId,
    val speciesName: String
) : TrackAnimalActionsError

data class DrugDoseAnimalSpeciesMismatch(
    val drugId: EntityId,
    val drugName: String,
    val drugSpeciesId: EntityId,
    val drugSpeciesName: String,
    val animalSpeciesId: EntityId,
    val animalSpeciesName: String
) : TrackAnimalActionsError

data class FatalError(
    val exception: Exception
) : TrackAnimalActionsError

class TrackAnimalActions(
    private val databaseHandler: DatabaseHandler,
    private val animalRepository: AnimalRepository,
    private val drugRepository: DrugRepository
) {
    suspend operator fun invoke(
        animalId: EntityId,
        ageInDays: Long,
        actions: ActionSet,
        timeStamp: LocalDateTime = LocalDateTime.now()
    ): Result<Unit, TrackAnimalActionsError> {
        val completedDrugActions = actions.vaccines.filter { it.isActionable && it.isComplete } +
                actions.dewormers.filter { it.isActionable && it.isComplete } +
                actions.otherDrugs.filter { it.isActionable && it.isComplete }

        val weightAction = actions.weight
        val hoovesAction = actions.hoofCheck
        val hornsAction = actions.hornCheck
        val shoeAction = actions.shoeing
        val shearAction = actions.shearing
        val weanAction = actions.weaning

        val hasCompletedActions = completedDrugActions.isNotEmpty() ||
                weightAction?.isComplete ?: false ||
                hoovesAction?.isComplete ?: false ||
                hornsAction?.isComplete ?: false ||
                shoeAction?.isComplete ?: false ||
                shearAction?.isComplete ?: false ||
                weanAction?.isComplete ?: false

        if (hasCompletedActions) {
            with(databaseHandler.writableDatabase) {
                beginTransaction()
                try {
                    if (completedDrugActions.isNotEmpty()) {
                        val drugWithdrawals = mutableListOf<DrugWithdrawal>()
                        val animalBasicInfo = animalRepository.queryAnimalBasicInfoByAnimalId(animalId) ?:
                            return Result.Failure(AnimalDoesNotExist)
                        completedDrugActions.forEach { drugAction ->
                            val drugAppInfo = drugAction.configuration.drugApplicationInfo
                            val drugDosageSpec = drugAction.drugDosageSpec
                            val offLabelDrugDose = drugAction.offLabelDrugDose
                            if (offLabelDrugDose != null && offLabelDrugDose.speciesId != animalBasicInfo.speciesId) {
                                return Result.Failure(
                                    DrugDoseAnimalSpeciesMismatch(
                                        drugId = drugAction.configuration.drugApplicationInfo.drugId,
                                        drugName = drugAction.configuration.drugApplicationInfo.genericDrugName,
                                        drugSpeciesId = offLabelDrugDose.speciesId,
                                        drugSpeciesName = offLabelDrugDose.speciesName,
                                        animalSpeciesId = animalBasicInfo.speciesId,
                                        animalSpeciesName = animalBasicInfo.speciesCommonName
                                    )
                                )
                            }
                            if ((drugDosageSpec != null && drugDosageSpec.speciesId != animalBasicInfo.speciesId)) {
                                return Result.Failure(
                                    DrugDoseAnimalSpeciesMismatch(
                                        drugId = drugDosageSpec.drugId,
                                        drugName = drugDosageSpec.drugGenericName,
                                        drugSpeciesId = drugDosageSpec.speciesId,
                                        drugSpeciesName = drugDosageSpec.speciesName,
                                        animalSpeciesId = animalBasicInfo.speciesId,
                                        animalSpeciesName = animalBasicInfo.speciesCommonName
                                    )
                                )
                            }
                            val effectiveDrugDose: String = drugAction.effectiveDrugDose
                                ?: return Result.Failure(
                                    NoValidDrugDosageForSpecies(
                                        drugId = drugAction.configuration.drugApplicationInfo.drugId,
                                        drugName = drugAction.configuration.drugApplicationInfo.genericDrugName,
                                        speciesId = animalBasicInfo.speciesId,
                                        speciesName = animalBasicInfo.speciesCommonName
                                    )
                                )
                            drugAction.drugMeatWithdrawal?.let { meatWithdrawalSpec ->
                                if (0 < meatWithdrawalSpec.userWithdrawal) {
                                    val withdrawalDate = computeWithdrawalDate(
                                        timeStamp = timeStamp,
                                        withdrawal = meatWithdrawalSpec.userWithdrawal,
                                        withdrawalTimeUnitsId = meatWithdrawalSpec.withdrawalUnitsId
                                    )
                                    drugWithdrawals.add(
                                        DrugWithdrawal(
                                            type = DrugWithdrawal.Type.MEAT,
                                            drugId = drugAppInfo.id,
                                            drugName = drugAppInfo.tradeDrugName,
                                            drugLot = drugAppInfo.lot ?: "",
                                            withdrawalDate = resolveMinWithdrawalDate(
                                                timeStamp,
                                                withdrawalDate
                                            ),
                                            withdrawalUnitsId = meatWithdrawalSpec.withdrawalUnitsId
                                        )
                                    )
                                }
                            }
                            drugAction.drugMilkWithdrawal?.let { milkWithdrawalSpec ->
                                if (0 < milkWithdrawalSpec.userWithdrawal) {
                                    val withdrawalDate = computeWithdrawalDate(
                                        timeStamp = timeStamp,
                                        withdrawal = milkWithdrawalSpec.userWithdrawal,
                                        withdrawalTimeUnitsId = milkWithdrawalSpec.withdrawalUnitsId
                                    )
                                    drugWithdrawals.add(
                                        DrugWithdrawal(
                                            type = DrugWithdrawal.Type.MILK,
                                            drugId = drugAppInfo.id,
                                            drugName = drugAppInfo.tradeDrugName,
                                            drugLot = drugAppInfo.lot ?: "",
                                            withdrawalDate = resolveMinWithdrawalDate(
                                                timeStamp,
                                                withdrawalDate
                                            ),
                                            withdrawalUnitsId = milkWithdrawalSpec.withdrawalUnitsId
                                        )
                                    )
                                }
                            }
                            animalRepository.recordDrugAdministeredToAnimal(
                                animalId,
                                drugAction.configuration.drugApplicationInfo.id,
                                drugAction.configuration.location.id,
                                effectiveDrugDose,
                                drugAction.offLabelDrugDose?.id,
                                timeStamp
                            )
                        }
                        drugWithdrawals.forEach { drugWithdrawal ->
                            animalRepository.addDrugWithdrawalAlertForAnimal(animalId, drugWithdrawal, timeStamp)
                        }
                    }
                    if (weightAction != null && weightAction.isComplete && weightAction.weight != null) {
                        animalRepository.recordAnimalWeight(
                            animalId,
                            weightAction.weight,
                            weightAction.units,
                            ageInDays,
                            timeStamp
                        )
                    }
                    if (hoovesAction != null && hoovesAction.isComplete && hoovesAction.hoofCheck != null) {
                        val hoofTrimmingNotes = hoovesAction.hoofCheck.trimmed.mapToTrimmingNotes()
                        val hoofRotNotes = hoovesAction.hoofCheck.withFootRotObserved.mapToFootRotNotes()
                        val hoofScaldNotes = hoovesAction.hoofCheck.withFootScaldObserved.mapToFootScaldNotes()
                        val allHoofNotes = hoofTrimmingNotes + hoofRotNotes + hoofScaldNotes
                        if (allHoofNotes.isNotEmpty()) {
                            animalRepository.addNotesToAnimal(
                                animalId,
                                customNote = null,
                                allHoofNotes,
                                timeStamp
                            )
                        }
                    }
                    if (hornsAction != null && hornsAction.isComplete && hornsAction.hornCheck != null) {
                        val hornQualityNotes = hornsAction.hornCheck.badHorns.mapToHornQualityNotes()
                        val hornSawNotes = hornsAction.hornCheck.sawedHorns.mapToHornSawedNotes()
                        val allHornNotes = hornQualityNotes + hornSawNotes
                        if (allHornNotes.isNotEmpty()) {
                            animalRepository.addNotesToAnimal(
                                animalId,
                                customNote = null,
                                allHornNotes,
                                timeStamp
                            )
                        }
                    }
                    if (shoeAction != null && shoeAction.isComplete) {
                        animalRepository.addNotesToAnimal(
                            animalId,
                            customNote = null,
                            listOf(PredefinedNote.ID_SHOD),
                            timeStamp
                        )
                    }
                    if (shearAction != null && shearAction.isComplete) {
                        animalRepository.addNotesToAnimal(
                            animalId,
                            customNote = null,
                            listOf(PredefinedNote.ID_SHORN),
                            timeStamp
                        )
                    }
                    if (weanAction != null && weanAction.isActionable && weanAction.isComplete) {
                        animalRepository.addNotesToAnimal(
                            animalId,
                            customNote = null,
                            listOf(PredefinedNote.ID_WEANED),
                            timeStamp
                        )
                        animalRepository.recordAnimalWeaned(animalId, timeStamp)
                    }
                    setTransactionSuccessful()
                } finally {
                    endTransaction()
                }
            }
        }
        return Result.Success(Unit)
    }

    private fun computeWithdrawalDate(timeStamp: LocalDateTime, withdrawal: Int, withdrawalTimeUnitsId: EntityId): LocalDate? {
        return when (withdrawalTimeUnitsId) {
            UnitOfMeasure.TIME_UNIT_YEARS -> timeStamp.plusDays(365L * withdrawal.toLong()).toLocalDate()
            UnitOfMeasure.TIME_UNIT_MONTHS -> timeStamp.plusDays(30L * withdrawal.toLong()).toLocalDate()
            UnitOfMeasure.TIME_UNIT_WEEKS -> timeStamp.plusDays(7L * withdrawal.toLong()).toLocalDate()
            UnitOfMeasure.TIME_UNIT_DAYS -> timeStamp.plusDays(withdrawal.toLong()).toLocalDate()
            UnitOfMeasure.TIME_UNIT_HOURS -> timeStamp.plusHours(withdrawal.toLong()).toLocalDate()
            UnitOfMeasure.TIME_UNIT_SECONDS -> timeStamp.plusSeconds(withdrawal.toLong()).toLocalDate()
            else -> null
        }
    }

    private fun resolveMinWithdrawalDate(timeStamp: LocalDateTime, withdrawalDate: LocalDate?): LocalDate {
        val timeStampDate = timeStamp.toLocalDate()
        if (withdrawalDate == null) {
            return timeStampDate.plusDays(1)
        }
        val days = ChronoUnit.DAYS.between(timeStampDate, withdrawalDate)
        if (days < 0) {
            return timeStampDate.plusDays(1)
        }
        return withdrawalDate
    }

    private fun Hooves.mapToTrimmingNotes(): List<EntityId> {
        return when {
            hasNone() -> emptyList()
            hasAll() -> listOf(PredefinedNote.ID_TRIMMED_HOOVES_ALL)
            else -> map { it.mapToTrimmingNote() }
        }
    }

    private fun Hoof.mapToTrimmingNote(): EntityId {
        return when (this) {
            Hoof.FRONT_LEFT -> PredefinedNote.ID_TRIMMED_HOOF_FRONT_LEFT
            Hoof.FRONT_RIGHT -> PredefinedNote.ID_TRIMMED_HOOF_FRONT_RIGHT
            Hoof.BACK_LEFT -> PredefinedNote.ID_TRIMMED_HOOF_HIND_LEFT
            Hoof.BACK_RIGHT -> PredefinedNote.ID_TRIMMED_HOOF_HIND_RIGHT
        }
    }

    private fun Hooves.mapToFootRotNotes(): List<EntityId> {
        return when {
            hasNone() -> emptyList()
            hasAll() -> listOf(PredefinedNote.ID_FOOT_ROT_HOOVES_ALL)
            else -> map { it.mapToFootRotNote() }
        }
    }

    private fun Hoof.mapToFootRotNote(): EntityId {
        return when (this) {
            Hoof.FRONT_LEFT -> PredefinedNote.ID_FOOT_ROT_HOOF_FRONT_LEFT
            Hoof.FRONT_RIGHT -> PredefinedNote.ID_FOOT_ROT_HOOF_FRONT_RIGHT
            Hoof.BACK_LEFT -> PredefinedNote.ID_FOOT_ROT_HOOF_HIND_LEFT
            Hoof.BACK_RIGHT -> PredefinedNote.ID_FOOT_ROT_HOOF_HIND_RIGHT
        }
    }

    private fun Hooves.mapToFootScaldNotes(): List<EntityId> {
        return when {
            hasNone() -> emptyList()
            hasAll() -> listOf(PredefinedNote.ID_FOOT_SCALD_HOOVES_ALL)
            else -> map { it.mapToFootScaldNote() }
        }
    }

    private fun Hoof.mapToFootScaldNote(): EntityId {
        return when (this) {
            Hoof.FRONT_LEFT -> PredefinedNote.ID_FOOT_SCALD_HOOF_FRONT_LEFT
            Hoof.FRONT_RIGHT -> PredefinedNote.ID_FOOT_SCALD_HOOF_FRONT_RIGHT
            Hoof.BACK_LEFT -> PredefinedNote.ID_FOOT_SCALD_HOOF_HIND_LEFT
            Hoof.BACK_RIGHT -> PredefinedNote.ID_FOOT_SCALD_HOOF_HIND_RIGHT
        }
    }

    private fun Horns.mapToHornQualityNotes(): List<EntityId> {
        return when {
            hasNone() -> listOf(PredefinedNote.ID_HORN_QUALITY_GOOD_ALL)
            hasAll() -> listOf(PredefinedNote.ID_HORN_QUALITY_BAD_ALL)
            else -> map { it.mapToHornQualityNote() }
        }
    }

    private fun Horn.mapToHornQualityNote(): EntityId {
        return when(this) {
            Horn.LEFT -> PredefinedNote.ID_HORN_QUALITY_BAD_LEFT
            Horn.RIGHT -> PredefinedNote.ID_HORN_QUALITY_BAD_RIGHT
        }
    }

    private fun Horns.mapToHornSawedNotes(): List<EntityId> {
        return when {
            hasNone() -> emptyList()
            hasAll() -> listOf(
                PredefinedNote.ID_HORN_SAWED_LEFT,
                PredefinedNote.ID_HORN_SAWED_RIGHT
            )
            else -> map { it.mapToHornSawedNote() }
        }
    }

    private fun Horn.mapToHornSawedNote(): EntityId {
        return when(this) {
            Horn.LEFT -> PredefinedNote.ID_HORN_SAWED_LEFT
            Horn.RIGHT -> PredefinedNote.ID_HORN_SAWED_RIGHT
        }
    }
}
