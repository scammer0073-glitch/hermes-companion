package com.m57.hermescontrol.ui.personal

import java.util.regex.Pattern

sealed interface ParsedEntry {
    data class Food(val text: String, val calories: Int? = null): ParsedEntry
    data class Sleep(val bed: String, val wake: String): ParsedEntry
}

object PersonalAppParser {
    private val sleepPat = Pattern.compile("(slept|sleep)[^0-9]*([0-9]{1,2}[:.][0-9]{2})[^0-9]*([0-9]{1,2}[:.][0-9]{2})", Pattern.CASE_INSENSITIVE)
    private val foodPat = Pattern.compile("(ate|had|eaten)[^\n]+", Pattern.CASE_INSENSITIVE)
    fun parse(text: String): List<ParsedEntry> {
        val out = mutableListOf<ParsedEntry>()
        val sm = sleepPat.matcher(text)
        while (sm.find()) out += ParsedEntry.Sleep(sm.group(2)!!, sm.group(3)!!)
        val fm = foodPat.matcher(text)
        while (fm.find()) out += ParsedEntry.Food(fm.group().trim(), estimateCalories(fm.group()))
        if (out.isEmpty() && text.isNotBlank()) out += ParsedEntry.Food(text.trim())
        return out
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
    suspend fun parseWithLLM(text: String, callLLM: suspend (String)->String): List<ParsedEntry> {
        return try { val j = callLLM("Extract food/sleep from: \"" + text.replace("\"","'") + "\" JSON {type, text, bed, wake, calories}"); parse(text) } catch (_: Exception) { parse(text) }
    }
}
