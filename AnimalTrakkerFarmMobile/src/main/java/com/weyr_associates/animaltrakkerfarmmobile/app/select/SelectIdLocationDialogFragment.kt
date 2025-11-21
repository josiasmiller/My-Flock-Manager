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
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.optionalItemSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.IdLocationRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.select.SelectIdLocationDialogFragment.Companion.REQUEST_KEY_SELECT_ID_LOCATION
import com.weyr_associates.animaltrakkerfarmmobile.model.IdLocation
import com.weyr_associates.animaltrakkerfarmmobile.model.filterByName

class SelectIdLocationDialogFragment : SelectItemDialogFragment<IdLocation>(
    IdLocation::class.java,
    R.string.title_select_id_location,
    provideFilter = false
) {

    companion object {
        @JvmStatic
        fun newInstance(
            requestKey: String = REQUEST_KEY_SELECT_ID_LOCATION,
            isOptional: Boolean = false
        ) =
            SelectIdLocationDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(SelectItem.EXTRA_REQUEST_KEY, requestKey)
                    putBoolean(SelectItem.EXTRA_IS_OPTIONAL, isOptional)
                }
            }

        const val REQUEST_KEY_SELECT_ID_LOCATION = "REQUEST_KEY_SELECT_ID_LOCATION"
    }

    override fun createItemDelegateFactory(context: Context): ItemDelegateFactory<IdLocation> {
        return Factory(context)
    }

    private class Factory(
        context: Context
    ) : EntityItemDelegateFactory<IdLocation>(context) {
        override fun createDataSource(): ItemDataSource<IdLocation> {
            return object : EntityItemDataSource<IdLocation>(appContext) {
                private val idLocationRepo = IdLocationRepositoryImpl(databaseHandler)
                override suspend fun queryItems(filterText: String): List<IdLocation> {
                    return idLocationRepo.queryIdLocations().filterByName(filterText)
                }
            }
        }
    }
}

// region Launch Helpers

fun FragmentActivity.idLocationSelectionPresenter(
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<IdLocation>? = null,
    onItemSelected: (IdLocation) -> Unit
): ItemSelectionPresenter<IdLocation> {
    return idLocationSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

fun FragmentActivity.optionalIdLocationSelectionPresenter(
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<IdLocation>? = null,
    onItemSelected: (IdLocation?) -> Unit
): ItemSelectionPresenter<IdLocation> {
    return optionalIdLocationSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

fun Fragment.idLocationSelectionPresenter(
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<IdLocation>? = null,
    onItemSelected: (IdLocation) -> Unit
): ItemSelectionPresenter<IdLocation> {
    return idLocationSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

fun Fragment.optionalIdLocationSelectionPresenter(
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<IdLocation>? = null,
    onItemSelected: (IdLocation?) -> Unit
): ItemSelectionPresenter<IdLocation> {
    return optionalIdLocationSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

fun idLocationSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<IdLocation>? = null,
    onItemSelected: (IdLocation) -> Unit
): ItemSelectionPresenter<IdLocation> {
    val requestKeyActual = requestKey ?: REQUEST_KEY_SELECT_ID_LOCATION
    return itemSelectionPresenter(
        registrar,
        requestKeyActual,
        button,
        hintText,
        itemDisplayTextProvider.orNameAsDefault(),
        onItemSelected
    ) { SelectIdLocationDialogFragment.newInstance(requestKeyActual) }
}

fun optionalIdLocationSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<IdLocation>? = null,
    onItemSelected: (IdLocation?) -> Unit
): ItemSelectionPresenter<IdLocation> {
    val requestKeyActual = requestKey ?: REQUEST_KEY_SELECT_ID_LOCATION
    return optionalItemSelectionPresenter(
        registrar,
        requestKeyActual,
        button,
        hintText,
        itemDisplayTextProvider.orNameAsDefault(),
        onItemSelected
    ) { SelectIdLocationDialogFragment.newInstance(requestKeyActual) }
}

// endregion
