package com.weyr_associates.animaltrakkerfarmmobile.database.queries

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Column
import com.weyr_associates.animaltrakkerfarmmobile.database.core.JoinType
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Qualifier
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Sql
import com.weyr_associates.animaltrakkerfarmmobile.database.core.TableSpec
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getInt
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getLocalDate
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getLocalTime
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getOptEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getOptLocalDate
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getOptLocalTime
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getOptString
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readFirstItem
import com.weyr_associates.animaltrakkerfarmmobile.database.sql.joinForSexModel
import com.weyr_associates.animaltrakkerfarmmobile.database.sql.projectionForSexModel
import com.weyr_associates.animaltrakkerfarmmobile.database.sql.readServiceType
import com.weyr_associates.animaltrakkerfarmmobile.database.sql.readSex
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.AnimalFemaleBreedingTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.AnimalMaleBreedingTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.AnimalTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.ServiceTypeTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.SexTable
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.MaleBreedingDetails

fun SQLiteDatabase.queryMaleBreedingDetails(animalId: EntityId): MaleBreedingDetails? {
    return rawQuery(QUERY_MALE_BREEDING_DETAILS, arrayOf(animalId.toString())).use { cursor ->
        cursor.readFirstItem(Cursor::readMaleBreedingDetails)
    }
}

fun Cursor.readMaleBreedingDetails(colQualifier: String? = null): MaleBreedingDetails {
    val sireId = getEntityId(MaleBreedingJoin.Columns.SIRE_ID.qualifiedBy(colQualifier))
    val breedingEvents = mutableListOf<MaleBreedingDetails.BreedingEvent>()
    val nonEventBirths = mutableListOf<MaleBreedingDetails.TotalBySex>()
    do {
        val currentMaleBreedingId = getOptEntityId(MaleBreedingJoin.Columns.MALE_BREEDING_ID)
        when (currentMaleBreedingId) {
            null -> { nonEventBirths.add(readTotalForSex(colQualifier)) }
            else -> { breedingEvents.add(readBreedingEvent(colQualifier)) }
        }
    } while (moveToNext())

    return MaleBreedingDetails(
        sireId = sireId,
        events = breedingEvents,
        nonEventBirthsBySex = nonEventBirths
    )
}

private fun Cursor.readBreedingEvent(colQualifier: String? = null): MaleBreedingDetails.BreedingEvent {
    val breedingRecordId = getEntityId(MaleBreedingJoin.Columns.MALE_BREEDING_ID.qualifiedBy(colQualifier))
    val dateIn = getLocalDate(MaleBreedingJoin.Columns.DATE_IN.qualifiedBy(colQualifier))
    val timeIn = getLocalTime(MaleBreedingJoin.Columns.TIME_IN.qualifiedBy(colQualifier))
    val dateOut = getOptLocalDate(MaleBreedingJoin.Columns.DATE_OUT.qualifiedBy(colQualifier))
    val timeOut = getOptLocalTime(MaleBreedingJoin.Columns.TIME_OUT.qualifiedBy(colQualifier))
    val serviceType = readServiceType(colQualifier)
    val birthingDate = getOptLocalDate(MaleBreedingJoin.Columns.BIRTHING_DATE)
    val birthingTime = getOptLocalTime(MaleBreedingJoin.Columns.BIRTHING_TIME)
    val birthingNotes = getOptString(MaleBreedingJoin.Columns.BIRTHING_NOTES)
    val totals = mutableListOf<MaleBreedingDetails.TotalBySex>()

    totals.add(readTotalForSex(colQualifier))

    while (moveToNext()) {
        val currentBreedingRecordId = getOptEntityId(MaleBreedingJoin.Columns.MALE_BREEDING_ID)
        if (currentBreedingRecordId != breedingRecordId) {
            moveToPrevious()
            break
        }
        totals.add(readTotalForSex(colQualifier))
    }

    return MaleBreedingDetails.BreedingEvent(
        maleBreedingId = breedingRecordId,
        dateMaleIn = dateIn,
        timeMaleIn = timeIn,
        dateMaleOut = dateOut,
        timeMaleOut = timeOut,
        serviceType = serviceType,
        birthingDate = birthingDate,
        birthingTime = birthingTime,
        birthNotes = birthingNotes,
        birthsBySex = totals
    )
}

private fun Cursor.readTotalForSex(colQualifier: String? = null): MaleBreedingDetails.TotalBySex {
    return MaleBreedingDetails.TotalBySex(
        sex = readSex(colQualifier),
        value = getInt(Sql.Columns.COUNT.qualifiedBy(colQualifier))
    )
}

private object MaleBreedingJoin : TableSpec<MaleBreedingJoin.Columns> {

    const val NAME = "male_breeding_join"

    object Columns {
        val MALE_BREEDING_ID get() = AnimalMaleBreedingTable.Columns.ID
        val SIRE_ID get() = AnimalMaleBreedingTable.Columns.ANIMAL_ID.qualifiedBy(Qualifier.SIRE)
        val DATE_IN get() = AnimalMaleBreedingTable.Columns.DATE_IN
        val TIME_IN get() = AnimalMaleBreedingTable.Columns.TIME_IN
        val DATE_OUT get() = AnimalMaleBreedingTable.Columns.DATE_OUT
        val TIME_OUT get() = AnimalMaleBreedingTable.Columns.TIME_OUT
        val SERVICE_TYPE_ID get() = ServiceTypeTable.Columns.ID
        val SERVICE_TYPE_NAME get() = ServiceTypeTable.Columns.NAME
        val SERVICE_TYPE_ABBR get() = ServiceTypeTable.Columns.ABBREVIATION
        val SERVICE_TYPE_ORDER get() = ServiceTypeTable.Columns.ORDER
        val FEMALE_BREEDING_ID get() = AnimalFemaleBreedingTable.Columns.ID.toNullable()
        val DAM_ID get() = AnimalFemaleBreedingTable.Columns.ANIMAL_ID.qualifiedBy(Qualifier.DAM).toNullable()
        val BIRTHING_DATE get() = AnimalFemaleBreedingTable.Columns.BIRTHING_DATE
        val BIRTHING_TIME get() = AnimalFemaleBreedingTable.Columns.BIRTHING_TIME
        val BIRTHING_NOTES get() = AnimalFemaleBreedingTable.Columns.BIRTHING_NOTES

