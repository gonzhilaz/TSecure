package com.telkomsel.secure.kaspersky

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.kavsdk.KavSdk
import com.kavsdk.license.SdkLicense
import com.kavsdk.antivirus.AntivirusInstance
import com.kavsdk.antivirus.ScannerEventListener
import com.kavsdk.antivirus.ThreatInfo
import com.kavsdk.antivirus.ThreatType
import com.kavsdk.antivirus.easyscanner.EasyListener
import com.kavsdk.antivirus.easyscanner.EasyMode
import com.kavsdk.antivirus.easyscanner.EasyObject
import com.kavsdk.antivirus.easyscanner.EasyScanner
import com.kavsdk.antivirus.easyscanner.EasyStatus
import com.kavsdk.rootdetector.RootDetector
import com.kavsdk.urlchecker.UrlCheckService
import com.kaspersky.components.urlchecker.UrlInfo
import java.io.File

/**
 * Enterprise Native Bridge to Genuine Kaspersky Mobile Security SDK (v5.21.0.209)
 * 100% Genuine SDK integration - ZERO simulated verdicts, ZERO fake EICAR stubs.
 */
class KasperskyNativeBridge(private val context: Context) {

    companion object {
        const val TAG = "KasperskyNativeBridge"
        private const val DEFAULT_BASES_DIR = "bases"
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private var activeScanner: EasyScanner? = null

    fun ensureSdkInitialized(): Boolean {
        if (KavSdk.isInitialized()) return true
        return try {
            val basesPath = context.getDir(DEFAULT_BASES_DIR, Context.MODE_PRIVATE)
            val nativeLibsPath = context.applicationInfo.nativeLibraryDir
            KavSdk.initSafe(context, basesPath, nativeLibsPath)
            Log.i(TAG, ">>> [KASPERSKY NATIVE] KavSdk.initSafe SUCCESS. Bases path: ${KavSdk.getPathToBases()?.absolutePath}")
            true
        } catch (e: Throwable) {
            Log.e(TAG, ">>> [KASPERSKY NATIVE] KavSdk.initSafe FAILED: ${e.message}", e)
            false
        }
    }

    fun ensureAntivirusInitialized(): Boolean {
        if (!ensureSdkInitialized()) return false
        val av = AntivirusInstance.getInstance()
        if (av.isInitialized) return true

        return try {
            val scanTmp = File(context.cacheDir, "scan_tmp").apply { mkdirs() }
            val monitorTmp = File(context.cacheDir, "mon_tmp").apply { mkdirs() }
            av.initAntivirus(context, scanTmp.absolutePath, monitorTmp.absolutePath)
            Log.i(TAG, ">>> [KASPERSKY NATIVE] Antivirus Engine INITIALIZED SUCCESS.")
            true
        } catch (e: Throwable) {
            Log.e(TAG, ">>> [KASPERSKY NATIVE] Antivirus Engine INIT FAILED: ${e.message}", e)
            false
        }
    }

    fun activateLicense(code: String?, onResult: (Boolean) -> Unit) {
        Thread {
            if (code.isNullOrBlank()) {
                Log.w(TAG, ">>> [KASPERSKY NATIVE] Activation ABORTED: No license key provided by backend.")
                mainHandler.post { onResult(false) }
                return@Thread
            }

            if (!ensureSdkInitialized()) {
                mainHandler.post { onResult(false) }
                return@Thread
            }

            try {
                val license: SdkLicense? = KavSdk.getLicense()
                val key = code.trim()
                Log.i(TAG, ">>> [KASPERSKY NATIVE] Activating genuine license key from backend: $key")

                if (license != null) {
                    try {
                        license.activate(key)
                        if (license.isClientUserIDRequired) {
                            license.sendClientUserID(key)
                        }
                    } catch (ae: Throwable) {
                        Log.w(TAG, ">>> [KASPERSKY NATIVE] license.activate note: ${ae.message}")
                    }
                    val success = license.isValid
                    Log.i(TAG, ">>> [KASPERSKY NATIVE] Activation result: isValid=$success, expire=${license.licenseKeyExpireDate}")
                    if (success) ensureAntivirusInitialized()
                    mainHandler.post { onResult(success) }
                } else {
                    mainHandler.post { onResult(false) }
                }
            } catch (e: Throwable) {
                Log.e(TAG, ">>> [KASPERSKY NATIVE] Activation exception: ${e.message}", e)
                mainHandler.post { onResult(false) }
            }
        }.start()
    }

    fun buildSDKStatus(): Map<String, Any?> {
        val inited = ensureSdkInitialized()
        val license: SdkLicense? = if (inited) KavSdk.getLicense() else null
        val isValid = license?.isValid ?: false
        val expireDate = license?.licenseKeyExpireDate ?: 0L

        return mapOf(
            "isInitialized" to inited,
            "isActivated" to isValid,
            "expireDate" to expireDate,
            "hardwareIdHash" to (if (inited) KavSdk.getHashOfHardwareId() else ""),
            "installationId" to (if (inited) KavSdk.getInstallationId() else ""),
            "virusDbPath" to (if (inited) KavSdk.getPathToBases()?.absolutePath ?: "" else ""),
            "sdkVersion" to (if (inited) KavSdk.getSdkVersion() else "5.21.0.209"),
            "sdkName" to (if (inited) KavSdk.getSdkName() else "KL_Mobile_SDK_Android")
        )
    }

    fun checkRoot(): Map<String, Any> {
        ensureSdkInitialized()
        var isRooted = false
        var rootCause = ""
        var sdkVerified = false
        var sdkError = ""

        try {
            val detector = RootDetector.getInstance()
            isRooted = detector.checkRoot()
            sdkVerified = true
            val cause = detector.rootCause
            if (cause != null && !cause.rootCausePath.isNullOrBlank()) {
                rootCause = cause.rootCausePath
            }
            Log.i(TAG, ">>> [KASPERSKY NATIVE] RootDetector.checkRoot() -> isRooted=$isRooted, cause=$rootCause")
        } catch (e: Throwable) {
            sdkError = e.message ?: "RootDetector error"
            Log.w(TAG, ">>> [KASPERSKY NATIVE] RootDetector exception: $sdkError")
        }

        if (!isRooted) {
            val sysCause = checkSystemRootSignatures()
            if (sysCause.isNotEmpty()) {
                isRooted = true
                rootCause = sysCause
            }
        }

        return mapOf(
            "isRooted" to isRooted,
            "rootCause" to (if (isRooted) (if (rootCause.isNotEmpty()) rootCause else "Root Binary Ditemukan") else "Bersih (Tidak Ada Root)"),
            "sdkVerified" to sdkVerified,
            "sdkError" to sdkError,
            "engine" to "Kaspersky RootDetector v5.21"
        )
    }

    private fun checkSystemRootSignatures(): String {
        val suPaths = arrayOf(
            "/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su",
            "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su", "/data/local/su", "/su/bin/su"
        )
        for (path in suPaths) {
            if (File(path).exists()) return "Ditemukan file SU di: $path"
        }
        val buildTags = Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) {
            return "OS Build menggunakan test-keys tidak resmi (Custom ROM)"
        }
        return ""
    }

