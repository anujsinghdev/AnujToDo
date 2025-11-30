package com.anujsinghdev.anujtodo.ui.pomodoro

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anujsinghdev.anujtodo.data.local.UserPreferencesRepository
import com.anujsinghdev.anujtodo.domain.model.FocusSession
import com.anujsinghdev.anujtodo.domain.model.SessionStatus
import com.anujsinghdev.anujtodo.domain.repository.TodoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PomodoroViewModel @Inject constructor(
    private val repository: UserPreferencesRepository,
    private val todoRepository: TodoRepository // <--- 1. Injected Main Repository
) : ViewModel() {

    var initialTimeInMillis = mutableStateOf(25 * 60 * 1000L)
    var timeLeftInMillis = mutableStateOf(25 * 60 * 1000L)
    var isTimerRunning = mutableStateOf(false)

    // <--- 2. New State for Tags (Default to Work)
    var currentTag = mutableStateOf("Work")

    // Custom durations as StateFlow
    val customDurations: StateFlow<List<Int>> = repository.customDurations
        .map { set ->
            set.mapNotNull { it.toIntOrNull() }.sorted()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            val endTime = repository.timerEndTime.first() ?: 0L
            val isRunning = repository.isTimerRunning.first() ?: false
            val savedRemaining = repository.timerRemainingPaused.first() ?: 0L

            if (isRunning && endTime > System.currentTimeMillis()) {
                isTimerRunning.value = true
                startTicker(endTime)
            } else if (isRunning && endTime <= System.currentTimeMillis()) {
                // If app was closed and timer finished in background, reset and save
                resetTimer()
            } else if (!isRunning && savedRemaining > 0L) {
                timeLeftInMillis.value = savedRemaining
                isTimerRunning.value = false
            }
        }
    }

    // <--- 3. Helper to Save Session to DB
    private fun saveSession(isCompleted: Boolean) {
        val totalDuration = initialTimeInMillis.value
        val remaining = timeLeftInMillis.value
        val timeSpentMillis = totalDuration - remaining

        // Only save if at least 1 minute (60,000ms) was spent to avoid noise
        if (timeSpentMillis > 60_000) {
            // Round up or down? integer division truncates, which is fine.
            val minutes = (timeSpentMillis / 1000 / 60).toInt()

            viewModelScope.launch {
                todoRepository.saveFocusSession(
                    FocusSession(
                        durationMinutes = minutes,
                        timestamp = System.currentTimeMillis(),
                        status = if (isCompleted) SessionStatus.COMPLETED else SessionStatus.STOPPED,
                        tag = currentTag.value
                    )
                )
            }
        }
    }

    fun startTimer() {
        val endTime = System.currentTimeMillis() + timeLeftInMillis.value
        isTimerRunning.value = true

        viewModelScope.launch {
            repository.saveTimerState(endTime, true)
        }

        startTicker(endTime)
    }

    private fun startTicker(endTime: Long) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                val currentTime = System.currentTimeMillis()
                val remaining = endTime - currentTime

                if (remaining > 0) {
                    timeLeftInMillis.value = remaining
                } else {
                    timeLeftInMillis.value = 0
                    isTimerRunning.value = false
                    repository.clearTimerState()

                    // <--- 4. Timer Finished Naturally -> Save as COMPLETED
                    saveSession(isCompleted = true)
                    break
                }
                delay(100)
            }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        isTimerRunning.value = false

        viewModelScope.launch {
            repository.savePausedState(timeLeftInMillis.value)
        }
    }

    fun resetTimer() {
        // <--- 5. User manually stopped/reset -> Save as STOPPED
        // Check if actual time passed to avoid saving if they hit start then stop immediately (covered by 1 min check too)
        if (timeLeftInMillis.value != initialTimeInMillis.value) {
            saveSession(isCompleted = false)
        }

        timerJob?.cancel()
        isTimerRunning.value = false
        timeLeftInMillis.value = initialTimeInMillis.value

        viewModelScope.launch {
            repository.clearTimerState()
        }
    }

    fun updateDuration(minutes: Int) {
        resetTimer() // This will trigger a save if a previous session was active
        val newTime = minutes * 60 * 1000L
        initialTimeInMillis.value = newTime
        timeLeftInMillis.value = newTime
    }

    fun addCustomDuration(minutes: Int) {
        viewModelScope.launch {
            repository.saveCustomDuration(minutes)
        }
    }

    fun removeCustomDuration(minutes: Int) {
        viewModelScope.launch {
            repository.removeCustomDuration(minutes)
        }
    }

    // New action to update tag from UI
    fun updateTag(newTag: String) {
        currentTag.value = newTag
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}