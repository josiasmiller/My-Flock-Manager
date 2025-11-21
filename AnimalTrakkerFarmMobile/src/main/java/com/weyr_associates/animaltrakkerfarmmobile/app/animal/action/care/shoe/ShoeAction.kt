package com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.care.shoe

import android.os.Parcelable
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.AnimalAction
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class ShoeAction(
    override val actionId: UUID = UUID.randomUUID(),
    override val isComplete: Boolean = false
) : AnimalAction, Parcelable
