package com.ando.tastechatgpt.domain.pojo

data class PageQuery(
    val pageSize: Int,
    val page:Int,
    val order:Order = Order.DESC,
    val orderField:String = "time"
    ){

    enum class Order{
        ASC,
        DESC
    }
}