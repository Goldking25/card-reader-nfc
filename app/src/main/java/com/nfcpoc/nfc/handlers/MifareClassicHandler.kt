package com.nfcpoc.nfc.handlers

import android.nfc.tech.MifareClassic
import com.nfcpoc.data.model.MifareSector
import com.nfcpoc.nfc.KeyDictionary
import timber.log.Timber

/**
 * Handles reading of MIFARE Classic 1K / 2K / 4K cards.
 *
 * Strategy:
 * 1. For each sector, attempt authentication with Key A then Key B
 *    using every key in [KeyDictionary.ALL_KEYS].
 * 2. On success, read all blocks in the sector.
 * 3. Sectors that cannot be authenticated are recorded with [authenticated = false].
 *
 * Must be called from a background thread (I/O operations on NFC tag).
 */
class MifareClassicHandler {

    data class ReadResult(
        val sectors: List<MifareSector>,
        val mifareType: Int,
        val memorySize: Int,
        val authenticatedCount: Int,
        val totalSectors: Int
    )

    /**
     * Connect to and fully read the tag. Caller is responsible for
     * calling [MifareClassic.close] after this returns.
     */
    fun read(mifare: MifareClassic): ReadResult {
        val sectors = mutableListOf<MifareSector>()
        val sectorCount = mifare.sectorCount

        for (sectorIndex in 0 until sectorCount) {
            val (authenticated, key, keyType) = tryAuthenticate(mifare, sectorIndex)

            val blocks = if (authenticated) {
                readSectorBlocks(mifare, sectorIndex)
            } else {
                emptyList()
            }

            sectors.add(
                MifareSector(
                    index = sectorIndex,
                    authenticated = authenticated,
                    keyUsed = key?.let { KeyDictionary.keyToHex(it) },
                    keyType = keyType,
                    blocks = blocks
                )
            )
        }

        val authCount = sectors.count { it.authenticated }
        Timber.d("MIFARE Classic read: $authCount/$sectorCount sectors authenticated")

        return ReadResult(
            sectors = sectors,
            mifareType = mifare.type,
            memorySize = mifare.size,
            authenticatedCount = authCount,
            totalSectors = sectorCount
        )
    }

    /**
     * Tries every key in the dictionary for Key A, then Key B.
     * Returns a Triple(success, keyBytes, "A"/"B").
     */
    private fun tryAuthenticate(
        mifare: MifareClassic,
        sector: Int
    ): Triple<Boolean, ByteArray?, String?> {
        for (key in KeyDictionary.ALL_KEYS) {
            try {
                if (mifare.authenticateSectorWithKeyA(sector, key)) {
                    Timber.v("Sector $sector authenticated with Key A: ${KeyDictionary.keyToHex(key)}")
                    return Triple(true, key, "A")
                }
            } catch (e: Exception) {
                // Continue trying other keys
            }
            try {
                if (mifare.authenticateSectorWithKeyB(sector, key)) {
                    Timber.v("Sector $sector authenticated with Key B: ${KeyDictionary.keyToHex(key)}")
                    return Triple(true, key, "B")
                }
            } catch (e: Exception) {
                // Continue
            }
        }
        Timber.w("Sector $sector: all keys failed")
        return Triple(false, null, null)
    }

    /**
     * Reads all blocks in a sector and returns them as hex strings.
     * Assumes the sector is already authenticated.
     */
    private fun readSectorBlocks(mifare: MifareClassic, sector: Int): List<String> {
        val blocks = mutableListOf<String>()
        val blockCount = mifare.getBlockCountInSector(sector)
        val firstBlock = mifare.sectorToBlock(sector)

        for (blockOffset in 0 until blockCount) {
            try {
                val blockData = mifare.readBlock(firstBlock + blockOffset)
                blocks.add(blockData.toHexString())
            } catch (e: Exception) {
                Timber.e(e, "Failed to read block ${firstBlock + blockOffset}")
                blocks.add("????????????????????????????????") // 16 bytes unknown
            }
        }
        return blocks
    }

    private fun ByteArray.toHexString(): String =
        joinToString("") { "%02X".format(it) }
}
