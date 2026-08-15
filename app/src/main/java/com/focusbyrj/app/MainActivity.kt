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

package com.focusbyrj.app

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.remember
import android.os.Bundle
import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.focusbyrj.app.ui.navigation.Screen
import com.focusbyrj.app.ui.screens.AnalyticsScreen
import com.focusbyrj.app.ui.screens.DashboardScreen
import com.focusbyrj.app.ui.screens.SchedulesScreen
import com.focusbyrj.app.ui.screens.TimeScreen
import com.focusbyrj.app.ui.screens.SecurityScreen
import com.focusbyrj.app.ui.screens.PermissionsScreen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import com.focusbyrj.app.ui.theme.AccentViolet
import com.focusbyrj.app.ui.theme.AccentEmerald
import com.focusbyrj.app.ui.theme.BorderGlass
import com.focusbyrj.app.ui.screens.AddRestrictionScreen
import com.focusbyrj.app.ui.screens.SettingsScreen
import com.focusbyrj.app.ui.components.SetupPermissionsDialog
import com.focusbyrj.app.ui.theme.FocusByRjTheme
import com.focusbyrj.app.ui.theme.SurfaceDark
import com.focusbyrj.app.ui.theme.AccentCyan
import com.focusbyrj.app.ui.theme.NeonGreen
import com.focusbyrj.app.ui.viewmodels.FocusViewModel
import com.focusbyrj.app.ui.viewmodels.FocusViewModelFactory

class MainActivity : FragmentActivity() {
  
  lateinit var viewModel: FocusViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    val app = application as FocusApplication
    val vm: FocusViewModel by viewModels {
        FocusViewModelFactory(app.repository, app)
    }
    viewModel = vm

    com.focusbyrj.app.service.FocusBlockerService.startService(this)
    
