package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.SpeciesTable.Columns

object SpeciesTable : TableSpec<Columns> {

    const val NAME = "species_table"

    object Columns {
        val ID = Column.NotNull("id_speciesid")
        val COMMON_NAME = Column.NotNull("species_common_name")
        val GENERIC_NAME = Column.NotNull("species_generic_name")
        val SCIENTIFIC_FAMILY = Column.NotNull("species_scientific_family")
        val SCIENTIFIC_SUBFAMILY = Column.Nullable("species_scientific_sub_family")
        val SCIENTIFIC_NAME = Column.NotNull("species_scientific_name")
        val EARLY_MALE_BREEDING_AGE_IN_DAYS = Column.NotNull("early_male_breeding_age_days")
        val EARLY_FEMALE_BREEDING_AGE_IN_DAYS = Column.NotNull("early_female_breeding_age_days")
        val EARLY_GESTATION_LENGTH_DAYS = Column.NotNull("early_gestation_length_days")
        val LATE_GESTATION_LENGTH_DAYS = Column.NotNull("late_gestation_length_days")
        val TYPICAL_GESTATION_LENGTH_DAYS = Column.NotNull("typical_gestation_length_days")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID
}
