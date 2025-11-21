package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import com.weyr_associates.animaltrakkerfarmmobile.database.tables.AnimalMaleBreedingTable.Columns
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec

object AnimalMaleBreedingTable : TableSpec<Columns> {

    const val NAME = "animal_male_breeding_table"

    object Columns {
        val ID = Column.NotNull("id_animalmalebreedingid")
        val ANIMAL_ID = Column.NotNull("id_animalid")
        val DATE_IN = Column.NotNull("date_male_in")
        val TIME_IN = Column.NotNull("time_male_in")
        val DATE_OUT = Column.Nullable("date_male_out")
        val TIME_OUT = Column.Nullable("time_male_out")
        val SERVICE_TYPE_ID = Column.NotNull("id_servicetypeid")
        val STORED_SEMEN_ID = Column.Nullable("id_animalstoredsemenid")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName = Columns.ID
}
