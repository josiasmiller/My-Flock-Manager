package com.weyr_associates.animaltrakkerfarmmobile.app.animal.alert

import kotlinx.coroutines.flow.Flow

interface EmitsAnimalAlerts {
    val animalAlertsEvent: Flow<AnimalAlertEvent>
}
