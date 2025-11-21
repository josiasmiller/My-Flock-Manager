package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Premise(
    override val id: EntityId,
    val type: Type,
    val number: String?,
    val nickname: String?,
    val address: Address?,
    val geoLocation: GeoLocation?,
    val jurisdiction: PremiseJurisdiction?
) : Parcelable, HasIdentity {
    companion object {
        const val ID_PREMISE_UNKNOWN_RAW = "4a04fcfe-889f-4cd9-8abe-bdd59807ad07" //LEGACY ID = 82
        val ID_PREMISE_UNKNOWN = EntityId(ID_PREMISE_UNKNOWN_RAW)
    }
    @Parcelize
    data class Type(
        override val id: EntityId,
        override val name: String,
        val order: Int
    ) : Parcelable, HasIdentity, HasName {
        companion object {
            const val ID_PHYSICAL_RAW = "7f58eb72-40bc-4dde-a929-611944eb291d" //LEGACY ID = 1
            const val ID_MAILING_RAW = "25ddcb3e-10a5-4afc-b4a0-4d1fafe8a7f2" //LEGACY ID = 2
            const val ID_BOTH_RAW = "28e2b327-5ced-4f39-bc60-90450bc719c6" //LEGACY ID = 3
            const val ID_UNKNOWN_RAW = "c16e3020-f394-42f3-9b5d-4b36833551f6" //NO LEGACY ID

            val ID_PHYSICAL = EntityId(ID_PHYSICAL_RAW)
            val ID_MAILING = EntityId(ID_MAILING_RAW)
            val ID_BOTH = EntityId(ID_BOTH_RAW)
            val ID_UNKNOWN = EntityId(ID_UNKNOWN_RAW)
        }
    }
    @Parcelize
    data class Address(
        val address1: String,
        val address2: String?,
        val city: String,
        val state: String,
        val postCode: String,
        val country: String,
    ) : Parcelable {
        companion object {
            fun from(
                address1: String?,
                address2: String?,
                city: String?,
                state: String?,
                postCode: String?,
                country: String?
            ): Address? {
                return if (address1 != null &&
                    city != null &&
                    state != null &&
                    postCode != null &&
                    country != null) {
                    Address(
                        address1 = address1,
                        address2 = address2,
                        city = city,
                        state = state,
                        postCode = postCode,
                        country = country
                    )
                } else null
            }
        }
    }
    @Parcelize
    data class GeoLocation(
        val latitude: Float,
        val longitude: Float
    ) : Parcelable {
        companion object {
            fun from(latitude: Float?, longitude: Float?): GeoLocation? {
                return if (latitude != null && longitude != null) {
                    GeoLocation(latitude, longitude)
                } else null
            }
        }
    }
}
