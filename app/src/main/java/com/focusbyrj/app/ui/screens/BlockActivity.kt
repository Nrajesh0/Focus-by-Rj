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

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.WindowCompat
import com.focusbyrj.app.service.FocusExitTracker
import com.focusbyrj.app.ui.theme.*
import com.focusbyrj.app.util.FocusQuotes
import com.focusbyrj.app.util.TemporaryUnlockManager
import kotlinx.coroutines.delay

class BlockActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
        )
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        
        window.statusBarColor = android.graphics.Color.BLACK
        window.navigationBarColor = android.graphics.Color.BLACK
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }
        
        enableEdgeToEdge()

        val packageName = intent.getStringExtra("package_name") ?: ""
        val quote = intent.getStringExtra("quote") ?: ""
        val mode = intent.getStringExtra("mode") ?: "HARD"

        setContent {
            FocusByRjTheme {
                BlockScreenContent(
                    packageName = packageName,
                    quote = quote,
                    mode = mode,
                    onExit = { goHome(packageName) },
                    onUnlock = {
                        val unlockMins = getSharedPreferences("focus_prefs", Context.MODE_PRIVATE)
                            .getInt("soft_unlock_duration", 5)
                        TemporaryUnlockManager.grantUnlock(this, packageName, unlockMins)
                        finish()
                    }
                )
            }
        }
    }

    private fun goHome(pkgName: String) {
        FocusExitTracker.notifyExited(pkgName)
        try {
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            }
            startActivity(homeIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        moveTaskToBack(true)
        finish()
    }
}

@Composable
fun BlockScreenContent(
    packageName: String,
    quote: String,
    mode: String,
    onExit: () -> Unit,
    onUnlock: () -> Unit
) {
    val isHardMode = mode.equals("HARD", ignoreCase = true)
    val context = LocalContext.current
    
    var appLabel by remember(packageName) { mutableStateOf(packageName) }
    var appIconDrawable by remember(packageName) { mutableStateOf<Drawable?>(null) }
    
    val displayedQuote = remember(quote) {
        FocusQuotes.getQuoteOrDefault(quote)
    }

    LaunchedEffect(packageName) {
        if (packageName.isNotBlank()) {
            try {
                val pm = context.packageManager
                val info = pm.getApplicationInfo(packageName, 0)
                appLabel = pm.getApplicationLabel(info).toString()
                appIconDrawable = pm.getApplicationIcon(info)
            } catch (e: Exception) {
                appLabel = packageName.substringAfterLast('.').replaceFirstChar { it.uppercase() }
            }
        }
    }

    val totalSoftLockSeconds = remember {
        context.getSharedPreferences("focus_prefs", Context.MODE_PRIVATE)
            .getInt("soft_lock_duration", 10)
    }

    val unlockDurationMinutes = remember {
        context.getSharedPreferences("focus_prefs", Context.MODE_PRIVATE)
            .getInt("soft_unlock_duration", 5)
    }

    var timeLeft by remember { mutableIntStateOf(if (isHardMode) 0 else totalSoftLockSeconds) }

    LaunchedEffect(isHardMode) {
        if (!isHardMode) {
            while (timeLeft > 0) {
                delay(1000L)
                timeLeft -= 1
            }
        }
    }

    BackHandler {
        onExit()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF000000),
                        Color(0xFF050608),
                        Color(0xFF020304)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFF0C0D14).copy(alpha = 0.95f))
                    .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(28.dp))
                    .padding(horizontal = 28.dp, vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF141620))
                            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (appIconDrawable != null) {
                            Image(
                                bitmap = appIconDrawable!!.toBitmap(68, 68).asImageBitmap(),
                                contentDescription = appLabel,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(14.dp))
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = null,
                                tint = if (isHardMode) AccentRose else Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = appLabel,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.2.sp
                        ),
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = if (isHardMode) "Focus Shielded." else "Pause & Reflect.",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp
                        ),
                        color = if (isHardMode) AccentRose else Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "“$displayedQuote”",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.Normal,
                                lineHeight = 22.sp,
                                letterSpacing = 0.2.sp
                            ),
                            color = Color(0xFFCBD5E1),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (isHardMode) {
                        Text(
                            text = "This app is strictly locked to honor your focus commitment.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(26.dp))

                        Button(
                            onClick = onExit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentRose,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(26.dp)
                        ) {
                            Text(
                                "Exit to Home",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    } else {
                        AnimatedContent(
                            targetState = timeLeft > 0,
                            transitionSpec = { fadeIn(tween(350)) togetherWith fadeOut(tween(250)) },
                            label = "TimerTransition"
                        ) { isCountingDown ->
                            if (isCountingDown) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val progress by animateFloatAsState(
                                        targetValue = if (totalSoftLockSeconds > 0) timeLeft.toFloat() / totalSoftLockSeconds.toFloat() else 0f,
                                        label = "ProgressAnimation"
                                    )

                                    Text(
                                        text = if (timeLeft < 10) "00:0$timeLeft" else "00:$timeLeft",
                                        style = MaterialTheme.typography.displaySmall.copy(
                                            fontWeight = FontWeight.Light,
                                            letterSpacing = 4.sp
                                        ),
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.55f)
                                            .height(3.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(Color.White.copy(alpha = 0.08f))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(progress)
                                                .fillMaxHeight()
                                                .background(
                                                    Brush.horizontalGradient(
                                                        listOf(Color(0xFF6366F1), AccentCyan, Color(0xFF38BDF8))
                                                    )
                                                )
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = "Mindful pause in progress",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            letterSpacing = 1.sp
                                        ),
                                        color = TextSecondary,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(26.dp))

                                    OutlinedButton(
                                        onClick = onExit,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                                        shape = RoundedCornerShape(25.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = Color(0xFF10121B),
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Text(
                                            "Exit to Home",
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                                        )
                                    }
                                }
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Pause Completed",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            letterSpacing = 0.5.sp
                                        ),
                                        color = Color(0xFFE2E8F0),
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "Would you like to open $appLabel for $unlockDurationMinutes minutes or exit?",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 22.sp
                                    )

                                    Spacer(modifier = Modifier.height(26.dp))

                                    Button(
                                        onClick = onUnlock,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.White,
                                            contentColor = Color(0xFF08090E)
                                        ),
                                        shape = RoundedCornerShape(26.dp)
                                    ) {
                                        Text(
                                            "Open for $unlockDurationMinutes Minutes",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(Icons.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedButton(
                                        onClick = onExit,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                                        shape = RoundedCornerShape(25.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = Color(0xFF10121B),
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Text(
                                            "Exit to Home",
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
