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
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.ContactRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.model.VetContact
import com.weyr_associates.animaltrakkerfarmmobile.model.filterByName

class SelectVeterinarianFragment : SelectItemDialogFragment<VetContact>(
    VetContact::class.java,
    R.string.title_select_veterinarian
) {
    companion object {
        fun newInstance(
            requestKey: String = REQUEST_KEY_SELECT_VETERINARIAN
        ) = SelectVeterinarianFragment().apply {
            arguments = Bundle().apply {
                putString(SelectItem.EXTRA_REQUEST_KEY, requestKey)
            }
        }

        const val REQUEST_KEY_SELECT_VETERINARIAN = "REQUEST_KEY_SELECT_VETERINARIAN"
    }

    override fun createItemDelegateFactory(context: Context): ItemDelegateFactory<VetContact> {
        return Factory(context)
    }

    private class Factory(
        context: Context
    ) : EntityItemDelegateFactory<VetContact>(context) {
        override fun createDataSource(): ItemDataSource<VetContact> {
            return object : EntityItemDataSource<VetContact>(appContext) {
                private val repo = ContactRepositoryImpl(databaseHandler)
                override suspend fun queryItems(filterText: String): List<VetContact> {
                    val items = repo.queryVeterinarians()
                    return items.filterByName(filterText)
                }
            }
        }
    }
}

// region Launch Helpers

fun FragmentActivity.veterinarianSelectionPresenter(
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<VetContact>? = null,
    onItemSelected: (VetContact) -> Unit
): ItemSelectionPresenter<VetContact> {
    return veterinarianSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

fun Fragment.veterinarianSelectionPresenter(
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<VetContact>? = null,
    onItemSelected: (VetContact) -> Unit
): ItemSelectionPresenter<VetContact> {
    return veterinarianSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

private fun veterinarianSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    button: Button?,
    requestKey: String?,
    hintText: String?,
    itemDisplayTextProvider: ItemDisplayTextProvider<VetContact>?,
    onItemSelected: (VetContact) -> Unit
): ItemSelectionPresenter<VetContact> {
    val requestKeyActual = requestKey ?: SelectVeterinarianFragment.REQUEST_KEY_SELECT_VETERINARIAN
    return itemSelectionPresenter(
        registrar,
        requestKeyActual,
        button,
        hintText,
        itemDisplayTextProvider.orNameAsDefault(),
        onItemSelected
    ) { SelectVeterinarianFragment.newInstance(requestKeyActual) }
}

// endregion
