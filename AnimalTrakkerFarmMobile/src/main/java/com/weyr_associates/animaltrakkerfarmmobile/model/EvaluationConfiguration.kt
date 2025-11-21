package com.weyr_associates.animaltrakkerfarmmobile.model

data class EvaluationConfiguration(
    val name: String = "",
    val saveSummaryAsAlert: Boolean = false,
    val trait01: EvalTraitConfig = EvalTraitConfig(),
    val trait02: EvalTraitConfig = EvalTraitConfig(),
    val trait03: EvalTraitConfig = EvalTraitConfig(),
    val trait04: EvalTraitConfig = EvalTraitConfig(),
    val trait05: EvalTraitConfig = EvalTraitConfig(),
    val trait06: EvalTraitConfig = EvalTraitConfig(),
    val trait07: EvalTraitConfig = EvalTraitConfig(),
    val trait08: EvalTraitConfig = EvalTraitConfig(),
    val trait09: EvalTraitConfig = EvalTraitConfig(),
    val trait10: EvalTraitConfig = EvalTraitConfig(),
    val trait11: EvalTraitConfig = EvalTraitConfig(),
    val trait12: EvalTraitConfig = EvalTraitConfig(),
    val trait13: EvalTraitConfig = EvalTraitConfig(),
    val trait14: EvalTraitConfig = EvalTraitConfig(),
    val trait15: EvalTraitConfig = EvalTraitConfig(),
    val trait16: EvalTraitConfig = EvalTraitConfig(),
    val trait17: EvalTraitConfig = EvalTraitConfig(),
    val trait18: EvalTraitConfig = EvalTraitConfig(),
    val trait19: EvalTraitConfig = EvalTraitConfig(),
    val trait20: EvalTraitConfig = EvalTraitConfig()
) {
    private val allTraits = listOf(
        trait01, trait02, trait03, trait04, trait05,
        trait06, trait07, trait08, trait09, trait10,
        trait11, trait12, trait13, trait14, trait15,
        trait16, trait17, trait18, trait19, trait20
    )

    val isConfigurationStarted by lazy {
        name.isNotEmpty() || saveSummaryAsAlert || allTraits.any { it.isConfigurationStarted }
    }

    val isConfigurationComplete by lazy {
        name.isNotBlank() && allTraits.any { it.isConfigurationComplete }
                && allTraits.filter { it.isConfigurationComplete }.any { !it.isDeferred }
    }
}
