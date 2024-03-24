package com.imbaland.cinenigma

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CinenigmaApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }
}