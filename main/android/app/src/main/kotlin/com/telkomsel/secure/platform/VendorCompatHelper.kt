package com.telkomsel.secure.platform

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log

/**
 * Vendor-specific device compatibility helper for TelkomSecure.
 *
 * Handles OEM-specific quirks that affect background services and
 * accessibility services across Chinese/Korean Android vendors:
 *  - Xiaomi/MIUI: background popup permission, autostart
 *  - Samsung OneUI: battery optimization, sleeping apps
 *  - OPPO/realme/OnePlus: ColorOS autostart
 *  - vivo: FunTouch/OriginOS autostart
 *  - Huawei/Honor: EMUI startup manager
 */
object VendorCompatHelper {

    private const val TAG = "VendorCompat"

    // --- Xiaomi / MIUI / HyperOS ---

    fun isMiuiDevice(): Boolean =
        Build.MANUFACTURER.equals("Xiaomi", ignoreCase = true) ||
        Build.MANUFACTURER.equals("Redmi", ignoreCase = true) ||
        Build.MANUFACTURER.equals("POCO", ignoreCase = true)

    fun hasMiuiPopupEditor(context: Context): Boolean {
        if (!isMiuiDevice()) return false
        return try {
            context.packageManager.getActivityInfo(
                ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.permissions.PermissionsEditorActivity"
                ), 0
            )
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun openMiuiBackgroundPopup(context: Context): Boolean {
        if (!hasMiuiPopupEditor(context)) return false
        return try {
            val intent = Intent("miui.intent.action.APP_PERM_EDITOR").apply {
                setClassName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.permissions.PermissionsEditorActivity"
                )
                putExtra("extra_pkgname", context.packageName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.w(TAG, "MIUI popup editor failed, falling back to app details", e)
            openAppDetails(context)
        }
    }

    // --- Samsung OneUI ---

    fun isSamsungDevice(): Boolean =
        Build.MANUFACTURER.equals("Samsung", ignoreCase = true)

    fun openSamsungBatteryOptimization(context: Context): Boolean {
        val intents = listOf(
            // Samsung Smart Manager / Device Care
            Intent().setComponent(ComponentName(
                "com.samsung.android.lool",
                "com.samsung.android.sm.battery.ui.BatteryActivity"
            )),
            // Fallback to standard battery optimization
            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        )
        return tryIntents(context, intents)
    }

    // --- General: Get Vendor Info ---

    fun getVendorInfo(): Map<String, Any> {
        return mapOf(
            "manufacturer" to Build.MANUFACTURER,
            "brand" to Build.BRAND,
            "model" to Build.MODEL,
            "isMiui" to isMiuiDevice(),
            "isSamsung" to isSamsungDevice(),
            "isOppo" to isOppoDevice(),
            "isVivo" to isVivoDevice(),
            "isHuawei" to isHuaweiDevice(),
            "sdkInt" to Build.VERSION.SDK_INT,
            "release" to Build.VERSION.RELEASE
        )
    }

    // --- Vendor detection helpers ---

    private fun isOppoDevice(): Boolean =
        Build.MANUFACTURER.equals("OPPO", ignoreCase = true) ||
        Build.MANUFACTURER.equals("realme", ignoreCase = true) ||
        Build.MANUFACTURER.equals("OnePlus", ignoreCase = true)

    private fun isVivoDevice(): Boolean =
        Build.MANUFACTURER.equals("vivo", ignoreCase = true) ||
        Build.MANUFACTURER.equals("iQOO", ignoreCase = true)

    private fun isHuaweiDevice(): Boolean =
        Build.MANUFACTURER.equals("HUAWEI", ignoreCase = true) ||
        Build.MANUFACTURER.equals("HONOR", ignoreCase = true)

    // --- Utility ---

    private fun openAppDetails(context: Context): Boolean {
        return try {
            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:${context.packageName}")
            ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
            context.startActivity(intent)
            true
        } catch (_: Throwable) {
            false
        }
    }

    private fun tryIntents(context: Context, intents: List<Intent>): Boolean {
        for (intent in intents) {
            try {
                intent.flags = intent.flags or Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                return true
            } catch (_: Throwable) {}
        }
        return false
    }
}
