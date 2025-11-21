package com.weyr_associates.animaltrakkerfarmmobile.app.repository

import com.weyr_associates.animaltrakkerfarmmobile.model.Drug
import com.weyr_associates.animaltrakkerfarmmobile.model.DrugApplicationInfo
import com.weyr_associates.animaltrakkerfarmmobile.model.DrugDosageSpec
import com.weyr_associates.animaltrakkerfarmmobile.model.DrugType
import com.weyr_associates.animaltrakkerfarmmobile.model.DrugWithdrawalSpec
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.OffLabelDrugDose
import com.weyr_associates.animaltrakkerfarmmobile.model.OffLabelDrugSpec
import java.time.LocalDate
import java.time.LocalDateTime

interface DrugRepository {

    suspend fun queryDrugTypes(): List<DrugType>
    suspend fun queryAllDrugs(): List<Drug>
    suspend fun queryDrugsByType(drugTypeId: EntityId): List<Drug>
    suspend fun queryDrugById(drugId: EntityId): Drug?
    suspend fun queryAvailableDrugsByType(drugTypeId: EntityId): List<DrugApplicationInfo>
    suspend fun queryDrugDosageSpecForSpecies(drugId: EntityId, speciesId: EntityId): DrugDosageSpec?
    suspend fun queryOffLabelDrugDoses(drugId: EntityId, currentDate: LocalDate): List<OffLabelDrugDose>
    suspend fun queryOffLabelDrugDose(offLabelDrugDoseId: EntityId): OffLabelDrugDose?

    suspend fun addDrug(
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
    ): EntityId

    suspend fun addDrugDose(
        drugId: EntityId,
        speciesId: EntityId,
        officialDrugDose: String,
        userDrugDose: String,
        meatWithdrawalSpec: DrugWithdrawalSpec?,
        milkWithdrawalSpec: DrugWithdrawalSpec?,
        timeStamp: LocalDateTime
    ): EntityId

    suspend fun addOffLabelDrugDose(
        drugId: EntityId,
        speciesId: EntityId,
        veterinarianContactId: EntityId,
        drugDosage: String,
        notes: String?,
        useStartDate: LocalDate,
        useEndDate: LocalDate?,
        timeStamp: LocalDateTime
    ): EntityId

    suspend fun addDrugLot(
        drugId: EntityId,
        drugLot: String,
        expirationDate: LocalDate,
        cost: Float?,
        currencyUnitsId: EntityId?,
        amountPurchased: String?,
        purchaseDate: LocalDate?,
        timeStamp: LocalDateTime
    ): EntityId
}