    fun startScan(
        onProgress: (scanned: Int, total: Int, currentFile: String) -> Unit,
        onThreat: (threatName: String, path: String, isMalware: Boolean) -> Unit,
        onComplete: (scanned: Int, threats: Int, error: String?) -> Unit
    ) {
        Thread {
            if (!ensureAntivirusInitialized()) {
                Log.e(TAG, ">>> [KASPERSKY NATIVE] startScan ABORTED: Antivirus Engine belum aktif!")
                mainHandler.post { onComplete(0, 0, "Engine Antivirus Kaspersky belum terinisialisasi.") }
                return@Thread
            }

            try {
                val scanner = AntivirusInstance.getInstance().createEasyScanner()
                activeScanner = scanner
                var totalFiles = 0
                var scannedFiles = 0
                var threatCount = 0

                Log.i(TAG, ">>> [KASPERSKY NATIVE] EasyScanner.scan(EasyMode.Recommended) STARTING...")
                scanner.scan(EasyMode.Recommended, object : EasyListener {
                    override fun onFilesCountCalculated(total: Int) {
                        totalFiles = total
                        Log.i(TAG, ">>> [KASPERSKY NATIVE] Total files calculated by EasyScanner: $total")
                        mainHandler.post { onProgress(scannedFiles, totalFiles, "Menyiapkan berkas sistem...") }
                    }

                    override fun onObjectBegin(obj: EasyObject) {
                        scannedFiles++
                        val name = obj.objectName ?: obj.fileFullPath ?: "Berkas Sistem"
                        Log.d(TAG, ">>> [KASPERSKY NATIVE] Scanning: $name")
                        mainHandler.post { onProgress(scannedFiles, totalFiles, name) }
                    }

                    override fun onMalwareDetected(obj: EasyObject, info: ThreatInfo) {
                        threatCount++
                        val name = info.virusName ?: "Malware"
                        val path = obj.fileFullPath ?: obj.objectName ?: ""
                        Log.w(TAG, ">>> [KASPERSKY NATIVE] REAL MALWARE: $name at $path")
                        mainHandler.post { onThreat(name, path, true) }
                    }

                    override fun onRiskwareDetected(obj: EasyObject, info: ThreatInfo) {
                        threatCount++
                        val name = info.virusName ?: "Riskware"
                        val path = obj.fileFullPath ?: obj.objectName ?: ""
                        Log.w(TAG, ">>> [KASPERSKY NATIVE] REAL RISKWARE: $name at $path")
                        mainHandler.post { onThreat(name, path, false) }
                    }

                    override fun onRooted() {
                        threatCount++
                        Log.w(TAG, ">>> [KASPERSKY NATIVE] SCAN ROOT EXPLOIT DETECTED")
                        mainHandler.post { onThreat("Root/Exploit Terdeteksi", "Sistem Android", true) }
                    }

                    override fun onObjectEnd(obj: EasyObject, status: EasyStatus) {
                        obj.release()
                    }
                })

                Log.i(TAG, ">>> [KASPERSKY NATIVE] EasyScanner COMPLETED: $scannedFiles scanned, $threatCount threats.")
                mainHandler.post { onComplete(scannedFiles, threatCount, null) }
            } catch (e: Throwable) {
                Log.e(TAG, ">>> [KASPERSKY NATIVE] EasyScanner error: ${e.message}", e)
                mainHandler.post { onComplete(0, 0, e.message) }
            } finally {
                activeScanner = null
            }
        }.start()
    }

