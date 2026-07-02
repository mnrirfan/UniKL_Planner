package com.example.uniklplanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uniklplanner.dataconnect.DefaultConnector
import com.example.uniklplanner.dataconnect.execute
import com.example.uniklplanner.dataconnect.instance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import android.app.Application
import androidx.lifecycle.AndroidViewModel


/**
 * ViewModel that reads/writes data from the Firebase Data Connect emulator.
 * Talks to localhost:9399 via Android's special 10.0.2.2 IP.
 */
class FirebaseAppViewModel(app: Application) : AndroidViewModel(app) {

    // Neural network recommender (loaded once; null if it fails to load)
    private val recommender: CourseRecommender? by lazy {
        CourseRecommender.tryLoad(getApplication())
    }

    // ── The connector (lazily initialized) ──
    private val connector by lazy {
        val c = DefaultConnector.instance
        c.dataConnect.useEmulator(host = "localhost", port = 9399)
// then on next line:
// (no extra config needed if useEmulator already defaults to plaintext)
        c
    }

    // ── State exposed to UI ──
    private val _profile = MutableStateFlow<StudentProfile?>(null)
    val profile: StateFlow<StudentProfile?> = _profile.asStateFlow()

    // Editable current semester (in-memory; drives AI re-scoring live)
    private val _currentSemester = MutableStateFlow(7)
    val currentSemester: StateFlow<Int> = _currentSemester.asStateFlow()

    fun setCurrentSemester(sem: Int) {
        if (sem in 1..8) _currentSemester.value = sem
    }

    private val _courses = MutableStateFlow<List<Course>>(emptyList())
    val courses: StateFlow<List<Course>> = _courses.asStateFlow()

