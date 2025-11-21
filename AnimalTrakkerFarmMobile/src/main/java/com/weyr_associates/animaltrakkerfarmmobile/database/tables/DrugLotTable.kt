package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.DrugLotTable.Columns
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec

object DrugLotTable : TableSpec<Columns> {

    const val NAME = "drug_lot_table"

    object Columns {
        val ID = Column.NotNull("id_druglotid")
        val DRUG_ID = Column.NotNull("id_drugid")
        val LOT = Column.NotNull("drug_lot")
        val EXPIRATION_DATE = Column.Nullable("drug_expire_date")
        val PURCHASE_DATE = Column.Nullable("drug_purchase_date")
        val AMOUNT_PURCHASED = Column.Nullable("drug_amount_purchased")
        val COST = Column.Nullable("drug_cost")
        val COST_UNITS_ID = Column.Nullable("id_drug_cost_id_unitsid")
        val DISPOSE_DATE = Column.Nullable("drug_dispose_date")
        val IS_GONE = Column.NotNull("drug_gone")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID
}
