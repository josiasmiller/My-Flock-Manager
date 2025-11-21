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
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.BirthTypeRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.model.BirthType
import com.weyr_associates.animaltrakkerfarmmobile.model.filterByName

class SelectBirthTypeDialogFragment : SelectItemDialogFragment<BirthType>(
    BirthType::class.java,
    R.string.select_title_birth_type
) {

    companion object {
        @JvmStatic
        fun newInstance(requestKey: String = REQUEST_KEY_SELECT_BIRTH_TYPE) =
            SelectBirthTypeDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(SelectItem.EXTRA_REQUEST_KEY, requestKey)
                }
            }

        const val REQUEST_KEY_SELECT_BIRTH_TYPE = "REQUEST_KEY_SELECT_BIRTH_TYPE"
    }

    override fun createItemDelegateFactory(context: Context): ItemDelegateFactory<BirthType> {
        return Factory(context)
    }

    private class Factory(context: Context) : EntityItemDelegateFactory<BirthType>(context) {
        override fun createDataSource(): ItemDataSource<BirthType> {
            return object : EntityItemDataSource<BirthType>(appContext) {
                private val birthTypeRepo = BirthTypeRepositoryImpl(databaseHandler)
                override suspend fun queryItems(filterText: String): List<BirthType> {
                    return birthTypeRepo.queryBirthTypes().filterByName(filterText)
                }
            }
        }
    }
}

// region Launch Helpers

fun FragmentActivity.birthTypeItemSelectionPresenter(
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<BirthType>? = null,
    onItemSelected: (BirthType) -> Unit
): ItemSelectionPresenter<BirthType> {
    return birthTypeItemSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

fun FragmentActivity.optionalBirthTypeItemSelectionPresenter(
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<BirthType>? = null,
    onItemSelected: (BirthType?) -> Unit
): ItemSelectionPresenter<BirthType> {
    return optionalBirthTypeItemSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

private fun birthTypeItemSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<BirthType>? = null,
    onItemSelected: (BirthType) -> Unit
): ItemSelectionPresenter<BirthType> {
    val requestKeyActual = requestKey ?: SelectBirthTypeDialogFragment.REQUEST_KEY_SELECT_BIRTH_TYPE
    return itemSelectionPresenter(
        registrar,
        requestKeyActual,
        button,
        hintText,
        itemDisplayTextProvider.orNameAsDefault(),
        onItemSelected
    ) { SelectBirthTypeDialogFragment.newInstance(requestKeyActual) }
}

private fun optionalBirthTypeItemSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<BirthType>? = null,
    onItemSelected: (BirthType?) -> Unit
): ItemSelectionPresenter<BirthType> {
    val requestKeyActual = requestKey ?: SelectBirthTypeDialogFragment.REQUEST_KEY_SELECT_BIRTH_TYPE
    return optionalItemSelectionPresenter(
        registrar,
        requestKeyActual,
        button,
        hintText,
        itemDisplayTextProvider.orNameAsDefault(),
        onItemSelected
    ) { SelectBirthTypeDialogFragment.newInstance(requestKeyActual) }
}

// endregion
