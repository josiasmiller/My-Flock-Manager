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
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.requireAs
import com.weyr_associates.animaltrakkerfarmmobile.app.core.putEntityId
import com.weyr_associates.animaltrakkerfarmmobile.app.core.viewBinding
import com.weyr_associates.animaltrakkerfarmmobile.app.core.widget.recyclerview.OutlineDividerDecoration
import com.weyr_associates.animaltrakkerfarmmobile.app.model.formatForDisplay
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.AnimalRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.DefaultSettingsRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.ActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseManager
import com.weyr_associates.animaltrakkerfarmmobile.databinding.FragmentFemaleBreedingDetailsBinding
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ItemFemaleBreedingDetailsBinding
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ItemSectionHeaderBinding
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ItemSummaryTotalBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.FemaleBreedingDetails

class FemaleBreedingDetailsFragment : Fragment(R.layout.fragment_female_breeding_details) {
    companion object {
        fun newInstance(animalId: EntityId) = FemaleBreedingDetailsFragment().apply {
            arguments = Bundle().apply {
                putEntityId(EXTRA_ANIMAL_ID, animalId)
            }
        }
        private const val EXTRA_ANIMAL_ID = "EXTRA_ANIMAL_ID"
    }

    private val viewModel by viewModels<FemaleBreedingDetailsViewModel> {
        ViewModelFactory(requireContext())
    }

    private val binding by viewBinding<FragmentFemaleBreedingDetailsBinding>()

