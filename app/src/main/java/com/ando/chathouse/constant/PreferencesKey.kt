package com.ando.chathouse.constant

import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PreferencesKey {
    const val apiKeySuffix:String = "_api-key"
    val nightMode = stringPreferencesKey("night_mode")
    val currentModel = stringPreferencesKey("current_model")
    val currentChatId = intPreferencesKey("current_chat_id")
}

