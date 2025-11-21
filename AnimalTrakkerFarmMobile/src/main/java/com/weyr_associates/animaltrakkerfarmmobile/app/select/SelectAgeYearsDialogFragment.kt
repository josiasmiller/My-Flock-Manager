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
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.ItemDataSource
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.ItemDelegateFactory
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.ItemDisplayTextProvider
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.ItemSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.SelectItem
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.SelectItemDialogFragment
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.intItemSelectionPresenter
import com.weyr_associates.animaltrakkerfarmmobile.app.core.widget.recyclerview.SimpleItemCallbacks
import com.weyr_associates.animaltrakkerfarmmobile.app.select.SelectAgeYearsDialogFragment.Companion.REQUEST_KEY_SELECT_AGE_YEARS

class SelectAgeYearsDialogFragment() : SelectItemDialogFragment<Int>(
    Int::class.java,
    R.string.title_select_age_in_years,
    provideFilter = false
) {

    companion object {
        @JvmStatic
        fun newInstance(requestKey: String = REQUEST_KEY_SELECT_AGE_YEARS) =
            SelectAgeYearsDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(SelectItem.EXTRA_REQUEST_KEY, requestKey)
            }
        }

        const val REQUEST_KEY_SELECT_AGE_YEARS = "REQUEST_KEY_SELECT_AGE_YEARS"
    }

    override fun createItemDelegateFactory(context: Context): ItemDelegateFactory<Int> {
        return Factory(context)
    }

    private class Factory(context: Context) : ItemDelegateFactory<Int> {

        private val appContext: Context = context.applicationContext

        override fun createDataSource(): ItemDataSource<Int> {
            return object : ItemDataSource<Int> {
                override suspend fun queryItems(filterText: String): List<Int> {
                    return (0..11).toList()
                }
            }
        }

        override fun createItemDiffCallback(): DiffUtil.ItemCallback<Int> {
            return SimpleItemCallbacks.INTEGERS
        }

        override fun createDisplayTextProvider(): ItemDisplayTextProvider<Int> {
            return AgeYearsDisplayTextProvider(appContext)
        }
    }
}

// region Launch Helpers

fun FragmentActivity.ageYearsSelectionPresenter(
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<Int>? = null,
    onItemSelected: (Int) -> Unit
): ItemSelectionPresenter<Int> {
    return ageYearsSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

fun Fragment.ageYearsSelectionPresenter(
    button: Button? = null,
    requestKey: String? = null,
    hintText: String? = null,
    itemDisplayTextProvider: ItemDisplayTextProvider<Int>? = null,
    onItemSelected: (Int) -> Unit
): ItemSelectionPresenter<Int> {
    return ageYearsSelectionPresenter(
        this.asFragmentResultListenerRegistrar(),
        button,
        requestKey,
        hintText,
        itemDisplayTextProvider,
        onItemSelected
    )
}

private fun ageYearsSelectionPresenter(
    registrar: FragmentResultListenerRegistrar,
    button: Button?,
    requestKey: String?,
    hintText: String?,
    itemDisplayTextProvider: ItemDisplayTextProvider<Int>?,
    onItemSelected: (Int) -> Unit
): ItemSelectionPresenter<Int> {
    val requestKeyActual = requestKey ?: REQUEST_KEY_SELECT_AGE_YEARS
    return intItemSelectionPresenter(
        registrar,
        requestKeyActual,
        button,
        hintText,
        itemDisplayTextProvider
            ?: AgeYearsDisplayTextProvider(registrar.context),
        onItemSelected
    ) { SelectAgeYearsDialogFragment.newInstance(requestKeyActual) }
}

// endregion
