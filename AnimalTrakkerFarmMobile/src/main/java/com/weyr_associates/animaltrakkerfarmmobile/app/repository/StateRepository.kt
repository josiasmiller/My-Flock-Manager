package com.weyr_associates.animaltrakkerfarmmobile.app.repository

import com.weyr_associates.animaltrakkerfarmmobile.model.State

interface StateRepository {
    fun queryStates(): List<State>
    fun queryForState(id: Int): State?
}
