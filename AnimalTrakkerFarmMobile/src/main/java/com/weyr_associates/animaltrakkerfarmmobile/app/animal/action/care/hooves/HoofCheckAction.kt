package com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.care.hooves

import android.os.Parcelable
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.action.AnimalAction
import com.weyr_associates.animaltrakkerfarmmobile.model.HoofCheck
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class HoofCheckAction(
    val hoofCheck: HoofCheck? = null,
    override val actionId: UUID = UUID.randomUUID()
) : AnimalAction, Parcelable {
    override val isComplete: Boolean get() = hoofCheck != null
}
