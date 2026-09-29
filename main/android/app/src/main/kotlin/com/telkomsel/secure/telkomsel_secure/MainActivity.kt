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
import com.telkomsel.blackwall.BlackWallGuard
import com.telkomsel.secure.kaspersky.KasperskyNativeBridge

class MainActivity : FlutterActivity() {

    private lateinit var kasperskyBridge: KasperskyNativeBridge

    companion object {
        private const val TAG = "TelkomSecure-Native"
        private const val CHANNEL_BLACKWALL = "com.telkomsel.secure/blackwall"
        private const val CHANNEL_KASPERSKY = "com.taspenguard/ksp"
        private const val NOTIF_CHANNEL_ID = "telkom_security_threats"
        private const val REQUEST_CODE_NOTIF = 1001

        init {
            try {
                System.loadLibrary("blackwall")
                Log.i(TAG, "BlackWall RASP native engine successfully loaded.")
            } catch (e: UnsatisfiedLinkError) {
                Log.w(TAG, "BlackWall native library not present in this APK. Running in unshielded mode.")
            } catch (e: Throwable) {
                Log.e(TAG, "BlackWall initialization notice: ${e.message}")
            }
        }
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        createNotificationChannel()
        kasperskyBridge = KasperskyNativeBridge(applicationContext)

        // 1. Channel com.telkomsel.secure/blackwall (RASP Defense & Telemetry)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL_BLACKWALL).setMethodCallHandler { call, result ->
            when (call.method) {
                "isShieldActive" -> {
                    val active = try {
                        BlackWallGuard.initNativeGuard(applicationContext, "")
                    } catch (e: Throwable) {
                        false
                    }
                    result.success(active)
                }
                "getThreatBitmask" -> {
                    val mask = try {
                        BlackWallGuard.getSecurityThreatBitmask()
                    } catch (e: Throwable) {
                        0
                    }
                    result.success(mask)
                }
                else -> result.notImplemented()
            }
        }

        // 2. Channel com.taspenguard/ksp (Genuine Kaspersky Mobile Security SDK Engine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL_KASPERSKY).setMethodCallHandler { call, result ->
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
                "startScan" -> {
                    val started = kasperskyBridge.startScan()
                    result.success(started)
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
                "setRealtimeProtection", "setWebFilter", "setPuaScanner" -> {
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
