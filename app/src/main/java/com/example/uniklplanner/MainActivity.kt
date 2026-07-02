package com.example.uniklplanner

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

// ─────────────────────────────────────────────
// THEME COLORS
// ─────────────────────────────────────────────
val UniKLBlue   = Color(0xFF5D8BF4)
val UniKLOrange = Color(0xFFFF914D)
val UniKLBg     = Color(0xFFF2F4FB)
val UniKLCard   = Color(0xFFFFFFFF)
val UniKLGray   = Color(0xFF9098A3)
val UniKLDark   = Color(0xFF1C2333)
val UniKLGreen  = Color(0xFF4CAF7D)
val UniKLRed    = Color(0xFFEF5350)

val SlotColors = listOf(
    Color(0xFF5D8BF4), Color(0xFFFF914D), Color(0xFF4CAF7D),
    Color(0xFFAB47BC), Color(0xFF26C6DA), Color(0xFFEF5350),
    Color(0xFFFFB74D), Color(0xFF66BB6A)
)

// Category colors
fun categoryColor(category: String): Color = when (category) {
    "CORE"     -> UniKLBlue
    "MPU"      -> UniKLOrange
    "UCS"      -> Color(0xFFAB47BC)
    "FYP"      -> UniKLGreen
    "INTRA"    -> Color(0xFF26C6DA)
    "ELECTIVE" -> Color(0xFFFFB74D)
    else       -> UniKLGray
}

// ─────────────────────────────────────────────
// DATA MODELS
// ─────────────────────────────────────────────

enum class CourseStatus { PENDING, ST, PASS, CT }   // ST = currently taking, CT = credit transfer

data class ScheduleGroup(
    val groupCode: String,    // "G1", "G2"
    val day: String,
    val startTime: String,
    val endTime: String,
    val room: String,
    val lecturer: String
)

data class Course(
    val code: String,                       // e.g. "FAB11203"
    val name: String,
    val credits: Int,
    val category: String,                   // CORE, MPU, UCS, FYP, INTRA, ELECTIVE
    val year: Int,
    val semester: Int,
    val prerequisite: String? = null,       // course code, nullable
    val isOptional: Boolean = false,        // marked with # in report (MPU choice subjects)
    val semestersOffered: List<String> = listOf("MARCH", "JULY", "OCTOBER"),
    val groups: List<ScheduleGroup> = emptyList(),
    val status: CourseStatus = CourseStatus.PENDING,
    val selectedGroupCode: String? = null   // which group student is enrolled in
)

data class StudyTask(
    val id: String,
    val title: String,
    val courseName: String,
    val dueDate: String,
    val isCompleted: Boolean = false
)

data class StudentProfile(
    val studentId: String,
    val name: String,
    val programme: String,
    val intake: String,
    val currentSemester: Int,
    val totalCreditsRequired: Int,
    val institute: String,
    val academicAdvisor: String
)

val DAYS_ORDER = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")

// Category requirements (from PDF summary)
val CATEGORY_REQUIREMENTS = mapOf(
    "CORE"     to 91,
    "ELECTIVE" to 9,
    "FYP"      to 10,
    "INTRA"    to 12,
    "MPU"      to 10,
    "UCS"      to 8
)

// ─────────────────────────────────────────────
// VIEWMODEL — now backed by Firebase!
// FirebaseAppViewModel is now an alias for FirebaseAppViewModel.
// All UI code that uses `vm: FirebaseAppViewModel` continues to work,
// but the data actually comes from the database.
// ─────────────────────────────────────────────

typealias AppViewModel = FirebaseAppViewModel

// ─────────────────────────────────────────────
// FALLBACK PROFILE (used while DB loads)
// ─────────────────────────────────────────────
val EMPTY_PROFILE = StudentProfile(
    studentId = "...",
    name = "Loading...",
    programme = "...",
    intake = "...",
    currentSemester = 9,
    totalCreditsRequired = 140,
    institute = "...",
    academicAdvisor = "..."
)

// ─────────────────────────────────────────────
// MAIN ACTIVITY
// ─────────────────────────────────────────────

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ClassReminders.createChannel(this)
        enableEdgeToEdge()
        setContent { UniKLPlannerTheme { AppNavigation() } }
    }
}

