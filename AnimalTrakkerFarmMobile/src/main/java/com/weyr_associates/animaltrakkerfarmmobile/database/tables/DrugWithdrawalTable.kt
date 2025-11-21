package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.DrugWithdrawalTable.Columns
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec

object DrugWithdrawalTable : TableSpec<Columns> {

    const val NAME = "drug_withdrawal_table"

    object Columns {
        val ID = Column.NotNull("id_drugwithdrawalid")
        val DRUG_ID = Column.NotNull("id_drugid")
        val SPECIES_ID = Column.NotNull("id_speciesid")
        val MEAT_WITHDRAWAL = Column.Nullable("drug_meat_withdrawal")
        val MEAT_WITHDRAWAL_UNITS_ID = Column.Nullable("id_meat_withdrawal_id_unitsid")
        val USER_MEAT_WITHDRAWAL = Column.Nullable("user_meat_withdrawal")
        val MILK_WITHDRAWAL = Column.Nullable("drug_milk_withdrawal")
        val MILK_WITHDRAWAL_UNITS_ID = Column.Nullable("id_milk_withdrawal_id_unitsid")
        val USER_MILK_WITHDRAWAL = Column.Nullable("user_milk_withdrawal")
        val OFFICIAL_DRUG_DOSAGE = Column.NotNull("official_drug_dosage")
        val USER_DRUG_DOSAGE = Column.NotNull("user_drug_dosage")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID
}
