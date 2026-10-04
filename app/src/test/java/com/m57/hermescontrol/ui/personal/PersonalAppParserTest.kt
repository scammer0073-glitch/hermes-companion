package com.m57.hermescontrol.ui.personal

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class PersonalAppParserTest {
    private val zone = ZoneId.of("Asia/Kolkata")
    private val now = Instant.parse("2026-10-03T04:30:00Z").toEpochMilli() // 10:00 local

    @Test
    fun overnightSleepPreservesActualTimesAndDuration() {
        val sleep = PersonalAppParser.resolveSleep(ParsedEntry.Sleep("23:30", "06:45"), now, zone)!!
        assertEquals(Instant.parse("2026-10-02T18:00:00Z").toEpochMilli(), sleep.bedMillis)
        assertEquals(Instant.parse("2026-10-03T01:15:00Z").toEpochMilli(), sleep.wakeMillis)
        assertEquals(7.25f, (sleep.wakeMillis - sleep.bedMillis) / 3600000f, 0f)
    }

    @Test
    fun daytimeNapAndDotSeparatedTimesAreSupported() {
        val sleep = PersonalAppParser.resolveSleep(ParsedEntry.Sleep("8.00", "09.30"), now, zone)!!
        assertEquals(1.5f, (sleep.wakeMillis - sleep.bedMillis) / 3600000f, 0f)
        assertEquals(Instant.parse("2026-10-03T02:30:00Z").toEpochMilli(), sleep.bedMillis)
    }

    @Test
    fun futureWakeTimeUsesPreviousCompletedInterval() {
        val sleep = PersonalAppParser.resolveSleep(ParsedEntry.Sleep("13:00", "14:00"), now, zone)!!
        assertEquals(Instant.parse("2026-10-02T08:30:00Z").toEpochMilli(), sleep.wakeMillis)
    }

    @Test
    fun invalidIncompleteAndEqualTimesAreRejected() {
        listOf("24:00", "23:60", "-1:00", "bad", "11:3").forEach {
            assertNull(PersonalAppParser.resolveSleep(ParsedEntry.Sleep(it, "06:45"), now, zone))
        }
        assertNull(PersonalAppParser.resolveSleep(ParsedEntry.Sleep("06:45", "06:45"), now, zone))
        assertNull(PersonalAppParser.resolveSleep(ParsedEntry.Sleep("23:30", "06:60"), now, zone))
        assertTrue(PersonalAppParser.parse("slept 23:30").isEmpty())
        assertTrue(PersonalAppParser.parse("sleep badly").isEmpty())
        assertTrue(PersonalAppParser.parse("slept 23:300-06:45").isEmpty())
        assertTrue(PersonalAppParser.parse("slept 11:30pm-06:45am").isEmpty())
        assertTrue(PersonalAppParser.parse("ate eggs, slept 23:30").isEmpty())
    }

    @Test
    fun sevenCalendarDaysAggregateNapsAndIgnoreOldOrFutureRecords() {
        val first = PersonalAppParser.resolveSleep(ParsedEntry.Sleep("23:30", "06:45"), now, zone)!!
        val nap = PersonalAppParser.resolveSleep(ParsedEntry.Sleep("08:00", "09:00"), now, zone)!!
        val old = PersonalAppParser.SleepInterval(first.bedMillis - 7 * DAY, first.wakeMillis - 7 * DAY)
        val yesterday = PersonalAppParser.SleepInterval(nap.bedMillis - DAY, nap.wakeMillis - DAY)
        val future = PersonalAppParser.SleepInterval(nap.bedMillis + DAY, nap.wakeMillis + DAY)
        val hours = PersonalAppParser.sleepHoursLastSevenDays(listOf(first, nap, old, yesterday, future), now, zone)
        assertEquals(listOf(0f, 0f, 0f, 0f, 0f, 1f, 8.25f), hours)
        assertEquals(List(7) { 0f }, PersonalAppParser.sleepHoursLastSevenDays(emptyList(), now, zone))
    }

    companion object {
        private const val DAY = 24 * 3600000L
    }
}
