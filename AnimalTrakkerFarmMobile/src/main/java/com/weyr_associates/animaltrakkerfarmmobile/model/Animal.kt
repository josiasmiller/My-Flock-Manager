package com.weyr_associates.animaltrakkerfarmmobile.model

import com.weyr_associates.animaltrakkerfarmmobile.model.Species.Companion.ID_CATTLE
import com.weyr_associates.animaltrakkerfarmmobile.model.Species.Companion.ID_DONKEY
import com.weyr_associates.animaltrakkerfarmmobile.model.Species.Companion.ID_GOAT
import com.weyr_associates.animaltrakkerfarmmobile.model.Species.Companion.ID_HORSE
import com.weyr_associates.animaltrakkerfarmmobile.model.Species.Companion.ID_PIG
import com.weyr_associates.animaltrakkerfarmmobile.model.Species.Companion.ID_SHEEP

object Animal {

    const val ID_UNKNOWN_SIRE_SHEEP_RAW = "1a97246f-9539-423e-9a63-8cfefa2cbf14" //LEGACY ID = 14754
    const val ID_UNKNOWN_SIRE_GOAT_RAW = "a1541ba6-34da-46f3-9d8c-e5370fb9fbcb" //LEGACY ID = 14854
    const val ID_UNKNOWN_SIRE_CATTLE_RAW = "b201b137-3c68-4d3e-a6da-82d3d6b5bd1d" //LEGACY ID = 14856
    const val ID_UNKNOWN_SIRE_HORSE_RAW = "1ae1135a-1faa-4a6e-bcf5-fa17cbffb9a0" //LEGACY ID = 14858
    const val ID_UNKNOWN_SIRE_DONKEY_RAW = "a2287ebc-8289-4d23-8065-20f7a82609ec" //LEGACY ID = 14860
    const val ID_UNKNOWN_SIRE_PIG_RAW = "78d28f7d-a22e-46fc-bba3-c96043d250c5" //LEGACY ID = 14862

    val ID_UNKNOWN_SIRE_SHEEP = EntityId(ID_UNKNOWN_SIRE_SHEEP_RAW)
    val ID_UNKNOWN_SIRE_GOAT = EntityId(ID_UNKNOWN_SIRE_GOAT_RAW)
    val ID_UNKNOWN_SIRE_CATTLE = EntityId(ID_UNKNOWN_SIRE_CATTLE_RAW)
    val ID_UNKNOWN_SIRE_HORSE = EntityId(ID_UNKNOWN_SIRE_HORSE_RAW)
    val ID_UNKNOWN_SIRE_DONKEY = EntityId(ID_UNKNOWN_SIRE_DONKEY_RAW)
    val ID_UNKNOWN_SIRE_PIG = EntityId(ID_UNKNOWN_SIRE_PIG_RAW)

    const val ID_UNKNOWN_DAM_SHEEP_RAW = "bc63874d-e98a-4d19-8411-2282a9db42cd" //LEGACY ID = 14755
    const val ID_UNKNOWN_DAM_GOAT_RAW = "55bdcb61-bec1-43a2-8f9c-3161614043b3" //LEGACY ID = 14855
    const val ID_UNKNOWN_DAM_CATTLE_RAW = "f2be46bd-4d0d-46c3-8207-91fdaa3030f5" //LEGACY ID = 14857
    const val ID_UNKNOWN_DAM_HORSE_RAW = "4f24e784-bc54-42ea-8fc2-af1cbd8b179a" //LEGACY ID = 14859
    const val ID_UNKNOWN_DAM_DONKEY_RAW = "49c8e135-0424-483e-9f62-9ce65b9b80ed" //LEGACY ID = 14861
    const val ID_UNKNOWN_DAM_PIG_RAW = "87923aac-7259-435c-8e96-c8dced8483b7" //LEGACY ID = 14863

    val ID_UNKNOWN_DAM_SHEEP = EntityId(ID_UNKNOWN_DAM_SHEEP_RAW)
    val ID_UNKNOWN_DAM_GOAT = EntityId(ID_UNKNOWN_DAM_GOAT_RAW)
    val ID_UNKNOWN_DAM_CATTLE = EntityId(ID_UNKNOWN_DAM_CATTLE_RAW)
    val ID_UNKNOWN_DAM_HORSE = EntityId(ID_UNKNOWN_DAM_HORSE_RAW)
    val ID_UNKNOWN_DAM_DONKEY = EntityId(ID_UNKNOWN_DAM_DONKEY_RAW)
    val ID_UNKNOWN_DAM_PIG = EntityId(ID_UNKNOWN_DAM_PIG_RAW)

    fun isUnknownAnimal(animalId: EntityId): Boolean {
        return when (animalId) {
            ID_UNKNOWN_SIRE_SHEEP,
            ID_UNKNOWN_SIRE_GOAT,
            ID_UNKNOWN_SIRE_CATTLE,
            ID_UNKNOWN_SIRE_HORSE,
            ID_UNKNOWN_SIRE_DONKEY,
            ID_UNKNOWN_SIRE_PIG,
            ID_UNKNOWN_DAM_SHEEP,
            ID_UNKNOWN_DAM_GOAT,
            ID_UNKNOWN_DAM_CATTLE,
            ID_UNKNOWN_DAM_HORSE,
            ID_UNKNOWN_DAM_DONKEY,
            ID_UNKNOWN_DAM_PIG -> true
            else -> false
        }
    }

    fun unknownSireIdForSpecies(speciesId: EntityId): EntityId = when (speciesId) {
        ID_SHEEP -> ID_UNKNOWN_SIRE_SHEEP
        ID_GOAT -> ID_UNKNOWN_SIRE_GOAT
        ID_CATTLE -> ID_UNKNOWN_SIRE_CATTLE
        ID_HORSE -> ID_UNKNOWN_SIRE_HORSE
        ID_DONKEY -> ID_UNKNOWN_SIRE_DONKEY
        ID_PIG -> ID_UNKNOWN_SIRE_PIG
        else -> EntityId.UNKNOWN
    }

    fun unknownDamIdForSpecies(speciesId: EntityId): EntityId = when (speciesId) {
        ID_SHEEP -> ID_UNKNOWN_DAM_SHEEP
        ID_GOAT -> ID_UNKNOWN_DAM_GOAT
        ID_CATTLE -> ID_UNKNOWN_DAM_CATTLE
        ID_HORSE -> ID_UNKNOWN_DAM_HORSE
        ID_DONKEY -> ID_UNKNOWN_DAM_DONKEY
        ID_PIG -> ID_UNKNOWN_DAM_PIG
        else -> EntityId.UNKNOWN
    }
}
