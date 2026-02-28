package com.nfcpoc.data.database

import androidx.room.*
import com.nfcpoc.data.model.NfcCard
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for NFC card persistence.
 * All write operations are suspend functions; reads expose reactive Flows.
 */
@Dao
interface CardDao {

    /** Insert a new card. Returns the generated row ID. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: NfcCard): Long

    /** Retrieve all stored cards, ordered by capture time (newest first). */
    @Query("SELECT * FROM nfc_cards ORDER BY timestamp DESC")
    fun getAllCards(): Flow<List<NfcCard>>

    /** Retrieve a specific card by its primary key. */
    @Query("SELECT * FROM nfc_cards WHERE id = :id")
    suspend fun getCardById(id: Long): NfcCard?

    /** Update an existing card (e.g., after relabelling). */
    @Update
    suspend fun updateCard(card: NfcCard)

    /** Delete a single card. */
    @Delete
    suspend fun deleteCard(card: NfcCard)

    /** Delete all stored cards. */
    @Query("DELETE FROM nfc_cards")
    suspend fun deleteAllCards()

    /** Count total stored cards. */
    @Query("SELECT COUNT(*) FROM nfc_cards")
    suspend fun getCardCount(): Int

    /** Search cards by label (case-insensitive partial match). */
    @Query("SELECT * FROM nfc_cards WHERE label LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchCards(query: String): Flow<List<NfcCard>>
}
