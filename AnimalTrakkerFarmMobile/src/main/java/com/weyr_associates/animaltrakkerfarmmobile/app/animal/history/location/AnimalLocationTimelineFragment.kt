package com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.location

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.AnimalDialogs
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.collectLatestOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.requireAs
import com.weyr_associates.animaltrakkerfarmmobile.app.core.viewBinding
import com.weyr_associates.animaltrakkerfarmmobile.app.model.formatForDisplay
import com.weyr_associates.animaltrakkerfarmmobile.app.model.itemCallbackUsingOnlyContent
import com.weyr_associates.animaltrakkerfarmmobile.databinding.FragmentAnimalLocationTimelineBinding
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ItemLocationEventBinding
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ItemLocationEventGapBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalLocationEvent
import com.weyr_associates.animaltrakkerfarmmobile.model.BirthEvent
import com.weyr_associates.animaltrakkerfarmmobile.model.DeathEvent
import com.weyr_associates.animaltrakkerfarmmobile.model.Gap
import com.weyr_associates.animaltrakkerfarmmobile.model.MovementEvent
import com.weyr_associates.animaltrakkerfarmmobile.model.Premise
import kotlinx.coroutines.flow.StateFlow

interface AnimalLocationTimelineViewModelContract {
    val animalLocationTimeline: StateFlow<List<AnimalLocationEvent>?>
}

abstract class AnimalLocationTimelineFragment : Fragment(R.layout.fragment_animal_location_timeline) {

    protected abstract val viewModel: AnimalLocationTimelineViewModelContract

