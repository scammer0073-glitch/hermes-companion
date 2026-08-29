package com.m57.hermescontrol.data.local

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class FoodEntry(val id: String, val text: String, val timeMillis: Long, val calories: Int? = null)

@Serializable
data class SleepEntry(val id: String, val bedMillis: Long, val wakeMillis: Long)

@Serializable
data class PersonalApp(val id: String, val name: String, val icon: String = "\u2764", val createdAt: Long, val food: List<FoodEntry> = emptyList(), val sleep: List<SleepEntry> = emptyList())

object PersonalAppStore {
    private const val PREF = "personal_apps"
    private const val KEY = "personal_apps_json"
    private val json = Json { ignoreUnknownKeys = true }
    private val _flow = MutableStateFlow<List<PersonalApp>>(emptyList())
    private var inited = false

    private fun prefs(ctx: Context) = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)

    private fun load(ctx: Context): List<PersonalApp> {
        val s = prefs(ctx).getString(KEY, null) ?: return emptyList()
        return runCatching { json.decodeFromString<List<PersonalApp>>(s) }.getOrElse { emptyList() }
    }

    private fun saveSync(ctx: Context, apps: List<PersonalApp>) {
        prefs(ctx).edit().putString(KEY, json.encodeToString(apps)).apply()
        _flow.value = apps
    }

    fun flow(ctx: Context): Flow<List<PersonalApp>> {
        if (!inited) {
            _flow.value = load(ctx)
            inited = true
        }
        return _flow
    }

    suspend fun save(ctx: Context, apps: List<PersonalApp>) { saveSync(ctx, apps) }

    suspend fun addApp(ctx: Context, app: PersonalApp) {
        val cur = _flow.value.ifEmpty { load(ctx) }
        saveSync(ctx, cur + app)
    }

    suspend fun addFood(ctx: Context, appId: String, entry: FoodEntry) {
        val cur = _flow.value.ifEmpty { load(ctx) }
        saveSync(ctx, cur.map { if (it.id == appId) it.copy(food = it.food + entry) else it })
    }

    suspend fun addSleep(ctx: Context, appId: String, entry: SleepEntry) {
        val cur = _flow.value.ifEmpty { load(ctx) }
        saveSync(ctx, cur.map { if (it.id == appId) it.copy(sleep = it.sleep + entry) else it })
    }
}
