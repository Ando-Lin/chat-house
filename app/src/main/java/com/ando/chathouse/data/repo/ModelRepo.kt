package com.ando.chathouse.data.repo

import com.ando.chathouse.model.ChatModel
import kotlinx.coroutines.flow.Flow


interface ModelRepo<T:ChatModel> {
    fun getAllModel(): Flow<T?>
    fun getAllModelName(): Flow<Set<String>>
    fun getModelByName(name:String):Flow<T?>
    suspend fun save(name: String, model:T):Result<Unit>
    suspend fun deleteByName(name: String):Result<Unit>
}