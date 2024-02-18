package dev.rohitverma882.miunlock

import android.app.Application
import android.content.Context
import android.content.SharedPreferences

import com.google.android.material.color.DynamicColors

class ApplicationLoader : Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)

        prefs = getSharedPreferences("app", Context.MODE_PRIVATE)
    }

    companion object {
        lateinit var prefs: SharedPreferences
    }
}