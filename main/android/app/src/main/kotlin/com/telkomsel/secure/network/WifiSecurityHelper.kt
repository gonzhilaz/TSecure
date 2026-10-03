package com.telkomsel.secure.network

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import androidx.core.content.ContextCompat
import java.net.Inet4Address

/**
 * Native Wi-Fi inspector. Reports ONLY values read from the device.
 * Anything the OS does not expose is returned as empty / "unknown" — never invented.
 */
object WifiSecurityHelper {

    private const val UNREADABLE_SSID = "<unknown ssid>"

    fun getWifiSecurityStatus(context: Context): Map<String, Any?> {
        val hasLocationPerm = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val gpsOn = isLocationEnabled(context)

        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            val vpnActive = cm.allNetworks.any {
                cm.getNetworkCapabilities(it)?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true
            }
            val wifiNet = findWifiNetwork(cm)

            if (wifiNet == null) {
                val active = cm.getNetworkCapabilities(cm.activeNetwork)
                val cellular = active?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
                return notOnWifi(cellular, vpnActive, hasLocationPerm, gpsOn)
            }

            val caps = cm.getNetworkCapabilities(wifiNet)
            @Suppress("DEPRECATION")
            val info: WifiInfo? = wm?.connectionInfo ?: (caps?.transportInfo as? WifiInfo)

            val ssid = readSsid(context, info)
            val bssid = info?.bssid?.takeIf { it.isNotEmpty() && it != "02:00:00:00:00:00" } ?: ""

            val secType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) info?.currentSecurityType ?: -1 else -1
            val sec = describeSecurity(secType)

            val lp = cm.getLinkProperties(wifiNet)
            val ip = lp?.linkAddresses?.firstOrNull { it.address is Inet4Address }?.address?.hostAddress ?: ""
            val gateway = lp?.routes?.firstOrNull { it.isDefaultRoute && it.gateway is Inet4Address }?.gateway?.hostAddress ?: ""
            val dns = lp?.dnsServers?.mapNotNull { it.hostAddress }?.joinToString(", ") ?: ""

