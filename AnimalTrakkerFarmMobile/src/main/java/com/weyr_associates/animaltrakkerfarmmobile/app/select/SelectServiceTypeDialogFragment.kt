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
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.ServiceTypeRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.model.ServiceType
import com.weyr_associates.animaltrakkerfarmmobile.model.filterByName

class SelectServiceTypeDialogFragment : SelectItemDialogFragment<ServiceType>(
    ServiceType::class.java,
    R.string.select_title_service_type
) {
    companion object {
        @JvmStatic
        fun newInstance(requestKey: String = REQUEST_KEY_SELECT_SERVICE_TYPE) =
            SelectServiceTypeDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(SelectItem.EXTRA_REQUEST_KEY, requestKey)
                }
            }

        const val REQUEST_KEY_SELECT_SERVICE_TYPE = "REQUEST_KEY_SELECT_SERVICE_TYPE"
    }

    override fun createItemDelegateFactory(context: Context): ItemDelegateFactory<ServiceType> {
        return Factory(context)
    }

    private class Factory(context: Context) : EntityItemDelegateFactory<ServiceType>(context) {
        override fun createDataSource(): ItemDataSource<ServiceType> {
            return object : EntityItemDataSource<ServiceType>(appContext) {
                private val serviceTypeRepo = ServiceTypeRepositoryImpl(databaseHandler)
                override suspend fun queryItems(filterText: String): List<ServiceType> {
                    return serviceTypeRepo.queryAllServiceTypes().filterByName(filterText)
                }
            }
        }
    }
}

// region Launch Helpers

fun FragmentActivity.serviceTypeItemSelectionPresenter(
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<ServiceType>? = null,
    onItemSelected: (ServiceType) -> Unit
): ItemSelectionPresenter<ServiceType> {
    return serviceTypeItemSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

fun FragmentActivity.optionalServiceTypeItemSelectionPresenter(
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<ServiceType>? = null,
    onItemSelected: (ServiceType?) -> Unit
): ItemSelectionPresenter<ServiceType> {
    return optionalServiceTypeItemSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

private fun serviceTypeItemSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<ServiceType>? = null,
    onItemSelected: (ServiceType) -> Unit
): ItemSelectionPresenter<ServiceType> {
    val requestKeyActual = requestKey ?: SelectServiceTypeDialogFragment.REQUEST_KEY_SELECT_SERVICE_TYPE
    return itemSelectionPresenter(
        registrar,
        requestKeyActual,
        button,
        hintText,
        itemDisplayTextProvider.orNameAsDefault(),
        onItemSelected
    ) { SelectServiceTypeDialogFragment.newInstance(requestKeyActual) }
}

private fun optionalServiceTypeItemSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<ServiceType>? = null,
    onItemSelected: (ServiceType?) -> Unit
): ItemSelectionPresenter<ServiceType> {
    val requestKeyActual = requestKey ?: SelectServiceTypeDialogFragment.REQUEST_KEY_SELECT_SERVICE_TYPE
    return optionalItemSelectionPresenter(
        registrar,
        requestKeyActual,
        button,
        hintText,
        itemDisplayTextProvider.orNameAsDefault(),
        onItemSelected
    ) { SelectServiceTypeDialogFragment.newInstance(requestKeyActual) }
}

// endregion
