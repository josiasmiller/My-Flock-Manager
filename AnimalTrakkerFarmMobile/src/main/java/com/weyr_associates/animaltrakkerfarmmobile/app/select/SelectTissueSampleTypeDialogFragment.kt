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
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.TissueSampleTypeRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.model.TissueSampleType
import com.weyr_associates.animaltrakkerfarmmobile.model.filterByName

class SelectTissueSampleTypeDialogFragment : SelectItemDialogFragment<TissueSampleType>(
    TissueSampleType::class.java,
    R.string.title_select_tissue_sample_type
) {

    companion object {
        @JvmStatic
        fun newInstance(requestKey: String = REQUEST_KEY_SELECT_TISSUE_SAMPLE_TYPE) =
            SelectTissueSampleTypeDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(SelectItem.EXTRA_REQUEST_KEY, requestKey)
                }
            }

        const val REQUEST_KEY_SELECT_TISSUE_SAMPLE_TYPE = "REQUEST_KEY_SELECT_TISSUE_SAMPLE_TYPE"
    }

    override fun createItemDelegateFactory(context: Context): ItemDelegateFactory<TissueSampleType> {
        return Factory(context)
    }

    private class Factory(
        context: Context
    ) : EntityItemDelegateFactory<TissueSampleType>(context) {
        override fun createDataSource(): ItemDataSource<TissueSampleType> {
            return object : EntityItemDataSource<TissueSampleType>(appContext) {
                private val tissueSampleTypeRepo = TissueSampleTypeRepositoryImpl(databaseHandler)
                override suspend fun queryItems(filterText: String): List<TissueSampleType> {
                    return tissueSampleTypeRepo.queryTissueSampleTypes().filterByName(filterText)
                }
            }
        }
    }
}

// region Launch Helpers

fun FragmentActivity.tissueSampleTypeSelectionPresenter(
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<TissueSampleType>? = null,
    onItemSelected: (TissueSampleType) -> Unit
): ItemSelectionPresenter<TissueSampleType> {
    return tissueSampleTypeSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

fun Fragment.tissueSampleTypeSelectionPresenter(
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<TissueSampleType>? = null,
    onItemSelected: (TissueSampleType) -> Unit
): ItemSelectionPresenter<TissueSampleType> {
    return tissueSampleTypeSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

private fun tissueSampleTypeSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    button: Button?,
    requestKey: String?,
    hintText: String?,
    itemDisplayTextProvider: ItemDisplayTextProvider<TissueSampleType>?,
    onItemSelected: (TissueSampleType) -> Unit
): ItemSelectionPresenter<TissueSampleType> {
    val requestKeyActual = requestKey ?: SelectTissueSampleTypeDialogFragment.REQUEST_KEY_SELECT_TISSUE_SAMPLE_TYPE
    return itemSelectionPresenter(
        registrar,
        requestKeyActual,
        button,
        hintText,
        itemDisplayTextProvider.orNameAsDefault(),
        onItemSelected
    ) { SelectTissueSampleTypeDialogFragment.newInstance(requestKeyActual) }
}

// endregion
