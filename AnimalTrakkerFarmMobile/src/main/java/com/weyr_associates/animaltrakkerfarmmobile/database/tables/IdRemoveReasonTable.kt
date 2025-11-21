package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.IdRemoveReasonTable.Columns
import com.weyr_associates.animaltrakkerfarmmobile.model.IdRemoveReason

object IdRemoveReasonTable : TableSpec<Columns> {

    const val NAME = "id_remove_reason_table"

    object Columns {
        val ID = Column.NotNull("id_idremovereasonid")
        val REMOVE_REASON = Column.NotNull("id_remove_reason")
        val ORDER = Column.NotNull("id_remove_reason_display_order")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")
    }

    object Sql {
        val QUERY_ALL_ID_REMOVE_REASONS get() =
            """SELECT * FROM ${NAME}
                WHERE ${Columns.ID} != '${IdRemoveReason.ID_CORRECT_TAG_DATA_RAW}'
                ORDER BY ${Columns.ORDER}"""
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID
}
