package com.weyr_associates.animaltrakkerfarmmobile.app.main.menu

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.AnimalActionsActivity
import com.weyr_associates.animaltrakkerfarmmobile.app.core.checkDatabaseValidityThen
import com.weyr_associates.animaltrakkerfarmmobile.databinding.FragmentMenuAnimalCareBinding

class AnimalCareMenuFragment : Fragment(R.layout.fragment_menu_animal_care) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(FragmentMenuAnimalCareBinding.bind(view)) {
            btnAdministerDrugs.setOnClickListener {
                checkDatabaseValidityThen {
                    startActivity(
                        AnimalActionsActivity.newIntentToAdministerDrugs(requireContext())
                    )
                }
            }
            btnVaccinateDeworm.setOnClickListener {
                checkDatabaseValidityThen {
                    startActivity(
                        AnimalActionsActivity.newIntentToVaccinateAndDeworm(requireContext())
                    )
                }
            }
            btnGeneralAnimalCare.setOnClickListener {
                checkDatabaseValidityThen {
                    startActivity(
                        AnimalActionsActivity.newIntentForGeneralAnimalCare(requireContext())
                    )
                }
            }
            listOf(
                btnGroupVaccinateDeworm,
                btnGroupGeneralAnimalCare
            ).forEach { it.deactivate() }
        }
    }
}
