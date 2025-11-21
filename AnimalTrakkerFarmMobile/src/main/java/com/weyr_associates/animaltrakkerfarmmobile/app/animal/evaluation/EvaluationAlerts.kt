package com.weyr_associates.animaltrakkerfarmmobile.app.animal.evaluation

import android.annotation.SuppressLint
import com.weyr_associates.animaltrakkerfarmmobile.model.BasicEvalTrait
import com.weyr_associates.animaltrakkerfarmmobile.model.CustomEvalTrait
import com.weyr_associates.animaltrakkerfarmmobile.model.EvalTrait
import com.weyr_associates.animaltrakkerfarmmobile.model.EvaluationSummary
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.SavedEvaluation
import com.weyr_associates.animaltrakkerfarmmobile.model.UnitsEvalTrait

object EvaluationAlerts {

    fun evaluationSummaryFor(
        animalEvaluationId: EntityId,
        evaluation: SavedEvaluation,
        evaluationEntries: EvaluationEntries
    ): EvaluationSummary? {
        if (!evaluation.summarizeInAlert) return null
        val traits = mutableListOf<EvaluationSummary.Trait>().apply {
            addTraitFor(evaluation.trait01, evaluationEntries.trait01Score)
            addTraitFor(evaluation.trait02, evaluationEntries.trait02Score)
            addTraitFor(evaluation.trait03, evaluationEntries.trait03Score)
            addTraitFor(evaluation.trait04, evaluationEntries.trait04Score)
            addTraitFor(evaluation.trait05, evaluationEntries.trait05Score)
            addTraitFor(evaluation.trait06, evaluationEntries.trait06Score)
            addTraitFor(evaluation.trait07, evaluationEntries.trait07Score)
            addTraitFor(evaluation.trait08, evaluationEntries.trait08Score)
            addTraitFor(evaluation.trait09, evaluationEntries.trait09Score)
            addTraitFor(evaluation.trait10, evaluationEntries.trait10Score)
            addTraitFor(evaluation.trait11, evaluationEntries.trait11Score)
            addTraitFor(evaluation.trait12, evaluationEntries.trait12Score)
            addTraitFor(evaluation.trait13, evaluationEntries.trait13Score)
            addTraitFor(evaluation.trait14, evaluationEntries.trait14Score)
            addTraitFor(evaluation.trait15, evaluationEntries.trait15Score)
            addTraitFor(evaluation.trait16, evaluationEntries.trait16OptionId)
            addTraitFor(evaluation.trait17, evaluationEntries.trait17OptionId)
            addTraitFor(evaluation.trait18, evaluationEntries.trait18OptionId)
            addTraitFor(evaluation.trait19, evaluationEntries.trait19OptionId)
            addTraitFor(evaluation.trait20, evaluationEntries.trait20OptionId)
        }
        return EvaluationSummary(
            animalEvaluationId = animalEvaluationId,
            evaluationId = evaluation.id,
            evaluationName = evaluation.name,
            evaluationTraits = traits
        )
    }

    fun alertForEvaluationSummary(evaluationSummary: EvaluationSummary): String {
        return when (evaluationSummary.evaluationId){
            SavedEvaluation.ID_SIMPLE_SORT -> {
                contentForSimpleSort(evaluationSummary)
            }
            SavedEvaluation.ID_SIMPLE_LAMBING -> {
                contentForSimpleLambing(evaluationSummary)
            }
            else -> contentForEvaluationSummary(evaluationSummary)
        }
    }

    private fun contentForSimpleSort(evaluationSummary: EvaluationSummary): String {
        return evaluationSummary.evaluationTraits.extractTraitValueFor(
            EvalTrait.OPTION_TRAIT_ID_SIMPLE_SORT
        ) ?: "Unknown Sort"
    }

