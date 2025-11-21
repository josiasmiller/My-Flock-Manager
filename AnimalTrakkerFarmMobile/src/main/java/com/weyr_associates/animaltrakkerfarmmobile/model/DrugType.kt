package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DrugType(
    override val id: EntityId,
    override val name: String,
    val order: Int
) : Parcelable, HasIdentity, HasName {
    companion object {

        const val ID_DEWORMER_RAW = "6976a1e8-2bff-4e36-9a5e-0e3ee58d5dea" //LEGACY ID = 1
        const val ID_VACCINE_RAW = "5a249b9b-dad9-4836-aa7e-3629e4e74d2d" //LEGACY ID = 2
        const val ID_ANTIBIOTIC_RAW = "cd2c3b02-1335-4449-9563-bd76b7f0a915" //LEGACY ID = 3
        const val ID_HORMONE_RAW = "bf7eb835-99ff-43a7-b63e-4b089f034b11" //LEGACY ID = 4
        const val ID_COCCIDIOSTAT_RAW = "d37f46c5-4a9d-492a-b5eb-eb2f2f2bb4b6" //LEGACY ID = 5
        const val ID_FEED_SUPPLEMENT_RAW = "b280065e-fdde-4973-80dd-efe282e260a3" //LEGACY ID = 6
        const val ID_ANALGESIC_RAW = "a735484d-d54d-45cf-aba6-4467b0484cad" //LEGACY ID = 7

        val ID_DEWORMER = EntityId(ID_DEWORMER_RAW)
        val ID_VACCINE = EntityId(ID_VACCINE_RAW)
        val ID_ANTIBIOTIC = EntityId(ID_ANTIBIOTIC_RAW)
        val ID_HORMONE = EntityId(ID_HORMONE_RAW)
        val ID_COCCIDIOSTAT = EntityId(ID_COCCIDIOSTAT_RAW)
        val ID_FEED_SUPPLEMENT = EntityId(ID_FEED_SUPPLEMENT_RAW)
        val ID_ANALGESIC = EntityId(ID_ANALGESIC_RAW)
    }
}
