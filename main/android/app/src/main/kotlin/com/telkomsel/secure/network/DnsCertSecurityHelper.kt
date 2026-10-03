package com.telkomsel.secure.network

import com.kavsdk.certificatechecker.CertificateCheckService
import com.kavsdk.certificatechecker.CertificateCheckTelemetry
import com.kavsdk.dnschecker.DnsChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Enterprise SSL Certificate & DNS Resolution Checker powered by Kaspersky Mobile SDK.
 * Verifies certificate chains, expiration, revocation, and DNS spoofing / cache poisoning.
 */
object DnsCertSecurityHelper {

    suspend fun checkCertificate(rawUrl: String): Map<String, Any?> = withContext(Dispatchers.IO) {
        try {
            val url = if (rawUrl.startsWith("http://") || rawUrl.startsWith("https://")) {
                rawUrl
            } else {
                "https://$rawUrl"
            }

            val service = CertificateCheckService()
            val checkResult = service.checkCertificate(url)

            val telemetryMap = mutableMapOf<String, Any?>()
            for (telemetry in CertificateCheckTelemetry.values()) {
                try {
                    val value = checkResult.getTelemetry(telemetry)
                    telemetryMap[telemetry.name] = value
                } catch (_: Throwable) {}
            }

            val verdictName = checkResult.verdict.name
            val isValid = verdictName.equals("VALID", ignoreCase = true) || verdictName.equals("TRUSTED", ignoreCase = true) || verdictName.equals("OK", ignoreCase = true)
            mapOf(
                "url" to url,
                "verdict" to verdictName,
                "extendedVerdict" to checkResult.extendedVerdict.name,
                "isValid" to isValid,
                "telemetry" to telemetryMap,
                "error" to null
            )
        } catch (e: Throwable) {
            mapOf(
                "url" to rawUrl,
                "verdict" to "ERROR",
                "extendedVerdict" to (e.message ?: "Check failed"),
                "isValid" to false,
                "telemetry" to emptyMap<String, Any?>(),
                "error" to (e.message ?: e.javaClass.simpleName)
            )
        }
    }

    suspend fun checkDns(rawUrl: String, trustedIps: List<String>): Map<String, Any?> = withContext(Dispatchers.IO) {
        try {
            val url = if (rawUrl.startsWith("http://") || rawUrl.startsWith("https://")) {
                rawUrl
            } else {
                "https://$rawUrl"
            }

            val checker = DnsChecker()
            val cloudResult = checker.checkURL(url)
            val isTrusted = if (trustedIps.isNotEmpty()) checker.checkURL(url, trustedIps) else true
            val cloudIps = cloudResult.trustedIpList ?: emptyList<String>()
            val matchedIps = cloudIps.filter { trustedIps.contains(it) }

            val dnsVerdictName = cloudResult.verdict.name
            val isSafe = dnsVerdictName.equals("SAFE", ignoreCase = true) || dnsVerdictName.equals("VALID", ignoreCase = true) || dnsVerdictName.equals("OK", ignoreCase = true)
            mapOf(
                "url" to url,
                "verdict" to dnsVerdictName,
                "cloudTrustedIps" to cloudIps,
                "matchedIps" to matchedIps,
                "isTrusted" to isTrusted,
                "isSafe" to isSafe,
                "error" to null
            )
        } catch (e: Throwable) {
            mapOf(
                "url" to rawUrl,
                "verdict" to "ERROR",
                "cloudTrustedIps" to emptyList<String>(),
                "matchedIps" to emptyList<String>(),
                "isTrusted" to false,
                "isSafe" to false,
                "error" to (e.message ?: e.javaClass.simpleName)
            )
        }
    }
}
