package com.m57.hermescontrol.ui.personal

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m57.hermescontrol.NavigationController
import com.m57.hermescontrol.PersonalAppDetailKey
import com.m57.hermescontrol.data.local.FoodEntry
import com.m57.hermescontrol.data.local.PersonalApp
import com.m57.hermescontrol.data.local.PersonalAppStore
import com.m57.hermescontrol.data.local.SleepEntry
import com.m57.hermescontrol.theme.NemasysPalette
import com.m57.hermescontrol.ui.common.HermesScaffold
import com.m57.hermescontrol.ui.common.NavIcon
import kotlinx.coroutines.launch

@Composable
fun PersonalAppsScreen(onOpenDrawer: (() -> Unit)? = null) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var apps by remember { mutableStateOf(emptyList<PersonalApp>()) }
    var showCreate by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { PersonalAppStore.flow(ctx).collect { apps = it } }
    HermesScaffold(title = { Text("Personal Apps") }, navigationIcon = onOpenDrawer?.let { NavIcon.Menu(it) }) {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().padding(16.dp)) {
                if (apps.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = NemasysPalette.Card),
                            border = BorderStroke(1.dp, NemasysPalette.CardBorder),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Box(
                                        Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(NemasysPalette.Accent),
                                    )
                                    Text(
                                        text = "PERSONAL APPS",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.6.sp,
                                        color = NemasysPalette.Text,
                                    )
                                }
                                Box(
                                    Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(NemasysPalette.Card)
                                        .border(
                                            BorderStroke(1.dp, NemasysPalette.CardBorder),
                                            RoundedCornerShape(16.dp),
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        Icons.Filled.Apps,
                                        null,
                                        tint = NemasysPalette.Accent,
                                        modifier = Modifier.size(28.dp),
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Box(
                                        Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(NemasysPalette.Accent),
                                    )
                                    Text(
                                        "NO PERSONAL APPS YET",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.6.sp,
                                        color = NemasysPalette.Text,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                                Text(
                                    "Create Food and Sleep, Gym, etc.",
                                    color = NemasysPalette.Muted,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(apps, key = { it.id }) { app ->
                            Card(
                                onClick = {
                                    NavigationController.navigateTo(PersonalAppDetailKey(app.id))
                                },
                                shape =
                                    RoundedCornerShape(
                                        16.dp,
                                    ),
                                colors =
                                    CardDefaults.cardColors(
                                        containerColor = NemasysPalette.Card,
                                    ),
                                border =
                                    androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        NemasysPalette.CardBorder,
                                    ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            ) {
                                Row(
                                    Modifier.fillMaxWidth().padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(
                                        Modifier.size(
                                            44.dp,
                                        ).clip(RoundedCornerShape(12.dp)).background(NemasysPalette.AccentContainer),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(app.icon, fontSize = 20.sp)
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            app.name,
                                            color = NemasysPalette.TextBright,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                        )
                                        Text(
                                            app.food.size.toString() + " foods " +
                                                app.sleep.size.toString() + " sleeps",
                                            color = NemasysPalette.Muted,
                                            fontSize = 11.sp,
                                        )
                                    }
                                    Badge(
                                        containerColor = NemasysPalette.AccentContainer,
                                        contentColor = NemasysPalette.Accent,
                                    ) { Text("Chat+Stats") }
                                }
                            }
                        }
                    }
                }
            }
            FloatingActionButton(
                onClick = {
                    showCreate = true
                },
                containerColor =
                    Color(
                        0xFF2DD4BF,
                    ),
                contentColor = NemasysPalette.OnAccent,
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            ) {
                Icon(Icons.Filled.Add, null)
            }
        }
        if (showCreate) {
            AlertDialog(onDismissRequest = {
                showCreate = false
            }, title = {
                Text("New Personal App")
            }, text = {
                OutlinedTextField(value = newName, onValueChange = {
                    newName = it
                }, placeholder = {
                    Text("e.g. Food and Sleep")
                }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }, confirmButton = {
                Button(onClick = {
                    if (newName.isNotBlank()) {
                        scope.launch {
                            PersonalAppStore.addApp(
                                ctx,
                                PersonalApp(
                                    id = System.currentTimeMillis().toString(),
                                    name = newName.trim(),
                                    createdAt = System.currentTimeMillis(),
                                ),
                            )
                            newName = ""
                            showCreate = false
                        }
                    }
                }, enabled = newName.isNotBlank()) {
                    Text("Create")
                }
            }, dismissButton = { TextButton(onClick = { showCreate = false }) { Text("Cancel") } })
        }
    }
}

