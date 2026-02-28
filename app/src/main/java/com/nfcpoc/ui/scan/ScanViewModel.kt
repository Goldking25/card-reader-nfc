package com.nfcpoc.ui.scan

import android.app.Application
import android.nfc.Tag
import androidx.lifecycle.*
import com.nfcpoc.data.database.CardDatabase
import com.nfcpoc.data.model.NfcCard
import com.nfcpoc.data.repository.CardRepository
import com.nfcpoc.nfc.NfcReadException
import com.nfcpoc.nfc.NfcReaderManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

/** UI state for the Scan screen. */
sealed class ScanUiState {
    object Idle : ScanUiState()
    object Reading : ScanUiState()
    data class Success(val card: NfcCard) : ScanUiState()
    data class Error(val message: String) : ScanUiState()
    object Saved : ScanUiState()
}

class ScanViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CardRepository.getInstance(
        CardDatabase.getInstance(application).cardDao()
    )
    private val readerManager = NfcReaderManager()

    private val _uiState = MutableLiveData<ScanUiState>(ScanUiState.Idle)
    val uiState: LiveData<ScanUiState> = _uiState

    /** Stores the most recently scanned card (not yet persisted). */
    private var pendingCard: NfcCard? = null

    /**
     * Called by [MainActivity] when a new NFC tag is discovered.
     * Launches tag reading on the IO dispatcher.
     */
    fun onTagDiscovered(tag: Tag) {
        if (_uiState.value is ScanUiState.Reading) return // ignore if already reading

        _uiState.value = ScanUiState.Reading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Timber.i("Reading tag: ${tag.id.toHexString()}")
                val card = readerManager.readTag(tag)
                pendingCard = card
                _uiState.postValue(ScanUiState.Success(card))
            } catch (e: NfcReadException) {
                Timber.e(e, "NFC read failed")
                _uiState.postValue(ScanUiState.Error("Read failed: ${e.message}"))
            } catch (e: Exception) {
                Timber.e(e, "Unexpected error during tag read")
                _uiState.postValue(ScanUiState.Error("Unexpected error: ${e.message}"))
            }
        }
    }

    /**
     * Save the pending card with an optional user-supplied label.
     * Posts [ScanUiState.Saved] on success.
     */
    fun saveCard(label: String?) {
        val card = pendingCard ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val finalCard = if (!label.isNullOrBlank()) card.copy(label = label) else card
            repository.saveCard(finalCard)
            Timber.i("Card saved: ${finalCard.uid}")
            _uiState.postValue(ScanUiState.Saved)
        }
    }

    /** Reset to idle (e.g., after saving or dismissal). */
    fun reset() {
        pendingCard = null
        _uiState.value = ScanUiState.Idle
    }

    private fun ByteArray.toHexString(): String =
        joinToString("") { "%02X".format(it) }
}

class ScanViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScanViewModel::class.java)) {
            return ScanViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}
