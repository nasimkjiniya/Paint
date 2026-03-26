package com.example.shapepaint

import android.app.Application
import com.example.shapepaint.data.AppContainer
import com.example.shapepaint.data.DefaultAppContainer

class ShapePaintApplication : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = DefaultAppContainer(this)
    }
}
