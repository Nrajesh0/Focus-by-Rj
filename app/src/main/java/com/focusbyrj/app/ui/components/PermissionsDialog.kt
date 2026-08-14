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

package com.focusbyrj.app.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.focusbyrj.app.ui.theme.*
import com.focusbyrj.app.util.PermissionUtils

@Composable
fun SetupPermissionsDialog(
    hasUsageStats: Boolean,
    hasOverlay: Boolean,
    isBatteryUnrestricted: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var showWhyBatteryDialog by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .padding(horizontal = 20.dp, vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(28.dp))
                    .border(1.dp, BorderGlass, RoundedCornerShape(28.dp)),
                color = SurfaceDark
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Icon
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(AccentCyan.copy(alpha = 0.15f))
                            .border(1.dp, AccentCyan.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Shield,
                            contentDescription = null,
                            tint = AccentCyan,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Essential Focus Setup",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "To reliably enforce locks and protect your focus sessions, Focus by Rj requires the following permissions:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Permission 1: Usage Access
                    PermissionSetupCard(
                        icon = Icons.Filled.QueryStats,
                        title = "1. Usage Access",
                        subtitle = "Detects when a blocked app is launched in foreground.",
                        isGranted = hasUsageStats,
                        onAction = { PermissionUtils.requestUsageStatsPermission(context) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Permission 2: Display Over Apps
                    PermissionSetupCard(
                        icon = Icons.Filled.Layers,
                        title = "2. Display Over Other Apps",
                        subtitle = "Allows displaying mindful pause & hard lock screens.",
                        isGranted = hasOverlay,
                        onAction = { PermissionUtils.requestOverlayPermission(context) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Permission 3: Unrestricted Battery
                    PermissionSetupCard(
                        icon = Icons.Filled.BatteryFull,
                        title = "3. No Battery Restrictions",
                        subtitle = "Prevents Android from stopping lock enforcement in background.",
                        isGranted = isBatteryUnrestricted,
                        highlight = true,
                        onAction = { PermissionUtils.requestIgnoreBatteryOptimizations(context) },
                        onLearnMore = { showWhyBatteryDialog = true }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Done / Continue Button
                    val allGranted = hasUsageStats && hasOverlay && isBatteryUnrestricted
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (allGranted) AccentCyan else SurfaceVariantDark,
                            contentColor = if (allGranted) MidnightBlack else Color.White
                        ),
                        shape = RoundedCornerShape(26.dp)
                    ) {
                        Text(
                            text = if (allGranted) "All Set — Let's Focus" else "Continue with Setup",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }

    if (showWhyBatteryDialog) {
        AlertDialog(
            onDismissRequest = { showWhyBatteryDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = AccentCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Why Unrestricted Battery?")
                }
            },
            text = {
                Column {
                    Text(
                        "Modern Android aggressively freezes or limits apps running in the background when battery optimization is set to 'Optimized' or 'Restricted'.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Setting Focus by Rj to 'Unrestricted' (No restrictions) ensures:",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "• Hard & Soft mode locks trigger immediately without lag.\n• Background routines start on time.\n• Android does not terminate the focus blocker service.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showWhyBatteryDialog = false
                        PermissionUtils.requestIgnoreBatteryOptimizations(context)
                    }
                ) {
                    Text("Set to Unrestricted", color = AccentCyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWhyBatteryDialog = false }) {
                    Text("Close", color = Color.Gray)
                }
            },
            containerColor = SurfaceDark,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun PermissionSetupCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isGranted: Boolean,
    highlight: Boolean = false,
    onAction: () -> Unit,
    onLearnMore: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (highlight && !isGranted) Color(0xFF221F2C) else SurfaceVariantDark)
            .border(
                1.dp,
                if (isGranted) NeonGreen.copy(alpha = 0.5f) else if (highlight) AccentRose.copy(alpha = 0.5f) else BorderGlass,
                RoundedCornerShape(18.dp)
            )
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isGranted) NeonGreen.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isGranted) Icons.Filled.CheckCircle else icon,
                            contentDescription = null,
                            tint = if (isGranted) NeonGreen else if (highlight) AccentRose else AccentCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onLearnMore != null && !isGranted) {
                    Text(
                        text = "Why is this needed?",
                        color = AccentCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clickable { onLearnMore() }
                            .padding(end = 12.dp, top = 4.dp, bottom = 4.dp)
                    )
                }

                if (isGranted) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(NeonGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Granted ✓",
                            color = NeonGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Button(
                        onClick = onAction,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (highlight) AccentRose else AccentCyan,
                            contentColor = if (highlight) Color.White else MidnightBlack
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(
                            text = if (highlight) "Set Unrestricted" else "Grant",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
