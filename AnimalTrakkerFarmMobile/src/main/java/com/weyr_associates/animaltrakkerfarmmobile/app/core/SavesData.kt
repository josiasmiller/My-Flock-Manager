package com.weyr_associates.animaltrakkerfarmmobile.app.core

import kotlinx.coroutines.flow.StateFlow

interface SavesData {
    val canSaveData: StateFlow<Boolean>
    fun saveData()
}
