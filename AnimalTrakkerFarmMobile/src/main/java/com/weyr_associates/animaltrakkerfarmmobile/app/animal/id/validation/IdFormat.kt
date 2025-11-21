package com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.validation

object IdFormat {

    const val DEFAULT_MAXIMUM_LENGTH = 30

    const val EID_NUMBER_LENGTH = 16
    const val EID_COUNTRY_CODE_ANIMAL_ID_SPLIT = '_'
    const val EID_COUNTRY_CODE_LENGTH = 3
    const val EID_ANIMAL_ID_LENGTH = 12

    const val TRICH_MINIMUM_LENGTH = 1
    const val TRICH_MAXIMUM_LENGTH = 5

    val SEPARATOR_CHARS = setOf('_', '-', ' ')
    const val FEDERAL_SCRAPIE_SEPARATOR = '-'

    fun isEIDFormat(idNumber: String): Boolean {
        if (idNumber.length != EID_NUMBER_LENGTH) {
            return false
        }

        val split = idNumber.split(EID_COUNTRY_CODE_ANIMAL_ID_SPLIT)

        if (split.size != 2) {
            return false
        }
        if (split[0].length != EID_COUNTRY_CODE_LENGTH ||
            split[1].length != EID_ANIMAL_ID_LENGTH
        ) {
            return false
        }
        if (!split.all { section -> section.all { character -> character.isDigit() } }) {
            return false
        }
        return true
    }

    fun extractEIDCountryCode(idNumber: String): String? {
        if (!isEIDFormat(idNumber)) { return null }
        return idNumber.substring(0, EID_COUNTRY_CODE_LENGTH)
    }

    fun isTrichIdFormat(idNumber: String): Boolean {
        return idNumber.length in TRICH_MINIMUM_LENGTH..TRICH_MAXIMUM_LENGTH &&
                idNumber.all { it.isDigit() }
    }

    fun isFarmIdFormat(idNumber: String): Boolean {
        return idNumber.all { it.isLetterOrDigit() || SEPARATOR_CHARS.contains(it) }
    }

    fun isFederalScrapieIdFormat(idNumber: String): Boolean {
        return idNumber.all {
            it.isLetterOrDigit() || it == FEDERAL_SCRAPIE_SEPARATOR
        } && idNumber.count { it == FEDERAL_SCRAPIE_SEPARATOR } <= 1
                && idNumber.indexOf(FEDERAL_SCRAPIE_SEPARATOR) !in
                    intArrayOf(0, idNumber.length - 1)
    }

    fun isFreezeBrandIdFormat(idNumber: String): Boolean {
        return idNumber.all { it.isLetterOrDigit() }
    }
}