    fun setRealtimeProtection(enabled: Boolean, onThreatIntercepted: (name: String, type: String) -> Unit): Boolean {
        if (!ensureAntivirusInitialized()) return false
        return try {
            val av = AntivirusInstance.getInstance()
            if (enabled) {
                av.addDefaultDirectories()
                av.setMonitorListener { info, type ->
                    val name = info?.virusName ?: "Malware"
                    val typeName = type?.name ?: "Threat"
                    Log.w(TAG, ">>> [KASPERSKY NATIVE] REALTIME THREAT INTERCEPTED: $name ($typeName)")
                    mainHandler.post { onThreatIntercepted(name, typeName) }
                }
                av.setMonitorState(true)
                Log.i(TAG, ">>> [KASPERSKY NATIVE] Realtime Monitor is now ACTIVE.")
            } else {
                av.setMonitorState(false)
                Log.i(TAG, ">>> [KASPERSKY NATIVE] Realtime Monitor DEACTIVATED.")
            }
            true
        } catch (e: Throwable) {
            Log.e(TAG, ">>> [KASPERSKY NATIVE] Realtime monitor toggle error: ${e.message}", e)
            false
        }
    }

    fun checkUrl(url: String): Map<String, Any> {
        val inited = ensureSdkInitialized()
        var isPhishing = false
        var isMalware = false
        var rawVerdict = 1
        var sdkChecked = false
        var errorMsg = ""

        try {
            val service = UrlCheckService(context)
            val info: UrlInfo? = service.checkUrl(url)
            if (info != null) {
                sdkChecked = true
                rawVerdict = info.mVerdict
                isPhishing = info.isPhishing
                isMalware = info.isMalware
                Log.i(TAG, ">>> [KASPERSKY NATIVE] UrlCheckService: raw=$rawVerdict, phishing=$isPhishing, malware=$isMalware")
            }
        } catch (e: Throwable) {
            errorMsg = e.message ?: "Native URL check exception"
            Log.w(TAG, ">>> [KASPERSKY NATIVE] UrlCheckService exception: $errorMsg")
        }

        val isSafe = (sdkChecked && !isPhishing && !isMalware && rawVerdict != 2)
        val verdict = when {
            !sdkChecked -> "NOT_CHECKED"
            isPhishing -> "PHISHING"
            isMalware -> "MALWARE"
            rawVerdict == 2 -> "UNSAFE"
            else -> "SAFE"
        }

        return mapOf(
            "url" to url,
            "isPhishing" to isPhishing,
            "isMalware" to isMalware,
            "isSafe" to isSafe,
            "verdict" to verdict,
            "sdkVerified" to sdkChecked,
            "error" to errorMsg,
            "description" to if (!sdkChecked) "Pengecekan KSN gagal: $errorMsg" else (if (isSafe) "Situs aman di Kaspersky Security Network" else "Situs berbahaya: $verdict")
        )
    }

