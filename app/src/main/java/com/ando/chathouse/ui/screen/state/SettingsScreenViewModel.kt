package com.ando.chathouse.ui.screen.state

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsScreenViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val apikeyKey = stringPreferencesKey(
        (OpenAIGPT3d5Model.LABEL + PreferencesKey.apiKeySuffix)
    )
    private val enableHelpKey = PreferencesKey.enableBugly

    val uiState: SettingsScreenUiState by mutableStateOf(SettingsScreenUiState(
        apiKeyFlow = readFromProfile(apikeyKey),
        enableHelpCollectingFlow = readFromProfile(enableHelpKey)
    ))

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

    fun updateApiKey(value:String) {
        writeToProfile(apikeyKey, value.trim())
    }
    fun updateEnableHelp(value: Boolean) = writeToProfile(enableHelpKey, value)

}

data class SettingsScreenUiState(
    private val apiKeyFlow:Flow<String?>,
    private val enableHelpCollectingFlow:Flow<Boolean?>
){
    @Composable
    fun apiKeyState() = apiKeyFlow.collectAsState(initial = "")

    @Composable
    fun enableHelpCollectingState() = enableHelpCollectingFlow.collectAsState(initial = null)
}