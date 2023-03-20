package com.ando.tastechatgpt.ext

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.ando.tastechatgpt.ui.component.BreathingLightState

fun Modifier.withCondition(condition:Boolean, modifier: Modifier) =
    if (condition) this.then(modifier) else this

fun Modifier.withCondition(condition:Boolean, modifier: Modifier.()->Modifier) =
    if (condition) this.modifier() else this

fun Modifier.breathingLight(breathingLightState: BreathingLightState) =
    this.graphicsLayer { alpha = breathingLightState.state.value }