package com.nfcpoc.ui.replay

import android.app.Application
import androidx.lifecycle.*
import com.nfcpoc.data.database.CardDatabase
import com.nfcpoc.data.model.NfcCard
import com.nfcpoc.data.repository.CardRepository
import com.nfcpoc.hce.EmulationSessionManager
import timber.log.Timber

/**
 * ViewModel for the Replay screen.
 * Bridges [EmulationSessionManager] state to the UI.
 */
class ReplayViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CardRepository.getInstance(
        CardDatabase.getInstance(application).cardDao()
    )

    /** All stored cards for the "pick a card" picker. */
    val allCards: LiveData<List<NfcCard>> = repository.allCards.asLiveData()

    /** The card currently loaded for emulation. */
    private val _activeCard = MutableLiveData<NfcCard?>(EmulationSessionManager.activeCard)
    val activeCard: LiveData<NfcCard?> = _activeCard

    /** Whether emulation is currently running. */
    private val _isEmulating = MutableLiveData(EmulationSessionManager.isEmulating)
    val isEmulating: LiveData<Boolean> = _isEmulating

    /** Load a card into the HCE session and start emulating. */
    fun startEmulation(card: NfcCard) {
        val nfcAdapter = android.nfc.NfcAdapter.getDefaultAdapter(getApplication())
        if (nfcAdapter != null) {
            val componentName = android.content.ComponentName(getApplication(), com.nfcpoc.hce.HceEmulationService::class.java)
            val emulation = android.nfc.cardemulation.CardEmulation.getInstance(nfcAdapter)
            val aids = mutableSetOf<String>()
            if (!card.selectedAid.isNullOrBlank()) aids.add(card.selectedAid.uppercase())
            card.apduLog.forEach { exchange ->
                if (exchange.command.startsWith("00A404", true) && exchange.command.length >= 10) {
                    try {
                        val lc = exchange.command.substring(8, 10).toInt(16)
                        if (exchange.command.length >= 10 + lc * 2) {
                            val aidHex = exchange.command.substring(10, 10 + lc * 2)
                            aids.add(aidHex.uppercase())
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Error parsing AID from APDU log")
                    }
                }
            }
            if (aids.isNotEmpty()) {
                val registered = emulation.registerAidsForService(componentName, "other", aids.toList())
                Timber.i("ReplayViewModel: registered dynamic AIDs ($aids) = $registered")
            }
        }

        EmulationSessionManager.loadCard(card)
        _activeCard.value = card
        _isEmulating.value = true
        Timber.i("ReplayViewModel: started emulation for card ${card.uid}")
    }

    /** Stop emulation and clear the session. */
    fun stopEmulation() {
        EmulationSessionManager.clearSession()
        _activeCard.value = null
        _isEmulating.value = false

        val nfcAdapter = android.nfc.NfcAdapter.getDefaultAdapter(getApplication())
        if (nfcAdapter != null) {
            val componentName = android.content.ComponentName(getApplication(), com.nfcpoc.hce.HceEmulationService::class.java)
            val emulation = android.nfc.cardemulation.CardEmulation.getInstance(nfcAdapter)
            emulation.removeAidsForService(componentName, "other")
            Timber.i("ReplayViewModel: removed dynamic AIDs from service")
        }

        Timber.i("ReplayViewModel: emulation stopped")
    }

    /**
     * Sync LiveData to actual session state (in case the service was
     * deactivated externally, e.g. by the reader moving away and OS re-routing).
     */
    fun syncState() {
        _activeCard.value = EmulationSessionManager.activeCard
        _isEmulating.value = EmulationSessionManager.isEmulating
    }
}

class ReplayViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReplayViewModel::class.java)) {
            return ReplayViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}
