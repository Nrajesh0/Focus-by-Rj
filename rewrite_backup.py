import re

path = 'app/src/main/java/com/focusbyrj/app/ui/screens/BackupSecurityScreen.kt'
with open(path, 'r') as f:
    text = f.read()

# I want to remove the sections for "Biometric Lock", "PIN Lock", and the "Change PIN" UI.

new_content = """/*
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

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.focusbyrj.app.service.FocusDeviceAdminReceiver
import com.focusbyrj.app.ui.theme.BorderGlass
import com.focusbyrj.app.ui.theme.SurfaceDark

@Composable
fun BackupSecurityScreen(navController: NavController) {
    val context = LocalContext.current
    val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val adminComponent = ComponentName(context, FocusDeviceAdminReceiver::class.java)
    var isUninstallProtectionEnabled by remember { mutableStateOf(dpm.isAdminActive(adminComponent)) }
    
    val adminLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        isUninstallProtectionEnabled = dpm.isAdminActive(adminComponent)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Security", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Security Section
            Spacer(modifier = Modifier.height(16.dp))
            Text("APP SECURITY", style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            Spacer(modifier = Modifier.height(16.dp))
            SettingsCard(
                title = "Uninstall Protection",
                subtitle = "Prevent accidental app deletion during deep work",
                icon = Icons.Filled.Security,
                iconColor = Color(0xFFFFB74D) // Orange
            ) {
                Button(
                    onClick = {
                        if (isUninstallProtectionEnabled) {
                            dpm.removeActiveAdmin(adminComponent)
                            isUninstallProtectionEnabled = false
                        } else {
                            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
                                putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Prevent accidental app deletion during deep work sessions.")
                            }
                            adminLauncher.launch(intent)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderGlass)
                ) {
                    Text(if (isUninstallProtectionEnabled) "Disable" else "Enable", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun SettingsCard(title: String, subtitle: String, icon: ImageVector, iconColor: Color, trailing: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceDark)
            .border(1.dp, BorderGlass, RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray, lineHeight = 16.sp)
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            trailing()
        }
    }
}
"""

with open(path, 'w') as f:
    f.write(new_content)
