package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Trait(
    override val id: EntityId,
    override val name: String,
    val typeId: EntityId,
    val unitsTypeId: EntityId?,
    val order: Int
) : Parcelable, HasIdentity, HasName {
    companion object {

        const val TYPE_ID_BASIC_RAW = "79f17211-ea5d-44a5-a462-8607a5743b08" //LEGACY ID = 1
        const val TYPE_ID_UNIT_RAW = "7079e750-9fc2-41e1-b46c-8505cdb8e67f" //LEGACY ID = 2
        const val TYPE_ID_OPTION_RAW = "abfb56ad-52a4-4e3e-99f5-19e8582f228f" //LEGACY ID = 3

        val TYPE_ID_BASIC = EntityId(TYPE_ID_BASIC_RAW)
        val TYPE_ID_UNIT = EntityId(TYPE_ID_UNIT_RAW)
        val TYPE_ID_OPTION = EntityId(TYPE_ID_OPTION_RAW)
    }
}
