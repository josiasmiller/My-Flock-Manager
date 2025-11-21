package com.weyr_associates.animaltrakkerfarmmobile.model

sealed interface EvalTrait {
    val id: EntityId
    val name: String
    val typeId: EntityId
    val order: Int
    val isEmpty: Boolean
    val isOptional: Boolean
    val isDeferred: Boolean

    companion object {

        const val TRAIT_ID_LAMB_EASE_RAW = "5e372585-ce1d-4bb6-893a-c765b915b882" //LEGACY ID = 24
        const val TRAIT_ID_SUCK_REFLEX_RAW = "c00505e5-9d8b-48d6-b6fd-84cdb3c65127" //LEGACY ID = 37
        const val TRAIT_ID_RAM_LAMBS_BORN_RAW = "968eae00-3c84-43bd-ab91-ce19246c2f79" //LEGACY ID = 68
        const val TRAIT_ID_WETHER_LAMBS_BORN_RAW = "bd0925b6-bf1d-4ac8-a275-7ba6da122d41" //LEGACY ID = 69
        const val TRAIT_ID_EWE_LAMBS_BORN_RAW = "923ee982-d1b4-4968-a9f0-9aed7126abc2" //LEGACY ID = 70
        const val TRAIT_ID_UNK_SEX_LAMBS_BORN_RAW = "601f910c-33fa-4894-87ae-9eff9f7d4b9e" //LEGACY ID = 71
        const val TRAIT_ID_STILL_BORN_LAMBS_BORN_RAW = "5f6f1a22-756c-4b12-8be3-4e2fd7d17fb5" //LEGACY ID = 72
        const val TRAIT_ID_ABORTED_LAMBS_RAW = "8644a896-c70c-463e-a3bd-12e7ae0e0a38" //LEGACY ID = 73
        const val TRAIT_ID_ADOPTED_LAMBS_RAW = "2b5e739c-06c8-4e24-915f-ba2484ffaa8e" //LEGACY ID = 74
        const val UNIT_TRAIT_ID_SCROTAL_CIRCUMFERENCE_RAW = "be459431-f1b7-4c99-a044-d3baff3e3c46" //LEGACY ID = 15
        const val UNIT_TRAIT_ID_WEIGHT_RAW = "44d307ab-5c32-44c7-bb06-e65c11269716" //LEGACY ID = 16
        const val UNIT_TRAIT_ID_BODY_CONDITION_SCORE_RAW = "b5865a35-f213-4b8a-9077-5472cd8a25b3" //LEGACY ID = 51
        const val OPTION_TRAIT_ID_SIMPLE_SORT_RAW = "a11275e7-0eeb-4e9f-b7dd-2adbb361ccfb" //LEGACY ID = 54
        const val OPTION_TRAIT_ID_PREGNANCY_STATUS_RAW = "72a721eb-9738-4023-b910-93af6e33386d" //LEGACY ID = 22
        const val OPTION_TRAIT_ID_CUSTOM_SCROTAL_PALPATION_RAW = "0c96e6f7-eb2e-406f-b639-d7d505f7ee3a" //LEGACY ID = 80

        val TRAIT_ID_LAMB_EASE = EntityId(TRAIT_ID_LAMB_EASE_RAW)
        val TRAIT_ID_SUCK_REFLEX = EntityId(TRAIT_ID_SUCK_REFLEX_RAW)
        val TRAIT_ID_RAM_LAMBS_BORN = EntityId(TRAIT_ID_RAM_LAMBS_BORN_RAW)
        val TRAIT_ID_WETHER_LAMBS_BORN = EntityId(TRAIT_ID_WETHER_LAMBS_BORN_RAW)
        val TRAIT_ID_EWE_LAMBS_BORN = EntityId(TRAIT_ID_EWE_LAMBS_BORN_RAW)
        val TRAIT_ID_UNK_SEX_LAMBS_BORN = EntityId(TRAIT_ID_UNK_SEX_LAMBS_BORN_RAW)
        val TRAIT_ID_STILL_BORN_LAMBS_BORN = EntityId(TRAIT_ID_STILL_BORN_LAMBS_BORN_RAW)
        val TRAIT_ID_ABORTED_LAMBS = EntityId(TRAIT_ID_ABORTED_LAMBS_RAW)
        val TRAIT_ID_ADOPTED_LAMBS = EntityId(TRAIT_ID_ADOPTED_LAMBS_RAW)
        val UNIT_TRAIT_ID_SCROTAL_CIRCUMFERENCE = EntityId(UNIT_TRAIT_ID_SCROTAL_CIRCUMFERENCE_RAW)
        val UNIT_TRAIT_ID_WEIGHT = EntityId(UNIT_TRAIT_ID_WEIGHT_RAW)
        val UNIT_TRAIT_ID_BODY_CONDITION_SCORE = EntityId(UNIT_TRAIT_ID_BODY_CONDITION_SCORE_RAW)
        val OPTION_TRAIT_ID_SIMPLE_SORT = EntityId(OPTION_TRAIT_ID_SIMPLE_SORT_RAW)
        val OPTION_TRAIT_ID_PREGNANCY_STATUS = EntityId(OPTION_TRAIT_ID_PREGNANCY_STATUS_RAW)
        val OPTION_TRAIT_ID_CUSTOM_SCROTAL_PALPATION = EntityId(OPTION_TRAIT_ID_CUSTOM_SCROTAL_PALPATION_RAW)
    }
}

