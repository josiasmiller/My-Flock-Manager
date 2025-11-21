package com.weyr_associates.animaltrakkerfarmmobile.database.sql

import android.database.Cursor
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getInt
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getString
import com.weyr_associates.animaltrakkerfarmmobile.database.core.isNotNull
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.IdLocationTable
import com.weyr_associates.animaltrakkerfarmmobile.model.IdLocation

fun Cursor.readIdLocation(colQualifier: String? = null): IdLocation {
    return IdLocation(
        id = getEntityId(IdLocationTable.Columns.ID.qualifiedBy(colQualifier)),
        name = getString(IdLocationTable.Columns.NAME.qualifiedBy(colQualifier)),
        abbreviation = getString(IdLocationTable.Columns.ABBREVIATION.qualifiedBy(colQualifier)),
        order = getInt(IdLocationTable.Columns.ORDER.qualifiedBy(colQualifier))
    )
}

fun Cursor.readOptIdLocation(colQualifier: String? = null): IdLocation? {
    return takeIf { it.isNotNull(IdLocationTable.Columns.ID.qualifiedBy(colQualifier)) }
        ?.readIdLocation(colQualifier)
}
