package com.nfcpoc.nfc.handlers

import android.nfc.tech.NfcB
import com.nfcpoc.data.model.ApduExchange
import timber.log.Timber

/**
 * Handles ISO 14443-3B (NFC-B) cards.
 *
 * Reads the ATQB response fields and attempts an IsoDep connection
 * for higher-layer protocol data.
 *
 * Must be called from a background thread.
 */
class NfcBHandler {

    data class ReadResult(
        val applicationData: String,   // 4 bytes hex from ATQB
        val protocolInfo: String,      // 3 bytes hex from ATQB
        val hiLayerResponse: String?,  // derived from protocol info byte 2 (FO field)
        val apduLog: List<ApduExchange>
    )

    fun read(nfcB: NfcB): ReadResult {
        val log = mutableListOf<ApduExchange>()

        // ATQB fields exposed by the Android NFC stack
        val appData = nfcB.applicationData?.toHexString() ?: ""
        val protInfo = nfcB.protocolInfo?.toHexString() ?: ""

        // NfcB does NOT expose hiLayerResponse directly in the Android API.
        // Protocol info byte 2 (FO field): bit 0 set means higher-layer response supported.
        val hiLayer: String? = nfcB.protocolInfo
            ?.takeIf { it.size >= 3 && (it[2].toInt() and 0x01) != 0 }
            ?.let { "HLR supported (FO=0x%02X)".format(it[2].toInt() and 0xFF) }

        Timber.d("NFC-B: AppData=$appData, ProtInfo=$protInfo, HiLayer=$hiLayer")

        // Attempt ATTRIB to enter ISO 14443-4 layer
        try {
            val attribCmd = buildAttrib(nfcB.tag.id)
            val attrib = nfcB.transceive(attribCmd)
            log.add(ApduExchange(
                command = attribCmd.toHexString(),
                response = attrib.toHexString(),
                description = "ATTRIB"
            ))
        } catch (e: Exception) {
            Timber.w(e, "ATTRIB failed — card may not support ISO 14443-4")
        }

        return ReadResult(
            applicationData = appData,
            protocolInfo = protInfo,
            hiLayerResponse = hiLayer,
            apduLog = log
        )
    }


    /** Simple ATTRIB command for entering ISO 14443-4 (Type B). */
    private fun buildAttrib(uid: ByteArray): ByteArray {
        // ATTRIB = 1D + UID(4 bytes) + Param1 + Param2 + Param3 + Param4
        return byteArrayOf(0x1D.toByte()) +
                uid.copyOfRange(0, minOf(4, uid.size)) +
                byteArrayOf(0x00.toByte(), 0x08.toByte(), 0x01.toByte(), 0x00.toByte())
    }

    private fun ByteArray.toHexString(): String =
        joinToString("") { "%02X".format(it) }
}
