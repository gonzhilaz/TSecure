package com.telkomsel.secure.telkomsel_secure

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.kavsdk.antivirus.Antivirus
import com.kavsdk.antivirus.AntivirusInstance
import com.kavsdk.antivirus.MonitorConstants
import com.kavsdk.antivirus.MonitorEventListener
import com.kavsdk.antivirus.MonitoringListener
import com.kavsdk.antivirus.MonitorNotifyConstants
import com.kavsdk.antivirus.ThreatInfo
import com.kavsdk.antivirus.ThreatType
import com.telkomsel.secure.platform.ProtectionLog
import com.telkomsel.secure.telkomsel_secure.receivers.BootReceiver
import java.io.File
import kotlin.concurrent.thread

/**
 * Enterprise Real-Time Protection (RTP) Service for TelkomSecure.
 *
 * Integrates with the genuine Kaspersky Mobile SDK file monitor to detect
 * threats as files are created, modified, or moved on the device storage.
 * Runs as a foreground service with START_STICKY for persistence.
 */
class RealtimeSecurityService : Service() {

    companion object {
        private const val TAG = "TelkomRTP"
        private const val STATUS_CHANNEL_ID = "telkom_realtime_protection"
        private const val STATUS_NOTIFICATION_ID = 2026
        private const val THREAT_CHANNEL_ID = "telkom_threat_alerts"
        private const val THREAT_NOTIFICATION_ID_BASE = 3000
        private const val EXTRA_THREAT_ACTION = "extra_threat_action"

        fun start(context: Context, threatAction: String = "delete") {
            val intent = Intent(context, RealtimeSecurityService::class.java).apply {
                putExtra(EXTRA_THREAT_ACTION, threatAction)
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Throwable) {
                Log.e(TAG, "Failed to start RTP service: ${e.message}")
            }
        }

        fun stop(context: Context) {
            try {
                context.stopService(Intent(context, RealtimeSecurityService::class.java))
            } catch (_: Throwable) {}
        }

        fun isRunning(context: Context): Boolean {
            return try {
                val av = AntivirusInstance.getInstance()
                av.isInitialized && av.isMonitorActive
            } catch (_: Exception) {
                false
            }
        }
    }

    private var currentThreatAction: String = "delete"

    // --- Kaspersky SDK File Monitor Callbacks ---

    @Volatile private var heartbeatRunning = false

    private val monitorEventListener = object : MonitorEventListener {
        override fun onMonitorEvent(threatInfo: ThreatInfo, threatType: ThreatType) {
            val name = threatInfo.virusName ?: "Malware"
            val path = threatInfo.fileFullPath ?: "Unknown"
            Log.w(TAG, "THREAT DETECTED: $name at $path (Type: $threatType)")
            ProtectionLog.event(applicationContext, "RTP", "THREAT", "$name | $path | $currentThreatAction")
            MainActivity.notifyRealtimeThreat(name, threatType.toString(), path)

            showThreatNotification(threatInfo, threatType)
            handleThreatAction(threatInfo)
        }
    }