    private val femaleBreedingDetailsAdapter = FemaleBreedingDetailsAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.recyclerFemaleBreedingDetails) {
            adapter = femaleBreedingDetailsAdapter
            layoutManager = LinearLayoutManager(
                view.context,
                RecyclerView.VERTICAL,
                false
            )
            addItemDecoration(OutlineDividerDecoration(
                context = view.context,
                insetDimenRes = R.dimen.activity_margin,
                sectionPositionProvider = femaleBreedingDetailsAdapter))
        }
        collectLatestOnStart(viewModel.femaleBreedingDetails) { breedingDetails ->
            updateDisplay(breedingDetails)
        }
    }

    private fun updateDisplay(breedingDetails: FemaleBreedingDetails?) {
        femaleBreedingDetailsAdapter.femaleBreedingDetails = breedingDetails
        binding.textNoBreedingDetails.isGone = breedingDetails != null && breedingDetails.yearly.isNotEmpty()
    }

    private class FemaleBreedingDetailsAdapter
        : RecyclerView.Adapter<FemaleBreedingDetailsViewHolder>(),
            OutlineDividerDecoration.SectionPositionProvider {

        companion object {
            private const val VIEW_TYPE_EMPTY = 0
            private const val VIEW_TYPE_SECTION = 1
            private const val VIEW_TYPE_BREEDING = 2
            private const val VIEW_TYPE_BREEDING_LAST = 3
        }

        var femaleBreedingDetails: FemaleBreedingDetails? = null
            @SuppressLint("NotifyDataSetChanged")
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        override fun getItemCount(): Int {
            return femaleBreedingDetails?.let { details ->
                details.takeIf { it.yearly.isNotEmpty() }?.let {
                    it.yearly.size + it.yearly.sumOf {
                        it.events.size + if (it.nonEventBirthsBySex.isEmpty()) 0 else 1
                    }
                }
            } ?: 0
        }

        override fun getItemViewType(position: Int): Int {
            var balance = position
            return femaleBreedingDetails?.yearly?.let { sections ->
                sections.forEach {
                    if (balance == 0) {
                        return@let VIEW_TYPE_SECTION
                    }
                    balance = balance - 1 //reduce for section
                    if (balance < it.events.size) {
                        return@let if (balance == it.events.lastIndex && it.nonEventBirthsBySex.isEmpty())
                            VIEW_TYPE_BREEDING_LAST else VIEW_TYPE_BREEDING
                    }
                    balance -= it.events.size //reduce for events
                    if (it.nonEventBirthsBySex.isNotEmpty()) {
                        if (balance == 0) {
                            return@let VIEW_TYPE_BREEDING_LAST
                        }
                        balance = balance - 1 //reduce for non breeding births
                    }
                }
                return@let VIEW_TYPE_EMPTY
            } ?: VIEW_TYPE_EMPTY
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FemaleBreedingDetailsViewHolder {
            return when(viewType) {
                VIEW_TYPE_SECTION -> HeaderViewHolder(
                    ItemSectionHeaderBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
                VIEW_TYPE_BREEDING,
                VIEW_TYPE_BREEDING_LAST -> ItemViewHolder(
                    ItemFemaleBreedingDetailsBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
                else -> throw IllegalStateException("Invalid view type.")
            }
        }

        override fun onBindViewHolder(holder: FemaleBreedingDetailsViewHolder, position: Int) {
            val viewType = getItemViewType(position)
            when (viewType) {
                VIEW_TYPE_SECTION -> {
                    bindHeaderAtPosition(
                        viewHolder = holder.requireAs<HeaderViewHolder>(),
                        position = position
                    )
                }
                VIEW_TYPE_BREEDING,
                VIEW_TYPE_BREEDING_LAST -> {
                    bindItemAtPosition(
                        viewHolder = holder.requireAs<ItemViewHolder>(),
                        position = position
                    )
                }
            }
        }

        override fun positionToSectionPosition(position: Int): OutlineDividerDecoration.SectionPosition {
            return when (getItemViewType(position)) {
                VIEW_TYPE_SECTION -> OutlineDividerDecoration.SectionPosition.BEGINNING
                VIEW_TYPE_BREEDING_LAST -> OutlineDividerDecoration.SectionPosition.END
                else -> OutlineDividerDecoration.SectionPosition.MIDDLE
            }
        }

        private fun bindHeaderAtPosition(viewHolder: HeaderViewHolder, position: Int) {
            femaleBreedingDetails?.let {
                var balance = position
                it.yearly.forEach {
                    if (balance == 0) {
                        viewHolder.bind(it.year)
                        return //returns from bindHeaderAtPosition
                    }
                    balance -= it.events.size + (1 + if (it.nonEventBirthsBySex.isEmpty()) 0 else 1)
                }
                throw IllegalStateException("Header not found at position: $position")
            } ?: throw IllegalStateException("Female breeding details is null.")
        }

        private fun bindItemAtPosition(viewHolder: ItemViewHolder, position: Int) {
            femaleBreedingDetails?.let {
                var balance = position
                it.yearly.forEach {
                    balance = balance - 1
                    if (0 <= balance && balance < it.events.size) {
                        viewHolder.bind(it.events[balance])
                        return //returns from bindItemAtPosition
                    }
                    balance -= it.events.size
                    if (it.nonEventBirthsBySex.isNotEmpty()) {
                        if (0 == balance) {
                            viewHolder.bind(it.nonEventBirthsBySex)
                            return //returns from bindItemAtPosition
                        }
                        balance = balance - 1
                    }
                }
                throw IllegalStateException("Item not found at position: $position")
            } ?: throw IllegalStateException("Female breeding details is null.")
        }
    }

    private abstract class FemaleBreedingDetailsViewHolder(itemView: View) : ViewHolder(itemView)

    private class HeaderViewHolder(
        private val binding: ItemSectionHeaderBinding
    ) : FemaleBreedingDetailsViewHolder(binding.root) {

        fun bind(year: Int?) {
            binding.textSectionHeader.text = year?.toString()
                ?: itemView.context.getString(R.string.text_untracked_breeding)
            binding.dividerSectionHeader.isVisible = year == null
        }
    }

    private class ItemViewHolder(
        private val binding: ItemFemaleBreedingDetailsBinding
    ) : FemaleBreedingDetailsViewHolder(binding.root) {

        private val summaryTotalCache = mutableListOf<ItemSummaryTotalBinding>()

        @SuppressLint("SetTextI18n")
        fun bind(event: FemaleBreedingDetails.BreedingEvent) {
            binding.groupMaleEventInfo.isVisible = event.maleBreedingInfo != null
            binding.textEventTitle.text = event.maleBreedingInfo?.sireFlockPrefix?.let {
                "$it ${event.maleBreedingInfo.sireName}"
            } ?: event.maleBreedingInfo?.sireName ?: "???"
            binding.textDateTimeIn.text = event.maleBreedingInfo?.let {
                "${it.dateMaleIn.formatForDisplay()} ${it.timeMaleIn.formatForDisplay()}"
            } ?: ""
            binding.textDateTimeOut.text = event.maleBreedingInfo?.let {
                "${it.dateMaleOut?.formatForDisplay() ?: "???"} ${it.timeMaleOut?.formatForDisplay() ?: "???"}"
            } ?: ""
            binding.textServiceType.text = event.maleBreedingInfo?.serviceType?.name ?: ""

            val birthingDate = event.birthingDate?.formatForDisplay() ?: "???"
            val birthingTime = event.birthingTime?.formatForDisplay() ?: "???"
            binding.textDateTimeBirthing.text = "$birthingDate $birthingTime"
            binding.groupBirthingDateTime.isVisible = event.birthsBySex.isNotEmpty()

            binding.textBirthNotes.text = event.birthNotes ?: ""
            binding.groupBirthNotes.isVisible = event.birthNotes != null

            bindSummaryTotals(event.birthsBySex)
        }

        fun bind(untrackedTotals: List<FemaleBreedingDetails.Total>) {
            binding.groupMaleEventInfo.isVisible = false
            binding.groupBirthingDateTime.isVisible = false
            binding.groupBirthNotes.isVisible = false
            binding.textDateTimeIn.text = null
            binding.textDateTimeOut.text = null
            binding.textServiceType.text = null
            binding.textDateTimeBirthing.text = null
            binding.textEventTitle.text = itemView.context.getString(R.string.text_untracked_breeding)
            bindSummaryTotals(untrackedTotals)
        }

        private fun bindSummaryTotals(totals: List<FemaleBreedingDetails.Total>) {
            val hasOffspring = totals.isNotEmpty()
            if (hasOffspring) {
                binding.containerBirthTotalsBySex.isVisible = true
                binding.textNoOffspring.isVisible = false
                while (summaryTotalCache.size < totals.size) {
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
            } else {
                binding.containerBirthTotalsBySex.isVisible = false
                binding.textNoOffspring.isVisible = true
            }
        }
    }

    private class ViewModelFactory(context: Context) : ViewModelProvider.Factory {

        private val appContext = context.applicationContext

        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return when (modelClass) {
                FemaleBreedingDetailsViewModel::class.java -> {
                    val databaseHandler = DatabaseManager.getInstance(appContext)
                        .createDatabaseHandler()
                    @Suppress("UNCHECKED_CAST")
                    FemaleBreedingDetailsViewModel(
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
