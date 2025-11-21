package com.weyr_associates.animaltrakkerfarmmobile.app.select

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.DrugTypePresentation
import com.weyr_associates.animaltrakkerfarmmobile.app.core.FragmentResultListenerRegistrar
import com.weyr_associates.animaltrakkerfarmmobile.app.core.asFragmentResultListenerRegistrar
import com.weyr_associates.animaltrakkerfarmmobile.app.core.getEntityId
import com.weyr_associates.animaltrakkerfarmmobile.app.core.getEntityIdSet
import com.weyr_associates.animaltrakkerfarmmobile.app.core.putEntityId
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
import com.weyr_associates.animaltrakkerfarmmobile.model.Drug
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.filterByName

class SelectDrugDialogFragment : SelectItemDialogFragment<Drug> (
    Drug::class.java,
    titleResId = R.string.title_select_drug
) {
    companion object {
        fun newInstance(
            drugTypeId: EntityId? = null,
            excludeDrugIds: Set<EntityId> = emptySet(),
            requestKey: String = REQUEST_KEY_SELECT_DRUG
        ) = SelectDrugDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(SelectItem.EXTRA_REQUEST_KEY, requestKey)
                    drugTypeId?.let { putEntityId(EXTRA_DRUG_TYPE_ID, drugTypeId) }
                    putEntityIdSet(EXTRA_EXCLUDED_DRUG_IDS, excludeDrugIds)
                }
            }

        const val REQUEST_KEY_SELECT_DRUG = "REQUEST_KEY_SELECT_DRUG"
        private const val EXTRA_DRUG_TYPE_ID = "EXTRA_DRUG_TYPE_ID"
        private const val EXTRA_EXCLUDED_DRUG_IDS = "EXTRA_EXCLUDED_DRUG_IDS"
    }

    private val drugTypeId: EntityId? by lazy {
        requireArguments().takeIf { it.containsKey(EXTRA_DRUG_TYPE_ID) }
            ?.getEntityId(EXTRA_DRUG_TYPE_ID)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        drugTypeId?.let {
            setTitle(
                getString(
                    R.string.title_select_drug_format,
                    DrugTypePresentation.nameForType(requireContext(), it)
                )
            )
        }
    }

    override fun createItemDelegateFactory(context: Context): ItemDelegateFactory<Drug> {
        return Factory(
            context,
            drugTypeId,
            requireArguments().getEntityIdSet(EXTRA_EXCLUDED_DRUG_IDS)
        )
    }

    private class Factory(
        context: Context,
        private val drugTypeId: EntityId?,
        private val excludedDrugIds: Set<EntityId>
    ) : EntityItemDelegateFactory<Drug>(context) {
        override fun createDataSource(): ItemDataSource<Drug> {
            return object : EntityItemDataSource<Drug>(appContext) {
                val drugRepository = DrugRepositoryImpl(databaseHandler)
                override suspend fun queryItems(filterText: String): List<Drug> {
                    return queryDrugs()
                        .filter { !excludedDrugIds.contains(it.id) }
                        .filterByName(filterText)
                }
                private suspend fun queryDrugs(): List<Drug> = if (drugTypeId == null) {
                    drugRepository.queryAllDrugs()
                } else {
                    drugRepository.queryDrugsByType(drugTypeId)
                }
            }
        }

        override fun createDisplayTextProvider(): ItemDisplayTextProvider<Drug> {
            return ItemDisplayTextProvider { item -> item.name }
        }
    }
}

// region Launch Helpers

fun FragmentActivity.drugSelectionPresenter(
    drugTypeId: EntityId? = null,
    excludedDrugIds: Set<EntityId> = emptySet(),
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<Drug>? = null,
    onItemSelected: (Drug) -> Unit
): ItemSelectionPresenter<Drug> {
    return drugSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        drugTypeId,
        excludedDrugIds,
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

fun Fragment.drugSelectionPresenter(
    drugTypeId: EntityId? = null,
    excludedDrugIds: Set<EntityId> = emptySet(),
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<Drug>? = null,
    onItemSelected: (Drug) -> Unit
): ItemSelectionPresenter<Drug> {
    return drugSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        drugTypeId,
        excludedDrugIds,
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

private fun drugSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    drugTypeId: EntityId? = null,
    excludedDrugIds: Set<EntityId> = emptySet(),
    button: Button?,
    requestKey: String?,
    hintText: String?,
    itemDisplayTextProvider: ItemDisplayTextProvider<Drug>?,
    onItemSelected: (Drug) -> Unit
): ItemSelectionPresenter<Drug> {
    val requestKeyActual = requestKey ?: SelectDrugDialogFragment.REQUEST_KEY_SELECT_DRUG
    return itemSelectionPresenter(
        registrar,
        requestKeyActual,
        button,
        hintText,
        itemDisplayTextProvider.orNameAsDefault(),
        onItemSelected
    ) { SelectDrugDialogFragment.newInstance(drugTypeId, excludedDrugIds, requestKeyActual) }
}

// endregion
