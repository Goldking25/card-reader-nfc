package com.nfcpoc.ui.cards

import android.app.Application
import androidx.lifecycle.*
import com.nfcpoc.data.database.CardDatabase
import com.nfcpoc.data.model.NfcCard
import com.nfcpoc.data.repository.CardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CardListViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CardRepository.getInstance(
        CardDatabase.getInstance(application).cardDao()
    )

    /** Live list of all cards, newest first. */
    val allCards: LiveData<List<NfcCard>> = repository.allCards.asLiveData()

    private val _searchQuery = MutableLiveData("")
    val searchQuery: LiveData<String> = _searchQuery

    /** Cards filtered by the current search query. */
    val filteredCards: LiveData<List<NfcCard>> = _searchQuery.switchMap { query ->
        if (query.isBlank()) {
            repository.allCards.asLiveData()
        } else {
            repository.searchCards(query).asLiveData()
        }
    }

    fun setSearchQuery(q: String) { _searchQuery.value = q }

    fun deleteCard(card: NfcCard) {
        viewModelScope.launch(Dispatchers.IO) { repository.deleteCard(card) }
    }

    fun deleteAllCards() {
        viewModelScope.launch(Dispatchers.IO) { repository.deleteAllCards() }
    }

    fun updateLabel(card: NfcCard, newLabel: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateCard(card.copy(label = newLabel))
        }
    }
}

class CardListViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CardListViewModel::class.java)) {
            return CardListViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}