object ClassReminders {
    const val CHANNEL_ID = "class_reminders"
    const val EXTRA_COURSE_CODE = "course_code"
    const val EXTRA_COURSE_NAME = "course_name"
    const val EXTRA_TIME = "time"
    const val EXTRA_ROOM = "room"
    const val MINUTES_BEFORE = 15
    private const val TEST_REQUEST_CODE = 99999

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Class Reminders", NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders 15 minutes before each class starts"
                enableVibration(true)
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    fun scheduleTestNotification(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ClassReminderReceiver::class.java).apply {
            putExtra(EXTRA_COURSE_CODE, "FPB49906")
            putExtra(EXTRA_COURSE_NAME, "Final Year Project 2")
            putExtra(EXTRA_TIME, "10:00–12:00")
            putExtra(EXTRA_ROOM, "G1 · LAB-MFI-1")
        }
        val pi = PendingIntent.getBroadcast(
            context, TEST_REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            am.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5_000L, pi
            )
        } catch (e: SecurityException) {
            am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5_000L, pi)
        }
    }

    fun scheduleOne(context: Context, course: Course, group: ScheduleGroup) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val dayMap = mapOf(
            "Monday" to java.util.Calendar.MONDAY, "Tuesday" to java.util.Calendar.TUESDAY,
            "Wednesday" to java.util.Calendar.WEDNESDAY, "Thursday" to java.util.Calendar.THURSDAY,
            "Friday" to java.util.Calendar.FRIDAY
        )
        val targetDay = dayMap[group.day] ?: return
        val parts = group.startTime.split(":")
        if (parts.size != 2) return
        val hour = parts[0].toIntOrNull() ?: return
        val minute = parts[1].toIntOrNull() ?: return
        val cal = java.util.Calendar.getInstance().apply {
            while (get(java.util.Calendar.DAY_OF_WEEK) != targetDay) add(java.util.Calendar.DAY_OF_YEAR, 1)
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute - MINUTES_BEFORE)
            set(java.util.Calendar.SECOND, 0); set(java.util.Calendar.MILLISECOND, 0)
            if (timeInMillis < System.currentTimeMillis()) add(java.util.Calendar.DAY_OF_YEAR, 7)
        }
        val intent = Intent(context, ClassReminderReceiver::class.java).apply {
            putExtra(EXTRA_COURSE_CODE, course.code)
            putExtra(EXTRA_COURSE_NAME, course.name)
            putExtra(EXTRA_TIME, "${group.startTime}–${group.endTime}")
            putExtra(EXTRA_ROOM, "${group.groupCode} · ${group.room}")
        }
        val pi = PendingIntent.getBroadcast(
            context, course.code.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
        } catch (e: SecurityException) {
            am.set(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
        }
    }

    fun scheduleAllReminders(context: Context, courses: List<Course>): Int {
        val enrolled = courses.filter { it.status == CourseStatus.ST }
        var count = 0
        enrolled.forEach { course ->
            val group = course.groups.firstOrNull { it.groupCode == course.selectedGroupCode }
                ?: course.groups.firstOrNull()
            if (group != null) { scheduleOne(context, course, group); count++ }
        }
        return count
    }

    fun cancelAll(context: Context, courses: List<Course>) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        courses.forEach { course ->
            val pi = PendingIntent.getBroadcast(
                context, course.code.hashCode(),
                Intent(context, ClassReminderReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            am.cancel(pi)
        }
        val testPi = PendingIntent.getBroadcast(
            context, TEST_REQUEST_CODE,
            Intent(context, ClassReminderReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.cancel(testPi)
    }
}

@Composable
fun UniKLPlannerTheme(content: @Composable () -> Unit) {
    val colorScheme = lightColorScheme(
        primary = UniKLBlue, secondary = UniKLOrange,
        background = UniKLBg, surface = UniKLCard,
        onPrimary = Color.White, onSecondary = Color.White,
        onBackground = UniKLDark, onSurface = UniKLDark,
        tertiary = UniKLGreen, error = UniKLRed
    )
    MaterialTheme(colorScheme = colorScheme, content = content)
}

enum class Screen { Splash, Login, Timetable, CourseRegistration, CourseManagement, Tasks, Profile }

@Composable
fun AppNavigation() {
    var currentScreen by remember { mutableStateOf(Screen.Splash) }
    val vm: FirebaseAppViewModel = viewModel()

    when (currentScreen) {
        Screen.Splash -> SplashScreen { currentScreen = Screen.Login }
        Screen.Login  -> LoginScreen  { currentScreen = Screen.Timetable }
        else -> MainDrawerContainer(currentScreen, { currentScreen = it }, vm) {
            when (currentScreen) {
                Screen.Timetable          -> TimetableScreen(vm)
                Screen.CourseRegistration -> CourseRegistrationScreen(vm)
                Screen.CourseManagement   -> CourseManagementScreen(vm)
                Screen.Tasks              -> StudyPlannerScreen(vm)
                Screen.Profile            -> ProfileScreen(vm)
                else -> {}
            }
        }
    }
}

// ─────────────────────────────────────────────
// DRAWER
// ─────────────────────────────────────────────

@Composable
fun MainDrawerContainer(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    vm: FirebaseAppViewModel,
    content: @Composable () -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val profileState by vm.profile.collectAsState()
    val p = profileState ?: EMPTY_PROFILE
    val totalDone = vm.completedCredits + vm.currentSemesterCredits

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = UniKLCard, modifier = Modifier.width(290.dp)) {
                Box(Modifier.fillMaxWidth().background(UniKLBlue).padding(20.dp)) {
                    Column {
                        SmallLogo(white = true)
                        Spacer(Modifier.height(14.dp))
                        Text(p.name.split(" ").take(3).joinToString(" "),
                            color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("ID: ${p.studentId}",
                            color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                        Text("Sem ${p.currentSemester} · MFI",
                            color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                        Spacer(Modifier.height(10.dp))
                        Surface(color = Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(20.dp)) {
                            Text("$totalDone / ${p.totalCreditsRequired} credits",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                DrawerItem("Timetable",           Icons.Default.DateRange, currentScreen == Screen.Timetable)          { onNavigate(Screen.Timetable);          scope.launch { drawerState.close() } }
                DrawerItem("Course Registration", Icons.Default.AddCircle, currentScreen == Screen.CourseRegistration) { onNavigate(Screen.CourseRegistration); scope.launch { drawerState.close() } }
                DrawerItem("Course Management",   Icons.Default.ListAlt,   currentScreen == Screen.CourseManagement)   { onNavigate(Screen.CourseManagement);   scope.launch { drawerState.close() } }
                DrawerItem("Study Tasks",         Icons.Default.Task,      currentScreen == Screen.Tasks)              { onNavigate(Screen.Tasks);              scope.launch { drawerState.close() } }
                DrawerItem("My Profile",          Icons.Default.Person,    currentScreen == Screen.Profile)            { onNavigate(Screen.Profile);            scope.launch { drawerState.close() } }

                Spacer(Modifier.weight(1f))
                HorizontalDivider(color = UniKLBg)
                DrawerItem("Log Out", Icons.Default.ExitToApp, false, tint = UniKLRed) {
                    onNavigate(Screen.Login); scope.launch { drawerState.close() }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                @OptIn(ExperimentalMaterial3Api::class)
                TopAppBar(
                    title = { Box(Modifier.fillMaxWidth().padding(end = 48.dp), Alignment.Center) { SmallLogo() } },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, "Menu", tint = UniKLDark)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = UniKLCard)
                )
            },
            containerColor = UniKLBg
        ) { innerPadding -> Box(Modifier.padding(innerPadding)) { content() } }
    }
}

@Composable
fun DrawerItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector,
               selected: Boolean, tint: Color = UniKLBlue, onClick: () -> Unit) {
    NavigationDrawerItem(
        label = { Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
        selected = selected, onClick = onClick,
        icon = { Icon(icon, null, tint = if (selected) UniKLBlue else tint) },
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = UniKLBlue.copy(alpha = 0.12f),
            unselectedContainerColor = Color.Transparent
        ),
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
    )
}

// ─────────────────────────────────────────────
// SPLASH + LOGIN
// ─────────────────────────────────────────────

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    LaunchedEffect(Unit) { delay(2200); onFinished() }
    Box(Modifier.fillMaxSize().background(UniKLBg), Alignment.Center) { LogoComponent(isSmall = false) }
}

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    Box(Modifier.fillMaxSize().background(UniKLBg), Alignment.Center) {
        Column(Modifier.fillMaxWidth().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            LogoComponent(isSmall = true)
            Spacer(Modifier.height(36.dp))
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = UniKLCard),
                elevation = CardDefaults.cardElevation(4.dp)) {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Sign In", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = UniKLDark)
                    Text("Use your UniKL student account", fontSize = 13.sp, color = UniKLGray)

                    LabelledField("Email") {
                        OutlinedTextField(value = email, onValueChange = { email = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("student@unikl.edu.my", color = UniKLGray) },
                            shape = RoundedCornerShape(8.dp), singleLine = true, colors = uniKLFieldColors())
                    }
                    LabelledField("Password") {
                        OutlinedTextField(value = password, onValueChange = { password = it },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            placeholder = { Text("••••••••", color = UniKLGray) },
                            shape = RoundedCornerShape(8.dp), singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, null, tint = UniKLGray)
                                }
                            },
                            colors = uniKLFieldColors())
                    }
                    Spacer(Modifier.height(4.dp))
                    if (isLoading) {
                        CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally), color = UniKLBlue)
                    } else {
                        Button(onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isLoading = true
                            auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) onLoginSuccess()
                                    else Toast.makeText(context, "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                }
                        },
                            Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = UniKLBlue)
                        ) { Text("Login", fontWeight = FontWeight.Bold, fontSize = 15.sp) }
                    }
                }
            }
        }
    }
}

