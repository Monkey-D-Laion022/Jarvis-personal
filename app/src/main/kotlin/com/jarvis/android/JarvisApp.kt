package com.jarvis.android

import android.app.Application
import com.jarvis.android.core.AppContainer

class JarvisApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
