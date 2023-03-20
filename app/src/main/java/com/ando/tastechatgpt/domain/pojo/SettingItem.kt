package com.ando.tastechatgpt.domain.pojo

import androidx.annotation.StringRes
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ando.tastechatgpt.Destination
import com.ando.tastechatgpt.NightModeScreenDestination
import com.ando.tastechatgpt.R
import com.ando.tastechatgpt.constant.PreferencesKey
import com.ando.tastechatgpt.model.ChatCatModel
import com.ando.tastechatgpt.model.OpenAIGPT3d5Model
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

abstract class SettingItem<T>(@StringRes val nameResId: Int) {
    abstract val value: Flow<T?>
    abstract val onInputComplete: suspend (T) -> Unit
    lateinit var dataStore: DataStore<Preferences>
    abstract val type: Type

    enum class Type {
        String,
        Int,
        Double,
        Boolean,
        Destination,
        Radio,
    }
}

abstract class BaseTypeSetting<T>(nameResId: Int, val key: Preferences.Key<T>, override val type:Type) :
    SettingItem<T>(nameResId) {
    override val value: Flow<T?>
        get() = dataStore.data.map { it[key] }
    override val onInputComplete: suspend (T) -> Unit
        get() = { value -> dataStore.edit { it[key] = value } }

}

abstract class DestinationSetting(nameResId: Int, val destination: Destination) :
    SettingItem<Destination>(nameResId) {
    override val value: Flow<Destination>
        get() = flowOf(destination)
    override val onInputComplete: suspend (Destination) -> Unit
        get() = {}
    override val type: Type
        get() = Type.Destination
}

abstract class RadioSetting(
    nameResId: Int,
    val key: Preferences.Key<String>,
    val optionalResId: List<Int>
) : SettingItem<String>(nameResId) {
    override val value: Flow<String?>
        get() = dataStore.data.map { it[key] }
    override val onInputComplete: suspend (String) -> Unit
        get() = { value -> dataStore.edit { it[key] = value } }
    override val type: Type
        get() = Type.Radio
}


object NightModeSetting : DestinationSetting(
    nameResId = R.string.night_mode_setting,
    destination = NightModeScreenDestination
) {

    object ModeSetting : RadioSetting(
        nameResId = R.string.night_mode_setting,
        key = PreferencesKey.nightMode,
        optionalResId = listOf(R.string.auto_switch, R.string.night_mode, R.string.day_mode)
    )
}

object ApiKeySetting:BaseTypeSetting<String>(
    nameResId = R.string.api_key_setting,
    key = stringPreferencesKey(ChatCatModel.modelName+PreferencesKey.apiKeySuffix) ,
    type = Type.String
)



