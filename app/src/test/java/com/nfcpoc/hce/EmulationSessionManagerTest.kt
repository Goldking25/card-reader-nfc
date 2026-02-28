package com.nfcpoc.hce

import com.google.common.truth.Truth.assertThat
import com.nfcpoc.data.model.CardType
import com.nfcpoc.data.model.NfcCard
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [EmulationSessionManager].
 *
 * NOTE: Timber calls inside EmulationSessionManager are safe in JVM unit tests
 * because with no tree planted, all Timber.i/w/d calls are silent no-ops.
 * DO NOT plant Timber.DebugTree() — it delegates to android.util.Log which
 * is not available in the pure JVM test environment.
 */
class EmulationSessionManagerTest {

    private val testCard = NfcCard(
        uid      = "04:DE:AD:BE",
        uidRaw   = "04DEADBE",
        cardType = CardType.ISO_DEP_A,
        label    = "Test VISA"
    )

    @Before
    fun setUp() {
        EmulationSessionManager.clearSession()
    }

    @After
    fun tearDown() {
        EmulationSessionManager.clearSession()
    }

    @Test
    fun `initially no active card`() {
        assertThat(EmulationSessionManager.activeCard).isNull()
    }

    @Test
    fun `initially not emulating`() {
        assertThat(EmulationSessionManager.isEmulating).isFalse()
    }

    @Test
    fun `loadCard sets active card`() {
        EmulationSessionManager.loadCard(testCard)
        assertThat(EmulationSessionManager.activeCard).isEqualTo(testCard)
    }

    @Test
    fun `loadCard sets isEmulating to true`() {
        EmulationSessionManager.loadCard(testCard)
        assertThat(EmulationSessionManager.isEmulating).isTrue()
    }

    @Test
    fun `clearSession sets isEmulating to false`() {
        EmulationSessionManager.loadCard(testCard)
        EmulationSessionManager.clearSession()
        assertThat(EmulationSessionManager.isEmulating).isFalse()
    }

    @Test
    fun `clearSession nulls active card`() {
        EmulationSessionManager.loadCard(testCard)
        EmulationSessionManager.clearSession()
        assertThat(EmulationSessionManager.activeCard).isNull()
    }

    @Test
    fun `loading second card replaces first`() {
        val card2 = testCard.copy(uid = "AA:BB:CC:DD", label = "Card 2")
        EmulationSessionManager.loadCard(testCard)
        EmulationSessionManager.loadCard(card2)
        assertThat(EmulationSessionManager.activeCard?.uid).isEqualTo("AA:BB:CC:DD")
    }

    @Test
    fun `routeApdu without session returns SW_COMMAND_NOT_ALLOWED`() {
        // No card loaded — should return 69 86
        val response = EmulationSessionManager.routeApdu(byteArrayOf(0x00, 0xA4.toByte(), 0x04, 0x00))
        assertThat(response.toHex()).isEqualTo("6986")
    }

    @Test
    fun `routeApdu with loaded card returns non-empty response`() {
        EmulationSessionManager.loadCard(testCard)
        val response = EmulationSessionManager.routeApdu(byteArrayOf(0x00, 0xA4.toByte(), 0x04, 0x00))
        assertThat(response).isNotEmpty()
    }

    private fun ByteArray.toHex() = joinToString("") { "%02X".format(it) }
}
