package com.weyr_associates.animaltrakkerfarmmobile.app.repository

import com.weyr_associates.animaltrakkerfarmmobile.model.Breed
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId

interface BreedRepository {
    fun queryBreedsForSpecies(speciesId: EntityId): List<Breed>
    fun queryBreed(id: EntityId): Breed?
}
