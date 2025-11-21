package com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.federal

import com.weyr_associates.animaltrakkerfarmmobile.app.repository.AnimalRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.ScrapieFlockRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.Owner

interface SuggestScrapieFlockNumber {
    suspend fun retrieveSuggestion(): String?
}

class SuggestDefaultScrapieFlockNumber(
    private val loadActiveDefaultSettings: LoadActiveDefaultSettings,
    private val scrapieFlockRepository: ScrapieFlockRepository
) : SuggestScrapieFlockNumber {

    private var isLoaded: Boolean = false
    private var suggestedScrapieFlockNumber: String? = null

    override suspend fun retrieveSuggestion(): String? {
        return if (isLoaded) {
            suggestedScrapieFlockNumber
        }
        else {
            loadActiveDefaultSettings().let { defaultSettings ->
                val ownerId = defaultSettings.ownerId
                val ownerType = defaultSettings.ownerType
                    ?.let { Owner.Type.fromCode(it) }
                if (ownerId != null && ownerType != null) {
                    querySuggestion(ownerId, ownerType)
                } else null
            }.also {
                isLoaded = true
                suggestedScrapieFlockNumber = it
            }
        }
    }

    private fun querySuggestion(ownerId: EntityId, ownerType: Owner.Type): String? {
        return scrapieFlockRepository.queryActiveScrapieFlockNumberForOwner(
            ownerId = ownerId,
            ownerType = ownerType
        )?.number
    }
}

class SuggestAnimalOwnerScrapieFlockNumber(
    private val animalId: EntityId,
    private val animalRepository: AnimalRepository,
    private val scrapieFlockRepository: ScrapieFlockRepository
) : SuggestScrapieFlockNumber {

    private var isLoaded: Boolean = false
    private var suggestedScrapieFlockNumber: String? = null

    override suspend fun retrieveSuggestion(): String? {
        return if (isLoaded) {
            suggestedScrapieFlockNumber
        } else {
            animalRepository.queryAnimalCurrentOwnership(animalId)?.let { ownership ->
                querySuggestion(ownership.ownerId, ownership.ownerType)
            }.also {
                isLoaded = true
                suggestedScrapieFlockNumber = it
            }
        }
    }

    private fun querySuggestion(ownerId: EntityId, ownerType: Owner.Type): String? {
        return scrapieFlockRepository.queryActiveScrapieFlockNumberForOwner(
            ownerId = ownerId,
            ownerType = ownerType
        )?.number
    }
}
