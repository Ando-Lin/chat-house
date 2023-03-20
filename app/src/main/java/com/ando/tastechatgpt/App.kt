package com.ando.tastechatgpt

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.ando.tastechatgpt.ui.component.SnackbarUI
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineExceptionHandler

@HiltAndroidApp
class App:Application() {
}

val Context.profile by preferencesDataStore(name = "profile")

//val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
//
//}