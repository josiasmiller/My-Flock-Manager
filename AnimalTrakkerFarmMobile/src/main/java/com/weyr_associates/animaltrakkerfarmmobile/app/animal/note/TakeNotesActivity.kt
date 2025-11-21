package com.weyr_associates.animaltrakkerfarmmobile.app.animal.note

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.FragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.core.getEntityId
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.coroutines.flow.observeOneTimeEvents
import com.weyr_associates.animaltrakkerfarmmobile.app.core.putEntityId
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.SelectItem
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.selectedItem
import com.weyr_associates.animaltrakkerfarmmobile.app.core.widget.TopButtonBar.Companion.UI_ACTION_UPDATE_DATABASE
import com.weyr_associates.animaltrakkerfarmmobile.app.core.widget.TopButtonBar.Companion.UI_CLEAR_DATA
import com.weyr_associates.animaltrakkerfarmmobile.app.model.itemCallbackUsingOnlyIdentity
import com.weyr_associates.animaltrakkerfarmmobile.app.reporting.errors.observeErrorReports
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.AnimalRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.DefaultSettingsRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.select.SelectPredefinedNoteDialogFragment
import com.weyr_associates.animaltrakkerfarmmobile.app.select.SelectPredefinedNoteDialogFragment.Companion.REQUEST_KEY_SELECT_PREDEFINED_NOTE
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.ActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.database.system.DatabaseManager
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ActivityTakeNotesBinding
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ItemPredefinedNoteBinding
import com.weyr_associates.animaltrakkerfarmmobile.model.DefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.PredefinedNote
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TakeNotesActivity : AppCompatActivity() {

    companion object {
        @JvmStatic
        fun newIntent(context: Context, animalId: EntityId): Intent {
            return Intent(context, TakeNotesActivity::class.java).apply {
                putExtra(TakeNotes.EXTRA_ANIMAL_ID, animalId)
            }
        }

        private const val EXTRA_REPLACING_NOTE_ID = "EXTRA_REPLACING_NOTE_ID"
        private const val TAG_FRAGMENT_SELECT_NOTE = "TAG_FRAGMENT_SELECT_NOTE"
    }

    private val binding by lazy {
        ActivityTakeNotesBinding.inflate(layoutInflater)
    }

    private val notesAdapter = PredefinedNoteAdapter(
        onUpdateNote = { id -> selectNoteToReplace(id) },
        onClearNote = { id -> clearNote(id) }
    )

    private val viewModel by viewModels<TakeNotesViewModel>() {
        ViewModelFactory(this)
    }

    private val onNoteSelected = FragmentResultListener { _, result ->
        val selectedNote: PredefinedNote = result.selectedItem()
        val associatedData: Bundle? = result.getBundle(SelectItem.EXTRA_ASSOCIATED_DATA)
        if (associatedData == null) {
            viewModel.addNote(selectedNote)
        } else {
            viewModel.replaceNote(
                associatedData.getEntityId(EXTRA_REPLACING_NOTE_ID),
                selectedNote
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        if (savedInstanceState == null) {
            binding.inputCustomNoteText.requestFocus()
        }
        binding.buttonPanelTop.show(UI_CLEAR_DATA or UI_ACTION_UPDATE_DATABASE)
        binding.buttonPanelTop.clearDataButton.setOnClickListener {
            viewModel.clearData()
        }
        binding.buttonPanelTop.mainActionButton.setOnClickListener {
            viewModel.saveData()
        }
        binding.buttonAddNote.setOnClickListener {
            addNewNote()
        }
        binding.inputCustomNoteText.addTextChangedListener {
            viewModel.customNoteText = it.toString()
        }
        with(binding.recyclerNotes) {
            adapter = notesAdapter
            layoutManager = LinearLayoutManager(
                this@TakeNotesActivity,
                RecyclerView.VERTICAL,
                false
            )
            addItemDecoration(NoteSpacingDecoration(this@TakeNotesActivity))
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.notes.collectLatest {
                    notesAdapter.submitList(it.items) {
                        binding.recyclerNotes.invalidateItemDecorations()
                    }
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.canClearData.collectLatest { canClear ->
                    binding.buttonPanelTop.clearDataButton.isEnabled = canClear
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.canSaveData.collectLatest { canSave ->
                    binding.buttonPanelTop.mainActionButton.isEnabled = canSave
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.observeOneTimeEvents {
                    handleEvent(it)
                }
            }
        }
        observeErrorReports(viewModel.errorReportFlow)
        supportFragmentManager.setFragmentResultListener(
            REQUEST_KEY_SELECT_PREDEFINED_NOTE,
            this,
            onNoteSelected
        )
    }

    private fun addNewNote() {
        SelectPredefinedNoteDialogFragment.newInstance(
            excludedNoteIds = viewModel.notes.value.ids
        ).show(supportFragmentManager, TAG_FRAGMENT_SELECT_NOTE)
    }

    private fun selectNoteToReplace(id: EntityId) {
        SelectPredefinedNoteDialogFragment.newInstance(
            excludedNoteIds = viewModel.notes.value.ids,
            associatedData = Bundle().apply {
                putEntityId(EXTRA_REPLACING_NOTE_ID, id)
            }
        ).show(supportFragmentManager, TAG_FRAGMENT_SELECT_NOTE)
    }

    private fun clearNote(id: EntityId) {
        viewModel.clearNote(id)
    }

    private fun handleEvent(event: TakeNotesViewModel.Event) {
        when (event) {
            TakeNotesViewModel.CustomNoteTextChanged -> {
                binding.inputCustomNoteText.setText(viewModel.customNoteText)
            }
            TakeNotesViewModel.DatabaseUpdateSucceeded -> {
                showDatabaseUpdateSucceeded()
            }
            TakeNotesViewModel.DatabaseUpdateFailed -> {
                showDatabaseUpdateFailed()
            }
        }
    }

    private fun showDatabaseUpdateSucceeded() {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_title_notes_added)
            .setMessage(R.string.dialog_message_notes_added)
            .setPositiveButton(R.string.ok) { _, _ -> /*NO-OP*/ }
            .setOnDismissListener { finish() }
            .create()
            .show()
    }

    private fun showDatabaseUpdateFailed() {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_title_add_notes_failed)
            .setMessage(R.string.dialog_message_add_notes_failed)
            .setPositiveButton(R.string.ok) { _, _ -> /*NO-OP*/ }
            .create()
            .show()
    }
}

private class PredefinedNoteAdapter(
    private val onUpdateNote: (EntityId) -> Unit,
    private val onClearNote: (EntityId) -> Unit
) : ListAdapter<PredefinedNote, PredefinedNoteViewHolder>(
    itemCallbackUsingOnlyIdentity()
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PredefinedNoteViewHolder {
        return PredefinedNoteViewHolder(
            ItemPredefinedNoteBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ),
            onUpdateNote,
            onClearNote
        )
    }

    override fun onBindViewHolder(holder: PredefinedNoteViewHolder, position: Int) {
        holder.bind(currentList[position])
    }
}

private class PredefinedNoteViewHolder(
    private val binding: ItemPredefinedNoteBinding,
    private val onUpdateNote: (EntityId) -> Unit,
    private val onClearNote: (EntityId) -> Unit
) : ViewHolder(binding.root) {

    fun bind(note: PredefinedNote) {
        binding.buttonSpinnerNote.text = note.text
        binding.buttonSpinnerNote.setOnClickListener {
            onUpdateNote(note.id)
        }
        binding.buttonClearNote.setOnClickListener {
            onClearNote(note.id)
        }
    }
}

private class NoteSpacingDecoration(context: Context) : RecyclerView.ItemDecoration() {
    private val inset = context.resources.getDimensionPixelOffset(R.dimen.activity_margin)
    private val insetInterior = inset / 4

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val itemPosition = parent.getChildAdapterPosition(view)
        val itemCount = requireNotNull(parent.adapter).itemCount
        when (itemPosition) {
            0 -> {
                outRect.set(inset, inset, inset, insetInterior)
            }
            itemCount - 1 -> {
                outRect.set(inset, insetInterior, inset, inset)
            }
            else -> {
                outRect.set(inset, insetInterior, inset, insetInterior)
            }
        }
    }
}

private class ViewModelFactory(context: Context) : ViewModelProvider.Factory {

    private val appContext = context.applicationContext

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return when (modelClass) {
            TakeNotesViewModel::class.java -> {
                val databaseHandler = DatabaseManager.getInstance(appContext)
                    .createDatabaseHandler()
                val defaultSettingsRepo = DefaultSettingsRepositoryImpl(
                    databaseHandler, ActiveDefaultSettings.from(appContext)
                )
                val animalRepo = AnimalRepositoryImpl(databaseHandler, defaultSettingsRepo)
                @Suppress("UNCHECKED_CAST")
                TakeNotesViewModel(
                    extras.createSavedStateHandle(),
                    animalRepo
                ) as T
            }
            else -> throw IllegalStateException("ViewModel of type ${modelClass.simpleName} not supported.")
        }
    }
}
