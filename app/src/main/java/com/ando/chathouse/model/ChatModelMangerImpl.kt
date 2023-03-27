package com.ando.chathouse.model

import com.ando.chathouse.domain.pojo.RoleMessage
import kotlinx.coroutines.flow.Flow

class ChatModelMangerImpl : ChatModelManger {
    //模型列表
    private val _models: MutableMap<String, Lazy<ChatModel>> = mutableMapOf()
    override val models: Map<String, Lazy<ChatModel>>
        get() = _models

    /**
     * 添加模型
     */
    override fun addModel(name: String, model: Lazy<ChatModel>): ChatModelManger {
        _models[name] = model
        return this
    }


    /**
     * 发送一条消息
     */
    override fun sendMessage(
        modelName: String,
        para: ChatModel.Para,
        message: String
    ): Flow<String?> {
        val model = _models[modelName] ?: throw IllegalArgumentException("未找到{$modelName}模型")
        return model.value.sendMessage(message = message, para = para)
    }

    /**
     * 发送多条消息。若模型不能发送多条则发送messages列表的最后一条
     */
    override fun sendMessages(
        modelName: String,
        para: ChatModel.Para,
        messages: List<RoleMessage>
    ): Flow<String?> {
        val lazyModel = _models[modelName] ?: throw IllegalArgumentException("未找到{$modelName}模型")
        val model = lazyModel.value
        return if (model is LongChatModel) {
            model.sendMessages(messages = messages, para = para)
        } else {
            model.sendMessage(message = messages[0].content, para = para)
        }
    }


}