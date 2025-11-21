package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import com.weyr_associates.animaltrakkerfarmmobile.database.tables.AnimalDrugTable.Columns
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec

object AnimalDrugTable : TableSpec<Columns> {

    const val NAME = "animal_drug_table"

    object Columns {
        val ID = Column.NotNull("id_animaldrugid")
        val ANIMAL_ID = Column.NotNull("id_animalid")
        val DRUG_LOT_ID = Column.NotNull("id_druglotid")
        val DATE_ON = Column.NotNull("drug_date_on")
        val TIME_ON = Column.Nullable("drug_time_on")
        val DATE_OFF = Column.Nullable("drug_date_off")
        val TIME_OFF = Column.Nullable("drug_time_off")
        val DOSAGE = Column.NotNull("drug_dosage")
        val OFF_LABEL_DRUG_ID = Column.Nullable("id_drugofflabelid")
        val LOCATION_ID = Column.NotNull("id_druglocationid")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID
}
