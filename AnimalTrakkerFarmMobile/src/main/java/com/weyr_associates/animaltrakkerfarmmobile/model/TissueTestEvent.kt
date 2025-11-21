package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalTime

@Parcelize
data class TissueTestEvent(
    override val id: EntityId,
    val animalId: Int,
    val tissueTestTypeId: Int,
    val tissueTestName: String,
    val labCompanyName: String,
    val labAscensionId: String?,
    val eventDate: LocalDate,
    val eventTime: LocalTime?,
    val tissueTestResults: String?,
    val tissueTestResultsDate: LocalDate?
) : Parcelable, HasIdentity
