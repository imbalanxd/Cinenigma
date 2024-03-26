package com.imbaland.common.ui.widget

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun TapDrag(modifier: Modifier, content: @Composable BoxScope.(box: Rect?) -> Unit) {
    val density = LocalDensity.current.density
    var selecting by rememberSaveable{ mutableStateOf(false) }
    var resolution by remember { mutableStateOf(Size(0f,0f)) }
    var size by rememberSaveable { mutableFloatStateOf(resolution.minDimension.coerceAtLeast(60f)/3f) }
    var offsetX by rememberSaveable { mutableFloatStateOf(0f) }
    var offsetY by rememberSaveable { mutableFloatStateOf(0f) }
    Box(modifier
        .pointerInput(Unit) {
            detectTransformGestures { _, pan, zoom, _ ->
                if(selecting) {
                    val increase = (size * (zoom - 1)) / 2.0f
                    size += increase
                    offsetX = (offsetX + pan.x - increase)
                    offsetY = (offsetY + pan.y - increase)
                }
            }
        }
        .pointerInput(Unit) {
            detectTapGestures(onTap = {
                selecting = !selecting
            })
        }
        .onSizeChanged { newSize ->
            resolution = Size(newSize.width.toFloat(), newSize.height.toFloat())
            size = resolution.minDimension.coerceAtLeast(60f)/(2f)
            offsetX = resolution.width / (2.0f) - size / 2.0f
            offsetY = resolution.height / (2.0f) - size / 2.0f
        }, contentAlignment = Alignment.TopStart) {
        val sizeDim = size
        content(if(!selecting) null else Rect(Offset(offsetX, offsetY), Size(sizeDim, sizeDim)))
        if(!resolution.isEmpty() && selecting) {
            Box(
                Modifier
                    .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                    .drawWithCache {
                        onDrawBehind {
                            this.drawRoundRect(
                                Color.Blue,
                                Offset(0f, 0f),
                                Size(this.size.width, this.size.height),
                                CornerRadius(5f,5f),
                                Stroke(5f))
                        }
                    }
                    .size((size/density).dp)
            )
        }
    }
}