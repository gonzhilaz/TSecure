package com.telkomsel.secure.telkomsel_secure

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import java.io.File
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import com.telkomsel.secure.kaspersky.KasperskyNativeBridge
import com.telkomsel.secure.network.DnsCertSecurityHelper
import com.telkomsel.secure.network.WifiSecurityHelper
import com.telkomsel.secure.platform.PermissionHelper
import com.telkomsel.secure.platform.VendorCompatHelper
import com.telkomsel.secure.quarantine.QuarantineNativeVault
import com.telkomsel.secure.webfilter.UrlFilterRepository
import com.telkomsel.secure.telkomsel_secure.receivers.BootReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : FlutterActivity() {

    private lateinit var kasperskyBridge: KasperskyNativeBridge
    private lateinit var quarantineVault: QuarantineNativeVault
    private var kasperskyChannel: MethodChannel? = null
    private var isWebFilterActive: Boolean = true

    companion object {
        private const val TAG = "TelkomSecure-Native"
        private const val CHANNEL_DEVICE = "com.telkomsel.secure/device"
        private const val CHANNEL_KASPERSKY = "com.taspenguard/ksp"
        private const val NOTIF_CHANNEL_ID = "telkom_security_threats"

        @Volatile
        var activeInstance: MainActivity? = null
            private set

        fun notifyUrlThreat(url: String, category: String, verdict: String) {
            val activity = activeInstance ?: return
            activity.runOnUiThread {
                activity.kasperskyChannel?.invokeMethod("onUrlThreatDetected", mapOf(
                    "url" to url,
                    "category" to category,
                    "verdict" to verdict
                ))
            }
        }
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        activeInstance = this

        createNotificationChannel()
        kasperskyBridge = KasperskyNativeBridge(applicationContext)
        quarantineVault = QuarantineNativeVault(applicationContext)

        val kspChannel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL_KASPERSKY)
        kasperskyChannel = kspChannel

        // 1. Channel com.telkomsel.secure/device (Hardware, Telemetry & App Updates)
        val deviceCallHandler: MethodChannel.MethodCallHandler = MethodChannel.MethodCallHandler { call, result ->
            when (call.method) {
                "isShieldActive" -> result.success(kasperskyBridge.ensureAntivirusInitialized())
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
                        result.success(mapOf(
                            "deviceModel" to deviceModel,
                            "osVersion" to "Android ${Build.VERSION.RELEASE}",
                            "manufacturer" to manufacturer,
                            "model" to model,
                            "brand" to Build.BRAND,
                            "sdkInt" to Build.VERSION.SDK_INT
                        ))
                    } catch (_: Throwable) {
                        result.success(mapOf("deviceModel" to "Android Device", "osVersion" to "Android ${Build.VERSION.RELEASE}"))
                    }
                }
                "getAppVersion" -> {
                    try {
                        val pInfo = packageManager.getPackageInfo(packageName, 0)
                        val vName = pInfo.versionName ?: "1.0.0"
                        val vCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) pInfo.longVersionCode.toInt() else @Suppress("DEPRECATION") pInfo.versionCode
                        result.success(mapOf("versionName" to vName, "versionCode" to vCode))
                    } catch (_: Throwable) {
                        result.success(mapOf("versionName" to "1.0.0", "versionCode" to 1))
                    }
                }
                "installApk" -> {
                    val filePath = call.argument<String>("filePath")
                    if (filePath.isNullOrEmpty()) {
                        result.error("INVALID_PATH", "File path is null or empty", null)
                        return@MethodCallHandler
                    }
                    val originalApk = File(filePath)
                    if (!originalApk.exists()) {
                        result.error("FILE_NOT_FOUND", "APK file does not exist at $filePath", null)
                        return@MethodCallHandler
                    }
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !packageManager.canRequestPackageInstalls()) {
                            PermissionHelper.openInstallPermissionSettings(this)
                            result.success(false)
                            return@MethodCallHandler
                        }

                        val targetApk = if (originalApk.absolutePath.startsWith(cacheDir.absolutePath)) {
                            originalApk
                        } else {
                            val safeCacheApk = File(cacheDir, "TelkomSecure-Update.apk")
                            originalApk.copyTo(safeCacheApk, overwrite = true)
                            safeCacheApk
                        }

                        val apkUri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            FileProvider.getUriForFile(applicationContext, "$packageName.fileprovider", targetApk)
                        } else {
                            Uri.fromFile(targetApk)
                        }

                        val installIntent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(apkUri, "application/vnd.android.package-archive")
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                        }
                        startActivity(installIntent)
                        result.success(true)
                    } catch (e: Throwable) {
                        Log.e(TAG, "Failed to launch APK installer: ${e.message}", e)
                        result.error("INSTALL_ERROR", e.message, null)
                    }
                }
                else -> result.notImplemented()
            }
        }
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL_DEVICE).setMethodCallHandler(deviceCallHandler)

        // 2. Channel com.taspenguard/ksp (Kaspersky SDK, Quarantine, Permissions & Network)
        kspChannel.setMethodCallHandler { call, result ->
            when (call.method) {
                "activateLicense" -> {
                    val licenseKey = call.argument<String>("licenseKey")
                    kasperskyBridge.activateLicense(licenseKey) { success -> result.success(success) }
                }
                "buildSDKStatus" -> result.success(kasperskyBridge.buildSDKStatus())
                "checkRoot" -> Thread {
                    val rootResult = kasperskyBridge.checkRoot()
                    runOnUiThread { result.success(rootResult) }
                }.start()
                "startScan" -> {
                    val scanMode = call.argument<String>("scanMode") ?: "FULL"
                    kasperskyBridge.startScan(
                        scanMode = scanMode,
                        onProgress = { sc, tot, cur -> runOnUiThread { kspChannel.invokeMethod("onScanProgress", mapOf("scanned" to sc, "total" to tot, "currentFile" to cur)) } },
                        onThreat = { th, path, isMal -> runOnUiThread { kspChannel.invokeMethod("onScanThreat", mapOf("threatName" to th, "path" to path, "isMalware" to isMal)) } },
                        onComplete = { sc, th, err -> runOnUiThread { kspChannel.invokeMethod("onScanComplete", mapOf("scanned" to sc, "threats" to th, "error" to err)) } }
                    )
                    result.success(true)
                }
                "setRealtimeProtection" -> {
                    val enabled = call.argument<Boolean>("enabled") ?: true
                    val action = call.argument<String>("threatAction") ?: "delete"
                    if (enabled) RealtimeSecurityService.start(applicationContext, action) else RealtimeSecurityService.stop(applicationContext)
                    // Persist preference for BootReceiver auto-restart
                    getSharedPreferences(BootReceiver.PREFS_NAME, Context.MODE_PRIVATE)
                        .edit().putBoolean(BootReceiver.KEY_RTP_ENABLED, enabled).apply()
                    val success = kasperskyBridge.setRealtimeProtection(enabled) { name, type ->
                        showSecurityAlertNotification("🚨 Kaspersky Threat Blocked", "Ancaman dicegat: $name ($type)", true)
                        runOnUiThread { kspChannel.invokeMethod("onRealtimeThreat", mapOf("name" to name, "type" to type)) }
                    }
                    result.success(success)
                }
                "isRtpActive" -> result.success(RealtimeSecurityService.isRunning(applicationContext))

                // --- URL FILTER (WebFilterControl from Kaspersky SDK) ---
                "urlFilterInit" -> Thread {
                    val initResult = UrlFilterRepository.init(applicationContext)
                    runOnUiThread { result.success(initResult.isSuccess) }
                }.start()
                "urlFilterEnable" -> Thread {
                    val enable = call.argument<Boolean>("enabled") ?: false
                    val enableResult = UrlFilterRepository.setEnabled(enable)
                    runOnUiThread { result.success(enableResult.getOrDefault(false)) }
                }.start()
                "isUrlFilterEnabled" -> result.success(UrlFilterRepository.isEnabled())
                "urlFilterCheckUrl" -> {
                    val url = call.argument<String>("url") ?: ""
                    Thread {
                        val checkResult = UrlFilterRepository.checkUrl(applicationContext, url)
                        runOnUiThread { result.success(checkResult) }
                    }.start()
                }
                "setWebFilter" -> {
                    isWebFilterActive = call.argument<Boolean>("enabled") ?: true
                    result.success(true)
                }

                // --- VENDOR COMPAT (MIUI, Samsung, etc.) ---
                "isMiui" -> result.success(VendorCompatHelper.isMiuiDevice())
                "hasMiuiPopupEditor" -> result.success(VendorCompatHelper.hasMiuiPopupEditor(applicationContext))
                "requestMiuiBackgroundPopup" -> result.success(VendorCompatHelper.openMiuiBackgroundPopup(applicationContext))
                "openSamsungBatterySettings" -> result.success(VendorCompatHelper.openSamsungBatteryOptimization(applicationContext))
                "getVendorInfo" -> result.success(VendorCompatHelper.getVendorInfo())
                "setPuaScanner" -> result.success(true)
                "getWifiSecurityStatus" -> Thread {
                    val wifiStatus = WifiSecurityHelper.getWifiSecurityStatus(applicationContext)
                    runOnUiThread { result.success(wifiStatus) }
                }.start()
                "checkUrl" -> {
                    val url = call.argument<String>("url") ?: ""
                    if (!isWebFilterActive) {
                        result.success(mapOf("url" to url, "isSafe" to true, "verdict" to "NONAKTIF", "score" to 100, "sdkVerified" to false))
                        return@setMethodCallHandler
                    }
                    Thread {
                        val checkResult = kasperskyBridge.checkUrl(url)
                        runOnUiThread { result.success(checkResult) }
                    }.start()
                }
                "updateBases" -> Thread {
                    val success = kasperskyBridge.checkAndUpdateBases { st -> runOnUiThread { kspChannel.invokeMethod("onUpdateStatus", st) } }
                    runOnUiThread { result.success(success) }
                }.start()
                "testScanEicar", "scanSpecificFile" -> {
                    val filePath = call.argument<String>("filePath")
                    Thread {
                        val scanResult = kasperskyBridge.scanSpecificFile(filePath)
                        runOnUiThread { result.success(scanResult) }
                    }.start()
                }

                // --- QUARANTINE VAULT & THREAT REMEDIATION ---
                "quarantineFile" -> {
                    val filePath = call.argument<String>("filePath") ?: ""
                    val threatName = call.argument<String>("threatName") ?: "Malware"
                    val threatType = call.argument<String>("threatType") ?: "Trojan"
                    val severity = call.argument<String>("severity") ?: "HIGH"
                    val qRes = quarantineVault.quarantineFile(filePath, threatName, threatType, severity)
                    result.success(qRes)
                }
                "restoreQuarantinedFile" -> {
                    val itemId = call.argument<String>("itemId") ?: ""
                    result.success(quarantineVault.restoreQuarantinedFile(itemId))
                }
                "deleteQuarantinedItem" -> {
                    val itemId = call.argument<String>("itemId") ?: ""
                    result.success(quarantineVault.deleteQuarantinedItem(itemId))
                }
                "clearAllQuarantined" -> result.success(quarantineVault.clearAllQuarantined())
                "getAllQuarantined" -> result.success(quarantineVault.getAllQuarantined())
                "deleteThreatFile" -> {
                    val filePath = call.argument<String>("filePath") ?: ""
                    result.success(quarantineVault.shredFile(filePath))
                }
                "uninstallThreatApp" -> {
                    val pkg = call.argument<String>("packageName") ?: ""
                    result.success(PermissionHelper.uninstallThreatApp(this, pkg))
                }

                // --- SECURITY PERMISSIONS & ONBOARDING ---
                "getAllPermissionsStatus" -> result.success(PermissionHelper.getAllPermissionsStatus(this))
                "checkStoragePermission" -> result.success(PermissionHelper.checkStoragePermission(this))
                "requestStoragePermission" -> result.success(PermissionHelper.requestStoragePermission(this))
                "requestNotificationPermission" -> result.success(PermissionHelper.requestNotificationPermission(this))
                "checkLocationPermission" -> result.success(PermissionHelper.checkLocationPermission(this))
                "requestLocationPermission" -> result.success(PermissionHelper.requestLocationPermission(this))
                "checkAccessibilityPermission" -> result.success(PermissionHelper.checkAccessibilityPermission(this))
                "requestAccessibilityPermission" -> result.success(PermissionHelper.openAccessibilitySettings(this))
                "checkInstallPermission" -> result.success(PermissionHelper.checkInstallPermission(this))
                "requestInstallPermission" -> result.success(PermissionHelper.openInstallPermissionSettings(this))
                "openAutostartSettings" -> result.success(PermissionHelper.openAutostartSettings(this))
                "openBackgroundPopupSettings" -> result.success(PermissionHelper.openBackgroundPopupSettings(this))
                "checkCertificate" -> {
                    val url = call.argument<String>("url") ?: ""
                    CoroutineScope(Dispatchers.Main).launch {
                        val certRes = DnsCertSecurityHelper.checkCertificate(url)
                        result.success(certRes)
                    }
                }
                "checkDns" -> {
                    val url = call.argument<String>("url") ?: ""
                    val trustedIps = call.argument<List<String>>("trustedIps") ?: emptyList()
                    CoroutineScope(Dispatchers.Main).launch {
                        val dnsRes = DnsCertSecurityHelper.checkDns(url, trustedIps)
                        result.success(dnsRes)
                    }
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
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(NOTIF_CHANNEL_ID, name, importance).apply {
                description = "Peringatan ancaman siber, malware, dan URL berbahaya"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val nm: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    private fun showSecurityAlertNotification(title: String, message: String, isThreat: Boolean) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
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

        if (PermissionHelper.checkNotificationPermission(this)) {
            NotificationManagerCompat.from(this).notify((System.currentTimeMillis() % 100000).toInt(), builder.build())
        }
    }

    override fun onDestroy() {
        if (activeInstance == this) activeInstance = null
        super.onDestroy()
    }
}
