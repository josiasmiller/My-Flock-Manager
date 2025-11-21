package com.weyr_associates.animaltrakkerfarmmobile.database.queries

import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.AnimalEvaluationTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.UnitsTable

object AnimalLastEvaluationOfTrait {

    object Columns {
        val ANIMAL_ID = Column.NotNull("id_animalid")
        val TRAIT_VALUE = Column.Nullable("trait_value")
        val TRAIT_UNITS_ID = Column.Nullable("trait_units_id")
        val TRAIT_UNITS_NAME = Column.Nullable("trait_units_name")
        val TRAIT_UNITS_ABBREV = Column.Nullable("trait_units_abbrev")
        val TRAIT_EVAL_DATE = Column.NotNull("trait_eval_date")
        val TRAIT_EVAL_TIME = Column.NotNull("trait_eval_time")
    }

    val SQL_QUERY_ANIMAL_LAST_EVALUATION_OF_UNIT_TRAIT get() =
        """SELECT
             ${Columns.ANIMAL_ID},
             ${AnimalEvaluationTable.Columns.TRAIT_SCORE_11} AS ${Columns.TRAIT_VALUE}, 
             ${AnimalEvaluationTable.Columns.TRAIT_UNITS_ID_11} AS ${Columns.TRAIT_UNITS_ID},
             ${UnitsTable.Columns.NAME} AS ${Columns.TRAIT_UNITS_NAME},
             ${UnitsTable.Columns.ABBREVIATION} AS ${Columns.TRAIT_UNITS_ABBREV},
             ${AnimalEvaluationTable.Columns.EVAL_DATE} AS ${Columns.TRAIT_EVAL_DATE},
             ${AnimalEvaluationTable.Columns.EVAL_TIME} AS ${Columns.TRAIT_EVAL_TIME}
           FROM ${AnimalEvaluationTable.NAME}
           JOIN ${UnitsTable.NAME} ON ${UnitsTable.Columns.ID} = ${Columns.TRAIT_UNITS_ID}
           WHERE ${Columns.ANIMAL_ID} = ?1 AND ${AnimalEvaluationTable.Columns.TRAIT_ID_11} = ?2
           UNION
           SELECT
             ${Columns.ANIMAL_ID},
             ${AnimalEvaluationTable.Columns.TRAIT_SCORE_12} AS ${Columns.TRAIT_VALUE}, 
             ${AnimalEvaluationTable.Columns.TRAIT_UNITS_ID_12} AS ${Columns.TRAIT_UNITS_ID},
             ${UnitsTable.Columns.NAME} AS ${Columns.TRAIT_UNITS_NAME},
             ${UnitsTable.Columns.ABBREVIATION} AS ${Columns.TRAIT_UNITS_ABBREV},
             ${AnimalEvaluationTable.Columns.EVAL_DATE} AS ${Columns.TRAIT_EVAL_DATE}, 
             ${AnimalEvaluationTable.Columns.EVAL_TIME} AS ${Columns.TRAIT_EVAL_TIME}
           FROM ${AnimalEvaluationTable.NAME}
           JOIN ${UnitsTable.NAME} ON ${UnitsTable.Columns.ID} = ${Columns.TRAIT_UNITS_ID}
           WHERE ${Columns.ANIMAL_ID} = ?1 AND ${AnimalEvaluationTable.Columns.TRAIT_ID_12} = ?2
           UNION
           SELECT
             ${Columns.ANIMAL_ID},
             ${AnimalEvaluationTable.Columns.TRAIT_SCORE_13} AS ${Columns.TRAIT_VALUE}, 
             ${AnimalEvaluationTable.Columns.TRAIT_UNITS_ID_13} AS ${Columns.TRAIT_UNITS_ID},
             ${UnitsTable.Columns.NAME} AS ${Columns.TRAIT_UNITS_NAME},
             ${UnitsTable.Columns.ABBREVIATION} AS ${Columns.TRAIT_UNITS_ABBREV},
             ${AnimalEvaluationTable.Columns.EVAL_DATE} AS ${Columns.TRAIT_EVAL_DATE}, 
             ${AnimalEvaluationTable.Columns.EVAL_TIME} AS ${Columns.TRAIT_EVAL_TIME}
           FROM ${AnimalEvaluationTable.NAME}
           JOIN ${UnitsTable.NAME} ON ${UnitsTable.Columns.ID} = ${Columns.TRAIT_UNITS_ID}
           WHERE ${Columns.ANIMAL_ID} = ?1 AND ${AnimalEvaluationTable.Columns.TRAIT_ID_13} = ?2
           UNION
           SELECT
             ${Columns.ANIMAL_ID},
             ${AnimalEvaluationTable.Columns.TRAIT_SCORE_14} AS ${Columns.TRAIT_VALUE}, 
             ${AnimalEvaluationTable.Columns.TRAIT_UNITS_ID_14} AS ${Columns.TRAIT_UNITS_ID},
             ${UnitsTable.Columns.NAME} AS ${Columns.TRAIT_UNITS_NAME},
             ${UnitsTable.Columns.ABBREVIATION} AS ${Columns.TRAIT_UNITS_ABBREV},
             ${AnimalEvaluationTable.Columns.EVAL_DATE} AS ${Columns.TRAIT_EVAL_DATE}, 
             ${AnimalEvaluationTable.Columns.EVAL_TIME} AS ${Columns.TRAIT_EVAL_TIME}
           FROM ${AnimalEvaluationTable.NAME}
           JOIN ${UnitsTable.NAME} ON ${UnitsTable.Columns.ID} = ${Columns.TRAIT_UNITS_ID}
           WHERE ${Columns.ANIMAL_ID} = ?1 AND ${AnimalEvaluationTable.Columns.TRAIT_ID_14} = ?2
           UNION
           SELECT
             ${Columns.ANIMAL_ID},
             ${AnimalEvaluationTable.Columns.TRAIT_SCORE_15} AS ${Columns.TRAIT_VALUE}, 
             ${AnimalEvaluationTable.Columns.TRAIT_UNITS_ID_15} AS ${Columns.TRAIT_UNITS_ID},
             ${UnitsTable.Columns.NAME} AS ${Columns.TRAIT_UNITS_NAME},
             ${UnitsTable.Columns.ABBREVIATION} AS ${Columns.TRAIT_UNITS_ABBREV},
             ${AnimalEvaluationTable.Columns.EVAL_DATE} AS ${Columns.TRAIT_EVAL_DATE}, 
             ${AnimalEvaluationTable.Columns.EVAL_TIME} AS ${Columns.TRAIT_EVAL_TIME}
           FROM ${AnimalEvaluationTable.NAME}
           JOIN ${UnitsTable.NAME} ON ${UnitsTable.Columns.ID} = ${Columns.TRAIT_UNITS_ID}
           WHERE ${Columns.ANIMAL_ID} = ?1 AND ${AnimalEvaluationTable.Columns.TRAIT_ID_15} = ?2
           ORDER BY ${Columns.TRAIT_EVAL_DATE} DESC, ${Columns.TRAIT_EVAL_TIME} DESC
           LIMIT 1"""
}