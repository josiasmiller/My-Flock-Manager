package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BirthType(
    override val id: EntityId,
    override val name: String,
    val abbreviation: String,
    val order: Int
) : Parcelable, HasIdentity, HasName {
    companion object {

        const val ID_UNKNOWN_RAW = "7585ea2e-dcdf-41cb-94c1-4d133d624c1e" //LEGACY ID = 42
        val ID_UNKNOWN = EntityId(ID_UNKNOWN_RAW)

        val ID_SINGLE = EntityId("3323b31f-c478-4d2b-abfe-1fa12e73e32a") //LEGACY ID = 1
        val ID_TWIN = EntityId("867bb971-68ae-4539-9b02-b27a22815305") //LEGACY ID = 2
        val ID_TRIPLET = EntityId("4ddf6aaa-009e-4a15-8c8b-f5eec5f8ff6e") //LEGACY ID = 3
        val ID_QUADRUPLET = EntityId("eee07d45-86a8-4488-8bb4-7a6846e69e39") //LEGACY ID = 4
        val ID_QUINTUPLET = EntityId("330e60fe-7e2b-4a3c-8e5c-8e3e6daeac2d") //LEGACY ID = 5
        val ID_SEXTUPLET = EntityId("0ba99855-f888-4504-ac8f-6a54ffea0187") //LEGACY ID = 6
        val ID_SEVEN = EntityId("335714b4-c190-438f-9c25-7c75cbac391a") //LEGACY ID = 7
        val ID_EIGHT = EntityId("40674fc7-693a-4e21-a3bc-256c04902d96") //LEGACY ID = 8
        val ID_NINE = EntityId("12e577ea-33c6-4e30-b44b-bb7233786881") //LEGACY ID = 9
        val ID_TEN = EntityId("50dd4e31-3b06-4720-9caa-af3ff2f84dde") //LEGACY ID = 10
        val ID_ELEVEN = EntityId("502d34c6-3163-49ad-95a8-daaf2aa63cc3") //LEGACY ID = 11
        val ID_TWELVE = EntityId("cd090c16-55e3-4b03-b81b-cd0fc06ae41a") //LEGACY ID = 12
        val ID_THIRTEEN = EntityId("2331d919-2965-4def-8672-8fc38258471d") //LEGACY ID = 13
        val ID_FOURTEEN = EntityId("471eeca3-6c09-42c0-8c9e-a0ec71654be5") //LEGACY ID = 14
        val ID_FIFTEEN = EntityId("e1737870-90b7-43f4-b1aa-ac6d2d1d299e") //LEGACY ID = 15
        val ID_SIXTEEN = EntityId("71946994-2faf-4b73-9122-966e22893e9e") //LEGACY ID = 16

        const val BIRTH_TYPE_MISSING = "Missing Birth Type"

        fun mapOffspringCountToBirthType(count: Int): EntityId {
            return when (count) {
                1 -> ID_SINGLE
                2 -> ID_TWIN
                3 -> ID_TRIPLET
                4 -> ID_QUADRUPLET
                5 -> ID_QUINTUPLET
                6 -> ID_SEXTUPLET
                7 -> ID_SEVEN
                8 -> ID_EIGHT
                9 -> ID_NINE
                10 -> ID_TEN
                11 -> ID_ELEVEN
                12 -> ID_TWELVE
                13 -> ID_THIRTEEN
                14 -> ID_FOURTEEN
                15 -> ID_FIFTEEN
                16 -> ID_SIXTEEN
                else -> ID_UNKNOWN
            }
        }
    }
}
