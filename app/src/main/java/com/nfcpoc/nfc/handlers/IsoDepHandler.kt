package com.nfcpoc.nfc.handlers

import android.nfc.tech.IsoDep
import com.nfcpoc.data.model.ApduExchange
import timber.log.Timber

/**
 * Handles ISO 14443-4 (IsoDep) cards — contactless payment and transit cards.
 *
 * Executes the EMV card reading flow:
 *   1. SELECT PPSE (Proximity Payment System Environment)
 *   2. Parse FCI to extract application AID
 *   3. SELECT application by AID
 *   4. GET PROCESSING OPTIONS (if PDOL present)
 *   5. READ RECORD for all tagged SFIs (1–3, records 1–10)
 *
 * All APDU exchanges are logged for replay via HCE.
 * Must be called from a background thread.
 */
class IsoDepHandler {

    data class ReadResult(
        val apduLog: List<ApduExchange>,
        val selectedAid: String?,
        val historicalBytes: String?,
        val notes: String
    )

    fun read(isoDep: IsoDep): ReadResult {
        val log = mutableListOf<ApduExchange>()
        val notes = StringBuilder()
        var selectedAid: String? = null

        isoDep.timeout = 5000 // 5-second APDU timeout

        // ── Step 1: SELECT PPSE ──────────────────────────────────────────────
        val ppseCmd = buildSelectByNameApdu(PPSE_AID_BYTES)
        val ppseResp = transceive(isoDep, ppseCmd, "SELECT PPSE", log)

        if (ppseResp == null || !isSuccess(ppseResp)) {
            notes.append("PPSE select failed or card not EMV compliant. ")
            // Still try reading as a generic IsoDep card
            readGenericIsoDep(isoDep, log, notes)
        } else {
            notes.append("PPSE OK. ")

            // ── Step 2: Parse PPSE response for AID ─────────────────────────
            val aid = extractAidFromPpseResponse(ppseResp)
            if (aid != null) {
                selectedAid = aid.toHexString()
                notes.append("AID: $selectedAid. ")

                // ── Step 3: SELECT application by AID ───────────────────────
                val selectCmd = buildSelectByNameApdu(aid)
                val selectResp = transceive(isoDep, selectCmd, "SELECT APP by AID", log)

                if (selectResp != null && isSuccess(selectResp)) {
                    notes.append("App selected. ")

                    // ── Step 4: GET PROCESSING OPTIONS ───────────────────────
                    val gpoCmd = buildGpoApdu(selectResp)
                    val gpoResp = transceive(isoDep, gpoCmd, "GET PROCESSING OPTIONS", log)

                    if (gpoResp != null && isSuccess(gpoResp)) {
                        notes.append("GPO OK. ")
                        // ── Step 5: READ RECORD ───────────────────────────
                        readAllRecords(isoDep, log, notes)
                    }
                }
            } else {
                notes.append("No AID in PPSE, attempting direct record read. ")
                readAllRecords(isoDep, log, notes)
            }
        }

        val histBytes = isoDep.historicalBytes?.toHexString()

        Timber.d("IsoDep read: ${log.size} APDU exchanges. Notes: $notes")

        return ReadResult(
            apduLog = log,
            selectedAid = selectedAid,
            historicalBytes = histBytes,
            notes = notes.toString().trim()
        )
    }

    // ───────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ───────────────────────────────────────────────────────────────────────────

    /** Transceive and record the exchange; returns null on I/O error. */
    private fun transceive(
        isoDep: IsoDep,
        command: ByteArray,
        description: String,
        log: MutableList<ApduExchange>
    ): ByteArray? {
        return try {
            val response = isoDep.transceive(command)
            log.add(
                ApduExchange(
                    command = command.toHexString(),
                    response = response.toHexString(),
                    description = description
                )
            )
            Timber.v("APDU [$description] CMD: ${command.toHexString()} → RSP: ${response.toHexString()}")
            response
        } catch (e: Exception) {
            Timber.e(e, "APDU error during $description")
            log.add(
                ApduExchange(
                    command = command.toHexString(),
                    response = "ERROR",
                    description = "$description [FAILED: ${e.message}]"
                )
            )
            null
        }
    }

    /** SELECT command built by name (AID). */
    private fun buildSelectByNameApdu(aid: ByteArray): ByteArray {
        return byteArrayOf(
            0x00.toByte(), // CLA
            0xA4.toByte(), // INS: SELECT
            0x04.toByte(), // P1: select by name
            0x00.toByte(), // P2: first/only match
            aid.size.toByte() // Lc
        ) + aid + byteArrayOf(0x00.toByte()) // Le
    }

