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
import com.focusbyrj.app.R
import com.focusbyrj.app.data.FocusDatabase
import com.focusbyrj.app.util.FocusQuotes
import com.focusbyrj.app.util.TemporaryUnlockManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

object FocusExitTracker {
    @Volatile
    var lastExitedPackage: String? = null
    @Volatile
    var exitTimestamp: Long = 0L

    fun notifyExited(packageName: String?) {
        lastExitedPackage = packageName
        exitTimestamp = System.currentTimeMillis()
    }

    fun isExitSuppressed(packageName: String?): Boolean {
        if (packageName.isNullOrBlank()) return false
        if (packageName != lastExitedPackage) return false
        return (System.currentTimeMillis() - exitTimestamp) < 25000L
    }

    fun onNewForegroundAppDetected(packageName: String) {
        if (packageName != lastExitedPackage && packageName.isNotBlank() && packageName != "com.android.systemui") {
            lastExitedPackage = null
            exitTimestamp = 0L
        }
    }
}

class FocusBlockerService : Service() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private lateinit var db: FocusDatabase

    private var currentForegroundPackage: String? = null

    override fun onCreate() {
        super.onCreate()
        db = (application as com.focusbyrj.app.FocusApplication).database

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
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
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
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Routine Alerts", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private val homePackages = mutableSetOf<String>()
    private var lastHomePackagesCheck = 0L

    private fun refreshHomePackages() {
        val now = System.currentTimeMillis()
        if (now - lastHomePackagesCheck < 30000L && homePackages.isNotEmpty()) return
        lastHomePackagesCheck = now
        try {
            val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            val list = packageManager.queryIntentActivities(homeIntent, 0)
            for (info in list) {
                info.activityInfo?.packageName?.let { homePackages.add(it) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun isIgnoredPackage(packageName: String): Boolean {
        if (packageName.isBlank()) return true
        if (packageName == applicationContext.packageName || packageName == "com.focusbyrj.app") return true
        if (packageName == "com.android.settings" || packageName == "com.android.systemui" || packageName == "android") return true
        
        refreshHomePackages()
        if (homePackages.contains(packageName)) return true

        val lower = packageName.lowercase()
        return lower.contains("launcher") ||
                lower.contains("quickstep") ||
                lower.contains("trebuchet") ||
                lower.contains("nexuslauncher") ||
                lower.contains("miui.home") ||
                lower.contains("sec.android.app.launcher") ||
                lower.contains("huawei.android.launcher") ||
                lower.contains("oppo.launcher") ||
                lower.contains("vivo.launcher") ||
                lower.contains("transsion.home") ||
                lower.contains("motorola.launcher") ||
                lower.contains("oneplus.launcher")
    }

    private fun getForegroundPackage(): String? {
        val usm = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return null
        val now = System.currentTimeMillis()

        // 1. Query recent UsageEvents
        val events = usm.queryEvents(now - 1000 * 10, now)
        val event = UsageEvents.Event()
        var latestPackage: String? = null
        var latestTime = 0L

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED || event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                if (event.timeStamp > latestTime) {
                    latestTime = event.timeStamp
                    latestPackage = event.packageName
                }
            }
        }

        if (latestPackage != null) {
            // Reject stale events from before the user clicked 'Exit to Home'
            if (latestPackage == FocusExitTracker.lastExitedPackage && latestTime <= FocusExitTracker.exitTimestamp) {
                return null
            }
            if (latestPackage != FocusExitTracker.lastExitedPackage) {
                FocusExitTracker.onNewForegroundAppDetected(latestPackage)
            }
            currentForegroundPackage = latestPackage
            return latestPackage
        }

        // 2. Fallback to queryUsageStats
        val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_BEST, now - 1000 * 30, now)
        if (!stats.isNullOrEmpty()) {
            val mostRecent = stats.maxByOrNull { it.lastTimeUsed }
            if (mostRecent != null && (now - mostRecent.lastTimeUsed) < 15000) {
                val pkg = mostRecent.packageName
                if (pkg == FocusExitTracker.lastExitedPackage && mostRecent.lastTimeUsed <= FocusExitTracker.exitTimestamp) {
                    return null
                }
                if (pkg != FocusExitTracker.lastExitedPackage) {
                    FocusExitTracker.onNewForegroundAppDetected(pkg)
                }
                currentForegroundPackage = pkg
                return pkg
            }
        }

        if (FocusExitTracker.isExitSuppressed(currentForegroundPackage)) {
            return null
        }

        return currentForegroundPackage
    }

    private suspend fun checkAndBlockApp(packageName: String) {
        if (isIgnoredPackage(packageName)) {
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                BlockOverlayManager.hideOverlay()
            }
            return
        }

        // If user just exited this app to Home, suppress blocking until they actively reopen
        if (FocusExitTracker.isExitSuppressed(packageName)) {
            return
        }

        if (TemporaryUnlockManager.isUnlocked(applicationContext, packageName)) {
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                BlockOverlayManager.hideOverlay()
            }
            return
        }

        var shouldBlock = false
        var blockQuote = ""
        var blockMode = "HARD"

        // 1. Check 24/7 restrictions
        val restriction = db.appRestrictionDao().getRestriction(packageName)
        if (restriction != null && restriction.isRestricted) {
            shouldBlock = true
            blockQuote = FocusQuotes.getQuoteOrDefault(restriction.customQuote)
            blockMode = restriction.mode
        }

        // 2. Check scheduled routines
        if (!shouldBlock) {
            for (schedule in activeRoutines.values) {
                if (schedule.appsToBlock.split(",").contains(packageName)) {
                    shouldBlock = true
                    blockQuote = "Routine '${schedule.name}' is active."
                    blockMode = schedule.mode
                    break
                }
            }
        }

        // 3. Check active Focus / Deep Work session
        val prefs = applicationContext.getSharedPreferences("focus_prefs", Context.MODE_PRIVATE)
        val isSessionActive = prefs.getBoolean("isSessionActive", false)
        if (!shouldBlock && isSessionActive && restriction != null) {
            shouldBlock = true
            blockQuote = FocusQuotes.getQuoteOrDefault(restriction.customQuote)
            blockMode = restriction.mode
        }

        if (shouldBlock) {
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                try {
                    BlockOverlayManager.showBlockScreen(this@FocusBlockerService, packageName, blockQuote, blockMode)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else if (BlockOverlayManager.isShowing) {
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                BlockOverlayManager.hideOverlay()
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
