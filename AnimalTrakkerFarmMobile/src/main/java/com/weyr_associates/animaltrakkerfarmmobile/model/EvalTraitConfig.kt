package com.weyr_associates.animaltrakkerfarmmobile.model

data class EvalTraitConfig(
    val trait: Trait? = null,
    val units: UnitOfMeasure? = null,
    val isOptional: Boolean = false,
    val isDeferred: Boolean = false
) {
    val isConfigurationStarted by lazy {
        trait != null || units != null || isOptional || isDeferred
    }

    val isConfigurationComplete by lazy {
        trait != null && (trait.typeId != Trait.TYPE_ID_UNIT || units != null)
    }
}