@Composable
fun LabelledField(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = UniKLDark)
        content()
    }
}

@Composable
fun uniKLFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = UniKLBlue, unfocusedBorderColor = UniKLBg,
    focusedTextColor = UniKLDark, unfocusedTextColor = UniKLDark,
    cursorColor = UniKLBlue,
    unfocusedContainerColor = UniKLBg, focusedContainerColor = UniKLBg
)

// ─────────────────────────────────────────────
// TIMETABLE
// ─────────────────────────────────────────────

@Composable
fun TimetableScreen(vm: FirebaseAppViewModel) {
    val courses by vm.courses.collectAsState()
    val profileState by vm.profile.collectAsState()
    val p = profileState ?: EMPTY_PROFILE
    val isLoading by vm.isLoading.collectAsState()
    val enrolled = courses.filter { it.status == CourseStatus.ST }
    val scrollH = rememberScrollState()
    val scrollV = rememberScrollState()

    val startHour = 8; val endHour = 18
    val hourH = 80.dp; val dayW = 140.dp; val timeW = 52.dp

    if (isLoading) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = UniKLBlue)
                Spacer(Modifier.height(12.dp))
                Text("Loading from database...", color = UniKLGray, fontSize = 13.sp)
            }
        }
        return
    }

    if (enrolled.isEmpty()) {
        EmptyState("No courses enrolled this semester.\nGo to Course Registration to add some.", Icons.Default.DateRange)
        return
    }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().background(UniKLCard).padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            CreditPill("This Sem", "${vm.currentSemesterCredits} cr", UniKLBlue)
            CreditPill("Completed", "${vm.completedCredits} cr", UniKLGreen)
            CreditPill("Total", "${vm.completedCredits + vm.currentSemesterCredits}/${p.totalCreditsRequired}", UniKLOrange)
        }
        HorizontalDivider(color = UniKLBg, thickness = 2.dp)

        Box(Modifier.fillMaxSize()) {
            Row(Modifier.verticalScroll(scrollV)) {
                Column(Modifier.width(timeW)) {
                    Spacer(Modifier.height(40.dp))
                    for (h in startHour until endHour) {
                        Box(Modifier.height(hourH).fillMaxWidth().border(0.5.dp, UniKLBg).padding(top = 4.dp, start = 4.dp)) {
                            Text("${h.toString().padStart(2,'0')}:00", fontSize = 10.sp, color = UniKLGray)
                        }
                    }
                }
                Box(Modifier.horizontalScroll(scrollH)) {
                    Column {
                        Row {
                            DAYS_ORDER.forEach { day ->
                                Box(Modifier.width(dayW).height(40.dp).background(UniKLBlue)
                                    .border(0.5.dp, Color.White.copy(alpha = 0.3f)), Alignment.Center) {
                                    Text(day.take(3).uppercase(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Box {
                            Row {
                                DAYS_ORDER.forEach { _ ->
                                    Column(Modifier.width(dayW)) {
                                        for (h in startHour until endHour) {
                                            Box(Modifier.height(hourH).fillMaxWidth()
                                                .background(if (h % 2 == 0) UniKLCard else UniKLBg)
                                                .border(0.5.dp, UniKLBg))
                                        }
                                    }
                                }
                            }
                            Row {
                                DAYS_ORDER.forEach { day ->
                                    Box(Modifier.width(dayW)) {
                                        enrolled.forEachIndexed { idx, course ->
                                            val grp = course.groups.firstOrNull { it.groupCode == course.selectedGroupCode }
                                                ?: course.groups.firstOrNull()
                                            if (grp != null && grp.day == day) {
                                                val sh = timeToHour(grp.startTime); val eh = timeToHour(grp.endTime)
                                                val topOffset = ((sh - startHour) * hourH.value).dp
                                                val slotHeight = ((eh - sh) * hourH.value).dp
                                                Box(Modifier.offset(y = topOffset).width(dayW - 4.dp)
                                                    .height(slotHeight - 2.dp).clip(RoundedCornerShape(8.dp))
                                                    .background(SlotColors[idx % SlotColors.size]).padding(6.dp)) {
                                                    Column {
                                                        Text(course.code, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                        Text(course.name, color = Color.White.copy(alpha = 0.9f), fontSize = 9.sp, maxLines = 2)
                                                        Spacer(Modifier.weight(1f))
                                                        Text("${grp.startTime}-${grp.endTime}", color = Color.White.copy(alpha = 0.8f), fontSize = 9.sp)
                                                        Text("${course.selectedGroupCode ?: "G1"} · ${grp.room}", color = Color.White.copy(alpha = 0.8f), fontSize = 9.sp)
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
            }
        }
    }
}

fun timeToHour(t: String): Int = t.split(":")[0].toInt()

@Composable
fun CreditPill(label: String, value: String, color: Color) {
    Surface(color = color.copy(alpha = 0.12f), shape = RoundedCornerShape(20.dp)) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(color))
            Text(label, fontSize = 11.sp, color = UniKLGray)
            Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

// ─────────────────────────────────────────────
// COURSE REGISTRATION
// ─────────────────────────────────────────────

@Composable
fun CourseRegistrationScreen(vm: FirebaseAppViewModel) {
    val courses by vm.courses.collectAsState()
    val profileState by vm.profile.collectAsState()
    val p = profileState ?: EMPTY_PROFILE
    val currentSem by vm.currentSemester.collectAsState()
    val suggestions = remember(courses, currentSem) { vm.getAISuggestions() }
    var showSemPicker by remember { mutableStateOf(false) }
    val enrolled = courses.filter { it.status == CourseStatus.ST }

    var showAddManual by remember { mutableStateOf(false) }
    var showAI by remember { mutableStateOf(true) }

    Column(Modifier.fillMaxSize().background(UniKLBg)) {
        Card(Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = UniKLBlue)) {
            Column(Modifier.padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text("Course Registration", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(p.programme.take(34) + "…",
                            color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                    }
                    Surface(color = Color.White.copy(alpha = 0.25f), shape = RoundedCornerShape(20.dp),
                        onClick = { showSemPicker = true }) {
                        Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Sem $currentSem", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(13.dp))
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SemesterStatChip("Enrolled", "${enrolled.size}")
                    SemesterStatChip("Credits", "${vm.currentSemesterCredits}")
                    SemesterStatChip("Progress", "${vm.completedCredits + vm.currentSemesterCredits}/${p.totalCreditsRequired}")
                }
            }
        }

        Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { showAI = !showAI },
                colors = ButtonDefaults.buttonColors(containerColor = UniKLOrange),
                shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.AutoAwesome, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(if (showAI) "Hide AI" else "AI Suggest", fontSize = 13.sp)
            }
            OutlinedButton(onClick = { showAddManual = true },
                shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f),
                border = androidx.compose.foundation.BorderStroke(1.dp, UniKLBlue)) {
                Icon(Icons.Default.Add, null, Modifier.size(16.dp), tint = UniKLBlue)
                Spacer(Modifier.width(4.dp))
                Text("Manual Add", fontSize = 13.sp, color = UniKLBlue)
            }
        }

        Spacer(Modifier.height(8.dp))

        LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp)) {

            if (showAI) {
                item {
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = UniKLOrange.copy(alpha = 0.08f))) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, null, tint = UniKLOrange)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text("🧠 Neural network scored ${suggestions.size} candidate courses", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = UniKLDark)
                                Text("On-device TensorFlow Lite model · ranks by suitability, respects 20-credit cap", fontSize = 10.sp, color = UniKLGray)
                            }
                        }
                    }
                }
                item { SectionHeader("✨ Smart Suggestions (${suggestions.size})", UniKLOrange) }
                if (suggestions.isEmpty()) {
                    item {
                        Text("No more suggestions available — try dropping an enrolled course to free a slot.",
                            color = UniKLGray, fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp))
                    }
                } else {
                    items(suggestions) { sug ->
                        AISuggestionCard(sug) { vm.enrollCourse(sug.course.code, sug.group.groupCode) }
                    }
                }
                item { Spacer(Modifier.height(4.dp)) }
            }

            item { SectionHeader("📚 Currently Enrolled (${enrolled.size})", UniKLBlue) }
            if (enrolled.isEmpty()) {
                item { Text("Not enrolled in anything yet.", color = UniKLGray, fontSize = 13.sp, modifier = Modifier.padding(vertical = 8.dp)) }
            } else {
                items(enrolled) { course ->
                    EnrolledCourseCard(course, onDrop = { vm.dropCourse(course.code) },
                        onSelectGroup = { g -> vm.selectGroup(course.code, g) })
                }
            }
        }
    }

    if (showAddManual) {
        ManualAddDialog(
            courses = courses,
            onDismiss = { showAddManual = false },
            onConfirm = { code, groupCode ->
                vm.enrollByGroup(code, groupCode)
                showAddManual = false
            }
        )
    }

    if (showSemPicker) {
        SemesterPickerDialog(
            currentSem = currentSem,
            onSelect = { vm.setCurrentSemester(it); showSemPicker = false },
            onDismiss = { showSemPicker = false }
        )
    }
}

@Composable
fun SemesterPickerDialog(currentSem: Int, onSelect: (Int) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Current Semester", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("The neural network re-scores course suggestions for the selected semester.",
                    fontSize = 12.sp, color = UniKLGray)
                Spacer(Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    (1..8).forEach { sem ->
                        val selected = sem == currentSem
                        Surface(
                            color = if (selected) UniKLBlue.copy(alpha = 0.12f) else UniKLBg,
                            shape = RoundedCornerShape(8.dp),
                            border = if (selected) androidx.compose.foundation.BorderStroke(1.dp, UniKLBlue) else null,
                            onClick = { onSelect(sem) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Text("Semester $sem",
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) UniKLBlue else UniKLDark,
                                    fontSize = 14.sp, modifier = Modifier.weight(1f))
                                Text("Year ${(sem + 1) / 2}", fontSize = 11.sp, color = UniKLGray)
                                if (selected) {
                                    Spacer(Modifier.width(8.dp))
                                    Icon(Icons.Default.Check, null, tint = UniKLBlue, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done", color = UniKLBlue) } },
        containerColor = UniKLCard
    )
}

@Composable
fun SemesterStatChip(label: String, value: String) {
    Surface(color = Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
        }
    }
}

@Composable
fun SectionHeader(title: String, color: Color) {
    Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color, modifier = Modifier.padding(vertical = 4.dp))
}

@Composable
fun AISuggestionCard(sug: FirebaseAppViewModel.AISuggestion, onAdd: () -> Unit) {
    val c = sug.course; val g = sug.group; val cat = categoryColor(c.category)
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = UniKLCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, UniKLOrange.copy(alpha = 0.4f))) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).clip(RoundedCornerShape(10.dp)).background(cat.copy(alpha = 0.15f)), Alignment.Center) {
                Text("${c.credits}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = cat)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(c.code, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = UniKLDark)
                    CategoryChip(c.category)
                }
                Text(c.name, fontSize = 12.sp, color = UniKLGray, maxLines = 1)
                Text("${g.day}  ${g.startTime}–${g.endTime}  ·  ${g.room}", fontSize = 11.sp, color = UniKLGray)
                if (c.prerequisite != null) Text("Prereq: ${c.prerequisite} ✓", fontSize = 10.sp, color = UniKLGreen)
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (sug.usedNN) {
                        Surface(color = UniKLGreen.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                            Text("NN ${(sug.score * 100).toInt()}%", fontSize = 9.sp, color = UniKLGreen,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp))
                        }
                    }
                    Text("💡 ${sug.reason}", fontSize = 10.sp, color = UniKLOrange, fontWeight = FontWeight.Medium)
                }
            }
            IconButton(onClick = onAdd) { Icon(Icons.Default.AddCircle, null, tint = UniKLBlue) }
        }
    }
}

