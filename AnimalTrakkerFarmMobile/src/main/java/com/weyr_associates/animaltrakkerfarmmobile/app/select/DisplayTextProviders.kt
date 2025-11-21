package com.weyr_associates.animaltrakkerfarmmobile.app.select

import android.content.Context
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.core.select.ItemDisplayTextProvider
import com.weyr_associates.animaltrakkerfarmmobile.model.HasAbbreviation
import com.weyr_associates.animaltrakkerfarmmobile.model.HasName

fun <T : HasName> useNameForItemDisplayText(): ItemDisplayTextProvider<T>
    = NameDisplayTextProvider

fun <T : HasName> ItemDisplayTextProvider<T>?.orNameAsDefault(): ItemDisplayTextProvider<T>
    = this ?: NameDisplayTextProvider

fun <T : HasAbbreviation> useAbbreviationForItemDisplayText(): ItemDisplayTextProvider<T>
    = AbbreviationDisplayTextProvider

fun <T : HasAbbreviation> ItemDisplayTextProvider<T>?.orAbbreviationAsDefault(): ItemDisplayTextProvider<T>
    = this ?: AbbreviationDisplayTextProvider

object NameDisplayTextProvider : ItemDisplayTextProvider<HasName> {
    override fun displayTextForItem(item: HasName): String {
        return item.name
    }
}

object AbbreviationDisplayTextProvider : ItemDisplayTextProvider<HasAbbreviation> {
    override fun displayTextForItem(item: HasAbbreviation): String {
        return item.abbreviation
    }
}

class AgeYearsDisplayTextProvider(private val context: Context) : ItemDisplayTextProvider<Int> {
    override fun displayTextForItem(item: Int): String {
        val quantityString = context.resources.getQuantityString(
            R.plurals.age_number_of_years,
            item
        )
        return context.getString(
            R.string.format_age_years, item, quantityString
        )
    }
}

class AgeMonthsDisplayTextProvider(private val context: Context) : ItemDisplayTextProvider<Int> {
    override fun displayTextForItem(item: Int): String {
        val quantityString = context.resources.getQuantityString(
            R.plurals.age_number_of_months,
            item
        )
        return context.getString(
            R.string.format_age_months, item, quantityString
        )
    }
}
