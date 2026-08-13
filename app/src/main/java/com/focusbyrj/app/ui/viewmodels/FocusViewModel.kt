package com.focusbyrj.app.ui.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.focusbyrj.app.data.AppRepository
import com.focusbyrj.app.data.AppRestriction
import com.focusbyrj.app.data.FocusSchedule
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class FocusViewModel(private val repository: AppRepository, application: Application) : AndroidViewModel(application) {
    
    private val prefs = application.getSharedPreferences("focus_prefs", Context.MODE_PRIVATE)

    // Ticker to refresh active schedules every minute
    private val minuteTicker = flow {
        while (true) {
            emit(Unit)
            delay(60000)
        }
    }

    val combinedRestrictions: StateFlow<List<AppRestriction>> = combine(
        repository.allRestrictions, 
        repository.allSchedules,
        minuteTicker
    ) { rests, scheds, _ ->
        val map = rests.associateBy { it.packageName }.toMutableMap()
        val pm = application.packageManager

        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_WEEK)
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        val currentTotalMinutes = currentHour * 60 + currentMinute

        for (s in scheds) {
            val activeDays = s.daysOfWeek.split(",")
            val isActiveNow = if (activeDays.contains(currentDay.toString())) {
                val startTotalMinutes = s.startHour * 60 + s.startMinute
                val endTotalMinutes = s.endHour * 60 + s.endMinute
                if (startTotalMinutes <= endTotalMinutes) {
                    currentTotalMinutes in startTotalMinutes..endTotalMinutes
                } else {
                    currentTotalMinutes >= startTotalMinutes || currentTotalMinutes <= endTotalMinutes
                }
            } else {
                false
            }

            val pkgs = s.appsToBlock.split(",").filter { it.isNotBlank() }
            for (pkg in pkgs) {
                if (!map.containsKey(pkg)) {
                    val appName = try { pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString() } catch(e: Exception) { pkg }
                    map[pkg] = AppRestriction(pkg, appName, isActiveNow, s.mode, "")
                } else if (isActiveNow) {
                    // If it's already in the map (e.g. manually off), but schedule is active, force it on
                    val existing = map[pkg]!!
                    map[pkg] = existing.copy(isRestricted = true, mode = s.mode)
                }
            }
        }
        map.values.toList().sortedBy { it.appName }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    val schedules: StateFlow<List<FocusSchedule>> = repository.allSchedules.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    fun addSchedule(name: String, startHour: Int, startMinute: Int, endHour: Int, endMinute: Int, daysOfWeek: String, mode: String, appsToBlock: String) {
        viewModelScope.launch {
            repository.insertSchedule(
                FocusSchedule(
                    name = name,
                    startHour = startHour,
                    startMinute = startMinute,
                    endHour = endHour,
                    endMinute = endMinute,
                    daysOfWeek = daysOfWeek,
                    mode = mode,
                    appsToBlock = appsToBlock
                )
            )
        }
    }
    
    fun updateSchedule(schedule: FocusSchedule) {
        viewModelScope.launch {
            repository.insertSchedule(schedule)
        }
    }
    
    fun deleteSchedule(schedule: FocusSchedule) {
        viewModelScope.launch {
            repository.deleteSchedule(schedule)
        }
    }

    private val _isSessionActive = kotlinx.coroutines.flow.MutableStateFlow(false)
    val isSessionActive: StateFlow<Boolean> = _isSessionActive

    private val _timeRemaining = kotlinx.coroutines.flow.MutableStateFlow(25 * 60L) // 25 minutes in seconds
    val timeRemaining: StateFlow<Long> = _timeRemaining

    private val _initialTime = kotlinx.coroutines.flow.MutableStateFlow(25 * 60L)
    val initialTime: StateFlow<Long> = _initialTime

    private var timerJob: kotlinx.coroutines.Job? = null

    fun setTimeRemaining(minutes: Int) {
        if (!_isSessionActive.value) {
            val seconds = minutes * 60L
            _timeRemaining.value = seconds
            _initialTime.value = seconds
        }
    }

    fun toggleFocusSession() {
        if (_isSessionActive.value) {
            timerJob?.cancel()
            _isSessionActive.value = false
            _timeRemaining.value = _initialTime.value
            prefs.edit().putBoolean("isSessionActive", false).apply()
        } else {
            _isSessionActive.value = true
            prefs.edit().putBoolean("isSessionActive", true).apply()
            timerJob = viewModelScope.launch {
                while (_timeRemaining.value > 0 && _isSessionActive.value) {
                    kotlinx.coroutines.delay(1000)
                    _timeRemaining.value -= 1
                }
                if (_timeRemaining.value == 0L) {
                    _isSessionActive.value = false
                    _timeRemaining.value = _initialTime.value
                    prefs.edit().putBoolean("isSessionActive", false).apply()
                }
            }
        }
    }

    fun toggleRestriction(app: AppRestriction) {
        viewModelScope.launch {
            repository.toggleRestriction(app)
        }
    }

    fun addRestriction(app: AppRestriction) {
        viewModelScope.launch {
            repository.saveApp(app)
        }
    }
}

class FocusViewModelFactory(private val repository: AppRepository, private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FocusViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FocusViewModel(repository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