            val captive = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_CAPTIVE_PORTAL) == true
            val rssi = info?.rssi ?: Int.MIN_VALUE
            val signal = when {
                rssi == Int.MIN_VALUE || rssi <= -127 -> "Tidak terbaca"
                rssi >= -60 -> "Sangat Baik ($rssi dBm)"
                rssi >= -70 -> "Baik ($rssi dBm)"
                rssi >= -80 -> "Cukup ($rssi dBm)"
                else -> "Lemah ($rssi dBm)"
            }

            val verdict = kasperskyVerdict(context)
            val risky = captive || sec.isOpen || sec.isWeak || verdict.safe == false
            val summary = when {
                !hasLocationPerm -> "Izin Lokasi belum diberikan, nama Wi-Fi tidak dapat dibaca."
                !gpsOn -> "Layanan Lokasi (GPS) mati, nama Wi-Fi tidak dapat dibaca."
                sec.isOpen -> "Peringatan: Wi-Fi terbuka tanpa enkripsi."
                sec.isWeak -> "Peringatan: Wi-Fi memakai enkripsi usang (WEP)."
                captive -> "Peringatan: Wi-Fi memerlukan login portal (captive portal)."
                verdict.safe == false -> "Peringatan: Kaspersky menandai jaringan ini berisiko."
                !sec.known -> "Jenis enkripsi tidak dapat dibaca oleh sistem Android ini."
                else -> "Wi-Fi terenkripsi (${sec.label})."
            }

            mapOf(
                "isConnected" to true,
                "isWifi" to true,
                "vpnActive" to vpnActive,
                "ssid" to ssid,
                "ssidAvailable" to ssid.isNotEmpty(),
                "networkType" to "Wi-Fi",
                "bssid" to bssid,
                "securityProtocol" to sec.label,
                "securityKnown" to sec.known,
                "isEncrypted" to (sec.known && !sec.isOpen),
                "isCaptivePortal" to captive,
                "isOpenNetwork" to sec.isOpen,
                "isSafe" to !risky,
                "kasperskyVerdict" to verdict.name,
                "isGpsEnabled" to gpsOn,
                "hasLocationPermission" to hasLocationPerm,
                "signalLevel" to signal,
                "linkSpeed" to if ((info?.linkSpeed ?: -1) > 0) "${info?.linkSpeed} Mbps" else "",
                "ipAddress" to ip,
                "gateway" to gateway,
                "dnsResolver" to dns,
                "summary" to summary
            )
        } catch (e: Throwable) {
            mapOf(
                "isConnected" to false,
                "isWifi" to false,
                "ssid" to "",
                "ssidAvailable" to false,
                "error" to (e.message ?: e.javaClass.simpleName),
                "summary" to "Audit Wi-Fi gagal dibaca dari sistem."
            )
        }
    }

    private fun notOnWifi(cellular: Boolean, vpn: Boolean, loc: Boolean, gps: Boolean): Map<String, Any?> = mapOf(
        "isConnected" to cellular,
        "isWifi" to false,
        "vpnActive" to vpn,
        "ssid" to "",
        "ssidAvailable" to false,
        "networkType" to if (cellular) "Data Seluler" else "Tidak Terhubung",
        "hasLocationPermission" to loc,
        "isGpsEnabled" to gps,
        "summary" to if (cellular) "HP tidak terhubung ke Wi-Fi (memakai data seluler)." else "HP tidak terhubung ke jaringan apa pun."
    )

    /** Picks a real Wi-Fi network even when a VPN is the default network. */
    private fun findWifiNetwork(cm: ConnectivityManager): Network? = cm.allNetworks.firstOrNull { n ->
        val c = cm.getNetworkCapabilities(n)
        c != null && c.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) && !c.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
    }

    private fun readSsid(context: Context, info: WifiInfo?): String {
        val raw = info?.ssid?.trim()?.trim('"') ?: ""
        if (raw.isNotEmpty() && raw != UNREADABLE_SSID) return raw
        return try {
            val k = com.kavsdk.wifi.WifiReputation(context.applicationContext).checkCurrentNetwork().ssid
                ?.trim()?.trim('"') ?: ""
            if (k == UNREADABLE_SSID) "" else k
        } catch (_: Throwable) { "" }
    }

    private class KavVerdict(val name: String, val safe: Boolean?)

    private fun kasperskyVerdict(context: Context): KavVerdict = try {
        val r = com.kavsdk.wifi.WifiReputation(context.applicationContext).checkCurrentNetwork()
        KavVerdict(r.verdict.name, r.verdict == com.kavsdk.wifi.Verdict.Safe)
    } catch (_: Throwable) { KavVerdict("Unavailable", null) }

    private class Security(val label: String, val known: Boolean, val isOpen: Boolean, val isWeak: Boolean)

    private fun describeSecurity(type: Int): Security {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return Security("Tidak dapat dibaca (Android < 12)", false, false, false)
        return when (type) {
            WifiInfo.SECURITY_TYPE_OPEN -> Security("Terbuka (tanpa enkripsi)", true, true, false)
            WifiInfo.SECURITY_TYPE_WEP -> Security("WEP (usang)", true, false, true)
            WifiInfo.SECURITY_TYPE_PSK -> Security("WPA/WPA2-Personal", true, false, false)
            WifiInfo.SECURITY_TYPE_SAE -> Security("WPA3-Personal", true, false, false)
            WifiInfo.SECURITY_TYPE_OWE -> Security("OWE (Enhanced Open)", true, false, false)
            WifiInfo.SECURITY_TYPE_EAP -> Security("WPA/WPA2-Enterprise", true, false, false)
            WifiInfo.SECURITY_TYPE_EAP_WPA3_ENTERPRISE_192_BIT -> Security("WPA3-Enterprise 192-bit", true, false, false)
            else -> Security("Tidak diketahui", false, false, false)
        }
    }

    private fun isLocationEnabled(context: Context): Boolean = try {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) lm.isLocationEnabled
        else lm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) ||
            lm.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)
    } catch (_: Throwable) { false }
}
