package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Parcelize
data class AnimalMovement(
    override val id: EntityId,
    val animalId: EntityId,
    val toPremise: Premise?,
    val fromPremise: Premise?,
    val movementDate: LocalDate
) : Parcelable, HasIdentity {
    init {
        require(toPremise != null || fromPremise != null) {
            "toPremise or fromPremise must be non null"
        }
    }
    fun applyNicknames(fromPremiseNickname: String?, toPremiseNickname: String?): AnimalMovement {
        return copy(
            fromPremise = fromPremise?.copy(nickname = fromPremiseNickname),
            toPremise = toPremise?.copy(nickname = toPremiseNickname)
        )
    }
}
