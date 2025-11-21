package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Sex(
    override val id: EntityId,
    override val name: String,
    override val abbreviation: String,
    val standard: String,
    val standardAbbreviation: String,
    val order: Int,
    val species: Species
) : Parcelable, HasIdentity, HasName, HasAbbreviation {

    companion object {

        const val ID_SHEEP_RAM_RAW = "23c415a7-b60f-4231-8cfc-c205a79faef3" //LEGACY ID = 1
        const val ID_SHEEP_EWE_RAW = "ef27e059-b9db-4fe5-8017-8b1e10acf59f" //LEGACY ID = 2
        const val ID_SHEEP_WETHER_RAW = "3230b749-e2a3-444c-a862-5f701cee5672" //LEGACY ID = 3
        const val ID_SHEEP_SPAYED_RAW = "79184358-1b5f-4e5e-811f-0dae722ea469" //LEGACY ID = 25
        const val ID_SHEEP_UNKNOWN_RAW = "7ae73a29-f79f-4bfb-b009-3933c48cebf6" //LEGACY ID = 4
        const val ID_GOAT_BUCK_RAW = "125d0f5e-05bf-4b91-b5f2-d0bb2ea4b32e" //LEGACY ID = 5
        const val ID_GOAT_DOE_RAW = "9a725f0a-2979-4063-9602-136a49fec454" //LEGACY ID = 6
        const val ID_GOAT_WETHER_RAW = "9239fbb6-52f0-474e-8d1a-bf9d688706fa" //LEGACY ID = 7
        const val ID_GOAT_SPAYED_RAW = "5ba27366-8880-4f59-9e88-e593a40919f1" //LEGACY ID = 26
        const val ID_GOAT_UNKNOWN_RAW = "f63ab268-623d-4364-9160-d1a8c287cc71" //LEGACY ID = 8
        const val ID_CATTLE_BULL_RAW = "9363482e-341c-453c-977b-a4f2898fbaf1" //LEGACY ID = 9
        const val ID_CATTLE_COW_RAW = "d89896da-5f58-4e3f-a8ce-23e5a104508b" //LEGACY ID = 10
        const val ID_CATTLE_STEER_RAW = "86333f95-7a78-4d34-85d9-a7a52e17f132" //LEGACY ID = 11
        const val ID_CATTLE_SPAYED_RAW = "ca5b2865-4508-4dd5-9254-35cf678f3439" //LEGACY ID = 27
        const val ID_CATTLE_UNKNOWN_RAW = "778c9996-ac82-4fac-a787-e9b6f728d5a7" //LEGACY ID = 12
        const val ID_HORSE_STALLION_RAW = "f8482618-57e7-4dc2-beac-a1974338713f" //LEGACY ID = 13
        const val ID_HORSE_MARE_RAW = "3a53ad48-05bd-4e99-8261-70e412772000" //LEGACY ID = 14
        const val ID_HORSE_GELDING_RAW = "bd3b67f0-b42d-4d74-9bfe-6b1f7019d82f" //LEGACY ID = 15
        const val ID_HORSE_SPAYED_RAW = "566868de-74e5-4d5c-a945-97cf34d33efe" //LEGACY ID = 28
        const val ID_HORSE_UNKNOWN_RAW = "ae6d2a12-89d8-4af4-95c3-2f75a477d260" //LEGACY ID = 16
        const val ID_DONKEY_JACK_RAW = "f7d2d691-e880-4907-bdcb-033f9fa55a3f" //LEGACY ID = 17
        const val ID_DONKEY_JENNY_RAW = "eb885c80-59ea-4656-aa2d-9a7c27de7a75" //LEGACY ID = 18
        const val ID_DONKEY_GELDING_RAW = "2750cfe3-dbb3-49d4-bae0-3ed8ffc42b18" //LEGACY ID = 19
        const val ID_DONKEY_SPAYED_RAW = "109dfd64-79b7-4164-bfaa-2f000e214b62" //LEGACY ID = 29
        const val ID_DONKEY_UNKNOWN_RAW = "6477f915-f9c1-4abb-b64a-f53c460d610e" //LEGACY ID = 20
        const val ID_PIG_BOAR_RAW = "8ff2b1e9-82a4-4a5d-b005-2e2ea406e22d" //LEGACY ID = 21
        const val ID_PIG_SOW_RAW = "24f10675-75e5-45da-83c9-37b72656c67a" //LEGACY ID = 22
        const val ID_PIG_BARROW_RAW = "8d8f7b85-8e6b-415b-a4f6-c31eb2b03441" //LEGACY ID = 23
        const val ID_PIG_SPAYED_RAW = "b436d6a8-cf86-4866-915e-c4e084dbca90" //LEGACY ID = 30
        const val ID_PIG_UNKNOWN_RAW = "c358d1e6-e290-47f2-821d-a16006c1f6c7" //LEGACY ID = 24

        val ID_SHEEP_RAM = EntityId(ID_SHEEP_RAM_RAW)
        val ID_SHEEP_EWE = EntityId(ID_SHEEP_EWE_RAW)
        val ID_SHEEP_WETHER = EntityId(ID_SHEEP_WETHER_RAW)
        val ID_SHEEP_SPAYED = EntityId(ID_SHEEP_SPAYED_RAW)
        val ID_SHEEP_UNKNOWN = EntityId(ID_SHEEP_UNKNOWN_RAW)
        val ID_GOAT_BUCK = EntityId(ID_GOAT_BUCK_RAW)
        val ID_GOAT_DOE = EntityId(ID_GOAT_DOE_RAW)
        val ID_GOAT_WETHER = EntityId(ID_GOAT_WETHER_RAW)
        val ID_GOAT_SPAYED = EntityId(ID_GOAT_SPAYED_RAW)
        val ID_GOAT_UNKNOWN = EntityId(ID_GOAT_UNKNOWN_RAW)
        val ID_CATTLE_BULL = EntityId(ID_CATTLE_BULL_RAW)
        val ID_CATTLE_COW = EntityId(ID_CATTLE_COW_RAW)
        val ID_CATTLE_STEER = EntityId(ID_CATTLE_STEER_RAW)
        val ID_CATTLE_SPAYED = EntityId(ID_CATTLE_SPAYED_RAW)
        val ID_CATTLE_UNKNOWN = EntityId(ID_CATTLE_UNKNOWN_RAW)
        val ID_HORSE_STALLION = EntityId(ID_HORSE_STALLION_RAW)
        val ID_HORSE_MARE = EntityId(ID_HORSE_MARE_RAW)
        val ID_HORSE_GELDING = EntityId(ID_HORSE_GELDING_RAW)
        val ID_HORSE_SPAYED = EntityId(ID_HORSE_SPAYED_RAW)
        val ID_HORSE_UNKNOWN = EntityId(ID_HORSE_UNKNOWN_RAW)
        val ID_DONKEY_JACK = EntityId(ID_DONKEY_JACK_RAW)
        val ID_DONKEY_JENNY = EntityId(ID_DONKEY_JENNY_RAW)
        val ID_DONKEY_GELDING = EntityId(ID_DONKEY_GELDING_RAW)
        val ID_DONKEY_SPAYED = EntityId(ID_DONKEY_SPAYED_RAW)
        val ID_DONKEY_UNKNOWN = EntityId(ID_DONKEY_UNKNOWN_RAW)
        val ID_PIG_BOAR = EntityId(ID_PIG_BOAR_RAW)
        val ID_PIG_SOW = EntityId(ID_PIG_SOW_RAW)
        val ID_PIG_BARROW = EntityId(ID_PIG_BARROW_RAW)
        val ID_PIG_SPAYED = EntityId(ID_PIG_SPAYED_RAW)
        val ID_PIG_UNKNOWN = EntityId(ID_PIG_UNKNOWN_RAW)

        fun speciesIdFromSexId(sexId: EntityId): EntityId = when (sexId) {
            ID_SHEEP_RAM,
            ID_SHEEP_EWE,
            ID_SHEEP_WETHER,
            ID_SHEEP_SPAYED,
            ID_SHEEP_UNKNOWN -> Species.ID_SHEEP
            ID_GOAT_BUCK,
            ID_GOAT_DOE,
            ID_GOAT_WETHER,
            ID_GOAT_SPAYED,
            ID_GOAT_UNKNOWN -> Species.ID_GOAT
            ID_CATTLE_BULL,
            ID_CATTLE_COW,
            ID_CATTLE_STEER,
            ID_CATTLE_SPAYED,
            ID_CATTLE_UNKNOWN -> Species.ID_CATTLE
            ID_HORSE_STALLION,
            ID_HORSE_MARE,
            ID_HORSE_GELDING,
            ID_HORSE_SPAYED,
            ID_HORSE_UNKNOWN -> Species.ID_HORSE
            ID_DONKEY_JACK,
            ID_DONKEY_JENNY,
            ID_DONKEY_GELDING,
            ID_DONKEY_SPAYED,
            ID_DONKEY_UNKNOWN -> Species.ID_DONKEY
            ID_PIG_BOAR,
            ID_PIG_SOW,
            ID_PIG_BARROW,
            ID_PIG_SPAYED,
            ID_PIG_UNKNOWN -> Species.ID_PIG
            else -> EntityId.UNKNOWN
        }

        fun isFemale(sexId: EntityId): Boolean = when (sexId) {
            ID_SHEEP_EWE,
            ID_GOAT_DOE,
            ID_CATTLE_COW,
            ID_HORSE_MARE,
            ID_DONKEY_JENNY,
            ID_PIG_SOW -> true
            else -> false
        }

        fun isSpayed(sexId: EntityId): Boolean = when (sexId) {
            ID_SHEEP_SPAYED,
            ID_GOAT_SPAYED,
            ID_CATTLE_SPAYED,
            ID_HORSE_SPAYED,
            ID_DONKEY_SPAYED,
            ID_PIG_SPAYED -> true
            else -> false
        }

        fun isOrWasFemale(sexId: EntityId) = isFemale(sexId) || isSpayed(sexId)

        fun isMale(sexId: EntityId): Boolean = when (sexId) {
            ID_SHEEP_RAM,
            ID_GOAT_BUCK,
            ID_CATTLE_BULL,
            ID_HORSE_STALLION,
            ID_DONKEY_JACK,
            ID_PIG_BOAR -> true
            else -> false
        }

        fun isCastrate(sexId: EntityId): Boolean = when (sexId) {
            ID_SHEEP_WETHER,
            ID_GOAT_WETHER,
            ID_CATTLE_STEER,
            ID_HORSE_GELDING,
            ID_DONKEY_GELDING,
            ID_PIG_BARROW -> true
            else -> false
        }

        fun isOrWasMale(sexId: EntityId): Boolean = isMale(sexId) || isCastrate(sexId)
    }
}
