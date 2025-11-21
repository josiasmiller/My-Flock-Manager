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
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.TissueSampleContainerTypeRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.model.TissueSampleContainerType
import com.weyr_associates.animaltrakkerfarmmobile.model.filterByName

class SelectTissueSampleContainerTypeDialogFragment : SelectItemDialogFragment<TissueSampleContainerType>(
    TissueSampleContainerType::class.java,
    R.string.title_select_tissue_sample_container_type,
    provideFilter = false
) {
    companion object {
        @JvmStatic
        fun newInstance(requestKey: String = REQUEST_KEY_SELECT_TISSUE_SAMPLE_CONTAINER_TYPE) =
            SelectTissueSampleContainerTypeDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(SelectItem.EXTRA_REQUEST_KEY, requestKey)
                }
            }

        const val REQUEST_KEY_SELECT_TISSUE_SAMPLE_CONTAINER_TYPE = "REQUEST_KEY_SELECT_TISSUE_SAMPLE_CONTAINER_TYPE"
    }

    override fun createItemDelegateFactory(context: Context): ItemDelegateFactory<TissueSampleContainerType> {
        return Factory(context)
    }

    private class Factory(
        context: Context
    ) : EntityItemDelegateFactory<TissueSampleContainerType>(context) {
        override fun createDataSource(): ItemDataSource<TissueSampleContainerType> {
            return object : EntityItemDataSource<TissueSampleContainerType>(appContext) {
                private val tissueSampleContainerTypeRepo = TissueSampleContainerTypeRepositoryImpl(databaseHandler)
                override suspend fun queryItems(filterText: String): List<TissueSampleContainerType> {
                    return tissueSampleContainerTypeRepo.queryTissueSampleContainerTypes().filterByName(filterText)
                }
            }
        }
    }
}

// region Launch Helpers

fun FragmentActivity.tissueSampleContainerTypeSelectionPresenter(
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<TissueSampleContainerType>? = null,
    onItemSelected: (TissueSampleContainerType) -> Unit
): ItemSelectionPresenter<TissueSampleContainerType> {
    return tissueSampleContainerTypeSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

fun Fragment.tissueSampleContainerTypeSelectionPresenter(
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<TissueSampleContainerType>? = null,
    onItemSelected: (TissueSampleContainerType) -> Unit
): ItemSelectionPresenter<TissueSampleContainerType> {
    return tissueSampleContainerTypeSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

private fun tissueSampleContainerTypeSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    button: Button?,
    requestKey: String?,
    hintText: String?,
    itemDisplayTextProvider: ItemDisplayTextProvider<TissueSampleContainerType>?,
    onItemSelected: (TissueSampleContainerType) -> Unit
): ItemSelectionPresenter<TissueSampleContainerType> {
    val requestKeyActual = requestKey ?: SelectTissueSampleContainerTypeDialogFragment.REQUEST_KEY_SELECT_TISSUE_SAMPLE_CONTAINER_TYPE
    return itemSelectionPresenter(
        registrar,
        requestKeyActual,
        button,
        hintText,
        itemDisplayTextProvider.orNameAsDefault(),
        onItemSelected
    ) { SelectTissueSampleContainerTypeDialogFragment.newInstance(requestKeyActual) }
}

// endregion
