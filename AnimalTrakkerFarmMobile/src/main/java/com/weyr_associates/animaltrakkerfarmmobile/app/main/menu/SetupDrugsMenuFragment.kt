package com.weyr_associates.animaltrakkerfarmmobile.app.main.menu

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.care.drugs.AddDrugActivity
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.care.drugs.doses.AddDrugDoseActivity
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.care.drugs.doses.offlabel.AddOffLabelDrugDoseActivity
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.care.drugs.lots.AddDrugLotActivity
import com.weyr_associates.animaltrakkerfarmmobile.app.core.checkDatabaseValidityThen
import com.weyr_associates.animaltrakkerfarmmobile.app.core.viewBinding
import com.weyr_associates.animaltrakkerfarmmobile.databinding.FragmentMenuSetupDrugsBinding

class SetupDrugsMenuFragment : Fragment(R.layout.fragment_menu_setup_drugs){

    private val binding by viewBinding<FragmentMenuSetupDrugsBinding>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            btnAddDrug.setOnClickListener {
                checkDatabaseValidityThen {
                    startActivity(AddDrugActivity.newIntentToAdd(requireContext()))
                }
            }
            btnAddDrugDose.setOnClickListener {
                checkDatabaseValidityThen {
                    startActivity(Intent(requireContext(), AddDrugDoseActivity::class.java))
                }
            }
            btnAddOffLabelDose.setOnClickListener {
                checkDatabaseValidityThen {
                    startActivity(Intent(requireContext(), AddOffLabelDrugDoseActivity::class.java))
                }
            }
            btnAddDrugLot.setOnClickListener {
                checkDatabaseValidityThen {
                    startActivity(Intent(requireContext(), AddDrugLotActivity::class.java))
                }
            }
        }
    }
}