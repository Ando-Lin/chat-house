package com.ando.chathouse.ui.screen.state

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ando.chathouse.constant.PreferencesKey
import com.ando.chathouse.model.impl.OpenAIGPT3d5Model
import com.ando.chathouse.profile
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsScreenViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    val key = stringPreferencesKey(
        (OpenAIGPT3d5Model.LABEL + PreferencesKey.apiKeySuffix)
    )
    val apiKey = readFromProfile(key)
            .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    fun <T> readFromProfile(key: Preferences.Key<T>): Flow<T?> {
        return context.profile.data.map { it[key] }
    }

    fun <T> writeToProfile(key: Preferences.Key<T>, value:T){
        viewModelScope.launch {
            context.profile.updateData {
                val mutablePreferences = it.toMutablePreferences()
                mutablePreferences[key] = value
                mutablePreferences
            }
        }
    }

}