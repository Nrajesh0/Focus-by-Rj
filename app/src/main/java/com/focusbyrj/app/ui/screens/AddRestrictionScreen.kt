package com.focusbyrj.app.ui.screens

import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import androidx.navigation.NavController
import com.focusbyrj.app.data.AppRestriction
import com.focusbyrj.app.ui.theme.AccentCyan
import com.focusbyrj.app.ui.theme.AccentViolet
import com.focusbyrj.app.ui.theme.BorderGlass
import com.focusbyrj.app.ui.theme.MidnightBlack
import com.focusbyrj.app.ui.theme.SurfaceDark
import com.focusbyrj.app.ui.theme.SurfaceVariantDark
import com.focusbyrj.app.ui.viewmodels.FocusViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class InstalledApp(val packageName: String, val appName: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRestrictionScreen(navController: NavController, viewModel: FocusViewModel) {
    val context = LocalContext.current
    var installedApps by remember { mutableStateOf<List<InstalledApp>>(emptyList()) }
    var selectedApp by remember { mutableStateOf<InstalledApp?>(null) }
    var selectedMode by remember { mutableStateOf("HARD") }
    var customQuote by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            val apps = packages
                .filter { pm.getLaunchIntentForPackage(it.packageName) != null && it.packageName != context.packageName }
                .map { InstalledApp(it.packageName, it.loadLabel(pm).toString()) }
                .sortedBy { it.appName }
            installedApps = apps
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shield New App", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MidnightBlack, titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        },
        containerColor = MidnightBlack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (selectedApp == null) {
                Text(
                    text = "SELECT APP TO SHIELD",
                    style = MaterialTheme.typography.labelMedium,
                    color = AccentCyan,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AccentViolet)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(installedApps) { app ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SurfaceDark)
                                    .border(1.dp, BorderGlass, RoundedCornerShape(16.dp))
                                    .clickable { selectedApp = app }
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = app.appName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            } else {
                // Configure Restriction
                val app = selectedApp!!
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceVariantDark)
                        .border(1.dp, BorderGlass, RoundedCornerShape(24.dp))
                        .padding(24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(app.appName, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
                        Text(app.packageName, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { selectedApp = null }) {
                            Text("Change App", color = AccentCyan)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "RESTRICTION MODE",
                    style = MaterialTheme.typography.labelMedium,
                    color = AccentCyan,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ModeSelector(
                        title = "HARD MODE",
                        description = "No bypass allowed",
                        isSelected = selectedMode == "HARD",
                        onClick = { selectedMode = "HARD" },
                        modifier = Modifier.weight(1f)
                    )
                    ModeSelector(
                        title = "SOFT MODE",
                        description = "10 sec wait bypass",
                        isSelected = selectedMode == "SOFT",
                        onClick = { selectedMode = "SOFT" },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "CUSTOM QUOTE (OPTIONAL)",
                    style = MaterialTheme.typography.labelMedium,
                    color = AccentCyan,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                OutlinedTextField(
                    value = customQuote,
                    onValueChange = { customQuote = it },
                    placeholder = { Text("Why are you blocking this app?") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceDark,
                        unfocusedContainerColor = SurfaceDark,
                        focusedBorderColor = AccentViolet,
                        unfocusedBorderColor = BorderGlass,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        val newRestriction = AppRestriction(
                            packageName = app.packageName,
                            appName = app.appName,
                            mode = selectedMode,
                            customQuote = customQuote.trim(),
                            isRestricted = true
                        )
                        viewModel.addRestriction(newRestriction)
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = MidnightBlack),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text("Enable Shield", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

@Composable
fun ModeSelector(title: String, description: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) AccentViolet.copy(alpha = 0.2f) else SurfaceDark)
            .border(2.dp, if (isSelected) AccentViolet else BorderGlass, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(title, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = if (isSelected) Color.White else Color.Gray)
                if (isSelected) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}