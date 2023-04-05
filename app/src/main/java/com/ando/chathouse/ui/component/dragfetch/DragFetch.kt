package com.ando.chathouse.ui.component.dragfetch

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.platform.inspectable
import androidx.compose.ui.unit.Velocity

fun Modifier.dragFetch(
    state: DragFetchState,
    enabled: Boolean = true
) = inspectable(inspectorInfo = debugInspectorInfo {
    name = "dragFetch"
    properties["state"] = state
    properties["enabled"] = enabled
}) {
    Modifier.dragFetch(state.threshold,state::onDrag, { state.onRelease() }, enabled)
}

fun Modifier.dragFetch(
    sign: Float,
    onDrag: (pullDelta: Float) -> Float,
    onRelease: suspend (flingVelocity: Float) -> Unit,
    enabled: Boolean = true
) = inspectable(inspectorInfo = debugInspectorInfo {
    name = "dragFetch"
    properties["onDrag"] = onDrag
    properties["onRelease"] = onRelease
    properties["enabled"] = enabled
}){
    Modifier.nestedScroll(DragFetchNestedScrollConnection(sign ,onDrag, onRelease, enabled))
}

private class DragFetchNestedScrollConnection(
    private val currentSign: Float,
    private val onDrag: (pullDelta: Float) -> Float,
    private val onRelease: suspend (flingVelocity: Float) -> Unit,
    private val enabled: Boolean
) : NestedScrollConnection {
    /**
     * 前处理则外部滑动优先处理
     * y>0即下滑
     */
    override fun onPreScroll(
        available: Offset, source: NestedScrollSource
    ): Offset = when {
        !enabled -> Offset.Zero
        source == NestedScrollSource.Drag && available.y*currentSign<0 -> Offset(
            x = 0f,
            y = onDrag(available.y)
        )
        else -> Offset.Zero
    }

    /**
     * 后处理则内部滑动优先处理
     * y<0即上提
     */
    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset = when {
        !enabled -> Offset.Zero
        source == NestedScrollSource.Drag && available.y*currentSign>0 -> Offset(
            x = 0f,
            y = onDrag(available.y)
        )
        else -> Offset.Zero
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        onRelease(available.y)
        return super.onPreFling(available)
    }

}
