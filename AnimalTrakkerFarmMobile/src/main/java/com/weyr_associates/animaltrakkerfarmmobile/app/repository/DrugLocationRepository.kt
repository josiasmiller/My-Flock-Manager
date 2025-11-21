package com.weyr_associates.animaltrakkerfarmmobile.app.repository

import com.weyr_associates.animaltrakkerfarmmobile.model.DrugLocation

interface DrugLocationRepository {
    suspend fun queryAllDrugLocations(): List<DrugLocation>
}
