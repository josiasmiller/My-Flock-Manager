package com.weyr_associates.animaltrakkerfarmmobile.app.main.menu

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.AnimalHistoryActivity
import com.weyr_associates.animaltrakkerfarmmobile.app.core.checkDatabaseValidityThen
import com.weyr_associates.animaltrakkerfarmmobile.app.label.ScanAndPrintActivity
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseManagementActivity
import com.weyr_associates.animaltrakkerfarmmobile.databinding.FragmentMenuRootBinding

class RootMenuFragment : Fragment(R.layout.fragment_menu_root) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(FragmentMenuRootBinding.bind(view)) {
            btnScanPrint.setOnClickListener {
                checkDatabaseValidityThen {
                    startActivity(Intent(requireActivity(), ScanAndPrintActivity::class.java))
                }
            }
            btnManageAnimals.setOnClickListener {
                findNavController().navigate(R.id.nav_dst_menu_manage_animals)
            }
            btnAnimalEvaluation.setOnClickListener {
                findNavController().navigate(R.id.nav_dst_menu_animal_evaluation)
            }
            btnAnimalCare.setOnClickListener {
                findNavController().navigate(R.id.nav_dst_menu_animal_care)
            }
            btnBirthing.setOnClickListener {
                findNavController().navigate(R.id.nav_dst_menu_birthing)
            }
            btnAnimalMovements.setOnClickListener {
                findNavController().navigate(R.id.nav_dst_menu_animal_movements)
            }
            btnAnimalHistory.setOnClickListener {
                checkDatabaseValidityThen {
                    startActivity(AnimalHistoryActivity.newIntent(requireContext()))
                }
            }
            btnSetup.setOnClickListener {
                findNavController().navigate(R.id.nav_dst_menu_setup)
            }
            btnManageDatabase.setOnClickListener {
                startActivity(Intent(requireActivity(), DatabaseManagementActivity::class.java))
            }
            btnQuitApp.setOnClickListener {
                requireActivity().finishAffinity()
            }
        }
    }
}
