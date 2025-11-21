package com.weyr_associates.animaltrakkerfarmmobile.database.queries

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.weyr_associates.animaltrakkerfarmmobile.database.core.JoinType
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Sql
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readAllItems
import com.weyr_associates.animaltrakkerfarmmobile.database.sql.joinForSexModel
import com.weyr_associates.animaltrakkerfarmmobile.database.sql.projectionForSexModel
import com.weyr_associates.animaltrakkerfarmmobile.database.sql.readBreedingSummaryOffspring
import com.weyr_associates.animaltrakkerfarmmobile.database.sql.readBreedingSummaryTotalForSex
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.AnimalFlockPrefixTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.AnimalRegistrationTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.AnimalTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.FlockPrefixTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.SexTable
import com.weyr_associates.animaltrakkerfarmmobile.model.BreedingSummary
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId

fun SQLiteDatabase.queryOffspringCountBySexForSire(sireId: EntityId): List<BreedingSummary.Total> {
    return rawQuery(QUERY_OFFSPRING_COUNT_BY_SEX_FOR_SIRE, arrayOf(sireId.toString())).use { cursor ->
        cursor.readAllItems(Cursor::readBreedingSummaryTotalForSex)
    }
}

fun SQLiteDatabase.queryOffspringCountBySexForDam(damId: EntityId): List<BreedingSummary.Total> {
    return rawQuery(QUERY_OFFSPRING_COUNT_BY_SEX_FOR_DAM, arrayOf(damId.toString())).use { cursor ->
        cursor.readAllItems(Cursor::readBreedingSummaryTotalForSex)
    }
}

fun SQLiteDatabase.queryWeanedCountBySexForDam(damId: EntityId): List<BreedingSummary.Total> {
    return rawQuery(QUERY_WEANED_COUNT_BY_SEX_FOR_DAM, arrayOf(damId.toString())).use { cursor ->
        cursor.readAllItems(Cursor::readBreedingSummaryTotalForSex)
    }
}

fun SQLiteDatabase.queryOffspringOfSire(sireId: EntityId, registryCompanyId: EntityId?): List<BreedingSummary.Offspring> {
    val query = QUERY_OFFSPRING_OF_SIRE
    return rawQuery(query, arrayOf(sireId.toString(), registryCompanyId.toString())).use { cursor ->
        cursor.readAllItems(Cursor::readBreedingSummaryOffspring)
    }
}

fun SQLiteDatabase.queryOffspringOfDam(damId: EntityId, registryCompanyId: EntityId?): List<BreedingSummary.Offspring> {
    return rawQuery(QUERY_OFFSPRING_OF_DAM, arrayOf(damId.toString(), registryCompanyId.toString())).use { cursor ->
        cursor.readAllItems(Cursor::readBreedingSummaryOffspring)
    }
}

private val QUERY_OFFSPRING_COUNT_BY_SEX_FOR_SIRE get() =
    """SELECT
        |${projectionForSexModel()},
        |COUNT(${AnimalTable.NAME}.${AnimalTable.Columns.SIRE_ID}) AS ${Sql.Columns.COUNT}
        |FROM ${AnimalTable.NAME}
        |${joinForSexModel(JoinType.INNER, AnimalTable.NAME, AnimalTable.Columns.SEX_ID)}
        |WHERE ${AnimalTable.NAME}.${AnimalTable.Columns.SIRE_ID} = ?1
        |GROUP BY ${SexTable.NAME}.${SexTable.Columns.ID}
        |ORDER BY ${SexTable.NAME}.${SexTable.Columns.ORDER} DESC
    """.trimMargin()

private val QUERY_OFFSPRING_COUNT_BY_SEX_FOR_DAM get() =
    """SELECT
        |${projectionForSexModel()},
        |COUNT(${AnimalTable.NAME}.${AnimalTable.Columns.DAM_ID}) AS ${Sql.Columns.COUNT}
        |FROM ${AnimalTable.NAME}
        |${joinForSexModel(JoinType.INNER, AnimalTable.NAME, AnimalTable.Columns.SEX_ID)}
        |WHERE ${AnimalTable.NAME}.${AnimalTable.Columns.DAM_ID} = ?1
        |GROUP BY ${SexTable.NAME}.${SexTable.Columns.ID}
        |ORDER BY ${SexTable.NAME}.${SexTable.Columns.ORDER} DESC
    """.trimMargin()

