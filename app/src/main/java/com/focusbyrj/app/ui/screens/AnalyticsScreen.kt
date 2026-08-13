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

package com.focusbyrj.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusbyrj.app.ui.theme.AccentCyan
import com.focusbyrj.app.ui.theme.AccentViolet
import com.focusbyrj.app.ui.theme.BorderGlass
import com.focusbyrj.app.ui.theme.SurfaceDark
import com.focusbyrj.app.ui.theme.SurfaceVariantDark
import com.focusbyrj.app.util.UsageStatsHelper
import java.util.Calendar

@Composable
fun AnalyticsScreen() {
    val context = LocalContext.current
    var dailyUsage by remember { mutableStateOf<Map<Int, Long>>(emptyMap()) }
    var currentStreak by remember { mutableStateOf(0) }
    var longestStreak by remember { mutableStateOf(0) }
    
    
    
    var trigger by remember { mutableStateOf(0) }
    androidx.lifecycle.compose.LifecycleEventEffect(androidx.lifecycle.Lifecycle.Event.ON_RESUME) { trigger++ }
    LaunchedEffect(trigger) {
        if (UsageStatsHelper.hasUsageStatsPermission(context)) {
            val stats = UsageStatsHelper.getLast30DaysUsageStats(context)
            dailyUsage = stats
            
            // Calculate streak based on days with >1hr screen time (arbitrary)
            var streak = 0
            val today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
            for (i in 0 downTo -30) {
                val day = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, i) }.get(Calendar.DAY_OF_YEAR)
                if ((stats[day] ?: 0L) > 60 * 60 * 1000) {
                    streak++
                } else if (i < 0) {
                    break
                }
            }
            currentStreak = streak
            var maxStreak = 0
            var tempStreak = 0
            for (i in -30..0) {
                val d = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, i) }.get(java.util.Calendar.DAY_OF_YEAR)
                if ((stats[d] ?: 0L) > 60 * 60 * 1000) {
                    tempStreak++
                    if (tempStreak > maxStreak) maxStreak = tempStreak
                } else {
                    tempStreak = 0
                }
            }
            longestStreak = maxStreak
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Heatmap",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Your consistency matrix",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            HeatmapWidget(dailyUsage)
        }
        
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatsCard("Longest Streak", "${longestStreak} Days", modifier = Modifier.weight(1f))
                StatsCard("Current Streak", "$currentStreak Days", modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun HeatmapWidget(dailyUsage: Map<Int, Long>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceDark)
            .border(1.dp, BorderGlass, RoundedCornerShape(24.dp))
            .padding(24.dp)
    ) {
        Column {
            Text(
                "ACTIVITY LAST 30 DAYS",
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val levels = listOf(
                    SurfaceVariantDark,
                    AccentViolet.copy(alpha = 0.4f),
                    AccentViolet.copy(alpha = 0.7f),
                    AccentViolet,
                    AccentCyan
                )
                
                val todayCalendar = Calendar.getInstance()
                
                for (week in 4 downTo 0) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (dayOfWeek in 0 until 7) {
                            val targetCal = Calendar.getInstance()
                            targetCal.add(Calendar.DAY_OF_YEAR, -(week * 7 + (6 - dayOfWeek)))
                            val dayOfYear = targetCal.get(Calendar.DAY_OF_YEAR)
                            
                            val isFuture = targetCal.after(todayCalendar)
                            
                            val usageMs = dailyUsage[dayOfYear] ?: 0L
                            val levelIndex = when {
                                isFuture -> 0
                                usageMs == 0L -> 0
                                usageMs < 1 * 60 * 60 * 1000 -> 1 // < 1 hour
                                usageMs < 3 * 60 * 60 * 1000 -> 2 // < 3 hours
                                usageMs < 5 * 60 * 60 * 1000 -> 3 // < 5 hours
                                else -> 4 // > 5 hours
                            }
                            
                            val color = levels[levelIndex]
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(color)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.height((24 * 7 + 6 * 6).dp)) {
                    Text("Mon", color = Color.Gray, fontSize = 10.sp)
                    Text("Wed", color = Color.Gray, fontSize = 10.sp)
                    Text("Fri", color = Color.Gray, fontSize = 10.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                Text("Less", color = Color.Gray, fontSize = 10.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    val levels = listOf(SurfaceVariantDark, AccentViolet.copy(alpha = 0.4f), AccentViolet.copy(alpha = 0.7f), AccentViolet, AccentCyan)
                    levels.forEach { color ->
                        Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(color))
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("More", color = Color.Gray, fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun StatsCard(label: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceDark)
            .border(1.dp, BorderGlass, RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
        }
    }
}