@Composable
fun CategoryChip(category: String) {
    val color = categoryColor(category)
    Surface(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
        Text(category, color = color, fontSize = 9.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp))
    }
}

@Composable
fun EnrolledCourseCard(course: Course, onDrop: () -> Unit, onSelectGroup: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val cat = categoryColor(course.category)
    val grp = course.groups.firstOrNull { it.groupCode == course.selectedGroupCode } ?: course.groups.firstOrNull()

    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = UniKLCard)) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(42.dp).clip(RoundedCornerShape(10.dp)).background(cat.copy(alpha = 0.12f)), Alignment.Center) {
                    Text("${course.credits}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = cat)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(course.code, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = UniKLDark)
                        CategoryChip(course.category)
                    }
                    Text(course.name, fontSize = 12.sp, color = UniKLGray, maxLines = 1)
                    if (grp != null) Text("${grp.day} ${grp.startTime}–${grp.endTime} · ${course.selectedGroupCode} · ${grp.room}", fontSize = 11.sp, color = UniKLGray)
                }
                if (course.groups.size > 1) {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null, tint = UniKLBlue)
                    }
                }
                IconButton(onClick = onDrop) { Icon(Icons.Default.RemoveCircle, null, tint = UniKLRed.copy(alpha = 0.8f)) }
            }
            AnimatedVisibility(expanded) {
                Column(Modifier.padding(top = 8.dp)) {
                    Text("Choose a group:", fontSize = 11.sp, color = UniKLGray, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    course.groups.forEach { g ->
                        val selected = g.groupCode == course.selectedGroupCode
                        Surface(
                            color = if (selected) UniKLBlue.copy(alpha = 0.12f) else UniKLBg,
                            shape = RoundedCornerShape(8.dp),
                            border = if (selected) androidx.compose.foundation.BorderStroke(1.dp, UniKLBlue) else null,
                            onClick = { onSelectGroup(g.groupCode) },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                        ) {
                            Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(g.groupCode, fontWeight = FontWeight.Bold, color = if (selected) UniKLBlue else UniKLDark, fontSize = 12.sp)
                                Spacer(Modifier.width(8.dp))
                                Text("${g.day} ${g.startTime}-${g.endTime} · ${g.lecturer}", fontSize = 11.sp, color = UniKLGray)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddCourseDialog(onDismiss: () -> Unit, onConfirm: (String, String, Int, String, String, String, String, String, String) -> Unit) {
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var credits by remember { mutableStateOf("3") }
    var category by remember { mutableStateOf("CORE") }
    var day by remember { mutableStateOf("Monday") }
    var start by remember { mutableStateOf("08:00") }
    var end by remember { mutableStateOf("10:00") }
    var room by remember { mutableStateOf("") }
    var lecturer by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = UniKLCard)) {
            Column(Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Add Course Manually", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = UniKLDark)
                Text("Custom courses are not yet stored in database", fontSize = 11.sp, color = UniKLGray)

                DialogField("Course Code", code) { code = it }
                DialogField("Course Name", name) { name = it }
                DialogField("Credit Hours", credits) { credits = it }

                Text("Category", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = UniKLDark)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("CORE", "MPU", "UCS", "ELECTIVE").forEach { c ->
                        FilterChip(selected = category == c, onClick = { category = c },
                            label = { Text(c, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = categoryColor(c), selectedLabelColor = Color.White))
                    }
                }

                Text("Day", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = UniKLDark)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    DAYS_ORDER.forEach { d ->
                        FilterChip(selected = day == d, onClick = { day = d },
                            label = { Text(d.take(3), fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = UniKLBlue, selectedLabelColor = Color.White))
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(Modifier.weight(1f)) { DialogField("Start", start) { start = it } }
                    Column(Modifier.weight(1f)) { DialogField("End", end) { end = it } }
                }
                DialogField("Room", room) { room = it }
                DialogField("Lecturer", lecturer) { lecturer = it }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismiss, Modifier.weight(1f)) { Text("Cancel", color = UniKLGray) }
                    Button(onClick = {
                        if (code.isNotBlank() && name.isNotBlank())
                            onConfirm(code, name, credits.toIntOrNull() ?: 3, category, day, start, end, room, lecturer)
                    },
                        Modifier.weight(1f), shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = UniKLBlue),
                        enabled = code.isNotBlank() && name.isNotBlank()) { Text("Add") }
                }
            }
        }
    }
}

