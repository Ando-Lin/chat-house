package com.ando.chathouse.domain.pojo

data class ChatContext (
    val myUid: Int,
    val order: Order = Order.TIME_DESC,
){
    enum class Order{
        TIME_DESC
    }
}
