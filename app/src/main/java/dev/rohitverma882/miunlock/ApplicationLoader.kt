package dev.rohitverma882.miunlock

import android.app.Application
import android.content.Context
import android.content.SharedPreferences

class ApplicationLoader : Application() {
    override fun onCreate() {
        super.onCreate()
        prefs = getSharedPreferences("app", Context.MODE_PRIVATE)
    }

    companion object {
        lateinit var prefs: SharedPreferences
    }
}