package com.ando.tastechatgpt.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.CoroutineScope

/**
 * 当key产生变化是才启动block，若产生重组而key没有变化则不执行
 * LaunchedEffect是每次重组都执行一次block,当key变化是也执行block
 */
@Composable
fun LaunchedKeyEffect(
    key1: Any?,
    block: suspend CoroutineScope.() -> Unit
) {
    val firstCompose = remember {
        mutableStateOf(true)
    }
    LaunchedEffect(key1){
        if (!firstCompose.value){
            block()
        }else{
            firstCompose.value = false
        }
    }
}