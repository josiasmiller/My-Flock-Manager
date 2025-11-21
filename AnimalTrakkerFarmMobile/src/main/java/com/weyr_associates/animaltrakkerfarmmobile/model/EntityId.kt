package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import com.weyr_associates.animaltrakkerfarmmobile.model.serialization.UUIDSerializer
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import java.util.UUID

@JvmInline
@Parcelize
@Serializable
value class EntityId(
    @Serializable(with = UUIDSerializer::class)
    val raw: UUID
) : Comparable<EntityId>, Parcelable {

    constructor(raw: String) : this(UUID.fromString(raw))

    companion object {
        const val UNKNOWN_RAW = "00000000-0000-0000-0000-000000000000"
        val UNKNOWN = EntityId(UNKNOWN_RAW)

        fun generate(): EntityId = EntityId(UUID.randomUUID())
    }

    val isValid: Boolean get() = raw != UNKNOWN.raw

    override fun toString(): String = raw.toString()

    override fun compareTo(other: EntityId): Int {
        return raw.compareTo(other.raw)
    }
}
