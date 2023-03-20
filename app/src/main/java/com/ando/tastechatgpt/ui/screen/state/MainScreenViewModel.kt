package com.ando.tastechatgpt.ui.screen.state

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.ando.tastechatgpt.constant.PreferencesKey
import com.ando.tastechatgpt.data.repo.ChatRepo
import com.ando.tastechatgpt.data.repo.UserRepo
import com.ando.tastechatgpt.profile
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val chatRepo: ChatRepo,
    private val userRepo: UserRepo,
    @ApplicationContext private val context:Context
):ViewModel() {

    var drawerUiState by mutableStateOf(
        DrawerUiState(
            pagingDataFlow = getPagingDataFlow(),
            currentChatId = getCurrentChatId()
        )
    )
        private set

    private fun getCurrentChatId():Flow<Int?>{
        return context.profile.data.map { it[PreferencesKey.currentChatId] }
    }

    private fun updateMessage(message: String){
        drawerUiState = drawerUiState.copy(message = message)
    }

    private fun getPagingDataFlow():Flow<PagingData<RecentChat>>{
        return Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = {chatRepo.getRecentChatPagingSource()}
        )
            .flow
            .map {pagingData->
                pagingData.map {
                    val user = userRepo.fetchById(it.uid).first()
                    RecentChat(chatId = it.id, avatar = user!!.avatar, name = user.name, lastMessage = it.text)
                }
            }
            .cachedIn(viewModelScope)
    }
}

data class RecentChat(
    val chatId:Int,
    val avatar: Uri?,
    val name:String,
    val lastMessage:String,
)

data class DrawerUiState(
    val pagingDataFlow:Flow<PagingData<RecentChat>>,
    val currentChatId: Flow<Int?>,
    val message:String = ""
)