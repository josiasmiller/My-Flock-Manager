package com.weyr_associates.animaltrakkerfarmmobile.database.sql

import android.database.Cursor
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getInt
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getString
import com.weyr_associates.animaltrakkerfarmmobile.database.core.isNotNull
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.IdRemoveReasonTable
import com.weyr_associates.animaltrakkerfarmmobile.model.IdRemoveReason

fun Cursor.readIdRemoveReason(colQualifier: String? = null): IdRemoveReason {
    return IdRemoveReason(
        id = getEntityId(IdRemoveReasonTable.Columns.ID.qualifiedBy(colQualifier)),
        text = getString(IdRemoveReasonTable.Columns.REMOVE_REASON.qualifiedBy(colQualifier)),
        order = getInt(IdRemoveReasonTable.Columns.ORDER.qualifiedBy(colQualifier))
    )
}

fun Cursor.readOptIdRemoveReason(colQualifier: String? = null): IdRemoveReason? {
    return takeIf { it.isNotNull(IdRemoveReasonTable.Columns.ID.qualifiedBy(colQualifier)) }
        ?.readIdRemoveReason(colQualifier)
}
