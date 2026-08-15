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
import com.focusbyrj.app.ui.theme.BorderGlass
import com.focusbyrj.app.ui.theme.SurfaceDark
import com.focusbyrj.app.util.FocusStatsManager
import com.focusbyrj.app.util.HeatmapTheme
import java.util.Calendar

@Composable
fun AnalyticsScreen() {
    val context = LocalContext.current
    val stats by FocusStatsManager.statsFlow.collectAsState()
    val heatmapTheme by FocusStatsManager.themeFlow.collectAsState()
    
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                FocusStatsManager.refreshStats(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        FocusStatsManager.refreshStats(context)
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
            HeatmapWidget(
                dailyUsage = stats.dailyFocusMinutes,
                theme = heatmapTheme
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatsCard(
                    label = "Longest Streak",
                    value = "${stats.longestStreak} Days",
                    modifier = Modifier.weight(1f),
                    accentColor = heatmapTheme.colors.last()
                )
                StatsCard(
                    label = "Current Streak",
                    value = "${stats.currentStreak} Days",
                    modifier = Modifier.weight(1f),
                    accentColor = heatmapTheme.colors[3]
                )
            }
        }
    }
}

@Composable
fun HeatmapWidget(
    dailyUsage: Map<Int, Long>,
    theme: HeatmapTheme
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceDark)
            .border(1.dp, BorderGlass, RoundedCornerShape(24.dp))
            .padding(24.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "ACTIVITY HEATMAP",
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    theme.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = theme.colors.last()
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Darker squares indicate longer focus sessions.",
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.sp),
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(18.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.height((24 * 7 + 6 * 6).dp)) {
                    val days = listOf("S", "M", "T", "W", "T", "F", "S")
                    days.forEach { d ->
                        Box(modifier = Modifier.height(24.dp), contentAlignment = Alignment.Center) {
                            Text(d, color = Color.Gray, fontSize = 10.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.width(4.dp))
                
                val levels = theme.colors
                val todayCalendar = Calendar.getInstance()
                
                for (weekIndex in 4 downTo 0) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (dayOfWeek in 1..7) {
                            val targetCal = Calendar.getInstance()
                            targetCal.set(Calendar.DAY_OF_WEEK, dayOfWeek)
                            targetCal.add(Calendar.WEEK_OF_YEAR, -weekIndex)
                            
                            val dayOfYear = targetCal.get(Calendar.DAY_OF_YEAR)
                            
                            val isFuture = targetCal.after(todayCalendar) && targetCal.get(Calendar.DAY_OF_YEAR) != todayCalendar.get(Calendar.DAY_OF_YEAR)
                            
                            val usageMs = dailyUsage[dayOfYear] ?: 0L
                            val levelIndex = when {
                                isFuture -> 0
                                usageMs == 0L -> 0
                                usageMs < 15 * 60 * 1000L -> 1
                                usageMs < 30 * 60 * 1000L -> 2
                                usageMs < 60 * 60 * 1000L -> 3
                                else -> 4
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
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Less", color = Color.Gray, fontSize = 10.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    theme.colors.forEach { color ->
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
fun StatsCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accentColor: Color = Color.White
) {
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
            Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = accentColor)
        }
    }
}
