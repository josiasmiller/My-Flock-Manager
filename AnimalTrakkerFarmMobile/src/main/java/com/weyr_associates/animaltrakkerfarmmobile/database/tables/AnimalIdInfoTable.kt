package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import com.weyr_associates.animaltrakkerfarmmobile.database.tables.AnimalIdInfoTable.Columns
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec

object AnimalIdInfoTable : TableSpec<Columns> {

    const val NAME = "animal_id_info_table"

    object Columns {
        val ID = Column.NotNull("id_animalidinfoid")
        val ANIMAL_ID = Column.NotNull("id_animalid")
        val ID_TYPE_ID = Column.NotNull("id_idtypeid")
        val MALE_ID_COLOR_ID = Column.NotNull("id_male_id_idcolorid")
        val FEMALE_ID_COLOR_ID = Column.NotNull("id_female_id_idcolorid")
        val ID_LOCATION_ID = Column.NotNull("id_idlocationid")
        val DATE_ON = Column.NotNull("id_date_on")
        val TIME_ON = Column.NotNull("id_time_on")
        val DATE_OFF = Column.Nullable("id_date_off")
        val TIME_OFF = Column.Nullable("id_time_off")
        val NUMBER = Column.NotNull("id_number")
        val SCRAPIE_FLOCK_ID = Column.Nullable("id_scrapieflockid")
        val IS_OFFICIAL_ID = Column.NotNull("official_id")
        val REMOVE_REASON_ID = Column.Nullable("id_idremovereasonid")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID
}
