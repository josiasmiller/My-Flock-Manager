package com.weyr_associates.animaltrakkerfarmmobile.app.main.menu

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.evaluation.configuration.ConfigureEvaluationActivity
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.evaluation.optimalag.OptAgRamBreedingSoundnessActivity
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.evaluation.simple.SimpleEvaluationActivity
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.evaluation.tissue.TissueSampleActivity
import com.weyr_associates.animaltrakkerfarmmobile.app.core.checkDatabaseValidityThen
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.selectedItem
import com.weyr_associates.animaltrakkerfarmmobile.app.select.SelectEvaluationDialogFragment
import com.weyr_associates.animaltrakkerfarmmobile.databinding.FragmentMenuAnimalEvaluationBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.ItemEntry
import com.weyr_associates.animaltrakkerfarmmobile.model.SavedEvaluation

class AnimalEvaluationMenuFragment : Fragment(R.layout.fragment_menu_animal_evaluation) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        childFragmentManager.setFragmentResultListener(
            SelectEvaluationDialogFragment.REQUEST_KEY_SELECT_ANIMAL_EVALUTION, this
        ) { requestKey, result ->
            if (requestKey == SelectEvaluationDialogFragment.REQUEST_KEY_SELECT_ANIMAL_EVALUTION) {
                val selectEvalItem = result.selectedItem<ItemEntry>()
                startActivity(
                    SimpleEvaluationActivity.newIntent(
                        requireContext(),
                        selectEvalItem.id,
                        allowLoadEvaluation = true
                    )
                )
            }
        }
        with(FragmentMenuAnimalEvaluationBinding.bind(view)) {
            btnTakeTissueSample.setOnClickListener {
                checkDatabaseValidityThen {
                    startActivity(Intent(requireActivity(), TissueSampleActivity::class.java))
                }
            }
            btnOptimalAgRamBse.setOnClickListener {
                checkDatabaseValidityThen {
                    startActivity(Intent(requireActivity(), OptAgRamBreedingSoundnessActivity::class.java))
                }
            }
            btnOptimalAgEweUltrasound.setOnClickListener {
                startActivity(
                    SimpleEvaluationActivity.newIntent(
                        requireContext(),
                        SavedEvaluation.ID_OPTIMAL_LIVESTOCK_EWE_ULTRASOUND
                    )
                )
            }
            btnEvaluateAnimal.setOnClickListener {
                checkDatabaseValidityThen {
                    SelectEvaluationDialogFragment.newInstance()
                        .show(childFragmentManager, "TAG_FRAGMENT_SELECT_EVALUATION")
                }
            }
            btnCreateEvaluation.setOnClickListener {
                checkDatabaseValidityThen {
                    startActivity(Intent(requireContext(), ConfigureEvaluationActivity::class.java))
                }
            }
            listOf(
                btnMaleBreedingSoundness
            ).forEach { it.deactivate() }
        }
    }
}
