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
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.SexRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.select.SelectSexDialogFragment.Companion.REQUEST_KEY_SELECT_SEX
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.Sex
import com.weyr_associates.animaltrakkerfarmmobile.model.filterByName

class SelectSexDialogFragment : SelectItemDialogFragment<Sex>(
    Sex::class.java,
    R.string.title_select_sex,
    provideFilter = false
) {

    companion object {
        @JvmStatic
        fun newInstance(speciesId: EntityId, requestKey: String = REQUEST_KEY_SELECT_SEX) =
            SelectSexDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(SelectItem.EXTRA_REQUEST_KEY, requestKey)
                    putEntityId(EXTRA_SPECIES_ID, speciesId)
                }
            }

        const val REQUEST_KEY_SELECT_SEX = "REQUEST_KEY_SELECT_SEX"
        private const val EXTRA_SPECIES_ID = "EXTRA_SPECIES_ID"
    }

    override fun createItemDelegateFactory(context: Context): ItemDelegateFactory<Sex> {
        return Factory(context, requireArguments().getEntityId(EXTRA_SPECIES_ID))
    }

    private class Factory(
        context: Context,
        private val speciesId: EntityId
    ) : EntityItemDelegateFactory<Sex>(context) {
        override fun createDataSource(): ItemDataSource<Sex> {
            return object : EntityItemDataSource<Sex>(appContext) {
                private val sexRepo = SexRepositoryImpl(databaseHandler)
                override suspend fun queryItems(filterText: String): List<Sex> {
                    return sexRepo.querySexesForSpeciesId(speciesId).filterByName(filterText)
                }
            }
        }
    }
}

// region Launch Helpers

fun FragmentActivity.sexSelectionPresenter(
    speciesId: EntityId,
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<Sex>? = null,
    onItemSelected: (Sex) -> Unit
): ItemSelectionPresenter<Sex> {
    return sexSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        speciesId,
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

fun Fragment.sexSelectionPresenter(
    speciesId: EntityId,
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<Sex>? = null,
    onItemSelected: (Sex) -> Unit
): ItemSelectionPresenter<Sex> {
    return sexSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        speciesId,
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

private fun sexSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    speciesId: EntityId,
    button: Button?,
    requestKey: String?,
    hintText: String?,
    itemDisplayTextProvider: ItemDisplayTextProvider<Sex>?,
    onItemSelected: (Sex) -> Unit
): ItemSelectionPresenter<Sex> {
    val requestKeyActual = requestKey ?: REQUEST_KEY_SELECT_SEX
    return itemSelectionPresenter(
        registrar,
        requestKeyActual,
        button,
        hintText,
        itemDisplayTextProvider.orNameAsDefault(),
        onItemSelected
    ) { SelectSexDialogFragment.newInstance(speciesId, requestKeyActual) }
}

// endregion
