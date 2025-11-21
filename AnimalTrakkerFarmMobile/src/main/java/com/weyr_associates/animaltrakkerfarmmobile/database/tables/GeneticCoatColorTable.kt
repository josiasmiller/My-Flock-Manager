package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import android.database.Cursor
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.GeneticCoatColorTable.Columns
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getInt
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getString
import com.weyr_associates.animaltrakkerfarmmobile.model.GeneticCoatColor

object GeneticCoatColorTable : TableSpec<Columns> {

    const val NAME = "genetic_coat_color_table"

    fun geneticCoatColorFromCursor(cursor: Cursor): GeneticCoatColor {
        return GeneticCoatColor(
            id = cursor.getEntityId(Columns.ID),
            registryCompanyId = cursor.getEntityId(Columns.REGISTRY_COMPANY_ID),
            color = cursor.getString(Columns.COLOR),
            colorAbbreviation = cursor.getString(Columns.ABBREVIATION),
            order = cursor.getInt(Columns.ORDER)
        )
    }

    object Columns {
        val ID = Column.NotNull("id_geneticcoatcolorid")
        val REGISTRY_COMPANY_ID = Column.NotNull("id_registry_id_companyid")
        val COLOR = Column.NotNull("coat_color")
        val ABBREVIATION = Column.NotNull("coat_color_abbrev")
        val ORDER = Column.NotNull("coat_color_display_order")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")
    }

    object Sql {
        val QUERY_COAT_COLORS_BY_REGISTRY_COMPANY_ID get() =
            """SELECT * FROM ${NAME}
                WHERE ${GeneticCoatColorTable.Columns.REGISTRY_COMPANY_ID} = ?
                ORDER BY ${GeneticCoatColorTable.Columns.ORDER}"""
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID
}
