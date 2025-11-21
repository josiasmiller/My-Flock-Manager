package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Parcelize
data class IdInfo(
    override val id: EntityId,
    val number: String,
    val type: IdType,
    val color: IdColor,
    val location: IdLocation,
    val isOfficial: Boolean,
    val corrected: Boolean,
    val dateOn: LocalDate,
    val timeOn: LocalTime,
    val removal: IdRemoval?
) : Parcelable, HasIdentity {

    @Parcelize
    data class IdRemoval(
        val dateOff: LocalDate,
        val timeOff: LocalTime,
        val reason: IdRemoveReason
    ) : Parcelable {
        companion object {
            fun from(dateOff: LocalDate?, timeOff: LocalTime?, reason: IdRemoveReason?): IdRemoval? {
                return if (dateOff != null && timeOff != null && reason != null) {
                    IdRemoval(dateOff, timeOff, reason)
                } else {
                    null
                }
            }
        }
    }

    object Differ : ItemCallback<IdInfo>() {
        override fun areItemsTheSame(oldItem: IdInfo, newItem: IdInfo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: IdInfo, newItem: IdInfo): Boolean {
            return oldItem == newItem
        }
    }

    @IgnoredOnParcel
    val dateTimeOn: LocalDateTime by lazy {
        LocalDateTime.of(dateOn, timeOn)
    }
}

fun List<IdInfo>.mostRecentDateOnOfType(idTypeId: EntityId): IdInfo? {
    return this.filter { it.type.id == idTypeId }.maxByOrNull { it.dateTimeOn }
}

fun List<IdInfo>.oldestDateOnOfType(idTypeId: EntityId): IdInfo? {
    return this.filter { it.type.id == idTypeId }.minByOrNull { it.dateTimeOn }
}
