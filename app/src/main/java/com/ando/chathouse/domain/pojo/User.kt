package com.ando.chathouse.domain.pojo

import android.net.Uri

data class User(
    val id: Int,
    val name: String,
    val avatar: Uri?,
    val description:String
){
    companion object{
        val emptyUser by lazy { User(id = 0, name = "", description = "", avatar = null) }
    }
}


