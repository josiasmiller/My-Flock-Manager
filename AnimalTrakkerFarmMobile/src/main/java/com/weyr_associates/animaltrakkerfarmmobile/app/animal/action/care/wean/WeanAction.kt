package com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.care.wean

import android.os.Parcelable
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.AnimalAction
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class WeanAction(
    override val actionId: UUID = UUID.randomUUID(),
    override val isComplete: Boolean = false,
    val isActionable: Boolean = false
) : AnimalAction, Parcelable
