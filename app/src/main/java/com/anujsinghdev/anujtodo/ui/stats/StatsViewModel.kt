package com.anujsinghdev.anujtodo.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anujsinghdev.anujtodo.domain.model.FocusSession
import com.anujsinghdev.anujtodo.domain.repository.TodoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.util.Calendar
import javax.inject.Inject

data class UserStats(
    val level: Int,
    val title: String,
    val progress: Float,
    val totalHours: Float,
    val nextLevelHours: Int,
    val totalTasksCompleted: Int,
    val currentStreak: Int,
    val bestStreak: Int
)

data class ChartDataPoint(
    val label: String,
    val value: Int,
    val isToday: Boolean = false
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: TodoRepository
) : ViewModel() {

    private val _showCelebration = MutableStateFlow(false)
    val showCelebration = _showCelebration.asStateFlow()

    private var previousLevel: Int? = null

    // User Stats
    val userStats = combine(
        repository.getTotalFocusMinutes(),
        repository.getCompletedTaskCount(),
        repository.getFocusSessions(0, Long.MAX_VALUE)
    ) { totalMins, taskCount, sessions ->
        val stats = calculateStats(totalMins ?: 0, taskCount, sessions)
        if (previousLevel != null && stats.level > previousLevel!!) {
            _showCelebration.value = true
        }
        previousLevel = stats.level
        stats
    }.stateIn(viewModelScope, SharingStarted.Lazily, calculateStats(0, 0, emptyList()))

    // Weekly Data
    val weeklyData = repository.getFocusSessions(0, Long.MAX_VALUE).map { sessions ->
        processWeeklyData(sessions)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Monthly Data
    val monthlyData = repository.getFocusSessions(0, Long.MAX_VALUE).map { sessions ->
        processMonthlyData(sessions)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Yearly Data
    val yearlyData = repository.getFocusSessions(0, Long.MAX_VALUE).map { sessions ->
        processYearlyData(sessions)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // [NEW] Lifetime Data (Grouped by Year)
    val lifetimeData = repository.getFocusSessions(0, Long.MAX_VALUE).map { sessions ->
        processLifetimeData(sessions)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun onCelebrationShown() {
        _showCelebration.value = false
    }

    // --- Data Processing Helpers ---

    private fun processLifetimeData(sessions: List<FocusSession>): List<ChartDataPoint> {
        if (sessions.isEmpty()) return emptyList()

        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)

        // Find the year of the very first session
        val minTimestamp = sessions.minOf { it.timestamp }
        calendar.timeInMillis = minTimestamp
        val startYear = calendar.get(Calendar.YEAR)

        val years = mutableListOf<ChartDataPoint>()

        for (year in startYear..currentYear) {
            val start = getStartOfYear(year)
            val end = getEndOfYear(year)

            val yearMinutes = sessions.filter { it.timestamp in start..end }
                .sumOf { it.durationMinutes }

            years.add(ChartDataPoint(year.toString(), yearMinutes, year == currentYear))
        }
        // Ensure at least one bar if empty
        if (years.isEmpty()) {
            years.add(ChartDataPoint(currentYear.toString(), 0, true))
        }
        return years
    }

    private fun getStartOfYear(year: Int): Long {
        val c = Calendar.getInstance()
        c.set(Calendar.YEAR, year); c.set(Calendar.DAY_OF_YEAR, 1)
        c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    private fun getEndOfYear(year: Int): Long {
        val c = Calendar.getInstance()
        c.set(Calendar.YEAR, year); c.set(Calendar.MONTH, Calendar.DECEMBER); c.set(Calendar.DAY_OF_MONTH, 31)
        c.set(Calendar.HOUR_OF_DAY, 23); c.set(Calendar.MINUTE, 59); c.set(Calendar.SECOND, 59); c.set(Calendar.MILLISECOND, 999)
        return c.timeInMillis
    }

    // ... [Keep calculateStats, calculateStreaks, processWeeklyData, processMonthlyData, processYearlyData, getDayName, getMonthName, getStartOfDay, getEndOfDay exactly as they were] ...

    // (Adding these helpers back for completeness of the snippet)
    private fun calculateStats(totalMinutes: Int, tasksCompleted: Int, sessions: List<FocusSession>): UserStats {
        val minutesPerLevel = 600
        val currentLevel = (totalMinutes / minutesPerLevel) + 1
        val minutesInCurrentLevel = totalMinutes % minutesPerLevel
        val progress = minutesInCurrentLevel / minutesPerLevel.toFloat()
        val totalHours = totalMinutes / 60f
        val hoursForNextLevel = 10 - (minutesInCurrentLevel / 60)

        val title = when (currentLevel) {
            in 1..10 -> "Novice"; in 11..50 -> "Apprentice"; in 51..100 -> "Adept"
            in 101..300 -> "Expert"; in 301..600 -> "Master"; in 601..999 -> "Grandmaster"; else -> "Legend"
        }
        val (currentStreak, bestStreak) = calculateStreaks(sessions)
        return UserStats(currentLevel, title, progress, totalHours, hoursForNextLevel, tasksCompleted, currentStreak, bestStreak)
    }

    private fun calculateStreaks(sessions: List<FocusSession>): Pair<Int, Int> {
        if (sessions.isEmpty()) return Pair(0, 0)
        val sortedSessions = sessions.sortedBy { it.timestamp }
        val calendar = Calendar.getInstance()
        val daysWithActivity = sortedSessions.map { session ->
            calendar.timeInMillis = session.timestamp
            calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0); calendar.set(Calendar.SECOND, 0); calendar.set(Calendar.MILLISECOND, 0)
            calendar.timeInMillis
        }.distinct().sorted()

        var currentStreak = 0
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0); calendar.set(Calendar.SECOND, 0); calendar.set(Calendar.MILLISECOND, 0)
        var checkDate = calendar.timeInMillis
        while (daysWithActivity.contains(checkDate)) {
            currentStreak++
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            checkDate = calendar.timeInMillis
        }

        var bestStreak = 0
        var tempStreak = 1
        for (i in 1 until daysWithActivity.size) {
            if ((daysWithActivity[i] - daysWithActivity[i - 1]) / (1000 * 60 * 60 * 24) == 1L) {
                tempStreak++; bestStreak = maxOf(bestStreak, tempStreak)
            } else { tempStreak = 1 }
        }
        bestStreak = maxOf(bestStreak, tempStreak)
        return Pair(currentStreak, bestStreak)
    }

    private fun processWeeklyData(sessions: List<FocusSession>): List<ChartDataPoint> {
        val calendar = Calendar.getInstance()
        val days = mutableListOf<ChartDataPoint>()
        for (i in 6 downTo 0) {
            calendar.timeInMillis = System.currentTimeMillis(); calendar.add(Calendar.DAY_OF_YEAR, -i)
            val s = getStartOfDay(calendar); val e = getEndOfDay(calendar)
            val min = sessions.filter { it.timestamp in s..e }.sumOf { it.durationMinutes }
            val label = if (i == 0) "Today" else getDayName(calendar.get(Calendar.DAY_OF_WEEK))
            days.add(ChartDataPoint(label, min, i == 0))
        }
        return days
    }

    private fun processMonthlyData(sessions: List<FocusSession>): List<ChartDataPoint> {
        val calendar = Calendar.getInstance()
        val days = mutableListOf<ChartDataPoint>()
        for (i in 5 downTo 0) {
            calendar.timeInMillis = System.currentTimeMillis(); calendar.add(Calendar.DAY_OF_YEAR, -(i * 5))
            val s = getStartOfDay(calendar); calendar.add(Calendar.DAY_OF_YEAR, 4); val e = getEndOfDay(calendar)
            val min = sessions.filter { it.timestamp in s..e }.sumOf { it.durationMinutes }
            days.add(ChartDataPoint(if (i == 0) "Now" else "${i * 5}d", min, i == 0))
        }
        return days
    }

    private fun processYearlyData(sessions: List<FocusSession>): List<ChartDataPoint> {
        val calendar = Calendar.getInstance()
        val months = mutableListOf<ChartDataPoint>()
        for (i in 11 downTo 0) {
            calendar.timeInMillis = System.currentTimeMillis(); calendar.add(Calendar.MONTH, -i); calendar.set(Calendar.DAY_OF_MONTH, 1)
            val s = getStartOfDay(calendar); calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH)); val e = getEndOfDay(calendar)
            val min = sessions.filter { it.timestamp in s..e }.sumOf { it.durationMinutes }
            months.add(ChartDataPoint(getMonthName(calendar.get(Calendar.MONTH)), min, i == 0))
        }
        return months
    }

    private fun getDayName(day: Int) = when(day) { Calendar.MONDAY->"M"; Calendar.TUESDAY->"T"; Calendar.WEDNESDAY->"W"; Calendar.THURSDAY->"T"; Calendar.FRIDAY->"F"; Calendar.SATURDAY->"S"; Calendar.SUNDAY->"S"; else->"" }
    private fun getMonthName(month: Int) = when(month) { Calendar.JANUARY->"Jan"; Calendar.FEBRUARY->"Feb"; Calendar.MARCH->"Mar"; Calendar.APRIL->"Apr"; Calendar.MAY->"May"; Calendar.JUNE->"Jun"; Calendar.JULY->"Jul"; Calendar.AUGUST->"Aug"; Calendar.SEPTEMBER->"Sep"; Calendar.OCTOBER->"Oct"; Calendar.NOVEMBER->"Nov"; Calendar.DECEMBER->"Dec"; else->"" }
    private fun getStartOfDay(cal: Calendar): Long { val c = cal.clone() as Calendar; c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0); return c.timeInMillis }
    private fun getEndOfDay(cal: Calendar): Long { val c = cal.clone() as Calendar; c.set(Calendar.HOUR_OF_DAY, 23); c.set(Calendar.MINUTE, 59); c.set(Calendar.SECOND, 59); c.set(Calendar.MILLISECOND, 999); return c.timeInMillis }
}