    /** Build a minimal GET PROCESSING OPTIONS APDU with empty PDOL. */
    private fun buildGpoApdu(selectResponse: ByteArray): ByteArray {
        // Try to extract PDOL from FCI; fall back to empty PDOL
        val pdolData = extractPdol(selectResponse)
        return if (pdolData.isEmpty()) {
            byteArrayOf(0x80.toByte(), 0xA8.toByte(), 0x00.toByte(), 0x00.toByte(), 0x02.toByte(), 0x83.toByte(), 0x00.toByte(), 0x00.toByte())
        } else {
            byteArrayOf(0x80.toByte(), 0xA8.toByte(), 0x00.toByte(), 0x00.toByte()) +
                    byteArrayOf((pdolData.size + 2).toByte(), 0x83.toByte(), pdolData.size.toByte()) +
                    pdolData +
                    byteArrayOf(0x00.toByte())
        }
    }

    /** Reads records from SFI 1–3, record 1–10 to capture full card data. */
    private fun readAllRecords(isoDep: IsoDep, log: MutableList<ApduExchange>, notes: StringBuilder) {
        var recordsFound = 0
        for (sfi in 1..3) {
            for (record in 1..10) {
                val cmd = buildReadRecordApdu(sfi, record)
                val resp = transceive(isoDep, cmd, "READ RECORD SFI=$sfi REC=$record", log)
                if (resp == null || !isSuccess(resp)) {
                    break // No more records in this SFI
                }
                recordsFound++
            }
        }
        notes.append("$recordsFound records read. ")
    }

    /** Fallback read for non-EMV IsoDep cards. */
    private fun readGenericIsoDep(isoDep: IsoDep, log: MutableList<ApduExchange>, notes: StringBuilder) {
        // Try GET DATA for common tags
        val getDataCmds = mapOf(
            "GET DATA ATC" to byteArrayOf(0x80.toByte(), 0xCA.toByte(), 0x9F.toByte(), 0x36.toByte(), 0x00.toByte()),
            "GET DATA Last Online ATC" to byteArrayOf(0x80.toByte(), 0xCA.toByte(), 0x9F.toByte(), 0x13.toByte(), 0x00.toByte()),
            "GET DATA PIN Try Counter" to byteArrayOf(0x80.toByte(), 0xCA.toByte(), 0x9F.toByte(), 0x17.toByte(), 0x00.toByte())
        )
        for ((desc, cmd) in getDataCmds) {
            transceive(isoDep, cmd, desc, log)
        }
        notes.append("Generic IsoDep data attempted. ")
    }

    private fun buildReadRecordApdu(sfi: Int, record: Int): ByteArray {
        val p2 = ((sfi shl 3) or 0x04).toByte()
        return byteArrayOf(0x00.toByte(), 0xB2.toByte(), record.toByte(), p2, 0x00.toByte())
    }

    /** Check if the last two bytes of a response are 90 00 (success). */
    private fun isSuccess(response: ByteArray): Boolean {
        if (response.size < 2) return false
        val sw1 = response[response.size - 2].toInt() and 0xFF
        val sw2 = response[response.size - 1].toInt() and 0xFF
        return sw1 == 0x90 && sw2 == 0x00
    }

    /**
     * Parses the PPSE FCI response (TLV) to extract the first AID (tag 0x4F).
     * Simple BER-TLV parser — handles nested templates.
     */
    private fun extractAidFromPpseResponse(response: ByteArray): ByteArray? {
        return findTlvTag(response, 0x4F)
    }

    /** Extracts PDOL value from FCI template (tag 0x9F38). */
    private fun extractPdol(response: ByteArray): ByteArray {
        return findTlvTag(response, 0x9F38) ?: byteArrayOf()
    }

    /**
     * Minimal BER-TLV scanner: finds the first occurrence of [tag] in [data]
     * and returns its value bytes.  Handles 1-byte and 2-byte tags.
     */
    private fun findTlvTag(data: ByteArray, tag: Int): ByteArray? {
        var i = 0
        while (i < data.size - 1) {
            val currentTag = if ((data[i].toInt() and 0x1F) == 0x1F) {
                // Two-byte tag
                if (i + 1 >= data.size) break
                ((data[i].toInt() and 0xFF) shl 8) or (data[i + 1].toInt() and 0xFF)
            } else {
                data[i].toInt() and 0xFF
            }

            val tagBytes = if ((data[i].toInt() and 0x1F) == 0x1F) 2 else 1
            i += tagBytes

            if (i >= data.size) break
            val length = data[i].toInt() and 0xFF
            i++

            if (i + length > data.size) break

            if (currentTag == tag) {
                return data.copyOfRange(i, i + length)
            }

            // Recurse into constructed tags (bit 6 of first byte set)
            val firstTagByte = if (tagBytes == 2) (currentTag shr 8) and 0xFF else currentTag and 0xFF
            if ((firstTagByte and 0x20) != 0) {
                val nested = findTlvTag(data.copyOfRange(i, i + length), tag)
                if (nested != null) return nested
            }

            i += length
        }
        return null
    }

    private fun ByteArray.toHexString(): String =
        joinToString("") { "%02X".format(it) }

    companion object {
        /** PPSE AID: "2PAY.SYS.DDF01" as bytes */
        private val PPSE_AID_BYTES =
            "2PAY.SYS.DDF01".toByteArray(Charsets.US_ASCII)
    }
}
