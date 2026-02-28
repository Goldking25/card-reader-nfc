package com.nfcpoc.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a captured NFC card.
 *
 * Stores all available data for every supported card technology.
 * Fields not applicable to a given card type will be null or empty lists.
 */
@Entity(tableName = "nfc_cards")
data class NfcCard(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Hex-encoded UID string (e.g., "04:A3:12:5B") */
    val uid: String,

    /** Raw UID byte array as a contiguous hex string (no separators) */
    val uidRaw: String,

    /** Detected card technology */
    val cardType: CardType,

    /** User-defined or auto-generated label */
    val label: String,

    /** Unix timestamp (ms) when this card was captured */
    val timestamp: Long = System.currentTimeMillis(),

    /** List of all android.nfc.tech.* class names present on this tag */
    val techList: List<String> = emptyList(),

    // ─── MIFARE Classic ──────────────────────────────────────────────────────

    /** Sector data; empty for non-MIFARE-Classic cards */
    val sectors: List<MifareSector> = emptyList(),

    /** Memory size in bytes (1024, 2048, 4096) */
    val mifareSize: Int = 0,

    /** MifareClassic.TYPE_CLASSIC / TYPE_PLUS / TYPE_PRO / TYPE_UNKNOWN */
    val mifareType: Int = -1,

    /** Count of sectors that were successfully authenticated */
    val authenticatedSectorCount: Int = 0,

    // ─── MIFARE Ultralight ───────────────────────────────────────────────────

    /** Page data; empty for non-Ultralight cards */
    val pages: List<UltralightPage> = emptyList(),

    /** MifareUltralight.TYPE_ULTRALIGHT / TYPE_ULTRALIGHT_C / TYPE_UNKNOWN */
    val ultralightType: Int = -1,

    // ─── ISO 14443-A Generic ─────────────────────────────────────────────────

    /** Historical bytes from ATR (hex), present on NfcA / IsoDep cards */
    val historicalBytes: String? = null,

    /** ATQA (Answer To Request A) as hex string (2 bytes) */
    val atqa: String? = null,

    /** SAK (Select Acknowledge) value */
    val sak: Int? = null,

    // ─── ISO 14443-B Generic ─────────────────────────────────────────────────

    /** Higher-layer response bytes (hex) from NFC-B */
    val hiLayerResponse: String? = null,

    /** Application data (4 bytes hex) from ATQB */
    val applicationData: String? = null,

    /** Protocol info (3 bytes hex) from ATQB */
    val protocolInfo: String? = null,

    // ─── NFC-F (FeliCa) ──────────────────────────────────────────────────────

    /** IDm — Manufacturer ID (8 bytes hex) */
    val idm: String? = null,

    /** PMm — Manufacturer Parameters (8 bytes hex) */
    val pmm: String? = null,

    /** System Code (2 bytes hex) */
    val systemCode: String? = null,

    // ─── ISO-DEP (Payment / Transit) ─────────────────────────────────────────

    /** Ordered list of APDU command/response exchanges captured during reading */
    val apduLog: List<ApduExchange> = emptyList(),

    /** AID selected during reading (hex), if applicable */
    val selectedAid: String? = null,

    /** Notes or parsing result (e.g., card scheme name, balance) */
    val notes: String = ""
)
