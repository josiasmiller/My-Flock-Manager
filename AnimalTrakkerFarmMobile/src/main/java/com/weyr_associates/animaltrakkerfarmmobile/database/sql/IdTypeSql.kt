package com.weyr_associates.animaltrakkerfarmmobile.database.sql

import android.database.Cursor
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getInt
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getString
import com.weyr_associates.animaltrakkerfarmmobile.database.core.isNotNull
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.IdTypeTable
import com.weyr_associates.animaltrakkerfarmmobile.model.IdType

fun Cursor.readIdType(colQualifier: String? = null): IdType {
    return IdType(
        id = getEntityId(IdTypeTable.Columns.ID.qualifiedBy(colQualifier)),
        name = getString(IdTypeTable.Columns.NAME.qualifiedBy(colQualifier)),
        abbreviation = getString(IdTypeTable.Columns.ABBREVIATION.qualifiedBy(colQualifier)),
        order = getInt(IdTypeTable.Columns.ORDER.qualifiedBy(colQualifier))
    )
}

fun Cursor.readOptIdType(colQualifier: String? = null): IdType? {
    return takeIf { it.isNotNull(IdTypeTable.Columns.ID.qualifiedBy(colQualifier)) }
        ?.readIdType(colQualifier)
}
