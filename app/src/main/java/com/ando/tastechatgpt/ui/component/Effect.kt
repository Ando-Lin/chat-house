package com.ando.tastechatgpt.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ando.tastechatgpt.ext.breathingLight
import com.ando.tastechatgpt.ui.theme.TasteChatGPTTheme

@Preview(showBackground = true)
@Composable
fun EffectTest() {
    TasteChatGPTTheme {
//        val transition = rememberInfiniteTransition()
//        val animateFloat by transition.animateFloat(
//            initialValue = 0.55f,
//            targetValue = 0.85f,
//            animationSpec = infiniteRepeatable(
//                animation = tween(durationMillis = 1000, easing = LinearEasing),
//                repeatMode = RepeatMode.Reverse
//            )
//        )
        val breathingLightState = rememberBreathingLightState()
        Box(
            modifier = Modifier
                .size(width = 100.dp, height = 60.dp)
                .breathingLight(breathingLightState)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(15.dp)
                )
        )
    }
}



@Composable
fun rememberBreathingLightState(
    initialMotion: Motion = Motion.RUN,
    finalValue: Float = 1f,
    initialValue: Float = 0.45f,
    targetValue: Float = 0.88f,
): BreathingLightState {
    val runState = rememberSaveable {
        mutableStateOf(initialMotion == Motion.RUN)
    }
    val transition = if (runState.value) rememberInfiniteTransition() else null
    val animateFloatState = transition?.animateFloat(
        initialValue = initialValue,
        targetValue = targetValue,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    return rememberSaveable(animateFloatState, saver = BreathingLightState.Saver()) {
        BreathingLightState(animateFloatState, initialMotion, runState, finalValue)
    }
}


enum class Motion {
    RUN, END
}

class BreathingLightState internal constructor(
    floatState: State<Float>?,
    initialMotion: Motion,
    private val runState: MutableState<Boolean>,
    private val finalValue: Float
) {
    val state = floatState ?: mutableStateOf(finalValue)
    private var _motion = initialMotion
    val motion
        get() = _motion


    fun end() {
        _motion = Motion.END
        runState.value = false
    }

    fun run() {
        _motion = Motion.RUN
        runState.value = true
    }

    companion object {
        fun Saver():Saver<BreathingLightState, Any>{
            val motion = "m"
            val state = "s"
            val runState = "rs"
            val finalValue = "fv"
            return mapSaver<BreathingLightState>(
                save = {
                    mapOf(
                        state to it.state.value,
                        motion to it.motion,
                        runState to it.runState.value,
                        finalValue to it.finalValue
                    )
                },
                restore = {
                    BreathingLightState(
                        it[state] as State<Float>,
                        it[motion] as Motion,
                        it[runState] as MutableState<Boolean>,
                        it[finalValue] as Float
                    )
                }
            )
        }
    }

}

