package com.weyr_associates.animaltrakkerfarmmobile.model

data class AnimalOwnership(
    val id: EntityId,
    val animalId: EntityId,
    val ownerId: EntityId,
    val ownerType: Owner.Type
)
