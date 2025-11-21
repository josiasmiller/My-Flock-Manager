package com.weyr_associates.animaltrakkerfarmmobile.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UnitOfMeasure(
    override val id: EntityId,
    override val name: String,
    override val abbreviation: String,
    val type: Type,
    val order: Int = Int.MAX_VALUE
) : Parcelable, HasIdentity, HasName, HasAbbreviation {
    companion object {

        const val SALE_PRICE_US_DOLLARS_RAW = "9b023940-1cc1-455a-996e-166700750650" //LEGACY ID = 8

        const val TIME_UNIT_DAYS_RAW = "ffabfa21-ef36-4144-b5c6-58c8e369b1a7" //LEGACY ID = 5
        const val TIME_UNIT_HOURS_RAW = "d2f3e19d-bae7-476c-92f3-e354eca0c209" //LEGACY ID = 6
        const val TIME_UNIT_YEARS_RAW = "7f717de7-7ed1-4562-b1bc-9f82be9d77a5" //LEGACY ID = 21
        const val TIME_UNIT_MONTHS_RAW = "f747af3b-b3be-43fc-8591-193df3bd77f9" //LEGACY ID = 22
        const val TIME_UNIT_WEEKS_RAW = "ef56d7b8-1b54-4d03-9ae7-5c44719c5358" //LEGACY ID = 23
        const val TIME_UNIT_SECONDS_RAW = "c5f2c6cb-f313-4861-ac6e-8d606dcf5d9e" //LEGACY ID = 24

        val SALE_PRICE_US_DOLLARS = EntityId(SALE_PRICE_US_DOLLARS_RAW)

        val TIME_UNIT_DAYS = EntityId(TIME_UNIT_DAYS_RAW)
        val TIME_UNIT_HOURS = EntityId(TIME_UNIT_HOURS_RAW)
        val TIME_UNIT_YEARS = EntityId(TIME_UNIT_YEARS_RAW)
        val TIME_UNIT_MONTHS = EntityId(TIME_UNIT_MONTHS_RAW)
        val TIME_UNIT_WEEKS = EntityId(TIME_UNIT_WEEKS_RAW)
        val TIME_UNIT_SECONDS = EntityId(TIME_UNIT_SECONDS_RAW)

        val NONE = UnitOfMeasure(
            id = EntityId.UNKNOWN,
            name = "None",
            abbreviation = "None",
            type = Type.NONE,
            order = Int.MAX_VALUE
        )

        fun from(
            id: EntityId?,
            name: String?,
            abbreviation: String?,
            type: Type?
        ): UnitOfMeasure? {
            return if (
                id == null ||
                name == null ||
                abbreviation == null ||
                type == null
            ) {
                null
            } else {
                UnitOfMeasure(
                    id = id,
                    name = name,
                    abbreviation = abbreviation,
                    type = type
                )
            }
        }
    }

    @Parcelize
    data class Type(
        val id: EntityId,
        val name: String,
        val order: Int = Int.MAX_VALUE
    ) : Parcelable {

        companion object {
            val NONE = Type(
                id = EntityId.UNKNOWN,
                name = "None",
                order = Int.MAX_VALUE
            )

            const val ID_WEIGHT_RAW = "26a0a6c1-01a9-42f4-9601-53e9e03bde71" //LEGACY ID = 1
            const val ID_CURRENCY_RAW = "aca60c83-db19-4cfb-86ed-c458a9413878" //LEGACY ID = 3
            const val ID_TIME_RAW = "2e55da17-1bd4-4178-a83b-5c1f2034d59e" //LEGACY ID = 4

            val ID_WEIGHT = EntityId(ID_WEIGHT_RAW)
            val ID_CURRENCY = EntityId(ID_CURRENCY_RAW)
            val ID_TIME = EntityId(ID_TIME_RAW)

            fun from(
                id: EntityId?,
                name: String?
            ): Type? {
                return if (
                    id == null ||
                    name == null
                ) {
                    null
                } else {
                    Type(id, name)
                }
            }
        }

        val isWeight: Boolean
            get() = when (id) {
                ID_WEIGHT -> true
                else -> false
            }
    }
}
