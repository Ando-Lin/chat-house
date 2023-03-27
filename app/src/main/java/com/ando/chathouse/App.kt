package com.ando.chathouse

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App:Application()

val Context.profile by preferencesDataStore(name = "profile")

//val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
//
//}