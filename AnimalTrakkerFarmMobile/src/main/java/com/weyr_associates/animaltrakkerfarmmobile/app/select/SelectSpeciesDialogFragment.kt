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
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.SpeciesRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.model.Species
import com.weyr_associates.animaltrakkerfarmmobile.model.filterByName

class SelectSpeciesDialogFragment : SelectItemDialogFragment<Species>(
    Species::class.java,
    R.string.title_select_species
) {
    companion object {

        fun newInstance(requestKey: String = REQUEST_KEY_SELECT_SPECIES) =
            SelectSpeciesDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(SelectItem.EXTRA_REQUEST_KEY, requestKey)
                }
            }

        const val REQUEST_KEY_SELECT_SPECIES = "REQUEST_KEY_SELECT_SPECIES"
    }

    override fun createItemDelegateFactory(context: Context): ItemDelegateFactory<Species> {
        return Factory(context)
    }

    private class Factory(context: Context) : EntityItemDelegateFactory<Species>(context) {
        override fun createDataSource(): ItemDataSource<Species> {
            return object : EntityItemDataSource<Species>(appContext) {
                private val speciesRepository = SpeciesRepositoryImpl(databaseHandler)
                override suspend fun queryItems(filterText: String): List<Species> {
                    return speciesRepository.queryAllSpecies().filterByName(filterText)
                }
            }
        }
    }
}

// region Launch Helpers

fun FragmentActivity.speciesSelectionPresenter(
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<Species>? = null,
    onItemSelected: (Species) -> Unit
): ItemSelectionPresenter<Species> {
    return speciesSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

fun Fragment.speciesSelectionPresenter(
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<Species>? = null,
    onItemSelected: (Species) -> Unit
): ItemSelectionPresenter<Species> {
    return speciesSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

private fun speciesSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    button: Button?,
    requestKey: String?,
    hintText: String?,
    itemDisplayTextProvider: ItemDisplayTextProvider<Species>?,
    onItemSelected: (Species) -> Unit
): ItemSelectionPresenter<Species> {
    val requestKeyActual = requestKey ?: SelectSpeciesDialogFragment.REQUEST_KEY_SELECT_SPECIES
    return itemSelectionPresenter(
        registrar,
        requestKeyActual,
        button,
        hintText,
        itemDisplayTextProvider.orNameAsDefault(),
        onItemSelected
    ) { SelectSpeciesDialogFragment.newInstance(requestKeyActual) }
}

// endregion
