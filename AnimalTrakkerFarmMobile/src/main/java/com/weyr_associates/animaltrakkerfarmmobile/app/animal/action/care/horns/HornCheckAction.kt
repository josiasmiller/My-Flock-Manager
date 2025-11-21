package com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.care.horns

import android.os.Parcelable
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.AnimalAction
import com.weyr_associates.animaltrakkerfarmmobile.model.HornCheck
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class HornCheckAction(
    val hornCheck: HornCheck? = null,
    override val actionId: UUID = UUID.randomUUID(),
) : AnimalAction, Parcelable {
    override val isComplete: Boolean get() = hornCheck != null
}
