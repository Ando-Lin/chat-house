package com.ando.tastechatgpt.exception;


class MessageStreamInterruptException: FetchDataException {
    constructor():super(LABEL)
    constructor(cause:Throwable) : super(LABEL, cause)
    constructor(message:String, cause: Throwable):super(message,cause)
    constructor(message: String):super(message)

    companion object{
        const val LABEL = "消息流中断异常"
    }
}
