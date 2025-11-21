package com.weyr_associates.animaltrakkerfarmmobile.app.animal.alert

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.collectLatestOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.viewBinding
import com.weyr_associates.animaltrakkerfarmmobile.databinding.FragmentAnimalAlertsBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalAlert
import kotlinx.coroutines.flow.StateFlow

interface AnimalAlertsViewModelContract {
    val animalAlerts: StateFlow<List<AnimalAlert>?>
}

abstract class AnimalAlertsFragment : Fragment(R.layout.fragment_animal_alerts) {

    protected abstract val viewModel: AnimalAlertsViewModelContract

    private val binding by viewBinding<FragmentAnimalAlertsBinding>()
    private val alertsAdapter = AnimalAlertsAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.recyclerAnimalAlerts) {
            adapter = alertsAdapter
            layoutManager = LinearLayoutManager(
                view.context,
                RecyclerView.VERTICAL,
                false
            )
            addItemDecoration(DividerItemDecoration(view.context, DividerItemDecoration.VERTICAL))
        }
        collectLatestOnStart(viewModel.animalAlerts) { animalAlerts ->
            alertsAdapter.submitList(animalAlerts?.map { AnimalAlertItem(it) }) {
                updateDisplay(hasList = animalAlerts != null)
            }
        }
    }

    private fun updateDisplay(hasList: Boolean) {
        binding.recyclerAnimalAlerts.isVisible = hasList && alertsAdapter.currentList.isNotEmpty()
        binding.textNoAlertsFound.isVisible = hasList && alertsAdapter.currentList.isEmpty()
    }
}
