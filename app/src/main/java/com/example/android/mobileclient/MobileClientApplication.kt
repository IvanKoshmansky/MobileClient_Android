package com.example.android.mobileclient

import android.app.Application
import com.example.android.mobileclient.di.AppComponent
import com.example.android.mobileclient.di.*
import timber.log.Timber

open class MobileClientApplication : Application() {

    val appComponent: AppComponent by lazy {
        DaggerAppComponent.factory().create(this)
    }

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}
