package com.weyr_associates.animaltrakkerfarmmobile.database.sql

import android.database.Cursor
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getInt
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getString
import com.weyr_associates.animaltrakkerfarmmobile.database.core.isNotNull
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.IdColorTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.IdColorTable.Columns
import com.weyr_associates.animaltrakkerfarmmobile.model.IdColor

fun Cursor.readIdColor(colQualifier: String? = null): IdColor {
    return IdColor(
        id = getEntityId(Columns.ID.qualifiedBy(colQualifier)),
        name = getString(Columns.NAME.qualifiedBy(colQualifier)),
        abbreviation = getString(Columns.ABBREVIATION.qualifiedBy(colQualifier)),
        order = getInt(Columns.ORDER.qualifiedBy(colQualifier))
    )
}

fun Cursor.readOptIdColor(colQualifier: String? = null): IdColor? {
    return takeIf { it.isNotNull(IdColorTable.Columns.ID.qualifiedBy(colQualifier)) }
        ?.readIdColor(colQualifier)
}
