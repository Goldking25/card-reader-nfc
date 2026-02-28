package com.nfcpoc.nfc

import android.nfc.Tag
import android.nfc.tech.*
import com.nfcpoc.data.model.*
import com.nfcpoc.nfc.handlers.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

/**
 * Central NFC tag dispatcher.
 *
 * Given a raw [Tag], this manager:
 *  1. Inspects the tag's tech list to determine the best handler.
 *  2. Delegates to the appropriate typed handler.
 *  3. Assembles and returns a fully populated [NfcCard] model.
 *
 * Must be called from a background thread (Dispatchers.IO).
 */
class NfcReaderManager {

    private val mifareClassicHandler = MifareClassicHandler()
    private val mifareUltralightHandler = MifareUltralightHandler()
    private val isoDepHandler = IsoDepHandler()
    private val nfcBHandler = NfcBHandler()
    private val nfcFHandler = NfcFHandler()

    /**
     * Read the full contents of [tag] and return a populated [NfcCard].
     * Throws [NfcReadException] if the read completely fails.
     */
    @Throws(NfcReadException::class)
    fun readTag(tag: Tag): NfcCard {
        val techList = tag.techList.toList()
        val uid = tag.id
        val uidHex = uid.toColonHex()
        val uidRaw = uid.toHexString()

        Timber.i("Tag detected — UID: $uidHex | Techs: ${techList.joinToString()}")

        val autoLabel = "Card-${SimpleDateFormat("HHmmss", Locale.US).format(Date())}"

        return when {
            // MIFARE Classic has highest priority — check before generic NfcA
            techList.contains(MifareClassic::class.java.name) ->
                readMifareClassic(tag, uid, uidHex, uidRaw, techList, autoLabel)

            // MIFARE Ultralight — also NfcA underneath
            techList.contains(MifareUltralight::class.java.name) ->
                readMifareUltralight(tag, uid, uidHex, uidRaw, techList, autoLabel)

            // ISO 14443-4 (payment / transit)
            techList.contains(IsoDep::class.java.name) -> {
                // Could be Type A or Type B underneath
                val isTypeB = techList.contains(NfcB::class.java.name)
                readIsoDep(tag, uid, uidHex, uidRaw, techList, autoLabel, isTypeB)
            }

            // NFC-B without IsoDep
            techList.contains(NfcB::class.java.name) ->
                readNfcB(tag, uid, uidHex, uidRaw, techList, autoLabel)

            // NFC-F (FeliCa)
            techList.contains(NfcF::class.java.name) ->
                readNfcF(tag, uid, uidHex, uidRaw, techList, autoLabel)

            // Generic NFC-A (no higher-level tech detected)
            techList.contains(NfcA::class.java.name) ->
                readGenericNfcA(tag, uid, uidHex, uidRaw, techList, autoLabel)

            else -> {
                Timber.w("No known tech handler for: $techList")
                NfcCard(
                    uid = uidHex,
                    uidRaw = uidRaw,
                    cardType = CardType.UNKNOWN,
                    label = autoLabel,
                    techList = techList,
                    notes = "Unsupported card type. Tech list: ${techList.joinToString()}"
                )
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Per-technology read methods
    // ─────────────────────────────────────────────────────────────────────────

    private fun readMifareClassic(
        tag: Tag, uid: ByteArray, uidHex: String, uidRaw: String,
        techList: List<String>, label: String
    ): NfcCard {
        val mifare = MifareClassic.get(tag)
            ?: throw NfcReadException("MifareClassic.get() returned null")
        return try {
            mifare.connect()
            val result = mifareClassicHandler.read(mifare)
            val nfcAAttr = runCatching { NfcA.get(tag)?.also { it.connect() } }.getOrNull()
            val atqa = nfcAAttr?.atqa?.toHexString()
            val sak = nfcAAttr?.sak?.toInt()
            runCatching { nfcAAttr?.close() }

            NfcCard(
                uid = uidHex,
                uidRaw = uidRaw,
                cardType = CardType.MIFARE_CLASSIC,
                label = label,
                techList = techList,
                sectors = result.sectors,
                mifareSize = result.memorySize,
                mifareType = result.mifareType,
                authenticatedSectorCount = result.authenticatedCount,
                atqa = atqa,
                sak = sak,
                notes = "${result.authenticatedCount}/${result.totalSectors} sectors read"
            )
        } finally {
            runCatching { mifare.close() }
        }
    }

    private fun readMifareUltralight(
        tag: Tag, uid: ByteArray, uidHex: String, uidRaw: String,
        techList: List<String>, label: String
    ): NfcCard {
        val ultralight = MifareUltralight.get(tag)
            ?: throw NfcReadException("MifareUltralight.get() returned null")
        return try {
            ultralight.connect()
            val result = mifareUltralightHandler.read(ultralight)
            val nfcA = runCatching { NfcA.get(tag)?.also { it.connect() } }.getOrNull()
            val atqa = nfcA?.atqa?.toHexString()
            val sak = nfcA?.sak?.toInt()
            runCatching { nfcA?.close() }

            NfcCard(
                uid = uidHex,
                uidRaw = uidRaw,
                cardType = CardType.MIFARE_ULTRALIGHT,
                label = label,
                techList = techList,
                pages = result.pages,
                ultralightType = result.ultralightType,
                atqa = atqa,
                sak = sak,
                notes = "${result.pages.count { !it.data.contains('?') }}/${result.pages.size} pages read"
            )
        } finally {
            runCatching { ultralight.close() }
        }
    }

    private fun readIsoDep(
        tag: Tag, uid: ByteArray, uidHex: String, uidRaw: String,
        techList: List<String>, label: String, isTypeB: Boolean
    ): NfcCard {
        val isoDep = IsoDep.get(tag)
            ?: throw NfcReadException("IsoDep.get() returned null")
        return try {
            isoDep.connect()
            val result = isoDepHandler.read(isoDep)

            if (isTypeB) {
                val nfcB = runCatching { NfcB.get(tag)?.also { it.connect() } }.getOrNull()
                val bResult = nfcB?.let {
                    val r = nfcBHandler.read(it)
                    it.close()
                    r
                }
                NfcCard(
                    uid = uidHex,
                    uidRaw = uidRaw,
                    cardType = CardType.ISO_DEP_B,
                    label = label,
                    techList = techList,
                    apduLog = result.apduLog + (bResult?.apduLog ?: emptyList()),
                    selectedAid = result.selectedAid,
                    historicalBytes = result.historicalBytes,
                    applicationData = bResult?.applicationData,
                    protocolInfo = bResult?.protocolInfo,
                    hiLayerResponse = bResult?.hiLayerResponse,
                    notes = result.notes
                )
            } else {
                val nfcA = runCatching { NfcA.get(tag)?.also { it.connect() } }.getOrNull()
                val atqa = nfcA?.atqa?.toHexString()
                val sak = nfcA?.sak?.toInt()
                runCatching { nfcA?.close() }

                NfcCard(
                    uid = uidHex,
                    uidRaw = uidRaw,
                    cardType = CardType.ISO_DEP_A,
                    label = label,
                    techList = techList,
                    apduLog = result.apduLog,
                    selectedAid = result.selectedAid,
                    historicalBytes = result.historicalBytes,
                    atqa = atqa,
                    sak = sak,
                    notes = result.notes
                )
            }
        } finally {
            runCatching { isoDep.close() }
        }
    }

    private fun readNfcB(
        tag: Tag, uid: ByteArray, uidHex: String, uidRaw: String,
        techList: List<String>, label: String
    ): NfcCard {
        val nfcB = NfcB.get(tag)
            ?: throw NfcReadException("NfcB.get() returned null")
        return try {
            nfcB.connect()
            val result = nfcBHandler.read(nfcB)
            NfcCard(
                uid = uidHex,
                uidRaw = uidRaw,
                cardType = CardType.ISO_DEP_B,
                label = label,
                techList = techList,
                applicationData = result.applicationData,
                protocolInfo = result.protocolInfo,
                hiLayerResponse = result.hiLayerResponse,
                apduLog = result.apduLog,
                notes = "NFC-B card"
            )
        } finally {
            runCatching { nfcB.close() }
        }
    }

    private fun readNfcF(
        tag: Tag, uid: ByteArray, uidHex: String, uidRaw: String,
        techList: List<String>, label: String
    ): NfcCard {
        val nfcF = NfcF.get(tag)
            ?: throw NfcReadException("NfcF.get() returned null")
        return try {
            nfcF.connect()
            val result = nfcFHandler.read(nfcF)
            NfcCard(
                uid = uidHex,
                uidRaw = uidRaw,
                cardType = CardType.NFC_F,
                label = label,
                techList = techList,
                idm = result.idm,
                pmm = result.pmm,
                systemCode = result.systemCode,
                apduLog = result.apduLog,
                notes = "FeliCa card — IDm: ${result.idm}"
            )
        } finally {
            runCatching { nfcF.close() }
        }
    }

    private fun readGenericNfcA(
        tag: Tag, uid: ByteArray, uidHex: String, uidRaw: String,
        techList: List<String>, label: String
    ): NfcCard {
        val nfcA = NfcA.get(tag) ?: throw NfcReadException("NfcA.get() returned null")
        return try {
            nfcA.connect()
            val atqa = nfcA.atqa.toHexString()
            val sak = nfcA.sak.toInt()
            NfcCard(
                uid = uidHex,
                uidRaw = uidRaw,
                cardType = CardType.UNKNOWN,
                label = label,
                techList = techList,
                atqa = atqa,
                sak = sak,
                notes = "Generic NFC-A — ATQA: $atqa, SAK: $sak"
            )
        } finally {
            runCatching { nfcA.close() }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Extensions
    // ─────────────────────────────────────────────────────────────────────────

    private fun ByteArray.toHexString(): String =
        joinToString("") { "%02X".format(it) }

    private fun ByteArray.toColonHex(): String =
        joinToString(":") { "%02X".format(it) }
}

/** Thrown when NFC tag reading fails at the dispatch level. */
class NfcReadException(message: String, cause: Throwable? = null) : Exception(message, cause)
