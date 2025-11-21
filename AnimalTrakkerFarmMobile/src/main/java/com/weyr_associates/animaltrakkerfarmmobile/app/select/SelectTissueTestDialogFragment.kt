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
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.TissueTestRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.model.TissueTest
import com.weyr_associates.animaltrakkerfarmmobile.model.filterByName

class SelectTissueTestDialogFragment : SelectItemDialogFragment<TissueTest>(
    TissueTest::class.java,
    R.string.title_select_tissue_test
) {
    companion object {
        @JvmStatic
        fun newInstance(requestKey: String = REQUEST_KEY_SELECT_TISSUE_TEST) =
            SelectTissueTestDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(SelectItem.EXTRA_REQUEST_KEY, requestKey)
                }
            }

        const val REQUEST_KEY_SELECT_TISSUE_TEST = "REQUEST_KEY_SELECT_TISSUE_TEST"
    }

    override fun createItemDelegateFactory(context: Context): ItemDelegateFactory<TissueTest> {
        return Factory(context)
    }

    private class Factory(
        context: Context
    ) : EntityItemDelegateFactory<TissueTest>(context) {
        override fun createDataSource(): ItemDataSource<TissueTest> {
            return object : EntityItemDataSource<TissueTest>(appContext) {
                private val tissueTestRepo = TissueTestRepositoryImpl(databaseHandler)
                override suspend fun queryItems(filterText: String): List<TissueTest> {
                    return tissueTestRepo.queryTissueTests().filterByName(filterText)
                }
            }
        }
    }
}

// region Launch Helpers

fun FragmentActivity.tissueTestSelectionPresenter(
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<TissueTest>? = null,
    onItemSelected: (TissueTest) -> Unit
): ItemSelectionPresenter<TissueTest> {
    return tissueTestSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

fun Fragment.tissueTestSelectionPresenter(
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<TissueTest>? = null,
    onItemSelected: (TissueTest) -> Unit
): ItemSelectionPresenter<TissueTest> {
    return tissueTestSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

private fun tissueTestSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    button: Button?,
    requestKey: String?,
    hintText: String?,
    itemDisplayTextProvider: ItemDisplayTextProvider<TissueTest>?,
    onItemSelected: (TissueTest) -> Unit
): ItemSelectionPresenter<TissueTest> {
    val requestKeyActual = requestKey ?: SelectTissueTestDialogFragment.REQUEST_KEY_SELECT_TISSUE_TEST
    return itemSelectionPresenter(
        registrar,
        requestKeyActual,
        button,
        hintText,
        itemDisplayTextProvider.orNameAsDefault(),
        onItemSelected
    ) { SelectTissueTestDialogFragment.newInstance(requestKeyActual) }
}

// endregion
