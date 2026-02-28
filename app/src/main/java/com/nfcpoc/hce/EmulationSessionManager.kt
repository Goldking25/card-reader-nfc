package com.nfcpoc.hce

import com.nfcpoc.data.model.NfcCard
import timber.log.Timber

/**
 * Singleton session manager controlling which card is currently loaded
 * into the HCE emulation service.
 *
 * This is an in-process singleton shared between the UI layer (ReplayFragment)
 * and the [HceEmulationService]. The service reads [activeCard] on each
 * incoming APDU to ensure it always operates on the latest loaded card.
 */
object EmulationSessionManager {

    /** The card currently loaded for emulation. Null = not emulating. */
    @Volatile
    var activeCard: NfcCard? = null
        private set

    /** Whether emulation is currently active and the service should respond. */
    @Volatile
    var isEmulating: Boolean = false
        private set

    /** The [ApduRouter] for the current session. Recreated for each new card. */
    @Volatile
    private var router: ApduRouter? = null

    /**
     * Load a card into the emulation session and mark as active.
     * Call this before enabling the HCE service (e.g., before bringing
     * ReplayFragment to the foreground).
     */
    fun loadCard(card: NfcCard) {
        synchronized(this) {
            activeCard = card
            router = ApduRouter(card)
            isEmulating = true
            Timber.i("EmulationSession: loaded card UID=${card.uid}, type=${card.cardType}")
        }
    }

    /**
     * Stop emulation and clear the active card.
     * The HCE service will return SW_UNKNOWN for any subsequent APDUs.
     */
    fun clearSession() {
        synchronized(this) {
            activeCard = null
            router = null
            isEmulating = false
            Timber.i("EmulationSession: cleared")
        }
    }

    /**
     * Route an incoming APDU from [HceEmulationService] to the active router.
     * Returns [ApduRouter.SW_COMMAND_NOT_ALLOWED] if no session is active.
     */
    fun routeApdu(commandApdu: ByteArray): ByteArray {
        return if (isEmulating) {
            router?.processApdu(commandApdu) ?: ApduRouter.SW_UNKNOWN
        } else {
            Timber.w("EmulationSession: APDU received but no active session")
            ApduRouter.SW_COMMAND_NOT_ALLOWED
        }
    }
}