    private val binding by viewBinding<FragmentAnimalLocationTimelineBinding>()
    private val movementsAdapter = AnimalMovementsAdapter(::onLocationEventClicked)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.recyclerAnimalMovementHistory) {
            adapter = movementsAdapter
            layoutManager = LinearLayoutManager(
                view.context,
                RecyclerView.VERTICAL,
                false
            )
            addItemDecoration(DividerItemDecoration(view.context, DividerItemDecoration.VERTICAL))
        }
        collectLatestOnStart(viewModel.animalLocationTimeline) { animalMovements ->
            movementsAdapter.submitList(animalMovements) {
                updateDisplay(hasList = animalMovements != null)
            }
        }
    }

    private fun updateDisplay(hasList: Boolean) {
        binding.recyclerAnimalMovementHistory.isVisible = hasList && movementsAdapter.currentList.isNotEmpty()
        binding.textNoMovementHistoryFound.isVisible = hasList && movementsAdapter.currentList.isEmpty()
    }

    private fun onLocationEventClicked(locationEvent: AnimalLocationEvent) {
        AnimalDialogs.showAnimalLocationEventIssues(requireContext(), locationEvent)
    }

    private class AnimalMovementsAdapter(
        private val onLocationEventClicked: (AnimalLocationEvent) -> Unit
    ) : ListAdapter<AnimalLocationEvent, LocationTimelineEventViewHolder>(
        itemCallbackUsingOnlyContent<AnimalLocationEvent>()
    ) {
        companion object {
            private const val VIEW_TYPE_EVENT = 1
            private const val VIEW_TYPE_GAP = 4
        }

        override fun getItemViewType(position: Int): Int {
            return when (currentList[position]) {
                is MovementEvent -> VIEW_TYPE_EVENT
                is BirthEvent -> VIEW_TYPE_EVENT
                is DeathEvent -> VIEW_TYPE_EVENT
                is Gap -> VIEW_TYPE_GAP
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationTimelineEventViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            return when (viewType) {
                VIEW_TYPE_EVENT -> LocationEventViewHolder(
                    ItemLocationEventBinding.inflate(
                        layoutInflater,
                        parent,
                        false
                    ),
                    onLocationEventClicked
                )
                VIEW_TYPE_GAP -> GapEventViewHolder(
                    ItemLocationEventGapBinding.inflate(
                        layoutInflater,
                        parent,
                        false
                    ),
                    onLocationEventClicked
                )
                else -> throw IllegalStateException("Invalid view type.")
            }
        }

        override fun onBindViewHolder(holder: LocationTimelineEventViewHolder, position: Int) {
            val item = currentList[position]
            when (holder) {
                is LocationEventViewHolder -> when (item) {
                    is BirthEvent -> holder.bind(item)
                    is MovementEvent -> holder.bind(item)
                    is DeathEvent -> holder.bind(item)
                    else -> throw IllegalStateException("Invalid location event type.")
                }
                is GapEventViewHolder -> {
                    holder.bind(item.requireAs<Gap>())
                }
            }
        }
    }

    private sealed class LocationTimelineEventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private class LocationEventViewHolder(
        private val binding: ItemLocationEventBinding,
        private val onLocationEventClicked: (AnimalLocationEvent) -> Unit
    ) : LocationTimelineEventViewHolder(binding.root) {

        fun bind(movementEvent: MovementEvent) {
            binding.root.setOnClickListener { onLocationEventClicked(movementEvent) }
            binding.textEventName.setText(R.string.text_movement_to)
            binding.textEventDate.text = movementEvent.movement.movementDate.formatForDisplay()
            val premise = movementEvent.movement.toPremise
            updatePremiseDisplay(premise)
            updatePremiseMessageDisplay(premise, R.string.text_movement_event_missing_premise)
            binding.imageEventIssues.isVisible = movementEvent.isMissingPremise ||
                    movementEvent.isNonPhysicalPremise || !movementEvent.isInAnimalLifetime
        }

        fun bind(birthEvent: BirthEvent) {
            binding.root.setOnClickListener { onLocationEventClicked(birthEvent) }
            binding.textEventName.setText(R.string.text_born)
            binding.textEventDate.text = birthEvent.date.formatForDisplay()
            val premise = birthEvent.premise
            updatePremiseDisplay(premise)
            updatePremiseMessageDisplay(premise, R.string.text_birth_event_missing_premise)
            binding.imageEventIssues.isVisible = premise == null
        }

        fun bind(deathEvent: DeathEvent) {
            binding.root.setOnClickListener { onLocationEventClicked(deathEvent) }
            binding.textEventName.setText(R.string.text_died)
            binding.textEventDate.text = deathEvent.date.formatForDisplay()
            val premise = deathEvent.premise
            updatePremiseDisplay(premise)
            updatePremiseMessageDisplay(premise, R.string.text_death_event_missing_premise)
            binding.imageEventIssues.isVisible = premise == null
        }

        @SuppressLint("SetTextI18n")
        private fun updatePremiseDisplay(premise: Premise?) {
            if (premise == null) {
                binding.textPremiseNickname.text = ""
                binding.textPremiseNickname.isVisible = true
                binding.textPremiseAddress.text = ""
                binding.textPremiseAddress.isVisible = false
                binding.textPremiseNumber.text = ""
                binding.textPremiseNumber.isVisible = false
            } else {
                binding.textPremiseNickname.text = premise.nickname
                binding.textPremiseNickname.isVisible = !premise.nickname.isNullOrBlank()
                val premiseAddress = premise.address
                val premiseGeoLocation = premise.geoLocation
                if (premiseAddress != null) {
                    binding.textPremiseAddress.text = buildString {
                        appendLine(premiseAddress.address1)
                        premiseAddress.address2?.let { appendLine(it) }
                        appendLine("${premiseAddress.city}, ${premiseAddress.state} ${premiseAddress.postCode}")
                        append(premiseAddress.country)
                    }
                    binding.textPremiseAddress.isVisible = true
                } else if (premiseGeoLocation != null) {
                    binding.textPremiseAddress.text =
                        "(${premiseGeoLocation.latitude}, ${premiseGeoLocation.longitude})"
                    binding.textPremiseAddress.isVisible = true
                } else {
                    binding.textPremiseAddress.text = ""
                    binding.textPremiseAddress.isVisible = false
                }
                binding.textPremiseNumber.text = premise.number?.let { number ->
                    premise.jurisdiction?.let { jurisdiction -> "$number - ${jurisdiction.name}" }
                        ?: number
                }
                binding.textPremiseNumber.isVisible = !premise.number.isNullOrBlank()
            }
        }

        private fun updatePremiseMessageDisplay(premise: Premise?, @StringRes premiseMessageResId: Int) {
            if (premise != null) {
                binding.textPremiseMessage.text = ""
            } else {
                binding.textPremiseMessage.setText(premiseMessageResId)
            }
            binding.textPremiseMessage.isVisible = premise == null
        }
    }

    private class GapEventViewHolder(
        private val binding: ItemLocationEventGapBinding,
        private val onLocationEventClicked: (AnimalLocationEvent) -> Unit
    ) : LocationTimelineEventViewHolder(binding.root) {
        fun bind(gap: Gap) {
            binding.root.setOnClickListener { onLocationEventClicked(gap) }
        }
    }
}
