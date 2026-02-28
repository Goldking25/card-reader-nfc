package com.nfcpoc

import android.app.Application
import timber.log.Timber

/**
 * Application class — initialises global logging via Timber.
 */
class NfcPocApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Plant a debug tree in debug builds only
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
