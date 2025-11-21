package com.weyr_associates.animaltrakkerfarmmobile.app.animal

import android.content.Context
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.model.DrugType
import com.weyr_associates.animaltrakkerfarmmobile.model.EntityId

object DrugTypePresentation {
    fun nameForType(context: Context, drugTypeId: EntityId, default: Int = R.string.text_drug): String {
        return context.getString(
            when (drugTypeId) {
                DrugType.ID_DEWORMER -> R.string.text_drug_type_dewormer
                DrugType.ID_VACCINE -> R.string.text_drug_type_vaccine
                DrugType.ID_ANTIBIOTIC -> R.string.text_drug_type_antibiotic
                DrugType.ID_HORMONE -> R.string.text_drug_type_hormone
                DrugType.ID_COCCIDIOSTAT -> R.string.text_drug_type_coccidiostat
                DrugType.ID_FEED_SUPPLEMENT -> R.string.text_drug_type_feed_supplement
                DrugType.ID_ANALGESIC -> R.string.text_drug_type_analgesic
                else -> default
            }
        )
    }
}
