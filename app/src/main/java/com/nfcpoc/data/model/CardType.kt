package com.nfcpoc.data.model

/**
 * Represents the detected NFC card technology type.
 * Each value maps to a specific NFC standard / card family.
 */
enum class CardType(val displayName: String) {
    MIFARE_CLASSIC("MIFARE Classic"),
    MIFARE_ULTRALIGHT("MIFARE Ultralight"),
    ISO_DEP_A("ISO 14443-4A"),
    ISO_DEP_B("ISO 14443-4B"),
    NFC_F("NFC-F (FeliCa)"),
    NFC_V("NFC-V (ISO 15693)"),
    UNKNOWN("Unknown")
}
