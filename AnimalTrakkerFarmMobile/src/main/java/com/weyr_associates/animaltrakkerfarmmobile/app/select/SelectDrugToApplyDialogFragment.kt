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
import com.weyr_associates.animaltrakkerfarmmobile.model.DrugApplicationInfo
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.filterByName

class SelectDrugToApplyDialogFragment : SelectItemDialogFragment<DrugApplicationInfo> (
    DrugApplicationInfo::class.java,
    titleResId = R.string.title_select_drug
) {
    companion object {
        fun newInstance(
            drugTypeId: EntityId,
            excludeDrugIds: Set<EntityId> = emptySet(),
            requestKey: String = REQUEST_KEY_SELECT_DRUG_APPLICATION_INFO
        ) = SelectDrugToApplyDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(SelectItem.EXTRA_REQUEST_KEY, requestKey)
                    putEntityId(EXTRA_DRUG_TYPE_ID, drugTypeId)
                    putEntityIdSet(EXTRA_EXCLUDED_DRUG_IDS, excludeDrugIds)
                }
            }

        const val REQUEST_KEY_SELECT_DRUG_APPLICATION_INFO = "REQUEST_KEY_SELECT_DRUG_APPLICATION_INFO"
        private const val EXTRA_DRUG_TYPE_ID = "EXTRA_DRUG_TYPE_ID"
        private const val EXTRA_EXCLUDED_DRUG_IDS = "EXTRA_EXCLUDED_DRUG_IDS"
    }

    private val drugTypeId: EntityId by lazy {
        requireArguments().getEntityId(EXTRA_DRUG_TYPE_ID)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTitle(
            getString(
                R.string.title_select_drug_format,
                DrugTypePresentation.nameForType(requireContext(), drugTypeId)
            )
        )
    }

    override fun createItemDelegateFactory(context: Context): ItemDelegateFactory<DrugApplicationInfo> {
        return Factory(
            context,
            drugTypeId,
            requireArguments().getEntityIdSet(EXTRA_EXCLUDED_DRUG_IDS)
        )
    }

    private class Factory(
        context: Context,
        private val drugTypeId: EntityId,
        private val excludedDrugIds: Set<EntityId>
    ) : EntityItemDelegateFactory<DrugApplicationInfo>(context) {
        override fun createDataSource(): ItemDataSource<DrugApplicationInfo> {
            return object : EntityItemDataSource<DrugApplicationInfo>(appContext) {
                val drugRepository = DrugRepositoryImpl(databaseHandler)
                override suspend fun queryItems(filterText: String): List<DrugApplicationInfo> {
                    return drugRepository.queryAvailableDrugsByType(drugTypeId)
                        .filter { !excludedDrugIds.contains(it.drugId) }
                        .filterByName(filterText)
                }
            }
        }

        override fun createDisplayTextProvider(): ItemDisplayTextProvider<DrugApplicationInfo> {
            return ItemDisplayTextProvider { item -> item.name }
        }
    }
}

// region Launch Helpers

fun FragmentActivity.drugForApplicationSelectionPresenter(
    drugTypeId: EntityId,
    excludedDrugIds: Set<EntityId> = emptySet(),
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<DrugApplicationInfo>? = null,
    onItemSelected: (DrugApplicationInfo) -> Unit
): ItemSelectionPresenter<DrugApplicationInfo> {
    return drugForApplicationSelectionPresenter(
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

fun Fragment.drugForApplicationSelectionPresenter(
    drugTypeId: EntityId,
    excludedDrugIds: Set<EntityId> = emptySet(),
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<DrugApplicationInfo>? = null,
    onItemSelected: (DrugApplicationInfo) -> Unit
): ItemSelectionPresenter<DrugApplicationInfo> {
    return drugForApplicationSelectionPresenter(
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

private fun drugForApplicationSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    drugTypeId: EntityId,
    excludedDrugIds: Set<EntityId> = emptySet(),
    button: Button?,
    requestKey: String?,
    hintText: String?,
    itemDisplayTextProvider: ItemDisplayTextProvider<DrugApplicationInfo>?,
    onItemSelected: (DrugApplicationInfo) -> Unit
): ItemSelectionPresenter<DrugApplicationInfo> {
    val requestKeyActual = requestKey ?: SelectDrugToApplyDialogFragment.REQUEST_KEY_SELECT_DRUG_APPLICATION_INFO
    return itemSelectionPresenter(
        registrar,
        requestKeyActual,
        button,
        hintText,
        itemDisplayTextProvider.orNameAsDefault(),
        onItemSelected
    ) { SelectDrugToApplyDialogFragment.newInstance(drugTypeId, excludedDrugIds, requestKeyActual) }
}

// endregion
