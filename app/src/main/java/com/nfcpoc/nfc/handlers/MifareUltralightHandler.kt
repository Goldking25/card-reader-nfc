package com.nfcpoc.nfc.handlers

import android.nfc.tech.MifareUltralight
import com.nfcpoc.data.model.UltralightPage
import timber.log.Timber

/**
 * Handles reading of MIFARE Ultralight and Ultralight C cards.
 *
 * Reads all available pages (Ultralight: 16, Ultralight C: 48).
 * Pages 0–3 contain manufacturer data, serial number, and lock bytes.
 * Pages 4–N contain user memory and OTP bytes.
 *
 * Must be called from a background thread.
 */
class MifareUltralightHandler {

    data class ReadResult(
        val pages: List<UltralightPage>,
        val ultralightType: Int
    )

    /**
     * Connect to and read all pages from a MIFARE Ultralight tag.
     * Caller is responsible for calling [MifareUltralight.close].
     */
    fun read(ultralight: MifareUltralight): ReadResult {
        // Determine expected page count from type
        val pageCount = when (ultralight.type) {
            MifareUltralight.TYPE_ULTRALIGHT -> 16
            MifareUltralight.TYPE_ULTRALIGHT_C -> 48
            else -> 16 // conservative default
        }

        val pages = mutableListOf<UltralightPage>()
        var pageIndex = 0

        // Read 4 pages at a time (readPages returns 4 pages = 16 bytes)
        while (pageIndex < pageCount) {
            try {
                val rawData = ultralight.readPages(pageIndex)
                // readPages returns 16 bytes = 4 pages
                for (offset in 0..3) {
                    val currentPage = pageIndex + offset
                    if (currentPage >= pageCount) break
                    val pageBytes = rawData.copyOfRange(offset * 4, offset * 4 + 4)
                    pages.add(UltralightPage(index = currentPage, data = pageBytes.toHexString()))
                }
                pageIndex += 4
            } catch (e: Exception) {
                Timber.w(e, "Failed to read Ultralight page $pageIndex, adding blanks")
                // Add placeholder pages and advance
                for (offset in 0..3) {
                    val currentPage = pageIndex + offset
                    if (currentPage >= pageCount) break
                    pages.add(UltralightPage(index = currentPage, data = "????????"))
                }
                pageIndex += 4
            }
        }

        Timber.d("Ultralight read: ${pages.count { !it.data.contains('?') }}/${pages.size} pages read")
        return ReadResult(pages = pages, ultralightType = ultralight.type)
    }

    private fun ByteArray.toHexString(): String =
        joinToString("") { "%02X".format(it) }
}
