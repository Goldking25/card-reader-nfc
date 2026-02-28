package com.nfcpoc.ui.cards

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.nfcpoc.R
import com.nfcpoc.data.model.NfcCard
import com.nfcpoc.databinding.FragmentCardListBinding

/**
 * Displays all stored NFC card dumps in a searchable, scrollable list.
 * Tapping a card opens [CardDetailActivity]; long-press / button deletes it.
 */
class CardListFragment : Fragment() {

    private var _binding: FragmentCardListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CardListViewModel by viewModels {
        CardListViewModelFactory(requireActivity().application)
    }

    private val adapter = CardAdapter(
        onItemClick = { card -> openDetail(card) },
        onDeleteClick = { card -> confirmDelete(card) }
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCardListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // LayoutManager set in code — never rely on app:layoutManager XML
        // (that attribute uses string reflection which R8 strips in release builds)
        binding.recyclerCards.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerCards.adapter = adapter

        viewModel.filteredCards.observe(viewLifecycleOwner) { cards ->
            adapter.submitList(cards)
            binding.emptyState.visibility = if (cards.isEmpty()) View.VISIBLE else View.GONE
        }

        // Modern MenuProvider API — replaces deprecated setHasOptionsMenu / onCreateOptionsMenu
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_card_list, menu)

                val searchItem = menu.findItem(R.id.action_search)
                val searchView = searchItem?.actionView as? SearchView
                searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(q: String?) = false
                    override fun onQueryTextChange(q: String?): Boolean {
                        viewModel.setSearchQuery(q ?: "")
                        return true
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_delete_all -> { confirmDeleteAll(); true }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun openDetail(card: NfcCard) {
        val intent = Intent(requireContext(), CardDetailActivity::class.java)
        intent.putExtra(CardDetailActivity.EXTRA_CARD_ID, card.id)
        startActivity(intent)
    }

    private fun confirmDelete(card: NfcCard) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_card_title)
            .setMessage(getString(R.string.delete_card_message, card.label))
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteCard(card)
                Snackbar.make(binding.root, R.string.card_deleted, Snackbar.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun confirmDeleteAll() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_all_title)
            .setMessage(R.string.delete_all_message)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteAllCards()
                Snackbar.make(binding.root, R.string.all_cards_deleted, Snackbar.LENGTH_SHORT).show()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
