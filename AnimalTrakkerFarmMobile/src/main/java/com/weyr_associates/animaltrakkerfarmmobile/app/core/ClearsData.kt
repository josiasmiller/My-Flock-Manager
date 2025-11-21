package com.weyr_associates.animaltrakkerfarmmobile.app.core

import kotlinx.coroutines.flow.StateFlow

interface ClearsData {
    val canClearData: StateFlow<Boolean>
    fun clearData()
}
