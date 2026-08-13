package com.focusbyrj.app.service

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.focusbyrj.app.util.TemporaryUnlockManager
import android.graphics.drawable.GradientDrawable

object BlockOverlayManager {
    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var currentPackageName: String? = null
    private var handler = Handler(Looper.getMainLooper())
    private var timeLeft = 10
    private var countdownRunnable: Runnable? = null
    private var isShowing = false

    fun showOverlay(context: Context, packageName: String, quote: String, mode: String) {
        if (isShowing && currentPackageName == packageName) return
        
        hideOverlay() // Ensure previous is removed
        
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            @Suppress("DEPRECATION")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#14151D"))
            gravity = Gravity.CENTER
            setPadding(64, 64, 64, 64)
        }

        val title = TextView(context).apply {
            text = "Pause."
            textSize = 36f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 48)
        }
        layout.addView(title)

        val quoteView = TextView(context).apply {
            text = quote
            textSize = 20f
            setTextColor(Color.parseColor("#00E5FF"))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 96)
        }
        layout.addView(quoteView)

        if (mode == "HARD") {
            val hardText = TextView(context).apply {
                text = "HARD SHIELD ACTIVE"
                textSize = 24f
                setTextColor(Color.parseColor("#FF5252"))
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, 64)
            }
            layout.addView(hardText)

            val exitBtn = Button(context).apply {
                text = "Exit App"
                setTextColor(Color.WHITE)
                background = GradientDrawable().apply {
                    setColor(Color.parseColor("#FF5252"))
                    cornerRadius = 32f
                }
                setOnClickListener {
                    goHome(context)
                }
            }
            layout.addView(exitBtn)
        } else {
            timeLeft = context.getSharedPreferences("focus_prefs", Context.MODE_PRIVATE).getInt("soft_lock_duration", 10)
            val timeText = TextView(context).apply {
                text = timeLeft.toString()
                textSize = 48f
                setTextColor(Color.LTGRAY)
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, 64)
            }
            layout.addView(timeText)

            val actionBtn = Button(context).apply {
                text = "Wait ${timeLeft}s..."
                setTextColor(Color.WHITE)
                isEnabled = false
                background = GradientDrawable().apply {
                    setColor(Color.parseColor("#424242"))
                    cornerRadius = 32f
                }
            }
            layout.addView(actionBtn)

            countdownRunnable = object : Runnable {
                override fun run() {
                    if (timeLeft > 0) {
                        timeLeft--
                        timeText.text = timeLeft.toString()
                        actionBtn.text = "Wait ${timeLeft}s..."
                        handler.postDelayed(this, 1000)
                    } else {
                        actionBtn.isEnabled = true
                        actionBtn.text = "Opening App..."
                        actionBtn.background = GradientDrawable().apply {
                            setColor(Color.parseColor("#00E5FF"))
                            cornerRadius = 32f
                        }
                        actionBtn.setTextColor(Color.BLACK)
                        actionBtn.setOnClickListener {
                            TemporaryUnlockManager.grantUnlock(context, packageName, 5)
                            hideOverlay()
                            // No need to launch intent, just hide overlay since app is already below!
                        }
                    }
                }
            }
            handler.postDelayed(countdownRunnable!!, 1000)
        }

        overlayView = layout
        currentPackageName = packageName
        isShowing = true
        
        try {
            windowManager?.addView(overlayView, params)
        } catch (e: Exception) {
            e.printStackTrace()
            isShowing = false
        }
    }

    fun hideOverlay() {
        if (!isShowing) return
        try {
            countdownRunnable?.let { handler.removeCallbacks(it) }
            overlayView?.let { windowManager?.removeView(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        overlayView = null
        isShowing = false
        currentPackageName = null
    }

    private fun goHome(context: Context) {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(homeIntent)
        hideOverlay()
    }
}
