package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import com.weyr_associates.animaltrakkerfarmmobile.database.tables.AnimalTissueTestRequestTable.Columns
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec

object AnimalTissueTestRequestTable : TableSpec<Columns> {

    const val NAME = "animal_tissue_test_request_table"

    object Columns {
        val ID = Column.NotNull("id_animaltissuetestrequestid")
        val SAMPLE_TAKEN_ID = Column.NotNull("id_animaltissuesampletakenid")
        val TEST_ID = Column.NotNull("id_tissuetestid")
        val LABORATORY_ID = Column.NotNull("id_companylaboratoryid")
        val LABORATORY_ACCESSION_ID = Column.Nullable("tissue_sample_lab_accession_id")
        val TEST_RESULTS = Column.Nullable("tissue_test_results")
        val TEST_RESULTS_DATE = Column.Nullable("tissue_test_results_date")
        val TEST_RESULTS_TIME = Column.Nullable("tissue_test_results_time")
        val ANIMAL_EXTERNAL_FILE_ID = Column.Nullable("id_tissue_test_results_id_animalexternalfileid")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID
}
