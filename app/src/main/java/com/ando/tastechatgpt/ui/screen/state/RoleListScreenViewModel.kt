package com.ando.tastechatgpt.ui.screen.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.ando.tastechatgpt.constant.HUMAN_UID
import com.ando.tastechatgpt.data.repo.UserRepo
import com.ando.tastechatgpt.domain.entity.UserEntity
import com.ando.tastechatgpt.domain.entity.toUser
import com.ando.tastechatgpt.domain.pojo.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoleListScreenViewModel @Inject constructor(
    private val userRepo: UserRepo
) : ViewModel() {
    var screenUiState: RoleListScreenUiState by mutableStateOf(
        RoleListScreenUiState(getPagingDataFlow())
    )

    private fun getPagingDataFlow() =
        Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { userRepo.getPagingSource() }
        )
            .flow
            .map { pagingData: PagingData<UserEntity> ->
                pagingData.map(UserEntity::toUser)
            }
            .cachedIn(viewModelScope)

    fun delete(uid: Int) {
        viewModelScope.launch {
            userRepo.deleteById(uid)
                .onFailure { updateMessage("删除用户失败${it.message}") }
                .onSuccess { updateMessage("成功删除用户") }
        }
    }

    private fun updateMessage(message: String) {
        screenUiState = screenUiState.copy(message = message)
    }

}

data class RoleListScreenUiState(
    val pagingDataFlow: Flow<PagingData<User>>,
    val message: String = "",
    val myId: Int = HUMAN_UID
)