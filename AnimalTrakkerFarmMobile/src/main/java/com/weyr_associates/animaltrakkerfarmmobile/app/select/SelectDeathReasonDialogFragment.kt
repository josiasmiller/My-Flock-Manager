package com.weyr_associates.animaltrakkerfarmmobile.app.select

import android.content.Context
import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil
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
import com.weyr_associates.animaltrakkerfarmmobile.app.model.itemCallbackUsingOnlyIdentity
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.DefaultSettingsRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.ActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.app.settings.LoadActiveDefaultSettings
import com.weyr_associates.animaltrakkerfarmmobile.model.DeathReason
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.UserType
import com.weyr_associates.animaltrakkerfarmmobile.repository.database.DeathReasonRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SelectDeathReasonDialogFragment : SelectItemDialogFragment<DeathReason>(
    DeathReason::class.java,
    R.string.title_select_death_reason
) {
    companion object {
        fun newInstance(requestKey: String = REQUEST_KEY_SELECT_DEATH_REASON) =
            SelectDeathReasonDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(SelectItem.EXTRA_REQUEST_KEY, requestKey)
                }
            }

        const val REQUEST_KEY_SELECT_DEATH_REASON = "REQUEST_KEY_SELECT_DEATH_REASON"
    }

    override fun createItemDelegateFactory(context: Context): ItemDelegateFactory<DeathReason> {
        return Factory(context)
    }

    private class Factory(context: Context): ItemDelegateFactory<DeathReason> {

        private val appContext = context.applicationContext

        override fun createDataSource(): ItemDataSource<DeathReason> {
            return object : EntityItemDataSource<DeathReason>(appContext) {
                private var userInfo: Pair<EntityId?, UserType?>? = null
                private val repo = DeathReasonRepositoryImpl(databaseHandler)
                private val loadActiveDefaultSettings: LoadActiveDefaultSettings = LoadActiveDefaultSettings(
                    activeDefaultSettings = ActiveDefaultSettings.from(appContext),
                    defaultSettingsRepo = DefaultSettingsRepositoryImpl(
                        databaseHandler, ActiveDefaultSettings.from(appContext)
                    )
                )
                override suspend fun queryItems(filterText: String): List<DeathReason> {
                    val (userId, userType) = loadUserInfo()
                    return when {
                        userId != null && userType != null -> {
                            repo.queryDeathReasonsByUser(userId, userType)
                        }
                        else -> {
                            repo.queryDefaultDeathReasons()
                        }
                    }.filter {
                        it.reason.lowercase().contains(filterText.lowercase())
                    }
                }

                private suspend fun loadUserInfo(): Pair<EntityId?, UserType?> {
                    userInfo?.let { return it }
                    val defaultSettings = withContext(Dispatchers.IO) {
                        loadActiveDefaultSettings()
                    }
                    return Pair(defaultSettings.userId, defaultSettings.userType)
                        .also { userInfo = it }
                }
            }
        }

        override fun createItemDiffCallback(): DiffUtil.ItemCallback<DeathReason> {
            return itemCallbackUsingOnlyIdentity()
        }

        override fun createDisplayTextProvider(): ItemDisplayTextProvider<DeathReason> {
            return ItemDisplayTextProvider { item -> item.reason }
        }
    }
}

// region Launch Helpers

fun FragmentActivity.deathReasonSelectionPresenter(
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<DeathReason>? = null,
    onItemSelected: (DeathReason) -> Unit
): ItemSelectionPresenter<DeathReason> {
    return deathReasonSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

fun Fragment.deathReasonSelectionPresenter(
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<DeathReason>? = null,
    onItemSelected: (DeathReason) -> Unit
): ItemSelectionPresenter<DeathReason> {
    return deathReasonSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

private fun deathReasonSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    button: Button?,
    requestKey: String?,
    hintText: String?,
    itemDisplayTextProvider: ItemDisplayTextProvider<DeathReason>?,
    onItemSelected: (DeathReason) -> Unit
): ItemSelectionPresenter<DeathReason> {
    val requestKeyActual = requestKey ?: SelectDeathReasonDialogFragment.REQUEST_KEY_SELECT_DEATH_REASON
    return itemSelectionPresenter(
        registrar,
        requestKeyActual,
        button,
        hintText,
        itemDisplayTextProvider ?: ItemDisplayTextProvider { item -> item.reason },
        onItemSelected
    ) { SelectDeathReasonDialogFragment.newInstance(requestKeyActual) }
}

// endregion
