package com.nfcpoc.hce

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import timber.log.Timber

/**
 * Android Host Card Emulation (HCE) service.
 *
 * Registered in AndroidManifest.xml with the BIND_NFC_SERVICE permission.
 * The Android NFC framework calls [processCommandApdu] on a binder thread
 * each time the phone is brought close to a reader and an APDU is received.
 *
 * This service is stateless itself — all routing logic and card data
 * live in [EmulationSessionManager] and [ApduRouter].
 *
 * Emulation is only active when [EmulationSessionManager.isEmulating] is true,
 * preventing spurious responses when the user is not in replay mode.
 */
class HceEmulationService : HostApduService() {

    override fun onCreate() {
        super.onCreate()
        Timber.d("HceEmulationService: onCreate")
    }

    /**
     * Called by the Android NFC framework on every incoming command APDU.
     * Must return the response synchronously (runs on a binder thread).
     *
     * @param commandApdu The complete command APDU received from the reader.
     * @param extras       Reserved for future use by the framework.
     * @return Response APDU bytes including status word.
     */
    override fun processCommandApdu(commandApdu: ByteArray, extras: Bundle?): ByteArray {
        Timber.d("HceEmulationService: processCommandApdu: ${commandApdu.toHexString()}")
        return EmulationSessionManager.routeApdu(commandApdu)
    }

    /**
     * Called when the service is deactivated — either the reader moved away
     * or another service was selected.
     *
     * @param reason [DEACTIVATION_LINK_LOSS] or [DEACTIVATION_DESELECTED]
     */
    override fun onDeactivated(reason: Int) {
        val reasonStr = when (reason) {
            DEACTIVATION_LINK_LOSS -> "LINK_LOSS"
            DEACTIVATION_DESELECTED -> "DESELECTED"
            else -> "UNKNOWN($reason)"
        }
        Timber.i("HceEmulationService: deactivated — reason: $reasonStr")
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("HceEmulationService: onDestroy")
    }

    private fun ByteArray.toHexString(): String =
        joinToString("") { "%02X".format(it) }
}
