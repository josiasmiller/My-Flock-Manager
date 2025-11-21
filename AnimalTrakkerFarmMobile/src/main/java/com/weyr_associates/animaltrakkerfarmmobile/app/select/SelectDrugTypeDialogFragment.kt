package com.weyr_associates.animaltrakkerfarmmobile.app.select

import android.content.Context
import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
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
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.DrugRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.model.DrugType
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.filterByName

class SelectDrugTypeDialogFragment : SelectItemDialogFragment<DrugType>(
    DrugType::class.java,
    R.string.title_select_drug_type,
    provideFilter = false
) {
    companion object {
        fun newInstance(excludedDrugTypeIds: Set<EntityId>, requestKey: String = REQUEST_KEY_SELECT_DRUG_TYPE) =
            SelectDrugTypeDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(SelectItem.EXTRA_REQUEST_KEY, requestKey)
                    putEntityIdSet(EXTRA_EXCLUDED_DRUG_TYPE_IDS, excludedDrugTypeIds)
                }
            }

        const val REQUEST_KEY_SELECT_DRUG_TYPE = "REQUEST_KEY_SELECT_DRUG_TYPE"
        private const val EXTRA_EXCLUDED_DRUG_TYPE_IDS = "EXTRA_EXCLUDED_DRUG_TYPE_IDS"
    }

    override fun createItemDelegateFactory(context: Context): ItemDelegateFactory<DrugType> {
        return Factory(
            context,
            requireArguments().getEntityIdSet(EXTRA_EXCLUDED_DRUG_TYPE_IDS)
        )
    }

    private class Factory(
        context: Context,
        private val excludedDrugTypeIds: Set<EntityId>
    ) : EntityItemDelegateFactory<DrugType>(context) {
        override fun createDataSource(): ItemDataSource<DrugType> {
            return object : EntityItemDataSource<DrugType>(appContext) {
                val drugRepo = DrugRepositoryImpl(databaseHandler)
                override suspend fun queryItems(filterText: String): List<DrugType> {
                    return drugRepo.queryDrugTypes()
                        .filter { !excludedDrugTypeIds.contains(it.id) }
                        .filterByName(filterText)
                }
            }
        }
    }
}

// region Launch Helpers

fun FragmentActivity.drugTypeSelectionPresenter(
    excludedDrugTypeIds: Set<EntityId> = emptySet(),
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<DrugType>? = null,
    onItemSelected: (DrugType) -> Unit
): ItemSelectionPresenter<DrugType> {
    return drugTypeSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        excludedDrugTypeIds,
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

fun Fragment.drugTypeSelectionPresenter(
    excludedDrugTypeIds: Set<EntityId> = emptySet(),
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<DrugType>? = null,
    onItemSelected: (DrugType) -> Unit
): ItemSelectionPresenter<DrugType> {
    return drugTypeSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        excludedDrugTypeIds,
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

private fun drugTypeSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    excludedDrugTypeIds: Set<EntityId> = emptySet(),
    button: Button?,
    requestKey: String?,
    hintText: String?,
    itemDisplayTextProvider: ItemDisplayTextProvider<DrugType>?,
    onItemSelected: (DrugType) -> Unit
): ItemSelectionPresenter<DrugType> {
    val requestKeyActual = requestKey ?: SelectDrugTypeDialogFragment.REQUEST_KEY_SELECT_DRUG_TYPE
    return itemSelectionPresenter(
        registrar,
        requestKeyActual,
        button,
        hintText,
        itemDisplayTextProvider.orNameAsDefault(),
        onItemSelected
    ) { SelectDrugTypeDialogFragment.newInstance(excludedDrugTypeIds, requestKeyActual) }
}

// endregion
