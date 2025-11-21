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
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.DrugLocationRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.model.DrugLocation
import com.weyr_associates.animaltrakkerfarmmobile.model.filterByName

class SelectDrugLocationDialogFragment : SelectItemDialogFragment<DrugLocation>(
    DrugLocation::class.java,
    R.string.title_select_drug_location
) {
    companion object {
        fun newInstance(requestKey: String = REQUEST_KEY_SELECT_DRUG_LOCATION) = SelectDrugLocationDialogFragment().apply {
            arguments = Bundle().apply {
                putString(SelectItem.EXTRA_REQUEST_KEY, requestKey)
            }
        }

        const val REQUEST_KEY_SELECT_DRUG_LOCATION = "REQUEST_KEY_SELECT_DRUG_LOCATION"
    }

    override fun createItemDelegateFactory(context: Context): ItemDelegateFactory<DrugLocation> {
        return Factory(context)
    }

    private class Factory(
        context: Context
    ) : EntityItemDelegateFactory<DrugLocation>(context) {
        override fun createDataSource(): ItemDataSource<DrugLocation> {
            return object : EntityItemDataSource<DrugLocation>(appContext) {
                override suspend fun queryItems(filterText: String): List<DrugLocation> {
                    val repo = DrugLocationRepositoryImpl(databaseHandler)
                    return repo.queryAllDrugLocations().filterByName(filterText)
                }
            }
        }
    }
}

// region Launch Helpers

fun FragmentActivity.drugLocationSelectionPresenter(
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<DrugLocation>? = null,
    onItemSelected: (DrugLocation) -> Unit
): ItemSelectionPresenter<DrugLocation> {
    return drugLocationSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

fun Fragment.drugLocationSelectionPresenter(
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<DrugLocation>? = null,
    onItemSelected: (DrugLocation) -> Unit
): ItemSelectionPresenter<DrugLocation> {
    return drugLocationSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

private fun drugLocationSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    button: Button?,
    requestKey: String?,
    hintText: String?,
    itemDisplayTextProvider: ItemDisplayTextProvider<DrugLocation>?,
    onItemSelected: (DrugLocation) -> Unit
): ItemSelectionPresenter<DrugLocation> {
    val requestKeyActual = requestKey ?: SelectDrugLocationDialogFragment.REQUEST_KEY_SELECT_DRUG_LOCATION
    return itemSelectionPresenter(
        registrar,
        requestKeyActual,
        button,
        hintText,
        itemDisplayTextProvider ?: ItemDisplayTextProvider { item -> item.name },
        onItemSelected
    ) { SelectDrugLocationDialogFragment.newInstance(requestKeyActual) }
}

// endregion
