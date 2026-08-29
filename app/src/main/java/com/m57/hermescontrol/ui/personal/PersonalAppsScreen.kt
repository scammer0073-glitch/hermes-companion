package com.m57.hermescontrol.ui.personal

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m57.hermescontrol.NavigationController
import com.m57.hermescontrol.PersonalAppDetailKey
import com.m57.hermescontrol.data.local.FoodEntry
import com.m57.hermescontrol.data.local.PersonalApp
import com.m57.hermescontrol.data.local.PersonalAppStore
import com.m57.hermescontrol.data.local.SleepEntry
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
    HermesScaffold(title = { Text("Personal Apps") }, navigationIcon = onOpenDrawer?.let { NavIcon.Menu(it) }) { pad ->
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().padding(16.dp)) {
        Column(Modifier.padding(pad).fillMaxSize().padding(16.dp)) {
            if (apps.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.size(64.dp).clip(CircleShape).background(Color(0xFF111820)), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Apps, null, tint = Color(0xFF2DD4BF), modifier = Modifier.size(28.dp)) }
                        Spacer(Modifier.height(12.dp))
                        Text("No personal apps yet", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Create Food and Sleep, Gym, etc.", color = Color(0xFF8B9AB0), fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(apps, key = { it.id }) { app ->
                        Card(onClick = { NavigationController.navigateTo(PersonalAppDetailKey(app.id)) }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0F12)), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2D44))) {
                            Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF14302C)), contentAlignment = Alignment.Center) { Text(app.icon, fontSize = 20.sp) }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(app.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text(app.food.size.toString() + " foods " + app.sleep.size.toString() + " sleeps", color = Color(0xFF8B9AB0), fontSize = 11.sp)
                                }
                                Badge(containerColor = Color(0xFF14302C), contentColor = Color(0xFF2DD4BF)) { Text("Chat+Stats") }
                            }
                        }
                    }
                }
            }
            }
            }
            FloatingActionButton(onClick = { showCreate = true }, containerColor = Color(0xFF2DD4BF), contentColor = Color(0xFF001018), modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)) { Icon(Icons.Filled.Add, null) }
        }
        if (showCreate) {
            AlertDialog(onDismissRequest = { showCreate = false }, title = { Text("New Personal App") }, text = { OutlinedTextField(value = newName, onValueChange = { newName = it }, placeholder = { Text("e.g. Food and Sleep") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }, confirmButton = { Button(onClick = { if (newName.isNotBlank()) { scope.launch { PersonalAppStore.addApp(ctx, PersonalApp(id = System.currentTimeMillis().toString(), name = newName.trim(), createdAt = System.currentTimeMillis())); newName=""; showCreate=false } } }, enabled = newName.isNotBlank()) { Text("Create") } }, dismissButton = { TextButton(onClick = { showCreate=false }) { Text("Cancel") } })
        }
    }
}

@Composable
fun PersonalAppDetailScreen(appId: String, onOpenDrawer: (() -> Unit)? = null) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var apps by remember { mutableStateOf(emptyList<PersonalApp>()) }
    var tab by remember { mutableIntStateOf(0) }
    var input by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { PersonalAppStore.flow(ctx).collect { apps = it } }
    val app = apps.find { it.id == appId }
    val voiceLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        if (res.resultCode == Activity.RESULT_OK) {
            val t = res.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!t.isNullOrBlank()) input = t
        }
    }
    HermesScaffold(title = { Text(app?.name ?: "Personal App") }, navigationIcon = onOpenDrawer?.let { NavIcon.Menu(it) }) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            TabRow(selectedTabIndex = tab, containerColor = Color.Black, contentColor = Color(0xFF2DD4BF)) {
                Tab(selected = tab==0, onClick = { tab=0 }, text = { Text("Chat") })
                Tab(selected = tab==1, onClick = { tab=1 }, text = { Text("Stats") })
            }
            if (app == null) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color(0xFF2DD4BF)) }; return@Column }
            if (tab==0) {
                LazyColumn(Modifier.weight(1f).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(app.food) { f -> Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0F12)), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2D44))) { Text(f.text, modifier = Modifier.padding(12.dp), color = Color.White, fontSize = 13.sp) } }
                    items(app.sleep) { s -> Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF14302C)), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2DD4BF))) { Text("Sleep entry", modifier = Modifier.padding(12.dp), color = Color.White) } }
                    if (app.food.isEmpty() && app.sleep.isEmpty()) item { Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { Text("Type or speak: ate 2 eggs 8am, slept 11:30-6:45", color = Color(0xFF8B9AB0)) } }
                }
                Row(Modifier.fillMaxWidth().padding(12.dp).background(Color(0xFF0D0F12), RoundedCornerShape(16.dp)).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(value = input, onValueChange = { input = it }, modifier = Modifier.weight(1f), placeholder = { Text("Log food or sleep...") }, singleLine = true)
                    IconButton(onClick = { val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply { putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM) }; voiceLauncher.launch(i) }) { Icon(Icons.Filled.Mic, null, tint = Color(0xFF2DD4BF)) }
                    Button(onClick = {
                        if (input.isBlank()) return@Button
                        val parsed = PersonalAppParser.parse(input)
                        scope.launch {
                            for (e in parsed) when(e) {
                                is ParsedEntry.Food -> PersonalAppStore.addFood(ctx, appId, FoodEntry(id=System.nanoTime().toString(), text=e.text, timeMillis=System.currentTimeMillis(), calories=e.calories))
                                is ParsedEntry.Sleep -> PersonalAppStore.addSleep(ctx, appId, SleepEntry(id=System.nanoTime().toString(), bedMillis=System.currentTimeMillis()-7*3600*1000, wakeMillis=System.currentTimeMillis()))
                            }
                            input=""
                        }
                    }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2DD4BF), contentColor = Color(0xFF001018))) { Text("Add") }
                }
            } else {
                Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0F12)), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2D44))) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Sleep last 7 days", color = Color.White, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Row(Modifier.fillMaxWidth().height(80.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
                                val hrs = app.sleep.takeLast(7).map { (it.wakeMillis - it.bedMillis)/3600000f }.ifEmpty { listOf(0f) }
                                hrs.forEach { h -> Box(Modifier.width(24.dp).height((h.coerceIn(0f,10f)/10f*70).dp).clip(RoundedCornerShape(6.dp)).background(Color(0xFF2DD4BF))) }
                                if (hrs.size<7) repeat(7-hrs.size) { Box(Modifier.width(24.dp).height(8.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xFF1E2D44))) }
                            }
                        }
                    }
                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0F12)), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2D44))) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Food recent", color = Color.White, fontWeight = FontWeight.Bold)
                            app.food.takeLast(5).reversed().forEach { f -> Text(f.text, color = Color(0xFFE6EDF3), fontSize = 12.sp, modifier = Modifier.padding(vertical=2.dp)) }
                            if (app.food.isEmpty()) Text("No food yet chat to log", color = Color(0xFF8B9AB0), fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
