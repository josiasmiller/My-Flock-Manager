package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import android.database.Cursor
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.SavedEvaluationTable.Columns
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getString
import com.weyr_associates.animaltrakkerfarmmobile.model.ItemEntry

object SavedEvaluationTable : TableSpec<Columns> {

    const val NAME = "saved_evaluations_table"

    fun itemEntryForSavedEvaluationFromCursor(cursor: Cursor): ItemEntry {
        return ItemEntry(
            id = cursor.getEntityId(Columns.ID),
            name = cursor.getString(Columns.NAME)
        )
    }

    object Columns {
        val ID = Column.NotNull("id_savedevaluationsid")
        val NAME = Column.NotNull("evaluation_name")
        val IS_SYSTEM_ONLY = Column.NotNull("is_system_only")
        val SUMMARIZE_IN_ALERT = Column.NotNull("add_alert_summary")
        val CONTACT_ID = Column.Nullable("saved_evaluation_id_contactid")
        val COMPANY_ID = Column.Nullable("saved_evaluation_id_companyid")
        val TRAIT_01_ID = Column.Nullable("trait_name01")
        val TRAIT_02_ID = Column.Nullable("trait_name02")
        val TRAIT_03_ID = Column.Nullable("trait_name03")
        val TRAIT_04_ID = Column.Nullable("trait_name04")
        val TRAIT_05_ID = Column.Nullable("trait_name05")
        val TRAIT_06_ID = Column.Nullable("trait_name06")
        val TRAIT_07_ID = Column.Nullable("trait_name07")
        val TRAIT_08_ID = Column.Nullable("trait_name08")
        val TRAIT_09_ID = Column.Nullable("trait_name09")
        val TRAIT_10_ID = Column.Nullable("trait_name10")
        val TRAIT_11_ID = Column.Nullable("trait_name11")
        val TRAIT_12_ID = Column.Nullable("trait_name12")
        val TRAIT_13_ID = Column.Nullable("trait_name13")
        val TRAIT_14_ID = Column.Nullable("trait_name14")
        val TRAIT_15_ID = Column.Nullable("trait_name15")
        val TRAIT_16_ID = Column.Nullable("trait_name16")
        val TRAIT_17_ID = Column.Nullable("trait_name17")
        val TRAIT_18_ID = Column.Nullable("trait_name18")
        val TRAIT_19_ID = Column.Nullable("trait_name19")
        val TRAIT_20_ID = Column.Nullable("trait_name20")
        val TRAIT_11_UNITS_ID = Column.Nullable("trait_units11")
        val TRAIT_12_UNITS_ID = Column.Nullable("trait_units12")
        val TRAIT_13_UNITS_ID = Column.Nullable("trait_units13")
        val TRAIT_14_UNITS_ID = Column.Nullable("trait_units14")
        val TRAIT_15_UNITS_ID = Column.Nullable("trait_units15")
        val TRAIT_01_OPTIONAL = Column.Nullable("trait_name01_optional")
        val TRAIT_02_OPTIONAL = Column.Nullable("trait_name02_optional")
        val TRAIT_03_OPTIONAL = Column.Nullable("trait_name03_optional")
        val TRAIT_04_OPTIONAL = Column.Nullable("trait_name04_optional")
        val TRAIT_05_OPTIONAL = Column.Nullable("trait_name05_optional")
        val TRAIT_06_OPTIONAL = Column.Nullable("trait_name06_optional")
        val TRAIT_07_OPTIONAL = Column.Nullable("trait_name07_optional")
        val TRAIT_08_OPTIONAL = Column.Nullable("trait_name08_optional")
        val TRAIT_09_OPTIONAL = Column.Nullable("trait_name09_optional")
        val TRAIT_10_OPTIONAL = Column.Nullable("trait_name10_optional")
        val TRAIT_11_OPTIONAL = Column.Nullable("trait_name11_optional")
        val TRAIT_12_OPTIONAL = Column.Nullable("trait_name12_optional")
        val TRAIT_13_OPTIONAL = Column.Nullable("trait_name13_optional")
        val TRAIT_14_OPTIONAL = Column.Nullable("trait_name14_optional")
        val TRAIT_15_OPTIONAL = Column.Nullable("trait_name15_optional")
        val TRAIT_16_OPTIONAL = Column.Nullable("trait_name16_optional")
        val TRAIT_17_OPTIONAL = Column.Nullable("trait_name17_optional")
        val TRAIT_18_OPTIONAL = Column.Nullable("trait_name18_optional")
        val TRAIT_19_OPTIONAL = Column.Nullable("trait_name19_optional")
        val TRAIT_20_OPTIONAL = Column.Nullable("trait_name20_optional")
        val TRAIT_01_DEFERRED = Column.Nullable("trait_name01_deferred")
        val TRAIT_02_DEFERRED = Column.Nullable("trait_name02_deferred")
        val TRAIT_03_DEFERRED = Column.Nullable("trait_name03_deferred")
        val TRAIT_04_DEFERRED = Column.Nullable("trait_name04_deferred")
        val TRAIT_05_DEFERRED = Column.Nullable("trait_name05_deferred")
        val TRAIT_06_DEFERRED = Column.Nullable("trait_name06_deferred")
        val TRAIT_07_DEFERRED = Column.Nullable("trait_name07_deferred")
        val TRAIT_08_DEFERRED = Column.Nullable("trait_name08_deferred")
        val TRAIT_09_DEFERRED = Column.Nullable("trait_name09_deferred")
        val TRAIT_10_DEFERRED = Column.Nullable("trait_name10_deferred")
        val TRAIT_11_DEFERRED = Column.Nullable("trait_name11_deferred")
        val TRAIT_12_DEFERRED = Column.Nullable("trait_name12_deferred")
        val TRAIT_13_DEFERRED = Column.Nullable("trait_name13_deferred")
        val TRAIT_14_DEFERRED = Column.Nullable("trait_name14_deferred")
        val TRAIT_15_DEFERRED = Column.Nullable("trait_name15_deferred")
        val TRAIT_16_DEFERRED = Column.Nullable("trait_name16_deferred")
        val TRAIT_17_DEFERRED = Column.Nullable("trait_name17_deferred")
        val TRAIT_18_DEFERRED = Column.Nullable("trait_name18_deferred")
        val TRAIT_19_DEFERRED = Column.Nullable("trait_name19_deferred")
        val TRAIT_20_DEFERRED = Column.Nullable("trait_name20_deferred")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")
    }

    object Sql {
        val QUERY_SAVED_EVALUATIONS_FOR_CONTACT_USER get() =
            """SELECT * FROM ${NAME}
                WHERE ${SavedEvaluationTable.Columns.IS_SYSTEM_ONLY} = 0 
                AND ${SavedEvaluationTable.Columns.CONTACT_ID} = ?
                ORDER BY ${SavedEvaluationTable.Columns.NAME}
            """

        val QUERY_SAVED_EVALUATIONS_FOR_COMPANY_USER get() =
            """SELECT * FROM ${NAME}
                WHERE ${SavedEvaluationTable.Columns.IS_SYSTEM_ONLY} = 0 
                AND ${SavedEvaluationTable.Columns.COMPANY_ID} = ?
                ORDER BY ${SavedEvaluationTable.Columns.NAME}
            """
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID
}
