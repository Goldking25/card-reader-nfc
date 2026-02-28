package com.nfcpoc.nfc

/**
 * Dictionary of well-known MIFARE Classic authentication keys.
 *
 * These keys are commonly used as factory defaults by card manufacturers
 * and transit / access-control system vendors. The handler will attempt
 * each key for both Key A and Key B on every sector before giving up.
 *
 * Reference sources: libnfc, mfcuk, Proxmark3 community key lists.
 */
object KeyDictionary {

    /**
     * Returns all default keys as byte arrays ready to pass to
     * [android.nfc.tech.MifareClassic.authenticateSectorWithKeyA] /
     * [android.nfc.tech.MifareClassic.authenticateSectorWithKeyB].
     */
    val ALL_KEYS: List<ByteArray> = listOf(
        // ── Universal factory defaults ──────────────────────────────────────
        byteArrayOf(0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte()),
        byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00),
        byteArrayOf(0xA0.toByte(), 0xA1.toByte(), 0xA2.toByte(), 0xA3.toByte(), 0xA4.toByte(), 0xA5.toByte()),
        byteArrayOf(0xB0.toByte(), 0xB1.toByte(), 0xB2.toByte(), 0xB3.toByte(), 0xB4.toByte(), 0xB5.toByte()),
        byteArrayOf(0xD3.toByte(), 0xF7.toByte(), 0xD3.toByte(), 0xF7.toByte(), 0xD3.toByte(), 0xF7.toByte()),

        // ── Common transit / access control keys ────────────────────────────
        byteArrayOf(0x4D.toByte(), 0x3A.toByte(), 0x99.toByte(), 0xC3.toByte(), 0x51.toByte(), 0xDD.toByte()),
        byteArrayOf(0x1A.toByte(), 0x98.toByte(), 0x2C.toByte(), 0x7E.toByte(), 0x45.toByte(), 0x9A.toByte()),
        byteArrayOf(0xAA.toByte(), 0xBB.toByte(), 0xCC.toByte(), 0xDD.toByte(), 0xEE.toByte(), 0xFF.toByte()),
        byteArrayOf(0x71.toByte(), 0x4C.toByte(), 0x5C.toByte(), 0x88.toByte(), 0x6E.toByte(), 0x97.toByte()),
        byteArrayOf(0x58.toByte(), 0x7E.toByte(), 0xE5.toByte(), 0xF9.toByte(), 0x35.toByte(), 0x0F.toByte()),
        byteArrayOf(0xA0.toByte(), 0x47.toByte(), 0x8C.toByte(), 0xC3.toByte(), 0x90.toByte(), 0x91.toByte()),
        byteArrayOf(0x53.toByte(), 0x3C.toByte(), 0xB6.toByte(), 0xC7.toByte(), 0x23.toByte(), 0xF6.toByte()),
        byteArrayOf(0x8F.toByte(), 0xD0.toByte(), 0xA4.toByte(), 0xF2.toByte(), 0x56.toByte(), 0xE9.toByte()),
        byteArrayOf(0x6C.toByte(), 0x78.toByte(), 0x92.toByte(), 0x8E.toByte(), 0x13.toByte(), 0x00.toByte()),
        byteArrayOf(0xAB.toByte(), 0xCD.toByte(), 0xEF.toByte(), 0x12.toByte(), 0x34.toByte(), 0x56.toByte()),

        // ── NXP application keys ─────────────────────────────────────────────
        byteArrayOf(0xFC.toByte(), 0x00.toByte(), 0x01.toByte(), 0x8E.toByte(), 0xE9.toByte(), 0x05.toByte()),
        byteArrayOf(0x2A.toByte(), 0x2C.toByte(), 0x13.toByte(), 0xCC.toByte(), 0x37.toByte(), 0x71.toByte()),

        // ── OV-Chipkaart (Dutch transit) ─────────────────────────────────────
        byteArrayOf(0x09.toByte(), 0x00.toByte(), 0x09.toByte(), 0x00.toByte(), 0x09.toByte(), 0x00.toByte()),

        // ── Gallagher / HID ──────────────────────────────────────────────────
        byteArrayOf(0xA0.toByte(), 0xA1.toByte(), 0xA2.toByte(), 0xA3.toByte(), 0xA4.toByte(), 0xA5.toByte()),

        // ── Infineon default ─────────────────────────────────────────────────
        byteArrayOf(0x49.toByte(), 0xFA.toByte(), 0xEC.toByte(), 0x0F.toByte(), 0xD5.toByte(), 0xA2.toByte())
    )

    /** Returns a human-readable hex string for a key byte array. */
    fun keyToHex(key: ByteArray): String =
        key.joinToString("") { "%02X".format(it) }
}
