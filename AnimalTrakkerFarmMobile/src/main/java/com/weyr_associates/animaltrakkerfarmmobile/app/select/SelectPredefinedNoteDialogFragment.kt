package com.weyr_associates.animaltrakkerfarmmobile.app.select

import android.content.Context
import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.core.FragmentResultListenerRegistrar
import com.weyr_associates.animaltrakkerfarmmobile.app.core.asFragmentResultListenerRegistrar
import com.weyr_associates.animaltrakkerfarmmobile.app.core.getEntityIdSet
import com.weyr_associates.animaltrakkerfarmmobile.app.core.putEntityIdSet
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.EntityItemDataSource
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.ItemDataSource
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.ItemDelegateFactory
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.ItemDisplayTextProvider
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.ItemSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.SelectItem
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.SelectItemDialogFragment
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.itemSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.model.itemCallbackUsingOnlyIdentity
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.DefaultSettingsRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.PredefinedNoteRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.ActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.PredefinedNote
import com.weyr_associates.animaltrakkerfarmmobile.model.UserType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SelectPredefinedNoteDialogFragment : SelectItemDialogFragment<PredefinedNote>(
    PredefinedNote::class.java,
    R.string.title_select_predefined_note,
    provideFilter = true
) {
    companion object {
        @JvmStatic
        fun newInstance(
            excludedNoteIds: Set<EntityId> = emptySet(),
            associatedData: Bundle? = null,
            requestKey: String = REQUEST_KEY_SELECT_PREDEFINED_NOTE
        ) = SelectPredefinedNoteDialogFragment().apply {
                arguments = Bundle().apply {
                    putEntityIdSet(EXTRA_EXCLUDED_NOTE_IDS, excludedNoteIds)
                    putString(SelectItem.EXTRA_REQUEST_KEY, requestKey)
                    putBundle(SelectItem.EXTRA_ASSOCIATED_DATA, associatedData)
                }
            }

        const val REQUEST_KEY_SELECT_PREDEFINED_NOTE = "REQUEST_KEY_SELECT_PREDEFINED_NOTE"

        private const val EXTRA_EXCLUDED_NOTE_IDS = "EXTRA_EXCLUDED_NOTE_IDS"
    }
    override fun createItemDelegateFactory(context: Context): ItemDelegateFactory<PredefinedNote> {
        return Factory(
            context,
            requireArguments().getEntityIdSet(EXTRA_EXCLUDED_NOTE_IDS)
        )
    }

    private class Factory(
        context: Context,
        private val excludedNoteIds: Set<EntityId>
    ): ItemDelegateFactory<PredefinedNote> {

        private val appContext = context.applicationContext

        override fun createDataSource(): ItemDataSource<PredefinedNote> {
            return object : EntityItemDataSource<PredefinedNote>(appContext) {
                private var userInfo: Pair<EntityId?, UserType?>? = null
                private val repo = PredefinedNoteRepositoryImpl(databaseHandler)
                private val loadActiveDefaultSettings: LoadActiveDefaultSettings = LoadActiveDefaultSettings(
                    activeDefaultSettings = ActiveDefaultSettings.from(appContext),
                    defaultSettingsRepo = DefaultSettingsRepositoryImpl(
                        databaseHandler, ActiveDefaultSettings.from(appContext)
                    )
                )
                override suspend fun queryItems(filterText: String): List<PredefinedNote> {
                    val (userId, userType) = loadUserInfo()
                    return when {
                        userId != null && userType != null -> {
                            repo.queryPredefinedNotes(userId, userType)
                        }
                        else -> {
                            repo.queryPredefinedNotesDefaultsOnly()
                        }
                    }.filter {
                        !excludedNoteIds.contains(it.id) && it.text.lowercase().contains(filterText.lowercase())
                    }
                }

                private suspend fun loadUserInfo(): Pair<EntityId?, UserType?> {
                    userInfo?.let { return it }
                    val defaultSettings = withContext(Dispatchers.IO) {
                        loadActiveDefaultSettings()
                    }
                    return Pair(defaultSettings.userId, defaultSettings.userType)
                        .also { userInfo = it }
                }
            }
        }

        override fun createItemDiffCallback(): DiffUtil.ItemCallback<PredefinedNote> {
            return itemCallbackUsingOnlyIdentity()
        }

        override fun createDisplayTextProvider(): ItemDisplayTextProvider<PredefinedNote> {
            return ItemDisplayTextProvider { item -> item.text }
        }
    }
}

// region Launch Helpers

fun FragmentActivity.noteSelectionPresenter(
    excludedNoteIds: Set<EntityId> = emptySet(),
    associatedData: Bundle? = null,
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<PredefinedNote>? = null,
    onItemSelected: (PredefinedNote) -> Unit
): ItemSelectionPresenter<PredefinedNote> {
    return noteSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        excludedNoteIds,
        associatedData,
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

fun Fragment.noteSelectionPresenter(
    excludedNoteIds: Set<EntityId> = emptySet(),
    associatedData: Bundle? = null,
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<PredefinedNote>? = null,
    onItemSelected: (PredefinedNote) -> Unit
): ItemSelectionPresenter<PredefinedNote> {
    return noteSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        excludedNoteIds,
        associatedData,
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

private fun noteSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    excludedNoteIds: Set<EntityId>,
    associatedData: Bundle?,
    button: Button?,
    requestKey: String?,
    hintText: String?,
    itemDisplayTextProvider: ItemDisplayTextProvider<PredefinedNote>?,
    onItemSelected: (PredefinedNote) -> Unit
): ItemSelectionPresenter<PredefinedNote> {
    val requestKeyActual = requestKey ?: SelectPredefinedNoteDialogFragment.REQUEST_KEY_SELECT_PREDEFINED_NOTE
    return itemSelectionPresenter(
        registrar,
        requestKeyActual,
        button,
        hintText,
        itemDisplayTextProvider ?: ItemDisplayTextProvider { item -> item.text },
        onItemSelected
    ) { SelectPredefinedNoteDialogFragment.newInstance(excludedNoteIds, associatedData, requestKeyActual) }
}

// endregion
