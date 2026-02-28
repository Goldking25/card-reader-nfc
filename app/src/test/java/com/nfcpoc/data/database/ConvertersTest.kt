package com.nfcpoc.data.database

import com.google.common.truth.Truth.assertThat
import com.nfcpoc.data.model.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for Room TypeConverters.
 * Verifies that every type survives a serialize → deserialize round-trip
 * and that null/blank JSON inputs never throw.
 */
class ConvertersTest {

    private lateinit var converters: Converters

    @Before
    fun setUp() {
        converters = Converters()
    }

    // ── CardType ──────────────────────────────────────────────────────────────

    @Test
    fun `CardType round-trip preserves all enum values`() {
        CardType.entries.forEach { type ->
            val json = converters.fromCardType(type)
            val restored = converters.toCardType(json)
            assertThat(restored).isEqualTo(type)
        }
    }

    @Test
    fun `toCardType with null returns UNKNOWN`() {
        assertThat(converters.toCardType(null)).isEqualTo(CardType.UNKNOWN)
    }

    @Test
    fun `toCardType with blank string returns UNKNOWN`() {
        assertThat(converters.toCardType("   ")).isEqualTo(CardType.UNKNOWN)
    }

    @Test
    fun `toCardType with invalid name returns UNKNOWN`() {
        assertThat(converters.toCardType("NOT_A_CARD_TYPE")).isEqualTo(CardType.UNKNOWN)
    }

    // ── List<String> ─────────────────────────────────────────────────────────

    @Test
    fun `String list round-trip preserves order and content`() {
        val list = listOf("android.nfc.tech.NfcA", "android.nfc.tech.IsoDep")
        val json = converters.fromStringList(list)
        val restored = converters.toStringList(json)
        assertThat(restored).containsExactlyElementsIn(list).inOrder()
    }

    @Test
    fun `toStringList with null returns empty list`() {
        assertThat(converters.toStringList(null)).isEmpty()
    }

    @Test
    fun `fromStringList with null returns empty JSON array`() {
        assertThat(converters.fromStringList(null)).isEqualTo("[]")
    }

    @Test
    fun `String list round-trip with empty list`() {
        val json = converters.fromStringList(emptyList())
        assertThat(converters.toStringList(json)).isEmpty()
    }

    // ── List<MifareSector> ────────────────────────────────────────────────────

    @Test
    fun `MifareSector list round-trip preserves all fields`() {
        val sectors = listOf(
            MifareSector(
                index = 0,
                blocks = listOf("AABBCCDD00112233", "FFEEDDCC99887766"),
                authenticated = true,
                keyType = "A",
                keyUsed = "FFFFFFFFFFFF"
            ),
            MifareSector(index = 1, blocks = emptyList(), authenticated = false)
        )
        val json = converters.fromMifareSectors(sectors)
        val restored = converters.toMifareSectors(json)
        assertThat(restored).hasSize(2)
        assertThat(restored[0].index).isEqualTo(0)
        assertThat(restored[0].authenticated).isTrue()
        assertThat(restored[0].keyUsed).isEqualTo("FFFFFFFFFFFF")
        assertThat(restored[1].authenticated).isFalse()
    }

    @Test
    fun `toMifareSectors with null returns empty list`() {
        assertThat(converters.toMifareSectors(null)).isEmpty()
    }

    // ── List<UltralightPage> ─────────────────────────────────────────────────

    @Test
    fun `UltralightPage list round-trip preserves page data`() {
        val pages = listOf(
            UltralightPage(index = 0, data = "04A3B2C1"),
            UltralightPage(index = 1, data = "00000000"),
            UltralightPage(index = 2, data = "FFFFFFFF")
        )
        val json = converters.fromUltralightPages(pages)
        val restored = converters.toUltralightPages(json)
        assertThat(restored).hasSize(3)
        assertThat(restored[0].data).isEqualTo("04A3B2C1")
        assertThat(restored[2].data).isEqualTo("FFFFFFFF")
    }

    @Test
    fun `toUltralightPages with null returns empty list`() {
        assertThat(converters.toUltralightPages(null)).isEmpty()
    }

    // ── List<ApduExchange> ───────────────────────────────────────────────────

    @Test
    fun `ApduExchange list round-trip preserves command and response`() {
        val log = listOf(
            ApduExchange(
                command = "00A404000E325041592E5359532E444446303100",
                response = "6F238407A0000000041010A5189F0A08000002510000000000BF0C05A5035001019000",
                description = "SELECT PPSE"
            ),
            ApduExchange(
                command = "00A4040007A00000000410100000",
                response = "9000",
                description = "SELECT AID"
            )
        )
        val json = converters.fromApduLog(log)
        val restored = converters.toApduLog(json)
        assertThat(restored).hasSize(2)
        assertThat(restored[0].description).isEqualTo("SELECT PPSE")
        assertThat(restored[1].response).isEqualTo("9000")
    }

    @Test
    fun `toApduLog with null returns empty list`() {
        assertThat(converters.toApduLog(null)).isEmpty()
    }

    @Test
    fun `toApduLog with blank string returns empty list`() {
        assertThat(converters.toApduLog("")).isEmpty()
    }
}
