package com.nfcpoc.ui

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.*
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import com.nfcpoc.R
import com.nfcpoc.databinding.ActivityMainBinding
import timber.log.Timber

/**
 * Single-activity host using Navigation Component.
 * Owns the NFC foreground dispatch lifecycle.
 *
 * Foreground dispatch intercepts NFC intents before the system-level
 * tag-dispatch happens, ensuring this app handles all NFC tags while open.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private var nfcAdapter: NfcAdapter? = null
    private var pendingNfcIntent: PendingIntent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // Navigation setup
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val appBarConfig = AppBarConfiguration(
            setOf(R.id.navigation_scan, R.id.navigation_cards, R.id.navigation_replay)
        )
        setupActionBarWithNavController(navController, appBarConfig)
        binding.bottomNavigation.setupWithNavController(navController)

        // NFC adapter
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            Snackbar.make(binding.root, "This device does not support NFC", Snackbar.LENGTH_LONG).show()
        }

        // Foreground dispatch PendingIntent — FLAG_MUTABLE required for NFC on API 31+
        pendingNfcIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_MUTABLE
        )

        // Handle tag if app was cold-launched by a tag tap
        handleIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        updateNfcState()
    }

    override fun onPause() {
        super.onPause()
        disableForegroundDispatch()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onSupportNavigateUp(): Boolean =
        navController.navigateUp() || super.onSupportNavigateUp()

    // ─────────────────────────────────────────────────────────────────────────

    fun toggleNfcDispatch(enable: Boolean) {
        updateNfcState()
    }

    fun updateNfcState() {
        val emulating = com.nfcpoc.hce.EmulationSessionManager.isEmulating
        if (emulating) {
            disableForegroundDispatch()
            enablePreferredService()
        } else {
            disablePreferredService()
            val scanViewModel = obtainScanViewModel()
            if (scanViewModel.isScanActive.value == true) {
                enableForegroundDispatch()
            } else {
                disableForegroundDispatch()
            }
        }
    }

    private fun enablePreferredService() {
        val adapter = nfcAdapter ?: return
        val cardEmulation = android.nfc.cardemulation.CardEmulation.getInstance(adapter)
        val componentName = android.content.ComponentName(this, com.nfcpoc.hce.HceEmulationService::class.java)
        cardEmulation.setPreferredService(this, componentName)
        Timber.d("NFC preferred HCE service enabled")
    }

    private fun disablePreferredService() {
        val adapter = nfcAdapter ?: return
        Timber.d("NFC preferred HCE service disabled")
        try {
            val cardEmulation = android.nfc.cardemulation.CardEmulation.getInstance(adapter)
            cardEmulation.unsetPreferredService(this)
        } catch (e: Exception) {
            Timber.e(e, "Error unsetting preferred service")
        }
    }

    private fun enableForegroundDispatch() {
        val adapter = nfcAdapter ?: return
        val filters = arrayOf(
            IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED),
            IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED),
            IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
        )
        val techLists = arrayOf(
            arrayOf(MifareClassic::class.java.name),
            arrayOf(MifareUltralight::class.java.name),
            arrayOf(IsoDep::class.java.name),
            arrayOf(NfcA::class.java.name),
            arrayOf(NfcB::class.java.name),
            arrayOf(NfcF::class.java.name),
            arrayOf(NfcV::class.java.name)
        )
        try {
            adapter.enableForegroundDispatch(this, pendingNfcIntent, filters, techLists)
            Timber.d("NFC foreground dispatch enabled")
        } catch (e: Exception) {
            Timber.e(e, "Failed to enable foreground dispatch")
        }
    }

    private fun disableForegroundDispatch() {
        try {
            nfcAdapter?.disableForegroundDispatch(this)
        } catch (e: Exception) {
            Timber.e(e, "Failed to disable foreground dispatch")
        }
    }

    private fun handleIntent(intent: Intent) {
        val action = intent.action ?: return
        if (action !in listOf(
                NfcAdapter.ACTION_TECH_DISCOVERED,
                NfcAdapter.ACTION_TAG_DISCOVERED,
                NfcAdapter.ACTION_NDEF_DISCOVERED
            )
        ) return

        // getParcelableExtra(String) is deprecated on API 33+; use typed version
        val tag: Tag? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        }

        if (tag != null) {
            Timber.d("handleIntent: tag UID=${tag.id.joinToString("") { "%02X".format(it) }}")
            navController.navigate(R.id.navigation_scan)
            obtainScanViewModel().onTagDiscovered(tag)
        }
    }

    private fun obtainScanViewModel(): com.nfcpoc.ui.scan.ScanViewModel {
        return androidx.lifecycle.ViewModelProvider(
            this,
            com.nfcpoc.ui.scan.ScanViewModelFactory(application)
        )[com.nfcpoc.ui.scan.ScanViewModel::class.java]
    }
}
