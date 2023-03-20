package com.ando.tastechatgpt.model

import androidx.collection.ArrayMap
import com.ando.tastechatgpt.domain.pojo.RoleMessage
import kotlinx.coroutines.flow.Flow

class ChatModelMangerImpl : ChatModelManger {
    //模型列表
    private val _models: ArrayMap<String, ChatModel> = ArrayMap()
    override val models: List<ChatModel>
        get() = _models.map { it.value }.toList()

    /**
     * 添加模型
     */
    override fun addModel(chatModel: ChatModel): ChatModelMangerImpl {
        _models[chatModel.name] = chatModel
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
        return model.sendMessage(message = message, para = para)
    }

    /**
     * 发送多条消息。若模型不能发送多条则发送messages列表的最后一条
     */
    override fun sendMessages(
        modelName: String,
        para: ChatModel.Para,
        messages: List<RoleMessage>
    ): Flow<String?> {
        val model = _models[modelName] ?: throw IllegalArgumentException("未找到{$modelName}模型")
        return if (model is LongChatModel) {
            model.sendMessages(messages = messages, para = para)
        } else {
            model.sendMessage(message = messages[0].content, para = para)
        }
    }


}