@Composable
fun ManualAddDialog(
    courses: List<Course>,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    val available = courses.filter {
        it.status == CourseStatus.PENDING && it.groups.isNotEmpty()
    }
    val byCategory = available.groupBy { it.category }

    var selectedCourse by remember { mutableStateOf<Course?>(null) }
    var selectedGroup by remember { mutableStateOf<String?>(null) }
    var courseMenuOpen by remember { mutableStateOf(false) }
    var groupMenuOpen by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = UniKLCard)) {
            Column(
                Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Add Course", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = UniKLDark)
                Text("Pick a course and a class group.", fontSize = 11.sp, color = UniKLGray)

                Text("Course", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = UniKLDark)
                Box {
                    Surface(
                        color = UniKLBg, shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, UniKLBlue.copy(alpha = 0.4f)),
                        onClick = { courseMenuOpen = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                selectedCourse?.let { "${it.code} — ${it.name}" } ?: "Select a course",
                                fontSize = 13.sp,
                                color = if (selectedCourse == null) UniKLGray else UniKLDark,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(Icons.Default.ArrowDropDown, null, tint = UniKLBlue)
                        }
                    }
                    DropdownMenu(
                        expanded = courseMenuOpen,
                        onDismissRequest = { courseMenuOpen = false },
                        modifier = Modifier.heightIn(max = 360.dp)
                    ) {
                        if (available.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No available courses", color = UniKLGray) },
                                onClick = { courseMenuOpen = false }
                            )
                        }
                        byCategory.forEach { (category, list) ->
                            Text(
                                category,
                                fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                color = categoryColor(category),
                                modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 2.dp)
                            )
                            list.forEach { course ->
                                DropdownMenuItem(
                                    text = {
                                        Text("${course.code} — ${course.name}  (${course.credits}cr)",
                                            fontSize = 12.sp)
                                    },
                                    onClick = {
                                        selectedCourse = course
                                        selectedGroup = null
                                        courseMenuOpen = false
                                    }
                                )
                            }
                        }
                    }
                }

                if (selectedCourse != null) {
                    Text("Class Group", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = UniKLDark)
                    Box {
                        Surface(
                            color = UniKLBg, shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, UniKLBlue.copy(alpha = 0.4f)),
                            onClick = { groupMenuOpen = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val g = selectedCourse!!.groups.firstOrNull { it.groupCode == selectedGroup }
                            Row(
                                Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    g?.let { "${it.groupCode} · ${it.day} ${it.startTime}–${it.endTime}" }
                                        ?: "Select a group",
                                    fontSize = 13.sp,
                                    color = if (g == null) UniKLGray else UniKLDark,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(Icons.Default.ArrowDropDown, null, tint = UniKLBlue)
                            }
                        }
                        DropdownMenu(
                            expanded = groupMenuOpen,
                            onDismissRequest = { groupMenuOpen = false }
                        ) {
                            selectedCourse!!.groups.forEach { grp ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text("${grp.groupCode} · ${grp.day} ${grp.startTime}–${grp.endTime}",
                                                fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                            Text("${grp.room} · ${grp.lecturer}",
                                                fontSize = 10.sp, color = UniKLGray)
                                        }
                                    },
                                    onClick = {
                                        selectedGroup = grp.groupCode
                                        groupMenuOpen = false
                                    }
                                )
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismiss, Modifier.weight(1f)) {
                        Text("Cancel", color = UniKLGray)
                    }
                    Button(
                        onClick = {
                            val c = selectedCourse; val g = selectedGroup
                            if (c != null && g != null) onConfirm(c.code, g)
                        },
                        Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = UniKLBlue),
                        enabled = selectedCourse != null && selectedGroup != null
                    ) { Text("Add") }
                }
            }
        }
    }
}

