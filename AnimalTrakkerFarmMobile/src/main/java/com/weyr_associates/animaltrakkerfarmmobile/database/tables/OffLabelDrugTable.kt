package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.OffLabelDrugTable.Columns
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec

object OffLabelDrugTable : TableSpec<Columns> {

    const val NAME = "drug_off_label_table"

    object Columns {
        val ID = Column.NotNull("id_drugofflabelid")
        val DRUG_ID = Column.NotNull("id_drugid")
        val SPECIES_ID = Column.NotNull("id_speciesid")
        val OFF_LABEL_VET_CONTACT_ID = Column.NotNull("off_label_id_contactveterinarianid")
        val OFF_LABEL_DRUG_DOSAGE = Column.NotNull("off_label_drug_dosage")
        val OFF_LABEL_USE_START = Column.NotNull("start_off_label_use")
        val OFF_LABEL_USE_END = Column.Nullable("end_off_label_use")
        val OFF_LABEL_NOTE = Column.Nullable("off_label_note")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID
}
