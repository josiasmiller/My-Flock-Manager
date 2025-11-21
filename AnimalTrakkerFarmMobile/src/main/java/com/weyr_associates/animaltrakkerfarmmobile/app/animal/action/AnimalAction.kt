package com.weyr_associates.animaltrakkerfarmmobile.app.animal.action

import java.util.UUID

interface AnimalAction {
    val actionId: UUID
    val isComplete: Boolean
}
