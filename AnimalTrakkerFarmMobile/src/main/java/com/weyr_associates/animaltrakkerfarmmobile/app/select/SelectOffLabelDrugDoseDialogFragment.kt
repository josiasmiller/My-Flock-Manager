package com.weyr_associates.animaltrakkerfarmmobile.app.select

import android.content.Context
import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.core.FragmentResultListenerRegistrar
import com.weyr_associates.animaltrakkerfarmmobile.app.core.asFragmentResultListenerRegistrar
import com.weyr_associates.animaltrakkerfarmmobile.app.core.getEntityId
import com.weyr_associates.animaltrakkerfarmmobile.app.core.putEntityId
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.EntityItemDataSource
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.ItemDataSource
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.ItemDelegateFactory
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.ItemDisplayTextProvider
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.ItemSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.SelectItem
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.SelectItemDialogFragment
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.itemSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.optionalItemSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.model.itemCallbackUsingOnlyIdentity
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.DrugRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.OffLabelDrugDose
import java.time.LocalDate

class SelectOffLabelDrugDoseDialogFragment : SelectItemDialogFragment<OffLabelDrugDose>(
    OffLabelDrugDose::class.java,
    R.string.title_select_off_label_drug_dose
) {
    companion object {
        fun newInstance(drugId: EntityId, requestKey: String = REQUEST_KEY_SELECT_OFF_LABEL_DRUG_DOSE) =
            SelectOffLabelDrugDoseDialogFragment().apply {
                arguments = Bundle().apply {
                    putEntityId(EXTRA_DRUG_ID, drugId)
                    putString(SelectItem.EXTRA_REQUEST_KEY, requestKey)
                }
            }

        const val REQUEST_KEY_SELECT_OFF_LABEL_DRUG_DOSE = "REQUEST_KEY_SELECT_OFF_LABEL_DRUG_DOSE"
        private const val EXTRA_DRUG_ID = "EXTRA_DRUG_ID"
    }

    override fun createItemDelegateFactory(context: Context): ItemDelegateFactory<OffLabelDrugDose> {
        return Factory(requireContext(), requireArguments().getEntityId(EXTRA_DRUG_ID))
    }

    private class Factory(context: Context, private val drugId: EntityId) : ItemDelegateFactory<OffLabelDrugDose> {

        private val appContext = context.applicationContext

        override fun createDataSource(): ItemDataSource<OffLabelDrugDose> {
            return object : EntityItemDataSource<OffLabelDrugDose>(appContext) {
                private val repo = DrugRepositoryImpl(databaseHandler)
                override suspend fun queryItems(filterText: String): List<OffLabelDrugDose> {
                    val lowerFilterText = filterText.lowercase()
                    val currentDate = LocalDate.now()
                    return repo.queryOffLabelDrugDoses(drugId, currentDate)
                        .filter {
                            it.vetLastName.contains(lowerFilterText) ||
                            it.speciesName.contains(lowerFilterText) ||
                            it.drugDose.contains(lowerFilterText)
                        }
                }
            }
        }

        override fun createItemDiffCallback(): DiffUtil.ItemCallback<OffLabelDrugDose> {
            return itemCallbackUsingOnlyIdentity()
        }

        override fun createDisplayTextProvider(): ItemDisplayTextProvider<OffLabelDrugDose> {
            return ItemDisplayTextProvider { item -> "${item.vetLastName} : ${item.speciesName} ${item.drugDose}" }
        }
    }
}

// region Launch Helpers

fun FragmentActivity.offLabelDrugDoseSelectionPresenter(
    drugId: EntityId,
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<OffLabelDrugDose>? = null,
    onItemSelected: (OffLabelDrugDose) -> Unit
): ItemSelectionPresenter<OffLabelDrugDose> {
    return offLabelDrugDoseSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        drugId,
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

fun FragmentActivity.optionalOffLabelDrugDoseSelectionPresenter(
    drugId: EntityId,
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<OffLabelDrugDose>? = null,
    onItemSelected: (OffLabelDrugDose?) -> Unit
): ItemSelectionPresenter<OffLabelDrugDose> {
    return optionalOffLabelDrugDoseSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        drugId,
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

fun Fragment.offLabelDrugDoseSelectionPresenter(
    drugId: EntityId,
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<OffLabelDrugDose>? = null,
    onItemSelected: (OffLabelDrugDose) -> Unit
): ItemSelectionPresenter<OffLabelDrugDose> {
    return offLabelDrugDoseSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        drugId,
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

fun Fragment.optionalOffLabelDrugDoseSelectionPresenter(
    drugId: EntityId,
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<OffLabelDrugDose>? = null,
    onItemSelected: (OffLabelDrugDose?) -> Unit
): ItemSelectionPresenter<OffLabelDrugDose> {
    return optionalOffLabelDrugDoseSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        drugId,
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

private fun offLabelDrugDoseSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    drugId: EntityId,
    button: Button?,
    requestKey: String?,
    hintText: String?,
    itemDisplayTextProvider: ItemDisplayTextProvider<OffLabelDrugDose>?,
    onItemSelected: (OffLabelDrugDose) -> Unit
): ItemSelectionPresenter<OffLabelDrugDose> {
    val requestKeyActual = requestKey ?: SelectOffLabelDrugDoseDialogFragment.REQUEST_KEY_SELECT_OFF_LABEL_DRUG_DOSE
    return itemSelectionPresenter(
        registrar,
        requestKeyActual,
        button,
        hintText,
        itemDisplayTextProvider.orNameAsDefault(),
        onItemSelected
    ) { SelectOffLabelDrugDoseDialogFragment.newInstance(drugId, requestKeyActual) }
}

private fun optionalOffLabelDrugDoseSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    drugId: EntityId,
    button: Button?,
    requestKey: String?,
    hintText: String?,
    itemDisplayTextProvider: ItemDisplayTextProvider<OffLabelDrugDose>?,
    onItemSelected: (OffLabelDrugDose?) -> Unit
): ItemSelectionPresenter<OffLabelDrugDose> {
    val requestKeyActual = requestKey ?: SelectOffLabelDrugDoseDialogFragment.REQUEST_KEY_SELECT_OFF_LABEL_DRUG_DOSE
    return optionalItemSelectionPresenter(
        registrar,
        requestKeyActual,
        button,
        hintText,
        itemDisplayTextProvider.orNameAsDefault(),
        onItemSelected
    ) { SelectOffLabelDrugDoseDialogFragment.newInstance(drugId, requestKeyActual) }
}

// endregion
