package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import com.weyr_associates.animaltrakkerfarmmobile.database.tables.AnimalNoteTable.Columns
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec

object AnimalNoteTable : TableSpec<Columns> {

    const val NAME = "animal_note_table"

    object Columns {
        val ID = Column.NotNull("id_animalnoteid")
        val ANIMAL_ID = Column.NotNull("id_animalid")
        val NOTE_TEXT = Column.Nullable("note_text")
        val NOTE_DATE = Column.NotNull("note_date")
        val NOTE_TIME = Column.NotNull("note_time")
        val PREDEFINED_NOTE_ID = Column.Nullable("id_predefinednotesid")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID
}
