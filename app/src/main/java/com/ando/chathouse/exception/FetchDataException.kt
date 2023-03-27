package com.ando.chathouse.exception

open class FetchDataException: RuntimeException{
    constructor():super(LABEL)
    constructor(cause:Throwable) : super(LABEL, cause)
    constructor(message:String, cause: Throwable):super(message,cause)
    constructor(message: String):super(message)

    companion object{
        const val LABEL:String = "拉取数据异常"
    }
}