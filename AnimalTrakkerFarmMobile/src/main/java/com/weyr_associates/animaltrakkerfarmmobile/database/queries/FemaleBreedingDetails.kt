package com.weyr_associates.animaltrakkerfarmmobile.database.queries

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Alias
import com.weyr_associates.animaltrakkerfarmmobile.database.core.JoinType
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Qualifier
import com.weyr_associates.animaltrakkerfarmmobile.database.core.Sql
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getInt
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getLocalDate
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getLocalTime
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getOptEntityId
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getOptLocalDate
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getOptLocalTime
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getOptString
import com.weyr_associates.animaltrakkerfarmmobile.database.core.getString
import com.weyr_associates.animaltrakkerfarmmobile.database.core.isNotNull
import com.weyr_associates.animaltrakkerfarmmobile.database.core.readFirstItem
import com.weyr_associates.animaltrakkerfarmmobile.database.sql.joinForSexModel
import com.weyr_associates.animaltrakkerfarmmobile.database.sql.projectionForSexModel
import com.weyr_associates.animaltrakkerfarmmobile.database.sql.projectionForSexModelIn
import com.weyr_associates.animaltrakkerfarmmobile.database.sql.readServiceType
import com.weyr_associates.animaltrakkerfarmmobile.database.sql.readSex
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.AnimalFemaleBreedingTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.AnimalFlockPrefixTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.AnimalMaleBreedingTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.AnimalTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.FlockPrefixTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.ServiceTypeTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.SexTable
import com.weyr_associates.animaltrakkerfarmmobile.database.tables.SpeciesTable
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.FemaleBreedingDetails
import com.weyr_associates.animaltrakkerfarmmobile.model.FemaleBreedingDetails.BreedingEvent
import com.weyr_associates.animaltrakkerfarmmobile.model.FemaleBreedingDetails.BreedingEventsByYear
import com.weyr_associates.animaltrakkerfarmmobile.model.FemaleBreedingDetails.MaleBreedingInfo
import com.weyr_associates.animaltrakkerfarmmobile.model.FemaleBreedingDetails.TotalBySex

fun SQLiteDatabase.queryFemaleBreedingDetails(damId: EntityId): FemaleBreedingDetails? {
    return rawQuery(QUERY_FEMALE_BREEDING_DETAILS, arrayOf(damId.toString())).use { cursor ->
        cursor.readFirstItem(Cursor::readFemaleBreedingDetails)
    }
}

private fun Cursor.readFemaleBreedingDetails(): FemaleBreedingDetails {
    val damId = getEntityId(AnimalTable.Columns.DAM_ID.toNotNull())
    val breedingEventsByYear = mutableListOf<BreedingEventsByYear>()

    do {
        val currentDamId = getEntityId(AnimalTable.Columns.DAM_ID.toNotNull())
        if (currentDamId != damId) {
            moveToPrevious()
            break
        }
        breedingEventsByYear.add(readBreedingEventsByYear())
    } while(moveToNext())

    return FemaleBreedingDetails(
        damId = damId,
        yearly = breedingEventsByYear
    )
}

private fun Cursor.readBreedingEventsByYear(): BreedingEventsByYear {
    val year = getInt(Sql.Columns.YEAR)
    val breedingEvents = mutableListOf<BreedingEvent>()
    val nonEventBirths = mutableListOf<TotalBySex>()

    do {
        val currentYear = getInt(Sql.Columns.YEAR)
        if (currentYear != year) {
            moveToPrevious()
            break
        }
        val femaleBreedingId = getOptEntityId(AnimalFemaleBreedingTable.Columns.ID)
        when (femaleBreedingId) {
            null -> { readTotalForSex()?.let { nonEventBirths.add(it) } }
            else -> { breedingEvents.add(readBreedingEvent()) }
        }
    } while (moveToNext())

    return BreedingEventsByYear(
        year = year,
        events = breedingEvents,
        nonEventBirthsBySex = nonEventBirths
    )
}

