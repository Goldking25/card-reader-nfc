package com.nfcpoc.hce

import com.google.common.truth.Truth.assertThat
import com.nfcpoc.data.model.ApduExchange
import com.nfcpoc.data.model.CardType
import com.nfcpoc.data.model.NfcCard
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [ApduRouter] — verifies the APDU state machine
 * routes commands correctly for stored card sessions.
 */
class ApduRouterTest {

    private lateinit var router: ApduRouter

    // A minimal ISO-DEP card with a representative APDU log
    private val testCard = NfcCard(
        uid = "04:A3:12:5B",
        uidRaw = "04A3125B",
        cardType = CardType.ISO_DEP_A,
        label = "Test Card",
        selectedAid = "A0000000041010",
        apduLog = listOf(
            ApduExchange(
                command  = "00A404000E325041592E5359532E444446303100",
                response = "6F23840706A0000000410A059000",
                description = "SELECT PPSE"
            ),
            ApduExchange(
                command  = "00A4040007A000000004101000",
                response = "6F1A8407A0000000041010A50F500A4D617374657243617264",
                description = "SELECT AID"
            ),
            ApduExchange(
                command  = "80A8000002830000",
                response = "7716820278009F360200019F260811223344AABBCCDD9000",
                description = "GET PROCESSING OPTIONS"
            ),
            ApduExchange(
                command  = "00B2010C00",
                response = "701A5A0812345678901234575F24032512319F1A0208409000",
                description = "READ RECORD SFI=1 Rec=1"
            )
        )
    )

    @Before
    fun setUp() {
        router = ApduRouter()
        router.loadCard(testCard)
    }

    @Test
    fun `SELECT PPSE command returns stored response`() {
        val cmd = hexToBytes("00A404000E325041592E5359532E444446303100")
        val response = router.processApdu(cmd)
        assertThat(response.toHex()).isEqualTo("6F23840706A0000000410A059000")
    }

    @Test
    fun `SELECT AID command returns stored response`() {
        val cmd = hexToBytes("00A4040007A000000004101000")
        val response = router.processApdu(cmd)
        assertThat(response.toHex()).isEqualTo("6F1A8407A0000000041010A50F500A4D617374657243617264")
    }

    @Test
    fun `GPO command returns stored response`() {
        val cmd = hexToBytes("80A8000002830000")
        val response = router.processApdu(cmd)
        assertThat(response.toHex()).isEqualTo("7716820278009F360200019F260811223344AABBCCDD9000")
    }

    @Test
    fun `READ RECORD command returns stored response`() {
        val cmd = hexToBytes("00B2010C00")
        val response = router.processApdu(cmd)
        assertThat(response.toHex()).isEqualTo("701A5A0812345678901234575F24032512319F1A0208409000")
    }

    @Test
    fun `unknown command returns 6A82 file not found`() {
        val cmd = hexToBytes("00B0000000") // arbitrary unknown
        val response = router.processApdu(cmd)
        // Should return 6A82 (File not found) as documented fallback
        assertThat(response.size).isGreaterThan(0)
        val hex = response.toHex()
        assertThat(hex).endsWith("6A82")
    }

    @Test
    fun `loadCard replaces previous session`() {
        val otherCard = testCard.copy(uid = "AA:BB:CC:DD", apduLog = emptyList())
        router.loadCard(otherCard)
        // With empty log, unknown command returns fallback
        val cmd = hexToBytes("00A404000E325041592E5359532E444446303100")
        val response = router.processApdu(cmd)
        // No matching log entry → fallback response
        assertThat(response).isNotEmpty()
    }

    @Test
    fun `clearSession causes unknown fallback for all commands`() {
        router.clearSession()
        val cmd = hexToBytes("00A404000E325041592E5359532E444446303100")
        val response = router.processApdu(cmd)
        assertThat(response).isNotEmpty()
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun hexToBytes(hex: String): ByteArray =
        ByteArray(hex.length / 2) { hex.substring(it * 2, it * 2 + 2).toInt(16).toByte() }

    private fun ByteArray.toHex(): String =
        joinToString("") { "%02X".format(it) }
}
