package com.telkomsel.secure.telkomsel_secure.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.telkomsel.secure.telkomsel_secure.RealtimeSecurityService

/**
 * Restarts real-time protection and web filter services automatically
 * after the device reboots (standard boot and vendor quick-reboot).
 *
 * Reads the user's preference from SharedPreferences before starting —
 * if the user explicitly disabled protection, the service stays off.
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "TelkomBoot"
        private const val BOOT_START_DELAY_MS = 5_000L
        const val PREFS_NAME = "telkom_secure_prefs"
        const val KEY_RTP_ENABLED = "realtime_protection_enabled"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "Boot receiver triggered: ${intent.action}")

        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            "android.intent.action.QUICKBOOT_POWERON" -> {
                handleBoot(context)
            }
        }
    }

    private fun handleBoot(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val rtpEnabled = prefs.getBoolean(KEY_RTP_ENABLED, true)

            Log.i(TAG, "Boot completed — RTP preference: enabled=$rtpEnabled")

            if (rtpEnabled) {
                // Delay start so the OS can finish its own boot sequence first
                Handler(Looper.getMainLooper()).postDelayed({
                    try {
                        Log.i(TAG, "Starting RealtimeSecurityService after boot delay")
                        RealtimeSecurityService.start(context.applicationContext)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to start RTP on boot", e)
                    }
                }, BOOT_START_DELAY_MS)
            } else {
                Log.i(TAG, "RTP disabled by user — skipping auto-start")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in boot handler", e)
        }
    }
}
