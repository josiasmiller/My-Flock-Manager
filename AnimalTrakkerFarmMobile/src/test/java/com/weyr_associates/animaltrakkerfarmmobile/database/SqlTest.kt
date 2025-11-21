package com.weyr_associates.animaltrakkerfarmmobile.database

import com.weyr_associates.animaltrakkerfarmmobile.database.core.Sql
import org.junit.Test
import java.time.format.DateTimeParseException

class SqlTest {

    @Test(expected = DateTimeParseException::class)
    fun dateFormat_rejectsDatesWithMissingDays() {
        Sql.FORMAT_DATE.parse("2024-06")
    }

    @Test(expected = DateTimeParseException::class)
    fun dateFormat_rejectsDatesWithMissingDaysAndMonth() {
        Sql.FORMAT_DATE.parse("2024")
    }

    @Test(expected = DateTimeParseException::class)
    fun dateFormat_rejectsDatesWithTimes() {
        Sql.FORMAT_DATE.parse("2024-06-11 10:10:10")
    }

    @Test(expected = DateTimeParseException::class)
    fun dateFormat_rejectsDatesWithLeadingWhiteSpace() {
        Sql.FORMAT_DATE.parse(" 2024-06-11")
    }

    @Test(expected = DateTimeParseException::class)
    fun dateFormat_rejectsDatesWithTrailingWhiteSpace() {
        Sql.FORMAT_DATE.parse("2024-06-11 ")
    }

    @Test(expected = DateTimeParseException::class)
    fun dateFormat_rejectsDatesWithNonDashSeparators() {
        Sql.FORMAT_DATE.parse("2024/06/11")
    }

    @Test(expected = DateTimeParseException::class)
    fun dateFormat_rejectsDatesWithInvalidYear() {
        Sql.FORMAT_DATE.parse("10000-01-01")
    }

    @Test(expected = DateTimeParseException::class)
    fun dateFormat_rejectsDatesWithInvalidMonths() {
        Sql.FORMAT_DATE.parse("2024-13-11")
    }

    @Test(expected = DateTimeParseException::class)
    fun dateFormat_rejectsDatesWithInvalidDays() {
        Sql.FORMAT_DATE.parse("2024-06-32")
    }

    @Test
    fun dateFormat_acceptsAnimalTrakkerPlaceholderDate() {
        Sql.FORMAT_DATE.parse("0000-01-01")
    }

    @Test
    fun dateFormat_acceptsProperlyFormattedDate() {
        Sql.FORMAT_DATE.parse("2024-06-11")
    }

    @Test(expected = DateTimeParseException::class)
    fun timeFormat_rejectsTimesWithoutSeconds() {
        Sql.FORMAT_TIME.parse("10:10")
    }

    @Test(expected = DateTimeParseException::class)
    fun timeFormat_rejectsTimesWithoutMinutesAndSeconds() {
        Sql.FORMAT_TIME.parse("10")
    }

    @Test(expected = DateTimeParseException::class)
    fun timeFormat_rejectsTimesWithDates() {
        Sql.FORMAT_TIME.parse("2024-06-11 10:10:10")
    }

    @Test(expected = DateTimeParseException::class)
    fun timeFormat_rejectsTimesWithLeadingSpaces() {
        Sql.FORMAT_TIME.parse(" 10:10:10")
    }

    @Test(expected = DateTimeParseException::class)
    fun timeFormat_rejectsTimesWithTrailingSpaces() {
        Sql.FORMAT_TIME.parse("10:10:10 ")
    }

    @Test(expected = DateTimeParseException::class)
    fun timeFormat_rejectsTimesWithNonColonSeparators() {
        Sql.FORMAT_TIME.parse("10-10-10")
    }

    @Test(expected = DateTimeParseException::class)
    fun timeFormat_rejectsTimesWithInvalidHours() {
        Sql.FORMAT_TIME.parse("25:10:10")
    }

    @Test(expected = DateTimeParseException::class)
    fun timeFormat_rejectsTimesWithInvalidMinutes() {
        Sql.FORMAT_TIME.parse("10:61:10")
    }

    @Test(expected = DateTimeParseException::class)
    fun timeFormat_rejectsTimesWithInvalidSeconds() {
        Sql.FORMAT_TIME.parse("10:10:61")
    }

