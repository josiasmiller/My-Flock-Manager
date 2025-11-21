package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import com.weyr_associates.animaltrakkerfarmmobile.database.tables.AnimalTissueSampleTakenTable.Columns
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec

object AnimalTissueSampleTakenTable : TableSpec<Columns> {

    const val NAME = "animal_tissue_sample_taken_table"

    object Columns {
        val ID = Column.NotNull("id_animaltissuesampletakenid")
        val ANIMAL_ID = Column.NotNull("id_animalid")
        val SAMPLE_TYPE_ID = Column.NotNull("id_tissuesampletypeid")
        val SAMPLE_DATE = Column.NotNull("tissue_sample_date")
        val SAMPLE_TIME = Column.Nullable("tissue_sample_time")
        val CONTAINER_TYPE_ID = Column.NotNull("id_tissuesamplecontainertypeid")
        val CONTAINER_ID = Column.Nullable("tissue_sample_container_id")
        val CONTAINER_EXP_DATE = Column.Nullable("tissue_sample_container_exp_date")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID
}
