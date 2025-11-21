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
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.EvaluationRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.Trait
import com.weyr_associates.animaltrakkerfarmmobile.model.filterByName

class SelectTraitDialogFragment : SelectItemDialogFragment<Trait>(
    Trait::class.java,
    R.string.title_select_trait
) {
    companion object {
        fun newInstance(traitTypeId: EntityId, requestKey: String = REQUEST_KEY_SELECT_TRAIT) =
            SelectTraitDialogFragment().apply {
                arguments = Bundle().apply {
                    putEntityId(EXTRA_TRAIT_TYPE_ID, traitTypeId)
                    putString(SelectItem.EXTRA_REQUEST_KEY, requestKey)
                }
            }

        const val REQUEST_KEY_SELECT_TRAIT = "REQUEST_KEY_SELECT_TRAIT"
        private const val EXTRA_TRAIT_TYPE_ID = "EXTRA_TRAIT_TYPE_ID"
    }

    override fun createItemDelegateFactory(context: Context): ItemDelegateFactory<Trait> {
        return Factory(context, requireArguments().getEntityId(EXTRA_TRAIT_TYPE_ID))
    }

    private class Factory(
        context: Context,
        private val traitTypeId: EntityId
    ) : EntityItemDelegateFactory<Trait>(context) {
        override fun createDataSource(): ItemDataSource<Trait> {
            return object : EntityItemDataSource<Trait>(appContext) {
                private val repo = EvaluationRepositoryImpl(databaseHandler)
                override suspend fun queryItems(filterText: String): List<Trait> {
                    return repo.queryTraitsByType(traitTypeId).filterByName(filterText)
                }
            }
        }
    }
}

// region Launch Helpers

fun FragmentActivity.traitSelectionPresenter(
    traitTypeId: EntityId,
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<Trait>? = null,
    onItemSelected: (Trait) -> Unit
): ItemSelectionPresenter<Trait> {
    return traitSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        traitTypeId,
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

fun FragmentActivity.optionalTraitSelectionPresenter(
    traitTypeId: EntityId,
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<Trait>? = null,
    onItemSelected: (Trait?) -> Unit
): ItemSelectionPresenter<Trait> {
    return optionalTraitSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        traitTypeId,
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

fun Fragment.traitSelectionPresenter(
    traitTypeId: EntityId,
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<Trait>? = null,
    onItemSelected: (Trait) -> Unit
): ItemSelectionPresenter<Trait> {
    return traitSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        traitTypeId,
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

fun Fragment.optionalTraitSelectionPresenter(
    traitTypeId: EntityId,
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<Trait>? = null,
    onItemSelected: (Trait?) -> Unit
): ItemSelectionPresenter<Trait> {
    return optionalTraitSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        traitTypeId,
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

private fun traitSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    traitTypeId: EntityId,
    button: Button?,
    requestKey: String?,
    hintText: String?,
    itemDisplayTextProvider: ItemDisplayTextProvider<Trait>?,
    onItemSelected: (Trait) -> Unit
): ItemSelectionPresenter<Trait> {
    val requestKeyActual = requestKey ?: SelectTraitDialogFragment.REQUEST_KEY_SELECT_TRAIT
    return itemSelectionPresenter(
        registrar,
        requestKeyActual,
        button,
        hintText,
        itemDisplayTextProvider.orNameAsDefault(),
        onItemSelected
    ) { SelectTraitDialogFragment.newInstance(traitTypeId, requestKeyActual) }
}

private fun optionalTraitSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    traitTypeId: EntityId,
    button: Button?,
    requestKey: String?,
    hintText: String?,
    itemDisplayTextProvider: ItemDisplayTextProvider<Trait>?,
    onItemSelected: (Trait?) -> Unit
): ItemSelectionPresenter<Trait> {
    val requestKeyActual = requestKey ?: SelectTraitDialogFragment.REQUEST_KEY_SELECT_TRAIT
    return optionalItemSelectionPresenter(
        registrar,
        requestKeyActual,
        button,
        hintText,
        itemDisplayTextProvider.orNameAsDefault(),
        onItemSelected
    ) { SelectTraitDialogFragment.newInstance(traitTypeId, requestKeyActual) }
}

// endregion
