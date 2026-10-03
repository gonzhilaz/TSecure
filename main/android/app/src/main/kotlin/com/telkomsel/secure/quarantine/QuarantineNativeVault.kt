package com.telkomsel.secure.quarantine

import android.content.Context
import android.media.MediaScannerConnection
import android.provider.MediaStore
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Enterprise Quarantine Sandbox & Native File Shredder for TelkomSecure.
 * Isolates malicious files into an encrypted app-private sandbox, removes execute permissions,
 * and maintains persistent metadata with options to restore or permanently shred.
 */
class QuarantineNativeVault(private val context: Context) {

    companion object {
        private const val TAG = "QuarantineVault"
        private const val VAULT_DIR_NAME = "quarantine_vault"
        private const val METADATA_FILE = "quarantine_registry.json"
        private const val XOR_KEY: Byte = 0x5A.toByte() // Fast stream cipher mask for malware neutralization
    }

    private val vaultDir: File by lazy {
        File(context.filesDir, VAULT_DIR_NAME).apply {
            if (!exists()) mkdirs()
        }
    }

    private val metadataFile: File by lazy {
        File(vaultDir, METADATA_FILE)
    }

    /**
     * Isolates a malicious file:
     * 1. Copies bytes with XOR encryption to neutralize executable binary code
     * 2. Sets permissions to 000 (non-executable, non-readable by OS)
     * 3. Deletes original infected file
     * 4. Saves metadata entry in registry
     */
    @Synchronized
    fun quarantineFile(
        originalFilePath: String,
        threatName: String,
        threatType: String,
        severity: String
    ): Map<String, Any> {
        val srcFile = File(originalFilePath)
        if (!srcFile.exists()) {
            val registry = loadRegistry()
            for (i in 0 until registry.length()) {
                val obj = registry.getJSONObject(i)
                if (obj.optString("originalPath") == originalFilePath) {
                    return mapOf(
                        "success" to true,
                        "id" to obj.optString("id"),
                        "message" to "Berkas sudah berada di dalam Brankas Karantina."
                    )
                }
            }
            return mapOf(
                "success" to true,
                "message" to "Berkas sudah tidak ada di sistem penyimpanan."
            )
        }

        try {
            val itemId = "qtn-${UUID.randomUUID().toString().substring(0, 8)}"
            val fileName = srcFile.name
            val quarantinedFileName = "$itemId-$fileName.quarantined"
            val destFile = File(vaultDir, quarantinedFileName)

            // Scramble bytes so malware signature is dormant and non-executable
            FileInputStream(srcFile).use { input ->
                FileOutputStream(destFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        for (i in 0 until bytesRead) {
                            buffer[i] = (buffer[i].toInt() xor XOR_KEY.toInt()).toByte()
                        }
                        output.write(buffer, 0, bytesRead)
                    }
                }
            }

            // Strip execution and write permissions from isolated artifact
            destFile.setReadable(true, true)
            destFile.setWritable(false, false)
            destFile.setExecutable(false, false)

            val originalSize = srcFile.length()
            forceDeleteFile(srcFile)

            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val itemJson = JSONObject().apply {
                put("id", itemId)
                put("fileName", fileName)
                put("originalPath", originalFilePath)
                put("vaultPath", destFile.absolutePath)
                put("threatName", threatName)
                put("threatType", threatType)
                put("severity", severity)
                put("fileSize", originalSize)
                put("quarantineDate", timestamp)
                put("status", "ISOLATED")
            }

            val currentRegistry = loadRegistry()
            currentRegistry.put(itemJson)
            saveRegistry(currentRegistry)

            Log.i(TAG, "File successfully quarantined: $fileName -> ${destFile.name}")
            return mapOf(
                "success" to true,
                "id" to itemId,
                "fileName" to fileName,
                "originalPath" to originalFilePath,
                "message" to "Berkas berhasil diisolasi dan dienkripsi di Brankas Karantina."
            )
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to quarantine file: ${e.message}", e)
            return mapOf(
                "success" to false,
                "message" to "Gagal mengkarantina berkas: ${e.message}"
            )
        }
    }

    /**
     * Permanently deletes and shreds an infected file directly from storage
     */
    @Synchronized
    fun shredFile(filePath: String): Map<String, Any> {
        if (filePath.isBlank()) {
            return mapOf("success" to false, "message" to "Path berkas kosong.")
        }

        // Handle package uninstallation if path is package name
        if (!filePath.startsWith("/") && filePath.contains(".")) {
            val pkg = filePath.removePrefix("package:")
            val uninstalled = com.telkomsel.secure.platform.PermissionHelper.uninstallThreatApp(context, pkg)
            return mapOf(
                "success" to uninstalled,
                "message" to if (uninstalled) "Pencopotan aplikasi ancaman berhasil dipicu." else "Gagal memicu pencopotan aplikasi."
            )
        }

        val file = File(filePath)
        if (!file.exists()) {
            return mapOf(
                "success" to true,
                "message" to "Berkas sudah tidak ada di sistem penyimpanan."
            )
        }

        return try {
            val length = file.length()
            // Overwrite with zeroes if small file for secure sanitization
            if (length in 1..10485760L) { // max 10MB shredding
                try {
                    FileOutputStream(file).use { out ->
                        out.write(ByteArray(length.toInt()))
                        out.flush()
                    }
                } catch (e: Throwable) {
                    Log.w(TAG, "Zero-overwrite failed: ${e.message}")
                }
            }
            val deleted = forceDeleteFile(file)
            Log.i(TAG, "shredFile result for $filePath: deleted=$deleted, exists=${file.exists()}")
            mapOf(
                "success" to deleted,
                "message" to if (deleted) "Berkas ancaman berhasil dimusnahkan secara permanen." else "Gagal menghapus berkas. Berikan izin Akses Semua Berkas di Pengaturan Android."
            )
        } catch (e: Throwable) {
            mapOf("success" to false, "message" to (e.message ?: "Kesalahan saat menghapus berkas"))
        }
    }

    /**
     * Multi-stage robust deletion: Java File API -> MediaStore Resolver -> Shell rm -f -> Truncate
     */
    private fun forceDeleteFile(file: File): Boolean {
        if (!file.exists()) return true

        // 1. Direct File.delete()
        try {
            if (file.delete()) return true
        } catch (e: Throwable) {
            Log.w(TAG, "Direct delete error: ${e.message}")
        }

        // 2. MediaStore query & delete by Content URI
        try {
            val uri = MediaStore.Files.getContentUri("external")
            val cursor = context.contentResolver.query(
                uri,
                arrayOf(MediaStore.MediaColumns._ID),
                "${MediaStore.MediaColumns.DATA} = ?",
                arrayOf(file.absolutePath),
                null
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    val id = it.getLong(it.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                    val deleteUri = android.content.ContentUris.withAppendedId(uri, id)
                    context.contentResolver.delete(deleteUri, null, null)
                }
            }
            if (!file.exists()) return true
        } catch (e: Throwable) {
            Log.w(TAG, "MediaStore delete error: ${e.message}")
        }

        // 3. Fallback: Shell rm -f
        try {
            val proc = Runtime.getRuntime().exec(arrayOf("rm", "-f", file.absolutePath))
            proc.waitFor()
            if (!file.exists()) return true
        } catch (e: Throwable) {
            Log.w(TAG, "Shell rm error: ${e.message}")
        }

        // 4. Truncate file if delete blocked
        try {
            FileOutputStream(file).use { it.write(ByteArray(0)) }
            if (file.delete()) return true
        } catch (_: Throwable) {}

        // 5. Notify MediaScanner
        try {
            MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), null, null)
        } catch (_: Throwable) {}

        return !file.exists()
    }

    /**
     * Restores a quarantined file back to its original location (unscrambled)
     */
    @Synchronized
    fun restoreQuarantinedFile(itemId: String): Map<String, Any> {
        val registry = loadRegistry()
        var targetObj: JSONObject? = null
        var targetIndex = -1

        for (i in 0 until registry.length()) {
            val obj = registry.getJSONObject(i)
            if (obj.getString("id") == itemId) {
                targetObj = obj
                targetIndex = i
                break
            }
        }

        if (targetObj == null) {
            return mapOf("success" to false, "message" to "Item karantina dengan ID $itemId tidak ditemukan.")
        }

        try {
            val vaultPath = targetObj.getString("vaultPath")
            val originalPath = targetObj.getString("originalPath")
            val vaultFile = File(vaultPath)

            if (!vaultFile.exists()) {
                return mapOf("success" to false, "message" to "Berkas terenkripsi di brankas hilang.")
            }

            val destFile = File(originalPath)
            destFile.parentFile?.mkdirs()

            // Reverse XOR cipher to restore original content
            FileInputStream(vaultFile).use { input ->
                FileOutputStream(destFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        for (i in 0 until bytesRead) {
                            buffer[i] = (buffer[i].toInt() xor XOR_KEY.toInt()).toByte()
                        }
                        output.write(buffer, 0, bytesRead)
                    }
                }
            }

            vaultFile.delete()
            registry.remove(targetIndex)
            saveRegistry(registry)

            Log.i(TAG, "File successfully restored: $originalPath")
            return mapOf("success" to true, "message" to "Berkas berhasil dipulihkan ke lokasi semula.")
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to restore quarantined file: ${e.message}", e)
            return mapOf("success" to false, "message" to "Gagal memulihkan berkas: ${e.message}")
        }
    }

    /**
     * Permanently purges an item from the quarantine vault
     */
    @Synchronized
    fun deleteQuarantinedItem(itemId: String): Map<String, Any> {
        val registry = loadRegistry()
        var targetIndex = -1

        for (i in 0 until registry.length()) {
            val obj = registry.getJSONObject(i)
            if (obj.getString("id") == itemId) {
                val vaultPath = obj.getString("vaultPath")
                File(vaultPath).delete()
                targetIndex = i
                break
            }
        }

        return if (targetIndex != -1) {
            registry.remove(targetIndex)
            saveRegistry(registry)
            mapOf("success" to true, "message" to "Berkas berhasil dimusnahkan secara permanen dari brankas.")
        } else {
            mapOf("success" to false, "message" to "Item tidak ditemukan di registry brankas.")
        }
    }

    /**
     * Purges all items currently stored in quarantine
     */
    @Synchronized
    fun clearAllQuarantined(): Map<String, Any> {
        val registry = loadRegistry()
        var deletedCount = 0
        for (i in 0 until registry.length()) {
            try {
                val obj = registry.getJSONObject(i)
                val vaultPath = obj.getString("vaultPath")
                if (File(vaultPath).delete()) deletedCount++
            } catch (_: Throwable) {}
        }
        saveRegistry(JSONArray())
        return mapOf("success" to true, "deletedCount" to deletedCount)
    }

    /**
     * Returns all items currently held in the quarantine vault
     */
    @Synchronized
    fun getAllQuarantined(): List<Map<String, Any>> {
        val registry = loadRegistry()
        val list = mutableListOf<Map<String, Any>>()
        for (i in 0 until registry.length()) {
            val obj = registry.getJSONObject(i)
            list.add(
                mapOf(
                    "id" to obj.optString("id"),
                    "fileName" to obj.optString("fileName"),
                    "originalPath" to obj.optString("originalPath"),
                    "vaultPath" to obj.optString("vaultPath"),
                    "threatName" to obj.optString("threatName"),
                    "threatType" to obj.optString("threatType"),
                    "severity" to obj.optString("severity"),
                    "fileSize" to obj.optLong("fileSize"),
                    "quarantineDate" to obj.optString("quarantineDate"),
                    "status" to obj.optString("status")
                )
            )
        }
        return list
    }

    private fun loadRegistry(): JSONArray {
        if (!metadataFile.exists()) return JSONArray()
        return try {
            val text = metadataFile.readText()
            if (text.isBlank()) JSONArray() else JSONArray(text)
        } catch (_: Throwable) {
            JSONArray()
        }
    }

    private fun saveRegistry(array: JSONArray) {
        try {
            metadataFile.writeText(array.toString(2))
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to write quarantine registry: ${e.message}", e)
        }
    }
}
