package com.nfcpoc.ui.cards

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.nfcpoc.R
import com.nfcpoc.data.database.CardDatabase
import com.nfcpoc.data.model.*
import com.nfcpoc.data.repository.CardRepository
import com.nfcpoc.databinding.ActivityCardDetailBinding
import com.nfcpoc.hce.EmulationSessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

/**
 * Full-detail view of a stored NFC card dump.
 * Shows all fields, the raw hex dump, and APDU log.
 * Provides "Load for Replay" which arms [EmulationSessionManager].
 */
class CardDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCardDetailBinding
    private var currentCard: NfcCard? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val cardId = intent.getLongExtra(EXTRA_CARD_ID, -1L)
        if (cardId == -1L) {
            finish()
            return
        }

        loadCard(cardId)

        binding.btnLoadForReplay.setOnClickListener { loadForReplay() }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_card_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { finish(); true }
            R.id.action_export -> { exportCard(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────

    private fun loadCard(cardId: Long) {
        lifecycleScope.launch {
            val repo = CardRepository.getInstance(
                CardDatabase.getInstance(applicationContext).cardDao()
            )
            val card = withContext(Dispatchers.IO) { repo.getCardById(cardId) }
            if (card == null) {
                Snackbar.make(binding.root, "Card not found", Snackbar.LENGTH_LONG).show()
                finish()
                return@launch
            }
            currentCard = card
            populateUi(card)
        }
    }

    private fun populateUi(card: NfcCard) {
        supportActionBar?.title = card.label

        // Header
        binding.tvDetailType.text = card.cardType.displayName
        binding.tvDetailUid.text = card.uid
        binding.tvDetailTimestamp.text =
            SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault()).format(Date(card.timestamp))
        binding.tvDetailNotes.text = card.notes.ifBlank { "—" }
        binding.tvDetailTechs.text = card.techList.joinToString("\n") { it.substringAfterLast('.') }

        // Type-specific data
        binding.tvDetailExtra.text = buildExtraSection(card)

        // Hex dump
        binding.tvHexDump.text = buildHexDump(card)

        // APDU log
        binding.tvApduLog.text = buildApduLog(card)

        // Only show replay button for ISO-DEP cards (HCE compatible)
        binding.btnLoadForReplay.isEnabled = card.cardType in listOf(
            CardType.ISO_DEP_A, CardType.ISO_DEP_B
        )
    }

    private fun buildExtraSection(card: NfcCard): String {
        return buildString {
            when (card.cardType) {
                CardType.MIFARE_CLASSIC -> {
                    appendLine("Memory Size: ${card.mifareSize / 1024}K")
                    appendLine("Sectors: ${card.sectors.size} total, ${card.authenticatedSectorCount} authenticated")
                    appendLine("ATQA: ${card.atqa ?: "N/A"}")
                    appendLine("SAK: ${card.sak?.let { "0x%02X".format(it) } ?: "N/A"}")
                }
                CardType.MIFARE_ULTRALIGHT -> {
                    appendLine("Pages: ${card.pages.size}")
                    appendLine("ATQA: ${card.atqa ?: "N/A"}")
                    appendLine("SAK: ${card.sak?.let { "0x%02X".format(it) } ?: "N/A"}")
                }
                CardType.ISO_DEP_A -> {
                    appendLine("Selected AID: ${card.selectedAid ?: "N/A"}")
                    appendLine("Historical Bytes: ${card.historicalBytes ?: "N/A"}")
                    appendLine("ATQA: ${card.atqa ?: "N/A"}")
                    appendLine("SAK: ${card.sak?.let { "0x%02X".format(it) } ?: "N/A"}")
                }
                CardType.ISO_DEP_B -> {
                    appendLine("Selected AID: ${card.selectedAid ?: "N/A"}")
                    appendLine("Application Data: ${card.applicationData ?: "N/A"}")
                    appendLine("Protocol Info: ${card.protocolInfo ?: "N/A"}")
                    appendLine("Hi-Layer Response: ${card.hiLayerResponse ?: "N/A"}")
                }
                CardType.NFC_F -> {
                    appendLine("IDm: ${card.idm ?: "N/A"}")
                    appendLine("PMm: ${card.pmm ?: "N/A"}")
                    appendLine("System Code: ${card.systemCode ?: "N/A"}")
                }
                else -> appendLine("No extra data available.")
            }
        }.trimEnd()
    }

    private fun buildHexDump(card: NfcCard): String {
        val sb = StringBuilder()
        when (card.cardType) {
            CardType.MIFARE_CLASSIC -> {
                for (sector in card.sectors) {
                    sb.appendLine("── Sector ${sector.index} " +
                            (if (sector.authenticated) "[Key ${sector.keyType}: ${sector.keyUsed}]" else "[LOCKED]") + " ──")
                    for ((bi, block) in sector.blocks.withIndex()) {
                        sb.appendLine("  Block ${sector.index * 4 + bi}: ${block.chunked(2).joinToString(" ")}")
                    }
                }
            }
            CardType.MIFARE_ULTRALIGHT -> {
                for (page in card.pages) {
                    sb.appendLine("Page %02d: %s".format(page.index, page.data.chunked(2).joinToString(" ")))
                }
            }
            else -> {
                if (card.apduLog.isEmpty()) {
                    sb.appendLine("No raw dump available for this card type.")
                }
            }
        }
        return sb.toString().ifBlank { "(No hex dump)" }
    }

    private fun buildApduLog(card: NfcCard): String {
        if (card.apduLog.isEmpty()) return "(No APDU log)"
        return card.apduLog.joinToString("\n\n") { exchange ->
            buildString {
                appendLine("▶ ${exchange.description}")
                appendLine("  CMD: ${exchange.command.chunked(2).joinToString(" ")}")
                append("  RSP: ${exchange.response.chunked(2).joinToString(" ")}")
            }
        }
    }

    private fun loadForReplay() {
        val card = currentCard ?: return
        EmulationSessionManager.loadCard(card)
        Snackbar.make(binding.root, "Card loaded for replay. Go to Replay tab.", Snackbar.LENGTH_LONG).show()
        Timber.i("Card ${card.uid} loaded for HCE replay")
    }

    private fun exportCard() {
        val card = currentCard ?: return
        val export = buildString {
            appendLine("=== NFC Card Export ===")
            appendLine("Label: ${card.label}")
            appendLine("UID: ${card.uid}")
            appendLine("Type: ${card.cardType.displayName}")
            appendLine("Timestamp: ${Date(card.timestamp)}")
            appendLine()
            appendLine(buildExtraSection(card))
            appendLine()
            appendLine("=== Hex Dump ===")
            appendLine(buildHexDump(card))
            appendLine()
            appendLine("=== APDU Log ===")
            append(buildApduLog(card))
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "NFC Card: ${card.label}")
            putExtra(Intent.EXTRA_TEXT, export)
        }
        startActivity(Intent.createChooser(shareIntent, "Export Card"))
    }

    companion object {
        const val EXTRA_CARD_ID = "extra_card_id"
    }
}