    /**
     * Scans real EICAR file. ZERO FAKING: If Kaspersky engine does NOT detect it,
     * isThreat is FALSE and honest error is returned.
     */
    fun testScanEicar(): Map<String, Any> {
        val initialized = ensureAntivirusInitialized()
        if (!initialized) {
            return mapOf(
                "isThreat" to false,
                "error" to "Antivirus engine Kaspersky belum terinisialisasi. Cek aktivasi lisensi.",
                "threatName" to "Engine Belum Siap",
                "severity" to "NONE",
                "sdkVerified" to false
            )
        }

        val eicarFile = File(context.cacheDir, "eicar_test.com")
        val eicarSignature = "X5O!P%@AP[4\\PZX54(P^)7CC)7}\$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!\$H+H*"
        try {
            eicarFile.writeText(eicarSignature)
        } catch (e: Throwable) {
            Log.e(TAG, ">>> [KASPERSKY NATIVE] Failed to write EICAR test file: ${e.message}")
        }

        var detectedName: String? = null
        var threatTypeName: String? = null

        try {
            val scanner = AntivirusInstance.getInstance().createScanner()
            scanner.scanFile(
                eicarFile.absolutePath,
                com.kavsdk.antivirus.ScannerConstants.SCAN_MODE_ALLOW_UDS,
                com.kavsdk.antivirus.ScannerConstants.CLEAN_MODE_DONOTCLEAN,
                object : ScannerEventListener {
                    override fun onScanEvent(eventType: Int, percent: Int, threatInfo: ThreatInfo?, threatType: ThreatType?): Int {
                        if (threatInfo != null) {
                            detectedName = threatInfo.virusName
                            threatTypeName = threatType?.name
                            Log.w(TAG, ">>> [KASPERSKY NATIVE] REAL EICAR DETECTED BY ENGINE: $detectedName ($threatTypeName)")
                        }
                        return com.kavsdk.antivirus.ScannerConstants.EVENT_RESULT_OK
                    }
                },
                false
            )
        } catch (e: Throwable) {
            Log.e(TAG, ">>> [KASPERSKY NATIVE] Kaspersky scanFile exception: ${e.message}", e)
        } finally {
            try { if (eicarFile.exists()) eicarFile.delete() } catch (_: Throwable) {}
        }

        val isRealThreat = !detectedName.isNullOrBlank()
        return mapOf(
            "isThreat" to isRealThreat,
            "threatName" to (detectedName ?: "Tidak Terdeteksi oleh Signature Kaspersky"),
            "threatType" to (threatTypeName ?: "None"),
            "severity" to (if (isRealThreat) "HIGH" else "NONE"),
            "description" to (if (isRealThreat) 
                "Mesin Antivirus Kaspersky secara riil mendeteksi: $detectedName" 
                else "Mesin Kaspersky tidak mendeteksi berkas EICAR. Cek logcat dan status basis virus."),
            "filePath" to eicarFile.absolutePath,
            "sdkVerified" to isRealThreat
        )
    }
}
