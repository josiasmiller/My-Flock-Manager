package com.weyr_associates.animaltrakkerfarmmobile.app.settings

import com.weyr_associates.animaltrakkerfarmmobile.app.repository.SpeciesRepository
import com.weyr_associates.animaltrakkerfarmmobile.model.Species

class LoadDefaultSpecies(
    private val loadDefaultSpeciesId: LoadDefaultSpeciesId,
    private val speciesRepository: SpeciesRepository
) {
    suspend operator fun invoke(): Species? {
        return speciesRepository.querySpeciesById(loadDefaultSpeciesId())
    }
}
