package com.nfcpoc.hce

import com.google.common.truth.Truth.assertThat
import com.nfcpoc.data.model.ApduExchange
import com.nfcpoc.data.model.CardType
import com.nfcpoc.data.model.NfcCard
import org.junit.Test

/**
 * Unit tests for [ApduRouter].
 * ApduRouter takes the card in its constructor — one instance per session.
 */
class ApduRouterTest {

    // A minimal ISO-DEP card with a representative APDU log
    private val testCard = NfcCard(
        uid = "04:A3:12:5B",
        uidRaw = "04A3125B",
        cardType = CardType.ISO_DEP_A,
        label = "Test Card",
        selectedAid = "A0000000041010",
        apduLog = listOf(
            ApduExchange(
                command     = "00A404000E325041592E5359532E444446303100",
                response    = "6F23840706A0000000410A059000",
                description = "SELECT PPSE"
            ),
            ApduExchange(
                command     = "00A4040007A000000004101000",
                response    = "6F1A8407A0000000041010A50F500A4D617374657243617264",
                description = "SELECT AID"
            ),
            ApduExchange(
                command     = "80A8000002830000",
                response    = "7716820278009F360200019F260811223344AABBCCDD9000",
                description = "GET PROCESSING OPTIONS"
            ),
            ApduExchange(
                command     = "00B2010C00",
                response    = "701A5A0812345678901234575F24032512319F1A0208409000",
                description = "READ RECORD SFI=1 REC=1"
            )
        )
    )

    private fun router(card: NfcCard = testCard) = ApduRouter(card)

    @Test
    fun `SELECT PPSE command returns stored response`() {
        val r = router()
        val response = r.processApdu("00A404000E325041592E5359532E444446303100".hexToBytes())
        assertThat(response.toHex()).isEqualTo("6F23840706A0000000410A059000")
    }

    @Test
    fun `SELECT AID command returns stored response`() {
        val r = router()
        val response = r.processApdu("00A4040007A000000004101000".hexToBytes())
        assertThat(response.toHex()).isEqualTo("6F1A8407A0000000041010A50F500A4D617374657243617264")
    }

    @Test
    fun `GPO command returns stored response`() {
        val r = router()
        val response = r.processApdu("80A8000002830000".hexToBytes())
        assertThat(response.toHex()).isEqualTo("7716820278009F360200019F260811223344AABBCCDD9000")
    }

    @Test
    fun `READ RECORD command returns stored response`() {
        val r = router()
        val response = r.processApdu("00B2010C00".hexToBytes())
        assertThat(response.toHex()).isEqualTo("701A5A0812345678901234575F24032512319F1A0208409000")
    }

    @Test
    fun `unknown command returns SW_FILE_NOT_FOUND 6A82`() {
        val emptyCard = testCard.copy(apduLog = emptyList())
        val r = router(emptyCard)
        val response = r.processApdu("00B0000000".hexToBytes())
        assertThat(response.toHex()).endsWith("6A82")
    }

    @Test
    fun `APDU shorter than 4 bytes returns SW_WRONG_LENGTH 6700`() {
        val r = router()
        val response = r.processApdu(byteArrayOf(0x00))
        assertThat(response.toHex()).isEqualTo("6700")
    }

    @Test
    fun `card with empty APDU log returns fallback for all commands`() {
        val empty = testCard.copy(apduLog = emptyList())
        val r = router(empty)
        val response = r.processApdu("00B2010C00".hexToBytes())
        assertThat(response).isNotEmpty()
        assertThat(response.toHex()).endsWith("6A82")
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun String.hexToBytes(): ByteArray =
        ByteArray(length / 2) { substring(it * 2, it * 2 + 2).toInt(16).toByte() }

    private fun ByteArray.toHex(): String =
        joinToString("") { "%02X".format(it) }
}
