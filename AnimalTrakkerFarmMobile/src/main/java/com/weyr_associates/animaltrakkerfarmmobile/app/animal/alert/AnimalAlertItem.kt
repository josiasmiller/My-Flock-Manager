package com.weyr_associates.animaltrakkerfarmmobile.app.animal.alert

import com.weyr_associates.animaltrakkerfarmmobile.app.animal.evaluation.EvaluationAlerts
import com.weyr_associates.animaltrakkerfarmmobile.app.model.formatForDisplay
import com.weyr_associates.animaltrakkerfarmmobile.model.AnimalAlert
import com.weyr_associates.animaltrakkerfarmmobile.model.DrugWithdrawal
import com.weyr_associates.animaltrakkerfarmmobile.model.DrugWithdrawalAlert
import com.weyr_associates.animaltrakkerfarmmobile.model.EvaluationSummary
import com.weyr_associates.animaltrakkerfarmmobile.model.EvaluationSummaryAlert
import com.weyr_associates.animaltrakkerfarmmobile.model.HasIdentity
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId
import com.weyr_associates.animaltrakkerfarmmobile.model.UserDefinedAlert

data class AnimalAlertItem(private val animalAlert: AnimalAlert) : HasIdentity {

    override val id: EntityId
        get() = animalAlert.id

    val header: String by lazy {
        "${animalAlert.eventDate.formatForDisplay()} ${animalAlert.eventTime.formatForDisplay()}"
    }

    val content: String by lazy {
        when (animalAlert) {
            is UserDefinedAlert -> animalAlert.content
            is DrugWithdrawalAlert -> animalAlert.drugWithdrawal.contentForAlert()
            is EvaluationSummaryAlert -> animalAlert.evaluationSummary.contentForAlert()
        }
    }

    private fun DrugWithdrawal.contentForAlert(): String {
        val drugLotName = drugLot.takeIf { it.isNotBlank() } ?: "???"
        return "${type.displayName} withdrawal until ${withdrawalDate.formatForDisplay()} for $drugName Lot $drugLotName"
    }

    private fun EvaluationSummary.contentForAlert(): String {
        return EvaluationAlerts.alertForEvaluationSummary(this)
    }
}
