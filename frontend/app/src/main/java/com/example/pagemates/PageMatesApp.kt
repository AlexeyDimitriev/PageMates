package com.example.pagemates

import android.app.Application
import com.example.pagemates.di.AppContainer

class PageMatesApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
