package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import android.database.Cursor
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getInt
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getString
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.BreedTable.Columns
import com.weyr_associates.animaltrakkerfarmmobile.model.Breed

object BreedTable : TableSpec<Columns> {

    const val NAME = "breed_table"

    @JvmStatic
    fun breedFromCursor(cursor: Cursor): Breed {
        return Breed(
            id = cursor.getEntityId(Columns.ID),
            name = cursor.getString(Columns.NAME),
            abbreviation = cursor.getString(Columns.ABBREVIATION),
            order = cursor.getInt(Columns.ORDER),
            speciesId = cursor.getEntityId(Columns.SPECIES_ID)
        )
    }

    object Columns {
        val ID = Column.NotNull("id_breedid")
        val NAME = Column.NotNull("breed_name")
        val ABBREVIATION = Column.NotNull("breed_abbrev")
        val ORDER = Column.NotNull("breed_display_order")
        val SPECIES_ID = Column.NotNull("id_speciesid")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID
}
