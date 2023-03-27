package com.ando.chathouse.ui.screen.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.ando.chathouse.constant.MY_UID
import com.ando.chathouse.data.repo.UserRepo
import com.ando.chathouse.domain.entity.UserEntity
import com.ando.chathouse.domain.entity.toUser
import com.ando.chathouse.domain.pojo.User
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
                .onFailure { updateMessage("删除角色失败：${it.localizedMessage}") }
                .onSuccess { updateMessage("删除角色成功") }
        }
    }

    private fun updateMessage(message: String) {
        screenUiState = screenUiState.copy(message = message)
    }

    fun resetMessage(){
        updateMessage("")
    }

}

data class RoleListScreenUiState(
    val pagingDataFlow: Flow<PagingData<User>>,
    val message: String = "",
    val myId: Int = MY_UID
)