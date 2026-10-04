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

        // Indonesian Judi Online / Slot Gacor SMS Patterns
        private val JUDI_ONLINE_PATTERNS = listOf(
            Pattern.compile("(?:slot|gacor|maxwin|scatter|pragmatic|zeus|olympus|mahjong|pgsoft|anti.?rungkad)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?:judi|kasino|casino|togel|sbobet|parlay|poker|domino.?qiu|bandar)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?:depo|deposit|wd|withdraw).*(?:pulsa|tanpa potongan|bonus|receh)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?:freebet|bonus new member|modal receh|pasti bayar|garansi kekalahan)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?:link alternatif|daftar sekarang|klik login).*(?:slot|gacor|maxwin|menang)", Pattern.CASE_INSENSITIVE)
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
        var threatType = "SMISHING"
        var detectedReason = ""
        var targetUrl = ""

        // 1a. Heuristic check for Judi Online & Slot Gacor
        for (pattern in JUDI_ONLINE_PATTERNS) {
            if (pattern.matcher(body).find()) {
                isScamDetected = true
                threatType = "JUDI_ONLINE"
                detectedReason = "Promosi Judi Online & Slot Ilegal"
                break
            }
        }

        // 1b. Heuristic Scan against known Smishing APK delivery and Telco scam templates
        if (!isScamDetected) {
            for (pattern in SCAM_KEYWORD_PATTERNS) {
                if (pattern.matcher(body).find()) {
                    isScamDetected = true
                    threatType = "SMISHING"
                    detectedReason = "Modus Penipuan APK / Rekayasa Sosial"
                    break
                }
            }
        }

        // 2. Direct check for .apk download strings
        if (!isScamDetected && body.contains(".apk", ignoreCase = true)) {
            isScamDetected = true
            threatType = "SMISHING"
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
                if (threatType != "JUDI_ONLINE") {
                    threatType = "PHISHING"
                    detectedReason = "Phishing / Malware URL ($category)"
                }
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
                threatType = "SMISHING"
                detectedReason = "Pesan Mendesak Mencurigakan dengan Tautan Tak Dikenal"
                targetUrl = extractedUrls[0]
            }
        }

        if (isScamDetected) {
            Log.w(TAG, "🚨 THREAT DETECTED! Type: $threatType, Sender: $sender, Reason: $detectedReason, URL: $targetUrl")

            // Log event to defensive telemetry
            ProtectionLog.event(
                context,
                threatType,
                "BLOCKED",
                "SMS [$threatType] dari $sender: $detectedReason ($targetUrl)"
            )

            // Trigger Heads-Up Warning Notification
            triggerSmishingNotification(context, sender, threatType, detectedReason, targetUrl)

            // Notify Foreground Flutter Engine
            MainActivity.notifySmishingThreat(sender, body, targetUrl, threatType, detectedReason)
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
        threatType: String,
        reason: String,
        url: String
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return

        val isJudi = threatType == "JUDI_ONLINE"
        val title = if (isJudi) "🚨 Peringatan SMS Judi Online Ilegal!" else "🚨 Peringatan SMS Penipuan / Phishing!"
        val actionText = if (isJudi) "Jangan klik tautan promosi judi online atau slot gacor ini!" else "Jangan buka tautan atau memasang file APK yang dikirimkan!"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIF_CHANNEL_ID,
                "Peringatan SMS Penipuan & Judi Online",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi peringatan saat mendeteksi SMS phishing, malware, atau judi online"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("threat_type", threatType)
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
            .setContentTitle(title)
            .setContentText("SMS dari $sender terindikasi $reason. Jangan klik tautan!")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Telkomsel Secure mendeteksi SMS mencurigakan dari $sender.\n" +
                    "Kategori: $reason\n" +
                    (if (url.isNotBlank()) "Tautan: $url\n" else "") +
                    "Tindakan: $actionText"
                )
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(if (isJudi) 0xFFE65100.toInt() else 0xFFED0226.toInt())
            .build()

        notificationManager.notify(NOTIF_ID, notification)
    }
}
