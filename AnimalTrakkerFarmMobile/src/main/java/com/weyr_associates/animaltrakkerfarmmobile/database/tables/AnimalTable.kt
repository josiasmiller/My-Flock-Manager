package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import com.weyr_associates.animaltrakkerfarmmobile.database.tables.AnimalTable.Columns
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec

object AnimalTable : TableSpec<Columns> {

    const val NAME = "animal_table"

    object Columns {
        val ID = Column.NotNull("id_animalid")
        val NAME = Column.NotNull("animal_name")
        val SEX_ID = Column.NotNull("id_sexid")
        val SEX_CHANGE_DATE = Column.Nullable("sex_change_date")
        val BIRTH_DATE = Column.Nullable("birth_date")
        val BIRTH_TIME = Column.Nullable("birth_time")
        val BIRTH_TYPE_ID = Column.NotNull("id_birthtypeid")
        val BIRTH_WEIGHT = Column.Nullable("birth_weight")
        val BIRTH_WEIGH_UNITS_ID = Column.Nullable("birth_weight_id_unitsid")
        val BIRTH_ORDER = Column.Nullable("birth_order")
        val REAR_TYPE_ID = Column.Nullable("rear_type")
        val WEANED_DATE = Column.Nullable("weaned_date")
        val DEATH_DATE = Column.Nullable("death_date")
        val DEATH_REASON_ID = Column.Nullable("id_deathreasonid")
        val SIRE_ID = Column.Nullable("sire_id")
        val DAM_ID = Column.Nullable("dam_id")
        val FOSTER_DAM_ID = Column.Nullable("foster_dam_id")
        val SURROGATE_DAM_ID = Column.Nullable("surrogate_dam_id")
        val IS_HAND_REARED = Column.NotNull("hand_reared")
        val MANAGEMENT_GROUP_ID = Column.Nullable("id_managementgroupid")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")
    }

    object Sql {
        val QUERY_COUNT_ALL_ANIMALS get() =
            """SELECT COUNT(*) FROM ${NAME}
               WHERE ${Columns.BIRTH_DATE} IS NOT NULL"""
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID
}
