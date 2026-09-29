package com.telkomsel.secure.telkomsel_secure

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import com.telkomsel.secure.kaspersky.KasperskyNativeBridge

class MainActivity : FlutterActivity() {

    private lateinit var kasperskyBridge: KasperskyNativeBridge
    private var kasperskyChannel: MethodChannel? = null

    companion object {
        private const val TAG = "TelkomSecure-Native"
        private const val CHANNEL_BLACKWALL = "com.telkomsel.secure/blackwall"
        private const val CHANNEL_KASPERSKY = "com.taspenguard/ksp"
        private const val NOTIF_CHANNEL_ID = "telkom_security_threats"
        private const val REQUEST_CODE_NOTIF = 1001
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        createNotificationChannel()
        kasperskyBridge = KasperskyNativeBridge(applicationContext)

        val kspChannel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL_KASPERSKY)
        kasperskyChannel = kspChannel

        // 1. Channel com.telkomsel.secure/blackwall (Hardware & Device Telemetry)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL_BLACKWALL).setMethodCallHandler { call, result ->
            when (call.method) {
                "isShieldActive" -> {
                    val active = kasperskyBridge.ensureAntivirusInitialized()
                    result.success(active)
                }
                "getThreatBitmask" -> {
                    val rootInfo = kasperskyBridge.checkRoot()
                    val isRooted = rootInfo["isRooted"] as? Boolean ?: false
                    result.success(if (isRooted) 1 else 0)
                }
                "getDeviceInfo" -> {
                    try {
                        val manufacturer = Build.MANUFACTURER.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                        val model = Build.MODEL
                        val deviceModel = if (model.startsWith(manufacturer, ignoreCase = true)) model else "$manufacturer $model"
                        val osVersion = "Android ${Build.VERSION.RELEASE}"
                        val info = mapOf(
                            "deviceModel" to deviceModel,
                            "osVersion" to osVersion,
                            "manufacturer" to manufacturer,
                            "model" to model,
                            "brand" to Build.BRAND,
                            "sdkInt" to Build.VERSION.SDK_INT
                        )
                        result.success(info)
                    } catch (e: Throwable) {
                        result.success(mapOf(
                            "deviceModel" to "Android Device",
                            "osVersion" to "Android ${Build.VERSION.RELEASE}"
                        ))
                    }
                }
                else -> result.notImplemented()
            }
        }

        // 2. Channel com.taspenguard/ksp (Genuine Kaspersky Mobile Security SDK Engine)
        kspChannel.setMethodCallHandler { call, result ->
            when (call.method) {
                "activateLicense" -> {
                    val licenseKey = call.argument<String>("licenseKey")
                    kasperskyBridge.activateLicense(licenseKey) { success ->
                        result.success(success)
                    }
                }
                "buildSDKStatus" -> {
                    val status = kasperskyBridge.buildSDKStatus()
                    result.success(status)
                }
                "checkRoot" -> {
                    Thread {
                        val rootResult = kasperskyBridge.checkRoot()
                        runOnUiThread { result.success(rootResult) }
                    }.start()
                }
                "startScan" -> {
                    kasperskyBridge.startScan(
                        onProgress = { scanned, total, currentFile ->
                            runOnUiThread {
                                kspChannel.invokeMethod("onScanProgress", mapOf(
                                    "scanned" to scanned,
                                    "total" to total,
                                    "currentFile" to currentFile
                                ))
                            }
                        },
                        onThreat = { threatName, path, isMalware ->
                            runOnUiThread {
                                kspChannel.invokeMethod("onScanThreat", mapOf(
                                    "threatName" to threatName,
                                    "path" to path,
                                    "isMalware" to isMalware
                                ))
                            }
                        },
                        onComplete = { scanned, threats, error ->
                            runOnUiThread {
                                kspChannel.invokeMethod("onScanComplete", mapOf(
                                    "scanned" to scanned,
                                    "threats" to threats,
                                    "error" to error
                                ))
                            }
                        }
                    )
                    result.success(true)
                }
                "setRealtimeProtection" -> {
                    val enabled = call.argument<Boolean>("enabled") ?: true
                    val success = kasperskyBridge.setRealtimeProtection(enabled) { name, type ->
                        showSecurityAlertNotification(
                            "🚨 Kaspersky Realtime Protection",
                            "Ancaman terdeteksi dan diisolasi: $name ($type)",
                            true
                        )
                        runOnUiThread {
                            kspChannel.invokeMethod("onRealtimeThreat", mapOf(
                                "name" to name,
                                "type" to type
                            ))
                        }
                    }
                    result.success(success)
                }
                "setWebFilter", "setPuaScanner" -> {
                    result.success(true)
                }
                "checkUrl" -> {
                    val url = call.argument<String>("url") ?: ""
                    Thread {
                        val checkResult = kasperskyBridge.checkUrl(url)
                        runOnUiThread {
                            result.success(checkResult)
                        }
                    }.start()
                }
                "testScanEicar" -> {
                    Thread {
                        val eicarResult = kasperskyBridge.testScanEicar()
                        runOnUiThread {
                            result.success(eicarResult)
                        }
                    }.start()
                }
                "requestNotificationPermission" -> {
                    val granted = requestNotifPermission()
                    result.success(granted)
                }
                "showNotification" -> {
                    val title = call.argument<String>("title") ?: "Peringatan Keamanan"
                    val message = call.argument<String>("message") ?: "Aktivitas mencurigakan terdeteksi."
                    val isThreat = call.argument<Boolean>("isThreat") ?: true
                    showSecurityAlertNotification(title, message, isThreat)
                    result.success(true)
                }
                else -> result.notImplemented()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "TelkomSecure Security Alerts"
            val descriptionText = "Peringatan ancaman siber, malware, dan URL berbahaya"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(NOTIF_CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun requestNotifPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val currentPerm = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            if (currentPerm != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_CODE_NOTIF
                )
                return false
            }
        }
        return true
    }

    private fun showSecurityAlertNotification(title: String, message: String, isThreat: Boolean) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, NOTIF_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDefaults(Notification.DEFAULT_ALL)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 200, 500))

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(this).notify((System.currentTimeMillis() % 100000).toInt(), builder.build())
        }
    }
}
