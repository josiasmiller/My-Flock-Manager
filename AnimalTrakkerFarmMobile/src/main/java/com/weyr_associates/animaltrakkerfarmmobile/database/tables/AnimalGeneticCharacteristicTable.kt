package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import com.weyr_associates.animaltrakkerfarmmobile.database.tables.AnimalGeneticCharacteristicTable.Columns
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec

object AnimalGeneticCharacteristicTable : TableSpec<Columns> {

    const val NAME = "animal_genetic_characteristic_table"

    object Columns {
        val ID = Column.NotNull("id_animalgeneticcharacteristicid")
        val ANIMAL_ID = Column.NotNull("id_animalid")
        val TABLE_ID = Column.NotNull("id_geneticcharacteristictableid")
        val VALUE_ID = Column.NotNull("id_geneticcharacteristicvalueid")
        val CALCULATION_ID = Column.NotNull("id_geneticcharacteristiccalculationmethodid")
        val DATE = Column.NotNull("genetic_characteristic_date")
        val TIME = Column.Nullable("genetic_characteristic_time")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID
}
