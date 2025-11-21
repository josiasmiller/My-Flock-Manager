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
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.StateRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.select.SelectStateDialogFragment.Companion.REQUEST_KEY_SELECT_STATE
import com.weyr_associates.animaltrakkerfarmmobile.model.State
import com.weyr_associates.animaltrakkerfarmmobile.model.filterByName

class SelectStateDialogFragment : SelectItemDialogFragment<State>(
    State::class.java,
    R.string.title_select_state
) {

    companion object {
        @JvmStatic
        fun newInstance(requestKey: String = REQUEST_KEY_SELECT_STATE) =
            SelectStateDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(SelectItem.EXTRA_REQUEST_KEY, requestKey)
                }
            }

        const val REQUEST_KEY_SELECT_STATE = "REQUEST_KEY_SELECT_STATE"
    }

    override fun createItemDelegateFactory(context: Context): ItemDelegateFactory<State> {
        return Factory(context)
    }

    private class Factory(
        context: Context
    ) : EntityItemDelegateFactory<State>(context) {
        override fun createDataSource(): ItemDataSource<State> {
            return object : EntityItemDataSource<State>(appContext) {
                private val stateRepo = StateRepositoryImpl(databaseHandler)
                override suspend fun queryItems(filterText: String): List<State> {
                    return stateRepo.queryStates().filterByName(filterText)
                }
            }
        }
    }
}

// region Launch Helpers

fun FragmentActivity.stateSelectionPresenter(
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<State>? = null,
    onItemSelected: (State) -> Unit
): ItemSelectionPresenter<State> {
    return stateSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

fun Fragment.stateSelectionPresenter(
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<State>? = null,
    onItemSelected: (State) -> Unit
): ItemSelectionPresenter<State> {
    return stateSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

private fun stateSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    button: Button?,
    requestKey: String?,
    hintText: String?,
    itemDisplayTextProvider: ItemDisplayTextProvider<State>?,
    onItemSelected: (State) -> Unit
): ItemSelectionPresenter<State> {
    val requestKeyActual = requestKey ?: REQUEST_KEY_SELECT_STATE
    return itemSelectionPresenter(
        registrar,
        requestKeyActual,
        button,
        hintText,
        itemDisplayTextProvider.orNameAsDefault(),
        onItemSelected
    ) { SelectStateDialogFragment.newInstance(requestKeyActual) }
}

// endregion