    @Test
    fun timeFormat_acceptsAnimalTrakkerDefaultTime() {
        Sql.FORMAT_TIME.parse("00:00:00")
    }

    @Test
    fun timeFormat_acceptsProperlyFormattedTime() {
        Sql.FORMAT_TIME.parse("10:10:10")
    }

    @Test(expected = DateTimeParseException::class)
    fun dateTimeFormat_rejectsDateTimesWithoutSeconds() {
        Sql.FORMAT_DATETIME.parse("2024-06-11 10:10")
    }

    @Test(expected = DateTimeParseException::class)
    fun dateTimeFormat_rejectsDateTimesWithoutMinutesAndSeconds() {
        Sql.FORMAT_DATETIME.parse("2024-06-11 10")
    }

    @Test(expected = DateTimeParseException::class)
    fun dateTimeFormat_rejectsDateTimesWithoutTime() {
        Sql.FORMAT_DATETIME.parse("2024-06-11")
    }

    @Test(expected = DateTimeParseException::class)
    fun dateTimeFormat_rejectsDateTimesWithoutDays() {
        Sql.FORMAT_DATETIME.parse("2024-06 10:10:10")
    }

    @Test(expected = DateTimeParseException::class)
    fun dateTimeFormat_rejectsDateTimesWithoutMonthsAndDays() {
        Sql.FORMAT_DATETIME.parse("2024 10:10:10")
    }

    @Test(expected = DateTimeParseException::class)
    fun dateTimeFormat_rejectsDateTimesWithLeadingSpace() {
        Sql.FORMAT_DATETIME.parse(" 2024-06-11 10:10:10")
    }

    @Test(expected = DateTimeParseException::class)
    fun dateTimeFormat_rejectsDateTimesWithTrailingSpace() {
        Sql.FORMAT_DATETIME.parse("2024-06-11 10:10:10 ")
    }

    @Test(expected = DateTimeParseException::class)
    fun dateTimeFormat_rejectsDateTimesWithNonColonTimeSeparators() {
        Sql.FORMAT_DATETIME.parse("2024-06-11 10.10.10")
    }

    @Test(expected = DateTimeParseException::class)
    fun dateTimeFormat_rejectsDateTimesWithNonDashDateSeparators() {
        Sql.FORMAT_DATETIME.parse("2024_06_11 10:10:10")
    }

    @Test(expected = DateTimeParseException::class)
    fun dateTimeFormat_rejectsDateTimesWithNonSpaceDateTimeSeparator() {
        Sql.FORMAT_DATETIME.parse("2024-06-11T10:10:10")
    }

    @Test(expected = DateTimeParseException::class)
    fun dateTimeFormat_rejectsDateTimesWithInvalidYears() {
        Sql.FORMAT_DATETIME.parse("10000-06-11 10:10:10")
    }

    @Test(expected = DateTimeParseException::class)
    fun dateTimeFormat_rejectsDateTimesWithInvalidMonths() {
        Sql.FORMAT_DATETIME.parse("2024-13-11 10:10:10")
    }

    @Test(expected = DateTimeParseException::class)
    fun dateTimeFormat_rejectsDateTimesWithInvalidDays() {
        Sql.FORMAT_DATETIME.parse("2024-06-32 10:10:10")
    }

    @Test(expected = DateTimeParseException::class)
    fun dateTimeFormat_rejectsDateTimesWithInvalidHours() {
        Sql.FORMAT_DATETIME.parse("2024-06-11 25:10:10")
    }

    @Test(expected = DateTimeParseException::class)
    fun dateTimeFormat_rejectsDateTimesWithInvalidMinutes() {
        Sql.FORMAT_DATETIME.parse("2024-06-11 10:61:10")
    }

    @Test(expected = DateTimeParseException::class)
    fun dateTimeFormat_rejectsDateTimesWithInvalidSeconds() {
        Sql.FORMAT_DATETIME.parse("2024-06-11 10:10:61")
    }

    @Test
    fun dateTimeFormat_acceptsAnimalTrakkerPlaceholderDateTime() {
        Sql.FORMAT_DATETIME.parse("0000-01-01 00:00:00")
    }

    @Test
    fun dateTimeFormat_acceptsProperlyFormattedDateTime() {
        Sql.FORMAT_DATETIME.parse("2024-06-11 10:10:10")
    }
}
