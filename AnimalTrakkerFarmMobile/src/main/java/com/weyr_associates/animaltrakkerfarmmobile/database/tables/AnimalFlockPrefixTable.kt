package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.AnimalFlockPrefixTable.Columns

object AnimalFlockPrefixTable : TableSpec<Columns> {

    const val NAME = "animal_flock_prefix_table"

    object Columns {
        val ID = Column.NotNull("id_animalflockprefixid")
        val ANIMAL_ID = Column.NotNull("id_animalid")
        val FLOCK_PREFIX_ID = Column.NotNull("id_flockprefixid")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID
}
