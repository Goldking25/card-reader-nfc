package com.nfcpoc.data.repository

import com.nfcpoc.data.database.CardDao
import com.nfcpoc.data.model.NfcCard
import kotlinx.coroutines.flow.Flow

/**
 * Repository mediating between the UI / ViewModel layer and the Room DAO.
 *
 * All public functions suspend (write ops) or return Flows (read ops).
 * No threading logic here — callers are responsible for dispatching
 * to the appropriate coroutine scope / dispatcher.
 */
class CardRepository(private val dao: CardDao) {

    /** Live stream of all stored cards (newest first). */
    val allCards: Flow<List<NfcCard>> = dao.getAllCards()

    /**
     * Persist a newly scanned card.
     * @return The generated primary key for the inserted card.
     */
    suspend fun saveCard(card: NfcCard): Long = dao.insertCard(card)

    /** Retrieve a card by its primary key, or null if not found. */
    suspend fun getCardById(id: Long): NfcCard? = dao.getCardById(id)

    /** Persist a user-edited label or notes update. */
    suspend fun updateCard(card: NfcCard) = dao.updateCard(card)

    /** Remove a card from the database. */
    suspend fun deleteCard(card: NfcCard) = dao.deleteCard(card)

    /** Remove all stored cards. */
    suspend fun deleteAllCards() = dao.deleteAllCards()

    /** Reactive search by label. */
    fun searchCards(query: String): Flow<List<NfcCard>> = dao.searchCards(query)

    companion object {
        @Volatile
        private var INSTANCE: CardRepository? = null

        fun getInstance(dao: CardDao): CardRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CardRepository(dao).also { INSTANCE = it }
            }
        }
    }
}
