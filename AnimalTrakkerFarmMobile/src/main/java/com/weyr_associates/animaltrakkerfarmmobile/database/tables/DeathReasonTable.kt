package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import android.database.Cursor
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.DeathReasonTable.Columns
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getInt
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getOptEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getString
import com.weyr_associates.animaltrakkerfarmmobile.model.Company
import com.weyr_associates.animaltrakkerfarmmobile.model.DeathReason
import com.weyr_associates.animaltrakkerfarmmobile.model.UserType

object DeathReasonTable : TableSpec<Columns> {

    const val NAME = "death_reason_table"

    fun deathReasonFromCursor(cursor: Cursor): DeathReason {
        val contactId = cursor.getOptEntityId(Columns.CONTACT_ID)
        val companyId = cursor.getOptEntityId(Columns.COMPANY_ID)
        return DeathReason(
            id = cursor.getEntityId(Columns.ID),
            reason = cursor.getString(Columns.REASON),
            userId = when {
                contactId != null && contactId.isValid -> contactId
                companyId != null && companyId.isValid -> companyId
                else -> throw IllegalStateException("Unable to determine user id. Contact and company are both 0.")
            },
            userType = when {
                contactId != null && contactId.isValid -> UserType.CONTACT
                companyId != null && companyId.isValid -> UserType.COMPANY
                else -> throw IllegalStateException("Unable to determine user type. Contact and company are both 0.")
            },
            order = cursor.getInt(Columns.ORDER)
        )
    }

    object Columns {
        val ID = Column.NotNull("id_deathreasonid")
        val REASON = Column.NotNull("death_reason")
        val CONTACT_ID = Column.Nullable("id_contactid")
        val COMPANY_ID = Column.Nullable("id_companyid")
        val ORDER = Column.NotNull("death_reason_display_order")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")
    }

    object Sql {
        const val QUERY_DEATH_REASONS_ALL =
            """SELECT * FROM ${NAME}"""

        val ORDER_BY_DEATH_REASON_ORDER get() =
            """ORDER BY ${Columns.ORDER} ASC"""

        val QUERY_DEATH_REASONS_FOR_CONTACT_USER_AND_DEFAULTS get() =
            """$QUERY_DEATH_REASONS_ALL
                WHERE ${Columns.CONTACT_ID} = ?
                    OR ${Columns.COMPANY_ID} = '${Company.ID_GENERIC_RAW}'
                $ORDER_BY_DEATH_REASON_ORDER"""

        val QUERY_DEATH_REASONS_FOR_COMPANY_USER_AND_DEFAULTS get() =
            """$QUERY_DEATH_REASONS_ALL
                WHERE ${Columns.COMPANY_ID} = ?
                    OR ${Columns.COMPANY_ID} = '${Company.ID_GENERIC_RAW}'
                $ORDER_BY_DEATH_REASON_ORDER"""

        val QUERY_DEATH_REASONS_DEFAULTS_ONLY get() =
            """$QUERY_DEATH_REASONS_ALL
                WHERE ${Columns.COMPANY_ID} = '${Company.ID_GENERIC_RAW}'
                $ORDER_BY_DEATH_REASON_ORDER"""
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID
}
