package com.example

import android.app.Application
import com.example.di.AppContainer

class ShivAiApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        // Initialization code (e.g. Logger setup) goes here
    }
}