@Composable
fun DialogField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = onChange,
        label = { Text(label, fontSize = 12.sp) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp), singleLine = true, colors = uniKLFieldColors())
}

// ─────────────────────────────────────────────
// COURSE MANAGEMENT
// ─────────────────────────────────────────────

@Composable
fun CourseManagementScreen(vm: FirebaseAppViewModel) {
    val all by vm.courses.collectAsState()
    val profileState by vm.profile.collectAsState()
    val p = profileState ?: EMPTY_PROFILE
    var filterCategory by remember { mutableStateOf<String?>(null) }
    var filterStatus   by remember { mutableStateOf<CourseStatus?>(null) }

    val filtered = all.filter { c ->
        (filterCategory == null || c.category == filterCategory) &&
                (filterStatus == null || c.status == filterStatus)
    }

    val listState = rememberLazyListState()
    var progressExpanded by remember { mutableStateOf(true) }

    // Auto-collapse the Academic Progress panel ONLY when scrolling down.
    // Once collapsed, user must tap header to expand again.
    var prevIndex by remember { mutableStateOf(0) }
    var prevOffset by remember { mutableStateOf(0) }
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                if (!(index == 0 && offset == 0)) {
                    val scrollingDown = index > prevIndex || (index == prevIndex && offset > prevOffset)
                    if (scrollingDown) {
                        progressExpanded = false
                    }
                }
                prevIndex = index
                prevOffset = offset
            }
    }

    Column(Modifier.fillMaxSize().background(UniKLBg)) {
        // Progress card — tap header to expand/collapse
        Card(Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = UniKLCard)) {
            Column(Modifier.padding(16.dp)) {
                val totalDone = vm.completedCredits + vm.currentSemesterCredits
                val progress = totalDone.toFloat() / p.totalCreditsRequired

                Row(Modifier.fillMaxWidth().clickable { progressExpanded = !progressExpanded },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Academic Progress", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = UniKLDark)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("${(progress * 100).toInt()}%", fontSize = 13.sp, color = UniKLGreen, fontWeight = FontWeight.Bold)
                        Icon(if (progressExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (progressExpanded) "Collapse" else "Expand", tint = UniKLGray)
                    }
                }
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                    color = UniKLGreen, trackColor = UniKLBg)

                AnimatedVisibility(visible = progressExpanded) {
                    Column {
                        Spacer(Modifier.height(6.dp))
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text("${(progress * 100).toInt()}% complete", fontSize = 12.sp, color = UniKLGray)
                            Text("$totalDone / ${p.totalCreditsRequired} credits",
                                fontSize = 12.sp, color = UniKLGreen, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(Modifier.height(16.dp))

                        // Category breakdown
                        Text("Breakdown by Category", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = UniKLDark)
                        Spacer(Modifier.height(8.dp))
                        CATEGORY_REQUIREMENTS.forEach { (cat, req) ->
                            CategoryProgressBar(cat, vm.creditsByCategory(cat), req)
                        }
                    }
                }
            }
        }

        // Filters
        Column(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
            Text("Filter by category:", fontSize = 11.sp, color = UniKLGray)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChipMini("All", filterCategory == null) { filterCategory = null }
                CATEGORY_REQUIREMENTS.keys.forEach { cat ->
                    FilterChipMini(cat, filterCategory == cat, categoryColor(cat)) { filterCategory = cat }
                }
            }
            Spacer(Modifier.height(4.dp))
            Text("Filter by status:", fontSize = 11.sp, color = UniKLGray)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChipMini("All", filterStatus == null) { filterStatus = null }
                FilterChipMini("Completed", filterStatus == CourseStatus.PASS, UniKLGreen) { filterStatus = CourseStatus.PASS }
                FilterChipMini("Transferred", filterStatus == CourseStatus.CT, UniKLBlue) { filterStatus = CourseStatus.CT }
                FilterChipMini("Taking", filterStatus == CourseStatus.ST, UniKLOrange) { filterStatus = CourseStatus.ST }
                FilterChipMini("Pending", filterStatus == CourseStatus.PENDING, UniKLGray) { filterStatus = CourseStatus.PENDING }
            }
        }

        Text("${filtered.size} course(s)", fontSize = 11.sp, color = UniKLGray,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))

        LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 24.dp)) {
            items(filtered) { course ->
                ManagementCourseCard(course, onCycleStatus = {
                    val next = when (course.status) {
                        CourseStatus.PENDING -> CourseStatus.ST
                        CourseStatus.ST      -> CourseStatus.PASS
                        CourseStatus.PASS    -> CourseStatus.CT
                        CourseStatus.CT      -> CourseStatus.PENDING
                    }
                    vm.setStatus(course.code, next)
                })
            }
        }
    }
}

