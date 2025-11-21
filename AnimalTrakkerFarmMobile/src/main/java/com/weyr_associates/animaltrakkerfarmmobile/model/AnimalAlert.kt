package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalTime

@Parcelize
sealed interface AnimalAlert : Parcelable, HasIdentity {
    override val id: EntityId
    val alertType: Type
    val animalId: Int
    val eventDate: LocalDate
    val eventTime: LocalTime
    enum class Type(val typeId: EntityId) {
        USER_DEFINED(EntityId("4d264192-63bc-4b81-8a1a-e5378f9c6649")),
        DRUG_WITHDRAWAL(EntityId("ab9b9fa3-fc41-4ec1-a1e7-a228fa35b042")),
        EVALUATION_SUMMARY(EntityId("386c2c81-503a-43d7-b319-b24824864776"))
    }
}

@Parcelize
data class UserDefinedAlert(
    override val id: EntityId,
    override val animalId: Int,
    override val eventDate: LocalDate,
    override val eventTime: LocalTime,
    val content: String,
) : AnimalAlert {
    override val alertType: AnimalAlert.Type
        get() = AnimalAlert.Type.USER_DEFINED
}

@Parcelize
data class DrugWithdrawalAlert(
    override val id: EntityId,
    override val animalId: Int,
    override val eventDate: LocalDate,
    override val eventTime: LocalTime,
    val drugWithdrawal: DrugWithdrawal
) : AnimalAlert {
    override val alertType: AnimalAlert.Type
        get() = AnimalAlert.Type.DRUG_WITHDRAWAL
}

@Parcelize
data class EvaluationSummaryAlert(
    override val id: EntityId,
    override val animalId: Int,
    override val eventDate: LocalDate,
    override val eventTime: LocalTime,
    val evaluationSummary: EvaluationSummary
) : AnimalAlert {
    override val alertType: AnimalAlert.Type
        get() = AnimalAlert.Type.EVALUATION_SUMMARY
}
