package com.nevruz.galaxysnapdragon

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
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
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class SnapdragonMonitorService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private var lastEventTime = 0L
    private var activePackage: String? = null
    private var overlay: View? = null
    private var overlayParams: WindowManager.LayoutParams? = null
    private var shownForPackage: String? = null
    private lateinit var windowManager: WindowManager
    private var expanded = false
    private var paused = false
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
                val events = manager.queryEvents((lastEventTime.takeIf { it > 0 } ?: now - 5000L), now)
                val event = UsageEvents.Event()
                while (events.hasNextEvent()) {
                    events.getNextEvent(event)
                    if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND ||
                        (Build.VERSION.SDK_INT >= 29 && event.eventType == UsageEvents.Event.ACTIVITY_RESUMED)) activePackage = event.packageName
                }
                lastEventTime = now
                val selected = getSharedPreferences("snapdragon_settings", MODE_PRIVATE).getStringSet("selected_apps", emptySet()) ?: emptySet()
                val target = activePackage?.takeIf { it in selected && it != packageName }
                if (target != null) {
                    if (shownForPackage != target) {
                        shownForPackage = target
                        val label = try { packageManager.getApplicationLabel(packageManager.getApplicationInfo(target, 0)).toString() } catch (_: Exception) { "Oyun" }
                        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(4108, buildGameNotification("Snapdragon Mode Enabled · " + label))
                    }
                    if (!paused && Settings.canDrawOverlays(this@SnapdragonMonitorService)) showOverlay()
                } else {
                    shownForPackage = null
                    removeOverlay()
                }
            } catch (_: Exception) { }
            handler.postDelayed(this, 650L)
        }
    }

    private fun buildNotification(text: String): Notification {
        val builder = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(this, channelId) else @Suppress("DEPRECATION") Notification.Builder(this)
        return builder.setSmallIcon(android.R.drawable.ic_media_play).setContentTitle("Snapdragon").setContentText(text).setOngoing(true).setOnlyAlertOnce(true).build()
    }
    private fun buildGameNotification(text: String): Notification {
        val builder = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(this, "snapdragon_game") else @Suppress("DEPRECATION") Notification.Builder(this)
        return builder.setSmallIcon(android.R.drawable.ic_media_play).setContentTitle("Snapdragon Mode Enabled").setContentText(text).setAutoCancel(true).setOnlyAlertOnce(true).build()
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
        val density = resources.displayMetrics.density
        fun dp(value: Int) = (value * density).toInt()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(9), dp(12), dp(9))
            background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(0xF21B222A.toInt(), 0xF20B1016.toInt())).apply {
                cornerRadius = dp(18).toFloat()
                setStroke(dp(1), 0xFF586472.toInt())
            }
        }
        val header = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val title = TextView(this).apply {
            text = "SNAPDRAGON"
            setTextColor(0xFFF4F6F8.toInt())
            textSize = 13f
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            letterSpacing = 0.06f
            setPadding(dp(5), dp(8), dp(12), dp(8))
        }
        val expand = TextView(this).apply { text = "＋"; setTextColor(0xFFF4F6F8.toInt()); textSize = 20f; gravity = Gravity.CENTER; setPadding(dp(8), 0, dp(8), 0); contentDescription = "Paneli genişlet" }
        val close = TextView(this).apply { text = "×"; setTextColor(0xFFCBD5E1.toInt()); textSize = 22f; gravity = Gravity.CENTER; setPadding(dp(8), 0, dp(5), 0); contentDescription = "Paneli kapat" }
        header.addView(title, LinearLayout.LayoutParams(0, dp(40), 1f))
        header.addView(expand, LinearLayout.LayoutParams(dp(38), dp(40)))
        header.addView(close, LinearLayout.LayoutParams(dp(30), dp(40)))
        root.addView(header)
        val detail = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; visibility = View.GONE; setPadding(dp(2), dp(8), dp(2), dp(2)) }
        val subtitle = TextView(this).apply { text = "OYUN ARAÇLARI"; setTextColor(0xFF9AA7B4.toInt()); textSize = 10f; typeface = Typeface.DEFAULT_BOLD; letterSpacing = 0.1f; setPadding(dp(5), dp(5), dp(5), dp(10)) }
        detail.addView(subtitle)
        val actions = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        fun action(label: String, callback: () -> Unit) {
            val button = Button(this).apply {
                text = label; textSize = 10f; isAllCaps = false; setTextColor(0xFFF1F5F9.toInt())
                backgroundTintList = ColorStateList.valueOf(0xFF303B47.toInt())
                setOnClickListener { callback() }
            }
            actions.addView(button, LinearLayout.LayoutParams(0, dp(44), 1f).apply { setMargins(dp(2), 0, dp(2), 0) })
        }
        action("Duraklat", { paused = !paused; subtitle.text = if (paused) "DURAKLATILDI" else "OYUN ARAÇLARI"; actions.getChildAt(0).let { (it as? Button)?.text = if (paused) "Devam" else "Duraklat" } })
        action("Gizle", { removeOverlay() })
        action("Durdur", { stopSelf() })
        detail.addView(actions)
        root.addView(detail)
        expand.setOnClickListener { expanded = !expanded; detail.visibility = if (expanded) View.VISIBLE else View.GONE; expand.text = if (expanded) "−" else "＋"; overlayParams?.let { try { windowManager.updateViewLayout(root, it) } catch (_: Exception) {} } }
        close.setOnClickListener { removeOverlay() }
        header.setOnTouchListener { _, event ->
            val p = overlayParams ?: return@setOnTouchListener false
            when (event.action) {
                MotionEvent.ACTION_DOWN -> { dragDownX = event.rawX; dragDownY = event.rawY; dragStartX = p.x; dragStartY = p.y; true }
                MotionEvent.ACTION_MOVE -> { p.x = dragStartX + (dragDownX - event.rawX).toInt(); p.y = dragStartY + (event.rawY - dragDownY).toInt(); try { windowManager.updateViewLayout(root, p) } catch (_: Exception) {}; true }
                MotionEvent.ACTION_UP -> false
                else -> false
            }
        }
        val type = if (Build.VERSION.SDK_INT >= 26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE
        val params = WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, type, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT).apply { gravity = Gravity.TOP or Gravity.END; x = dp(12); y = dp(170) }
        try { windowManager.addView(root, params); overlay = root; overlayParams = params } catch (_: Exception) {}
    }

    private var dragDownX = 0f
    private var dragDownY = 0f
    private var dragStartX = 0
    private var dragStartY = 0

    private fun removeOverlay() {
        val view = overlay ?: return
        try { windowManager.removeView(view) } catch (_: Exception) {}
        overlay = null
        overlayParams = null
        expanded = false
    }
    override fun onDestroy() { handler.removeCallbacks(pollTask); removeOverlay(); super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = null
}
