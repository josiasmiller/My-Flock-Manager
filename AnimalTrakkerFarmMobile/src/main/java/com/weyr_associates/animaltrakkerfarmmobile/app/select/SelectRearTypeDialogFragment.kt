package com.weyr_associates.animaltrakkerfarmmobile.app.select

import android.content.Context
import android.os.Bundle
import android.widget.Button
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
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.optionalItemSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.RearTypeRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.model.RearType
import com.weyr_associates.animaltrakkerfarmmobile.model.filterByName

class SelectRearTypeDialogFragment : SelectItemDialogFragment<RearType>(
    RearType::class.java,
    R.string.select_title_rear_type
) {

    companion object {
        @JvmStatic
        fun newInstance(requestKey: String = REQUEST_KEY_SELECT_REAR_TYPE) =
            SelectRearTypeDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(SelectItem.EXTRA_REQUEST_KEY, requestKey)
                }
            }

        const val REQUEST_KEY_SELECT_REAR_TYPE = "REQUEST_KEY_SELECT_REAR_TYPE"
    }

    override fun createItemDelegateFactory(context: Context): ItemDelegateFactory<RearType> {
        return Factory(context)
    }

    private class Factory(context: Context) : EntityItemDelegateFactory<RearType>(context) {
        override fun createDataSource(): ItemDataSource<RearType> {
            return object : EntityItemDataSource<RearType>(appContext) {
                private val rearTypeRepo = RearTypeRepositoryImpl(databaseHandler)
                override suspend fun queryItems(filterText: String): List<RearType> {
                    return rearTypeRepo.queryAllRearTypes().filterByName(filterText)
                }
            }
        }
    }
}

// region Launch Helpers

fun FragmentActivity.rearTypeItemSelectionPresenter(
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<RearType>? = null,
    onItemSelected: (RearType) -> Unit
): ItemSelectionPresenter<RearType> {
    return rearTypeItemSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

fun FragmentActivity.optionalRearTypeItemSelectionPresenter(
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<RearType>? = null,
    onItemSelected: (RearType?) -> Unit
): ItemSelectionPresenter<RearType> {
    return optionalRearTypeItemSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

private fun rearTypeItemSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<RearType>? = null,
    onItemSelected: (RearType) -> Unit
): ItemSelectionPresenter<RearType> {
    val requestKeyActual = requestKey ?: SelectRearTypeDialogFragment.REQUEST_KEY_SELECT_REAR_TYPE
    return itemSelectionPresenter(
        registrar,
        requestKeyActual,
        button,
        hintText,
        itemDisplayTextProvider.orNameAsDefault(),
        onItemSelected
    ) { SelectRearTypeDialogFragment.newInstance(requestKeyActual) }
}

private fun optionalRearTypeItemSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<RearType>? = null,
    onItemSelected: (RearType?) -> Unit
): ItemSelectionPresenter<RearType> {
    val requestKeyActual = requestKey ?: SelectRearTypeDialogFragment.REQUEST_KEY_SELECT_REAR_TYPE
    return optionalItemSelectionPresenter(
        registrar,
        requestKeyActual,
        button,
        hintText,
        itemDisplayTextProvider.orNameAsDefault(),
        onItemSelected
    ) { SelectRearTypeDialogFragment.newInstance(requestKeyActual) }
}

// endregion
