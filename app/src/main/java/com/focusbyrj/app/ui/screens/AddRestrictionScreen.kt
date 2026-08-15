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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.foundation.Image
import androidx.navigation.NavController
import androidx.compose.foundation.lazy.LazyRow
import com.focusbyrj.app.data.AppRestriction
import com.focusbyrj.app.ui.theme.AccentCyan
import com.focusbyrj.app.ui.theme.AccentViolet
import com.focusbyrj.app.ui.theme.BorderGlass
import com.focusbyrj.app.ui.theme.MidnightBlack
import com.focusbyrj.app.ui.theme.SurfaceDark
import com.focusbyrj.app.ui.theme.SurfaceVariantDark
import com.focusbyrj.app.ui.viewmodels.FocusViewModel
import com.focusbyrj.app.util.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class AppCategory(val title: String) {
    ALL("All"),
    SOCIAL("Social"),
    PAYMENT("Finance"),
    GAMES("Games"),
    UTILITY("Utility"),
    OTHERS("Others")
}

fun getCategoryForApp(appInfo: android.content.pm.ApplicationInfo, packageName: String): AppCategory {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        when (appInfo.category) {
            android.content.pm.ApplicationInfo.CATEGORY_SOCIAL -> return AppCategory.SOCIAL
            android.content.pm.ApplicationInfo.CATEGORY_GAME -> return AppCategory.GAMES
            android.content.pm.ApplicationInfo.CATEGORY_PRODUCTIVITY -> return AppCategory.UTILITY
        }
    }
    
    val lowerPkg = packageName.lowercase()
    if (lowerPkg.contains("whatsapp") || lowerPkg.contains("instagram") || 
        lowerPkg.contains("facebook") || lowerPkg.contains("twitter") || 
        lowerPkg.contains("tiktok") || lowerPkg.contains("snapchat") ||
        lowerPkg.contains("reddit") || lowerPkg.contains("telegram") ||
        lowerPkg.contains("discord")) {
        return AppCategory.SOCIAL
    }
    
    if (lowerPkg.contains("pay") || lowerPkg.contains("bank") || 
        lowerPkg.contains("cash") || lowerPkg.contains("wallet") ||
        lowerPkg.contains("paypal") || lowerPkg.contains("venmo") ||
        lowerPkg.contains("stripe") || lowerPkg.contains("finance")) {
        return AppCategory.PAYMENT
    }
    
    if (lowerPkg.contains("game") || lowerPkg.contains("unity") || 
        lowerPkg.contains("unreal") || lowerPkg.contains("roblox") ||
        lowerPkg.contains("mojang") || lowerPkg.contains("ea") ||
        lowerPkg.contains("supercell") || lowerPkg.contains("king") ||
        lowerPkg.contains("epic")) {
        return AppCategory.GAMES
    }
    
    if (lowerPkg.contains("tool") || lowerPkg.contains("util") || 
        lowerPkg.contains("calculator") || lowerPkg.contains("calendar") ||
        lowerPkg.contains("clock") || lowerPkg.contains("camera") ||
        lowerPkg.contains("weather") || lowerPkg.contains("notes") ||
        lowerPkg.contains("file") || lowerPkg.contains("settings") ||
        lowerPkg.contains("chrome") || lowerPkg.contains("browser") || lowerPkg.contains("drive")) {
        return AppCategory.UTILITY
    }
    
    return AppCategory.OTHERS
}

data class InstalledApp(val packageName: String, val appName: String, val category: AppCategory)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRestrictionScreen(navController: NavController, viewModel: FocusViewModel) {
    val context = LocalContext.current
    var installedApps by remember { mutableStateOf<List<InstalledApp>>(emptyList()) }
    var selectedApp by remember { mutableStateOf<InstalledApp?>(null) }
    var selectedCategory by remember { mutableStateOf(AppCategory.ALL) }
    var selectedMode by remember { mutableStateOf("HARD") }
    var customQuote by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            val apps = packages
                .filter { pm.getLaunchIntentForPackage(it.packageName) != null && it.packageName != context.packageName }
                .map { InstalledApp(it.packageName, it.loadLabel(pm).toString(), getCategoryForApp(it, it.packageName)) }
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
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(AppCategory.entries.toTypedArray()) { category ->
                            val isSelected = selectedCategory == category
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) AccentViolet else SurfaceDark)
                                    .clickable { selectedCategory = category }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = category.title,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }

                    val filteredApps = remember(installedApps, selectedCategory) {
                        if (selectedCategory == AppCategory.ALL) {
                            installedApps
                        } else {
                            installedApps.filter { it.category == selectedCategory }
                        }
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(filteredApps) { app ->
                            val pm = context.packageManager
                            val icon = remember(app.packageName) { ImageUtils.getAppIcon(pm, app.packageName) }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SurfaceDark)
                                    .border(1.dp, BorderGlass, RoundedCornerShape(16.dp))
                                    .clickable { selectedApp = app }
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (icon != null) {
                                        Image(
                                            bitmap = icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp))
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                    }
                                    Column {
                                        Text(
                                            text = app.appName,
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                        Text(
                                            text = app.category.title,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                val app = selectedApp!!
                val pm = context.packageManager
                val icon = remember(app.packageName) { ImageUtils.getAppIcon(pm, app.packageName) }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceVariantDark)
                        .border(1.dp, BorderGlass, RoundedCornerShape(24.dp))
                        .padding(24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        if (icon != null) {
                            Image(
                                bitmap = icon,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp))
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        Text(app.appName, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
                        Text(app.category.title, style = MaterialTheme.typography.labelMedium, color = AccentViolet)
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
    val backgroundColor = if (isSelected) Color(0xFF231D38) else Color(0xFF191C2B)
    val borderColor = if (isSelected) AccentViolet else Color(0xFF282D42)
    val titleColor = if (isSelected) Color.White else Color(0xFFCBD5E1)
    val subtitleColor = if (isSelected) Color(0xFFCBD5E1) else Color(0xFF94A3B8)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(backgroundColor)
            .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = titleColor
                )
                if (isSelected) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        tint = AccentCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = subtitleColor
            )
        }
    }
}