@Composable
fun PersonalAppDetailScreen(
    appId: String,
    onOpenDrawer: (() -> Unit)? = null,
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var apps by remember { mutableStateOf(emptyList<PersonalApp>()) }
    var tab by remember { mutableIntStateOf(0) }
    var input by remember { mutableStateOf("") }
    var inputError by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) { PersonalAppStore.flow(ctx).collect { apps = it } }
    val app = apps.find { it.id == appId }
    val voiceLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
            if (res.resultCode == Activity.RESULT_OK) {
                val t = res.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
                if (!t.isNullOrBlank()) input = t
            }
        }
    HermesScaffold(title = {
        Text(app?.name ?: "Personal App")
    }, navigationIcon = onOpenDrawer?.let { NavIcon.Menu(it) }) {
        Column(Modifier.fillMaxSize()) {
            TabRow(
                selectedTabIndex = tab,
                containerColor = NemasysPalette.Canvas,
                contentColor = NemasysPalette.Accent,
            ) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Chat") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Stats") })
            }
            if (app == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NemasysPalette.Accent)
                }
                return@Column
            }
            if (tab == 0) {
                LazyColumn(Modifier.weight(1f).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(app.food) { f ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = NemasysPalette.Card),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NemasysPalette.CardBorder),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        ) {
                            Text(
                                f.text,
                                modifier = Modifier.padding(12.dp),
                                color = NemasysPalette.TextBright,
                                fontSize = 13.sp,
                            )
                        }
                    }
                    items(app.sleep) { s ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = NemasysPalette.Card),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NemasysPalette.CardBorder),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        ) {
                            Text("Sleep entry", modifier = Modifier.padding(12.dp), color = NemasysPalette.TextBright)
                        }
                    }
                    if (app.food.isEmpty() && app.sleep.isEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = NemasysPalette.Card),
                                border = BorderStroke(1.dp, NemasysPalette.CardBorder),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        Box(Modifier.size(6.dp).clip(CircleShape).background(NemasysPalette.Accent))
                                        Text(
                                            text = "NO ENTRIES",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.6.sp,
                                            color = NemasysPalette.Muted,
                                        )
                                    }
                                    Surface(
                                        modifier = Modifier.size(48.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        color = NemasysPalette.Card,
                                        border = BorderStroke(1.dp, NemasysPalette.CardBorder),
                                        tonalElevation = 0.dp,
                                        shadowElevation = 0.dp,
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Filled.Mic,
                                                null,
                                                tint = NemasysPalette.Text,
                                                modifier = Modifier.size(22.dp),
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Type or speak: ate 2 eggs 8am, slept 23:30-06:45",
                                        color = NemasysPalette.Muted,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }
                    }
                }
                Row(
                    Modifier.fillMaxWidth().padding(
                        12.dp,
                    ).background(NemasysPalette.Card, RoundedCornerShape(16.dp)).padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = {
                            input = it
                            inputError = null
                        },
                        isError = inputError != null,
                        supportingText = { inputError?.let { Text(it) } },
                        modifier =
                            Modifier.weight(
                                1f,
                            ),
                        placeholder = { Text("Log food or sleep...") },
                        singleLine = true,
                    )
                    IconButton(onClick = {
                        val i =
                            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(
                                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
                                )
                            }
                        voiceLauncher.launch(i)
                    }) { Icon(Icons.Filled.Mic, null, tint = NemasysPalette.Accent) }
                    Button(
                        onClick = {
                            if (input.isBlank()) return@Button
                            val parsed = PersonalAppParser.parse(input)
                            val nowMillis = System.currentTimeMillis()
                            val sleeps = parsed.filterIsInstance<ParsedEntry.Sleep>()
                            val intervals = sleeps.associateWith { PersonalAppParser.resolveSleep(it, nowMillis) }
                            if (parsed.isEmpty() || intervals.values.any { it == null }) {
                                inputError = "Enter sleep times in 24-hour format, e.g. slept 23:30-06:45."
                                return@Button
                            }
                            scope.launch {
                                for (e in parsed) when (e) {
                                    is ParsedEntry.Food ->
                                        PersonalAppStore.addFood(
                                            ctx,
                                            appId,
                                            FoodEntry(
                                                id = System.nanoTime().toString(),
                                                text = e.text,
                                                timeMillis = System.currentTimeMillis(),
                                                calories = e.calories,
                                            ),
                                        )
                                    is ParsedEntry.Sleep ->
                                        PersonalAppStore.addSleep(
                                            ctx,
                                            appId,
                                            SleepEntry(
                                                id = System.nanoTime().toString(),
                                                bedMillis = intervals.getValue(e)!!.bedMillis,
                                                wakeMillis = intervals.getValue(e)!!.wakeMillis,
                                            ),
                                        )
                                }
                                input = ""
                                inputError = null
                            }
                        },
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = NemasysPalette.Accent,
                                contentColor = NemasysPalette.OnAccent,
                            ),
                    ) {
                        Text("Add")
                    }
                }
            } else {
                Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = NemasysPalette.Card),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NemasysPalette.CardBorder),
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Sleep last 7 days", color = NemasysPalette.TextBright, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Row(
                                Modifier.fillMaxWidth().height(80.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.Bottom,
                            ) {
                                val hrs =
                                    PersonalAppParser.sleepHoursLastSevenDays(
                                        app.sleep.map {
                                            PersonalAppParser.SleepInterval(it.bedMillis, it.wakeMillis)
                                        },
                                        System.currentTimeMillis(),
                                    )
                                hrs.forEach {
                                        h ->
                                    Box(
                                        Modifier.width(
                                            24.dp,
                                        ).height(
                                            (h.coerceIn(0f, 10f) / 10f * 70).dp,
                                        ).clip(RoundedCornerShape(6.dp)).background(NemasysPalette.Accent),
                                    )
                                }
                            }
                        }
                    }
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = NemasysPalette.Card),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NemasysPalette.CardBorder),
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Food recent", color = NemasysPalette.TextBright, fontWeight = FontWeight.Bold)
                            app.food.takeLast(5).reversed().forEach {
                                    f ->
                                Text(
                                    f.text,
                                    color = NemasysPalette.Text,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(vertical = 2.dp),
                                )
                            }
                            if (app.food.isEmpty()) {
                                Spacer(Modifier.height(8.dp))
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = NemasysPalette.Card),
                                    border = BorderStroke(1.dp, NemasysPalette.CardBorder),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        ) {
                                            Box(Modifier.size(6.dp).clip(CircleShape).background(NemasysPalette.Accent))
                                            Text(
                                                text = "NO FOOD",
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.6.sp,
                                                color = NemasysPalette.Muted,
                                            )
                                        }
                                        Text(
                                            "NO FOOD YET, CHAT TO LOG",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.6.sp,
                                            color = NemasysPalette.Muted,
                                            textAlign = TextAlign.Center,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