@Composable
fun CategoryProgressBar(category: String, done: Int, required: Int) {
    val color = categoryColor(category)
    val progress = if (required == 0) 0f else (done.toFloat() / required).coerceIn(0f, 1f)
    Column(Modifier.padding(vertical = 4.dp)) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(color))
                Text(category, fontSize = 11.sp, color = UniKLDark, fontWeight = FontWeight.SemiBold)
            }
            Text("$done / $required", fontSize = 11.sp, color = color, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(2.dp))
        LinearProgressIndicator(progress = { progress },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = color, trackColor = UniKLBg)
    }
}

@Composable
fun FilterChipMini(label: String, selected: Boolean, color: Color = UniKLBlue, onClick: () -> Unit) {
    Surface(color = if (selected) color else UniKLCard,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) color else UniKLBg),
        onClick = onClick) {
        Text(label, fontSize = 11.sp, color = if (selected) Color.White else UniKLDark,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
    }
}

@Composable
fun ManagementCourseCard(course: Course, onCycleStatus: () -> Unit) {
    val statusInfo = when (course.status) {
        CourseStatus.PASS    -> Triple(UniKLGreen, "PASS", "✓")
        CourseStatus.CT      -> Triple(UniKLBlue, "CT", "↻")
        CourseStatus.ST      -> Triple(UniKLOrange, "TAKING", "▶")
        CourseStatus.PENDING -> Triple(UniKLGray, "PENDING", "○")
    }
    val (color, label, icon) = statusInfo
    val cat = categoryColor(course.category)

    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (course.status == CourseStatus.PASS || course.status == CourseStatus.CT)
                color.copy(alpha = 0.06f) else UniKLCard)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // Status icon (tap to cycle)
            Surface(color = color.copy(alpha = 0.15f), shape = CircleShape, onClick = onCycleStatus) {
                Box(Modifier.size(36.dp), Alignment.Center) {
                    Text(icon, fontSize = 16.sp, color = color, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(course.code, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                        color = if (course.status == CourseStatus.PASS || course.status == CourseStatus.CT) color else UniKLDark,
                        textDecoration = if (course.status == CourseStatus.PASS || course.status == CourseStatus.CT)
                            TextDecoration.LineThrough else TextDecoration.None)
                    CategoryChip(course.category)
                    Surface(color = cat.copy(alpha = 0.12f), shape = RoundedCornerShape(4.dp)) {
                        Text("${course.credits}cr", fontSize = 9.sp, color = cat,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp))
                    }
                }
                Text(course.name, fontSize = 12.sp, color = UniKLGray, maxLines = 2)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Y${course.year}·S${course.semester}", fontSize = 10.sp, color = UniKLGray)
                    if (course.prerequisite != null) Text("Prereq: ${course.prerequisite}", fontSize = 10.sp, color = UniKLOrange)
                    if (course.isOptional) Text("Optional", fontSize = 10.sp, color = UniKLGray)
                }
            }
            Text(label, fontSize = 10.sp, color = color, fontWeight = FontWeight.Bold)
        }
    }
}

// ─────────────────────────────────────────────
// PROFILE SCREEN
// ─────────────────────────────────────────────

@Composable
fun ProfileScreen(vm: FirebaseAppViewModel) {
    val profileState by vm.profile.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val p = profileState

    if (isLoading || p == null) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = UniKLBlue)
                Spacer(Modifier.height(12.dp))
                Text("Loading profile from database...", color = UniKLGray, fontSize = 13.sp)
            }
        }
        return
    }

    Column(Modifier.fillMaxSize().background(UniKLBg).verticalScroll(rememberScrollState())) {
        // Avatar header
        Box(Modifier.fillMaxWidth().background(UniKLBlue).padding(24.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()) {
                Box(Modifier.size(80.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.25f)),
                    Alignment.Center) {
                    Text(p.name.split(" ").take(2).joinToString("") { it.take(1) },
                        color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(12.dp))
                Text(p.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Text("ID: ${p.studentId}", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            }
        }

        Card(Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = UniKLCard)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Academic Info", fontWeight = FontWeight.Bold, color = UniKLDark)
                ProfileRow("Programme", p.programme)
                ProfileRow("Institute", p.institute)
                ProfileRow("Intake", p.intake)
                ProfileRow("Current Semester", p.currentSemester.toString())
                ProfileRow("Total Credits", "${p.totalCreditsRequired}")
                ProfileRow("Academic Advisor", p.academicAdvisor)
            }
        }

        Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = UniKLCard)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Credit Summary", fontWeight = FontWeight.Bold, color = UniKLDark)
                ProfileRow("Completed", "${vm.completedCredits} cr", UniKLGreen)
                ProfileRow("This Semester", "${vm.currentSemesterCredits} cr", UniKLOrange)
                ProfileRow("Remaining", "${p.totalCreditsRequired - vm.completedCredits - vm.currentSemesterCredits} cr", UniKLBlue)
            }
        }
        Spacer(Modifier.height(12.dp))
        ClassReminderCard(vm)
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun ClassReminderCard(vm: FirebaseAppViewModel) {
    val context = LocalContext.current
    val courses by vm.courses.collectAsState()
    val enrolledCount = courses.count { it.status == CourseStatus.ST }
    var hasPermission by remember { mutableStateOf(ClassReminders.hasNotificationPermission(context)) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        Toast.makeText(
            context,
            if (granted) "Notifications enabled ✓" else "Permission denied. Enable in Settings.",
            Toast.LENGTH_SHORT
        ).show()
    }

    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = UniKLCard)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Notifications, null, tint = UniKLOrange)
                Text("Class Reminders", fontWeight = FontWeight.Bold, color = UniKLDark)
            }
            Text("Get a notification ${ClassReminders.MINUTES_BEFORE} minutes before each enrolled class starts.",
                fontSize = 11.sp, color = UniKLGray)

            if (!hasPermission) {
                Button(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        else hasPermission = true
                    },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = UniKLBlue)
                ) {
                    Icon(Icons.Default.Notifications, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Enable Notifications", fontSize = 13.sp)
                }
            } else {
                Surface(color = UniKLGreen.copy(alpha = 0.08f), shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.CheckCircle, null, tint = UniKLGreen, modifier = Modifier.size(18.dp))
                        Text("Enabled · $enrolledCount enrolled course${if (enrolledCount == 1) "" else "s"}",
                            fontSize = 12.sp, color = UniKLDark)
                    }
                }
                Button(
                    onClick = {
                        ClassReminders.scheduleTestNotification(context)
                        Toast.makeText(context, "Test notification in 5 seconds…", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = UniKLOrange)
                ) {
                    Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Test Notification (5s)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            val n = ClassReminders.scheduleAllReminders(context, courses)
                            Toast.makeText(context,
                                if (n > 0) "Scheduled $n class reminders ✓" else "No enrolled courses",
                                Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, UniKLBlue)
                    ) { Text("Schedule Week", fontSize = 11.sp, color = UniKLBlue) }
                    OutlinedButton(
                        onClick = {
                            ClassReminders.cancelAll(context, courses)
                            Toast.makeText(context, "All reminders cancelled", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, UniKLRed.copy(alpha = 0.5f))
                    ) { Text("Cancel All", fontSize = 11.sp, color = UniKLRed) }
                }
            }
        }
    }
}

