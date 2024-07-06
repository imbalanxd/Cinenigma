package com.imbaland.common.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.CacheDrawScope
import androidx.compose.ui.draw.DrawResult
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.MutableRect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun DragWindowLayout(
    modifier: Modifier,
    enabled: Boolean = true,
    state: DragState = rememberDragState(),
    onDrawWindow: CacheDrawScope.() -> DrawResult = {
        onDrawBehind {
            this.drawRoundRect(
                Color.Red,
                Offset(0f, 0f),
                Size(this.size.width, this.size.height),
                CornerRadius(5f, 5f),
                Stroke(5.0f)
            )
        }
    },
    content: @Composable BoxScope.() -> Unit = {}
) {
    val density = LocalDensity.current.density
    Box(modifier.let {
        if (enabled) {
            it.pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    if (state.isWindowVisible) {
                        state.translate(pan.x, pan.y)
                        state.adjustSize(
                            (zoom - 1f) * state.bounds.width / 2f,
                            (zoom - 1f) * state.bounds.height / 2f
                        )
                    }
                }
            }.pointerInput(Unit) {
                detectTapGestures(onTap = {
                    state.setVisible(!state.isWindowVisible)
                })
            }
        } else {
            it
        }.let{ clipping ->
            if(state.clipToWindow) {
                clipping.drawWithContent {
                    val clipActive = state.isWindowVisible && state.clipToWindow
                    val offset = if(clipActive) Offset(state.window.left, state.window.top) else Offset(0f,0f)
                    val size = if(clipActive) state.window.size else this.size
                    drawRect(
                        topLeft = offset,
                        size = size,
                        blendMode = BlendMode.SrcOut,
                        color = Color.Transparent
                    )
                }
            } else {
                clipping
            }
        }
    }.onSizeChanged { newSize ->
        if (enabled) {
            state.updateBounds(Size(newSize.width.toFloat(), newSize.height.toFloat()))
        }
    }, contentAlignment = Alignment.TopStart
    ) {
        content()
        if (state.isWindowVisible) {
            with(state.window) {
                Box(
                    Modifier
                        .offset { IntOffset((left).roundToInt(), (top).roundToInt()) }
                        .size((width / density).dp, (height / density).dp)
                        .drawWithCache {
                            onDrawWindow()
                        }
                )
            }
        }
    }
}

class DragState(
    window: Rect = Rect(0f, 0f, 0f, 0f),
    isWindowVisible: Boolean = !window.isEmpty,
    clipToWindow: Boolean = false
) {
    internal var bounds: Size = Size(0f, 0f)
    val isWindowVisible: Boolean get() = _isWindowVisible
    val clipToWindow: Boolean get() = _clipToWindow

    private val _window = mutableStateOf(
        Rect(
            window.left,
            window.top,
            window.right,
            window.bottom
        )
    )
    val window by _window
    val windowNormalized by derivedStateOf {
        with(_window.value) {
            Rect(left/bounds.width,top/bounds.height,right/bounds.width,bottom/bounds.height)
        }
    }
    private var _isWindowVisible by mutableStateOf(isWindowVisible)
    private var _clipToWindow by mutableStateOf(clipToWindow)

    private fun updateWindow(newWindow: Rect) {
        _window.value = keepRectInside(newWindow)
    }

    private fun keepRectInside(newWindow: Rect): Rect {
        var deltaX = 0f
        var deltaY = 0f
        var deltaWidth = 0f
        var deltaHeight = 0f
        if (newWindow.width > bounds.width) {
            deltaWidth = bounds.width - newWindow.width
        } else if (newWindow.left < 0f) {
            deltaX = -newWindow.left
        } else if (newWindow.right > bounds.width) {
            deltaX = bounds.width - newWindow.right
        }

        if (newWindow.height > bounds.height) {
            deltaHeight = bounds.height - newWindow.height
        }
        if (newWindow.top < 0f) {
            deltaY = -newWindow.top
        } else if (newWindow.bottom > bounds.height) {
            deltaY = bounds.height - newWindow.bottom
        }
        return newWindow.translate(deltaX, deltaY).let { rect ->
            rect.copy(
                right = rect.left + rect.width + deltaWidth,
                bottom = rect.top + rect.height + deltaHeight
            )
        }
    }

    fun translate(x: Float, y: Float) {
        updateWindow(window.translate(x, y))
    }

    fun setWindow(window: Rect) {
        updateWindow(window)
    }

    fun adjustSize(width: Float, height: Float) {
        updateWindow(
            Rect(
                window.left,
                window.top,
                window.right + width,
                window.bottom + height
            )
        )
    }

    fun setVisible(visible: Boolean) {
        _isWindowVisible = visible
    }

    fun clipToWindow(clip: Boolean) {
        _clipToWindow = clip
    }

    private val DEFAULT_WINDOW_SIZE = 0.5f
    internal fun updateBounds(bounds: Size) {
        this.bounds = bounds
        if (_window.value.isEmpty) {
            _window.value = Rect(
                Offset(
                    this.bounds.width / 2f,
                    this.bounds.height / 2f
                ),
                Size(
                    this.bounds.width * DEFAULT_WINDOW_SIZE,
                    this.bounds.height * DEFAULT_WINDOW_SIZE
                )
            )
        }
    }

    companion object {
        val Saver: Saver<DragState, *> = listSaver(
            save = {
                listOf(
                    it.window.left,
                    it.window.top,
                    it.window.right,
                    it.window.bottom,
                    it.isWindowVisible,
                    it.bounds.width,
                    it.bounds.height
                )
            },
            restore = {
                DragState(
                    window = Rect(it[0] as Float, it[1] as Float, it[2] as Float, it[3] as Float),
                    isWindowVisible = it[4] as Boolean
                ).apply { adjustSize(it[5] as Float, it[6] as Float) }
            }
        )
    }
}

@Composable
fun rememberDragState(
    initialWindow: Rect = Rect(0f, 0f, 0f, 0f),
    isWindowVisible: Boolean = !initialWindow.isEmpty,
    clipToWindow: Boolean = false
): DragState {
    return rememberSaveable(saver = DragState.Saver) {
        DragState(
            initialWindow, isWindowVisible, clipToWindow
        )
    }
}