package com.weyr_associates.animaltrakkerfarmmobile.app.select

import android.content.Context
import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.core.FragmentResultListenerRegistrar
import com.weyr_associates.animaltrakkerfarmmobile.app.core.asFragmentResultListenerRegistrar
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.EntityItemDataSource
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.ItemDataSource
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.ItemDelegateFactory
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.ItemDisplayTextProvider
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.ItemSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.SelectItem
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.SelectItemDialogFragment
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.itemSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.DefaultSettingsRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.ActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.model.ItemEntry
import com.weyr_associates.animaltrakkerfarmmobile.model.filterByName

class SelectDefaultSettingsDialogFragment : SelectItemDialogFragment<ItemEntry>(
    ItemEntry::class.java,
    R.string.title_select_default_settings
) {

    companion object {
        @JvmStatic
        fun newInstance(requestKey: String = REQUEST_KEY_SELECT_DEFAULT_SETTINGS) =
            SelectDefaultSettingsDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(SelectItem.EXTRA_REQUEST_KEY, requestKey)
                }
            }

        const val REQUEST_KEY_SELECT_DEFAULT_SETTINGS = "REQUEST_KEY_SELECT_DEFAULT_SETTINGS"
    }
    override fun createItemDelegateFactory(context: Context): ItemDelegateFactory<ItemEntry> {
        return Factory(context)
    }

    private class Factory(context: Context) : EntityItemDelegateFactory<ItemEntry>(context) {
        override fun createDataSource(): ItemDataSource<ItemEntry> {
            return object : EntityItemDataSource<ItemEntry>(appContext) {
                private val defSettingsRepo = DefaultSettingsRepositoryImpl(
                    databaseHandler, ActiveDefaultSettings.from(appContext)
                )
                override suspend fun queryItems(filterText: String): List<ItemEntry> {
                    return defSettingsRepo.queryDefaultSettingsEntries()
                        .filterByName(filterText)
                }
            }
        }
    }
}

// region Launch Helpers

fun FragmentActivity.defaultSettingsSelectionPresenter(
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<ItemEntry>? = null,
    onItemSelected: (ItemEntry) -> Unit
): ItemSelectionPresenter<ItemEntry> {
    return defaultSettingsSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

fun Fragment.defaultSettingsSelectionPresenter(
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<ItemEntry>? = null,
    onItemSelected: (ItemEntry) -> Unit
): ItemSelectionPresenter<ItemEntry> {
    return defaultSettingsSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

private fun defaultSettingsSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    button: Button?,
    requestKey: String?,
    hintText: String?,
    itemDisplayTextProvider: ItemDisplayTextProvider<ItemEntry>?,
    onItemSelected: (ItemEntry) -> Unit
): ItemSelectionPresenter<ItemEntry> {
    val requestKeyActual = requestKey ?: SelectDefaultSettingsDialogFragment.REQUEST_KEY_SELECT_DEFAULT_SETTINGS
    return itemSelectionPresenter(
        registrar,
        requestKeyActual,
        button,
        hintText,
        itemDisplayTextProvider.orNameAsDefault(),
        onItemSelected
    ) { SelectDefaultSettingsDialogFragment.newInstance(requestKeyActual) }
}

// endregion
