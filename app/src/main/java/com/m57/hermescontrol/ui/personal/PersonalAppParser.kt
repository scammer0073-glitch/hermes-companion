package com.m57.hermescontrol.ui.personal

import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.util.regex.Pattern

sealed interface ParsedEntry {
    data class Food(val text: String, val calories: Int? = null) : ParsedEntry

    data class Sleep(val bed: String, val wake: String) : ParsedEntry
}

object PersonalAppParser {
    data class SleepInterval(val bedMillis: Long, val wakeMillis: Long)

    private val sleepKeyword = Regex("\\b(slept|sleep)\\b", RegexOption.IGNORE_CASE)
    private val sleepPat =
        Pattern.compile(
            "\\b(slept|sleep)\\b[^0-9]*([0-9]{1,2}[:.][0-9]{2})(?![0-9]|\\s*[ap]m\\b)" +
                "[^0-9]*([0-9]{1,2}[:.][0-9]{2})(?![0-9]|\\s*[ap]m\\b)",
            Pattern.CASE_INSENSITIVE,
        )
    private val foodPat = Pattern.compile("(ate|had|eaten)[^\n]+", Pattern.CASE_INSENSITIVE)

    fun parse(text: String): List<ParsedEntry> {
        val out = mutableListOf<ParsedEntry>()
        val sm = sleepPat.matcher(text)
        while (sm.find()) out += ParsedEntry.Sleep(sm.group(2)!!, sm.group(3)!!)
        if (sleepKeyword.findAll(text).count() > out.size) return emptyList()
        val fm = foodPat.matcher(text)
        while (fm.find()) out += ParsedEntry.Food(fm.group().trim(), estimateCalories(fm.group()))
        if (out.isEmpty() && text.isNotBlank() && !sleepKeyword.containsMatchIn(text)) {
            out += ParsedEntry.Food(text.trim())
        }
        return out
    }

    // Time-only notes refer to the most recent completed interval, in the user's local zone.
    fun resolveSleep(
        entry: ParsedEntry.Sleep,
        nowMillis: Long,
        zone: ZoneId = ZoneId.systemDefault(),
    ): SleepInterval? {
        fun time(value: String): LocalTime? {
            if (!Regex("[0-9]{1,2}[:.][0-9]{2}").matches(value)) return null
            val parts = value.split(':', '.')
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()
            if (hour !in 0..23 || minute !in 0..59) return null
            return LocalTime.of(hour, minute)
        }
        val bed = time(entry.bed) ?: return null
        val wake = time(entry.wake) ?: return null
        if (bed == wake) return null
        val now = Instant.ofEpochMilli(nowMillis).atZone(zone)
        var wakeDate = now.toLocalDate()
        if (wakeDate.atTime(wake).atZone(zone).toInstant().toEpochMilli() > nowMillis) {
            wakeDate = wakeDate.minusDays(1)
        }
        val bedDate = if (bed > wake) wakeDate.minusDays(1) else wakeDate
        val interval =
            SleepInterval(
                bedDate.atTime(bed).atZone(zone).toInstant().toEpochMilli(),
                wakeDate.atTime(wake).atZone(zone).toInstant().toEpochMilli(),
            )
        return interval.takeIf { it.wakeMillis > it.bedMillis }
    }

    // Attribute a completed sleep interval to its wake date; missing calendar days remain zero.
    fun sleepHoursLastSevenDays(
        entries: List<SleepInterval>,
        nowMillis: Long,
        zone: ZoneId = ZoneId.systemDefault(),
    ): List<Float> {
        val today = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
        val totals =
            entries.filter { it.wakeMillis > it.bedMillis && it.wakeMillis <= nowMillis }
                .groupBy { Instant.ofEpochMilli(it.wakeMillis).atZone(zone).toLocalDate() }
                .mapValues { (_, sleeps) -> sleeps.sumOf { it.wakeMillis - it.bedMillis } / 3600000f }
        return (6L downTo 0L).map { totals[today.minusDays(it)] ?: 0f }
    }

    private fun estimateCalories(s: String): Int? {
        val l = s.lowercase()
        return when {
            "egg" in l -> 70
            "paratha" in l -> 250
            "rice" in l -> 200
            "curd" in l -> 100
            else -> null
        }
    }

    suspend fun parseWithLLM(
        text: String,
        callLLM: suspend (String) -> String,
    ): List<ParsedEntry> {
        return try {
            val j =
                callLLM(
                    "Extract food/sleep from: \"" +
                        text.replace(
                            "\"",
                            "'",
                        ) + "\" JSON {type, text, bed, wake, calories}",
                )
            parse(text)
        } catch (
            _: Exception,
        ) {
            parse(text)
        }
    }
}
