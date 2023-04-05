package com.ando.chathouse.ui.component.dragfetch

import androidx.compose.animation.core.animate
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.pow


@Composable
fun rememberDragFetchState(
    fetching: Boolean,
    onFetch: () -> Unit,
    dragData: DragData = DragFetchDefaults.dragData
): DragFetchState {
    require(dragData.dragThreshold != 0.dp) {
        "阈值不得为0"
    }

    val scope = rememberCoroutineScope()
    val onFetchState = rememberUpdatedState(newValue = onFetch)
    val thresholdPx: Float
    val dragOffsetPx: Float

    LocalDensity.current.run {
        thresholdPx = dragData.dragThreshold.toPx()
        dragOffsetPx = dragData.dragOffset.toPx()
    }

    val state = remember(scope) {
        DragFetchState(
            scope,
            onFetchState,
            DragFetchData(
                dragThreshold = thresholdPx,
                dragOffset = dragOffsetPx,
                maxOverDragPercent = dragData.maxOverDragPercent
            )
        )
    }

    SideEffect {
        state.setFetching(fetching)
    }

    return state
}

class DragFetchState internal constructor(
    private val animationScope: CoroutineScope,
    private val onFetchState: State<() -> Unit>,
    private val dragFetchData: DragFetchData,
) {
    private val progress get() = adjustedDistanceDraged / threshold

    private var _fetching by mutableStateOf(false)
    private var _position by mutableStateOf(0f)
    private var distanceDraged by mutableStateOf(0f)

    val threshold = dragFetchData.dragThreshold
    private var dragOffset = dragFetchData.dragOffset
    private var maxOverDragPercent = dragFetchData.maxOverDragPercent

    private val adjustedDistanceDraged by derivedStateOf { distanceDraged * DragMultiplier }

    val fetching get() = _fetching
    val position get() = _position

    internal fun onRelease() {
        if (!this._fetching) {
            if (abs(adjustedDistanceDraged) > abs(threshold)) {//释放后若调整的距离大于阈值则执行
                onFetchState.value()
            } else {
                animateIndicatorTo(0f)
            }
        }
        distanceDraged = 0f
    }


    internal fun onDrag(dragDelta: Float): Float {
        if (this._fetching) return 0f

//        if (dragDelta * threshold > 0) {
//            return 0f
//        }

        val newOffset =
            if (threshold > 0) (distanceDraged + dragDelta).coerceAtLeast(0f)
            else (distanceDraged + dragDelta).coerceAtMost(0f)
        val dragConsumed = newOffset - distanceDraged
        distanceDraged = newOffset
        _position = calculateIndicatorPosition()
        return dragConsumed
    }

    internal fun setFetching(fetching: Boolean) {
        if (this._fetching != fetching) {
            this._fetching = fetching
            this.distanceDraged = 0f
            animateIndicatorTo(if (fetching) dragOffset else 0f)
        }
    }


    private fun animateIndicatorTo(offset: Float) = animationScope.launch {
        animate(initialValue = _position, targetValue = offset) { value, _ ->
            _position = value
        }
    }

    private fun calculateIndicatorPosition(): Float = when {
        //当拖拽的距离没超过阈值时按拖拽的距离算。负数比较选择>=
        abs(adjustedDistanceDraged) <= abs(threshold) -> adjustedDistanceDraged
        else -> {
            // How far beyond the threshold pull has gone, as a percentage of the threshold.
            val overshootPercent = abs(progress) - 1.0f
            // Limit the overshoot to 200%. Linear between 0 and 200.
            val linearTension = overshootPercent.coerceIn(0f, maxOverDragPercent)
            // Non-linear tension. Increases with linearTension, but at a decreasing rate.
            val tensionPercent = linearTension - linearTension.pow(2) / 4
            // The additional offset beyond the threshold.
            val extraOffset = threshold * tensionPercent
            threshold + extraOffset
        }
    }
}

object DragFetchDefaults {
    val dragData = DragData(dragThreshold = 60.dp, dragOffset = 40.dp)
}

data class DragData(
    val dragThreshold: Dp,
    val dragOffset: Dp,
    val maxOverDragPercent: Float = 0f,
)

internal data class DragFetchData(
    val dragThreshold: Float,
    val dragOffset: Float,
    val maxOverDragPercent: Float = 0f,
)


private const val DragMultiplier = 0.5f