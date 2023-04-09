package com.ando.chathouse.exception

class UpdateFailedException:RuntimeException{
    constructor():super(LABEL)
    constructor(cause:Throwable) : super(LABEL, cause)
    constructor(message:String, cause: Throwable):super(message,cause)
    constructor(message: String):super(message)

    companion object{
        const val LABEL = "更新失败"
    }
}