    private val monitoringListener = object : MonitoringListener {
        override fun onMonitorStart(monitorId: Int) {
            ProtectionLog.countFile(applicationContext)
        }

        override fun onMonitorStop(monitorId: Int) {
            Log.d(TAG, "Monitor task finished: $monitorId")
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        Log.d(TAG, "RealtimeSecurityService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        currentThreatAction = intent?.getStringExtra(EXTRA_THREAT_ACTION) ?: "delete"
        Log.d(TAG, "RTP start command — action: $currentThreatAction")

        // Must call startForeground immediately to avoid ANR
        startForeground(STATUS_NOTIFICATION_ID, buildStatusNotification())

        // Persist the user's RTP preference so BootReceiver can read it
        getSharedPreferences(BootReceiver.PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(BootReceiver.KEY_RTP_ENABLED, true).apply()

        // Initialize SDK monitor on a background thread to avoid main-thread freeze
        thread(start = true, name = "RTP-Init") {
            initializeMonitor()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        Log.d(TAG, "RealtimeSecurityService destroying")
        heartbeatRunning = false
        ProtectionLog.event(applicationContext, "RTP", "STOPPED", "Layanan proteksi real-time dihentikan")
        ProtectionLog.flush(applicationContext)

        // Persist preference
        getSharedPreferences(BootReceiver.PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(BootReceiver.KEY_RTP_ENABLED, false).apply()

        // Stop monitor on background thread
        thread(start = true, name = "RTP-Shutdown") {
            try {
                val av = AntivirusInstance.getInstance()
                if (av.isInitialized) {
                    av.setMonitorState(false)
                    av.setMonitorListener(null)
                    av.setMonitoringListener(null)
                    Log.d(TAG, "SDK Monitor stopped successfully")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping monitor", e)
            }
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // --- SDK Monitor Initialization ---

    private fun initializeMonitor() {
        val ctx = applicationContext
        var lastError = "Mesin antivirus belum siap"
        for (attempt in 1..5) {
            try {
                val bridge = com.telkomsel.secure.kaspersky.KasperskyNativeBridge(ctx)
                val ready = bridge.ensureAntivirusInitialized()
                val av = AntivirusInstance.getInstance()
                if (!ready || !av.isInitialized) {
                    lastError = "Mesin antivirus belum siap (percobaan $attempt/5)"
                    ProtectionLog.event(ctx, "RTP", "WAITING", lastError)
                    Thread.sleep(4000)
                    continue
                }

                av.setMonitorListener(monitorEventListener)
                av.setMonitoringListener(monitoringListener)
                av.setMonitorScanMode(MonitorConstants.ALL_FILES or MonitorConstants.ALLOW_UDS)
                av.setMonitorCleanMode(
                    if (currentThreatAction.lowercase() == "delete") MonitorConstants.DELETE_AND_LOG
                    else MonitorConstants.LOG_ONLY
                )
                av.addDefaultDirectories()
                addExtraMonitoredDirs(av)
                av.setMonitorState(true)

                val dirs = av.monitoredDirectories?.map { it.path } ?: emptyList()
                val active = av.isMonitorActive
                dirs.forEach { Log.i(TAG, "Watching: $it") }
                if (active) {
                    ProtectionLog.event(ctx, "RTP", "STARTED", "Memantau ${dirs.size} folder: ${dirs.take(4).joinToString(", ")}")
                    startHeartbeat()
                } else {
                    ProtectionLog.event(ctx, "RTP", "ERROR", "Monitor tidak aktif setelah diaktifkan (cek izin Penyimpanan Penuh)")
                }
                return
            } catch (e: Throwable) {
                lastError = e.message ?: e.javaClass.simpleName
                Log.e(TAG, "Failed to initialize SDK monitor", e)
            }
        }
        ProtectionLog.event(ctx, "RTP", "ERROR", "Gagal memulai: $lastError")
    }

    /** Every 60s record proof the monitor is still alive; one HEARTBEAT event is stored every 10 minutes. */
    private fun startHeartbeat() {
        if (heartbeatRunning) return
        heartbeatRunning = true
        thread(start = true, isDaemon = true, name = "RTP-Heartbeat") {
            var ticks = 0
            while (heartbeatRunning) {
                try { Thread.sleep(60_000) } catch (_: InterruptedException) { return@thread }
                if (!heartbeatRunning) return@thread
                ticks++
                val active = try { AntivirusInstance.getInstance().isMonitorActive } catch (_: Throwable) { false }
                ProtectionLog.flush(applicationContext)
                if (!active) {
                    ProtectionLog.event(applicationContext, "RTP", "ERROR", "Monitor berhenti tanpa diminta, mencoba menyalakan ulang")
                    try { AntivirusInstance.getInstance().setMonitorState(true) } catch (_: Throwable) {}
                } else if (ticks % 10 == 0) {
                    ProtectionLog.event(applicationContext, "RTP", "HEARTBEAT", "Monitor aktif")
                }
            }
        }
    }

    private fun addExtraMonitoredDirs(av: Antivirus) {
        val monitorFlags = MonitorNotifyConstants.NOTIFY_CREATE or
                MonitorNotifyConstants.NOTIFY_MODIFY or
                MonitorNotifyConstants.NOTIFY_CLOSE_WRITE or
                MonitorNotifyConstants.NOTIFY_MOVED_TO or
                MonitorNotifyConstants.NOTIFY_OPEN

        val extraDirs = listOf(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            File(Environment.getExternalStorageDirectory(), "WhatsApp/Media"),
            File(Environment.getExternalStorageDirectory(), "Telegram"),
        )
        for (dir in extraDirs) {
            if (dir.exists() && dir.isDirectory) {
                try {
                    av.addDirectoryToMonitor(dir.absolutePath, monitorFlags)
                    Log.i(TAG, "Added monitor dir: ${dir.absolutePath}")
                } catch (e: Exception) {
                    Log.w(TAG, "Could not add ${dir.absolutePath}: ${e.message}")
                }
            }
        }
    }

    // --- Threat Action Handling ---

    private fun handleThreatAction(threatInfo: ThreatInfo) {
        try {
            val av = AntivirusInstance.getInstance()
            when (currentThreatAction.lowercase()) {
                "quarantine" -> {
                    Log.i(TAG, "Quarantining: ${threatInfo.fileFullPath}")
                    av.addToQuarantine(threatInfo)
                }
                "delete" -> {
                    Log.i(TAG, "Deleting threat: ${threatInfo.fileFullPath}")
                    val removed = av.removeThreat(threatInfo)
                    Log.i(TAG, "Removal result: $removed")
                }
                else -> {
                    Log.i(TAG, "No action taken (mode: $currentThreatAction)")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Threat action '$currentThreatAction' failed", e)
        }
    }

    // --- Notification Channels & Builders ---

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)

            nm.createNotificationChannel(
                NotificationChannel(STATUS_CHANNEL_ID, "Real-Time Protection",
                    NotificationManager.IMPORTANCE_LOW).apply {
                    description = "Status perlindungan aktif Kaspersky Engine"
                    setShowBadge(false)
                }
            )

            nm.createNotificationChannel(
                NotificationChannel(THREAT_CHANNEL_ID, "Threat Alerts",
                    NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Peringatan saat ancaman terdeteksi secara real-time"
                    enableLights(true)
                    lightColor = android.graphics.Color.RED
                    enableVibration(true)
                }
            )
        }
    }

    private fun buildStatusNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pi = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val largeIcon = try {
            BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
        } catch (_: Exception) { null }

        val builder = NotificationCompat.Builder(this, STATUS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_shield)
            .setContentTitle("Telkomsel Secure • Aktif")
            .setContentText("Kaspersky Engine aktif melindungi perangkat")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setColor(0xFFED0226.toInt())
            .setOngoing(true)
            .setContentIntent(pi)
            .setCategory(Notification.CATEGORY_SERVICE)

        if (largeIcon != null) {
            builder.setLargeIcon(largeIcon)
        }

        return builder.build()
    }

    private fun showThreatNotification(threatInfo: ThreatInfo, threatType: ThreatType) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pi = PendingIntent.getActivity(
            this, System.currentTimeMillis().toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val actionLabel = if (currentThreatAction.lowercase() == "delete") "Dihapus" else "Dikarantina"
        val name = threatInfo.virusName ?: "Malware"
        val path = threatInfo.fileFullPath ?: "unknown"

        val largeIcon = try {
            BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
        } catch (_: Exception) { null }

        val builder = NotificationCompat.Builder(this, THREAT_CHANNEL_ID)
            .setContentTitle("🚨 Ancaman Terdeteksi!")
            .setContentText("$name ditemukan dan $actionLabel.")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("File: $path\nAncaman: $name ($threatType)\nTindakan: $actionLabel"))
            .setSmallIcon(R.drawable.ic_stat_shield)
            .setColor(0xFFED0226.toInt())
            .setContentIntent(pi)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDefaults(Notification.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(Notification.CATEGORY_ERROR)

        if (largeIcon != null) {
            builder.setLargeIcon(largeIcon)
        }

        val notification = builder.build()
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(THREAT_NOTIFICATION_ID_BASE + (System.currentTimeMillis() % 1000).toInt(), notification)
    }
}
