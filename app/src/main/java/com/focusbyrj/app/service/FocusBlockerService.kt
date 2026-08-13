/*
 * Copyright (C) 2024-2026 Focus by Rj
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.focusbyrj.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.room.Room
import com.focusbyrj.app.data.FocusDatabase
import com.focusbyrj.app.util.TemporaryUnlockManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class FocusBlockerService : Service() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private lateinit var db: FocusDatabase

    private var lastBlockedPackage: String? = null
    private var lastBlockTime: Long = 0L

    override fun onCreate() {
        super.onCreate()
        db = Room.databaseBuilder(applicationContext, FocusDatabase::class.java, "focus_database")
            .fallbackToDestructiveMigration(true)
            .build()

        startForegroundServiceNotification()
        startRoutineMonitorLoop()
        startAppMonitoringLoop()
    }

    private fun startForegroundServiceNotification() {
        val channelId = "focus_blocker_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Focus Guard Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors restricted apps in background"
                setSound(null, null)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Focus Guard Active")
            .setContentText("Protecting your screen time and boundaries")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun startAppMonitoringLoop() {
        scope.launch {
            while (isActive) {
                try {
                    val currentPackage = getForegroundPackage()
                    if (!currentPackage.isNullOrBlank()) {
                        checkAndBlockApp(currentPackage)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(350L) // Fast polling for responsive blocking
            }
        }
    }

    
    private var activeRoutines = mutableMapOf<String, com.focusbyrj.app.data.FocusSchedule>()
    
    private fun startRoutineMonitorLoop() {
        scope.launch {
            // Give time for DB to init
            delay(2000L)
            while (isActive) {
                try {
                    checkRoutinesAndNotify()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(60000L) // Check every minute
            }
        }
    }

    private suspend fun checkRoutinesAndNotify() {
        val prefs = applicationContext.getSharedPreferences("focus_prefs", Context.MODE_PRIVATE)
        val notifyEnabled = prefs.getBoolean("routine_notifications", true)
        
        val schedules = db.scheduleDao().getAllSchedulesSync()
        val calendar = java.util.Calendar.getInstance()
        val currentDay = calendar.get(java.util.Calendar.DAY_OF_WEEK)
        val currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(java.util.Calendar.MINUTE)
        val currentTotalMinutes = currentHour * 60 + currentMinute
        
        val currentlyActive = mutableMapOf<String, com.focusbyrj.app.data.FocusSchedule>()
        
        for (schedule in schedules) {
            val activeDays = schedule.daysOfWeek.split(",")
            if (activeDays.contains(currentDay.toString())) {
                val startTotalMinutes = schedule.startHour * 60 + schedule.startMinute
                val endTotalMinutes = schedule.endHour * 60 + schedule.endMinute
                
                val isTimeMatch = if (startTotalMinutes <= endTotalMinutes) {
                    currentTotalMinutes in startTotalMinutes..endTotalMinutes
                } else {
                    currentTotalMinutes >= startTotalMinutes || currentTotalMinutes <= endTotalMinutes
                }
                
                if (isTimeMatch) {
                    currentlyActive[schedule.id.toString()] = schedule
                    if (!activeRoutines.containsKey(schedule.id.toString())) {
                        if (notifyEnabled) {
                            sendRoutineNotification("Routine Started", "${schedule.name} is now active.")
                        }
                    }
                }
            }
        }
        
        for (activeId in activeRoutines.keys) {
            if (!currentlyActive.containsKey(activeId)) {
                if (notifyEnabled) {
                    val scheduleName = activeRoutines[activeId]?.name ?: "Routine"
                    sendRoutineNotification("Routine Ended", "$scheduleName has ended.")
                }
            }
        }
        
        activeRoutines.clear()
        activeRoutines.putAll(currentlyActive)
    }

    private fun sendRoutineNotification(title: String, message: String) {
        val channelId = "routine_alerts"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(channelId, "Routine Alerts", android.app.NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }
        val notification = androidx.core.app.NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private var lastCheckedTime: Long = System.currentTimeMillis()
    private var currentForegroundPackage: String? = null


    private fun getForegroundPackage(): String? {
        // Log everything

        val usm = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return currentForegroundPackage
        val now = System.currentTimeMillis()
        val startTime = now - 1000 * 30 // 30 second sliding window
        
        val events = usm.queryEvents(startTime, now)
        val event = UsageEvents.Event()
        var latestPackage: String? = null
        var latestTime = 0L
        
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                if (event.timeStamp > latestTime) {
                    latestTime = event.timeStamp
                    latestPackage = event.packageName
                }
            }
        }
        

        if (latestPackage != null) {
            currentForegroundPackage = latestPackage
        } else if (currentForegroundPackage == null) {
            val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, now)
            if (!stats.isNullOrEmpty()) {
                val mostRecent = stats.maxByOrNull { it.lastTimeUsed }
                if (mostRecent != null) {
                    currentForegroundPackage = mostRecent.packageName
                }
            }
        }
        return currentForegroundPackage

    }

    private suspend fun checkAndBlockApp(packageName: String) {

        if (packageName == applicationContext.packageName ||
            packageName == "com.focusbyrj.app" ||
            packageName == "com.android.systemui" ||
            packageName == "com.android.settings" ||
            packageName.contains("launcher")
        ) {
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                BlockOverlayManager.hideOverlay()
            }
            return
        }


        if (TemporaryUnlockManager.isUnlocked(applicationContext, packageName)) {
            android.util.Log.d("FocusGuard", "Skipping block for $packageName because it is temporarily unlocked")
            return
        }

        var shouldBlock = false
        var blockQuote = ""
        var blockMode = "HARD"

        // 1. Check 24/7 restrictions
        val restriction = db.appRestrictionDao().getRestriction(packageName)
        if (restriction != null && restriction.isRestricted) {
            shouldBlock = true
            blockQuote = restriction.customQuote
            blockMode = restriction.mode
        }

        // 2. Check scheduled routines if not already blocked (Optimized - No DB calls here!)
        if (!shouldBlock) {
            for (schedule in activeRoutines.values) {
                if (schedule.appsToBlock.split(",").contains(packageName)) {
                    shouldBlock = true
                    blockQuote = "Routine: ${schedule.name} is active."
                    blockMode = schedule.mode
                    break
                }
            }
        }

        if (shouldBlock) {
            val now = System.currentTimeMillis()
            if (lastBlockedPackage == packageName && (now - lastBlockTime) < 1200) {
                return
            }
            lastBlockedPackage = packageName
            lastBlockTime = now

            android.os.Handler(android.os.Looper.getMainLooper()).post {
                try {
                    BlockOverlayManager.showOverlay(this@FocusBlockerService, packageName, blockQuote, blockMode)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    companion object {
        private const val NOTIFICATION_ID = 1001

        fun startService(context: Context) {
            try {
                val intent = Intent(context, FocusBlockerService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
