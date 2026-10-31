package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.InjazApplication
import com.example.data.model.PomodoroSession
import com.example.data.model.Task
import com.example.data.model.TaskCategory
import com.example.data.model.TaskPriority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class DayCompletionStat(
    val dayLabel: String,
    val completedCount: Int,
    val totalCount: Int
)

data class AnalyticsState(
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val pendingTasks: Int = 0,
    val completionRate: Float = 0f,
    val weeklyStats: List<DayCompletionStat> = emptyList(),
    val monthlyCompletionRate: Float = 0f,
    val categoryDistribution: Map<TaskCategory, Int> = emptyMap(),
    val priorityDistribution: Map<TaskPriority, Int> = emptyMap(),
    val focusMinutesTotal: Int = 0,
    val completedPomodoroSessions: Int = 0,
    val currentStreakDays: Int = 0,
    val urgentPendingTasks: List<Task> = emptyList()
)

class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as InjazApplication).repository

    private val _isLoading = kotlinx.coroutines.flow.MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        viewModelScope.launch {
            repository.allTasks.collect {
                _isLoading.value = false
            }
        }
    }

    val analyticsState: StateFlow<AnalyticsState> = combine(
        repository.allTasks,
        repository.allSessions
    ) { tasks, sessions ->
        computeAnalytics(tasks, sessions)
    }
        .flowOn(Dispatchers.Default) // Execute on background thread for zero UI stutter
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = AnalyticsState()
        )

    private fun computeAnalytics(tasks: List<Task>, sessions: List<PomodoroSession>): AnalyticsState {
        val total = tasks.size
        var completed = 0
        val catCounts = mutableMapOf<TaskCategory, Int>()
        val prioCounts = mutableMapOf<TaskPriority, Int>()

        for (task in tasks) {
            if (task.isCompleted) completed++
            catCounts[task.category] = (catCounts[task.category] ?: 0) + 1
            if (!task.isCompleted) {
                prioCounts[task.priority] = (prioCounts[task.priority] ?: 0) + 1
            }
        }

        val pending = total - completed
        val rate = if (total > 0) (completed.toFloat() / total.toFloat()) * 100f else 0f

        // Weekly stats for the last 7 days (Fast timestamp math)
        val weeklyStats = calculateWeeklyStats(tasks)

        // Monthly completion rate
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfMonth = cal.timeInMillis

        var monthTotal = 0
        var monthCompleted = 0
        for (task in tasks) {
            if (task.createdAt >= startOfMonth) {
                monthTotal++
                if (task.isCompleted) monthCompleted++
            }
        }
        val monthlyRate = if (monthTotal > 0) (monthCompleted.toFloat() / monthTotal.toFloat()) * 100f else rate

        // Total focus minutes
        var totalFocusMin = 0
        for (session in sessions) {
            totalFocusMin += session.durationMinutes
        }

        // Streak calculation (Fast bounded loop)
        val streak = calculateStreak(tasks)

        // Urgent pending tasks (Top 3)
        val urgent = tasks
            .asSequence()
            .filter { !it.isCompleted }
            .sortedWith(compareByDescending<Task> { it.priority.level }.thenBy { it.dueDate ?: Long.MAX_VALUE })
            .take(3)
            .toList()

        return AnalyticsState(
            totalTasks = total,
            completedTasks = completed,
            pendingTasks = pending,
            completionRate = rate,
            weeklyStats = weeklyStats,
            monthlyCompletionRate = monthlyRate,
            categoryDistribution = catCounts,
            priorityDistribution = prioCounts,
            focusMinutesTotal = totalFocusMin,
            completedPomodoroSessions = sessions.size,
            currentStreakDays = streak,
            urgentPendingTasks = urgent
        )
    }

    private fun calculateWeeklyStats(tasks: List<Task>): List<DayCompletionStat> {
        val arabicDayNames = listOf("الأحد", "الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت")
        val result = ArrayList<DayCompletionStat>(7)

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayMidnight = cal.timeInMillis
        val currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val oneDayMillis = 86400000L

        // Generate stats from 6 days ago up to today
        for (i in 6 downTo 0) {
            val dayStart = todayMidnight - (i * oneDayMillis)
            val dayEnd = dayStart + oneDayMillis

            // Calculate Arabic day label
            val dayIndex = (currentDayOfWeek - 1 - i) % 7
            val normalizedIndex = (dayIndex + 7) % 7
            val label = arabicDayNames[normalizedIndex]

            var completedOnDay = 0
            var activeOnDay = 0
            for (task in tasks) {
                val comp = task.completedAt
                if (comp != null && comp >= dayStart && comp < dayEnd) {
                    completedOnDay++
                }
                if (task.createdAt <= dayEnd && (comp == null || comp >= dayStart)) {
                    activeOnDay++
                }
            }

            result.add(
                DayCompletionStat(
                    dayLabel = label,
                    completedCount = completedOnDay,
                    totalCount = maxOf(activeOnDay, completedOnDay, 1)
                )
            )
        }
        return result
    }

    private fun calculateStreak(tasks: List<Task>): Int {
        val completedTasks = tasks.filter { it.isCompleted && it.completedAt != null }
        if (completedTasks.isEmpty()) return 0

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        var streak = 0
        val oneDayMillis = 86400000L
        var checkTime = cal.timeInMillis

        // If today has completion, streak starts today, else check yesterday
        val hasToday = completedTasks.any { (it.completedAt ?: 0) >= checkTime }
        if (!hasToday) {
            checkTime -= oneDayMillis
        }

        // Bounded to 365 days max to prevent infinite loops
        for (day in 0..365) {
            val dayStart = checkTime
            val dayEnd = dayStart + oneDayMillis
            val completedInDay = completedTasks.any { (it.completedAt ?: 0) in dayStart until dayEnd }
            if (completedInDay) {
                streak++
                checkTime -= oneDayMillis
            } else {
                break
            }
        }

        return streak
    }
}