private fun Cursor.readBreedingEvent(): BreedingEvent {
    val femaleBreedingId = getOptEntityId(AnimalFemaleBreedingTable.Columns.ID)
    val maleBreedingInfo = readOptMaleBreedingInfo()
    val birthingDate = getOptLocalDate(AnimalFemaleBreedingTable.Columns.BIRTHING_DATE)
    val birthingTime = getOptLocalTime(AnimalFemaleBreedingTable.Columns.BIRTHING_TIME)
    val birthNotes = getOptString(AnimalFemaleBreedingTable.Columns.BIRTHING_NOTES)

    val birthsBySex = mutableListOf<TotalBySex>()

    do {
        val currentFemaleBreedingId = getOptEntityId(AnimalFemaleBreedingTable.Columns.ID)
        if (currentFemaleBreedingId != femaleBreedingId) {
            moveToPrevious()
            break
        }
        readTotalForSex()?.let { birthsBySex.add(it) }
    } while(moveToNext())

    return BreedingEvent(
        maleBreedingInfo = maleBreedingInfo,
        birthingDate = birthingDate,
        birthingTime = birthingTime,
        birthNotes = birthNotes,
        birthsBySex = birthsBySex
    )
}

private fun Cursor.readMaleBreedingInfo(): MaleBreedingInfo {
    return MaleBreedingInfo(
        maleBreedingId = getEntityId(AnimalMaleBreedingTable.Columns.ID),
        sireId = getEntityId(AnimalTable.Columns.ID.qualifiedBy(Qualifier.SIRE)),
        sireName = getString(AnimalTable.Columns.NAME.qualifiedBy(Qualifier.SIRE)),
        sireFlockPrefix = getOptString(FlockPrefixTable.Columns.PREFIX.qualifiedBy(Qualifier.SIRE)),
        dateMaleIn = getLocalDate(AnimalMaleBreedingTable.Columns.DATE_IN),
        timeMaleIn = getLocalTime(AnimalMaleBreedingTable.Columns.TIME_IN),
        dateMaleOut = getOptLocalDate(AnimalMaleBreedingTable.Columns.DATE_OUT),
        timeMaleOut = getOptLocalTime(AnimalMaleBreedingTable.Columns.TIME_OUT),
        serviceType = readServiceType()
    )
}

private fun Cursor.readOptMaleBreedingInfo(): MaleBreedingInfo? {
    return takeIf { it.isNotNull(AnimalMaleBreedingTable.Columns.ID) }
        ?.readMaleBreedingInfo()
}

private fun Cursor.readTotalForSex(): TotalBySex? {
    return getOptEntityId(SexTable.Columns.ID)?.let {
        TotalBySex(
            sex = readSex(),
            value = getInt(Sql.Columns.COUNT.qualifiedBy(Qualifier.OFFSPRING))
        )
    }
}

private const val ANIMAL_ALIAS = "animal"
private const val BREEDING_ALIAS = "breeding"

