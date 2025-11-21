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
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.ContactRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.model.Contact
import com.weyr_associates.animaltrakkerfarmmobile.model.filterByName

class SelectContactDialogFragment : SelectItemDialogFragment<Contact>(
    Contact::class.java,
    R.string.title_select_contact
) {
    companion object {
        fun newInstance(
            requestKey: String = REQUEST_KEY_SELECT_CONTACT
        ) = SelectContactDialogFragment().apply {
            arguments = Bundle().apply {
                putString(SelectItem.EXTRA_REQUEST_KEY, requestKey)
            }
        }

        const val REQUEST_KEY_SELECT_CONTACT = "REQUEST_KEY_SELECT_CONTACT"
    }

    override fun createItemDelegateFactory(context: Context): ItemDelegateFactory<Contact> {
        return Factory(context)
    }

    private class Factory(
        context: Context
    ) : EntityItemDelegateFactory<Contact>(context) {
        override fun createDataSource(): ItemDataSource<Contact> {
            return object : EntityItemDataSource<Contact>(appContext) {
                private val repo = ContactRepositoryImpl(databaseHandler)
                override suspend fun queryItems(filterText: String): List<Contact> {
                    val items = repo.queryContacts()
                    return items.filterByName(filterText)
                }
            }
        }
    }
}

// region Launch Helpers

fun FragmentActivity.contactSelectionPresenter(
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<Contact>? = null,
    onItemSelected: (Contact) -> Unit
): ItemSelectionPresenter<Contact> {
    return contactSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

fun Fragment.contactSelectionPresenter(
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<Contact>? = null,
    onItemSelected: (Contact) -> Unit
): ItemSelectionPresenter<Contact> {
    return contactSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

private fun contactSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    button: Button?,
    requestKey: String?,
    hintText: String?,
    itemDisplayTextProvider: ItemDisplayTextProvider<Contact>?,
    onItemSelected: (Contact) -> Unit
): ItemSelectionPresenter<Contact> {
    val requestKeyActual = requestKey ?: SelectContactDialogFragment.REQUEST_KEY_SELECT_CONTACT
    return itemSelectionPresenter(
        registrar,
        requestKeyActual,
        button,
        hintText,
        itemDisplayTextProvider.orNameAsDefault(),
        onItemSelected
    ) { SelectContactDialogFragment.newInstance(requestKeyActual) }
}

// endregion
