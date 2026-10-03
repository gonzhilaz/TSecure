package com.telkomsel.secure.platform

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicLong

/**
 * Persistent proof-of-operation log for Real-Time Protection and Web Filter.
 * Written by the native services (so it keeps recording while the UI is closed)
 * and read back by Flutter to show verifiable evidence to the user.
 */
object ProtectionLog {
    private const val PREFS = "telkom_protection_log"
    private const val KEY_EVENTS = "events"
    private const val MAX_EVENTS = 80
    private const val FLUSH_MS = 3000L

    private val files = AtomicLong(-1)
    private val urls = AtomicLong(-1)
    @Volatile private var lastFlush = 0L
    @Volatile private var lastActivity = 0L

    private fun prefs(c: Context) = c.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun load(c: Context) {
        if (files.get() < 0) files.compareAndSet(-1, prefs(c).getLong("files", 0))
        if (urls.get() < 0) urls.compareAndSet(-1, prefs(c).getLong("urls", 0))
    }

    /** kind: STARTED, STOPPED, HEARTBEAT, THREAT, BLOCKED, ERROR, WAITING ... source: RTP | WEB */
    @Synchronized
    fun event(c: Context, source: String, kind: String, detail: String) {
        val now = System.currentTimeMillis()
        val p = prefs(c)
        val arr = try { JSONArray(p.getString(KEY_EVENTS, "[]")) } catch (_: Throwable) { JSONArray() }
        arr.put(JSONObject().put("t", now).put("src", source).put("kind", kind).put("detail", detail))
        val trimmed = JSONArray()
        for (i in maxOf(0, arr.length() - MAX_EVENTS) until arr.length()) trimmed.put(arr.get(i))
        val ed = p.edit().putString(KEY_EVENTS, trimmed.toString())
        if (source == "RTP" && kind == "STARTED") ed.putLong("rtpStartedAt", now)
        if (source == "RTP" && kind == "THREAT") ed.putLong("threats", p.getLong("threats", 0) + 1)
        ed.apply()
        lastActivity = now
    }

    fun countFile(c: Context) { load(c); files.incrementAndGet(); touch(c) }
    fun countUrl(c: Context) { load(c); urls.incrementAndGet(); touch(c) }

    /** Set when the app sends the user to Accessibility settings; consumed when the service connects. */
    fun expectAccessibility(c: Context) {
        prefs(c).edit().putLong("a11yWaitUntil", System.currentTimeMillis() + 5 * 60_000).apply()
    }

    fun consumeAccessibilityExpectation(c: Context): Boolean {
        val until = prefs(c).getLong("a11yWaitUntil", 0)
        prefs(c).edit().remove("a11yWaitUntil").apply()
        return until > System.currentTimeMillis()
    }

    private fun touch(c: Context) {
        val now = System.currentTimeMillis()
        lastActivity = now
        if (now - lastFlush > FLUSH_MS) flush(c)
    }

    fun flush(c: Context) {
        load(c)
        lastFlush = System.currentTimeMillis()
        prefs(c).edit().putLong("files", files.get()).putLong("urls", urls.get())
            .putLong("lastActivity", lastActivity).apply()
    }

    fun snapshot(c: Context, rtpRunning: Boolean, webRunning: Boolean): Map<String, Any?> {
        flush(c)
        val p = prefs(c)
        val events = ArrayList<Map<String, Any?>>()
        val arr = try { JSONArray(p.getString(KEY_EVENTS, "[]")) } catch (_: Throwable) { JSONArray() }
        for (i in arr.length() - 1 downTo 0) {
            val o = arr.getJSONObject(i)
            events.add(mapOf("t" to o.optLong("t"), "src" to o.optString("src"),
                "kind" to o.optString("kind"), "detail" to o.optString("detail")))
        }
        return mapOf(
            "rtpRunning" to rtpRunning,
            "webRunning" to webRunning,
            "rtpStartedAt" to p.getLong("rtpStartedAt", 0),
            "filesChecked" to files.get().coerceAtLeast(0),
            "urlsChecked" to urls.get().coerceAtLeast(0),
            "threats" to p.getLong("threats", 0),
            "lastActivity" to p.getLong("lastActivity", 0),
            "events" to events
        )
    }
}
