package com.nfcpoc.data.model

/**
 * Represents a single MIFARE Classic sector with authentication state and block data.
 *
 * @param index       Sector number (0-based)
 * @param authenticated Whether authentication succeeded for this sector
 * @param keyUsed     The hex string of the key that was successfully used (null if auth failed)
 * @param keyType     "A" or "B" — which key type succeeded
 * @param blocks      List of block data, each as a 16-byte hex string (32 chars)
 */
data class MifareSector(
    val index: Int,
    val authenticated: Boolean,
    val keyUsed: String?,
    val keyType: String?,
    val blocks: List<String>
)

/**
 * Represents a single MIFARE Ultralight page (4 bytes).
 *
 * @param index Page number (0-based)
 * @param data  4 bytes of page data as an 8-character hex string
 */
data class UltralightPage(
    val index: Int,
    val data: String
)

/**
 * Represents a single APDU command/response exchange captured during card reading.
 *
 * @param command     The command APDU bytes as hex string
 * @param response    The response APDU bytes as hex string (includes SW1SW2)
 * @param description Human-readable label for this exchange
 */
data class ApduExchange(
    val command: String,
    val response: String,
    val description: String = ""
)
