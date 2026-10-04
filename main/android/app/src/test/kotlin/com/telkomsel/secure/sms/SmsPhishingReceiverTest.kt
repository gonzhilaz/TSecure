package com.telkomsel.secure.sms

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Enterprise Unit Test Suite for SmsPhishingReceiver detection engine.
 * Tests Smishing (APK malware, social engineering, fake bank) and Judi Online / Slot Gacor SMS detection.
 */
class SmsPhishingReceiverTest {

    @Test
    fun testDetectJudiOnlineSlotGacor() {
        val sms = "PROMO TERGACOR! Nikmati MAXWIN malam ini di Pragmatic Olympus scatter hitam. Depo pulsa tanpa potongan klik https://slot-zeus.xyz"
        val eval = SmsPhishingReceiver.evaluateSmsText(sms)

        assertTrue("Harus terdeteksi sebagai ancaman", eval.isThreat)
        assertEquals("Tipe ancaman harus JUDI_ONLINE", "JUDI_ONLINE", eval.threatType)
        assertTrue("Harus mengandung alasan promosi judi online", eval.reason.contains("Judi Online"))
        assertTrue("URL target harus ter-ekstraksi", eval.targetUrl.contains("slot-zeus.xyz"))
    }

    @Test
    fun testDetectJudiOnlineFreebet() {
        val sms = "Khusus Member Baru! Klaim FREEBET 50K modal receh garansi kekalahan 100% langsung wd tanpa ribet: bit.ly/slotgacor88"
        val eval = SmsPhishingReceiver.evaluateSmsText(sms)

        assertTrue("Harus terdeteksi sebagai ancaman", eval.isThreat)
        assertEquals("Tipe ancaman harus JUDI_ONLINE", "JUDI_ONLINE", eval.threatType)
        assertEquals("bit.ly/slotgacor88", eval.targetUrl)
    }

    @Test
    fun testDetectSmishingSuratTilangApk() {
        val sms = "KEPOLISIAN RI: Kendaraan B 1234 CD melanggar batas kecepatan ETLE. Buka surat_tilang_elektronik.apk untuk detail berkas perkara."
        val eval = SmsPhishingReceiver.evaluateSmsText(sms)

        assertTrue("Harus terdeteksi sebagai ancaman", eval.isThreat)
        assertEquals("Tipe ancaman harus SMISHING", "SMISHING", eval.threatType)
    }

    @Test
    fun testDetectSmishingUndanganPernikahanApk() {
        val sms = "Halo sahabat, kami mengundang kehadiranmu pada acara pernikahan kami: Undangan_Pernikahan_Digital_2026.apk"
        val eval = SmsPhishingReceiver.evaluateSmsText(sms)

        assertTrue("Harus terdeteksi sebagai ancaman", eval.isThreat)
        assertEquals("Tipe ancaman harus SMISHING", "SMISHING", eval.threatType)
    }

    @Test
    fun testDetectGenericApkDownload() {
        val sms = "Silakan perbarui aplikasi mobile anda melalui https://updater.s-id.xyz/update.apk segera"
        val eval = SmsPhishingReceiver.evaluateSmsText(sms)

        assertTrue("Harus terdeteksi sebagai ancaman", eval.isThreat)
        assertEquals("Tipe ancaman harus SMISHING", "SMISHING", eval.threatType)
    }

    @Test
    fun testLegitimateSmsTelkomsel() {
        val sms = "Paket Internet Sakti 25GB telah aktif berlaku s.d 04/11/2026. Cek kuota di MyTelkomsel."
        val eval = SmsPhishingReceiver.evaluateSmsText(sms)

        assertFalse("SMS resmi Telkomsel tidak boleh terdeteksi sebagai ancaman", eval.isThreat)
    }

    @Test
    fun testLegitimateOtpBanking() {
        val sms = "JANGAN BERIKAN KEPADA SIAPAPUN! Kode verifikasi Anda adalah 839210. Berlaku selama 5 menit."
        val eval = SmsPhishingReceiver.evaluateSmsText(sms)

        assertFalse("SMS OTP biasa tanpa link/apk/judi tidak boleh terdeteksi sebagai ancaman", eval.isThreat)
    }
}
