package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.CustomEvalTraitsTable.Columns
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec

object CustomEvalTraitsTable : TableSpec<Columns> {

    const val NAME = "custom_evaluation_traits_table"

    object Columns {
        val ID = Column.NotNull("id_customevaluationtraitsid")
        val TRAIT_ID = Column.NotNull("id_evaluationtraitid")
        val ITEM = Column.NotNull("custom_evaluation_item")
        val ORDER = Column.NotNull("custom_evaluation_order")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID
}
