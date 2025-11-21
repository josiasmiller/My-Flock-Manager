package com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.input

import android.text.InputFilter
import android.text.Spanned
import com.weyr_associates.animaltrakkerfarmmobile.app.animal.id.validation.IdFormat
import com.weyr_associates.animaltrakkerfarmmobile.app.core.text.InputFilters

object IdInputFilters {

    private class DefaultIdNumberFilter(private val maxLength: Int) : InputFilter {

        init {
            require(0 < maxLength) { "maxLength must be greater than 0." }
        }

        override fun filter(
            source: CharSequence,
            srcStart: Int,
            srcEnd: Int,
            destination: Spanned,
            dstStart: Int,
            dstEnd: Int
        ): CharSequence {
            val validChars = source.subSequence(srcStart, srcEnd).filter {
                it.isLetterOrDigit() || isSeparator(it)
            }
            if (validChars.isEmpty()) {
                return ""
            }
            val keep = maxLength - (destination.length - (dstEnd - dstStart))
            if (keep <= 0) {
                return ""
            }
            val keepChars = validChars.take(keep)
            val resultingString = combineCharSequences(
                keepChars,
                0,
                keepChars.length,
                destination,
                dstStart,
                dstEnd
            )
            var followsSeparator = false
            resultingString.forEachIndexed { index, character ->
                if (index == 0 || index == resultingString.length - 1) {
                    if (isSeparator(character)) {
                        return ""
                    }
                    followsSeparator = false
                } else {
                    if (isSeparator(character)) {
                        if (followsSeparator) {
                            return ""
                        }
                        followsSeparator = true
                    } else {
                        followsSeparator = false
                    }
                }
            }
            return keepChars
        }

        private fun isSeparator(character: Char): Boolean {
            return IdFormat.SEPARATOR_CHARS.contains(character)
        }
    }

    private val EID_INPUT_FILTER = InputFilter { source, srcStart, srcEnd, destination, dstStart, dstEnd ->
        val validChars = source.subSequence(srcStart, srcEnd).filter {
            it.isDigit() || it == IdFormat.EID_COUNTRY_CODE_ANIMAL_ID_SPLIT
        }
        if (validChars.isEmpty() || 1 < validChars.count { it == IdFormat.EID_COUNTRY_CODE_ANIMAL_ID_SPLIT }) {
            return@InputFilter ""
        }
        val keep = IdFormat.EID_NUMBER_LENGTH - (destination.length - (dstEnd - dstStart))
        if (keep <= 0) {
            return@InputFilter ""
        }
        val keepChars = validChars.take(keep)
        val resultingString = combineCharSequences(
            keepChars,
            0,
            keepChars.length,
            destination,
            dstStart,
            dstEnd
        )
        val splits = resultingString.split(IdFormat.EID_COUNTRY_CODE_ANIMAL_ID_SPLIT)
        //If we have more than one _ character the string is invalid.
        if (2 < splits.size) {
            return@InputFilter ""
        }
        //If we have only one split we have no _ and the length must be shorter than
        //max EID length.
        if (splits.size == 1 && IdFormat.EID_NUMBER_LENGTH <= splits[0].length) {
            return@InputFilter ""
        }
        //If we have two splits, we have a _ and each portion cannot be longer
        //then it's associated max length.
        if (splits.size == 2 && (IdFormat.EID_COUNTRY_CODE_LENGTH < splits[0].length ||
                IdFormat.EID_ANIMAL_ID_LENGTH < splits[1].length)) {
            return@InputFilter ""
        }
        return@InputFilter validChars
    }

    private val FEDERAL_INPUT_FILTER = InputFilter { source, srcStart, srcEnd, destination, dstStart, dstEnd ->
        val validChars = source.filter {
            it.isLetterOrDigit() || it == IdFormat.FEDERAL_SCRAPIE_SEPARATOR
        }
        if (validChars.isEmpty()) {
            return@InputFilter ""
        }
        val dstRemaining = destination.removeRange(dstStart, dstEnd)
        val numSrcSeparators = validChars.count { it == IdFormat.FEDERAL_SCRAPIE_SEPARATOR }
        val numDstSeparators = dstRemaining.count { it == IdFormat.FEDERAL_SCRAPIE_SEPARATOR }
        val numSeparators = numSrcSeparators + numDstSeparators
        return@InputFilter if (1 <= numSeparators) {
            if (numDstSeparators == 1) {
                validChars.filter { it != IdFormat.FEDERAL_SCRAPIE_SEPARATOR }
            } else if (numSrcSeparators == 1) {
                validChars
            } else {
                ""
            }
        } else {
            validChars
        }
    }

    private val FEDERAL_CANADIAN_INPUT_FILTER = InputFilter { source, srcStart, srcEnd, destination, dstStart, dstEnd ->
        null
    }

    val DEFAULT = arrayOf<InputFilter>(DefaultIdNumberFilter(IdFormat.DEFAULT_MAXIMUM_LENGTH))

    val EID = arrayOf(EID_INPUT_FILTER)
    val TRICH = arrayOf(InputFilters.DIGITS, InputFilter.LengthFilter(IdFormat.TRICH_MAXIMUM_LENGTH))
    val FEDERAL = arrayOf(FEDERAL_INPUT_FILTER)
    val FEDERAL_CANADIAN = arrayOf(FEDERAL_CANADIAN_INPUT_FILTER)

    private fun combineCharSequences(
        source: CharSequence,
        srcStart: Int,
        srcEnd: Int,
        destination: CharSequence,
        dstStart: Int,
        dstEnd: Int
    ): CharSequence {
        return buildString {
            for (i in 0 until dstStart) {
                append(destination[i])
            }
            append(source.subSequence(srcStart, srcEnd))
            for (i in dstEnd until destination.length) {
                append(destination[i])
            }
        }
    }
}
