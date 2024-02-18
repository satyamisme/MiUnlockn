package dev.rohitverma882.miunlock

import android.app.Application

class ApplicationLoader : Application() {
    override fun onCreate() {
        super.onCreate()
        applicationLoader = this
    }

    companion object {
        lateinit var applicationLoader: ApplicationLoader
    }
}