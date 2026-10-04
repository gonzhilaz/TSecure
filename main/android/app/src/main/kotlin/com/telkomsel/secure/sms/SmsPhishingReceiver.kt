package com.telkomsel.secure.sms

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.util.Log
import androidx.core.app.NotificationCompat
import com.telkomsel.secure.platform.ProtectionLog
import com.telkomsel.secure.telkomsel_secure.MainActivity
import com.telkomsel.secure.telkomsel_secure.R
import com.telkomsel.secure.webfilter.UrlFilterRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.regex.Pattern

/**
 * Enterprise SMS Scam & Smishing Phishing Receiver for Telkomsel Secure.
 * Inspects incoming SMS in real-time, extracts suspicious links (APK drops, fake banking,
 * courier scam, traffic ticket ETLE), checks them against Kaspersky Security Network,
 * and triggers immediate high-priority defensive alerts.
 */
class SmsPhishingReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "TelkomSmsGuardian"
        private const val NOTIF_CHANNEL_ID = "telkom_sms_phishing_alerts"
        private const val NOTIF_ID = 9021

        // Regex to extract URLs, bit.ly, shortlinks, raw domains, and direct APK download links
        private val URL_PATTERN = Pattern.compile(
            "(https?://[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]+|" +
            "[\\w\\d.-]+\\.(?:apk|top|xyz|icu|buzz|club|site|online|app|vip|link|live|shop)/?[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*|" +
            "(?:bit\\.ly|tinyurl\\.com|t\\.me|s\\.id|cutt\\.ly|is\\.gd)/[\\w\\d\\-_]+)",
            Pattern.CASE_INSENSITIVE
        )

        // Indonesian common smishing / APK malware trigger terms
        private val SCAM_KEYWORD_PATTERNS = listOf(
            Pattern.compile("undangan.*(?:pernikahan|nikah|digital).*\\.apk", Pattern.CASE_INSENSITIVE),
            Pattern.compile("surat.*(?:tilang|etle|kepolisian).*\\.apk", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?:paket|resi|jne|j&t|sicepat|pos|ninja|antaraja).*\\.apk", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?:tagihan|pln|pdam|bpjs).*\\.apk", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?:hadiah|bonus pulsa|poin telkomsel).*klik", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?:blokir|rekening|bca|mandiri|bri|bni|livin|brimo).*verifikasi", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?:lowongan|gaji|part.?time).*klik", Pattern.CASE_INSENSITIVE)
        )
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val sender = messages[0].displayOriginatingAddress ?: "Unknown"
        val bodyBuilder = StringBuilder()
        for (sms in messages) {
            bodyBuilder.append(sms.displayMessageBody ?: "")
        }
        val fullBody = bodyBuilder.toString()

        Log.i(TAG, "Incoming SMS from: $sender (length: ${fullBody.length})")

        // Async Threat Inspection
        CoroutineScope(Dispatchers.IO).launch {
            inspectSmsContent(context.applicationContext, sender, fullBody)
        }
    }

    private fun inspectSmsContent(context: Context, sender: String, body: String) {
        val extractedUrls = extractUrls(body)
        var isScamDetected = false
        var detectedReason = ""
        var targetUrl = ""

        // 1. Heuristic Scan against known Smishing APK delivery and Telco scam templates
        for (pattern in SCAM_KEYWORD_PATTERNS) {
            if (pattern.matcher(body).find()) {
                isScamDetected = true
                detectedReason = "Modus Penipuan APK / Rekayasa Sosial"
                break
            }
        }

        // 2. Direct check for .apk download strings
        if (body.contains(".apk", ignoreCase = true)) {
            isScamDetected = true
            detectedReason = "Tautan Unduhan Malware Android (.APK)"
        }

        // 3. Inspect every extracted URL against Kaspersky SDK WebFilter / KSN
        for (url in extractedUrls) {
            targetUrl = url
            val verdict = UrlFilterRepository.checkUrl(context, url)
            val isBad = verdict["isBlocked"] as? Boolean ?: false
            if (isBad) {
                isScamDetected = true
                val category = verdict["category"] as? String ?: "Malicious URL"
                detectedReason = "Phishing / Malware URL ($category)"
                break
            }
        }

        // If high-risk scam pattern found even without known bad URL
        if (!isScamDetected && extractedUrls.isNotEmpty()) {
            val lower = body.lowercase()
            val containsUrgency = lower.contains("segera") || lower.contains("blokir") ||
                                 lower.contains("hadiah") || lower.contains("menang") ||
                                 lower.contains("klaim") || lower.contains("kadaluarsa")
            if (containsUrgency) {
                isScamDetected = true
                detectedReason = "Pesan Mendesak Mencurigakan dengan Tautan Tak Dikenal"
                targetUrl = extractedUrls[0]
            }
        }

        if (isScamDetected) {
            Log.w(TAG, "🚨 SMISHING THREAT DETECTED! Sender: $sender, Reason: $detectedReason, URL: $targetUrl")

            // Log event to defensive telemetry
            ProtectionLog.event(
                context,
                "SMS_SMISHING",
                "BLOCKED",
                "SMS Penipuan dari $sender: $detectedReason ($targetUrl)"
            )

            // Trigger Heads-Up Warning Notification
            triggerSmishingNotification(context, sender, detectedReason, targetUrl)

            // Notify Foreground Flutter Engine
            MainActivity.notifySmishingThreat(sender, body, targetUrl)
        }
    }

    private fun extractUrls(text: String): List<String> {
        val list = mutableListOf<String>()
        val matcher = URL_PATTERN.matcher(text)
        while (matcher.find()) {
            val url = matcher.group()
            if (!url.isNullOrBlank()) {
                list.add(url)
            }
        }
        return list
    }

    private fun triggerSmishingNotification(
        context: Context,
        sender: String,
        reason: String,
        url: String
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIF_CHANNEL_ID,
                "Peringatan SMS Penipuan (Smishing)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi peringatan saat mendeteksi SMS phishing atau APK malware"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("threat_type", "SMS_SMISHING")
            putExtra("sender", sender)
            putExtra("reason", reason)
            putExtra("url", url)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIF_ID,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val notification = NotificationCompat.Builder(context, NOTIF_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Peringatan SMS Penipuan / Phishing!")
            .setContentText("SMS dari $sender terindikasi $reason. Jangan klik tautan!")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Telkomsel Secure mendeteksi SMS mencurigakan dari $sender.\n" +
                    "Kategori: $reason\n" +
                    (if (url.isNotBlank()) "Tautan: $url\n" else "") +
                    "Tindakan: Jangan buka tautan atau memasang file yang dikirimkan!"
                )
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(0xFFED0226.toInt())
            .build()

        notificationManager.notify(NOTIF_ID, notification)
    }
}
