package com.weyr_associates.animaltrakkerfarmmobile.app.animal.history

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.drug.AnimalDrugHistoryFragment
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.evaluation.AnimalEvaluationHistoryFragment
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.notes.AnimalNotesFragment
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.tissue.AnimalTissueSampleHistoryFragment
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.tissue.AnimalTissueTestHistoryFragment

interface AnimalHistoryFragmentFactory {
    fun createAnimalNotesFragment(): AnimalNotesFragment
    fun createAnimalDrugHistoryFragment(): AnimalDrugHistoryFragment
    fun createAnimalTissueSampleHistoryFragment(): AnimalTissueSampleHistoryFragment
    fun createAnimalTissueTestSampleFragment(): AnimalTissueTestHistoryFragment
    fun createAnimalEvaluationHistoryFragment(): AnimalEvaluationHistoryFragment
}

class AnimalHistoryTabsAdapter(
    fragmentManager: FragmentManager,
    lifeCycle: Lifecycle,
    private val fragmentFactory: AnimalHistoryFragmentFactory
) : FragmentStateAdapter(fragmentManager, lifeCycle), TabLayoutMediator.TabConfigurationStrategy {

    constructor(activity: FragmentActivity, fragmentFactory: AnimalHistoryFragmentFactory)
            : this(activity.supportFragmentManager, activity.lifecycle, fragmentFactory)

    constructor(fragment: Fragment, fragmentFactory: AnimalHistoryFragmentFactory)
            : this(fragment.childFragmentManager, fragment.lifecycle, fragmentFactory)

    override fun getItemCount(): Int = 5

    override fun onConfigureTab(tab: TabLayout.Tab, position: Int) {
        tab.setText(
            when (position) {
                0 -> R.string.tab_title_animal_notes_history
                1 -> R.string.tab_title_animal_drug_history
                2 -> R.string.tab_title_animal_tissue_sample_history
                3 -> R.string.tab_title_animal_lab_test_history
                4 -> R.string.tab_title_animal_evaluation_history
                else -> throw IllegalStateException("Invalid position: $position")
            }
        )
    }

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> fragmentFactory.createAnimalNotesFragment()
            1 -> fragmentFactory.createAnimalDrugHistoryFragment()
            2 -> fragmentFactory.createAnimalTissueSampleHistoryFragment()
            3 -> fragmentFactory.createAnimalTissueTestSampleFragment()
            4 -> fragmentFactory.createAnimalEvaluationHistoryFragment()
            else -> throw IllegalStateException("Invalid position: $position")
        }
    }
}
