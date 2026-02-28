package com.nfcpoc.ui.scan

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.nfcpoc.R
import com.nfcpoc.data.model.CardType
import com.nfcpoc.data.model.NfcCard
import com.nfcpoc.databinding.FragmentScanBinding
import timber.log.Timber

/**
 * The Scan screen — guides the user to tap a card and shows live read results.
 *
 * Uses an activity-scoped ViewModel so that tag events dispatched from
 * [MainActivity.handleIntent] are shared seamlessly with this fragment.
 */
class ScanFragment : Fragment() {

    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding!!

    // Activity-scoped so MainActivity can post tag events
    private val viewModel: ScanViewModel by activityViewModels {
        ScanViewModelFactory(requireActivity().application)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupClickListeners()
        showIdleState()
    }

    private fun setupObservers() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            Timber.d("ScanFragment: state=$state")
            when (state) {
                is ScanUiState.Idle -> showIdleState()
                is ScanUiState.Reading -> showReadingState()
                is ScanUiState.Success -> showSuccessState(state.card)
                is ScanUiState.Error -> showErrorState(state.message)
                is ScanUiState.Saved -> onCardSaved()
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnSaveCard.setOnClickListener { showSaveDialog() }
        binding.btnReset.setOnClickListener { viewModel.reset() }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UI state transitions
    // ─────────────────────────────────────────────────────────────────────────

    private fun showIdleState() {
        binding.apply {
            nfcRingView.visibility = View.VISIBLE
            progressIndicator.visibility = View.GONE
            cardResultCard.visibility = View.GONE
            btnSaveCard.visibility = View.GONE
            btnReset.visibility = View.GONE
            tvStatus.text = getString(R.string.scan_idle_hint)
        }
        startRingAnimation()
    }

    private fun showReadingState() {
        stopRingAnimation()
        binding.apply {
            nfcRingView.visibility = View.GONE
            progressIndicator.visibility = View.VISIBLE
            cardResultCard.visibility = View.GONE
            btnSaveCard.visibility = View.GONE
            btnReset.visibility = View.GONE
            tvStatus.text = getString(R.string.scan_reading)
        }
    }

    private fun showSuccessState(card: NfcCard) {
        binding.apply {
            progressIndicator.visibility = View.GONE
            nfcRingView.visibility = View.GONE
            cardResultCard.visibility = View.VISIBLE
            btnSaveCard.visibility = View.VISIBLE
            btnReset.visibility = View.VISIBLE
            tvStatus.text = getString(R.string.scan_success)

            // Populate result card
            tvCardType.text = card.cardType.displayName
            tvCardUid.text = card.uid
            tvCardTechs.text = card.techList.joinToString("\n") { it.substringAfterLast('.') }
            tvCardNotes.text = card.notes.ifBlank { "—" }

            // Show type-specific details
            when (card.cardType) {
                CardType.MIFARE_CLASSIC -> {
                    tvExtraInfo.text = "Memory: ${card.mifareSize / 1024}K — " +
                            "${card.authenticatedSectorCount}/${card.sectors.size} sectors read"
                }
                CardType.MIFARE_ULTRALIGHT -> {
                    tvExtraInfo.text = "${card.pages.size} pages read"
                }
                CardType.ISO_DEP_A, CardType.ISO_DEP_B -> {
                    tvExtraInfo.text = "AID: ${card.selectedAid ?: "N/A"} — " +
                            "${card.apduLog.size} APDU exchanges"
                }
                CardType.NFC_F -> {
                    tvExtraInfo.text = "IDm: ${card.idm ?: "N/A"}"
                }
                else -> tvExtraInfo.text = "—"
            }
        }
    }

    private fun showErrorState(message: String) {
        binding.apply {
            progressIndicator.visibility = View.GONE
            nfcRingView.visibility = View.VISIBLE
            cardResultCard.visibility = View.GONE
            btnSaveCard.visibility = View.GONE
            btnReset.visibility = View.VISIBLE
            tvStatus.text = getString(R.string.scan_error)
        }
        startRingAnimation()
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun onCardSaved() {
        Snackbar.make(binding.root, getString(R.string.card_saved), Snackbar.LENGTH_SHORT).show()
        viewModel.reset()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Dialogs
    // ─────────────────────────────────────────────────────────────────────────

    private fun showSaveDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_save_card, null)
        val labelInput = dialogView.findViewById<TextInputEditText>(R.id.editLabelInput)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.save_card_title)
            .setView(dialogView)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.save) { _, _ ->
                viewModel.saveCard(labelInput.text?.toString())
            }
            .show()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Animations
    // ─────────────────────────────────────────────────────────────────────────

    private fun startRingAnimation() {
        binding.nfcRingView.startAnimation(
            AnimationUtils.loadAnimation(requireContext(), R.anim.pulse_ring)
        )
    }

    private fun stopRingAnimation() {
        binding.nfcRingView.clearAnimation()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
