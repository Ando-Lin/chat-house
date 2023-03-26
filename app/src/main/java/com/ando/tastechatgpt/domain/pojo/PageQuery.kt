package com.ando.tastechatgpt.domain.pojo

data class PageQuery(
    val pageSize: Int,
    val page:Int = 1,
    val order:Order = Order.DESC,
    val orderField:String = "timestamp"
    ){

    enum class Order{
        ASC,
        DESC
    }
}