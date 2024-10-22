package com.vonabe.vibromessage

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics

class App : Application() {

    companion object {
        const val TAG: String = "VibroMessage -> App"
    }

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        Log.d(TAG," Initialize App")
    }

}