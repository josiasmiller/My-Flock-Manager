package com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.validation

import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.input.IdEntry
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.validation.IdsValidationError.ExceededIdLimitForIdType
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.validation.IdsValidationError.RequiredOfficialIdsNotMet
import com.weyr_associates.animaltrakkerfarmmobile.app.core.Result
import com.weyr_associates.animaltrakkerfarmmobile.app.core.Result.Failure
import com.weyr_associates.animaltrakkerfarmmobile.app.core.Result.Success
import com.weyr_associates.animaltrakkerfarmmobile.model.IdType
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId

sealed interface IdsValidationError {

    data object NameIdsNotSupported : IdsValidationError

    data class RequiredOfficialIdsNotMet(
        val requiredOfficialIds: Int
    ) : IdsValidationError

    data class ExceededIdLimitForIdType(
        val idType: IdType,
        val limit: Int
    ) : IdsValidationError
}

class CheckIdCombinationValidity {

    companion object {

        const val LIMIT_BANGS_IDS = 1
        const val LIMIT_TRICH_IDS = 1
        const val LIMIT_NUES_IDS = 1
        const val LIMIT_CARCASS_IDS = 1

        const val REQUIRED_OFFICIAL_TAGS = 1
    }

    fun whenAddingAnimal(idEntries: List<IdEntry>): Result<Unit, IdsValidationError> {
        if (idEntries.count { it.isOfficial } < REQUIRED_OFFICIAL_TAGS) {
            return Failure(RequiredOfficialIdsNotMet(REQUIRED_OFFICIAL_TAGS))
        }
        return performCommonChecks(idEntries) ?: Success(Unit)
    }

    fun whenAddingOffspring(idEntries: List<IdEntry>): Result<Unit, IdsValidationError> {
        return performCommonChecks(idEntries) ?: Success(Unit)
    }

    fun whenAddingIdToAnimal(idEntry: IdEntry, existingEntries: List<IdEntry>): Result<Unit, IdsValidationError> {
        val allEntries = buildList {
            addAll(existingEntries)
            add(idEntry)
        }
        return performCommonChecks(allEntries) ?: Success(Unit)
    }

    fun whenUpdatingIdOnAnimal(idEntry: IdEntry, existingEntries: List<IdEntry>): Result<Unit, IdsValidationError> {
        val allEntries = buildList {
            addAll(existingEntries.filter { it.id != idEntry.id })
            add(idEntry)
        }
        return performCommonChecks(allEntries) ?: Success(Unit)
    }

    private fun performCommonChecks(idEntries: List<IdEntry>): Failure<Unit, IdsValidationError>? {
        if (idEntries.any { it.type.id == IdType.ID_TYPE_ID_NAME }) {
            return Failure(IdsValidationError.NameIdsNotSupported)
        }
        if (LIMIT_BANGS_IDS < idEntries.count { it.type.id == IdType.ID_TYPE_ID_BANGS }) {
            return Failure(ExceededIdLimitForIdType(idEntries.findIdType(IdType.ID_TYPE_ID_BANGS), LIMIT_BANGS_IDS))
        }
        if (LIMIT_TRICH_IDS < idEntries.count { it.type.id == IdType.ID_TYPE_ID_TRICH }) {
            return Failure(ExceededIdLimitForIdType(idEntries.findIdType(IdType.ID_TYPE_ID_TRICH), LIMIT_TRICH_IDS))
        }
        if (LIMIT_NUES_IDS < idEntries.count { it.type.id == IdType.ID_TYPE_ID_NUES }) {
            return Failure(ExceededIdLimitForIdType(idEntries.findIdType(IdType.ID_TYPE_ID_NUES), LIMIT_NUES_IDS))
        }
        if (LIMIT_CARCASS_IDS < idEntries.count { it.type.id == IdType.ID_TYPE_ID_CARCASS_TAG }) {
            return Failure(ExceededIdLimitForIdType(idEntries.findIdType(IdType.ID_TYPE_ID_CARCASS_TAG), LIMIT_CARCASS_IDS))
        }
        return null
    }

    private fun List<IdEntry>.findIdType(idTypeId: EntityId): IdType {
        return first { it.type.id == idTypeId }.type
    }
}