    setContent {
      FocusByRjTheme {
        MainAppScreen(viewModel)
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: FocusViewModel) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    val isSessionActive by viewModel.isSessionActive.collectAsStateWithLifecycle()

    val items = listOf(
        Screen.Dashboard,
        Screen.Schedules,
        Screen.Analytics,
        Screen.Time
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("focus_prefs", Context.MODE_PRIVATE) }
    
    var hasUsageStats by remember { mutableStateOf(com.focusbyrj.app.util.PermissionUtils.hasUsageStatsPermission(context)) }
    var hasOverlay by remember { mutableStateOf(com.focusbyrj.app.util.PermissionUtils.hasOverlayPermission(context)) }
    var isBatteryUnrestricted by remember { mutableStateOf(com.focusbyrj.app.util.PermissionUtils.isIgnoringBatteryOptimizations(context)) }
    
    var showSetupDialog by remember { 
        val hasSeenOnboarding = prefs.getBoolean("has_seen_permission_onboarding", false)
        mutableStateOf(!hasSeenOnboarding && (!hasUsageStats || !hasOverlay || !isBatteryUnrestricted))
    }

    val allPermissionsGranted = hasUsageStats && hasOverlay

    androidx.compose.runtime.LaunchedEffect(allPermissionsGranted) {
        if (allPermissionsGranted) {
            com.focusbyrj.app.service.FocusBlockerService.startService(context)
        }
    }
    
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                hasUsageStats = com.focusbyrj.app.util.PermissionUtils.hasUsageStatsPermission(context)
                hasOverlay = com.focusbyrj.app.util.PermissionUtils.hasOverlayPermission(context)
                isBatteryUnrestricted = com.focusbyrj.app.util.PermissionUtils.isIgnoringBatteryOptimizations(context)
                
                if (hasUsageStats && hasOverlay) {
                    com.focusbyrj.app.service.FocusBlockerService.startService(context)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (showSetupDialog) {
        SetupPermissionsDialog(
            hasUsageStats = hasUsageStats,
            hasOverlay = hasOverlay,
            isBatteryUnrestricted = isBatteryUnrestricted,
            onDismiss = {
                prefs.edit().putBoolean("has_seen_permission_onboarding", true).apply()
                showSetupDialog = false
            }
        )
    }
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(280.dp),
                drawerContainerColor = MaterialTheme.colorScheme.background
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(24.dp)
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(24.dp))
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(androidx.compose.ui.res.painterResource(id = com.focusbyrj.app.R.drawable.ic_launcher_foreground), contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Focus Options",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            "Manage your boundaries",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.Security, contentDescription = null, tint = AccentEmerald) },
                    label = { Text("Security", color = Color.White, style = MaterialTheme.typography.bodyLarge) },
                    selected = false,
                    onClick = { 
                         scope.launch { drawerState.close() }
                                                navController.navigate(Screen.Security.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = AccentViolet) },
                    label = { 
                         Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Permissions", color = Color.White, style = MaterialTheme.typography.bodyLarge)
                            if (hasUsageStats && hasOverlay && isBatteryUnrestricted) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = "Configured", tint = NeonGreen, modifier = Modifier.size(16.dp))
                            } else {
                                Icon(Icons.Filled.Warning, contentDescription = "Action needed", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    selected = false,
                    onClick = { 
                         scope.launch { drawerState.close() }
                                                navController.navigate(Screen.Permissions.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                )
                
                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.Palette, contentDescription = null, tint = AccentCyan) },
                    label = { Text("App Settings", color = Color.White, style = MaterialTheme.typography.bodyLarge) },
                    selected = false,
                    onClick = { 
                         scope.launch { drawerState.close() }
                                                navController.navigate(Screen.Settings.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                )

                HorizontalDivider(color = BorderGlass, modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp))
                
                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    label = { Text("Setup Guide", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        showSetupDialog = true
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                )
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                if (!isSessionActive) {
                    TopAppBar(
                        title = { 
                            Text(
                                text = "Focus by Rj", 
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Light,
                                    letterSpacing = 2.5.sp,
                                    fontSize = 21.sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            ) 
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onBackground)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                    )
                }
            },
            bottomBar = {
                val hideBottomBarRoutes = listOf(Screen.AddRestriction.route, Screen.Security.route, Screen.Permissions.route, Screen.Settings.route)
                if (currentDestination?.route !in hideBottomBarRoutes && !isSessionActive) {
                    NavigationBar(
                        containerColor = SurfaceDark,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ) {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title, style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp)) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AccentCyan,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                selectedTextColor = AccentCyan,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = Color.Transparent
                            ),
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                    }
                }
            },
            floatingActionButton = {
                if (currentDestination?.route == Screen.Dashboard.route) {
                    FloatingActionButton(
                        onClick = { navController.navigate(Screen.AddRestriction.route) },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Restriction")
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Dashboard.route) {
                    val restrictions by viewModel.combinedRestrictions.collectAsStateWithLifecycle()
                    val timeRemaining by viewModel.timeRemaining.collectAsStateWithLifecycle()
                    val initialTime by viewModel.initialTime.collectAsStateWithLifecycle()

                    DashboardScreen(
                        restrictions = restrictions,
                        onToggle = { app: com.focusbyrj.app.data.AppRestriction -> viewModel.toggleRestriction(app) },
                        onDelete = { app: com.focusbyrj.app.data.AppRestriction -> viewModel.deleteRestriction(app) },
                        onUpdate = { app: com.focusbyrj.app.data.AppRestriction -> viewModel.updateRestriction(app) },
                        isSessionActive = isSessionActive,
                        timeRemaining = timeRemaining,
                        initialTime = initialTime,
                        onToggleSession = { viewModel.toggleFocusSession() },
                        onSetTime = { time: Int -> viewModel.setTimeRemaining(time) }
                    )
                }
                composable(Screen.Schedules.route) {
                    SchedulesScreen(viewModel)
                }
                composable(Screen.Analytics.route) {
                    AnalyticsScreen()
                }
                composable(Screen.Time.route) {
                    TimeScreen()
                }
                composable(Screen.AddRestriction.route) {
                    AddRestrictionScreen(navController, viewModel)
                }
                composable(Screen.Security.route) {
                    SecurityScreen(navController)
                }
                composable(Screen.Permissions.route) {
                    PermissionsScreen(navController)
                }
                composable(Screen.Settings.route) { com.focusbyrj.app.ui.screens.SettingsScreen(navController) }
            }
        }
    }
}


