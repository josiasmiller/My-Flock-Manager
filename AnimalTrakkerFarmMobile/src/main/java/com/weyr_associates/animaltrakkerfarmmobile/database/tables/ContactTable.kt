package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import android.database.Cursor
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.ContactTable.Columns
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getOptEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getOptString
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getString
import com.weyr_associates.animaltrakkerfarmmobile.model.Contact

object ContactTable : TableSpec<Columns> {

    const val NAME = "contact_table"

    fun contactFromCursor(cursor: Cursor): Contact {
        return Contact(
            id = cursor.getEntityId(Columns.ID),
            firstName = cursor.getString(Columns.FIRST_NAME),
            middleName = cursor.getOptString(Columns.MIDDLE_NAME),
            lastName = cursor.getString(Columns.LAST_NAME),
            titleId = cursor.getOptEntityId(Columns.TITLE_ID)
        )
    }

    object Columns {
        val ID = Column.NotNull("id_contactid")
        val LAST_NAME = Column.NotNull("contact_last_name")
        val FIRST_NAME = Column.NotNull("contact_first_name")
        val MIDDLE_NAME = Column.Nullable("contact_middle_name")
        val TITLE_ID = Column.Nullable("id_contacttitleid")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")

        val FULL_NAME_ALIAS = Column.NotNull("contact_full_name")
        val FULL_NAME_PROJECTION get() = """$NAME.$FIRST_NAME || ' ' || $NAME.$LAST_NAME"""
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID
}
