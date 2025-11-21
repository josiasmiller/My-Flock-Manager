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
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.DefaultSettingsRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.EvaluationRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.ActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadDefaultUserInfo
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.UserInfo
import com.weyr_associates.animaltrakkerfarmmobile.model.ItemEntry
import com.weyr_associates.animaltrakkerfarmmobile.model.filterByName

class SelectEvaluationDialogFragment : SelectItemDialogFragment<ItemEntry>(
    ItemEntry::class.java,
    R.string.title_select_evaluation,
) {
    companion object {
        fun newInstance(requestKey: String = REQUEST_KEY_SELECT_ANIMAL_EVALUTION) =
            SelectEvaluationDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(SelectItem.EXTRA_REQUEST_KEY, requestKey)
                }
            }

        const val REQUEST_KEY_SELECT_ANIMAL_EVALUTION = "REQUEST_KEY_SELECT_ANIMAL_EVALUTION"
    }

    override fun createItemDelegateFactory(context: Context): ItemDelegateFactory<ItemEntry> {
        return Factory(context)
    }

    private class Factory(
        context: Context
    ) : EntityItemDelegateFactory<ItemEntry>(context) {
        override fun createDataSource(): ItemDataSource<ItemEntry> {
            return object : EntityItemDataSource<ItemEntry>(appContext) {
                private var userInfo: UserInfo? = null
                private val savedEvalRepo = EvaluationRepositoryImpl(databaseHandler)
                private val loadDefaultUserInfo = LoadDefaultUserInfo(
                    LoadActiveDefaultSettings(
                        activeDefaultSettings = ActiveDefaultSettings.from(appContext),
                        defaultSettingsRepo = DefaultSettingsRepositoryImpl(
                            databaseHandler, ActiveDefaultSettings.from(appContext)
                        )
                    )
                )
                override suspend fun queryItems(filterText: String): List<ItemEntry> {
                    val userInfo = loadUserInfo()
                    return savedEvalRepo.querySavedEvaluationsForUser(userInfo.userId, userInfo.userType)
                        .filterByName(filterText)
                }

                private suspend fun loadUserInfo(): UserInfo {
                    return userInfo?.let { return it } ?: loadDefaultUserInfo().also {
                        userInfo = it
                    }
                }
            }
        }
    }
}

// region Launch Helpers

fun FragmentActivity.evaluationItemSelectionPresenter(
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<ItemEntry>? = null,
    onItemSelected: (ItemEntry) -> Unit
): ItemSelectionPresenter<ItemEntry> {
    return evaluationItemSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

fun FragmentActivity.optionalEvaluationItemSelectionPresenter(
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<ItemEntry>? = null,
    onItemSelected: (ItemEntry?) -> Unit
): ItemSelectionPresenter<ItemEntry> {
    return optionalEvaluationItemSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

private fun evaluationItemSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<ItemEntry>? = null,
    onItemSelected: (ItemEntry) -> Unit
): ItemSelectionPresenter<ItemEntry> {
    val requestKeyActual = requestKey ?: SelectEvaluationDialogFragment.REQUEST_KEY_SELECT_ANIMAL_EVALUTION
    return itemSelectionPresenter(
        registrar,
        requestKeyActual,
        button,
        hintText,
        itemDisplayTextProvider.orNameAsDefault(),
        onItemSelected
    ) { SelectEvaluationDialogFragment.newInstance(requestKeyActual) }
}

private fun optionalEvaluationItemSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<ItemEntry>? = null,
    onItemSelected: (ItemEntry?) -> Unit
): ItemSelectionPresenter<ItemEntry> {
    val requestKeyActual = requestKey ?: SelectEvaluationDialogFragment.REQUEST_KEY_SELECT_ANIMAL_EVALUTION
    return optionalItemSelectionPresenter(
        registrar,
        requestKeyActual,
        button,
        hintText,
        itemDisplayTextProvider.orNameAsDefault(),
        onItemSelected
    ) { SelectEvaluationDialogFragment.newInstance(requestKeyActual) }
}


// endregion
