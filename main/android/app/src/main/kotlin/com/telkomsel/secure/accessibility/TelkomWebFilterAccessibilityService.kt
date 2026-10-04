package com.telkomsel.secure.accessibility

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.app.NotificationCompat
import com.telkomsel.secure.kaspersky.KasperskyNativeBridge
import com.telkomsel.secure.platform.ProtectionLog
import com.telkomsel.secure.telkomsel_secure.MainActivity
import com.telkomsel.secure.telkomsel_secure.R

/**
 * Enterprise Realtime Web Filter Accessibility Service for TelkomSecure.
 * Inspects URLs in active browsers (Chrome, Edge, Samsung Internet, Firefox),
 * checks them against Kaspersky Security Network (KSN), and warns against phishing & malware.
 */
class TelkomWebFilterAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "TelkomWebFilter"
        private const val NOTIF_CHANNEL_ID = "telkom_web_filter_alerts"
        private var lastCheckedUrl: String = ""
        private var lastCheckedTime: Long = 0
        var isServiceRunning = false
            private set
    }

    private lateinit var kasperskyBridge: KasperskyNativeBridge

    override fun onServiceConnected() {
        super.onServiceConnected()
        isServiceRunning = true
        kasperskyBridge = KasperskyNativeBridge(applicationContext)
        createNotificationChannel()
        ProtectionLog.event(applicationContext, "WEB", "STARTED", "Layanan aksesibilitas Web Filter aktif")
        Log.i(TAG, ">>> TelkomSecure Web Filter Accessibility Service CONNECTED & ACTIVE")
        if (ProtectionLog.consumeAccessibilityExpectation(applicationContext)) {
            try {
                startActivity(Intent(this, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                })
            } catch (_: Throwable) {}
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val rootNode = rootInActiveWindow ?: return

        try {
            val url = extractUrlFromNode(rootNode)
            if (!url.isNullOrBlank() && url.length > 4 && (url.contains(".") || url.startsWith("http"))) {
                val now = System.currentTimeMillis()
                if (url != lastCheckedUrl || (now - lastCheckedTime) > 10000) {
                    lastCheckedUrl = url
                    lastCheckedTime = now
                    inspectUrlInBackground(url)
                }
            }
        } catch (e: Throwable) {
            Log.w(TAG, "Event processing note: ${e.message}")
        }
    }

    private fun extractUrlFromNode(node: AccessibilityNodeInfo): String? {
        val viewId = node.viewIdResourceName?.lowercase() ?: ""
        val text = node.text?.toString()?.trim()
        val desc = node.contentDescription?.toString()?.trim()

        if (viewId.contains("url") || viewId.contains("address") ||
            viewId.contains("location") || viewId.contains("search") ||
            viewId.contains("toolbar") || viewId.contains("omnibox")
        ) {
            if (!text.isNullOrBlank() && (text.contains(".") || text.startsWith("http"))) return text
            if (!desc.isNullOrBlank() && (desc.contains(".") || desc.startsWith("http"))) return desc
        }

        if (!text.isNullOrBlank()) {
            if (text.startsWith("http://", true) || text.startsWith("https://", true) ||
                text.contains("kaspersky.com/test/wmuf", true) || text.contains("/test/wmuf", true)
            ) return text
        }

        val childCount = node.childCount
        for (i in 0 until childCount) {
            val child = node.getChild(i) ?: continue
            val found = extractUrlFromNode(child)
            if (!found.isNullOrBlank()) return found
        }
        return null
    }

    private fun inspectUrlInBackground(rawUrl: String) {
        Thread {
            try {
                val formattedUrl = if (!rawUrl.startsWith("http://") && !rawUrl.startsWith("https://")) {
                    "https://$rawUrl"
                } else {
                    rawUrl
                }

                val result = kasperskyBridge.checkUrl(formattedUrl)
                ProtectionLog.countUrl(applicationContext)
                val isPhishing = result["isPhishing"] as? Boolean ?: false
                val isMalware = result["isMalware"] as? Boolean ?: false
                val isSafe = result["isSafe"] as? Boolean ?: true

                if (!isSafe || isPhishing || isMalware) {
                    val verdict = if (isPhishing) "Situs Phishing / Penipuan" else "Situs Penyebar Malware"
                    Log.w(TAG, "[WEB FILTER BLOCKED] Threat detected: $formattedUrl ($verdict)")
                    ProtectionLog.event(applicationContext, "WEB", "BLOCKED", "$verdict | $formattedUrl")

                    // 1. Immediately force browser BACK to navigate away from dangerous page
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        try {
                            performGlobalAction(GLOBAL_ACTION_BACK)
                        } catch (e: Throwable) {
                            Log.w(TAG, "Global back action failed: ${e.message}")
                        }
                    }

                    // 2. High priority notification
                    showThreatAlertNotification(formattedUrl, verdict)

                    // 3. Notify Flutter UI and SOC telemetry
                    MainActivity.notifyUrlThreat(formattedUrl, verdict, "BLOCKED")

                    // 4. Bring TelkomSecure to front with alert dialog
                    try {
                        val alertIntent = Intent(applicationContext, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            putExtra("blocked_url", formattedUrl)
                            putExtra("blocked_reason", verdict)
                        }
                        startActivity(alertIntent)
                    } catch (_: Throwable) {}
                }
            } catch (e: Throwable) {
                Log.e(TAG, "Error checking URL: ${e.message}")
            }
        }.start()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIF_CHANNEL_ID,
                "TelkomSecure Web Protection Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Peringatan real-time saat mendeteksi situs phishing atau malware di peramban"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    private fun showThreatAlertNotification(url: String, threatReason: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("blocked_url", url)
            putExtra("blocked_reason", threatReason)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val largeIcon = try {
            android.graphics.BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
        } catch (_: Exception) { null }

        val builder = NotificationCompat.Builder(this, NOTIF_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_shield)
            .setColor(0xFFED0226.toInt())
            .setContentTitle("ANCAMAN TERDETEKSI: $threatReason")
            .setContentText("Akses ke $url dicegat oleh Telkomsel Secure.")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Telkomsel Secure Web Filter memblokir akses ke situs berbahaya:\n$url\n\nKaspersky Security Network (KSN) mendeteksi indikasi bahaya. Tab peramban telah ditutup otomatis."))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDefaults(Notification.DEFAULT_ALL)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pendingIntent, true)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        if (largeIcon != null) builder.setLargeIcon(largeIcon)

        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify((System.currentTimeMillis() % 100000).toInt(), builder.build())
    }

    override fun onInterrupt() {
        isServiceRunning = false
        Log.w(TAG, "TelkomSecure Web Filter Accessibility Service INTERRUPTED")
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        ProtectionLog.event(applicationContext, "WEB", "STOPPED", "Layanan aksesibilitas Web Filter dimatikan")
    }
}