        fun allColumns(): Array<Column> {
            return arrayOf(
                MALE_BREEDING_ID,
                SIRE_ID,
                DATE_IN,
                DATE_OUT,
                TIME_IN,
                TIME_OUT,
                SERVICE_TYPE_ID,
                SERVICE_TYPE_NAME,
                SERVICE_TYPE_ABBR,
                SERVICE_TYPE_ORDER,
                FEMALE_BREEDING_ID,
                DAM_ID,
                BIRTHING_DATE,
                BIRTHING_TIME,
                BIRTHING_NOTES
            )
        }
    }

    override val name: String = NAME
    override val columns: Columns = Columns
    override val primaryKeyColumnName: Column.NotNull = Columns.MALE_BREEDING_ID
}

private val QUERY_MALE_BREEDING_DETAILS get() =
    """SELECT
        |${MaleBreedingJoin.project { allColumns() }},
        |${projectionForSexModel()},
        |COUNT(${AnimalTable.NAME}.${AnimalTable.Columns.ID}) AS ${Sql.Columns.COUNT}
        |FROM (
        |   SELECT ${AnimalTable.project { arrayOf(ID, SEX_ID, SIRE_ID, DAM_ID, BIRTH_DATE) }} 
        |   FROM ${AnimalTable.NAME}
        |   WHERE ${AnimalTable.NAME}.${AnimalTable.Columns.SIRE_ID} = ?1
        |) AS ${AnimalTable.NAME}
        |${joinForSexModel(
            joinType = JoinType.INNER,
            foreignKeyTableName = AnimalTable.NAME,
            foreignKeyColumn = AnimalTable.Columns.SEX_ID
        )}
        |LEFT OUTER JOIN (
        |   SELECT
        |       ${AnimalMaleBreedingTable.project { 
                    arrayOf(ID, DATE_IN, TIME_IN, DATE_OUT, TIME_OUT, SERVICE_TYPE_ID) 
                }},
        |       ${Sql.columnProjection(
                    tableIdentifier = AnimalMaleBreedingTable.NAME,
                    columnIdentifier = AnimalMaleBreedingTable.Columns.ANIMAL_ID.name,
                    columnAlias = AnimalMaleBreedingTable.Columns.ANIMAL_ID.qualifiedName(Qualifier.SIRE)
                )},
        |       ${AnimalFemaleBreedingTable.project {
                    arrayOf(ID, BIRTHING_DATE, BIRTHING_TIME, BIRTHING_NOTES)
                }},
        |       ${Sql.columnProjection(
                    tableIdentifier = AnimalFemaleBreedingTable.NAME,
                    columnIdentifier = AnimalFemaleBreedingTable.Columns.ANIMAL_ID.name,
                    columnAlias = AnimalFemaleBreedingTable.Columns.ANIMAL_ID.qualifiedName(Qualifier.DAM)
                )},
        |       ${ServiceTypeTable.project { allColumns() }}
        |   FROM ${AnimalMaleBreedingTable.NAME}
        |   ${ServiceTypeTable.join(
                joinType = JoinType.INNER,
                tableAliasPrefix = null,
                foreignTableName = AnimalMaleBreedingTable.NAME,
                foreignColumn = AnimalMaleBreedingTable.Columns.SERVICE_TYPE_ID
            )}
        |   ${AnimalFemaleBreedingTable.join(
                joinType = JoinType.OUTER_LEFT,
                tableAliasPrefix = null,
                foreignTableName = AnimalMaleBreedingTable.NAME,
                foreignColumn = AnimalMaleBreedingTable.Columns.ID,
                localColumn = { MALE_BREEDING_ID }
            )}
        |   WHERE ${AnimalMaleBreedingTable.NAME}.${AnimalMaleBreedingTable.Columns.ANIMAL_ID} = ?1
        |) AS ${MaleBreedingJoin.NAME}
        |ON ${AnimalTable.NAME}.${AnimalTable.Columns.SIRE_ID} = ${MaleBreedingJoin.NAME}.${MaleBreedingJoin.Columns.SIRE_ID}
        |   AND ${AnimalTable.NAME}.${AnimalTable.Columns.DAM_ID} = ${MaleBreedingJoin.NAME}.${MaleBreedingJoin.Columns.DAM_ID}
        |   AND ${AnimalTable.NAME}.${AnimalTable.Columns.BIRTH_DATE} BETWEEN ${MaleBreedingJoin.NAME}.${MaleBreedingJoin.Columns.BIRTHING_DATE}
        |       AND DATE(${MaleBreedingJoin.NAME}.${MaleBreedingJoin.Columns.BIRTHING_DATE}, '+1 day')
        |GROUP BY ${MaleBreedingJoin.NAME}.${MaleBreedingJoin.Columns.MALE_BREEDING_ID},
        |   ${SexTable.NAME}.${SexTable.Columns.ID}
        |ORDER BY ${MaleBreedingJoin.NAME}.${MaleBreedingJoin.Columns.DATE_IN} DESC,
        |   ${SexTable.NAME}.${SexTable.Columns.ORDER} DESC
    """.trimMargin()
