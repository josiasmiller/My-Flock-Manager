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
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.GeneticsRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadDefaultRegistryCompanyId
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.GeneticCoatColor

class SelectGeneticCoatColorDialogFragment : SelectItemDialogFragment<GeneticCoatColor>(
    GeneticCoatColor::class.java,
    R.string.title_select_coat_color
){
    companion object {
        fun newInstance(registryCompanyId: EntityId = EntityId.UNKNOWN, requestKey: String = REQUEST_KEY_SELECT_GENETIC_COAT_COLOR)
            = SelectGeneticCoatColorDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(SelectItem.EXTRA_REQUEST_KEY, requestKey)
                    putEntityId(EXTRA_REGISTRY_COMPANY_ID, registryCompanyId)
                }
        }
        const val REQUEST_KEY_SELECT_GENETIC_COAT_COLOR = "REQUEST_KEY_SELECT_GENETIC_COAT_COLOR"
        private const val EXTRA_REGISTRY_COMPANY_ID = "EXTRA_REGISTRY_COMPANY_ID"
    }

    private val registryCompanyIdArgument: EntityId by lazy {
        requireArguments().getEntityId(EXTRA_REGISTRY_COMPANY_ID, EntityId.UNKNOWN)
    }

    override fun createItemDelegateFactory(context: Context): ItemDelegateFactory<GeneticCoatColor> {
        return Factory(context)
    }

    private inner class Factory(context: Context) : EntityItemDelegateFactory<GeneticCoatColor>(context) {
        override fun createDataSource(): ItemDataSource<GeneticCoatColor> {
            return object : EntityItemDataSource<GeneticCoatColor>(appContext) {
                private val geneticsRepo = GeneticsRepositoryImpl(databaseHandler)
                private var resolvedRegistryCompanyID: EntityId? = null
                private val loadDefaultRegistryCompanyId by lazy {
                    LoadDefaultRegistryCompanyId.from(appContext, databaseHandler)
                }
                override suspend fun queryItems(filterText: String): List<GeneticCoatColor> {
                    return resolveRegistryCompanyId()?.let { geneticsRepo.queryCoatColorsByRegistry(it) }
                        ?: emptyList()
                }
                private suspend fun resolveRegistryCompanyId(): EntityId? {
                    val registryCompanyId = resolvedRegistryCompanyID
                    if (registryCompanyId != null) {
                        return registryCompanyId
                    }
                    return (registryCompanyIdArgument.takeIf { it != EntityId.UNKNOWN }
                        ?: loadDefaultRegistryCompanyId()).also { resolvedRegistryCompanyID = it }
                }
            }
        }
    }
}

// region Launch Helpers

fun FragmentActivity.geneticCoatColorSelectionPresenter(
    registryCompanyId: EntityId = EntityId.UNKNOWN,
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<GeneticCoatColor>? = null,
    onItemSelected: (GeneticCoatColor) -> Unit
): ItemSelectionPresenter<GeneticCoatColor> {
    return geneticCoatColorSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        registryCompanyId,
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

fun Fragment.geneticCoatColorSelectionPresenter(
    registryCompanyId: EntityId = EntityId.UNKNOWN,
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<GeneticCoatColor>? = null,
    onItemSelected: (GeneticCoatColor) -> Unit
): ItemSelectionPresenter<GeneticCoatColor> {
    return geneticCoatColorSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        registryCompanyId,
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

private fun geneticCoatColorSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    registryCompanyId: EntityId = EntityId.UNKNOWN,
    button: Button?,
    requestKey: String?,
    hintText: String?,
    itemDisplayTextProvider: ItemDisplayTextProvider<GeneticCoatColor>?,
    onItemSelected: (GeneticCoatColor) -> Unit
): ItemSelectionPresenter<GeneticCoatColor> {
    val requestKeyActual = requestKey ?: SelectGeneticCoatColorDialogFragment.REQUEST_KEY_SELECT_GENETIC_COAT_COLOR
    return itemSelectionPresenter(
        registrar,
        requestKeyActual,
        button,
        hintText,
        itemDisplayTextProvider.orNameAsDefault(),
        onItemSelected
    ) { SelectGeneticCoatColorDialogFragment.newInstance(registryCompanyId, requestKeyActual) }
}

// endregion
