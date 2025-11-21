package com.weyr_associates.animaltrakkerfarmmobile.app.main.menu

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.breeding.DetailedLambingActivity
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.evaluation.simple.SimpleEvaluationActivity
import com.weyr_associates.animaltrakkerfarmmobile.app.core.checkDatabaseValidityThen
import com.weyr_associates.animaltrakkerfarmmobile.databinding.FragmentMenuBirthingBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.SavedEvaluation

class BirthingMenuFragment : Fragment(R.layout.fragment_menu_birthing) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(FragmentMenuBirthingBinding.bind(view)) {
            btnSimpleLambing.setOnClickListener {
                checkDatabaseValidityThen {
                    startActivity(
                        SimpleEvaluationActivity.newIntent(
                            requireContext(),
                            SavedEvaluation.ID_SIMPLE_LAMBING
                        )
                    )
                }
            }
            btnDetailedLambing.setOnClickListener {
                checkDatabaseValidityThen {
                    startActivity(
                        DetailedLambingActivity.newIntent(
                            requireContext()
                        )
                    )
                }
            }
            btnSimpleBirths.setOnClickListener {
                checkDatabaseValidityThen {
                    startActivity(
                        SimpleEvaluationActivity.newIntent(
                            requireContext(),
                            SavedEvaluation.ID_SIMPLE_BIRTHS
                        )
                    )
                }
            }
            listOf(
                btnAddFemaleBirthingHistory
            ).forEach { it.deactivate() }
        }
    }
}