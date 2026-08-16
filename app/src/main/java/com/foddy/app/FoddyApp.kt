package com.foddy.app

import android.app.Application
import com.foddy.app.BuildConfig
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import org.osmdroid.config.Configuration

@HiltAndroidApp
class FoddyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        
        // OSMDroid Configuration for cache and user agent (Required)
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
