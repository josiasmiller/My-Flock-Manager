package com.weyr_associates.animaltrakkerfarmmobile.app.main.menu

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.counter.AnimalCounterActivity
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.add.SimpleAddAnimalActivity
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.death.AnimalDeathActivity
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.manage.ManageAnimalIdsActivity
import com.weyr_associates.animaltrakkerfarmmobile.app.core.checkDatabaseValidityThen
import com.weyr_associates.animaltrakkerfarmmobile.databinding.FragmentMenuManageAnimalsBinding

class ManageAnimalsMenuFragment : Fragment(R.layout.fragment_menu_manage_animals) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(FragmentMenuManageAnimalsBinding.bind(view)) {
            btnScanCountLog.setOnClickListener {
                checkDatabaseValidityThen {
                    startActivity(Intent(requireActivity(), AnimalCounterActivity::class.java))
                }
            }
            btnSimpleAddAnimal.setOnClickListener {
                checkDatabaseValidityThen {
                    startActivity(Intent(requireActivity(), SimpleAddAnimalActivity::class.java))
                }
            }
            btnUpdateAnimalId.setOnClickListener {
                checkDatabaseValidityThen {
                    startActivity(Intent(requireActivity(), ManageAnimalIdsActivity::class.java))
                }
            }
            btnAnimalDeaths.setOnClickListener {
                checkDatabaseValidityThen {
                    startActivity(Intent(requireActivity(), AnimalDeathActivity::class.java))
                }
            }
            listOf(
                btnUpdateAnimalDetails
            ).forEach { it.deactivate() }
        }
    }
}
