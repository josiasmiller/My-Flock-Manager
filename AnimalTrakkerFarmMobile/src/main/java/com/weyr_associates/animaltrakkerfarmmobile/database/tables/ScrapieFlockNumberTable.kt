package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import android.database.Cursor
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getInt
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getString
import com.weyr_associates.animaltrakkerfarmmobile.model.ScrapieFlockNumber

object ScrapieFlockNumberTable {

    const val NAME = "scrapie_flock_number_table"

    fun scrapieFlockNumberFromCursor(cursor: Cursor): ScrapieFlockNumber {
        return ScrapieFlockNumber(
            id = cursor.getEntityId(Columns.ID),
            number = cursor.getString(Columns.NUMBER),
            order = cursor.getInt(Columns.ORDER)
        )
    }

    object Columns {
        val ID = Column.NotNull("id_scrapieflocknumberid")
        val NUMBER = Column.NotNull("scrapie_flockid")
        val ORDER = Column.NotNull("scrapie_flock_display_order")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")
    }

    object Sql {
        val QUERY_SCRAPIE_FLOCK_NUMBER_FROM_NUMBER get() =
            """SELECT * FROM ${NAME}
                WHERE ${Columns.NUMBER} = ?
                LIMIT 1"""
    }
}
