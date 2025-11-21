package com.weyr_associates.animaltrakkerfarmmobile.app.select

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil
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
import com.weyr_associates.animaltrakkerfarmmobile.app.model.itemCallbackUsingOnlyIdentity
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.EvaluationRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.EvalTraitOption
import com.weyr_associates.animaltrakkerfarmmobile.model.filterByName

class SelectTraitOptionDialogFragment : SelectItemDialogFragment<EvalTraitOption>(
    EvalTraitOption::class.java,
    R.string.select_title_trait_option,
    provideFilter = false
) {

    companion object {

        @JvmStatic
        fun newInstance(traitId: EntityId, requestKey: String = REQUEST_KEY_SELECT_TRAIT_OPTION) =
            SelectTraitOptionDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(SelectItem.EXTRA_REQUEST_KEY, requestKey)
                    putEntityId(EXTRA_TRAIT_ID, traitId)
                }
            }

        @JvmStatic
        fun newInstance(options: List<EvalTraitOption>, requestKey: String = REQUEST_KEY_SELECT_TRAIT_OPTION) =
            SelectTraitOptionDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(SelectItem.EXTRA_REQUEST_KEY, requestKey)
                    putParcelableArrayList(EXTRA_TRAIT_OPTIONS, ArrayList(options))
                }
            }

        const val REQUEST_KEY_SELECT_TRAIT_OPTION = "REQUEST_KEY_SELECT_TRAIT_OPTION"
        private const val EXTRA_TRAIT_ID = "EXTRA_TRAIT_ID"
        private const val EXTRA_TRAIT_OPTIONS = "EXTRA_TRAIT_OPTIONS"
    }

    private val traitId: EntityId by lazy {
        requireArguments().getEntityId(EXTRA_TRAIT_ID, EntityId.UNKNOWN)
    }

    private val traitOptions: List<EvalTraitOption>? by lazy {
        requireArguments().getParcelableArrayList<EvalTraitOption>(EXTRA_TRAIT_OPTIONS)?.toList()
    }

    override fun createItemDelegateFactory(context: Context): ItemDelegateFactory<EvalTraitOption> {
        return when {
            traitId != EntityId.UNKNOWN -> OptionsFromQueryFactory(context, traitId)
            traitOptions != null -> OptionsFromParameterFactory(traitOptions!!)
            else -> throw IllegalStateException("Trait ID or trait options must be supplied.")
        }
    }

    private class OptionsFromQueryFactory(
        context: Context,
        private val traitId: EntityId
    ) : EntityItemDelegateFactory<EvalTraitOption>(context) {
        override fun createDataSource(): ItemDataSource<EvalTraitOption> {
            return object : EntityItemDataSource<EvalTraitOption>(appContext) {
                private val evaluationRepo = EvaluationRepositoryImpl(databaseHandler)
                override suspend fun queryItems(filterText: String): List<EvalTraitOption> {
                    return evaluationRepo.queryEvalTraitOptionsForTrait(traitId)
                        .filterByName(filterText)
                }
            }
        }
    }

    private class OptionsFromParameterFactory(private val traitOptions: List<EvalTraitOption>) : ItemDelegateFactory<EvalTraitOption> {
        override fun createDataSource(): ItemDataSource<EvalTraitOption> {
            return object : ItemDataSource<EvalTraitOption> {
                override suspend fun queryItems(filterText: String): List<EvalTraitOption> {
                   return traitOptions.filterByName(filterText)
                }
            }
        }

        override fun createItemDiffCallback(): DiffUtil.ItemCallback<EvalTraitOption> {
            return itemCallbackUsingOnlyIdentity()
        }

        override fun createDisplayTextProvider(): ItemDisplayTextProvider<EvalTraitOption> {
            return NameDisplayTextProvider
        }
    }
}

// region Launch Helpers

fun FragmentActivity.evalTraitOptionSelectionPresenter(
    traitId: EntityId,
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<EvalTraitOption>? = null,
    onItemSelected: (EvalTraitOption) -> Unit
): ItemSelectionPresenter<EvalTraitOption> {
    return evalTraitOptionSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        traitId,
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

fun evalTraitOptionSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    traitId: EntityId,
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<EvalTraitOption>? = null,
    onItemSelected: (EvalTraitOption) -> Unit
): ItemSelectionPresenter<EvalTraitOption> {
    val requestKeyActual = requestKey ?: SelectTraitOptionDialogFragment.REQUEST_KEY_SELECT_TRAIT_OPTION
    return itemSelectionPresenter(
        registrar,
        requestKeyActual,
        button,
        hintText,
        itemDisplayTextProvider.orNameAsDefault(),
        onItemSelected
    ) { SelectTraitOptionDialogFragment.newInstance(traitId, requestKeyActual) }
}

fun evalTraitOptionSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    optionsProvider: () -> List<EvalTraitOption>,
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<EvalTraitOption>? = null,
    onItemSelected: (EvalTraitOption) -> Unit
): ItemSelectionPresenter<EvalTraitOption> {
    val requestKeyActual = requestKey ?: SelectTraitOptionDialogFragment.REQUEST_KEY_SELECT_TRAIT_OPTION
    return itemSelectionPresenter(
        registrar,
        requestKeyActual,
        button,
        hintText,
        itemDisplayTextProvider.orNameAsDefault(),
        onItemSelected
    ) { SelectTraitOptionDialogFragment.newInstance(optionsProvider.invoke(), requestKeyActual) }
}

fun optionalEvalTraitOptionSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    optionsProvider: () -> List<EvalTraitOption>,
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<EvalTraitOption>? = null,
    onItemSelected: (EvalTraitOption?) -> Unit
): ItemSelectionPresenter<EvalTraitOption> {
    val requestKeyActual = requestKey ?: SelectTraitOptionDialogFragment.REQUEST_KEY_SELECT_TRAIT_OPTION
    return optionalItemSelectionPresenter(
        registrar,
        requestKeyActual,
        button,
        hintText,
        itemDisplayTextProvider.orNameAsDefault(),
        onItemSelected
    ) { SelectTraitOptionDialogFragment.newInstance(optionsProvider.invoke(), requestKeyActual) }
}

// endregion