private val QUERY_WEANED_COUNT_BY_SEX_FOR_DAM get() =
    """SELECT
        |${projectionForSexModel()},
        |COUNT(${AnimalTable.NAME}.${AnimalTable.Columns.ID}) AS ${Sql.Columns.COUNT}
        |FROM ${AnimalTable.NAME}
        |${joinForSexModel(JoinType.INNER, AnimalTable.NAME, AnimalTable.Columns.SEX_ID)}
        |WHERE ${AnimalTable.NAME}.${AnimalTable.Columns.WEANED_DATE} IS NOT NULL AND 
        |(
        |    (
        |        ${AnimalTable.NAME}.${AnimalTable.Columns.DAM_ID} = ?1 AND
        |        ${AnimalTable.NAME}.${AnimalTable.Columns.FOSTER_DAM_ID} IS NULL AND
        |        ${AnimalTable.NAME}.${AnimalTable.Columns.SURROGATE_DAM_ID} IS NULL
        |    ) OR
        |    ${AnimalTable.NAME}.${AnimalTable.Columns.FOSTER_DAM_ID} = ?1 OR
        |    ${AnimalTable.NAME}.${AnimalTable.Columns.SURROGATE_DAM_ID} = ?1
        |)
        |GROUP BY ${SexTable.NAME}.${SexTable.Columns.ID}
        |ORDER BY ${SexTable.NAME}.${SexTable.Columns.ORDER} DESC
    """.trimMargin()

private val QUERY_OFFSPRING get() = """
    |SELECT
    |${AnimalTable.project { arrayOf(ID, NAME, BIRTH_DATE, BIRTH_TIME) }},
    |${AnimalRegistrationTable.project { arrayOf(REGISTRATION_NUMBER) } },
    |${FlockPrefixTable.project { arrayOf(PREFIX) }},
    |${projectionForSexModel()}
    |FROM ${AnimalTable.NAME}
    |${AnimalFlockPrefixTable.join(
        joinType = JoinType.OUTER_LEFT,
        tableAliasPrefix = null,
        foreignTableName = AnimalTable.NAME,
        foreignColumn = AnimalTable.Columns.ID,
        localColumn = { ANIMAL_ID }
    )}
    |${FlockPrefixTable.join(
        joinType = JoinType.OUTER_LEFT,
        tableAliasPrefix = null,
        foreignTableName = AnimalFlockPrefixTable.NAME,
        foreignColumn = AnimalFlockPrefixTable.Columns.FLOCK_PREFIX_ID,
    )}
    |LEFT OUTER JOIN (
    |   SELECT *, MAX(${AnimalRegistrationTable.NAME}.${AnimalRegistrationTable.Columns.REGISTRATION_DATE})
    |   FROM ${AnimalRegistrationTable.NAME}
    |   WHERE ${AnimalRegistrationTable.NAME}.${AnimalRegistrationTable.Columns.ID_REGISTRY_COMPANY_ID} = ?2
    |   GROUP BY ${AnimalRegistrationTable.NAME}.${AnimalRegistrationTable.Columns.ANIMAL_ID}
    |) AS ${AnimalRegistrationTable.NAME} ON
    |   ${AnimalRegistrationTable.NAME}.${AnimalRegistrationTable.Columns.ANIMAL_ID} =
    |   ${AnimalTable.NAME}.${AnimalTable.Columns.ID}
    |${joinForSexModel(JoinType.INNER, AnimalTable.NAME, AnimalTable.Columns.SEX_ID)}
""".trimMargin()

private val ORDER_OFFSPRING get() = """
    |ORDER BY ${AnimalTable.NAME}.${AnimalTable.Columns.BIRTH_DATE} IS NOT NULL DESC,
    |${AnimalTable.NAME}.${AnimalTable.Columns.BIRTH_DATE} DESC,
    |${AnimalTable.NAME}.${AnimalTable.Columns.BIRTH_TIME} IS NOT NULL DESC,
    |${AnimalTable.NAME}.${AnimalTable.Columns.BIRTH_TIME} DESC,
    |${AnimalTable.NAME}.${AnimalTable.Columns.NAME}
""".trimMargin()

private val QUERY_OFFSPRING_OF_SIRE get() = """
    |$QUERY_OFFSPRING
    |WHERE ${AnimalTable.NAME}.${AnimalTable.Columns.SIRE_ID} = ?1
    |$ORDER_OFFSPRING
""".trimMargin()

private val QUERY_OFFSPRING_OF_DAM get() = """
    |$QUERY_OFFSPRING
    |WHERE ${AnimalTable.NAME}.${AnimalTable.Columns.DAM_ID} = ?1
    |$ORDER_OFFSPRING
""".trimMargin()