@Composable
fun ProfileRow(label: String, value: String, valueColor: Color = UniKLDark) {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
        Text(label, fontSize = 12.sp, color = UniKLGray)
        Text(value, fontSize = 12.sp, color = valueColor, fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth(0.6f))
    }
}

// ─────────────────────────────────────────────
// STUDY TASKS
// ─────────────────────────────────────────────

@Composable
fun StudyPlannerScreen(vm: FirebaseAppViewModel) {
    val tasks by vm.tasks.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    val pending = tasks.filter { !it.isCompleted }
    val completed = tasks.filter { it.isCompleted }

    Box(Modifier.fillMaxSize()) {
        if (tasks.isEmpty()) {
            EmptyState("No tasks yet.\nTap + to start planning!", Icons.Default.Task)
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp)) {
                item {
                    val done = completed.size; val total = tasks.size
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = UniKLCard)) {
                        Column(Modifier.padding(16.dp)) {
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                Text("Task Progress", fontWeight = FontWeight.Bold, color = UniKLDark)
                                Text("$done / $total", color = UniKLBlue, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(progress = { if (total == 0) 0f else done.toFloat() / total },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = UniKLBlue, trackColor = UniKLBg)
                        }
                    }
                }
                if (pending.isNotEmpty()) {
                    item { SectionHeader("📋 Pending", UniKLOrange) }
                    items(pending) { task -> TaskCard(task, { vm.toggleTask(task.id) }, { vm.deleteTask(task.id) }) }
                }
                if (completed.isNotEmpty()) {
                    item { SectionHeader("✅ Completed", UniKLGreen) }
                    items(completed) { task -> TaskCard(task, { vm.toggleTask(task.id) }, { vm.deleteTask(task.id) }) }
                }
            }
        }
        FloatingActionButton(onClick = { showDialog = true },
            containerColor = UniKLBlue, contentColor = Color.White,
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp)) {
            Icon(Icons.Default.Add, "Add Task")
        }
    }

    if (showDialog) {
        AddTaskDialog(onDismiss = { showDialog = false },
            onConfirm = { t, c, d -> vm.addTask(t, c, d); showDialog = false })
    }
}

@Composable
fun TaskCard(task: StudyTask, onToggle: () -> Unit, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) UniKLGreen.copy(alpha = 0.07f) else UniKLCard)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = task.isCompleted, onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = UniKLGreen, uncheckedColor = UniKLGray))
            Column(Modifier.weight(1f).padding(horizontal = 6.dp)) {
                Text(task.title, fontWeight = FontWeight.Bold, color = UniKLDark, fontSize = 14.sp,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(task.courseName, fontSize = 12.sp, color = UniKLGray)
                    if (task.dueDate.isNotBlank()) {
                        Surface(color = UniKLOrange.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                            Text("Due: ${task.dueDate}", fontSize = 11.sp, color = UniKLOrange,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
            }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = UniKLGray) }
        }
    }
}

@Composable
fun AddTaskDialog(onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss,
        title = { Text("New Task", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DialogField("Task Title", title) { title = it }
                DialogField("Course Name", course) { course = it }
                DialogField("Due Date (e.g. 25 May)", dueDate) { dueDate = it }
            }
        },
        confirmButton = {
            Button(onClick = { if (title.isNotBlank()) onConfirm(title, course, dueDate) },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = UniKLBlue),
                shape = RoundedCornerShape(8.dp)) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = UniKLGray) } },
        containerColor = UniKLCard)
}

// ─────────────────────────────────────────────
// SHARED COMPONENTS
// ─────────────────────────────────────────────

@Composable
fun EmptyState(message: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, Modifier.size(56.dp), tint = UniKLGray.copy(alpha = 0.4f))
            Spacer(Modifier.height(16.dp))
            Text(message, textAlign = TextAlign.Center, color = UniKLGray,
                style = MaterialTheme.typography.bodyMedium, lineHeight = 22.sp)
        }
    }
}

@Composable
fun SmallLogo(white: Boolean = false) {
    val textColor = if (white) Color.White else UniKLDark
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(R.drawable.ic_mascot_logo),
            contentDescription = null,
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(6.dp))
                .padding(end = 6.dp)
        )
        Surface(
            color = if (white) Color.White.copy(alpha = 0.25f) else UniKLBlue,
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                "Uni", Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
        }
        Text(
            "KL", color = UniKLOrange, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(start = 2.dp), letterSpacing = 0.5.sp
        )
        Text(" Planner", color = textColor, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun LogoComponent(isSmall: Boolean) {
    val scale by animateFloatAsState(
        targetValue = if (isSmall) 0.65f else 1.0f,
        animationSpec = tween(1000), label = "scale"
    )
    Box(Modifier.scale(scale), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Mascot icon with navy shadow ring
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0D1860),
                shadowElevation = 8.dp
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_mascot_logo),
                    contentDescription = "UniKL Mascot",
                    modifier = Modifier.size(88.dp).clip(RoundedCornerShape(20.dp))
                )
            }
            Spacer(Modifier.height(18.dp))
            // "UniKL Planner" wordmark
            Row(verticalAlignment = Alignment.Bottom) {
                Surface(
                    color = UniKLBlue,
                    shape = RoundedCornerShape(6.dp),
                    shadowElevation = 2.dp
                ) {
                    Text(
                        "Uni", Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        color = Color.White, fontSize = 38.sp,
                        fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp
                    )
                }
                Text(
                    "KL", color = UniKLOrange, fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            Text(
                "Planner", color = UniKLDark, fontSize = 28.sp,
                fontWeight = FontWeight.Bold, letterSpacing = 4.sp,
                modifier = Modifier.padding(top = 2.dp, start = 4.dp)
            )
            Text(
                "Your Academic Companion", color = UniKLGray,
                fontSize = 12.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}