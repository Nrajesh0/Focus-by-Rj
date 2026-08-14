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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.ui.draw.scale
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusbyrj.app.MainActivity
import com.focusbyrj.app.data.AppRestriction
import com.focusbyrj.app.ui.theme.*
import com.focusbyrj.app.util.DeviceStatsHelper
import com.focusbyrj.app.util.DndHelper
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    restrictions: List<AppRestriction> = emptyList(),
    onToggle: (AppRestriction) -> Unit = {},
    isSessionActive: Boolean = false,
    timeRemaining: Long = 25 * 60L,
    initialTime: Long = 25 * 60L,
    onToggleSession: () -> Unit = {},
    onSetTime: (Int) -> Unit = {}
) {
    if (isSessionActive) {
        ActiveSessionScreen(timeRemaining = timeRemaining, initialTime = initialTime, onToggleSession = onToggleSession)
    } else {
        NormalDashboard(
            restrictions = restrictions,
            onToggle = onToggle,
            timeRemaining = timeRemaining,
            onToggleSession = onToggleSession,
            onSetTime = onSetTime
        )
    }
}

@Composable
fun NormalDashboard(
    restrictions: List<AppRestriction>,
    onToggle: (AppRestriction) -> Unit,
    timeRemaining: Long,
    onToggleSession: () -> Unit,
    onSetTime: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))

            val context = LocalContext.current
            val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
            var hasUsageStats by remember { mutableStateOf(com.focusbyrj.app.util.PermissionUtils.hasUsageStatsPermission(context)) }
            var hasOverlay by remember { mutableStateOf(com.focusbyrj.app.util.PermissionUtils.hasOverlayPermission(context)) }
            var isBatteryUnrestricted by remember { mutableStateOf(com.focusbyrj.app.util.PermissionUtils.isIgnoringBatteryOptimizations(context)) }
            
            DisposableEffect(lifecycleOwner) {
                val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                    if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                        hasUsageStats = com.focusbyrj.app.util.PermissionUtils.hasUsageStatsPermission(context)
                        hasOverlay = com.focusbyrj.app.util.PermissionUtils.hasOverlayPermission(context)
                        isBatteryUnrestricted = com.focusbyrj.app.util.PermissionUtils.isIgnoringBatteryOptimizations(context)
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            if (!hasUsageStats || !hasOverlay || !isBatteryUnrestricted) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .clickable {
                            if (!hasUsageStats) {
                                com.focusbyrj.app.util.PermissionUtils.requestUsageStatsPermission(context)
                            } else if (!hasOverlay) {
                                com.focusbyrj.app.util.PermissionUtils.requestOverlayPermission(context)
                            } else if (!isBatteryUnrestricted) {
                                com.focusbyrj.app.util.PermissionUtils.requestIgnoreBatteryOptimizations(context)
                            }
                        }
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Warning, contentDescription = "Warning", tint = MaterialTheme.colorScheme.onErrorContainer)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            val title = when {
                                !hasUsageStats -> "Usage Access Required"
                                !hasOverlay -> "Display Over Apps Required"
                                else -> "Battery: Set to 'No Restrictions'"
                            }
                            val subtitle = when {
                                !hasUsageStats -> "Tap to grant Usage Access to detect running apps."
                                !hasOverlay -> "Tap to allow displaying block overlay over apps."
                                else -> "Tap to set Unrestricted battery so Android doesn't kill focus locks."
                            }
                            Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onErrorContainer, fontWeight = FontWeight.Bold)
                            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            DeepWorkCard(timeRemaining = timeRemaining, onToggleSession = onToggleSession, onSetTime = onSetTime)
            Spacer(modifier = Modifier.height(12.dp))
            DeviceStatsSection()
            Spacer(modifier = Modifier.height(12.dp))
            StreakAndShieldedSection(restrictions = restrictions)
            Spacer(modifier = Modifier.height(12.dp))
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Your boundaries",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (restrictions.isEmpty()) {
            item { EmptyStateView() }
        } else {
            items(restrictions, key = { it.packageName }) { app ->
                AppRestrictionCard(app = app, onToggle = { onToggle(app) })
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun DeepWorkCard(
    timeRemaining: Long,
    onToggleSession: () -> Unit,
    onSetTime: (Int) -> Unit
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var sliderValue by remember { mutableStateOf((timeRemaining / 60).toFloat()) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Set Focus Duration") },
            text = {
                Column {
                    Text("Duration: ${sliderValue.toInt()} minutes", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    Slider(
                        value = sliderValue,
                        onValueChange = { sliderValue = it },
                        valueRange = 5f..120f,
                        steps = 23,
                        colors = SliderDefaults.colors(thumbColor = AccentCyan, activeTrackColor = AccentViolet)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onSetTime(sliderValue.toInt())
                    showDialog = false
                }) { Text("Save", color = AccentCyan) }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel", color = Color.Gray) }
            },
            containerColor = SurfaceVariantDark,
            titleContentColor = Color.White,
            textContentColor = Color.White
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 140.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(SurfaceVariantDark, SurfaceDark)))
            .border(1.dp, BorderGlass, RoundedCornerShape(24.dp))
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column {
                    Text(
                        text = "DEEP WORK",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${timeRemaining / 60} min",
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        modifier = Modifier.clickable { showDialog = true }
                    )
                }
                
                Button(
                    onClick = {
                        if (!DndHelper.hasDndPermission(context)) {
                            DndHelper.requestDndPermission(context)
                        } else {
                            DndHelper.setDndMode(context, true)
                            onToggleSession()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = MidnightBlack
                    ),
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text("Start", fontWeight = FontWeight.Bold)
                }
            }
            Text("Tap time to edit duration", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun DeviceStatsSection() {
    val context = LocalContext.current
    val statsFlow = remember { DeviceStatsHelper.getBatteryStats(context) }
    val initialBatteryInfo = remember {
        com.focusbyrj.app.util.BatteryHealthInfo(
            rawChargePercentage = 80,
            maxCapacityHealthPercent = 88,
            realRemainingCapacityPercent = 70.4f,
            temperatureCelsius = 28.5f,
            voltageMv = 3850,
            healthStatusLabel = "Normal",
            peakPerformanceStatus = "Your battery is currently supporting normal peak performance.",
            isCharging = false
        )
    }
    val stats by statsFlow.collectAsState(initial = initialBatteryInfo)
    var showBatteryDetails by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth().height(140.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Temperature Card
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceDark)
                .border(1.dp, BorderGlass, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.DeviceThermostat, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${stats.temperatureCelsius}°C",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text("Temp", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }

        // Battery Health & Real Capacity Card
        Box(
            modifier = Modifier
                .weight(1.3f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceDark)
                .border(1.dp, BorderGlass, RoundedCornerShape(20.dp))
                .clickable { showBatteryDetails = true }
                .padding(14.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.BatteryFull, contentDescription = null, tint = AccentViolet, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (stats.isCharging) "Charging" else "Real Capacity",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = String.format("%.1f%%", stats.realRemainingCapacityPercent),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = "${stats.rawChargePercentage}% raw @ ${stats.maxCapacityHealthPercent}% health",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = AccentViolet,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    if (showBatteryDetails) {
        AlertDialog(
            onDismissRequest = { showBatteryDetails = false },
            containerColor = Color(0xFF1E1E2E), // Solid dark background to fix opacity/readability
            confirmButton = {
                TextButton(onClick = { showBatteryDetails = false }) {
                    Text("Close", color = AccentCyan)
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.BatteryFull, contentDescription = null, tint = AccentViolet)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Battery Health & Capacity", color = Color.White, style = MaterialTheme.typography.titleMedium)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Maximum Capacity measures battery charge capability relative to factory original condition after chemical aging.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray
                    )

                    HorizontalDivider(color = BorderGlass)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Real Battery Capacity:", style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Bold)
                        Text(String.format("%.1f%%", stats.realRemainingCapacityPercent), style = MaterialTheme.typography.bodyMedium, color = AccentCyan, fontWeight = FontWeight.Bold)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Maximum Capacity (Health):", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        Text("${stats.maxCapacityHealthPercent}%", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Raw Charge Level:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        Text("${stats.rawChargePercentage}%", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Battery Condition:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        Text(stats.healthStatusLabel, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Voltage / Temperature:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        Text("${stats.voltageMv} mV / ${stats.temperatureCelsius}°C", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                    }

                    HorizontalDivider(color = BorderGlass)

                    Text(
                        "Peak Performance Capability:",
                        style = MaterialTheme.typography.labelMedium,
                        color = AccentViolet
                    )
                    Text(
                        stats.peakPerformanceStatus,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray
                    )
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun ActiveSessionScreen(timeRemaining: Long, initialTime: Long, onToggleSession: () -> Unit) {
    val progress = if (initialTime > 0) (timeRemaining.toFloat() / initialTime.toFloat()) else 0f
    val minutes = timeRemaining / 60
    val seconds = timeRemaining % 60
    val timeString = String.format("%02d:%02d", minutes, seconds)
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBlack)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "DEEP WORK",
                style = MaterialTheme.typography.labelMedium,
                color = AccentCyan,
                letterSpacing = 4.sp
            )
            Spacer(modifier = Modifier.height(64.dp))
            
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(280.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.05f),
                        style = Stroke(width = 8.dp.toPx())
                    )
                    drawArc(
                        color = AccentViolet,
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = timeString,
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(80.dp))
            
            Button(
                onClick = {
                    DndHelper.setDndMode(context, false)
                    onToggleSession()
                },
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark, contentColor = Color.White),
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 40.dp, vertical = 16.dp)
            ) {
                Text("Stop Focus", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}


@Composable
fun EmptyStateView() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(SurfaceVariantDark, SurfaceDark)))
            .border(1.dp, BorderGlass, RoundedCornerShape(28.dp))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.LockOpen,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No apps locked",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap + to add a distraction",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Composable
fun StreakAndShieldedSection(restrictions: List<AppRestriction>) {
    val context = LocalContext.current
    val stats by com.focusbyrj.app.util.FocusStatsManager.statsFlow.collectAsState()
    val heatmapTheme by com.focusbyrj.app.util.FocusStatsManager.themeFlow.collectAsState()
    
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                com.focusbyrj.app.util.FocusStatsManager.refreshStats(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Row(
        modifier = Modifier.fillMaxWidth().height(140.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Streak Box
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceDark)
                .border(1.dp, BorderGlass, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column {
                Text("STREAK", style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp, fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.weight(1f).fillMaxHeight())
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("${stats.currentStreak}", style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp), color = heatmapTheme.colors.last())
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("days", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    heatmapTheme.colors.forEach { color ->
                        Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(color))
                    }
                }
            }
        }

        // Shielded Box
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceDark)
                .border(1.dp, BorderGlass, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column {
                Text("SHIELDED", style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp, fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.weight(1f)
                .fillMaxHeight())
                
                Row(modifier = Modifier.padding(vertical = 8.dp)) {
                    val activeRestrictions = restrictions.filter { it.isRestricted }.take(4)
                    
                    if (activeRestrictions.isEmpty()) {
                        Text("No active boundaries", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    } else {
                        activeRestrictions.forEachIndexed { index, app ->
                            val pm = context.packageManager
                            val icon = remember(app.packageName) {
                                com.focusbyrj.app.util.ImageUtils.getAppIcon(pm, app.packageName)
                            }
                            
                            Box(
                                modifier = Modifier
                                    .offset(x = (-4 * index).dp)
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceVariantDark)
                                    .border(2.dp, MidnightBlack, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (icon != null) {
                                    androidx.compose.foundation.Image(bitmap = icon, contentDescription = null, modifier = Modifier.fillMaxSize().padding(4.dp))
                                } else {
                                    val text = app.appName.take(2).uppercase()
                                    Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
                Text("${restrictions.count { it.isRestricted }} shielded apps", style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
@Composable
fun AppRestrictionCard(app: AppRestriction, onToggle: () -> Unit) {
    val isLocked = app.isRestricted
    val cardColor = if (isLocked) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
    val borderColor = if (isLocked) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else BorderGlass
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onToggle() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                val pm = LocalContext.current.packageManager
                val icon = remember(app.packageName) { com.focusbyrj.app.util.ImageUtils.getAppIcon(pm, app.packageName) }
                if (icon != null) {
                    androidx.compose.foundation.Image(
                        bitmap = icon,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.background),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(app.appName.take(2).uppercase(), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (isLocked) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = app.mode.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Switch(
                checked = isLocked,
                onCheckedChange = { onToggle() },
                modifier = Modifier.scale(0.85f),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.background,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedTrackColor = MaterialTheme.colorScheme.background
                )
            )
        }
    }
}
