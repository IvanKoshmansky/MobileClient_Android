package com.example.android.mobileclient.di

import android.app.Application
import android.content.SharedPreferences
import com.example.android.mobileclient.database.LocalDatabase
import com.example.android.mobileclient.database.getDatabase
import com.example.android.mobileclient.storage.SharedPreferencesStorage
import com.example.android.mobileclient.storage.getSharedPreferencesStorage
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

//
// памятка
//

//Use @Binds to tell Dagger which implementation it needs to use when providing an interface.

//You can use the @Provides annotation in Dagger modules to tell Dagger how to provide:
//Implementations of an interface (although @Binds is recommended because it generates less code and therefore it's more efficient).
//Classes that your project doesn't own (e.g. instances of Retrofit)

//
// модуль для инжекции БД в локальный репозиторий
//
@Module
class StorageModule {

    @Provides
    @Singleton
    fun provideStorageDatabase(application: Application): LocalDatabase {
        return getDatabase(application.applicationContext)
    }

    @Provides
    @Singleton
    fun provideSharedPreferencesStorage(application: Application): SharedPreferencesStorage {
        return getSharedPreferencesStorage(application.applicationContext)
    }
}
