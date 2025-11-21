package com.weyr_associates.animaltrakkerfarmmobile.app.main.menu

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.evaluation.simple.SimpleEvaluationActivity
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.premise.MoveToPremiseActivity
import com.weyr_associates.animaltrakkerfarmmobile.app.core.checkDatabaseValidityThen
import com.weyr_associates.animaltrakkerfarmmobile.databinding.FragmentMenuAnimalMovementsBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.SavedEvaluation

class AnimalMovementsMenuFragment : Fragment(R.layout.fragment_menu_animal_movements) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(FragmentMenuAnimalMovementsBinding.bind(view)) {
            btnSimpleSort.setOnClickListener {
                checkDatabaseValidityThen {
                    startActivity(
                        SimpleEvaluationActivity.newIntent(
                            requireContext(),
                            SavedEvaluation.ID_SIMPLE_SORT
                        )
                    )
                }
            }
            btnChangePremise.setOnClickListener {
                checkDatabaseValidityThen {
                    startActivity(
                        MoveToPremiseActivity.newIntent(
                            requireContext()
                        )
                    )
                }
            }
            listOf(
                btnBuyAnimals,
                btnSortEwesBreeding,
                btnSellAnimals
            ).forEach { it.deactivate() }
        }
    }
}
