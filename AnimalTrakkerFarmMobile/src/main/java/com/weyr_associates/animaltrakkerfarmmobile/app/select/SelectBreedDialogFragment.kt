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
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.BreedRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.select.SelectBreedDialogFragment.Companion.REQUEST_KEY_SELECT_BREED
import com.weyr_associates.animaltrakkerfarmmobile.model.Breed
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.filterByName

class SelectBreedDialogFragment : SelectItemDialogFragment<Breed>(
    Breed::class.java,
    R.string.title_select_breed
) {

    companion object {
        @JvmStatic
        fun newInstance(speciesId: EntityId, requestKey: String = REQUEST_KEY_SELECT_BREED) =
            SelectBreedDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(SelectItem.EXTRA_REQUEST_KEY, requestKey)
                    putEntityId(EXTRA_SPECIES_ID, speciesId)
                }
            }

        const val REQUEST_KEY_SELECT_BREED = "REQUEST_KEY_SELECT_BREED"
        private const val EXTRA_SPECIES_ID = "EXTRA_SPECIES_ID"
    }

    override fun createItemDelegateFactory(context: Context): ItemDelegateFactory<Breed> {
        return Factory(context, requireArguments().getEntityId(EXTRA_SPECIES_ID))
    }

    private class Factory(
        context: Context,
        private val speciesId: EntityId
    ) : EntityItemDelegateFactory<Breed>(context) {
        override fun createDataSource(): ItemDataSource<Breed> {
            return object : EntityItemDataSource<Breed>(appContext) {
                private val breedRepo = BreedRepositoryImpl(databaseHandler)
                override suspend fun queryItems(filterText: String): List<Breed> {
                    return breedRepo.queryBreedsForSpecies(speciesId)
                        .filterByName(filterText)
                }
            }
        }
    }
}

// region Launch Helpers

fun FragmentActivity.breedSelectionPresenter(
    speciesId: EntityId,
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<Breed>? = null,
    onItemSelected: (Breed) -> Unit
): ItemSelectionPresenter<Breed> {
    return breedSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        speciesId,
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

fun Fragment.breedSelectionPresenter(
    speciesId: EntityId,
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<Breed>? = null,
    onItemSelected: (Breed) -> Unit
): ItemSelectionPresenter<Breed> {
    return breedSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        speciesId,
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

private fun breedSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    speciesId: EntityId,
    button: Button?,
    requestKey: String?,
    hintText: String?,
    itemDisplayTextProvider: ItemDisplayTextProvider<Breed>?,
    onItemSelected: (Breed) -> Unit
): ItemSelectionPresenter<Breed> {
    val requestKeyActual = requestKey ?: REQUEST_KEY_SELECT_BREED
    return itemSelectionPresenter(
        registrar,
        requestKeyActual,
        button,
        hintText,
        itemDisplayTextProvider.orNameAsDefault(),
        onItemSelected
    ) { SelectBreedDialogFragment.newInstance(speciesId, requestKeyActual) }
}

// endregion
