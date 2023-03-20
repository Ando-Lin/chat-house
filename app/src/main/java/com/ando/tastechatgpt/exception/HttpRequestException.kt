package com.ando.tastechatgpt.exception

class HttpRequestException:RuntimeException {
    constructor():super(LABEL)
    constructor(cause:Throwable) : super(LABEL, cause)
    constructor(message:String, cause: Throwable):super(message,cause)
    constructor(message: String):super(message)
    companion object{
        const val LABEL = "HTTP请求异常"
    }
}