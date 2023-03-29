package com.ando.chathouse.exception

class NoSuchStrategy:RuntimeException {
    constructor():super(LABEL)
    constructor(cause:Throwable) : super(LABEL, cause)
    constructor(message:String, cause: Throwable):super(message,cause)
    constructor(message: String):super(message)

    companion object {
        const val LABEL = "找不到策略"
    }
}