package com.anujsinghdev.anujtodo.ui.pomodoro

import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anujsinghdev.anujtodo.data.local.UserPreferencesRepository
import com.anujsinghdev.anujtodo.domain.model.FocusSession
import com.anujsinghdev.anujtodo.domain.model.SessionStatus
import com.anujsinghdev.anujtodo.domain.repository.TodoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PomodoroViewModel @Inject constructor(
    @ApplicationContext private val context: Context, // 1. Inject Context for Sound/Vibration
    private val repository: UserPreferencesRepository,
    private val todoRepository: TodoRepository
) : ViewModel() {

    var initialTimeInMillis = mutableStateOf(25 * 60 * 1000L)
    var timeLeftInMillis = mutableStateOf(25 * 60 * 1000L)
    var isTimerRunning = mutableStateOf(false)
    var currentTag = mutableStateOf("Work")

    // Event to notify UI when timer finishes (for Snackbar)
    private val _timerFinishedEvent = MutableSharedFlow<Unit>()
    val timerFinishedEvent = _timerFinishedEvent.asSharedFlow()

    val customDurations: StateFlow<List<Int>> = repository.customDurations
        .map { set -> set.mapNotNull { it.toIntOrNull() }.sorted() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
                resetTimer()
            } else if (!isRunning && savedRemaining > 0L) {
                timeLeftInMillis.value = savedRemaining
                isTimerRunning.value = false
            }
        }
    }

    private fun saveSession(isCompleted: Boolean) {
        val totalDuration = initialTimeInMillis.value
        val remaining = timeLeftInMillis.value
        val timeSpentMillis = totalDuration - remaining

        // Only save if at least 1 minute (60,000ms) was spent
        if (timeSpentMillis >= 60_000) {
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
        viewModelScope.launch { repository.saveTimerState(endTime, true) }
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
                    // --- TIMER FINISHED LOGIC ---
                    timeLeftInMillis.value = 0
                    isTimerRunning.value = false
                    repository.clearTimerState()

                    // 1. Save Session
                    saveSession(isCompleted = true)

                    // 2. Play Sound & Vibrate
                    triggerAlarm()

                    // 3. Reset UI to initial state (Fixes "Stuck" screen)
                    timeLeftInMillis.value = initialTimeInMillis.value

                    // 4. Notify UI
                    _timerFinishedEvent.emit(Unit)

                    break
                }
                delay(100)
            }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        isTimerRunning.value = false
        viewModelScope.launch { repository.savePausedState(timeLeftInMillis.value) }
    }

    fun resetTimer() {
        // --- FIX FOR INACCURATE STATS ---
        val remaining = timeLeftInMillis.value
        val initial = initialTimeInMillis.value

        // Only save a "Stopped" session if the timer was actually running/paused
        // AND it hasn't already hit 0 (which is handled by startTicker).
        if (remaining > 0 && remaining != initial) {
            saveSession(isCompleted = false)
        }

        timerJob?.cancel()
        isTimerRunning.value = false
        timeLeftInMillis.value = initialTimeInMillis.value // Reset UI

        viewModelScope.launch {
            repository.clearTimerState()
        }
    }

    fun updateDuration(minutes: Int) {
        resetTimer()
        val newTime = minutes * 60 * 1000L
        initialTimeInMillis.value = newTime
        timeLeftInMillis.value = newTime
    }

    fun addCustomDuration(minutes: Int) {
        viewModelScope.launch { repository.saveCustomDuration(minutes) }
    }

    fun removeCustomDuration(minutes: Int) {
        viewModelScope.launch { repository.removeCustomDuration(minutes) }
    }

    fun updateTag(newTag: String) {
        currentTag.value = newTag
    }

    private fun triggerAlarm() {
        try {
            // Vibrate
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(500)
            }

            // Sound
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val r = RingtoneManager.getRingtone(context, notification)
            r.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}