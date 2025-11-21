package com.weyr_associates.animaltrakkerfarmmobile.app.select

import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.DiffUtil
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.EntityItemDataSource
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.ItemDataSource
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.ItemDelegateFactory
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.ItemDisplayTextProvider
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.SelectItem
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.SelectItemDialogFragment
import com.weyr_associates.animaltrakkerfarmmobile.app.model.itemCallbackUsingOnlyIdentity
import com.weyr_associates.animaltrakkerfarmmobile.app.repository.database.IdRemoveReasonRepositoryImpl
import com.weyr_associates.animaltrakkerfarmmobile.model.IdRemoveReason

class SelectIdRemoveReasonDialogFragment : SelectItemDialogFragment<IdRemoveReason>(
    IdRemoveReason::class.java,
    R.string.title_select_id_remove_reason,
    provideFilter = false
) {

    companion object {
        @JvmStatic
        fun newInstance(
            requestKey: String = REQUEST_KEY_SELECT_ID_REMOVE_REASON,
            associatedData: Bundle? = null
        ) = SelectIdRemoveReasonDialogFragment().apply {
            arguments = Bundle().apply {
                putString(SelectItem.EXTRA_REQUEST_KEY, requestKey)
                associatedData?.let { putBundle(SelectItem.EXTRA_ASSOCIATED_DATA, it) }
            }
        }

        const val REQUEST_KEY_SELECT_ID_REMOVE_REASON = "REQUEST_KEY_SELECT_ID_REMOVE_REASON"
    }

    override fun createItemDelegateFactory(context: Context): ItemDelegateFactory<IdRemoveReason> {
        return Factory(context)
    }

    private class Factory(
        context: Context
    ) : ItemDelegateFactory<IdRemoveReason> {

        private val appContext = context.applicationContext

        override fun createDataSource(): ItemDataSource<IdRemoveReason> {
            return object : EntityItemDataSource<IdRemoveReason>(appContext) {
                private val removeReasonRepo = IdRemoveReasonRepositoryImpl (databaseHandler)
                override suspend fun queryItems(filterText: String): List<IdRemoveReason> {
                    return removeReasonRepo.queryIdRemoveReasons()
                }
            }
        }

        override fun createItemDiffCallback(): DiffUtil.ItemCallback<IdRemoveReason> {
            return itemCallbackUsingOnlyIdentity()
        }

        override fun createDisplayTextProvider(): ItemDisplayTextProvider<IdRemoveReason> {
            return ItemDisplayTextProvider { it.text }
        }
    }
}