    private fun contentForSimpleLambing(evaluationSummary: EvaluationSummary): String {
        return with(evaluationSummary.evaluationTraits) {

            val numWetherLambs = extractTraitValueFor(EvalTrait.TRAIT_ID_WETHER_LAMBS_BORN) ?: ""
            val numEweLambs = extractTraitValueFor(EvalTrait.TRAIT_ID_EWE_LAMBS_BORN) ?: ""
            val numRamLambs = extractTraitValueFor(EvalTrait.TRAIT_ID_RAM_LAMBS_BORN) ?: ""
            val numUnkSexLambs = extractTraitValueFor(EvalTrait.TRAIT_ID_UNK_SEX_LAMBS_BORN) ?: ""
            val numStillbornLambs = extractTraitValueFor(EvalTrait.TRAIT_ID_STILL_BORN_LAMBS_BORN) ?: ""
            val numAbortedLambs = extractTraitValueFor(EvalTrait.TRAIT_ID_ABORTED_LAMBS) ?: ""
            val numAdoptedLambs = extractTraitValueFor(EvalTrait.TRAIT_ID_ADOPTED_LAMBS) ?: ""

            buildString {
                appendLine("Lambed Already")
                appendLine("$numWetherLambs Wether Lambs")
                appendLine("$numEweLambs Ewe Lambs")
                appendLine("$numRamLambs Ram Lambs")
                appendLine("$numUnkSexLambs Unknown Sex Lambs")
                appendLine("$numStillbornLambs Stillborn Lambs")
                appendLine("$numAbortedLambs Aborted Lambs")
                append("$numAdoptedLambs Adopted Lambs")
            }
        }
    }

    private fun contentForEvaluationSummary(evaluationSummary: EvaluationSummary): String {
        return buildString {
            appendLine("-- ${evaluationSummary.evaluationName} --")
            evaluationSummary.evaluationTraits.forEachIndexed { index, trait ->
                if (index < evaluationSummary.evaluationTraits.size - 1) {
                    appendLine("${trait.name}: ${trait.value}")
                } else {
                    append("${trait.name}: ${trait.value}")
                }
            }
        }
    }

    private fun MutableList<EvaluationSummary.Trait>.addTraitFor(basicEvalTrait: BasicEvalTrait?, traitScore: Int?) {
        if (basicEvalTrait != null && traitScore != null && shouldWriteAlertLineItem(basicEvalTrait)) {
            add(
                EvaluationSummary.Trait(
                    basicEvalTrait.id,
                    basicEvalTrait.typeId,
                    basicEvalTrait.name,
                    traitScore.toString()
                )
            )
        }
    }

    @SuppressLint("DefaultLocale")
    private fun MutableList<EvaluationSummary.Trait>.addTraitFor(unitsEvalTrait: UnitsEvalTrait?, traitScore: Float?) {
        if (unitsEvalTrait != null && traitScore != null && shouldWriteAlertLineItem(unitsEvalTrait)) {
            add(
                EvaluationSummary.Trait(
                    unitsEvalTrait.id,
                    unitsEvalTrait.typeId,
                    unitsEvalTrait.name,
                    "${String.format("%.2f", traitScore)} ${unitsEvalTrait.units.abbreviation}"
                )
            )
        }
    }

    private fun MutableList<EvaluationSummary.Trait>.addTraitFor(optionEvalTrait: CustomEvalTrait?, traitOptionId: EntityId?) {
        if (optionEvalTrait != null && traitOptionId != null && shouldWriteAlertLineItem(optionEvalTrait)) {
            add(
                EvaluationSummary.Trait(
                    optionEvalTrait.id,
                    optionEvalTrait.typeId,
                    optionEvalTrait.name,
                    optionEvalTrait.options.firstOrNull { it.id == traitOptionId }?.name
                        ?: "OptionId=$traitOptionId"
                )
            )
        }
    }

    private fun shouldWriteAlertLineItem(evalTrait: EvalTrait): Boolean =
        !evalTrait.isEmpty && !evalTrait.isDeferred

    private fun List<EvaluationSummary.Trait>.extractTraitValueFor(traitId: EntityId): String? {
        return firstOrNull { it.id == traitId }?.value
    }
}
