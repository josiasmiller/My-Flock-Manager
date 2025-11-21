package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PredefinedNote(
    override val id: EntityId,
    val text: String,
    val userId: EntityId,
    val userType: UserType,
    val order: Int
): Parcelable, HasIdentity {
    
    companion object {

        const val ID_TRIMMED_HOOVES_ALL_RAW = "d09cdc60-ec42-4aa8-8e9a-31e1e717ebfe" //LEGACY ID = 14
        const val ID_TRIMMED_HOOF_FRONT_LEFT_RAW = "b594b66f-8694-424d-8292-e13f7074f29b" //LEGACY ID = 153
        const val ID_TRIMMED_HOOF_FRONT_RIGHT_RAW = "cc556913-6be8-4667-92d4-4ca45ff5e27b" //LEGACY ID = 154
        const val ID_TRIMMED_HOOF_HIND_LEFT_RAW = "ddf459a2-0537-49e8-9745-8548b83a9da7" //LEGACY ID = 155
        const val ID_TRIMMED_HOOF_HIND_RIGHT_RAW = "bfb7ebd1-321d-4bdb-a66f-5814b94180aa" //LEGACY ID = 156

        val ID_TRIMMED_HOOVES_ALL = EntityId(ID_TRIMMED_HOOVES_ALL_RAW)
        val ID_TRIMMED_HOOF_FRONT_LEFT = EntityId(ID_TRIMMED_HOOF_FRONT_LEFT_RAW)
        val ID_TRIMMED_HOOF_FRONT_RIGHT = EntityId(ID_TRIMMED_HOOF_FRONT_RIGHT_RAW)
        val ID_TRIMMED_HOOF_HIND_LEFT = EntityId(ID_TRIMMED_HOOF_HIND_LEFT_RAW)
        val ID_TRIMMED_HOOF_HIND_RIGHT = EntityId(ID_TRIMMED_HOOF_HIND_RIGHT_RAW)

        const val ID_FOOT_ROT_HOOVES_ALL_RAW = "d6909e5e-3194-4736-8833-6ea66f700ded" //LEGACY ID = 163
        const val ID_FOOT_ROT_HOOF_FRONT_LEFT_RAW = "1ee1b5b4-f433-49cb-a69c-59524e7f0206" //LEGACY ID = 164
        const val ID_FOOT_ROT_HOOF_FRONT_RIGHT_RAW = "4f695fb4-3de7-4903-b544-92a90a249534" //LEGACY ID = 165
        const val ID_FOOT_ROT_HOOF_HIND_LEFT_RAW = "884e8b70-1f67-4123-a191-1476eb61b7c5" //LEGACY ID = 166
        const val ID_FOOT_ROT_HOOF_HIND_RIGHT_RAW = "7a4212fa-34b4-40df-b083-21e21a578c12" //LEGACY ID = 167

        val ID_FOOT_ROT_HOOVES_ALL = EntityId(ID_FOOT_ROT_HOOVES_ALL_RAW)
        val ID_FOOT_ROT_HOOF_FRONT_LEFT = EntityId(ID_FOOT_ROT_HOOF_FRONT_LEFT_RAW)
        val ID_FOOT_ROT_HOOF_FRONT_RIGHT = EntityId(ID_FOOT_ROT_HOOF_FRONT_RIGHT_RAW)
        val ID_FOOT_ROT_HOOF_HIND_LEFT = EntityId(ID_FOOT_ROT_HOOF_HIND_LEFT_RAW)
        val ID_FOOT_ROT_HOOF_HIND_RIGHT = EntityId(ID_FOOT_ROT_HOOF_HIND_RIGHT_RAW)

        const val ID_FOOT_SCALD_HOOVES_ALL_RAW = "1d8e1cea-6551-4c56-b5ee-ce856816807a" //LEGACY ID = 158
        const val ID_FOOT_SCALD_HOOF_FRONT_LEFT_RAW = "3f33be5c-2878-4f33-a150-170802096e24" //LEGACY ID = 159
        const val ID_FOOT_SCALD_HOOF_FRONT_RIGHT_RAW = "29b7fef9-4488-4963-8540-cb8f379795d6" //LEGACY ID = 160
        const val ID_FOOT_SCALD_HOOF_HIND_LEFT_RAW = "84c3c550-c7a1-4e42-b4ea-d7ddd4e0e035" //LEGACY ID = 161
        const val ID_FOOT_SCALD_HOOF_HIND_RIGHT_RAW = "87af910a-2d1e-4318-b5fe-b18d7c3ac053" //LEGACY ID = 162

        val ID_FOOT_SCALD_HOOVES_ALL = EntityId(ID_FOOT_SCALD_HOOVES_ALL_RAW)
        val ID_FOOT_SCALD_HOOF_FRONT_LEFT = EntityId(ID_FOOT_SCALD_HOOF_FRONT_LEFT_RAW)
        val ID_FOOT_SCALD_HOOF_FRONT_RIGHT = EntityId(ID_FOOT_SCALD_HOOF_FRONT_RIGHT_RAW)
        val ID_FOOT_SCALD_HOOF_HIND_LEFT = EntityId(ID_FOOT_SCALD_HOOF_HIND_LEFT_RAW)
        val ID_FOOT_SCALD_HOOF_HIND_RIGHT = EntityId(ID_FOOT_SCALD_HOOF_HIND_RIGHT_RAW)

        const val ID_HORN_QUALITY_GOOD_ALL_RAW = "b6d6f521-c15c-443e-a62e-84ec8eea4b54" //LEGACY ID = 32
        const val ID_HORN_QUALITY_BAD_ALL_RAW = "0fd32af5-31f7-4858-b557-8dac871026de" //LEGACY ID = 33
        const val ID_HORN_QUALITY_BAD_LEFT_RAW = "2d843148-bb71-494f-8845-5bbe91112848" //LEGACY ID = 12
        const val ID_HORN_QUALITY_BAD_RIGHT_RAW = "982b1a17-394d-4db0-b2d0-2b9b78fec148" //LEGACY ID = 11

        val ID_HORN_QUALITY_GOOD_ALL = EntityId(ID_HORN_QUALITY_GOOD_ALL_RAW)
        val ID_HORN_QUALITY_BAD_ALL = EntityId(ID_HORN_QUALITY_BAD_ALL_RAW)
        val ID_HORN_QUALITY_BAD_LEFT = EntityId(ID_HORN_QUALITY_BAD_LEFT_RAW)
        val ID_HORN_QUALITY_BAD_RIGHT = EntityId(ID_HORN_QUALITY_BAD_RIGHT_RAW)

        const val ID_HORN_SAWED_LEFT_RAW = "ec49680e-b3f1-432e-93ef-3206e44c3a61" //LEGACY ID = 10
        const val ID_HORN_SAWED_RIGHT_RAW = "c121adae-2e80-4d77-81cb-0c41be09be8f" //LEGACY ID = 9

        val ID_HORN_SAWED_LEFT = EntityId(ID_HORN_SAWED_LEFT_RAW)
        val ID_HORN_SAWED_RIGHT = EntityId(ID_HORN_SAWED_RIGHT_RAW)

        const val ID_SHORN_RAW = "d99a7bef-ba7d-4b36-bdb6-149766d62b6e" //LEGACY ID = 23
        const val ID_SHOD_RAW = "c6369416-cf40-4bdc-9a1e-604ff40c5f1c" //LEGACY ID = 152
        const val ID_WEANED_RAW = "a0783def-fa45-47a9-bf6b-845d646aae06" //LEGACY ID = 157

        val ID_SHORN = EntityId(ID_SHORN_RAW)
        val ID_SHOD = EntityId(ID_SHOD_RAW)
        val ID_WEANED = EntityId(ID_WEANED_RAW)
    }
}
