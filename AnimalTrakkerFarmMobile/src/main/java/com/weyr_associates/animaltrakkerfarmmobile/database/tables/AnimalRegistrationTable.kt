package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import com.weyr_associates.animaltrakkerfarmmobile.database.tables.AnimalRegistrationTable.Columns
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec

object AnimalRegistrationTable : TableSpec<Columns> {

    const val NAME = "animal_registration_table"

    object Columns {
        val ID = Column.NotNull("id_animalregistrationid")
        val ANIMAL_ID = Column.NotNull("id_animalid")
        val ANIMAL_NAME = Column.NotNull("animal_name")
        val REGISTRATION_NUMBER = Column.Nullable("registration_number")
        val ID_REGISTRY_COMPANY_ID = Column.Nullable("id_registry_id_companyid")
        val ANIMAL_REGISTRATION_TYPE_ID = Column.Nullable("id_animalregistrationtypeid")
        val FLOCK_BOOK_ID = Column.Nullable("id_flockbookid")
        val REGISTRATION_DATE = Column.Nullable("registration_date")
        val REGISTRATION_DESCRIPTION = Column.Nullable("registration_description")
        val BREEDER_CONTACT_ID = Column.Nullable("id_breeder_id_contactid")
        val BREEDER_COMPANY_ID = Column.Nullable("id_breeder_id_companyid")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")

        val BREEDER_ID = Column.NotNull("id_breederid")
        val BREEDER_NAME = Column.NotNull("breeder_name")
        val BREEDER_TYPE_ID = Column.NotNull("breeder_type_id")
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID
}
