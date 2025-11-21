package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import com.weyr_associates.animaltrakkerfarmmobile.database.tables.AnimalBreedTable.Columns
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec

object AnimalBreedTable : TableSpec<Columns> {

    const val NAME = "animal_breed_table"

    object Columns {
        val ID = Column.NotNull("id_animalbreedid")
        val ANIMAL_ID = Column.NotNull("id_animalid")
        val BREED_ID = Column.NotNull("id_breedid")
        val BREED_PERCENTAGE = Column.NotNull("breed_percentage")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID
}
