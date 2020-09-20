package com.app.meetup

import android.app.Application
import com.google.android.libraries.places.api.Places
import java.util.*

class App: Application() {

    override fun onCreate() {
        super.onCreate()

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.google_maps_key), Locale.US);
        }
    }
}