package com.weyr_associates.animaltrakkerfarmmobile.app.select

import android.content.Context
import androidx.recyclerview.widget.DiffUtil
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.ItemDelegateFactory
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.ItemDisplayTextProvider
import com.weyr_associates.animaltrakkerfarmmobile.app.model.itemCallbackUsingOnlyIdentity
import com.weyr_associates.animaltrakkerfarmmobile.model.HasIdentity
import com.weyr_associates.animaltrakkerfarmmobile.model.HasName

abstract class EntityItemDelegateFactory<T>(context: Context) : ItemDelegateFactory<T>
        where T : HasIdentity, T : HasName {

    protected val appContext: Context = context.applicationContext

    override fun createItemDiffCallback(): DiffUtil.ItemCallback<T> {
        return itemCallbackUsingOnlyIdentity()
    }

    override fun createDisplayTextProvider(): ItemDisplayTextProvider<T> {
        return NameDisplayTextProvider
    }
}
