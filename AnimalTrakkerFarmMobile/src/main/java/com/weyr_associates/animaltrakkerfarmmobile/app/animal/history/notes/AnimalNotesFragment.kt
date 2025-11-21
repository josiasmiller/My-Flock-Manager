package com.weyr_associates.animaltrakkerfarmmobile.app.animal.history.notes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.core.android.lifecycle.collectLatestOnStart
import com.weyr_associates.animaltrakkerfarmmobile.app.core.viewBinding
import com.weyr_associates.animaltrakkerfarmmobile.app.model.formatForDisplay
import com.weyr_associates.animaltrakkerfarmmobile.app.model.itemCallbackUsingOnlyIdentity
import com.weyr_associates.animaltrakkerfarmmobile.databinding.FragmentAnimalNotesBinding
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ItemAnimalNoteBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalNote
import kotlinx.coroutines.flow.StateFlow

interface AnimalNotesViewModelContract {
    val animalNoteHistory: StateFlow<List<AnimalNote>?>
}

abstract class AnimalNotesFragment : Fragment(R.layout.fragment_animal_notes) {

    protected abstract val viewModel: AnimalNotesViewModelContract

    private val binding by viewBinding<FragmentAnimalNotesBinding>()
    private val notesAdapter = AnimalNotesAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.recyclerAnimalNotes) {
            adapter = notesAdapter
            layoutManager = LinearLayoutManager(
                view.context,
                RecyclerView.VERTICAL,
                false
            )
            addItemDecoration(DividerItemDecoration(view.context, DividerItemDecoration.VERTICAL))
        }
        collectLatestOnStart(viewModel.animalNoteHistory) { animalNotes ->
            notesAdapter.submitList(animalNotes) {
                updateDisplay(hasList = animalNotes != null)
            }
        }
    }

    private fun updateDisplay(hasList: Boolean) {
        binding.recyclerAnimalNotes.isVisible = hasList && notesAdapter.currentList.isNotEmpty()
        binding.textNoNotesFound.isVisible = hasList && notesAdapter.currentList.isEmpty()
    }

    private class AnimalNotesAdapter : ListAdapter<AnimalNote, AnimalNoteViewHolder>(
        itemCallbackUsingOnlyIdentity()
    ) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimalNoteViewHolder {
            return AnimalNoteViewHolder(
                ItemAnimalNoteBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: AnimalNoteViewHolder, position: Int) {
            holder.bind(currentList[position])
        }
    }

    private class AnimalNoteViewHolder(
        private val binding: ItemAnimalNoteBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(animalNote: AnimalNote) {
            binding.textNoteDate.text = animalNote.noteDate.formatForDisplay()
            binding.textNoteTime.text = animalNote.noteTime.formatForDisplay()
            binding.textNoteContent.text = animalNote.noteText
        }
    }
}
