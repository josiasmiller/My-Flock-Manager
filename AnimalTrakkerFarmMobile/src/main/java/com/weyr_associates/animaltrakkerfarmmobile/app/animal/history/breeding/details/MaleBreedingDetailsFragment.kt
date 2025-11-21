package com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.breeding.details

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.collectLatestOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.putEntityId
import com.weyr_associates.animaltrakkerfarmmobile.app.core.viewBinding
import com.weyr_associates.animaltrakkerfarmmobile.app.core.widget.recyclerview.OutlineDividerDecoration
import com.weyr_associates.animaltrakkerfarmmobile.app.model.formatForDisplay
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.AnimalRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.DefaultSettingsRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.ActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseManager
import com.weyr_associates.animaltrakkerfarmmobile.databinding.FragmentMaleBreedingDetailsBinding
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ItemMaleBreedingDetailsBinding
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ItemSummaryTotalBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.MaleBreedingDetails

class MaleBreedingDetailsFragment : Fragment(R.layout.fragment_male_breeding_details) {
    companion object {
        fun newInstance(animalId: EntityId) = MaleBreedingDetailsFragment().apply {
            arguments = Bundle().apply { putEntityId(EXTRA_ANIMAL_ID, animalId) }
        }

        private const val EXTRA_ANIMAL_ID = MaleBreedingDetailsViewModel.EXTRA_ANIMAL_ID
    }

    private val viewModel by viewModels<MaleBreedingDetailsViewModel> {
        ViewModelFactory(requireContext())
    }

    private val binding by viewBinding<FragmentMaleBreedingDetailsBinding>()
    private val maleBreedingDetailsAdapter = MaleBreedingDetailsAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.recyclerMaleBreedingDetails) {
            adapter = maleBreedingDetailsAdapter
            layoutManager = LinearLayoutManager(
                view.context,
                RecyclerView.VERTICAL,
                false
            )
            addItemDecoration(OutlineDividerDecoration(view.context))
        }
        collectLatestOnStart(viewModel.maleBreedingDetails) { breedingDetails ->
            updateDisplay(breedingDetails)
        }
    }

    private fun updateDisplay(breedingDetails: MaleBreedingDetails?) {
        maleBreedingDetailsAdapter.maleBreedingDetails = breedingDetails
        binding.textNoBreedingDetails.isGone = breedingDetails != null &&
                (breedingDetails.events.isNotEmpty() || breedingDetails.nonEventBirthsBySex.isNotEmpty())
    }

    private class MaleBreedingDetailsAdapter : RecyclerView.Adapter<MaleBreedingDetailsViewHolder>() {

        var maleBreedingDetails: MaleBreedingDetails? = null
            @SuppressLint("NotifyDataSetChanged")
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        override fun getItemCount(): Int {
            return maleBreedingDetails?.let {
                it.events.size + if (it.nonEventBirthsBySex.isNotEmpty()) 1 else 0
            } ?: 0
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MaleBreedingDetailsViewHolder {
            return MaleBreedingDetailsViewHolder(
                ItemMaleBreedingDetailsBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: MaleBreedingDetailsViewHolder, position: Int) {
            val maleBreedingDetails = maleBreedingDetails ?: return
            if (position == maleBreedingDetails.events.size) {
                holder.bind(maleBreedingDetails.nonEventBirthsBySex)
            } else {
                holder.bind(maleBreedingDetails.events[position])
            }
        }
    }

    private class MaleBreedingDetailsViewHolder(
        private val binding: ItemMaleBreedingDetailsBinding
    ) : ViewHolder(binding.root) {

        private val summaryTotalCache = mutableListOf<ItemSummaryTotalBinding>()

        @SuppressLint("SetTextI18n")
        fun bind(event: MaleBreedingDetails.BreedingEvent) {
            binding.groupBreedingInfo.isVisible = true
            binding.textUntrackedBreeding.isVisible = false
            binding.textDateTimeIn.text = "${event.dateMaleIn.formatForDisplay()} ${event.timeMaleIn.formatForDisplay()}"
            binding.textDateTimeOut.text = "${event.dateMaleOut?.formatForDisplay() ?: "???"} ${event.timeMaleOut?.formatForDisplay() ?: "???"}"
            binding.textServiceType.text = event.serviceType.name
            bindSummaryTotals(event.birthsBySex)
        }

        fun bind(untrackedTotals: List<MaleBreedingDetails.Total>) {
            binding.groupBreedingInfo.isVisible = false
            binding.textUntrackedBreeding.isVisible = true
            bindSummaryTotals(untrackedTotals)
        }

        private fun bindSummaryTotals(totals: List<MaleBreedingDetails.Total>) {
            while(summaryTotalCache.size < totals.size) {
                summaryTotalCache.add(
                    ItemSummaryTotalBinding.inflate(
                        LayoutInflater.from(binding.containerBirthTotalsBySex.context),
                        binding.containerBirthTotalsBySex,
                        true
                    )
                )
            }
            summaryTotalCache.forEachIndexed { index, totalView ->
                val total = if (0 <= index && index < totals.size)
                    totals[index] else null
                totalView.textTotalName.text = total?.name
                totalView.textTotalValue.text = total?.value.toString()
                totalView.root.isVisible = total != null
            }
        }
    }

    private class ViewModelFactory(context: Context) : ViewModelProvider.Factory {

        private val appContext = context.applicationContext

        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return when (modelClass) {
                MaleBreedingDetailsViewModel::class.java -> {
                    val databaseHandler = DatabaseManager.getInstance(appContext)
                        .createDatabaseHandler()
                    @Suppress("UNCHECKED_CAST")
                    MaleBreedingDetailsViewModel(
                        savedStateHandle = extras.createSavedStateHandle(),
                        animalRepo = AnimalRepositoryImpl(
                            databaseHandler,
                            DefaultSettingsRepositoryImpl(
                                databaseHandler,
                                ActiveDefaultSettings.from(appContext)
                            )
                        )
                    ) as T
                }
                else -> {
                    throw IllegalStateException("${modelClass.simpleName} is not supported.")
                }
            }
        }
    }
}
