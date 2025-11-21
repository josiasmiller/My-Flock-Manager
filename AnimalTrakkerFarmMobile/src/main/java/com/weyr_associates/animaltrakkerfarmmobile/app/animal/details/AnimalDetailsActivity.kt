package com.weyr_associates.animaltrakkerfarmmobile.app.animal.details

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.isGone
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.alert.AnimalAlertsFragment
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.alert.AnimalAlertsViewModelContract
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.breeding.details.AnimalBreedingDetailsFragment
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.breeding.details.AnimalBreedingDetailsViewModelContract
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.breeding.summary.AnimalBreedingSummaryFragment
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.breeding.summary.AnimalBreedingSummaryViewModelContract
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.drug.AnimalDrugHistoryFragment
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.drug.AnimalDrugHistoryViewModelContract
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.evaluation.AnimalEvaluationHistoryFragment
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.evaluation.AnimalEvaluationsHistoryViewModelContract
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.genetics.AnimalGeneticsFragment
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.genetics.AnimalGeneticsViewModelContract
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.id.AnimalIdHistoryFragment
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.id.AnimalIdHistoryViewModelContract
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.location.AnimalLocationTimelineFragment
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.location.AnimalLocationTimelineViewModelContract
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.notes.AnimalNotesFragment
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.notes.AnimalNotesViewModelContract
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.tissue.AnimalTissueSampleHistoryFragment
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.tissue.AnimalTissueSampleHistoryViewModelContract
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.tissue.AnimalTissueTestHistoryFragment
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.tissue.AnimalTissueTestHistoryViewModelContract
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.info.AnimalDetailedInfoPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.collectLatestOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.requireAs
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.takeAs
import com.weyr_associates.animaltrakkerfarmmobile.app.core.viewBinding
import com.weyr_associates.animaltrakkerfarmmobile.app.core.widget.toggle
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.AnimalRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.DefaultSettingsRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.PremiseRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.ActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseManager
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ActivityAnimalDetailsBinding
import com.weyr_associates.animaltrakkerfarmmobile.databinding.FragmentAnimalDetailsInfoBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId

private const val EXTRA_ANIMAL_ID = "EXTRA_ANIMAL_ID"

class AnimalDetailsActivity : AppCompatActivity() {

    companion object {
        fun newIntent(context: Context, animalId: EntityId) =
            Intent(context, AnimalDetailsActivity::class.java).apply {
                putExtra(EXTRA_ANIMAL_ID, animalId)
            }
    }

    private val binding by lazy {
        ActivityAnimalDetailsBinding.inflate(layoutInflater)
    }

    private val viewModel by viewModels<AnimalDetailsViewModel> {
        ViewModelFactory(this, extractAnimalIdFrom(this))
    }

    private val drawerCloseBackPressHandler = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        }
    }

    private val drawerListener = object : DrawerListener {
        override fun onDrawerOpened(drawerView: View) {
            drawerCloseBackPressHandler.isEnabled = true
        }
        override fun onDrawerClosed(drawerView: View) {
            drawerCloseBackPressHandler.isEnabled = false
        }
        override fun onDrawerSlide(drawerView: View, slideOffset: Float) { /*NO-OP*/ }
        override fun onDrawerStateChanged(newState: Int) { /*NO-OP*/ }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val navHostFragment = supportFragmentManager.findFragmentById(
            R.id.container_nav_host
        ) as NavHostFragment
        val navController = navHostFragment.navController.apply {
            addOnDestinationChangedListener { _, _, _ ->
                updateTitle(this)
            }
        }
        binding.drawerLayout.addDrawerListener(drawerListener)
        binding.drawerView.setupWithNavController(navController)
        binding.drawerView.setCheckedItem(R.id.nav_dst_animal_info)
        onBackPressedDispatcher.addCallback(this, drawerCloseBackPressHandler)
        collectLatestOnStart(viewModel.animalDetails) { updateTitle(navController) }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_animal_details, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_item_drawer) {
            binding.drawerLayout.toggle(GravityCompat.END)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateTitle(navController: NavController) {
        val animalDetails = viewModel.animalDetails.value
            .takeAs<AnimalDetailsViewModel.AnimalDetailsLoaded>()?.animalDetails
        val currentDestination = navController.currentDestination
        when {
            animalDetails != null && currentDestination != null -> {
                title = getString(
                    R.string.title_activity_animal_details_format,
                    animalDetails.basicInfo.name
                )
                supportActionBar?.subtitle = currentDestination.label
            }
            animalDetails != null -> {
                title = getString(
                    R.string.title_activity_animal_details_format,
                    animalDetails.basicInfo.name
                )
            }
            else -> {
                setTitle(R.string.title_activity_animal_details)
            }
        }
    }
}

//Slightly hacky way to get animal id into VM factory
//for child fragments.
private fun extractAnimalIdFrom(activity: Activity): EntityId {
    return activity.requireAs<AnimalDetailsActivity>().intent
        .getParcelableExtra(EXTRA_ANIMAL_ID) ?: EntityId.UNKNOWN
}

class AnimalDetailsInfoFragment : Fragment(R.layout.fragment_animal_details_info) {

    private val binding by viewBinding<FragmentAnimalDetailsInfoBinding>()

    private val viewModel by activityViewModels<AnimalDetailsViewModel> {
        ViewModelFactory(requireContext(), extractAnimalIdFrom(requireActivity()))
    }

