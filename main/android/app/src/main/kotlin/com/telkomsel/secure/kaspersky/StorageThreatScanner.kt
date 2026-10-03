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
    private const val EICAR_SIG = "X5O!P%@AP[4\\PZX54(P^)7CC)7}\$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!\$H+H*"

    fun scanStorageFolders(
        context: Context,
        onFileScanned: (path: String) -> Unit,
        onThreatFound: (threatName: String, path: String, isMalware: Boolean) -> Unit
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
            onFileScanned(file.name)
            var fileThreatDetected = false
            var detectedThreatName = ""

            // 1. Genuine Kaspersky scanFile inspection
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

            // 2. Standard EICAR Test Signature Verification
            if (!fileThreatDetected && file.length() < 2048) {
                try {
                    val content = file.readText().trim()
                    if (content.contains(EICAR_SIG) || content.startsWith("X5O!P%@AP[4\\PZX54(P^)7CC)7}")) {
                        fileThreatDetected = true
                        detectedThreatName = "EICAR-Standard-AV-Test"
                        Log.w(TAG, ">>> [STORAGE SCAN] EICAR signature matched in: ${file.absolutePath}")
                    }
                } catch (_: Throwable) {}
            }

            if (fileThreatDetected) {
                threatCount++
                val finalName = if (detectedThreatName.isNotBlank()) detectedThreatName else "EICAR-Test-File"
                Log.w(TAG, ">>> [STORAGE SCAN] THREAT REGISTERED: $finalName at ${file.absolutePath}")
                onThreatFound(finalName, file.absolutePath, true)
            }
        }

        return threatCount
    }
}
