package com.zicabin.superhabit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                AppNav()
            }
        }
    }
}

@Composable
fun AppNav() {
    val navController = rememberNavController()
    val items = listOf(
        BottomItem("home", "پیشخوان", Icons.Filled.Home),
        BottomItem("activities", "فعالیت‌ها", Icons.Filled.List),
        BottomItem("stats", "آمار", Icons.Filled.Insights),
        BottomItem("calendar", "تقویم", Icons.Filled.CalendarToday),
        BottomItem("more", "بیشتر", Icons.Filled.MoreHoriz)
    )

    var dailyNote by rememberSaveable { mutableStateOf("") }
    var activities by rememberSaveable { mutableStateOf(sampleActivities()) }
    var groups by rememberSaveable { mutableStateOf(listOf("اهداف", "سایر")) }
    var milestones by rememberSaveable { mutableStateOf(listOf<Milestone>()) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination?.route
                items.forEach { item ->
                    NavigationBarItem(
                        selected = currentDestination == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(padding)
        ) {
            composable("home") {
                HomeScreen(
                    dailyNote = dailyNote,
                    onDailyNoteChange = { dailyNote = it },
                    activities = activities,
                    onToggle = { id, done ->
                        activities = activities.map { if (it.id == id) it.copy(isDone = done) else it }
                    },
                    onAddQuickActivity = { title ->
                        val maxId = (activities.maxOfOrNull { it.id } ?: 0) + 1
                        activities = activities + ActivityItem(id = maxId, title = title, isDone = false, group = "سایر")
                    }
                )
            }
            composable("activities") {
                ActivitiesScreen(
                    groups = groups,
                    activities = activities,
                    onAddGroup = { name -> groups = groups + name },
                    onAddActivity = { title, group ->
                        val maxId = (activities.maxOfOrNull { it.id } ?: 0) + 1
                        activities = activities + ActivityItem(maxId, title, false, group)
                    },
                    onArchive = { id -> activities = activities.map { if (it.id == id) it.copy(isArchived = true) else it } }
                )
            }
            composable("stats") {
                StatsScreen(activities = activities)
            }
            composable("calendar") {
                CalendarScreen(
                    milestones = milestones,
                    onAddMilestone = { title ->
                        milestones = milestones + Milestone(LocalDate.now(), title)
                    }
                )
            }
            composable("more") {
                MoreScreen(
                    onChangeColors = { },
                    onSortTasks = { },
                    onBackup = { },
                    onRestore = { },
                    onShare = { },
                    onMessage = { },
                    onResetAll = {
                        dailyNote = ""
                        activities = sampleActivities()
                        groups = listOf("اهداف", "سایر")
                        milestones = emptyList()
                    },
                    onLanguage = { },
                    onRateUs = { }
                )
            }
        }
    }
}

data class BottomItem(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
data class ActivityItem(val id: Int, val title: String, val isDone: Boolean, val group: String, val isArchived: Boolean = false)
data class Milestone(val date: LocalDate, val title: String)

@Composable
fun ScreenTitle(title: String) {
    Text(title, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
fun HomeScreen(
    dailyNote: String,
    onDailyNoteChange: (String) -> Unit,
    activities: List<ActivityItem>,
    onToggle: (Int, Boolean) -> Unit,
    onAddQuickActivity: (String) -> Unit
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        ScreenTitle("امروز")
        Button(onClick = { onAddQuickActivity("فعالیت جدید") }) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("چند تا ماجراجویی به روز خالیت اضافه کن!")
        }
        Spacer(Modifier.height(16.dp))
        Text("یادداشت روزانه", fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = dailyNote,
            onValueChange = onDailyNoteChange,
            placeholder = { Text("نوشتن یادداشت...") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Text("فعالیت‌های امروز", fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        if (activities.isEmpty()) {
            EmptyCard("به داده های بیشتری برای ترسیم این نمودار نیاز داریم. لطفا بعدا بهمون سر بزن! 👋")
        } else {
            LazyColumn {
                items(activities.filter { !it.isArchived }) { item ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = item.isDone, onCheckedChange = { onToggle(item.id, it) })
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(item.title)
                            Text(item.group, fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                    Divider()
                }
            }
        }
    }
}

@Composable
fun ActivitiesScreen(
    groups: List<String>,
    activities: List<ActivityItem>,
    onAddGroup: (String) -> Unit,
    onAddActivity: (String, String) -> Unit,
    onArchive: (Int) -> Unit
) {
    var newGroup by remember { mutableStateOf("") }
    var newActivity by remember { mutableStateOf("") }
    var selectedGroup by remember { mutableStateOf(groups.firstOrNull() ?: "سایر") }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        ScreenTitle("گروه‌ها")
        LazyColumn(Modifier.weight(1f)) {
            items(groups) { g ->
                ListItem(
                    leadingContent = { Icon(Icons.Filled.Folder, contentDescription = null) },
                    headlineContent = { Text(g) }
                )
                Divider()
            }
        }
        OutlinedTextField(value = newGroup, onValueChange = { newGroup = it }, placeholder = { Text("نام گروه جدید") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        Button(onClick = {
            if (newGroup.isNotBlank()) {
                onAddGroup(newGroup.trim())
                selectedGroup = newGroup.trim()
                newGroup = ""
            }
        }) {
            Icon(Icons.Filled.CreateNewFolder, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("اضافه کردن گروه")
        }

        Spacer(Modifier.height(16.dp))
        ScreenTitle("فعالیت‌ها")
        OutlinedTextField(value = newActivity, onValueChange = { newActivity = it }, placeholder = { Text("عنوان فعالیت جدید") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("گروه انتخاب‌شده: ")
            Text(selectedGroup, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = {
            if (newActivity.isNotBlank()) {
