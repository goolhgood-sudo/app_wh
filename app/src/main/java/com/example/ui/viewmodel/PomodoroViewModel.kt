package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.InjazApplication
import com.example.data.model.Task
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class PomodoroMode(val displayName: String, val defaultMinutes: Int) {
    WORK("جلسة تركيز", 25),
    SHORT_BREAK("استراحة قصيرة", 5),
    LONG_BREAK("استراحة طويلة", 15)
}

class PomodoroViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as InjazApplication).repository

    private val _currentMode = MutableStateFlow(PomodoroMode.WORK)
    val currentMode: StateFlow<PomodoroMode> = _currentMode

    private val _timeLeftSeconds = MutableStateFlow(PomodoroMode.WORK.defaultMinutes * 60)
    val timeLeftSeconds: StateFlow<Int> = _timeLeftSeconds

    private val _totalDurationSeconds = MutableStateFlow(PomodoroMode.WORK.defaultMinutes * 60)
    val totalDurationSeconds: StateFlow<Int> = _totalDurationSeconds

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    private val _selectedTask = MutableStateFlow<Task?>(null)
    val selectedTask: StateFlow<Task?> = _selectedTask

    private var timerJob: Job? = null

    private val todayStartMillis: Long
        get() {
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return cal.timeInMillis
        }

    val todayCompletedSessions: StateFlow<Int> = repository.getTodaySessionsCount(todayStartMillis)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val totalFocusMinutes: StateFlow<Int?> = repository.getTotalFocusMinutes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    fun selectTask(task: Task?) {
        _selectedTask.value = task
    }

    fun switchMode(mode: PomodoroMode) {
        pause()
        _currentMode.value = mode
        val seconds = mode.defaultMinutes * 60
        _timeLeftSeconds.value = seconds
        _totalDurationSeconds.value = seconds
    }

    fun start() {
        if (_isRunning.value) return
        _isRunning.value = true

        timerJob = viewModelScope.launch {
            while (_timeLeftSeconds.value > 0 && _isRunning.value) {
                delay(1000)
                _timeLeftSeconds.value -= 1
            }

            if (_timeLeftSeconds.value == 0) {
                onTimerFinished()
            }
        }
    }

    fun pause() {
        _isRunning.value = false
        timerJob?.cancel()
        timerJob = null
    }

    fun reset() {
        pause()
        val seconds = _currentMode.value.defaultMinutes * 60
        _timeLeftSeconds.value = seconds
        _totalDurationSeconds.value = seconds
    }

    fun skip() {
        pause()
        if (_currentMode.value == PomodoroMode.WORK) {
            switchMode(PomodoroMode.SHORT_BREAK)
        } else {
            switchMode(PomodoroMode.WORK)
        }
    }

    private fun onTimerFinished() {
        _isRunning.value = false
        triggerVibration()

        if (_currentMode.value == PomodoroMode.WORK) {
            viewModelScope.launch {
                repository.recordPomodoroSession(
                    taskId = _selectedTask.value?.id,
                    taskTitle = _selectedTask.value?.title,
                    durationMinutes = _currentMode.value.defaultMinutes
                )
            }
            switchMode(PomodoroMode.SHORT_BREAK)
        } else {
            switchMode(PomodoroMode.WORK)
        }
    }

    private fun triggerVibration() {
        val context = getApplication<Application>()
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 300, 200, 300), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(500)
            }
        } catch (_: Exception) {}
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
