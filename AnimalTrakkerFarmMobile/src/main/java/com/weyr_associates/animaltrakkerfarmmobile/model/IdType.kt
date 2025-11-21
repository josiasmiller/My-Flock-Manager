package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class IdType(
    override val id: EntityId,
    override val name: String,
    override val abbreviation: String,
    val order: Int
) : Parcelable, HasIdentity, HasName, HasAbbreviation {

    companion object {

        const val ID_TYPE_ID_FED_RAW = "d13a17a0-33c9-4590-850c-63a07d504993" //LEGACY ID = 1
        const val ID_TYPE_ID_NAME_RAW = "1e57ea5b-b388-4b64-8d75-2ed87be998bd" //LEGACY ID = 8
        const val ID_TYPE_ID_EID_RAW = "50f1c64f-e56e-420e-8150-9347fe51c0c1" //LEGACY ID = 2
        const val ID_TYPE_ID_PAINT_RAW = "7255aaf5-b62f-4ff9-a7b3-78cf28e2b0e2" //LEGACY ID = 3
        const val ID_TYPE_ID_FARM_RAW = "6af3845e-0abc-4afa-bcb4-4eea96f2ecc2" //LEGACY ID = 4
        const val ID_TYPE_ID_TATTOO_RAW = "1f66a23f-8d3e-4a3c-8df5-6ba185df5a04" //LEGACY ID = 5
        const val ID_TYPE_ID_SPLIT_RAW = "e63c8c31-d186-4220-9756-9630a95773bc" //LEGACY ID = 6
        const val ID_TYPE_ID_NOTCH_RAW = "175ebcc9-b142-438d-8e2c-474b0945ea8d" //LEGACY ID = 7
        const val ID_TYPE_ID_FREEZE_BRAND_RAW = "e3c57ebe-6310-448f-9327-8eb0412bf0cc" //LEGACY ID = 9
        const val ID_TYPE_ID_TRICH_RAW = "59bef26c-ee6b-4dfa-9068-7fd45588fb75" //LEGACY ID = 10
        const val ID_TYPE_ID_NUES_RAW = "98cc5d2e-b8cc-4b6f-a051-fb5078ee9ce5" //LEGACY ID = 11
        const val ID_TYPE_ID_SALE_ORDER_RAW = "cd6e881f-b6e9-4339-816d-a8ed1a3be1c9" //LEGACY ID = 12
        const val ID_TYPE_ID_BANGS_RAW = "d0c59f03-78ee-4c11-9cb2-232406d1ecaf" //LEGACY ID = 13
        const val ID_TYPE_ID_UNKNOWN_RAW = "d8519b7a-be8d-4151-b0eb-6740ff015089" //LEGACY ID = 14
        const val ID_TYPE_ID_CARCASS_TAG_RAW = "9e5a4712-224b-4cff-ae07-c6b8d6ef7b3f" //LEGACY ID = 15
        const val ID_TYPE_ID_FED_CANADIAN_RAW = "3d2795d9-d0bf-46e6-8cef-3ff23bd0a7e1" //LEGACY ID = 16

        val ID_TYPE_ID_FED = EntityId(ID_TYPE_ID_FED_RAW)
        val ID_TYPE_ID_EID = EntityId(ID_TYPE_ID_EID_RAW)
        val ID_TYPE_ID_PAINT = EntityId(ID_TYPE_ID_PAINT_RAW)
        val ID_TYPE_ID_FARM = EntityId(ID_TYPE_ID_FARM_RAW)
        val ID_TYPE_ID_TATTOO = EntityId(ID_TYPE_ID_TATTOO_RAW)
        val ID_TYPE_ID_SPLIT = EntityId(ID_TYPE_ID_SPLIT_RAW)
        val ID_TYPE_ID_NOTCH = EntityId(ID_TYPE_ID_NOTCH_RAW)
        val ID_TYPE_ID_NAME = EntityId(ID_TYPE_ID_NAME_RAW)
        val ID_TYPE_ID_FREEZE_BRAND = EntityId(ID_TYPE_ID_FREEZE_BRAND_RAW)
        val ID_TYPE_ID_TRICH = EntityId(ID_TYPE_ID_TRICH_RAW)
        val ID_TYPE_ID_NUES = EntityId(ID_TYPE_ID_NUES_RAW)
        val ID_TYPE_ID_SALE_ORDER = EntityId(ID_TYPE_ID_SALE_ORDER_RAW)
        val ID_TYPE_ID_BANGS = EntityId(ID_TYPE_ID_BANGS_RAW)
        val ID_TYPE_ID_UNKNOWN = EntityId(ID_TYPE_ID_UNKNOWN_RAW)
        val ID_TYPE_ID_CARCASS_TAG = EntityId(ID_TYPE_ID_CARCASS_TAG_RAW)
        val ID_TYPE_ID_FED_CANADIAN = EntityId(ID_TYPE_ID_FED_CANADIAN_RAW)

        fun isOfficialId(id: EntityId): Boolean = when (id) {
            ID_TYPE_ID_FED,
            ID_TYPE_ID_NUES,
            ID_TYPE_ID_BANGS -> true
            else -> false
        }
    }
}
