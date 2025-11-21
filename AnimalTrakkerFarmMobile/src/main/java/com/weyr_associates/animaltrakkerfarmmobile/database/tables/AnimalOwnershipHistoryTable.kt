package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import android.database.Cursor
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.AnimalOwnershipHistoryTable.Columns
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getOptEntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalOwnership
import com.weyr_associates.animaltrakkerfarmmobile.model.Owner

object AnimalOwnershipHistoryTable : TableSpec<Columns> {

    const val NAME = "animal_ownership_history_table"

    fun animalOwnershipFromCursor(cursor: Cursor): AnimalOwnership {
        val toContactId = cursor.getOptEntityId(Columns.TO_CONTACT_ID)
        val toCompanyId = cursor.getOptEntityId(Columns.TO_COMPANY_ID)
        val ownerType = when {
            toContactId != null -> Owner.Type.CONTACT
            toCompanyId != null -> Owner.Type.COMPANY
            else -> throw IllegalStateException("Owner type indeterminate.")
        }
        return AnimalOwnership(
            id = cursor.getEntityId(Columns.ID),
            animalId = cursor.getEntityId(Columns.ANIMAL_ID),
            ownerId = when (ownerType) {
                Owner.Type.CONTACT -> requireNotNull(toContactId)
                Owner.Type.COMPANY -> requireNotNull(toCompanyId)
            },
            ownerType = ownerType
        )
    }

    object Columns {
        val ID = Column.NotNull("id_animalownershiphistoryid")
        val ANIMAL_ID = Column.NotNull("id_animalid")
        val TRANSFER_DATE = Column.NotNull("transfer_date")
        val FROM_CONTACT_ID = Column.Nullable("from_id_contactid")
        val TO_CONTACT_ID = Column.Nullable("to_id_contactid")
        val FROM_COMPANY_ID = Column.Nullable("from_id_companyid")
        val TO_COMPANY_ID = Column.Nullable("to_id_companyid")
        val TRANSFER_REASON_ID = Column.NotNull("id_transferreasonid")
        val SALE_PRICE = Column.Nullable("sale_price")
        val SALE_PRICE_UNITS_ID = Column.Nullable("sale_price_id_unitsid")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")
    }

    object Sql {

        val SQL_CURRENT_ANIMAL_OWNERSHIP get() =
            """SELECT ${Columns.ID}, 
	            ${Columns.ANIMAL_ID}, 
	            MAX(${Columns.TRANSFER_DATE})
                    AS ${Columns.TRANSFER_DATE},
                ${Columns.TO_CONTACT_ID},
                ${Columns.TO_COMPANY_ID},
                ${Columns.FROM_CONTACT_ID},
                ${Columns.FROM_COMPANY_ID},
                ${Columns.TRANSFER_REASON_ID}, 
                ${Columns.SALE_PRICE}, 
                ${Columns.SALE_PRICE_UNITS_ID}
            FROM $NAME
            GROUP BY ${Columns.ANIMAL_ID}"""

        val QUERY_CURRENT_ANIMAL_OWNERSHIP_BY_ANIMAL_ID get() =
            """SELECT ${Columns.ID}, 
	            ${Columns.ANIMAL_ID}, 
	            MAX(${Columns.TRANSFER_DATE})
                    AS ${Columns.TRANSFER_DATE},
                ${Columns.TO_CONTACT_ID},
                ${Columns.TO_COMPANY_ID},
                ${Columns.FROM_CONTACT_ID},
                ${Columns.FROM_COMPANY_ID},
                ${Columns.TRANSFER_REASON_ID}, 
                ${Columns.SALE_PRICE}, 
                ${Columns.SALE_PRICE_UNITS_ID}
            FROM $NAME
            WHERE ${Columns.ANIMAL_ID} = ?1
            GROUP BY ${Columns.ANIMAL_ID}"""

        val QUERY_ANIMAL_OWNERSHIP_ON_DATE get() =
            """SELECT ${Columns.ID}, 
	            ${Columns.ANIMAL_ID}, 
	            MAX(${Columns.TRANSFER_DATE})
                    AS ${Columns.TRANSFER_DATE},
                ${Columns.TO_CONTACT_ID},
                ${Columns.TO_COMPANY_ID},
                ${Columns.FROM_CONTACT_ID},
                ${Columns.FROM_COMPANY_ID},
                ${Columns.TRANSFER_REASON_ID}, 
                ${Columns.SALE_PRICE}, 
                ${Columns.SALE_PRICE_UNITS_ID}
            FROM $NAME
            WHERE ${Columns.ANIMAL_ID} = ?1
                AND ${Columns.TRANSFER_DATE} <= ?2
            GROUP BY ${Columns.ANIMAL_ID}"""
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID
}
