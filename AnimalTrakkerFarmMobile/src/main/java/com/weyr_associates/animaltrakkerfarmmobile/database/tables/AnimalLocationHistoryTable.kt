package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import com.weyr_associates.animaltrakkerfarmmobile.database.tables.AnimalLocationHistoryTable.Columns
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec

object AnimalLocationHistoryTable : TableSpec<Columns> {

    const val NAME = "animal_location_history_table"

    object Columns {
        val ID = Column.NotNull("id_animallocationhistoryid")
        val ANIMAL_ID = Column.NotNull("id_animalid")
        val MOVEMENT_DATE = Column.NotNull("movement_date")
        val FROM_PREMISE_ID = Column.Nullable("from_id_premiseid")
        val TO_PREMISE_ID = Column.Nullable("to_id_premiseid")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")

        fun allColumns(): Array<Column> {
            return arrayOf(ID, ANIMAL_ID, MOVEMENT_DATE, FROM_PREMISE_ID, TO_PREMISE_ID)
        }
    }

    object Sql {
        val QUERY_ANIMAL_CURRENT_LOCATION_PREMISE_ID get() =
            """SELECT ${Columns.TO_PREMISE_ID} FROM ${NAME}
                WHERE ${Columns.ANIMAL_ID} = ?
                ORDER BY ${Columns.MOVEMENT_DATE} DESC
                LIMIT 1"""

        val QUERY_ANIMAL_CURRENT_LOCATION_PREMISE get() =
            """SELECT
                ${PremiseTable.NAME}.*,
                ${StatesTable.NAME}.${StatesTable.Columns.NAME},
                ${CountryTable.NAME}.${CountryTable.Columns.NAME},
                ${PremiseJurisdictionTable.NAME}.${PremiseJurisdictionTable.Columns.ID},
                ${PremiseJurisdictionTable.NAME}.${PremiseJurisdictionTable.Columns.NAME},
                ${PremiseJurisdictionTable.NAME}.${PremiseJurisdictionTable.Columns.ORDER},
                ${PremiseNicknameTable.NAME}.${PremiseNicknameTable.Columns.NICKNAME}
                FROM ${NAME}
                INNER JOIN ${PremiseTable.NAME}
                    ON ${NAME}.${AnimalLocationHistoryTable.Columns.TO_PREMISE_ID} =
                        ${PremiseTable.NAME}.${PremiseTable.Columns.ID}
                INNER JOIN ${StatesTable.NAME}
                    ON ${PremiseTable.NAME}.${PremiseTable.Columns.STATE_ID} =
                        ${StatesTable.NAME}.${StatesTable.Columns.ID}
                INNER JOIN ${CountryTable.NAME}
                    ON ${PremiseTable.NAME}.${PremiseTable.Columns.COUNTRY_ID} =
                        ${CountryTable.NAME}.${CountryTable.Columns.ID}
                LEFT OUTER JOIN ${PremiseJurisdictionTable.NAME}
                    ON ${PremiseTable.NAME}.${PremiseTable.Columns.JURISDICTION_ID} =
                        ${PremiseJurisdictionTable.NAME}.${PremiseJurisdictionTable.Columns.ID}
                LEFT OUTER JOIN ${PremiseNicknameTable.NAME}
                    ON ${PremiseTable.NAME}.${PremiseTable.Columns.ID} =
                        ${PremiseNicknameTable.NAME}.${PremiseNicknameTable.Columns.PREMISE_ID} AND
                        ${PremiseNicknameTable.NAME}.${PremiseNicknameTable.Columns.CONTACT_ID} = ?1
                WHERE ${NAME}.${AnimalLocationHistoryTable.Columns.ANIMAL_ID} = ?
                ORDER BY ${Columns.MOVEMENT_DATE} DESC
                LIMIT 1"""
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID
}
