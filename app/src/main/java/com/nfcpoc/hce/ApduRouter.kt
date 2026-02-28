package com.nfcpoc.hce

import com.nfcpoc.data.model.ApduExchange
import com.nfcpoc.data.model.NfcCard
import timber.log.Timber

/**
 * Routes incoming APDU commands to the appropriate stored response.
 *
 * Strategy:
 *  1. Match by exact command prefix (CLA+INS+P1+P2).
 *  2. If no exact match, match by CLA+INS only.
 *  3. If still no match, return 6A82 (File Not Found).
 *
 * The router uses the ordered [ApduExchange] log captured during card reading
 * along with a simple state machine to respond correctly to multi-step sequences.
 */
class ApduRouter(private val card: NfcCard) {

    /** Index into the apduLog for sequential state-machine mode. */
    private var sequenceIndex = 0

    /** The AID that was most recently SELECTed. */
    private var selectedAid: String? = null

    /**
     * Process an incoming command APDU and return the response bytes.
     * Never returns null — always returns at least a status word.
     */
    fun processApdu(commandApdu: ByteArray): ByteArray {
        val cmdHex = commandApdu.toHexString()
        Timber.d("HCE → incoming APDU: $cmdHex")

        // Sanity check minimum APDU length (CLA INS P1 P2)
        if (commandApdu.size < 4) {
            Timber.w("APDU too short: $cmdHex")
            return SW_WRONG_LENGTH
        }

        val cla = commandApdu[0].toInt() and 0xFF
        val ins = commandApdu[1].toInt() and 0xFF

        return when {
            // SELECT by name (AID)
            isSelectByName(commandApdu) -> handleSelectByName(commandApdu)

            // READ RECORD
            ins == 0xB2 -> handleReadRecord(commandApdu)

            // GET PROCESSING OPTIONS
            ins == 0xA8 -> handleGpo(commandApdu)

            // GENERATE AC (for EMV online auth — we return a canned response)
            ins == 0xAE -> handleGenerateAc(commandApdu)

            // GET DATA
            ins == 0xCA -> handleGetData(commandApdu)

            // Fallback: search entire APDU log for a matching command
            else -> lookupInLog(cmdHex)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Command handlers
    // ─────────────────────────────────────────────────────────────────────────

    private fun handleSelectByName(apdu: ByteArray): ByteArray {
        if (apdu.size < 6) return SW_WRONG_LENGTH

        // Extract AID from Lc + data
        val lc = apdu[4].toInt() and 0xFF
        if (apdu.size < 5 + lc) return SW_WRONG_LENGTH

        val aidBytes = apdu.copyOfRange(5, 5 + lc)
        val aidHex = aidBytes.toHexString()

        // Match against stored AID; also check if it's the PPSE AID
        val isPpse = aidHex == "325041592E5359532E4444463031"
        val matchesCard = card.selectedAid?.equals(aidHex, ignoreCase = true) == true

        return if (isPpse || matchesCard) {
            selectedAid = aidHex
            sequenceIndex = 0 // Reset sequence on new SELECT
            Timber.i("HCE: SELECT matched AID $aidHex → searching log for response")

            // 1st choice: exchange whose command contains THIS specific AID (exact match)
            val specificExchange = card.apduLog.firstOrNull {
                it.command.startsWith("00A404", ignoreCase = true) &&
                        it.command.contains(aidHex, ignoreCase = true) &&
                        it.response.length >= 4 &&
                        !it.response.equals("ERROR", ignoreCase = true)
            }
            // 2nd choice: fall back to the first SELECT exchange in the log
            val selectExchange = specificExchange ?: card.apduLog.firstOrNull {
                it.command.startsWith("00A404", ignoreCase = true) &&
                        it.response.length >= 4 &&
                        !it.response.equals("ERROR", ignoreCase = true)
            }
            selectExchange?.responseBytes() ?: (buildFciResponse(aidBytes) + SW_OK)
        } else {
            Timber.w("HCE: No match for AID $aidHex")
            SW_FILE_NOT_FOUND
        }
    }

    private fun handleReadRecord(apdu: ByteArray): ByteArray {
        val record = apdu[2].toInt() and 0xFF
        val p2 = apdu[3].toInt() and 0xFF
        val sfi = (p2 shr 3) and 0x1F

        val tag = "READ RECORD SFI=$sfi REC=$record"
        val exchange = card.apduLog.firstOrNull {
            it.description.equals(tag, ignoreCase = true) &&
                    !it.response.equals("ERROR", ignoreCase = true)
        }

        return if (exchange != null) {
            Timber.d("HCE: READ RECORD $tag → ${exchange.response}")
            exchange.responseBytes()
        } else {
            // Try sequential match
            val seqExchange = card.apduLog.getOrNull(sequenceIndex++)
            if (seqExchange != null && seqExchange.command.startsWith("00B2", ignoreCase = true)) {
                seqExchange.responseBytes()
            } else {
                SW_FILE_NOT_FOUND
            }
        }
    }

    private fun handleGpo(apdu: ByteArray): ByteArray {
        val exchange = card.apduLog.firstOrNull {
            it.description.contains("GPO", ignoreCase = true) ||
                    it.description.contains("PROCESSING OPTIONS", ignoreCase = true) &&
                    !it.response.equals("ERROR", ignoreCase = true)
        }
        return exchange?.responseBytes() ?: (byteArrayOf(
            0x80.toByte(), 0x0E.toByte(),
            0x60.toByte(), 0x00.toByte(),
            0x08.toByte(), 0x01.toByte(), 0x01.toByte(), 0x00.toByte(),
            0x10.toByte(), 0x01.toByte(), 0x03.toByte(), 0x00.toByte(),
            0x18.toByte(), 0x01.toByte(), 0x02.toByte(), 0x00.toByte()
        ) + SW_OK)
    }

    private fun handleGenerateAc(apdu: ByteArray): ByteArray {
        // Return a canned TC (offline approved) response
        // In real replay this would fail at the acquirer — demonstrates the attempt
        val cannedTc = byteArrayOf(
            0x80.toByte(), 0x1A.toByte(),
            0x40.toByte(), // Cryptogram info: TC
            0x00.toByte(), // ATC high
            0x01.toByte(), // ATC low
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), // AC (8 bytes)
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte() // IAD
        )
        return cannedTc + SW_OK
    }

    private fun handleGetData(apdu: ByteArray): ByteArray {
        val p1 = apdu[2].toInt() and 0xFF
        val p2 = apdu[3].toInt() and 0xFF
        val tagHex = "%02X%02X".format(p1, p2)

        val exchange = card.apduLog.firstOrNull {
            it.command.contains(tagHex, ignoreCase = true) &&
                    it.description.contains("GET DATA", ignoreCase = true) &&
                    !it.response.equals("ERROR", ignoreCase = true)
        }
        return exchange?.responseBytes() ?: SW_FILE_NOT_FOUND
    }

    /** Last-resort lookup in the full APDU log by command prefix match. */
    private fun lookupInLog(cmdHex: String): ByteArray {
        // Try 8-char match (CLA INS P1 P2)
        val prefix8 = cmdHex.take(8)
        val exchange = card.apduLog.firstOrNull {
            it.command.startsWith(prefix8, ignoreCase = true) &&
                    !it.response.equals("ERROR", ignoreCase = true)
        }
        if (exchange != null) {
            Timber.d("HCE: log lookup match for prefix $prefix8")
            return exchange.responseBytes()
        }

        // Try 4-char match (CLA INS)
        val prefix4 = cmdHex.take(4)
        val exchange4 = card.apduLog.firstOrNull {
            it.command.startsWith(prefix4, ignoreCase = true) &&
                    !it.response.equals("ERROR", ignoreCase = true)
        }
        if (exchange4 != null) {
            Timber.d("HCE: log fallback match for prefix $prefix4")
            return exchange4.responseBytes()
        }

        Timber.w("HCE: No response found for APDU $cmdHex")
        return SW_FILE_NOT_FOUND
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private fun isSelectByName(apdu: ByteArray): Boolean =
        apdu.size >= 4 &&
                (apdu[0].toInt() and 0xFF) == 0x00 &&
                (apdu[1].toInt() and 0xFF) == 0xA4 &&
                (apdu[2].toInt() and 0xFF) == 0x04

    /** Builds a minimal FCI template if no stored SELECT response is available. */
    private fun buildFciResponse(aid: ByteArray): ByteArray {
        val fciTemplate = byteArrayOf(0x6F.toByte()) +
                byteArrayOf((4 + aid.size).toByte()) +
                byteArrayOf(0x84.toByte(), aid.size.toByte()) + aid +
                byteArrayOf(0xA5.toByte(), 0x00.toByte())
        return fciTemplate
    }

    private fun ApduExchange.responseBytes(): ByteArray =
        response.chunked(2).map { it.toInt(16).toByte() }.toByteArray()

    private fun ByteArray.toHexString(): String =
        joinToString("") { "%02X".format(it) }

    companion object {
        val SW_OK = byteArrayOf(0x90.toByte(), 0x00.toByte())
        val SW_FILE_NOT_FOUND = byteArrayOf(0x6A.toByte(), 0x82.toByte())
        val SW_WRONG_LENGTH = byteArrayOf(0x67.toByte(), 0x00.toByte())
        val SW_COMMAND_NOT_ALLOWED = byteArrayOf(0x69.toByte(), 0x86.toByte())
        val SW_UNKNOWN = byteArrayOf(0x6F.toByte(), 0x00.toByte())
    }
}
