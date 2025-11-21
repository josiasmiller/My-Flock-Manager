package com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.validation

import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.input.IdEntry
import com.weyr_associates.animaltrakkerfarmmobile.app.core.Result
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.AnimalRepository
import com.weyr_associates.animaltrakkerfarmmobile.model.IdType

data class EIDNumberAlreadyInUse(val eidNumber: String) : IdsValidationError

data class DuplicationOfEIDs(val duplicates: List<IdEntry>) : IdsValidationError

class CheckEIDsNotDuplicated(private val animalRepository: AnimalRepository) {

    suspend fun isEIDInUse(eidNumber: String): Result<Unit, EIDNumberAlreadyInUse> {
        return if(!animalRepository.queryEIDExistence(eidNumber))
            Result.Success(Unit) else Result.Failure(EIDNumberAlreadyInUse(eidNumber))
    }

    suspend fun withAdditionOf(idEntry: IdEntry): Result<Unit, DuplicationOfEIDs> {
        return if (!animalRepository.queryEIDExistence(idEntry.number))
            Result.Success(Unit) else Result.Failure(DuplicationOfEIDs(listOf(idEntry)))
    }

    suspend fun withAdditionOf(idEntries: List<IdEntry>): Result<Unit, DuplicationOfEIDs> {
        val eidEntries = idEntries.filter { it.type.id == IdType.ID_TYPE_ID_EID }
        if (eidEntries.isEmpty()) {
            return Result.Success(Unit)
        }
        val duplicatingEntries = mutableListOf<IdEntry>()
        val localEIDNumbers = mutableSetOf<String>()
        eidEntries.forEach {
            if (localEIDNumbers.contains(it.number) ||
                animalRepository.queryEIDExistence(it.number)) {
                duplicatingEntries.add(it)
            }
            localEIDNumbers.add(it.number)
        }
        return if (duplicatingEntries.isEmpty()) Result.Success(Unit)
        else Result.Failure(DuplicationOfEIDs(duplicatingEntries))
    }

    suspend fun withUpdateOf(idEntry: IdEntry): Result<Unit, DuplicationOfEIDs> {
        return if (!animalRepository.queryEIDExistenceForUpdate(idEntry.id, idEntry.number))
            Result.Success(Unit) else Result.Failure(DuplicationOfEIDs(listOf(idEntry)))
    }
}
