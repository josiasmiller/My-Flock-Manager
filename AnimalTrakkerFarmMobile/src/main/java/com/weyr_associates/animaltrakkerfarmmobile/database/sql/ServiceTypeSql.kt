package com.weyr_associates.animaltrakkerfarmmobile.database.sql

import android.database.Cursor
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getInt
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getString
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.ServiceTypeTable.Columns
import com.weyr_associates.animaltrakkerfarmmobile.model.ServiceType

fun Cursor.readServiceType(colQualifier: String? = null): ServiceType {
    return ServiceType(
        id = getEntityId(Columns.ID.qualifiedBy(colQualifier)),
        name = getString(Columns.NAME.qualifiedBy(colQualifier)),
        abbreviation = getString(Columns.ABBREVIATION.qualifiedBy(colQualifier)),
        order = getInt(Columns.ORDER.qualifiedBy(colQualifier))
    )
}
