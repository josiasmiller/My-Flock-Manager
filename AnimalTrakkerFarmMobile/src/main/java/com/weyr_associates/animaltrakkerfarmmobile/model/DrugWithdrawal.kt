package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import com.weyr_associates.animaltrakkerfarmmobile.model.serialization.LocalDateSerializer
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDate

class DrugWithdrawalTypeSerializer : KSerializer<DrugWithdrawal.Type> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        "DrugWithdrawalType", PrimitiveKind.STRING
    )
    override fun serialize(encoder: Encoder, value: DrugWithdrawal.Type) {
        encoder.encodeString(value.name.lowercase())
    }
    override fun deserialize(decoder: Decoder): DrugWithdrawal.Type {
        val decodedValue = decoder.decodeString().lowercase()
        return DrugWithdrawal.Type.entries.first { it.name.lowercase() ==  decodedValue }
    }
}

@Parcelize
@Serializable
data class DrugWithdrawal(
    val type: Type,
    val drugId: EntityId,
    val drugName: String,
    val drugLot: String,
    val withdrawalUnitsId: EntityId,
    @Serializable(with = LocalDateSerializer::class)
    val withdrawalDate: LocalDate,
) : Parcelable {
    @Serializable(with = DrugWithdrawalTypeSerializer::class)
    enum class Type {
        MEAT,
        MILK;
        val displayName: String get() = when (this) {
            MEAT -> "Meat"
            MILK -> "Milk"
        }
    }
}
