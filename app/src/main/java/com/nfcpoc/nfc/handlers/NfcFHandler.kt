package com.nfcpoc.nfc.handlers

import android.nfc.tech.NfcF
import com.nfcpoc.data.model.ApduExchange
import timber.log.Timber

/**
 * Handles NFC-F (FeliCa / JIS 6319-4) cards.
 *
 * FeliCa cards are common in Japanese and some Asian transit systems (Suica, Pasmo, Octopus).
 * This handler reads IDm, PMm, and System Code, then attempts a generic
 * REQUEST SERVICE command to enumerate available nodes.
 *
 * Must be called from a background thread.
 */
class NfcFHandler {

    data class ReadResult(
        val idm: String,         // Manufacturer ID (8 bytes hex)
        val pmm: String,         // Manufacturer Parameters (8 bytes hex)
        val systemCode: String,  // System Code (2 bytes hex)
        val apduLog: List<ApduExchange>
    )

    fun read(nfcF: NfcF): ReadResult {
        val log = mutableListOf<ApduExchange>()

        val idm = nfcF.manufacturer?.toHexString() ?: ""
        val systemCode = nfcF.systemCode?.toHexString() ?: ""

        Timber.d("NFC-F: IDm=$idm, SystemCode=$systemCode")

        // Build REQUEST RESPONSE command (code 04) to check if card is alive
        val requestResponse = buildRequestResponse(nfcF.manufacturer ?: byteArrayOf())
        try {
            val response = nfcF.transceive(requestResponse)
            log.add(ApduExchange(
                command = requestResponse.toHexString(),
                response = response.toHexString(),
                description = "REQUEST RESPONSE"
            ))
            Timber.v("NFC-F REQUEST RESPONSE: ${response.toHexString()}")
        } catch (e: Exception) {
            Timber.w(e, "NFC-F REQUEST RESPONSE failed")
        }

        // Read without encryption from common service codes (transit balance etc.)
        val serviceNodes = listOf(
            0x090F,  // Common transit: balance service
            0x0B0D,  // Transit service variant
            0x880B   // Some transit log service
        )
        for (serviceCode in serviceNodes) {
            val readCmd = buildReadWithoutEncryption(
                idmBytes = nfcF.manufacturer ?: byteArrayOf(),
                serviceCode = serviceCode,
                blockList = listOf(0x8000) // block 0
            )
            try {
                val resp = nfcF.transceive(readCmd)
                log.add(ApduExchange(
                    command = readCmd.toHexString(),
                    response = resp.toHexString(),
                    description = "READ WITHOUT ENCRYPTION service=0x${"%04X".format(serviceCode)}"
                ))
            } catch (e: Exception) {
                Timber.v("Service 0x${"%04X".format(serviceCode)} not available or encrypted")
            }
        }

        // PMm is not directly exposed by Android NfcF API; parse from manufacturer bytes if available
        val pmm = if ((nfcF.manufacturer?.size ?: 0) >= 8)
            nfcF.manufacturer!!.copyOfRange(0, 8).toHexString()
        else ""

        return ReadResult(idm = idm, pmm = pmm, systemCode = systemCode, apduLog = log)
    }

    /** FeliCa REQUEST RESPONSE command (response code 05). */
    private fun buildRequestResponse(idm: ByteArray): ByteArray {
        val paddedIdm = if (idm.size >= 8) idm.copyOfRange(0, 8) else idm + ByteArray(8 - idm.size)
        val cmd = byteArrayOf(0x00.toByte(), 0x04.toByte()) + paddedIdm
        val length = (cmd.size + 1).toByte()
        return byteArrayOf(length) + cmd
    }

    /** FeliCa READ WITHOUT ENCRYPTION command (response code 07). */
    private fun buildReadWithoutEncryption(
        idmBytes: ByteArray,
        serviceCode: Int,
        blockList: List<Int>
    ): ByteArray {
        val paddedIdm = if (idmBytes.size >= 8) idmBytes.copyOfRange(0, 8) else idmBytes + ByteArray(8 - idmBytes.size)
        val sc = byteArrayOf(
            (serviceCode and 0xFF).toByte(),
            ((serviceCode shr 8) and 0xFF).toByte()
        )
        val blocks = blockList.flatMap { block ->
            listOf(((block shr 8) and 0xFF).toByte(), (block and 0xFF).toByte())
        }.toByteArray()

        val payload = byteArrayOf(0x06.toByte()) + // Command code
                paddedIdm +
                byteArrayOf(0x01.toByte()) + sc + // 1 service
                byteArrayOf(blockList.size.toByte()) + blocks
        val length = (payload.size + 1).toByte()
        return byteArrayOf(length) + payload
    }

    private fun ByteArray.toHexString(): String =
        joinToString("") { "%02X".format(it) }
}
