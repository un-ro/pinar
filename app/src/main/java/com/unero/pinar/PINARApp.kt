package com.unero.pinar

import android.app.Application
import com.unero.pinar.di.repositoryModule
import com.unero.pinar.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber

class PINARApp: Application() {

    override fun onCreate() {
        super.onCreate()

        // Create Timber Tree
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Init Koin
        startKoin {
            androidLogger()
            androidContext(this@PINARApp)
            modules(
                listOf(
                    viewModelModule,
                    repositoryModule
                )
            )
        }
    }
}