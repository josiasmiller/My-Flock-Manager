package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import com.weyr_associates.animaltrakkerfarmmobile.database.tables.AnimalEvaluationTable.Columns
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec

object AnimalEvaluationTable : TableSpec<Columns> {

    const val NAME = "animal_evaluation_table"

    object Columns {
        val ID = Column.NotNull("id_animalevaluationid")
        val ANIMAL_ID = Column.NotNull("id_animalid")
        val AGE_IN_DAYS = Column.NotNull("age_in_days")
        val ANIMAL_RANK = Column.Nullable("animal_rank")
        val NUMBER_RANKED = Column.Nullable("number_animals_ranked")
        val EVAL_DATE = Column.NotNull("eval_date")
        val EVAL_TIME = Column.NotNull("eval_time")
        val TRAIT_ID_01 = Column.Nullable("trait_name01")
        val TRAIT_ID_02 = Column.Nullable("trait_name02")
        val TRAIT_ID_03 = Column.Nullable("trait_name03")
        val TRAIT_ID_04 = Column.Nullable("trait_name04")
        val TRAIT_ID_05 = Column.Nullable("trait_name05")
        val TRAIT_ID_06 = Column.Nullable("trait_name06")
        val TRAIT_ID_07 = Column.Nullable("trait_name07")
        val TRAIT_ID_08 = Column.Nullable("trait_name08")
        val TRAIT_ID_09 = Column.Nullable("trait_name09")
        val TRAIT_ID_10 = Column.Nullable("trait_name10")
        val TRAIT_ID_11 = Column.Nullable("trait_name11")
        val TRAIT_ID_12 = Column.Nullable("trait_name12")
        val TRAIT_ID_13 = Column.Nullable("trait_name13")
        val TRAIT_ID_14 = Column.Nullable("trait_name14")
        val TRAIT_ID_15 = Column.Nullable("trait_name15")
        val TRAIT_ID_16 = Column.Nullable("trait_name16")
        val TRAIT_ID_17 = Column.Nullable("trait_name17")
        val TRAIT_ID_18 = Column.Nullable("trait_name18")
        val TRAIT_ID_19 = Column.Nullable("trait_name19")
        val TRAIT_ID_20 = Column.Nullable("trait_name20")
        val TRAIT_SCORE_01 = Column.Nullable("trait_score01")
        val TRAIT_SCORE_02 = Column.Nullable("trait_score02")
        val TRAIT_SCORE_03 = Column.Nullable("trait_score03")
        val TRAIT_SCORE_04 = Column.Nullable("trait_score04")
        val TRAIT_SCORE_05 = Column.Nullable("trait_score05")
        val TRAIT_SCORE_06 = Column.Nullable("trait_score06")
        val TRAIT_SCORE_07 = Column.Nullable("trait_score07")
        val TRAIT_SCORE_08 = Column.Nullable("trait_score08")
        val TRAIT_SCORE_09 = Column.Nullable("trait_score09")
        val TRAIT_SCORE_10 = Column.Nullable("trait_score10")
        val TRAIT_SCORE_11 = Column.Nullable("trait_score11")
        val TRAIT_SCORE_12 = Column.Nullable("trait_score12")
        val TRAIT_SCORE_13 = Column.Nullable("trait_score13")
        val TRAIT_SCORE_14 = Column.Nullable("trait_score14")
        val TRAIT_SCORE_15 = Column.Nullable("trait_score15")
        val TRAIT_SCORE_16 = Column.Nullable("trait_score16")
        val TRAIT_SCORE_17 = Column.Nullable("trait_score17")
        val TRAIT_SCORE_18 = Column.Nullable("trait_score18")
        val TRAIT_SCORE_19 = Column.Nullable("trait_score19")
        val TRAIT_SCORE_20 = Column.Nullable("trait_score20")
        val TRAIT_UNITS_ID_11 = Column.Nullable("trait_units11")
        val TRAIT_UNITS_ID_12 = Column.Nullable("trait_units12")
        val TRAIT_UNITS_ID_13 = Column.Nullable("trait_units13")
        val TRAIT_UNITS_ID_14 = Column.Nullable("trait_units14")
        val TRAIT_UNITS_ID_15 = Column.Nullable("trait_units15")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID
}
