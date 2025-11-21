package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DrugWithdrawalSpec(
    val withdrawal: Int,
    val userWithdrawal: Int,
    val withdrawalUnitsId: EntityId
) : Parcelable {
    companion object {
        fun create(withdrawal: Int?, userWithdrawal: Int?, withdrawalUnitsId: EntityId?): DrugWithdrawalSpec? {
            return if (withdrawal != null && userWithdrawal != null && withdrawalUnitsId != null) {
                DrugWithdrawalSpec(
                    withdrawal = withdrawal,
                    userWithdrawal = userWithdrawal,
                    withdrawalUnitsId = withdrawalUnitsId
                )
            } else null
        }
    }
}
