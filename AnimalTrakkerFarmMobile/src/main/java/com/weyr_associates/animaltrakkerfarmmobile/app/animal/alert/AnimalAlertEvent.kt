package com.weyr_associates.animaltrakkerfarmmobile.app.animal.alert

import com.weyr_associates.animaltrakkerfarmmobile.app.animal.lookup.LookupAnimalInfo.AnimalInfoState
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalAlert
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

data class AnimalAlertEvent(val alerts: List<AnimalAlert>)

fun Flow<AnimalInfoState>.extractAnimalAlertEvents(): Flow<AnimalAlertEvent> {
    return filterIsInstance<AnimalInfoState.Loaded>()
        .map { it.animalBasicInfo.alerts }
        .filterNotNull()
        .filter { it.isNotEmpty() }
        .map { AnimalAlertEvent(it) }
}
