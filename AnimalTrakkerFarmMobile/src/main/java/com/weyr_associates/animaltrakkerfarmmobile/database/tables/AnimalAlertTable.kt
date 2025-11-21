package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import com.weyr_associates.animaltrakkerfarmmobile.database.tables.AnimalAlertTable.Columns
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec

object AnimalAlertTable : TableSpec<Columns> {

    const val NAME = "animal_alert_table"

    object Columns {
        val ID = Column.NotNull("id_animalalertid")
        val ALERT_TYPE_ID = Column.NotNull("id_alerttypeid")
        val ANIMAL_ID = Column.NotNull("id_animalid")
        val ALERT_CONTENT = Column.NotNull("alert")
        val ALERT_DATE = Column.NotNull("alert_date")
        val ALERT_TIME = Column.NotNull("alert_time")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID
}