private val QUERY_FEMALE_BREEDING_DETAILS get() =
    """WITH animal_query AS (
    |   SELECT
    |       ${AnimalTable.project { arrayOf(ID, DAM_ID, BIRTH_DATE) }},
    |       ${projectionForSexModel()}
    |       FROM ${AnimalTable.NAME}
    |       ${joinForSexModel(
                joinType = JoinType.INNER,
                foreignKeyTableName = AnimalTable.NAME,
                foreignKeyColumn = AnimalTable.Columns.SEX_ID
            )}
    |       WHERE ${AnimalTable.NAME}.${AnimalTable.Columns.DAM_ID} = ?1
    |), breeding_query AS (
    |   SELECT
    |       ${AnimalFemaleBreedingTable.project { 
                arrayOf(ID, BIRTHING_DATE, BIRTHING_TIME, GESTATION_LENGTH, BIRTHING_NOTES) 
            }},
    |       ${Sql.columnProjection(
                tableIdentifier = AnimalFemaleBreedingTable.NAME,
                columnIdentifier = AnimalFemaleBreedingTable.Columns.ANIMAL_ID.name,
                columnAlias = AnimalFemaleBreedingTable.Columns.ANIMAL_ID.qualifiedName(Qualifier.DAM)
            )},
    |       ${AnimalMaleBreedingTable.project { 
                arrayOf(ID, DATE_IN, TIME_IN, DATE_OUT, TIME_OUT) 
            }},
    |       ${ServiceTypeTable.project { allColumns() }},
    |       ${Sql.columnProjection(
                tableIdentifier = AnimalMaleBreedingTable.NAME,
                columnIdentifier = AnimalMaleBreedingTable.Columns.ANIMAL_ID.name,
                columnAlias = AnimalMaleBreedingTable.Columns.ANIMAL_ID.qualifiedName(Qualifier.SIRE)
            )},
    |       ${Sql.columnProjection(
                tableIdentifier = AnimalTable.prefixWith(Qualifier.SIRE),
                columnIdentifier = AnimalTable.Columns.ID.name,
                columnAlias = AnimalTable.Columns.ID.qualifiedName(Qualifier.SIRE)
            )},
    |       ${Sql.columnProjection(
                tableIdentifier = AnimalTable.prefixWith(Alias.SIRE),
                columnIdentifier = AnimalTable.Columns.NAME.name,
                columnAlias = AnimalTable.Columns.NAME.qualifiedName(Qualifier.SIRE) 
            )},
    |       ${Sql.columnProjection(
                tableIdentifier = FlockPrefixTable.NAME,
                columnIdentifier = FlockPrefixTable.Columns.PREFIX.name,
                columnAlias = FlockPrefixTable.Columns.PREFIX.qualifiedName(Qualifier.SIRE) 
            )},
    |       ${SpeciesTable.project(tableQualifier = Alias.DAM) { 
                arrayOf(TYPICAL_GESTATION_LENGTH_DAYS) 
            }}
    |   FROM ${AnimalFemaleBreedingTable.NAME}
    |   ${AnimalTable.join(
            joinType = JoinType.INNER,
            foreignTableName = AnimalFemaleBreedingTable.NAME,
            foreignColumn = AnimalFemaleBreedingTable.Columns.ANIMAL_ID,
            tableAliasPrefix = Alias.DAM
        )}
    |   ${SexTable.join(
            joinType = JoinType.INNER,
            foreignTableName = AnimalTable.prefixWith(Alias.DAM),
            foreignColumn = AnimalTable.Columns.SEX_ID,
            tableAliasPrefix = Alias.DAM
        )}
    |   ${SpeciesTable.join(
            joinType = JoinType.INNER,
            foreignTableName = SexTable.prefixWith(Alias.DAM),
            foreignColumn = SexTable.Columns.SPECIES_ID,
            tableAliasPrefix = Alias.DAM
        )}
    |   ${AnimalMaleBreedingTable.join(
            joinType = JoinType.OUTER_LEFT,
            foreignTableName = AnimalFemaleBreedingTable.NAME,
            foreignColumn = AnimalFemaleBreedingTable.Columns.MALE_BREEDING_ID
        )}
    |   ${ServiceTypeTable.join(
            joinType = JoinType.OUTER_LEFT,
            foreignTableName = AnimalMaleBreedingTable.NAME,
            foreignColumn = AnimalMaleBreedingTable.Columns.SERVICE_TYPE_ID
        )}
    |   ${AnimalTable.join(
            joinType = JoinType.OUTER_LEFT,
            foreignTableName = AnimalMaleBreedingTable.NAME,
            foreignColumn = AnimalMaleBreedingTable.Columns.ANIMAL_ID,
            tableAliasPrefix = Alias.SIRE
        )}
    |   ${AnimalFlockPrefixTable.join(
            joinType = JoinType.OUTER_LEFT,
            foreignTableName = AnimalTable.prefixWith(Alias.SIRE),
            foreignColumn = AnimalTable.Columns.ID,
            localColumn = { ANIMAL_ID }
        )}
    |   ${FlockPrefixTable.join(
            joinType = JoinType.OUTER_LEFT,
            foreignTableName = AnimalFlockPrefixTable.NAME,
            foreignColumn = AnimalFlockPrefixTable.Columns.FLOCK_PREFIX_ID
        )}
    |   WHERE ${AnimalFemaleBreedingTable.NAME}.${AnimalFemaleBreedingTable.Columns.ANIMAL_ID} = ?1
    |)
    |SELECT
    |   ${ANIMAL_ALIAS}.${AnimalTable.Columns.DAM_ID},
    |   STRFTIME('%Y', ${ANIMAL_ALIAS}.${AnimalTable.Columns.BIRTH_DATE}) AS ${Sql.Columns.YEAR},
    |   ${BREEDING_ALIAS}.${AnimalFemaleBreedingTable.Columns.ID},
	|   ${BREEDING_ALIAS}.${AnimalTable.Columns.NAME.qualifiedName(Qualifier.SIRE)},
	|   ${BREEDING_ALIAS}.${FlockPrefixTable.Columns.PREFIX.qualifiedName(Qualifier.SIRE)},
    |   ${BREEDING_ALIAS}.${AnimalMaleBreedingTable.Columns.ID},
    |   ${BREEDING_ALIAS}.${AnimalTable.Columns.ID.qualifiedBy(Qualifier.SIRE)},
    |   ${BREEDING_ALIAS}.${AnimalTable.Columns.NAME.qualifiedBy(Qualifier.SIRE)},
    |   ${BREEDING_ALIAS}.${FlockPrefixTable.Columns.PREFIX.qualifiedBy(Qualifier.SIRE)},
	|   ${BREEDING_ALIAS}.${AnimalMaleBreedingTable.Columns.DATE_IN},
	|   ${BREEDING_ALIAS}.${AnimalMaleBreedingTable.Columns.TIME_IN},
	|   ${BREEDING_ALIAS}.${AnimalMaleBreedingTable.Columns.DATE_OUT},
	|   ${BREEDING_ALIAS}.${AnimalMaleBreedingTable.Columns.TIME_OUT},
    |   ${ServiceTypeTable.projectIn(resultsIdentifier = BREEDING_ALIAS) { allColumns() }},   
	|   ${BREEDING_ALIAS}.${AnimalFemaleBreedingTable.Columns.GESTATION_LENGTH},
    |   ${BREEDING_ALIAS}.${AnimalFemaleBreedingTable.Columns.BIRTHING_DATE},
    |   ${BREEDING_ALIAS}.${AnimalFemaleBreedingTable.Columns.BIRTHING_TIME},   
	|   ${BREEDING_ALIAS}.${AnimalFemaleBreedingTable.Columns.BIRTHING_NOTES},
    |   ${projectionForSexModelIn(resultsIdentifier = ANIMAL_ALIAS)},   
	|   COUNT(${ANIMAL_ALIAS}.${AnimalTable.Columns.ID}) AS ${Sql.Columns.COUNT.qualifiedName("offspring")}
    |FROM animal_query AS $ANIMAL_ALIAS
    |LEFT OUTER JOIN breeding_query AS $BREEDING_ALIAS
    |   ON ${ANIMAL_ALIAS}.${AnimalTable.Columns.DAM_ID} = 
    |       ${BREEDING_ALIAS}.${AnimalFemaleBreedingTable.Columns.ANIMAL_ID.qualifiedName(Qualifier.DAM)}
    |       AND ${ANIMAL_ALIAS}.${AnimalTable.Columns.BIRTH_DATE} BETWEEN 
    |           ${BREEDING_ALIAS}.${AnimalFemaleBreedingTable.Columns.BIRTHING_DATE} AND 
    |               DATE(${BREEDING_ALIAS}.${AnimalFemaleBreedingTable.Columns.BIRTHING_DATE}, '+1 day')
    |GROUP BY ${Sql.Columns.YEAR}, 
    |   ${BREEDING_ALIAS}.${AnimalFemaleBreedingTable.Columns.ID}, 
    |   ${ANIMAL_ALIAS}.${SexTable.Columns.NAME}
    |UNION
    |SELECT
    |   ${BREEDING_ALIAS}.${AnimalFemaleBreedingTable.Columns.ANIMAL_ID.qualifiedBy(Qualifier.DAM)} AS ${AnimalTable.Columns.DAM_ID},
    |   STRFTIME('%Y', 
    |       COALESCE(
    |           ${ANIMAL_ALIAS}.${AnimalTable.Columns.BIRTH_DATE},
    |           ${BREEDING_ALIAS}.${AnimalFemaleBreedingTable.Columns.BIRTHING_DATE},
    |           DATE(
    |               ${BREEDING_ALIAS}.${AnimalMaleBreedingTable.Columns.DATE_OUT}, 
    |               '+' || ${BREEDING_ALIAS}.${SpeciesTable.Columns.TYPICAL_GESTATION_LENGTH_DAYS} || ' days'
    |           )
    |       )
    |   ) AS ${Sql.Columns.YEAR},
    |   ${BREEDING_ALIAS}.${AnimalFemaleBreedingTable.Columns.ID},
	|   ${BREEDING_ALIAS}.${AnimalTable.Columns.NAME.qualifiedName(Qualifier.SIRE)},
	|   ${BREEDING_ALIAS}.${FlockPrefixTable.Columns.PREFIX.qualifiedName(Qualifier.SIRE)},
    |   ${BREEDING_ALIAS}.${AnimalMaleBreedingTable.Columns.ID},
    |   ${BREEDING_ALIAS}.${AnimalTable.Columns.ID.qualifiedBy(Qualifier.SIRE)},
    |   ${BREEDING_ALIAS}.${AnimalTable.Columns.NAME.qualifiedBy(Qualifier.SIRE)},
    |   ${BREEDING_ALIAS}.${FlockPrefixTable.Columns.PREFIX.qualifiedBy(Qualifier.SIRE)},
	|   ${BREEDING_ALIAS}.${AnimalMaleBreedingTable.Columns.DATE_IN},
	|   ${BREEDING_ALIAS}.${AnimalMaleBreedingTable.Columns.TIME_IN},
	|   ${BREEDING_ALIAS}.${AnimalMaleBreedingTable.Columns.DATE_OUT},
	|   ${BREEDING_ALIAS}.${AnimalMaleBreedingTable.Columns.TIME_OUT},
    |   ${ServiceTypeTable.projectIn(resultsIdentifier = BREEDING_ALIAS) { allColumns() }},   
	|   ${BREEDING_ALIAS}.${AnimalFemaleBreedingTable.Columns.GESTATION_LENGTH},
    |   ${BREEDING_ALIAS}.${AnimalFemaleBreedingTable.Columns.BIRTHING_DATE},
    |   ${BREEDING_ALIAS}.${AnimalFemaleBreedingTable.Columns.BIRTHING_TIME},   
	|   ${BREEDING_ALIAS}.${AnimalFemaleBreedingTable.Columns.BIRTHING_NOTES},
    |   ${projectionForSexModelIn(resultsIdentifier = ANIMAL_ALIAS)},   
	|   COUNT(${ANIMAL_ALIAS}.${AnimalTable.Columns.ID}) AS ${Sql.Columns.COUNT.qualifiedName("offspring")}
    |FROM breeding_query AS $BREEDING_ALIAS
    |LEFT OUTER JOIN animal_query AS $ANIMAL_ALIAS
    |   ON ${ANIMAL_ALIAS}.${AnimalTable.Columns.DAM_ID} = 
    |       ${BREEDING_ALIAS}.${AnimalFemaleBreedingTable.Columns.ANIMAL_ID.qualifiedName(Qualifier.DAM)}
    |       AND ${ANIMAL_ALIAS}.${AnimalTable.Columns.BIRTH_DATE} BETWEEN 
    |           ${BREEDING_ALIAS}.${AnimalFemaleBreedingTable.Columns.BIRTHING_DATE} AND 
    |               DATE(${BREEDING_ALIAS}.${AnimalFemaleBreedingTable.Columns.BIRTHING_DATE}, '+1 day')
    |GROUP BY
    |   ${AnimalTable.Columns.DAM_ID},
    |   ${Sql.Columns.YEAR}, 
    |   ${BREEDING_ALIAS}.${AnimalFemaleBreedingTable.Columns.ID}, 
    |   ${ANIMAL_ALIAS}.${SexTable.Columns.NAME}
    |   ORDER BY ${Sql.Columns.YEAR} DESC, 
    |       ${BREEDING_ALIAS}.${AnimalFemaleBreedingTable.Columns.ID} DESC, 
    |       ${ANIMAL_ALIAS}.${SexTable.Columns.ORDER} DESC
    """.trimMargin()
