package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.GeneticCharacteristicCalculationMethodTable.Columns
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec

object GeneticCharacteristicCalculationMethodTable : TableSpec<Columns> {

    const val NAME = "genetic_characteristic_calculation_method_table"

    object Columns {
        val ID = Column.NotNull("id_geneticcharacteristiccalculationmethodid")
        val NAME = Column.NotNull("genetic_characteristic_calculation_method")
        val ORDER = Column.NotNull("genetic_characteristic_calculation_method_display_order")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID
}
