package com.telkomsel.secure.kaspersky

import android.content.Context
import android.os.Environment
import android.util.Log
import com.kavsdk.antivirus.AntivirusInstance
import com.kavsdk.antivirus.ScannerConstants
import com.kavsdk.antivirus.ScannerEventListener
import com.kavsdk.antivirus.ThreatInfo
import com.kavsdk.antivirus.ThreatType
import java.io.File

/**
 * Enterprise Storage Threat Scanner
 * Inspects user storage directories (Downloads, Documents) using Kaspersky Scanner engine & standard test signatures.
 */
object StorageThreatScanner {
    private const val TAG = "StorageThreatScanner"

    fun scanStorageFolders(
        context: Context,
        onFileScanned: (path: String) -> Unit,
        onThreatFound: (threatName: String, path: String, isMalware: Boolean) -> Unit,
        isCancelled: () -> Boolean = { false }
    ): Int {
        var threatCount = 0
        val targetDirs = mutableListOf<File>()

        try {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)?.let {
                if (it.exists()) targetDirs.add(it)
            }
        } catch (_: Throwable) {}

        val altDownload = File("/sdcard/Download")
        if (altDownload.exists() && !targetDirs.contains(altDownload)) {
            targetDirs.add(altDownload)
        }

        try {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)?.let {
                if (it.exists() && !targetDirs.contains(it)) targetDirs.add(it)
            }
        } catch (_: Throwable) {}

        val filesToScan = mutableListOf<File>()
        for (dir in targetDirs) {
            dir.listFiles()?.filter { it.isFile && it.length() > 0 && it.length() < 100 * 1024 * 1024 }?.let {
                filesToScan.addAll(it)
            }
        }

        Log.i(TAG, ">>> [STORAGE SCAN] Found ${filesToScan.size} storage files to inspect.")

        val scanner = try {
            if (AntivirusInstance.getInstance().isInitialized) {
                AntivirusInstance.getInstance().createScanner()
            } else null
        } catch (e: Throwable) {
            Log.w(TAG, "Scanner creation note: ${e.message}")
            null
        }

        for (file in filesToScan) {
            if (isCancelled()) {
                Log.i(TAG, ">>> [STORAGE SCAN] Scan cancelled by user.")
                break
            }

            onFileScanned(file.name)
            var fileThreatDetected = false
            var detectedThreatName = ""

            // Genuine Kaspersky scanFile inspection
            if (scanner != null) {
                try {
                    val mode = ScannerConstants.SCAN_MODE_ALLOW_UDS or ScannerConstants.SCAN_MODE_DETECT_RISKWARE_ADWARE
                    scanner.scanFile(file.absolutePath, mode, ScannerConstants.CLEAN_MODE_DONOTCLEAN, object : ScannerEventListener {
                        override fun onScanEvent(eventType: Int, percent: Int, threatInfo: ThreatInfo?, threatType: ThreatType?): Int {
                            if (threatInfo != null && !threatInfo.virusName.isNullOrBlank()) {
                                fileThreatDetected = true
                                detectedThreatName = threatInfo.virusName ?: "Malware"
                                Log.w(TAG, ">>> [STORAGE SCAN] Kaspersky detected: $detectedThreatName in ${file.absolutePath}")
                            }
                            return ScannerConstants.EVENT_RESULT_OK
                        }
                    }, true)
                } catch (e: Throwable) {
                    Log.w(TAG, ">>> [STORAGE SCAN] scanFile error for ${file.name}: ${e.message}")
                }
            }

            if (fileThreatDetected) {
                threatCount++
                val finalName = if (detectedThreatName.isNotBlank()) detectedThreatName else "Ancaman-Penyimpanan"
                Log.w(TAG, ">>> [STORAGE SCAN] THREAT REGISTERED: $finalName at ${file.absolutePath}")
                onThreatFound(finalName, file.absolutePath, true)
            }
        }

        return threatCount
    }
}
