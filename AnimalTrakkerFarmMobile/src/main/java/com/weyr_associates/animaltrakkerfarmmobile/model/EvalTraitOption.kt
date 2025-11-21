package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EvalTraitOption(
    override val id: EntityId,
    val traitId: EntityId,
    override val name: String,
    val order: Int = Int.MAX_VALUE
) : Parcelable, HasIdentity, HasName {
    companion object {

        const val ID_SUCK_REFLEX_UNTESTED_RAW = "ff497d06-c288-4978-b75b-5862451796c5" //LEGACY ID = 24
        const val ID_LAMB_EASE_UNASSISTED_RAW = "fa4b9e07-68a1-449f-bc19-a69a7a3bd30d" //LEGACY ID = 9

        const val ID_SIMPLE_SORT_KEEP_RAW = "aac8b648-ceac-451b-87ee-69582e778af3" //LEGACY ID = 40
        const val ID_SIMPLE_SORT_SHIP_RAW = "13c72929-b2fb-4873-b95f-78d689deffb6" //LEGACY ID = 41
        const val ID_SIMPLE_SORT_CULL_RAW = "11459d3b-a712-4e31-bc80-0acc85301bcf" //LEGACY ID = 42
        const val ID_SIMPLE_SORT_OTHER_RAW = "3aafef14-46f5-4f92-949a-57110101d19f" //LEGACY ID = 43

        const val ID_PREGNANCY_STATUS_PREGNANT_RAW = "62da608e-47fc-43e3-a40d-cd11b8fa50bd" //LEGACY ID 5

        const val ID_CUSTOM_SCROTAL_PALPATION_SATISFACTORY_RAW = "e8f24d07-50e9-448f-a44d-95ca2656f679" //LEGACY ID = 44

        val ID_SUCK_REFLEX_UNTESTED = EntityId(ID_SUCK_REFLEX_UNTESTED_RAW)
        val ID_LAMB_EASE_UNASSISTED = EntityId(ID_LAMB_EASE_UNASSISTED_RAW)

        val ID_SIMPLE_SORT_KEEP = EntityId(ID_SIMPLE_SORT_KEEP_RAW)
        val ID_SIMPLE_SORT_SHIP = EntityId(ID_SIMPLE_SORT_SHIP_RAW)
        val ID_SIMPLE_SORT_CULL = EntityId(ID_SIMPLE_SORT_CULL_RAW)
        val ID_SIMPLE_SORT_OTHER = EntityId(ID_SIMPLE_SORT_OTHER_RAW)

        val ID_PREGNANCY_STATUS_PREGNANT = EntityId(ID_PREGNANCY_STATUS_PREGNANT_RAW)

        val ID_CUSTOM_SCROTAL_PALPATION_SATISFACTORY = EntityId(ID_CUSTOM_SCROTAL_PALPATION_SATISFACTORY_RAW)
    }
}