    private val _tasks = MutableStateFlow<List<StudyTask>>(emptyList())
    val tasks: StateFlow<List<StudyTask>> = _tasks.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        android.util.Log.e("UNIKL_TEST", "🔥 FirebaseAppViewModel CREATED!")
        loadAllData()
    }

    // ── Load everything ──
    fun loadAllData() {
        android.util.Log.e("UNIKL_TEST", "🔥 loadAllData() STARTED")
        viewModelScope.launch {
            android.util.Log.e("UNIKL_TEST", "🔥 launched coroutine")
            _isLoading.value = true
            _errorMessage.value = null
            try {
                loadProfile()
                loadCoursesWithEnrollments()
                loadTasks()
            } catch (e: Exception) {
                android.util.Log.e("UNIKL_TEST", "🔥 ERROR: ${e.message}", e)
                _errorMessage.value = "Failed to load data: ${e.message}"
                e.printStackTrace()
            } finally {
                android.util.Log.e("UNIKL_TEST", "🔥 finally block - isLoading=false")
                _isLoading.value = false
            }
        }
    }

    // ── Load profile ──
    private suspend fun loadProfile() {
        try {
            val result = connector.getStudentProfile.execute()
            val s = result.data.student
            if (s != null) {
                _profile.value = StudentProfile(
                    studentId = s.studentId,
                    name = s.name,
                    programme = s.programme,
                    intake = s.intake,
                    currentSemester = s.currentSemester,
                    totalCreditsRequired = s.totalCreditsRequired,
                    institute = s.institute,
                    academicAdvisor = s.academicAdvisor
                )
                _currentSemester.value = s.currentSemester.coerceIn(1, 8)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ── Load courses + enrollments merged ──
    private suspend fun loadCoursesWithEnrollments() {
        val allCoursesResult = connector.listAllCourses.execute()
        val enrollmentsResult = connector.getMyEnrollments.execute()
        val allGroupsResult = connector.listAllGroups.execute()
        android.util.Log.e("UNIKL_GROUPS", "groups from DB: ${allGroupsResult.data.scheduleGroups.size}")

        // Index enrollments by course code
        val enrollmentByCode = enrollmentsResult.data.enrollments
            .associateBy { it.course.code }

        // Group all schedule groups by their course code
        val groupsByCourse = allGroupsResult.data.scheduleGroups
            .groupBy { it.course.code }
            .mapValues { (_, list) ->
                list.map { g ->
                    ScheduleGroup(
                        groupCode = g.groupCode,
                        day = g.day,
                        startTime = g.startTime,
                        endTime = g.endTime,
                        room = g.room,
                        lecturer = g.lecturer
                    )
                }
            }
        android.util.Log.e("UNIKL_GROUPS", "groupsByCourse keys: ${groupsByCourse.keys.take(5)} ... total ${groupsByCourse.size}")

        // Merge into single Course list
        _courses.value = allCoursesResult.data.courses.map { c ->
            val enr = enrollmentByCode[c.code]
            val status = when (enr?.status) {
                "PASS" -> CourseStatus.PASS
                "CT"   -> CourseStatus.CT
                "ST"   -> CourseStatus.ST
                else   -> CourseStatus.PENDING
            }
            // All available groups for this course (from the DB)
            val courseGroups = groupsByCourse[c.code] ?: emptyList()
            // Which group is the student enrolled in (if any)
            val enrolledGroupCode = enr?.group?.groupCode
                ?: courseGroups.firstOrNull()?.groupCode

            Course(
                code = c.code,
                name = c.name,
                credits = c.credits,
                category = c.category,
                year = c.year,
                semester = c.semester,
                prerequisite = c.prerequisite,
                isOptional = c.isOptional,
                status = status,
                selectedGroupCode = enrolledGroupCode,
                groups = courseGroups
            )
        }
    }

    // ── Load study tasks ──
    private suspend fun loadTasks() {
        val result = connector.getMyTasks.execute()
        _tasks.value = result.data.studyTasks.map {
            StudyTask(
                id = it.id.toString(),
                title = it.title,
                courseName = it.courseName,
                dueDate = it.dueDate,
                isCompleted = it.isCompleted
            )
        }
    }

    // ── Computed properties ──
    fun enrolled() = _courses.value.filter { it.status == CourseStatus.ST }
    fun completed() = _courses.value.filter {
        it.status == CourseStatus.PASS || it.status == CourseStatus.CT
    }

    val completedCredits: Int
        get() = completed().sumOf { it.credits }
    val currentSemesterCredits: Int
        get() = enrolled().sumOf { it.credits }

    fun creditsByCategory(category: String): Int =
        completed().filter { it.category == category }.sumOf { it.credits }

    fun remainingByCategory(category: String): Int =
        (CATEGORY_REQUIREMENTS[category] ?: 0) - creditsByCategory(category)

    // ── Task mutations ──
    fun addTask(title: String, course: String, date: String) {
        viewModelScope.launch {
            try {
                connector.addStudyTask.execute(
                    title = title,
                    courseName = course,
                    dueDate = date
                )
                loadTasks()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add task: ${e.message}"
            }
        }
    }

    fun toggleTask(id: String) {
        viewModelScope.launch {
            try {
                val task = _tasks.value.firstOrNull { it.id == id } ?: return@launch
                connector.toggleTaskCompletion.execute(
                    taskId = UUID.fromString(id),
                    isCompleted = !task.isCompleted
                )
                loadTasks()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to toggle task: ${e.message}"
            }
        }
    }

    fun deleteTask(id: String) {
        viewModelScope.launch {
            try {
                connector.deleteTask.execute(taskId = UUID.fromString(id))
                loadTasks()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete task: ${e.message}"
            }
        }
    }

    // ── AI smart scheduling — runs locally on cached data ──
    // AISuggestion now carries the NN suitability score (0..1)
    data class AISuggestion(
        val course: Course,
        val group: ScheduleGroup,
        val reason: String,
        val priority: Int,
        val score: Float = 0f,        // neural-network suitability
        val usedNN: Boolean = false   // true if NN produced the score
    )

    fun getAISuggestions(): List<AISuggestion> {
        val all = _courses.value
        val sem = _currentSemester.value
        val enrolledList = all.filter { it.status == CourseStatus.ST }
        val currentLoad = enrolledList.sumOf { it.credits }
        val passedCodes = all
            .filter { it.status == CourseStatus.PASS || it.status == CourseStatus.CT }
            .map { it.code }.toSet()
        val mpu34Passed = all.any {
            it.code.startsWith("MPU34") && it.status == CourseStatus.PASS
        }

        val candidates = all.filter { c ->
            c.status == CourseStatus.PENDING &&
                    (c.prerequisite == null || c.prerequisite in passedCodes) &&
                    !(c.code.startsWith("MPU34") && mpu34Passed) &&
                    remainingByCategory(c.category).coerceAtLeast(0) > 0   // skip fully-completed categories
        }

        val scored = candidates.mapNotNull { candidate ->
            val clashFreeGroup = candidate.groups.firstOrNull { g ->
                enrolledList.none { e ->
                    val eg = e.groups.firstOrNull { it.groupCode == e.selectedGroupCode }
                        ?: e.groups.firstOrNull()
                    eg != null && eg.day == g.day &&
                            timesOverlap(eg.startTime, eg.endTime, g.startTime, g.endTime)
                }
            } ?: candidate.groups.firstOrNull() ?: return@mapNotNull null

            val prereqMet = candidate.prerequisite == null || candidate.prerequisite in passedCodes
            val catReq = CATEGORY_REQUIREMENTS[candidate.category] ?: 0
            val catDeficit = remainingByCategory(candidate.category).coerceAtLeast(0)

            val nn = recommender
            if (nn != null) {
                val features = CourseRecommender.buildFeatures(
                    course = candidate,
                    currentSemester = sem,
                    prereqMet = prereqMet,
                    categoryDeficitCredits = catDeficit,
                    categoryRequirement = catReq,
                    currentLoadCredits = currentLoad
                )
                val score = nn.score(features)
                val reason = nnReason(candidate, score, currentLoad)
                AISuggestion(
                    course = candidate,
                    group = clashFreeGroup,
                    reason = reason,
                    priority = 0,
                    score = score,
                    usedNN = true
                )
            } else {
                val (reason, priority) = buildReasonFallback(candidate, enrolledList)
                AISuggestion(
                    course = candidate,
                    group = clashFreeGroup,
                    reason = reason,
                    priority = priority,
                    score = 0f,
                    usedNN = false
                )
            }
        }

        return if (scored.any { it.usedNN }) {
            scored.sortedByDescending { it.score }
        } else {
            scored.sortedBy { it.priority }
        }
    }

    private fun nnReason(course: Course, score: Float, currentLoad: Int): String {
        val pct = (score * 100).toInt()
        val catRemaining = remainingByCategory(course.category).coerceAtLeast(0)
        val base = when {
            score >= 0.66f -> "Strong match"
            score >= 0.40f -> "Good fit"
            else           -> "Possible option"
        }
        val detail = when {
            course.category == "ELECTIVE" && catRemaining > 0 ->
                "elective credits still needed"
            catRemaining > 0 ->
                "$catRemaining ${course.category} credits remaining"
            currentLoad + course.credits > 20 ->
                "would exceed 20-credit cap"
            else ->
                "fits your current plan"
        }
        return "$base ($pct%) — $detail"
    }

    private fun buildReasonFallback(course: Course, enrolled: List<Course>): Pair<String, Int> {
        val currentCredits = enrolled.sumOf { it.credits }
        val catRemaining = remainingByCategory(course.category)
        return when {
            catRemaining > 0 && course.category == "CORE" ->
                "Need $catRemaining more CORE credits" to 1
            catRemaining > 0 && course.category == "ELECTIVE" ->
                "Only $catRemaining ELECTIVE credits left" to 2
            catRemaining > 0 && course.category == "MPU" && !course.isOptional ->
                "Required MPU subject" to 3
            catRemaining > 0 && course.category == "MPU" && course.isOptional ->
                "MPU choice subject" to 4
            currentCredits < 12 ->
                "Light load — adds balance" to 5
            course.credits <= 2 ->
                "Light 2-credit add" to 6
            else ->
                "Aligns with your pace" to 7
        }
    }

    private fun timesOverlap(s1: String, e1: String, s2: String, e2: String): Boolean {
        val start1 = timeToMinutes(s1); val end1 = timeToMinutes(e1)
        val start2 = timeToMinutes(s2); val end2 = timeToMinutes(e2)
        return start1 < end2 && start2 < end1
    }
    private fun timeToMinutes(t: String): Int {
        val p = t.split(":"); return p[0].toInt() * 60 + p[1].toInt()
    }

    // ── Course mutations ──
    fun enrollCourse(code: String, groupCode: String? = null) {
        viewModelScope.launch {
            try {
                connector.enrollCourse.execute(courseCode = code)
                loadCoursesWithEnrollments()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to enroll: ${e.message}"
            }
        }
    }

    fun setStatus(code: String, newStatus: CourseStatus) {
        // Optimistic local update — DB sync requires enrollment ID lookup, TODO
        _courses.value = _courses.value.map {
            if (it.code == code) it.copy(status = newStatus) else it
        }
    }

    fun dropCourse(code: String) {
        // Optimistic local update — DB delete requires enrollment ID lookup, TODO
        _courses.value = _courses.value.map {
            if (it.code == code) it.copy(status = CourseStatus.PENDING) else it
        }
    }

    // Manual add: enroll a course into a chosen group (local UI update + DB call)
    fun enrollByGroup(code: String, groupCode: String) {
        // Optimistic local update so it shows immediately
        _courses.value = _courses.value.map {
            if (it.code == code) it.copy(status = CourseStatus.ST, selectedGroupCode = groupCode)
            else it
        }
        // Fire the DB mutation in the background (best-effort)
        viewModelScope.launch {
            try {
                connector.enrollCourse.execute(courseCode = code)
            } catch (e: Exception) {
                android.util.Log.e("UNIKL_ENROLL", "bg enroll failed: ${e.message}")
            }
        }
    }

    fun selectGroup(code: String, groupCode: String) {
        // Optimistic local update — DB sync TODO
        _courses.value = _courses.value.map {
            if (it.code == code) it.copy(selectedGroupCode = groupCode) else it
        }
    }

    fun addCustomCourse(
        code: String, name: String, credits: Int, category: String,
        day: String, start: String, end: String, room: String, lecturer: String
    ) {
        _errorMessage.value = "Custom courses not supported with database yet"
    }
}