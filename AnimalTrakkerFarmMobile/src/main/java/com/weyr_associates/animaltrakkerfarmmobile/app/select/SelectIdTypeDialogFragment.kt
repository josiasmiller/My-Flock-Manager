package com.weyr_associates.animaltrakkerfarmmobile.app.select

import android.content.Context
import android.os.Bundle
import android.widget.Button
import androidx.annotation.StringDef
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
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.IdTypeRepository
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.IdTypeRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.select.SelectIdTypeDialogFragment.Companion.ACTION_SELECT_ID_TYPE_FOR_ID
import com.weyr_associates.animaltrakkerfarmmobile.app.select.SelectIdTypeDialogFragment.Companion.ACTION_SELECT_ID_TYPE_FOR_SEARCH
import com.weyr_associates.animaltrakkerfarmmobile.app.select.SelectIdTypeDialogFragment.Companion.REQUEST_KEY_SELECT_ID_TYPE
import com.weyr_associates.animaltrakkerfarmmobile.app.select.SelectIdTypeDialogFragment.Companion.SelectIdTypeAction
import com.weyr_associates.animaltrakkerfarmmobile.model.IdType
import com.weyr_associates.animaltrakkerfarmmobile.model.filterByName

class SelectIdTypeDialogFragment : SelectItemDialogFragment<IdType>(
    IdType::class.java,
    R.string.title_select_id_type,
    provideFilter = false
) {

    companion object {

        const val ACTION_SELECT_ID_TYPE_FOR_ID = "ACTION_SELECT_ID_TYPE_FOR_ID"
        const val ACTION_SELECT_ID_TYPE_FOR_SEARCH = "ACTION_SELECT_ID_TYPE_FOR_SEARCH"

        @StringDef(ACTION_SELECT_ID_TYPE_FOR_ID, ACTION_SELECT_ID_TYPE_FOR_SEARCH)
        annotation class SelectIdTypeAction

        @JvmStatic
        fun newInstance(
            requestKey: String = REQUEST_KEY_SELECT_ID_TYPE,
            @SelectIdTypeAction action: String = ACTION_SELECT_ID_TYPE_FOR_ID,
            isOptional: Boolean = false
        ) =
            SelectIdTypeDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(SelectItem.EXTRA_REQUEST_KEY, requestKey)
                    putString(EXTRA_SELECT_ID_TYPE_ACTION, action)
                    putBoolean(SelectItem.EXTRA_IS_OPTIONAL, isOptional)
                }
            }

        const val REQUEST_KEY_SELECT_ID_TYPE = "REQUEST_KEY_SELECT_ID_TYPE"
        private const val EXTRA_SELECT_ID_TYPE_ACTION = "EXTRA_SELECT_ID_TYPE_ACTION"
    }

    override fun createItemDelegateFactory(context: Context): ItemDelegateFactory<IdType> {
        val action = requireArguments().getString(
            EXTRA_SELECT_ID_TYPE_ACTION, ACTION_SELECT_ID_TYPE_FOR_ID
        )
        return Factory(context, action)
    }

    private class Factory(
        context: Context, @SelectIdTypeAction private val action: String
    ) : EntityItemDelegateFactory<IdType>(context) {
        override fun createDataSource(): ItemDataSource<IdType> {
            return createDataSource(
                query = when (action) {
                    ACTION_SELECT_ID_TYPE_FOR_SEARCH -> { idTypeRepo: IdTypeRepository ->
                        idTypeRepo.queryIdTypesForSearch()
                    }
                    else -> { idTypeRepo: IdTypeRepository ->
                        idTypeRepo.queryIdTypes() }
            })
        }
        private fun createDataSource(query: (IdTypeRepository) -> List<IdType>): EntityItemDataSource<IdType> {
            return object : EntityItemDataSource<IdType>(appContext) {
                val idTypeRepo = IdTypeRepositoryImpl(databaseHandler)
                override suspend fun queryItems(filterText: String): List<IdType> {
                    return query(idTypeRepo).filterByName(filterText)
                }
            }
        }
    }
}

// region Launch Helpers

/////////////////// FOR ID TYPE_SELECTION FOR SEARCH /////////////////////////

fun FragmentActivity.idTypeSelectionForSearchPresenter(
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<IdType>? = null,
    onItemSelected: (IdType) -> Unit
): ItemSelectionPresenter<IdType> {
    return idTypeSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        ACTION_SELECT_ID_TYPE_FOR_SEARCH,
        onItemSelected
    )
}

/////////////////// FOR ID TYPE SELECTION FOR IDS ///////////////////////////

fun FragmentActivity.idTypeSelectionPresenter(
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<IdType>? = null,
    onItemSelected: (IdType) -> Unit
): ItemSelectionPresenter<IdType> {
    return idTypeSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        ACTION_SELECT_ID_TYPE_FOR_ID,
        onItemSelected
    )
}

fun FragmentActivity.optionalIdTypeSelectionPresenter(
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<IdType>? = null,
    onItemSelected: (IdType?) -> Unit
): ItemSelectionPresenter<IdType> {
    return optionalIdTypeSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        ACTION_SELECT_ID_TYPE_FOR_ID,
        onItemSelected
    )
}

fun Fragment.idTypeSelectionPresenter(
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<IdType>? = null,
    onItemSelected: (IdType) -> Unit
): ItemSelectionPresenter<IdType> {
    return idTypeSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        ACTION_SELECT_ID_TYPE_FOR_ID,
        onItemSelected
    )
}

fun Fragment.optionalIdTypeSelectionPresenter(
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<IdType>? = null,
    onItemSelected: (IdType?) -> Unit
): ItemSelectionPresenter<IdType> {
    return optionalIdTypeSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        ACTION_SELECT_ID_TYPE_FOR_ID,
        onItemSelected
    )
}

fun idTypeSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<IdType>? = null,
    @SelectIdTypeAction action: String = ACTION_SELECT_ID_TYPE_FOR_ID,
    onItemSelected: (IdType) -> Unit
): ItemSelectionPresenter<IdType> {
    val requestKeyActual = requestKey ?: REQUEST_KEY_SELECT_ID_TYPE
    return itemSelectionPresenter(
        registrar,
        requestKeyActual,
        button,
        hintText,
        itemDisplayTextProvider.orNameAsDefault(),
        onItemSelected
    ) { SelectIdTypeDialogFragment.newInstance(requestKeyActual, action) }
}

fun optionalIdTypeSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<IdType>? = null,
    @SelectIdTypeAction action: String = ACTION_SELECT_ID_TYPE_FOR_ID,
    onItemSelected: (IdType?) -> Unit
): ItemSelectionPresenter<IdType> {
    val requestKeyActual = requestKey ?: REQUEST_KEY_SELECT_ID_TYPE
    return optionalItemSelectionPresenter(
        registrar,
        requestKeyActual,
        button,
        hintText,
        itemDisplayTextProvider.orNameAsDefault(),
        onItemSelected
    ) { SelectIdTypeDialogFragment.newInstance(requestKeyActual, action) }
}

// endregion
