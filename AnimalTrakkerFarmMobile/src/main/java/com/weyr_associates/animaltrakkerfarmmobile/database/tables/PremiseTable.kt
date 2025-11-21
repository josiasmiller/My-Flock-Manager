package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.PremiseTable.Columns
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec

object PremiseTable : TableSpec<Columns> {

    const val NAME = "premise_table"

    object Columns {
        val ID = Column.NotNull("id_premiseid")
        val TYPE_ID = Column.NotNull("id_premisetypeid")
        val JURISDICTION_ID = Column.Nullable("id_premisejurisdictionid")
        val PREMISE_NUMBER = Column.Nullable("premise_number")
        val ADDRESS1 = Column.Nullable("premise_address1")
        val ADDRESS2 = Column.Nullable("premise_address2")
        val CITY = Column.Nullable("premise_city")
        val STATE_ID = Column.Nullable("premise_id_stateid")
        val POSTCODE = Column.Nullable("premise_postcode")
        val COUNTY_ID = Column.Nullable("premise_id_countyid")
        val COUNTRY_ID = Column.Nullable("premise_id_countryid")
        val LATITUDE = Column.Nullable("premise_latitude")
        val LONGITUDE = Column.Nullable("premise_longitude")

        fun allColumns(): Array<Column> = arrayOf(
            ID, TYPE_ID, JURISDICTION_ID, PREMISE_NUMBER, ADDRESS1, ADDRESS2,
            CITY, STATE_ID, POSTCODE, COUNTY_ID, COUNTRY_ID, LATITUDE, LONGITUDE
        )
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID
}
