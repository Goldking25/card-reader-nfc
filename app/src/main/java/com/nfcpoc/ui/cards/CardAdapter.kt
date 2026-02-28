package com.nfcpoc.ui.cards

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nfcpoc.R
import com.nfcpoc.data.model.CardType
import com.nfcpoc.data.model.NfcCard
import com.nfcpoc.databinding.ItemCardBinding
import java.text.SimpleDateFormat
import java.util.*

/**
 * ListAdapter for the stored cards RecyclerView.
 * Uses DiffUtil for efficient updates.
 */
class CardAdapter(
    private val onItemClick: (NfcCard) -> Unit,
    private val onDeleteClick: (NfcCard) -> Unit
) : ListAdapter<NfcCard, CardAdapter.CardViewHolder>(DIFF_CALLBACK) {

    inner class CardViewHolder(private val binding: ItemCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(card: NfcCard) {
            binding.tvCardLabel.text = card.label
            binding.tvCardUid.text = card.uid
            binding.tvCardType.text = card.cardType.displayName
            binding.tvCardTimestamp.text = formatTimestamp(card.timestamp)
            binding.tvCardNotes.text = card.notes.ifBlank { "\u2014" }

            // Use ContextCompat.getColor — safe on all API levels
            val badgeColor = ContextCompat.getColor(
                binding.root.context,
                getTypeBadgeColorRes(card.cardType)
            )
            binding.viewTypeBadge.setBackgroundColor(badgeColor)

            binding.root.setOnClickListener { onItemClick(card) }
            binding.btnDeleteCard.setOnClickListener { onDeleteClick(card) }
        }

        private fun formatTimestamp(ts: Long): String =
            SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(ts))

        private fun getTypeBadgeColorRes(type: CardType): Int = when (type) {
            CardType.MIFARE_CLASSIC    -> R.color.badge_mifare_classic
            CardType.MIFARE_ULTRALIGHT -> R.color.badge_mifare_ultralight
            CardType.ISO_DEP_A        -> R.color.badge_iso_dep_a
            CardType.ISO_DEP_B        -> R.color.badge_iso_dep_b
            CardType.NFC_F            -> R.color.badge_nfc_f
            CardType.NFC_V            -> R.color.badge_nfc_v
            CardType.UNKNOWN          -> R.color.badge_unknown
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding = ItemCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<NfcCard>() {
            override fun areItemsTheSame(oldItem: NfcCard, newItem: NfcCard): Boolean =
                oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: NfcCard, newItem: NfcCard): Boolean =
                oldItem == newItem
        }
    }
}