    private val animalDetailsInfoPresenter = AnimalDetailedInfoPresenter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        animalDetailsInfoPresenter.binding = binding.animalDetailedInfo
        collectLatestOnStart(viewModel.animalDetails) {
            when (it) {
                AnimalDetailsViewModel.AnimalDetailsLoading -> {
                    binding.containerAnimalDetailedInfo.isGone = true
                    binding.containerNoAnimalDetailedInfo.isGone = true
                    animalDetailsInfoPresenter.animalDetails = null
                }
                AnimalDetailsViewModel.AnimalDetailsNotFound -> {
                    binding.containerAnimalDetailedInfo.isGone = true
                    binding.containerNoAnimalDetailedInfo.isGone = false
                    animalDetailsInfoPresenter.animalDetails = null
                }
                is AnimalDetailsViewModel.AnimalDetailsLoaded -> {
                    binding.containerAnimalDetailedInfo.isGone = false
                    binding.containerNoAnimalDetailedInfo.isGone = true
                    animalDetailsInfoPresenter.animalDetails = it.animalDetails
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        animalDetailsInfoPresenter.binding = null
    }
}

class AnimalDetailsGeneticsFragment : AnimalGeneticsFragment() {
    override val viewModel: AnimalGeneticsViewModelContract
        by activityViewModels<AnimalDetailsViewModel> {
            ViewModelFactory(
                requireContext(),
                extractAnimalIdFrom(requireActivity())
            )
        }
}

class AnimalDetailsAlertsFragment : AnimalAlertsFragment() {
    override val viewModel: AnimalAlertsViewModelContract
        by activityViewModels<AnimalDetailsViewModel> {
            ViewModelFactory(
                requireContext(),
                extractAnimalIdFrom(requireActivity())
            )
        }
}

class AnimalDetailsNotesFragment : AnimalNotesFragment() {
    override val viewModel: AnimalNotesViewModelContract
        by activityViewModels<AnimalDetailsViewModel> {
            ViewModelFactory(
                requireContext(),
                extractAnimalIdFrom(requireActivity())
            )
        }
}

class AnimalDetailsDrugsFragment : AnimalDrugHistoryFragment() {
    override val viewModel: AnimalDrugHistoryViewModelContract
        by activityViewModels<AnimalDetailsViewModel> {
            ViewModelFactory(
                requireContext(),
                extractAnimalIdFrom(requireActivity())
            )
        }
}

class AnimalDetailsTissueSamplesFragment : AnimalTissueSampleHistoryFragment() {
    override val viewModel: AnimalTissueSampleHistoryViewModelContract
        by activityViewModels<AnimalDetailsViewModel> {
            ViewModelFactory(
                requireContext(),
                extractAnimalIdFrom(requireActivity())
            )
        }
}

class AnimalDetailsTissueTestsFragment : AnimalTissueTestHistoryFragment() {
    override val viewModel: AnimalTissueTestHistoryViewModelContract
        by activityViewModels<AnimalDetailsViewModel> {
            ViewModelFactory(
                requireContext(),
                extractAnimalIdFrom(requireActivity())
            )
        }
}

class AnimalDetailsEvaluationsFragment : AnimalEvaluationHistoryFragment() {
    override val viewModel: AnimalEvaluationsHistoryViewModelContract
        by activityViewModels<AnimalDetailsViewModel> {
            ViewModelFactory(
                requireContext(),
                extractAnimalIdFrom(requireActivity())
            )
        }
}

class AnimalDetailsMovementHistoryFragment : AnimalLocationTimelineFragment() {
    override val viewModel: AnimalLocationTimelineViewModelContract
        by activityViewModels<AnimalDetailsViewModel> {
            ViewModelFactory(
                requireContext(),
                extractAnimalIdFrom(requireActivity())
            )
        }
}

class AnimalDetailsIdHistoryFragment : AnimalIdHistoryFragment() {
    override val viewModel: AnimalIdHistoryViewModelContract
            by activityViewModels<AnimalDetailsViewModel> {
                ViewModelFactory(
                    requireContext(),
                    extractAnimalIdFrom(requireActivity())
                )
            }
}

class AnimalDetailsBreedingSummaryFragment : AnimalBreedingSummaryFragment() {
    override val viewModel: AnimalBreedingSummaryViewModelContract
        by activityViewModels<AnimalDetailsViewModel> {
            ViewModelFactory(
                requireContext(),
                extractAnimalIdFrom(requireActivity())
            )
        }
}

class AnimalDetailsBreedingDetailsFragment : AnimalBreedingDetailsFragment() {
    override val viewModel: AnimalBreedingDetailsViewModelContract
        by activityViewModels<AnimalDetailsViewModel> {
            ViewModelFactory(
                requireContext(),
                extractAnimalIdFrom(requireActivity())
            )
        }
}

private class ViewModelFactory(context: Context, private val animalId: EntityId) : ViewModelProvider.Factory {

    private val appContext = context.applicationContext

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return when (modelClass) {
            AnimalDetailsViewModel::class.java -> {
                val databaseHandler = DatabaseManager.getInstance(appContext)
                    .createDatabaseHandler()
                val defaultSettingsRepo = DefaultSettingsRepositoryImpl(
                    databaseHandler, ActiveDefaultSettings.from(appContext)
                )
                val animalRepo = AnimalRepositoryImpl(databaseHandler, defaultSettingsRepo)
                val premiseRepo = PremiseRepositoryImpl(databaseHandler)
                val loadActiveDefaults = LoadActiveDefaultSettings.from(appContext, databaseHandler)
                @Suppress("UNCHECKED_CAST")
                AnimalDetailsViewModel(animalId, animalRepo, premiseRepo, loadActiveDefaults) as T
            }
            else -> throw IllegalStateException("${modelClass.simpleName} is not supported.")
        }
    }
}
