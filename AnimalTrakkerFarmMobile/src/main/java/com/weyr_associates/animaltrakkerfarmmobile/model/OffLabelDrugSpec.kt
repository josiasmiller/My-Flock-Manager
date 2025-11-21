package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Parcelize
data class OffLabelDrugSpec(
    val id: EntityId = EntityId.UNKNOWN,
    val veterinarianContactId: EntityId,
    val speciesId: EntityId,
    val drugDosage: String,
    val useStartDate: LocalDate,
    val useEndDate: LocalDate?,
    val note: String?
) : Parcelable {
    companion object {
        fun create(
            veterinarianContactId: EntityId?,
            speciesId: EntityId?,
            drugDosage: String?,
            useStartDate: LocalDate?,
            useEndDate: LocalDate?,
            note: String?
        ): OffLabelDrugSpec? {
            return if (veterinarianContactId != null &&
                speciesId != null &&
                drugDosage != null &&
                useStartDate != null) {
                OffLabelDrugSpec(
                    veterinarianContactId = veterinarianContactId,
                    speciesId = speciesId,
                    drugDosage = drugDosage,
                    useStartDate = useStartDate,
                    useEndDate = useEndDate,
                    note = note
                )
            } else null
        }
    }
}
