package com.nfcpoc.ui.replay

import android.os.Bundle
import android.view.*
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nfcpoc.R
import com.nfcpoc.data.model.CardType
import com.nfcpoc.data.model.NfcCard
import com.nfcpoc.databinding.FragmentReplayBinding

/**
 * Replay screen — lets the user:
 *  1. Pick a stored card (or use the one already loaded via CardDetailActivity).
 *  2. Toggle HCE emulation ON/OFF.
 *  3. See live status of the emulation session.
 */
class ReplayFragment : Fragment() {

    private var _binding: FragmentReplayBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ReplayViewModel by viewModels {
        ReplayViewModelFactory(requireActivity().application)
    }

    /** Local cache of all stored cards — populated once allCards LiveData emits. */
    private var cachedCards: List<NfcCard> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReplayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.syncState()
        setupObservers()
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        viewModel.syncState() // Re-sync when returning from card detail
    }

    // ─────────────────────────────────────────────────────────────────────────

    private fun setupObservers() {
        viewModel.activeCard.observe(viewLifecycleOwner) { card ->
            if (card != null) showCardLoaded(card) else showNoCardState()
        }

        viewModel.isEmulating.observe(viewLifecycleOwner) { emulating ->
            updateEmulatingUi(emulating)
        }

        // Observe allCards so the Flow is active and cachedCards stays up-to-date.
        // Without this observer the LiveData never starts collecting from Room
        // and .value is always null when the card picker dialog is opened.
        viewModel.allCards.observe(viewLifecycleOwner) { cards ->
            cachedCards = cards ?: emptyList()
        }
    }

    private fun setupClickListeners() {
        binding.btnToggleEmulation.setOnClickListener {
            if (viewModel.isEmulating.value == true) {
                viewModel.stopEmulation()
            } else {
                val card = viewModel.activeCard.value
                if (card != null) {
                    viewModel.startEmulation(card)
                }
            }
        }

        binding.btnPickCard.setOnClickListener { showCardPicker() }
    }

    // ─────────────────────────────────────────────────────────────────────────

    private fun showNoCardState() {
        binding.tvReplayCardLabel.text = getString(R.string.replay_no_card)
        binding.tvReplayCardUid.text = "—"
        binding.tvReplayCardType.text = "—"
        binding.btnToggleEmulation.isEnabled = false
        binding.btnToggleEmulation.text = getString(R.string.start_emulation)
        binding.emulationStatusIcon.setImageResource(R.drawable.ic_nfc_off)
        binding.tvEmulationStatus.text = getString(R.string.emulation_inactive)
        stopPulse()
    }

    private fun showCardLoaded(card: NfcCard) {
        binding.tvReplayCardLabel.text = card.label
        binding.tvReplayCardUid.text = card.uid
        binding.tvReplayCardType.text = card.cardType.displayName
        binding.btnToggleEmulation.isEnabled =
            card.cardType in listOf(CardType.ISO_DEP_A, CardType.ISO_DEP_B)

        if (card.cardType !in listOf(CardType.ISO_DEP_A, CardType.ISO_DEP_B)) {
            binding.tvHceNote.visibility = View.VISIBLE
            binding.tvHceNote.text = getString(R.string.hce_limitation_note)
        } else {
            binding.tvHceNote.visibility = View.GONE
        }
    }

    private fun updateEmulatingUi(emulating: Boolean) {
        // Toggle the foreground dispatch and HCE preferred service in MainActivity
        (requireActivity() as? com.nfcpoc.ui.MainActivity)?.updateNfcState()

        if (emulating) {
            binding.btnToggleEmulation.text = getString(R.string.stop_emulation)
            binding.emulationStatusIcon.setImageResource(R.drawable.ic_nfc_on)
            binding.tvEmulationStatus.text = getString(R.string.emulation_active)
            startPulse()
        } else {
            binding.btnToggleEmulation.text = getString(R.string.start_emulation)
            binding.emulationStatusIcon.setImageResource(R.drawable.ic_nfc_off)
            binding.tvEmulationStatus.text = getString(R.string.emulation_inactive)
            stopPulse()
        }
    }

    private fun showCardPicker() {
        // Use the locally cached list — allCards.value would be null if
        // the LiveData had no observer yet (fixed in setupObservers).
        if (cachedCards.isEmpty()) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.no_cards_title)
                .setMessage(R.string.no_cards_message)
                .setPositiveButton(R.string.ok, null)
                .show()
            return
        }

        val labels = cachedCards
            .map { "${it.label}  ·  ${it.cardType.displayName}" }
            .toTypedArray()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.pick_card_title)
            .setItems(labels) { _, index ->
                viewModel.startEmulation(cachedCards[index])
            }
            .show()
    }

    private fun startPulse() {
        binding.emulationStatusIcon.startAnimation(
            AnimationUtils.loadAnimation(requireContext(), R.anim.pulse_ring)
        )
    }

    private fun stopPulse() {
        binding.emulationStatusIcon.clearAnimation()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
