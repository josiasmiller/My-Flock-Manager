package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column

object ScrapieFlockOwnerTable {

    const val NAME = "scrapie_flock_owner_table"

    object Columns {
        val ID = Column.NotNull("id_scrapieflockownerid")
        val SCRAPIE_FLOCK_ID = Column.NotNull("id_scrapieflocknumberid")
        val OWNER_CONTACT_ID = Column.Nullable("owner_id_contactid")
        val OWNER_COMPANY_ID = Column.Nullable("owner_id_companyid")
        val NOTE = Column.Nullable("owner_scrapie_flock_note")
        val START_USE_DATE = Column.NotNull("start_scrapie_flock_use")
        val END_USE_DATE = Column.Nullable("end_scrapie_flock_use")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")
    }
}
