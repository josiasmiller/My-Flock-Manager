package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class Species(
    override val id: EntityId,
    val commonName: String,
    val genericName: String,
    val earlyFemaleBreedingAgeDays: Int,
    val earlyGestationLengthDays: Int,
    val lateGestationLengthDays: Int,
    val typicalGestationLengthDays: Int
) : Parcelable, HasIdentity, HasName {

    @IgnoredOnParcel
    override val name: String = commonName

    companion object {

        const val ID_SHEEP_RAW = "3ca0b500-3f96-4342-8620-bfda6e900222" //LEGACY ID = 1
        const val ID_GOAT_RAW = "3ea774ba-9103-410e-a904-d91a22b38276" //LEGACY ID = 2
        const val ID_CATTLE_RAW = "93d8dd69-8d85-44e3-a757-9d215b4295be" //LEGACY ID = 3
        const val ID_HORSE_RAW = "a4652a1b-b5fc-4d3a-a2b3-55b8843c33cf" //LEGACY ID = 4
        const val ID_DONKEY_RAW = "67966e17-2e3d-44e3-9401-a2c6112fdbb4" //LEGACY ID = 5
        const val ID_PIG_RAW = "ce4876de-69c7-4e11-b123-c333d557eece" //LEGACY ID = 6

        val ID_SHEEP = EntityId(ID_SHEEP_RAW)
        val ID_GOAT = EntityId(ID_GOAT_RAW)
        val ID_CATTLE = EntityId(ID_CATTLE_RAW)
        val ID_HORSE = EntityId(ID_HORSE_RAW)
        val ID_DONKEY = EntityId(ID_DONKEY_RAW)
        val ID_PIG = EntityId(ID_PIG_RAW)
    }
}
