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
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.LaboratoryRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.model.Laboratory
import com.weyr_associates.animaltrakkerfarmmobile.model.filterByName

class SelectLaboratoryDialogFragment : SelectItemDialogFragment<Laboratory>(
    Laboratory::class.java,
    R.string.title_select_laboratory,
    provideFilter = false
) {
    companion object {
        @JvmStatic
        fun newInstance(requestKey: String = REQUEST_KEY_SELECT_LABORATORY) =
            SelectLaboratoryDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(SelectItem.EXTRA_REQUEST_KEY, requestKey)
                }
            }

        const val REQUEST_KEY_SELECT_LABORATORY = "REQUEST_KEY_SELECT_LABORATORY"
    }

    override fun createItemDelegateFactory(context: Context): ItemDelegateFactory<Laboratory> {
        return Factory(context)
    }

    private class Factory(
        context: Context
    ) : EntityItemDelegateFactory<Laboratory>(context) {
        override fun createDataSource(): ItemDataSource<Laboratory> {
            return object : EntityItemDataSource<Laboratory>(appContext) {
                private val laboratoryRepo = LaboratoryRepositoryImpl(databaseHandler)
                override suspend fun queryItems(filterText: String): List<Laboratory> {
                    return laboratoryRepo.queryLaboratories().filterByName(filterText)
                }
            }
        }
    }
}

// region Launch Helpers

fun FragmentActivity.laboratorySelectionPresenter(
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<Laboratory>? = null,
    onItemSelected: (Laboratory) -> Unit
): ItemSelectionPresenter<Laboratory> {
    return laboratorySelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

fun Fragment.laboratorySelectionPresenter(
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<Laboratory>? = null,
    onItemSelected: (Laboratory) -> Unit
): ItemSelectionPresenter<Laboratory> {
    return laboratorySelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

private fun laboratorySelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    button: Button?,
    requestKey: String?,
    hintText: String?,
    itemDisplayTextProvider: ItemDisplayTextProvider<Laboratory>?,
    onItemSelected: (Laboratory) -> Unit
): ItemSelectionPresenter<Laboratory> {
    val requestKeyActual = requestKey ?: SelectLaboratoryDialogFragment.REQUEST_KEY_SELECT_LABORATORY
    return itemSelectionPresenter(
        registrar,
        requestKeyActual,
        button,
        hintText,
        itemDisplayTextProvider.orNameAsDefault(),
        onItemSelected
    ) { SelectLaboratoryDialogFragment.newInstance(requestKeyActual) }
}

// endregion
