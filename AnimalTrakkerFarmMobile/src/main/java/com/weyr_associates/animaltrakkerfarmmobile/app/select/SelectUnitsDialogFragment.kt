package com.weyr_associates.animaltrakkerfarmmobile.app.select

import android.content.Context
import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
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
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.UnitOfMeasureRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.UnitOfMeasure
import com.weyr_associates.animaltrakkerfarmmobile.model.filterByName

class SelectUnitsDialogFragment : SelectItemDialogFragment<UnitOfMeasure>(
    UnitOfMeasure::class.java,
    R.string.title_select_units
) {
    companion object {
        fun newInstance(unitsTypeId: EntityId, requestKey: String = REQUEST_KEY_SELECT_UNITS) =
            SelectUnitsDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(SelectItem.EXTRA_REQUEST_KEY, requestKey)
                    putEntityId(EXTRA_UNITS_TYPE_ID, unitsTypeId)
                }
            }

        const val REQUEST_KEY_SELECT_UNITS = "REQUEST_KEY_SELECT_UNITS"
        private const val EXTRA_UNITS_TYPE_ID = "EXTRA_UNITS_TYPE_ID"
        private val DEFAULT_UNITS_TYPE_ID = EntityId.UNKNOWN
    }

    private val unitsTypeId: EntityId by lazy {
        requireArguments().getEntityId(EXTRA_UNITS_TYPE_ID, DEFAULT_UNITS_TYPE_ID)
    }

    override fun createItemDelegateFactory(context: Context): ItemDelegateFactory<UnitOfMeasure> {
        return Factory(context, unitsTypeId)
    }

    private class Factory(
        context: Context,
        private val typeId: EntityId
    ) : EntityItemDelegateFactory<UnitOfMeasure>(context) {
        override fun createDataSource(): ItemDataSource<UnitOfMeasure> {
            return object : EntityItemDataSource<UnitOfMeasure>(appContext) {
                private val repo = UnitOfMeasureRepositoryImpl(databaseHandler)
                override suspend fun queryItems(filterText: String): List<UnitOfMeasure> {
                    //We intend this to return no results if the DEFAULT
                    return repo.queryUnitsOfMeasureByType(typeId).filterByName(filterText)
                }
            }
        }
    }
}

// region Launch Helpers

fun FragmentActivity.unitsSelectionPresenter(
    unitsTypeId: EntityId,
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<UnitOfMeasure>? = null,
    onItemSelected: (UnitOfMeasure) -> Unit
): ItemSelectionPresenter<UnitOfMeasure> {
    return unitsSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        unitsTypeId,
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

fun FragmentActivity.optionalUnitsSelectionPresenter(
    unitsTypeId: EntityId,
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<UnitOfMeasure>? = null,
    onItemSelected: (UnitOfMeasure?) -> Unit
): ItemSelectionPresenter<UnitOfMeasure> {
    return optionalUnitsSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        unitsTypeId,
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

fun FragmentActivity.optionalUnitsSelectionPresenter(
    unitsTypeIdFrom: () -> EntityId,
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<UnitOfMeasure>? = null,
    onItemSelected: (UnitOfMeasure?) -> Unit
): ItemSelectionPresenter<UnitOfMeasure> {
    return optionalUnitsSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        unitsTypeIdFrom,
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

fun Fragment.unitsSelectionPresenter(
    unitsTypeId: EntityId,
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<UnitOfMeasure>? = null,
    onItemSelected: (UnitOfMeasure) -> Unit
): ItemSelectionPresenter<UnitOfMeasure> {
    return unitsSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        unitsTypeId,
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

fun Fragment.optionalUnitsSelectionPresenter(
    unitsTypeId: EntityId,
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<UnitOfMeasure>? = null,
    onItemSelected: (UnitOfMeasure?) -> Unit
): ItemSelectionPresenter<UnitOfMeasure> {
    return optionalUnitsSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        unitsTypeId,
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

private fun unitsSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    unitsTypeId: EntityId,
    button: Button?,
    requestKey: String?,
    hintText: String?,
    itemDisplayTextProvider: ItemDisplayTextProvider<UnitOfMeasure>?,
    onItemSelected: (UnitOfMeasure) -> Unit
): ItemSelectionPresenter<UnitOfMeasure> {
    val requestKeyActual = requestKey ?: SelectUnitsDialogFragment.REQUEST_KEY_SELECT_UNITS
    return itemSelectionPresenter(
        registrar,
        requestKeyActual,
        button,
        hintText,
        itemDisplayTextProvider.orNameAsDefault(),
        onItemSelected
    ) { SelectUnitsDialogFragment.newInstance(unitsTypeId, requestKeyActual) }
}

private fun optionalUnitsSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    unitsTypeId: EntityId,
    button: Button?,
    requestKey: String?,
    hintText: String?,
    itemDisplayTextProvider: ItemDisplayTextProvider<UnitOfMeasure>?,
    onItemSelected: (UnitOfMeasure?) -> Unit
): ItemSelectionPresenter<UnitOfMeasure> {
    val requestKeyActual = requestKey ?: SelectUnitsDialogFragment.REQUEST_KEY_SELECT_UNITS
    return optionalItemSelectionPresenter(
        registrar,
        requestKeyActual,
        button,
        hintText,
        itemDisplayTextProvider.orNameAsDefault(),
        onItemSelected
    ) { SelectUnitsDialogFragment.newInstance(unitsTypeId, requestKeyActual) }
}

private fun optionalUnitsSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    unitsTypeIdFrom: () -> EntityId,
    button: Button?,
    requestKey: String?,
    hintText: String?,
    itemDisplayTextProvider: ItemDisplayTextProvider<UnitOfMeasure>?,
    onItemSelected: (UnitOfMeasure?) -> Unit
): ItemSelectionPresenter<UnitOfMeasure> {
    val requestKeyActual = requestKey ?: SelectUnitsDialogFragment.REQUEST_KEY_SELECT_UNITS
    return optionalItemSelectionPresenter(
        registrar,
        requestKeyActual,
        button,
        hintText,
        itemDisplayTextProvider.orNameAsDefault(),
        onItemSelected
    ) { SelectUnitsDialogFragment.newInstance(unitsTypeIdFrom.invoke(), requestKeyActual) }
}

// endregion
