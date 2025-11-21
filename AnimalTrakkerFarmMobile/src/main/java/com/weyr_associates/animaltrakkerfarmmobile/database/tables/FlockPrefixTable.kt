package com.weyr_associates.animaltrakkerfarmmobile.database.tables

import android.database.Cursor
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.FlockPrefixTable.Columns
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getOptEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getString
import com.weyr_associates.animaltrakkerfarmmobile.model.FlockPrefix
import com.weyr_associates.animaltrakkerfarmmobile.model.Owner

object FlockPrefixTable : TableSpec<Columns> {

    const val NAME = "flock_prefix_table"

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.ID

    object Columns {
        val ID = Column.NotNull("id_flockprefixid")
        val PREFIX = Column.NotNull("flock_prefix")
        val OWNER_CONTACT_ID = Column.Nullable("id_prefixowner_id_contactid")
        val OWNER_COMPANY_ID = Column.Nullable("id_prefixowner_id_companyid")
        val REGISTRY_COMPANY_ID = Column.NotNull("id_registry_id_companyid")
        val CREATED = Column.NotNull("created")
        val MODIFIED = Column.NotNull("modified")
    }

    object Sql {
        val QUERY_FLOCK_PREFIX_BY_ID get() =
            """SELECT * FROM ${NAME}
                WHERE ${FlockPrefixTable.Columns.ID} = ?"""

        val QUERY_FLOCK_PREFIX_BY_OWNER get() =
            """SELECT * FROM ${NAME}
                WHERE ((${FlockPrefixTable.Columns.OWNER_CONTACT_ID} = ?1 AND "${Owner.Type.TYPE_ID_CONTACT}" = ?2) 
                OR (${FlockPrefixTable.Columns.OWNER_COMPANY_ID} = ?1 AND "${Owner.Type.TYPE_ID_COMPANY}" = ?2)) 
                AND ${FlockPrefixTable.Columns.REGISTRY_COMPANY_ID} = ?3"""
    }

    fun flockPrefixFromCursor(cursor: Cursor): FlockPrefix {
        val ownerContactId = cursor.getOptEntityId(Columns.OWNER_CONTACT_ID)
        val ownerCompanyId = cursor.getOptEntityId(Columns.OWNER_COMPANY_ID)
        return FlockPrefix(
            id = cursor.getEntityId(Columns.ID),
            name = cursor.getString(Columns.PREFIX),
            ownerId = when {
                ownerContactId != null -> ownerContactId
                ownerCompanyId != null -> ownerCompanyId
                else -> throw IllegalStateException("No company or contact owner id.")
            },
            ownerType = when {
                ownerContactId != null -> Owner.Type.CONTACT
                ownerCompanyId != null -> Owner.Type.COMPANY
                else -> throw IllegalStateException("No company or contact owner id.")
            },
            registryCompanyId = cursor.getEntityId(Columns.REGISTRY_COMPANY_ID)
        )
    }
}
