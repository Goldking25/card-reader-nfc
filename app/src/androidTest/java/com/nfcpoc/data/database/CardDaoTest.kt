package com.nfcpoc.data.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.nfcpoc.data.model.CardType
import com.nfcpoc.data.model.NfcCard
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented integration test for [CardDao] using an in-memory Room database.
 * Must run on an Android device or emulator (uses real SQLite + TypeConverters).
 */
@RunWith(AndroidJUnit4::class)
class CardDaoTest {

    private lateinit var db: CardDatabase
    private lateinit var dao: CardDao

    @Before
    fun setUp() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(ctx, CardDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.cardDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun makeCard(label: String, type: CardType = CardType.ISO_DEP_A): NfcCard =
        NfcCard(uid = "04:AA:BB:CC", uidRaw = "04AABBCC", cardType = type, label = label)

    @Test
    fun insertAndGetById() = runTest {
        val id = dao.insertCard(makeCard("My Visa"))
        val card = dao.getCardById(id)
        assertThat(card).isNotNull()
        assertThat(card!!.label).isEqualTo("My Visa")
        assertThat(card.cardType).isEqualTo(CardType.ISO_DEP_A)
    }

    @Test
    fun getAllCards_emitsInsertedCard() = runTest {
        dao.insertCard(makeCard("Card A"))
        dao.getAllCards().test {
            val list = awaitItem()
            assertThat(list).hasSize(1)
            assertThat(list.first().label).isEqualTo("Card A")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAllCards_orderedNewestFirst() = runTest {
        dao.insertCard(makeCard("Old Card").copy(timestamp = 1_000L))
        dao.insertCard(makeCard("New Card").copy(timestamp = 2_000L))
        dao.getAllCards().test {
            val list = awaitItem()
            assertThat(list[0].label).isEqualTo("New Card")
            assertThat(list[1].label).isEqualTo("Old Card")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteCard_removesIt() = runTest {
        val id = dao.insertCard(makeCard("To Delete"))
        val card = dao.getCardById(id)!!
        dao.deleteCard(card)
        assertThat(dao.getCardById(id)).isNull()
    }

    @Test
    fun deleteAllCards_emptiesTable() = runTest {
        dao.insertCard(makeCard("Card 1"))
        dao.insertCard(makeCard("Card 2"))
        dao.deleteAllCards()
        assertThat(dao.getCardCount()).isEqualTo(0)
    }

    @Test
    fun updateCard_changesLabel() = runTest {
        val id = dao.insertCard(makeCard("Original Label"))
        val card = dao.getCardById(id)!!
        dao.updateCard(card.copy(label = "Updated Label"))
        val updated = dao.getCardById(id)!!
        assertThat(updated.label).isEqualTo("Updated Label")
    }

    @Test
    fun searchCards_findsPartialMatch() = runTest {
        dao.insertCard(makeCard("My Visa Card"))
        dao.insertCard(makeCard("Mastercard Transit"))
        dao.searchCards("visa").test {
            val results = awaitItem()
            assertThat(results).hasSize(1)
            assertThat(results.first().label).isEqualTo("My Visa Card")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun searchCards_isCaseInsensitive() = runTest {
        dao.insertCard(makeCard("VISA DEBIT"))
        dao.searchCards("visa").test {
            val results = awaitItem()
            assertThat(results).hasSize(1)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun typeConverters_surviveSqliteRoundTrip() = runTest {
        val card = makeCard("Converter Test", CardType.MIFARE_CLASSIC).copy(
            techList = listOf("android.nfc.tech.NfcA", "android.nfc.tech.MifareClassic"),
            notes = "Test notes"
        )
        val id = dao.insertCard(card)
        val restored = dao.getCardById(id)!!
        assertThat(restored.techList).containsExactly(
            "android.nfc.tech.NfcA", "android.nfc.tech.MifareClassic"
        )
        assertThat(restored.cardType).isEqualTo(CardType.MIFARE_CLASSIC)
    }
}