data class BasicEvalTrait(
    override val id: EntityId,
    override val name: String,
    override val typeId: EntityId,
    override val isOptional: Boolean,
    override val isDeferred: Boolean,
    override val isEmpty: Boolean = false,
    override val order: Int = Int.MAX_VALUE,
) : EvalTrait {
    companion object {
        fun from(
            id: EntityId?,
            name: String?,
            typeId: EntityId?,
            isOptional: Boolean?,
            isDeferred: Boolean?,
        ): BasicEvalTrait? {
            return if (
                id == null ||
                name == null ||
                typeId == null ||
                isOptional == null ||
                isDeferred == null
            ) {
                null
            } else {
                BasicEvalTrait(
                    id = id,
                    name = name,
                    typeId = typeId,
                    isOptional = isOptional,
                    isDeferred = isDeferred
                )
            }
        }
    }
}

data class UnitsEvalTrait(
    override val id: EntityId,
    override val name: String,
    override val typeId: EntityId,
    override val isOptional: Boolean,
    override val isDeferred: Boolean,
    val units: UnitOfMeasure,
    override val isEmpty: Boolean = false,
    override val order: Int = Int.MAX_VALUE,
) : EvalTrait {
    companion object {
        val EMPTY = UnitsEvalTrait(
            id = EntityId.UNKNOWN,
            name = "N/A",
            typeId = Trait.TYPE_ID_UNIT,
            isEmpty = true,
            isOptional = false,
            isDeferred = false,
            units = UnitOfMeasure.NONE
        )
        fun from(
            id: EntityId?,
            name: String?,
            typeId: EntityId?,
            isOptional: Boolean?,
            isDeferred: Boolean?,
            units: UnitOfMeasure?
        ): UnitsEvalTrait? {
            return if (
                id == null ||
                name == null ||
                typeId == null ||
                isOptional == null ||
                isDeferred == null ||
                units == null
            ) {
                null
            } else {
                UnitsEvalTrait(
                    id = id,
                    name = name,
                    typeId = typeId,
                    isOptional = isOptional,
                    isDeferred = isDeferred,
                    units = units
                )
            }
        }
    }
}

data class CustomEvalTrait(
    override val id: EntityId,
    override val name: String,
    override val typeId: EntityId,
    override val isOptional: Boolean,
    override val isDeferred: Boolean,
    val options: List<EvalTraitOption>,
    override val isEmpty: Boolean = false,
    override val order: Int = Int.MAX_VALUE
) : EvalTrait {
    companion object {

        fun from(
            id: EntityId?,
            name: String?,
            typeId: EntityId?,
            isOptional: Boolean?,
            isDeferred: Boolean?,
            options: List<EvalTraitOption>?
        ): CustomEvalTrait? {
            return if (
                id == null ||
                name == null ||
                typeId == null ||
                isOptional == null ||
                isDeferred == null ||
                options == null
            ) {
                null
            } else {
                CustomEvalTrait(
                    id = id,
                    name = name,
                    typeId = typeId,
                    isOptional = isOptional,
                    isDeferred = isDeferred,
                    options = options
                )
            }
        }
    }
}
