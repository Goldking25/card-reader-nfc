package com.nfcpoc.hce

import com.google.common.truth.Truth.assertThat
import com.nfcpoc.data.model.CardType
import com.nfcpoc.data.model.NfcCard
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [EmulationSessionManager].
 * Verifies load, clear, and state transitions.
 */
class EmulationSessionManagerTest {

    private val testCard = NfcCard(
        uid = "04:DE:AD:BE",
        uidRaw = "04DEADBE",
        cardType = CardType.ISO_DEP_A,
        label = "Test VISA"
    )

    @Before
    fun setUp() {
        // Always start each test with a clean session
        EmulationSessionManager.stopEmulation()
    }

    @After
    fun tearDown() {
        EmulationSessionManager.stopEmulation()
    }

    @Test
    fun `initially no active card`() {
        assertThat(EmulationSessionManager.getActiveCard()).isNull()
    }

    @Test
    fun `initially not emulating`() {
        assertThat(EmulationSessionManager.isEmulating()).isFalse()
    }

    @Test
    fun `loadCard sets active card`() {
        EmulationSessionManager.loadCard(testCard)
        assertThat(EmulationSessionManager.getActiveCard()).isEqualTo(testCard)
    }

    @Test
    fun `startEmulation sets isEmulating true`() {
        EmulationSessionManager.loadCard(testCard)
        EmulationSessionManager.startEmulation(testCard)
        assertThat(EmulationSessionManager.isEmulating()).isTrue()
    }

    @Test
    fun `stopEmulation sets isEmulating false`() {
        EmulationSessionManager.startEmulation(testCard)
        EmulationSessionManager.stopEmulation()
        assertThat(EmulationSessionManager.isEmulating()).isFalse()
    }

    @Test
    fun `stopEmulation clears active card`() {
        EmulationSessionManager.loadCard(testCard)
        EmulationSessionManager.stopEmulation()
        assertThat(EmulationSessionManager.getActiveCard()).isNull()
    }

    @Test
    fun `loading a second card replaces first`() {
        val card2 = testCard.copy(uid = "AA:BB:CC:DD", label = "Card 2")
        EmulationSessionManager.loadCard(testCard)
        EmulationSessionManager.loadCard(card2)
        assertThat(EmulationSessionManager.getActiveCard()?.uid).isEqualTo("AA:BB:CC:DD")
    }
}
