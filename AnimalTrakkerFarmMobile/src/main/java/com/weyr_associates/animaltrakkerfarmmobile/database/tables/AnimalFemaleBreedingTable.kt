package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import com.weyr_associates.animaltrakkerfarmmobile.database.tables.AnimalFemaleBreedingTable.Columns
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec

object AnimalFemaleBreedingTable : TableSpec<Columns> {

    const val NAME = "animal_female_breeding_table"

    object Columns {
        val ID = Column.NotNull("id_animalfemalebreedingid")
        val ANIMAL_ID = Column.NotNull("id_animalid")
        val BIRTHING_DATE = Column.Nullable("birthing_date")
        val BIRTHING_TIME = Column.Nullable("birthing_time")
        val BIRTHING_NOTES = Column.Nullable("birthing_notes")
        val NUMBER_ANIMALS_BORN = Column.NotNull("number_animals_born")
        val NUMBER_ANIMALS_WEANED = Column.NotNull("number_animals_weaned")
        val GESTATION_LENGTH = Column.Nullable("gestation_length")
        val MALE_BREEDING_ID = Column.Nullable("id_animalmalebreedingid")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID
}
