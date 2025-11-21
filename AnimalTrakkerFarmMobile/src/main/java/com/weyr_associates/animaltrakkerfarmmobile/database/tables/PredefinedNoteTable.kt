package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import android.database.Cursor
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.PredefinedNoteTable.Columns
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getInt
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getOptEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getString
import com.weyr_associates.animaltrakkerfarmmobile.model.Company
import com.weyr_associates.animaltrakkerfarmmobile.model.PredefinedNote
import com.weyr_associates.animaltrakkerfarmmobile.model.UserType

object PredefinedNoteTable : TableSpec<Columns> {

    const val NAME = "predefined_notes_table"

    fun predefinedNoteFromCursor(cursor: Cursor): PredefinedNote {
        val contactId = cursor.getOptEntityId(Columns.CONTACT_ID)
        val companyId = cursor.getOptEntityId(Columns.COMPANY_ID)
        return PredefinedNote(
            id = cursor.getEntityId(Columns.ID),
            text = cursor.getString(Columns.NOTE_TEXT),
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
        val ID = Column.NotNull("id_predefinednotesid")
        val NOTE_TEXT = Column.NotNull("predefined_note_text")
        val CONTACT_ID = Column.Nullable("id_contactid")
        val COMPANY_ID = Column.Nullable("id_companyid")
        val ORDER = Column.NotNull("predefined_note_display_order")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")
    }

    object Sql {

        const val QUERY_PREDEFINED_NOTES_ALL =
            """SELECT * FROM ${NAME}"""

        val ORDER_BY_PREDEFINED_NOTE_ORDER get() =
            """ORDER BY ${PredefinedNoteTable.Columns.ORDER} ASC"""

        val QUERY_PREDEFINED_NOTES_FOR_CONTACT_USER_AND_DEFAULTS get() =
            """$QUERY_PREDEFINED_NOTES_ALL
                WHERE ${PredefinedNoteTable.Columns.CONTACT_ID} = ?
                    OR ${PredefinedNoteTable.Columns.COMPANY_ID} = '${Company.ID_GENERIC_RAW}'
                $ORDER_BY_PREDEFINED_NOTE_ORDER"""

        val QUERY_PREDEFINED_NOTES_FOR_COMPANY_USER_AND_DEFAULTS get() =
            """$QUERY_PREDEFINED_NOTES_ALL
                WHERE ${PredefinedNoteTable.Columns.COMPANY_ID} = ?
                    OR ${PredefinedNoteTable.Columns.COMPANY_ID} = '${Company.ID_GENERIC_RAW}'
                $ORDER_BY_PREDEFINED_NOTE_ORDER"""

        val QUERY_PREDEFINED_NOTES_DEFAULTS_ONLY get() =
            """$QUERY_PREDEFINED_NOTES_ALL
                WHERE ${PredefinedNoteTable.Columns.COMPANY_ID} = '${Company.ID_GENERIC_RAW}'
                $ORDER_BY_PREDEFINED_NOTE_ORDER
            """

        val QUERY_PREDEFINED_NOTE_BY_ID get() =
            """$QUERY_PREDEFINED_NOTES_ALL
                WHERE ${PredefinedNoteTable.Columns.ID} = ?"""
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID
}
