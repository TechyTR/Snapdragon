package com.nevruz.galaxysnapdragon

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.TextView

class SnapdragonMonitorService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private var lastEventTime = 0L
    private var activePackage: String? = null
    private var overlay: TextView? = null
    private var overlayParams: WindowManager.LayoutParams? = null
    private var shownForPackage: String? = null
    private lateinit var windowManager: WindowManager
    private val channelId = "snapdragon_monitor"

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createChannel()
        startForeground(4107, buildNotification("Hazır · seçili uygulamalar bekleniyor"))
        handler.post(pollTask)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    private val pollTask = object : Runnable {
        override fun run() {
            try {
                val now = System.currentTimeMillis()
                val manager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                val events = manager.queryEvents((lastEventTime.takeIf { it > 0 } ?: (now - 5000L)), now)
                val event = UsageEvents.Event()
                while (events.hasNextEvent()) {
                    events.getNextEvent(event)
                    if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND ||
                        (Build.VERSION.SDK_INT >= 29 && event.eventType == UsageEvents.Event.ACTIVITY_RESUMED)) {
                        activePackage = event.packageName
                    }
                }
                lastEventTime = now
                val prefs = getSharedPreferences("snapdragon_settings", MODE_PRIVATE)
                val selected = prefs.getStringSet("selected_apps", emptySet()) ?: emptySet()
                val target = activePackage?.takeIf { it in selected && it != packageName }
                if (target != null) {
                    if (shownForPackage != target) {
                        shownForPackage = target
                        val label = try { packageManager.getApplicationLabel(packageManager.getApplicationInfo(target, 0)).toString() } catch (_: Exception) { "Oyun" }
                        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                        nm.notify(4108, buildNotification("Snapdragon Mode Enabled · $label", ongoing = false))
                    }
                    if (Settings.canDrawOverlays(this@SnapdragonMonitorService)) showOverlay()
                } else {
                    shownForPackage = null
                    removeOverlay()
                }
            } catch (_: Exception) { }
            handler.postDelayed(this, 650L)
        }
    }

    private fun buildNotification(text: String, ongoing: Boolean = true): Notification {
        val builder = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(this, channelId) else @Suppress("DEPRECATION") Notification.Builder(this)
        return builder.setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("Snapdragon")
            .setContentText(text)
            .setOngoing(ongoing)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(NotificationChannel(channelId, "Snapdragon oyun algılama", NotificationManager.IMPORTANCE_LOW))
            nm.createNotificationChannel(NotificationChannel("snapdragon_game", "Snapdragon oyun bildirimleri", NotificationManager.IMPORTANCE_HIGH))
        }
    }

    private fun showOverlay() {
        if (overlay != null) return
        val view = TextView(this).apply {
            text = "⚡  SNAPDRAGON  ·  PANEL"
            setTextColor(0xFF42E8FF.toInt())
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(20, 13, 20, 13)
            background = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(0xEE101A2B.toInt(), 0xEE25204A.toInt())).apply {
                cornerRadius = 60f
                setStroke(2, 0xFF42E8FF.toInt())
            }
            setOnClickListener {
                text = if (text.toString().contains("PANEL")) "Ⅱ  DURAKLAT   ·   ■  DURDUR" else "⚡  SNAPDRAGON  ·  PANEL"
            }
        }
        val type = if (Build.VERSION.SDK_INT >= 26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE
        val params = WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, type, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 18
            y = 180
        }
        var downX = 0f
        var downY = 0f
        var startX = 0
        var startY = 0
        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> { downX = event.rawX; downY = event.rawY; startX = params.x; startY = params.y; true }
                MotionEvent.ACTION_MOVE -> { params.x = startX + (downX - event.rawX).toInt(); params.y = startY + (event.rawY - downY).toInt(); try { windowManager.updateViewLayout(view, params) } catch (_: Exception) {}; true }
                MotionEvent.ACTION_UP -> { if (kotlin.math.abs(event.rawX - downX) < 8f && kotlin.math.abs(event.rawY - downY) < 8f) view.performClick(); true }
                else -> true
            }
        }
        try {
            windowManager.addView(view, params)
            overlay = view
            overlayParams = params
        } catch (_: Exception) { }
    }

    private fun removeOverlay() {
        val view = overlay ?: return
        try { windowManager.removeView(view) } catch (_: Exception) { }
        overlay = null
        overlayParams = null
    }

    override fun onDestroy() {
        handler.removeCallbacks(pollTask)
        removeOverlay()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
