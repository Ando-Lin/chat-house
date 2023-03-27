package com.ando.chathouse.exception

class NoSuchChatException:FetchDataException {
    constructor():super(LABEL)
    constructor(cause:Throwable) : super(LABEL, cause)
    constructor(message:String, cause: Throwable):super(message,cause)
    constructor(message: String):super(message)
    constructor(id:Int):super("${LABEL}: $id")
    constructor(id:Int, cause: Throwable):super("${LABEL}: $id", cause)

    companion object {
        const val LABEL = "找不到该对话"
    }
}