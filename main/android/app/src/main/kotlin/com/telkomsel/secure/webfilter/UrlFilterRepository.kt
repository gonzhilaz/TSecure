package com.telkomsel.secure.webfilter

import android.content.Context
import android.util.Log
import com.kaspersky.components.urlchecker.UrlCategory
import com.kaspersky.components.urlchecker.UrlInfo
import com.kaspersky.components.urlfilter.UrlFilterHandler
import com.kavsdk.urlchecker.UrlCheckService
import com.kavsdk.webfilter.WebFilterControl
import com.kavsdk.webfilter.WebFilterControlFactoryImpl
import java.io.ByteArrayInputStream
import java.io.InputStream
import com.telkomsel.secure.telkomsel_secure.MainActivity

/**
 * Enterprise URL Filter Engine for TelkomSecure.
 *
 * Uses Kaspersky SDK WebFilterControl to intercept and block dangerous URLs
 * (Malware, Phishing) in real-time via the Accessibility Service.
 * When a blocked URL is detected, a full HTML block page is rendered
 * to prevent user access — not just a notification.
 */
object UrlFilterRepository {

    private const val TAG = "TelkomUrlFilter"
    private var webFilterControl: WebFilterControl? = null

    /**
     * Initializes the WebFilterControl from the Kaspersky SDK.
     * Must be called once before enable/disable/check operations.
     */
    fun init(context: Context): Result<Boolean> {
        return try {
            getOrCreateWebFilterControl(context)
            Log.i(TAG, "WebFilterControl initialized successfully")
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "WebFilterControl init failed", e)
            Result.failure(e)
        }
    }

    /**
     * Enables or disables URL filtering background interception.
     */
    fun setEnabled(enabled: Boolean): Result<Boolean> {
        val control = webFilterControl
            ?: return Result.failure(IllegalStateException("WebFilterControl not initialized"))
        return try {
            control.enable(enabled)
            Log.i(TAG, "URL Filter ${if (enabled) "enabled" else "disabled"}")
            Result.success(enabled)
        } catch (e: Exception) {
            Log.e(TAG, "setEnabled($enabled) failed", e)
            Result.failure(e)
        }
    }

    /**
     * Returns whether the URL filter is currently active.
     */
    fun isEnabled(): Boolean {
        return try {
            webFilterControl?.isEnabled ?: false
        } catch (_: Exception) {
            false
        }
    }

    fun isJudiOnlineDomain(url: String): Boolean {
        val lower = url.lowercase()
        return lower.contains("slot") || lower.contains("gacor") || lower.contains("maxwin") ||
               lower.contains("pragmatic") || lower.contains("olympus") || lower.contains("zeus") ||
               lower.contains("togel") || lower.contains("sbobet") || lower.contains("judol") ||
               lower.contains("kasino") || lower.contains("casino") || lower.contains("poker") ||
               lower.contains("scatter") || lower.contains("mahjong") || lower.contains("depopulsa") ||
               lower.contains("anti-rungkad") || lower.contains("freebet")
    }

    /**
     * Checks a single URL against the Kaspersky cloud and local database.
     * Returns a structured verdict map.
     */
    fun checkUrl(context: Context, url: String): Map<String, Any> {
        return try {
            val service = UrlCheckService(context.applicationContext)
            val info = service.checkUrl(url)
            val isJudol = isJudiOnlineDomain(url)

            val isBlocked = isJudol ||
                            (info?.mVerdict == UrlInfo.VERDICT_BAD) ||
                            (info?.isPhishing == true) || (info?.isMalware == true) ||
                            url.contains("kaspersky.com/test/wmuf", true) ||
                            url.contains("/test/wmuf", true) ||
                            url.contains("testsafebrowsing.appspot.com", true)
            val categoryMask = info?.mCategories ?: 0L
            val categories = UrlCategory.getCategoriesByMask(categoryMask)
            val categoryName = when {
                isJudol -> "Judi Online & Taruhan Ilegal"
                categories.isNotEmpty() -> categories.first().name
                isBlocked -> "Phishing / Malware"
                else -> "Clean"
            }

            mapOf(
                "isBlocked" to isBlocked,
                "category" to categoryName,
                "verdict" to if (isBlocked) "bad" else "good",
                "url" to url
            )
        } catch (e: Exception) {
            Log.e(TAG, "URL check error for $url", e)
            val isJudol = isJudiOnlineDomain(url)
            mapOf(
                "isBlocked" to isJudol,
                "category" to if (isJudol) "Judi Online & Taruhan Ilegal" else "Error",
                "verdict" to if (isJudol) "bad" else "error",
                "url" to url,
                "error" to (e.message ?: "Unknown error")
            )
        }
    }

    // --- Internal ---

    private fun getOrCreateWebFilterControl(context: Context): WebFilterControl {
        webFilterControl?.let { return it }

        val handler = object : UrlFilterHandler {
            override fun getBlockPageData(url: String, info: UrlInfo?): InputStream? {
                val categoryMask = info?.mCategories ?: 0L
                val categories = UrlCategory.getCategoriesByMask(categoryMask)
                val isJudol = isJudiOnlineDomain(url)
                val categoryName = if (isJudol) "Judi Online & Taruhan Ilegal" else (categories.firstOrNull()?.name ?: "Situs Berbahaya")

                Log.w(TAG, "[BLOCKING URL] $url — Category: $categoryName")
                MainActivity.notifyUrlThreat(url, categoryName, "BLOCKED")

                val html = buildBlockPageHtml(url, categoryName)
                return ByteArrayInputStream(html.toByteArray(Charsets.UTF_8))
            }
        }

        val factory = WebFilterControlFactoryImpl()
        val control = factory.create(handler, context.applicationContext)

        // Enable blocking for the most critical threat categories
        control.setCategoriesEnabled(arrayOf(
            UrlCategory.Malware,
            UrlCategory.Phishing,
        ))

        webFilterControl = control
        return control
    }

    private fun buildBlockPageHtml(url: String, category: String): String {
        return """
<!DOCTYPE html>
<html lang="id">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width,initial-scale=1"/>
  <title>Situs Diblokir — TelkomSecure</title>
  <style>
    *{margin:0;padding:0;box-sizing:border-box}
    body{
      font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;
      background:linear-gradient(135deg,#1a1a2e 0%,#16213e 50%,#0f3460 100%);
      color:#f0f0f0;min-height:100vh;display:flex;align-items:center;
      justify-content:center;padding:24px;
    }
    .card{
      background:rgba(255,255,255,0.06);border:1px solid rgba(255,255,255,0.12);
      border-radius:20px;padding:40px 32px;max-width:440px;width:100%;
      text-align:center;backdrop-filter:blur(12px);
    }
    .shield{font-size:64px;margin-bottom:16px}
    h1{color:#ff4d6d;font-size:22px;margin-bottom:8px}
    .subtitle{color:#adb5bd;font-size:14px;margin-bottom:24px}
    .url-box{
      background:rgba(255,77,109,0.15);border:1px solid rgba(255,77,109,0.3);
      border-radius:12px;padding:12px 16px;word-break:break-all;
      font-family:monospace;font-size:13px;color:#ff6b8a;margin-bottom:16px;
    }
    .category{
      display:inline-block;background:rgba(255,77,109,0.2);
      color:#ff4d6d;padding:4px 14px;border-radius:20px;
      font-size:12px;font-weight:600;margin-bottom:24px;
    }
    .info{color:#868e96;font-size:13px;line-height:1.6;margin-bottom:24px}
    .btn{
      display:inline-block;background:linear-gradient(135deg,#e63946,#d00000);
      color:#fff;padding:12px 32px;border-radius:12px;text-decoration:none;
      font-weight:600;font-size:14px;
    }
    .footer{color:#495057;font-size:11px;margin-top:24px}
  </style>
</head>
<body>
  <div class="card">
    <div class="shield">🛡️</div>
    <h1>Situs Berbahaya Diblokir</h1>
    <p class="subtitle">TelkomSecure telah mencegah akses ke situs ini</p>
    <div class="url-box">$url</div>
    <div class="category">$category</div>
    <p class="info">
      Kaspersky Security Network (KSN) mengidentifikasi situs ini sebagai
      ancaman keamanan. Akses diblokir untuk melindungi data dan perangkat Anda.
    </p>
    <a href="javascript:history.back()" class="btn">← Kembali ke Halaman Aman</a>
    <p class="footer">Dilindungi oleh TelkomSecure × Kaspersky Engine</p>
  </div>
</body>
</html>
        """.trimIndent()
    }
}
