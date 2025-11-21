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
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.optionalItemSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.IdColorRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.select.SelectIdColorDialogFragment.Companion.REQUEST_KEY_SELECT_ID_COLOR
import com.weyr_associates.animaltrakkerfarmmobile.model.IdColor
import com.weyr_associates.animaltrakkerfarmmobile.model.filterByName

class SelectIdColorDialogFragment : SelectItemDialogFragment<IdColor>(
    IdColor::class.java,
    R.string.title_select_id_color,
    provideFilter = false
) {

    companion object {
        @JvmStatic
        fun newInstance(
            requestKey: String = REQUEST_KEY_SELECT_ID_COLOR,
            isOptional: Boolean = false
        ) =
            SelectIdColorDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(SelectItem.EXTRA_REQUEST_KEY, requestKey)
                    putBoolean(SelectItem.EXTRA_IS_OPTIONAL, isOptional)
                }
            }

        const val REQUEST_KEY_SELECT_ID_COLOR = "REQUEST_KEY_SELECT_ID_COLOR"
    }

    override fun createItemDelegateFactory(context: Context): ItemDelegateFactory<IdColor> {
        return Factory(context)
    }

    private class Factory(
        context: Context
    ) : EntityItemDelegateFactory<IdColor>(context) {
        override fun createDataSource(): ItemDataSource<IdColor> {
            return object : EntityItemDataSource<IdColor>(appContext) {
                private val idColorRepo = IdColorRepositoryImpl(databaseHandler)
                override suspend fun queryItems(filterText: String): List<IdColor> {
                    return idColorRepo.queryIdColors().filterByName(filterText)
                }
            }
        }
    }
}

// region Launch Helpers

fun FragmentActivity.idColorSelectionPresenter(
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<IdColor>? = null,
    onItemSelected: (IdColor) -> Unit
): ItemSelectionPresenter<IdColor> {
    return idColorSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

fun FragmentActivity.optionalIdColorSelectionPresenter(
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<IdColor>? = null,
    onItemSelected: (IdColor?) -> Unit
): ItemSelectionPresenter<IdColor> {
    return optionalIdColorSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

fun Fragment.idColorSelectionPresenter(
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<IdColor>? = null,
    onItemSelected: (IdColor) -> Unit
): ItemSelectionPresenter<IdColor> {
    return idColorSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

fun Fragment.optionalIdColorSelectionPresenter(
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<IdColor>? = null,
    onItemSelected: (IdColor?) -> Unit
): ItemSelectionPresenter<IdColor> {
    return optionalIdColorSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

fun idColorSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<IdColor>? = null,
    onItemSelected: (IdColor) -> Unit
): ItemSelectionPresenter<IdColor> {
    val requestKeyActual = requestKey ?: REQUEST_KEY_SELECT_ID_COLOR
    return itemSelectionPresenter(
        registrar,
        requestKeyActual,
        button,
        hintText,
        itemDisplayTextProvider.orNameAsDefault(),
        onItemSelected
    ) { SelectIdColorDialogFragment.newInstance(requestKeyActual) }
}

fun optionalIdColorSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<IdColor>? = null,
    onItemSelected: (IdColor?) -> Unit
): ItemSelectionPresenter<IdColor> {
    val requestKeyActual = requestKey ?: REQUEST_KEY_SELECT_ID_COLOR
    return optionalItemSelectionPresenter(
        registrar,
        requestKeyActual,
        button,
        hintText,
        itemDisplayTextProvider.orNameAsDefault(),
        onItemSelected
    ) { SelectIdColorDialogFragment.newInstance(requestKeyActual) }
}

// endregion
