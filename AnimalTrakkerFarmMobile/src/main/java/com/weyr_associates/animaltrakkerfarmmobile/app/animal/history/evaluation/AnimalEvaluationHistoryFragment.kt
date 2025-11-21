package com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.evaluation

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.collectLatestOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.text.NumericOrder
import com.weyr_associates.animaltrakkerfarmmobile.app.core.text.styledBold
import com.weyr_associates.animaltrakkerfarmmobile.app.core.viewBinding
import com.weyr_associates.animaltrakkerfarmmobile.app.model.formatForDisplay
import com.weyr_associates.animaltrakkerfarmmobile.app.model.itemCallbackUsingOnlyIdentity
import com.weyr_associates.animaltrakkerfarmmobile.databinding.FragmentAnimalEvaluationHistoryBinding
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ItemAnimalEvalTraitBinding
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ItemAnimalEvaluationBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalEvaluation
import kotlinx.coroutines.flow.StateFlow

interface AnimalEvaluationsHistoryViewModelContract {
    val animalEvaluationsHistory: StateFlow<List<AnimalEvaluation>?>
}

abstract class AnimalEvaluationHistoryFragment : Fragment(R.layout.fragment_animal_evaluation_history) {

    protected abstract val viewModel: AnimalEvaluationsHistoryViewModelContract

    private val binding by viewBinding<FragmentAnimalEvaluationHistoryBinding>()
    private val evaluationsAdapter = AnimalEvaluationsAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.recyclerAnimalEvals) {
            adapter = evaluationsAdapter
            layoutManager = LinearLayoutManager(
                view.context,
                RecyclerView.VERTICAL,
                false
            )
            addItemDecoration(DividerItemDecoration(view.context, DividerItemDecoration.VERTICAL))
        }
        collectLatestOnStart(viewModel.animalEvaluationsHistory) { animalEvaluations ->
            evaluationsAdapter.submitList(animalEvaluations) {
                updateDisplay(hasList = animalEvaluations != null)
            }
        }
    }

    private fun updateDisplay(hasList: Boolean) {
        binding.recyclerAnimalEvals.isVisible = hasList && evaluationsAdapter.currentList.isNotEmpty()
        binding.textNoEvalsFound.isVisible = hasList && evaluationsAdapter.currentList.isEmpty()
    }

    private class AnimalEvaluationsAdapter : ListAdapter<AnimalEvaluation, AnimalEvaluationViewHolder>(
        itemCallbackUsingOnlyIdentity()
    ) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimalEvaluationViewHolder {
            return AnimalEvaluationViewHolder(
                ItemAnimalEvaluationBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: AnimalEvaluationViewHolder, position: Int) {
            holder.bind(currentList[position])
        }
    }

    private class AnimalEvaluationViewHolder(
        private val binding: ItemAnimalEvaluationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val traitItemViewCache = mutableListOf<ItemAnimalEvalTraitBinding>()
        private val layoutInflater = LayoutInflater.from(binding.root.context)

        fun bind(animalEval: AnimalEvaluation) {
            binding.textEvalDate.text = animalEval.evalDate.formatForDisplay()
            binding.textEvalTime.text = animalEval.evalTime.formatForDisplay()
            updateTraitsDisplay(animalEval)
        }

        private fun updateTraitsDisplay(animalEval: AnimalEvaluation) {
            val animalRank = animalEval.animalRank
            val numRequiredTraitViews = animalEval.traits.size +
                    if (animalRank != null) 1 else 0
            while (traitItemViewCache.size < numRequiredTraitViews) {
                traitItemViewCache.add(
                    ItemAnimalEvalTraitBinding.inflate(
                        layoutInflater, binding.containerEvalTraits, true
                    )
                )
            }
            traitItemViewCache.forEachIndexed { index, traitItemViewBinding ->
                val evalTrait = index.takeIf { it < (animalEval.traits.size) }
                    ?.let { animalEval.traits[index] }
                if (evalTrait != null) {
                    traitItemViewBinding.bind(evalTrait)
                } else if (animalRank != null && index == animalEval.traits.size) {

                    traitItemViewBinding.bind(animalRank.rank, animalRank.numberRanked)
                } else {
                    traitItemViewBinding.collapse()
                }
            }
        }

        fun ItemAnimalEvalTraitBinding.bind(rank: Int, numberRanked: Int) {
            textEvalTraitTitle.text = itemView.context.getString(R.string.text_ranking).styledBold()
            @SuppressLint("SetTextI18n")
            textEvalTraitSummary.text = "${rank}${NumericOrder.suffixForOrdinal(itemView.context, rank)} of $numberRanked".styledBold()
            root.isVisible = true
        }

        private fun ItemAnimalEvalTraitBinding.bind(entry: AnimalEvaluation.Entry) {
            when (entry) {
                is AnimalEvaluation.ScoreEntry -> bindScoreEntry(entry)
                is AnimalEvaluation.UnitsEntry -> bindUnitsEntry(entry)
                is AnimalEvaluation.OptionEntry -> bindOptionEntry(entry)
            }
        }

        private fun ItemAnimalEvalTraitBinding.bindScoreEntry(entry: AnimalEvaluation.ScoreEntry) {
            textEvalTraitTitle.text = entry.traitName
            textEvalTraitSummary.text = entry.traitScore.toString()
            root.isVisible = true
        }

        private fun ItemAnimalEvalTraitBinding.bindUnitsEntry(entry: AnimalEvaluation.UnitsEntry) {
            @SuppressLint("DefaultLocale")
            val summary = "${String.format("%.2f", entry.traitScore)} ${entry.unitsAbbreviation}"
            textEvalTraitTitle.text= entry.traitName
            textEvalTraitSummary.text = summary
            root.isVisible = true
        }

        private fun ItemAnimalEvalTraitBinding.bindOptionEntry(entry: AnimalEvaluation.OptionEntry) {
            textEvalTraitTitle.text = entry.traitName
            textEvalTraitSummary.text = entry.optionName
            root.isVisible = true
        }

        private fun ItemAnimalEvalTraitBinding.collapse() {
            textEvalTraitTitle.text = ""
            textEvalTraitSummary.text = ""
            root.isGone = true
        }
    }
}
