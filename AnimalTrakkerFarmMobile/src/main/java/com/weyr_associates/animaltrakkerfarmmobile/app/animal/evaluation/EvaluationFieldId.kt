package com.weyr_associates.animaltrakkerfarmmobile.app.animal.evaluation

enum class EvaluationFieldId {
    TRAIT_01,
    TRAIT_02,
    TRAIT_03,
    TRAIT_04,
    TRAIT_05,
    TRAIT_06,
    TRAIT_07,
    TRAIT_08,
    TRAIT_09,
    TRAIT_10,
    TRAIT_11,
    TRAIT_12,
    TRAIT_13,
    TRAIT_14,
    TRAIT_15,
    TRAIT_16,
    TRAIT_17,
    TRAIT_18,
    TRAIT_19,
    TRAIT_20;

    companion object {
        fun optValueOf(name: String): EvaluationFieldId? {
            return try { valueOf(name) }
            catch (ex: IllegalArgumentException) { null }
        }
    }
}
