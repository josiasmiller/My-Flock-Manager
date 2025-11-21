package com.weyr_associates.animaltrakkerfarmmobile.app.select

import android.content.Context
import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil.ItemCallback
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
import com.weyr_associates.animaltrakkerfarmmobile.app.select.SelectOwnerDialogFragment.Companion.REQUEST_KEY_SELECT_OWNER
import com.weyr_associates.animaltrakkerfarmmobile.model.Owner
import com.weyr_associates.animaltrakkerfarmmobile.model.filterByName
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.OwnerRepositoryImpl

class SelectOwnerDialogFragment : SelectItemDialogFragment<Owner>(
    Owner::class.java,
    R.string.title_select_owner
) {

    companion object {
        @JvmStatic
        fun newInstance(requestKey: String = REQUEST_KEY_SELECT_OWNER) =
            SelectOwnerDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(SelectItem.EXTRA_REQUEST_KEY, requestKey)
                }
            }

        const val REQUEST_KEY_SELECT_OWNER = "REQUEST_KEY_SELECT_OWNER"
    }

    override fun createItemDelegateFactory(context: Context): ItemDelegateFactory<Owner> {
        return Factory(context)
    }

    private class Factory(context: Context) : ItemDelegateFactory<Owner> {

        private val appContext = context.applicationContext

        override fun createDataSource(): ItemDataSource<Owner> {
            return object : EntityItemDataSource<Owner>(appContext) {
                private val ownerRepo = OwnerRepositoryImpl(databaseHandler)
                override suspend fun queryItems(filterText: String): List<Owner> {
                    return ownerRepo.queryOwners().filterByName(filterText)
                }
            }
        }

        override fun createItemDiffCallback(): ItemCallback<Owner> {
            return object : ItemCallback<Owner>() {
                override fun areItemsTheSame(oldItem: Owner, newItem: Owner): Boolean {
                    return oldItem.id == newItem.id && oldItem.type == newItem.type
                }

                override fun areContentsTheSame(oldItem: Owner, newItem: Owner): Boolean {
                    return oldItem == newItem
                }
            }
        }

        override fun createDisplayTextProvider(): ItemDisplayTextProvider<Owner> {
            return NameDisplayTextProvider
        }
    }
}

// region Launch Helpers

fun FragmentActivity.ownerSelectionPresenter(
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<Owner>? = null,
    onItemSelected: (Owner) -> Unit
): ItemSelectionPresenter<Owner> {
    return ownerSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

fun Fragment.ownerSelectionPresenter(
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<Owner>? = null,
    onItemSelected: (Owner) -> Unit
): ItemSelectionPresenter<Owner> {
    return ownerSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

private fun ownerSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    button: Button?,
    requestKey: String?,
    hintText: String?,
    itemDisplayTextProvider: ItemDisplayTextProvider<Owner>?,
    onItemSelected: (Owner) -> Unit
): ItemSelectionPresenter<Owner> {
    val requestKeyActual = requestKey ?: REQUEST_KEY_SELECT_OWNER
    return itemSelectionPresenter(
        registrar,
        requestKeyActual,
        button,
        hintText,
        itemDisplayTextProvider.orNameAsDefault(),
        onItemSelected
    ) { SelectOwnerDialogFragment.newInstance(requestKeyActual) }
}

// endregion
