package com.imbaland.movies.ui.widget

import android.graphics.Rect
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toAndroidRectF
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.times
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.imbaland.common.tool.TextVisionFinder
import com.imbaland.common.ui.shader.pixelate
import com.imbaland.common.ui.widget.TapDrag
import kotlin.math.pow

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun MoviePoster(modifier: Modifier = Modifier, imageUrl: String) {
    TapDrag(
        modifier = modifier
    ) { box ->
        var imageWidth by remember { mutableFloatStateOf(0f) }
        var viewWidth by remember { mutableFloatStateOf(0f) }
        var scaleRatio by remember(imageWidth, viewWidth) {
            mutableFloatStateOf(viewWidth.coerceAtLeast(1f)/imageWidth.coerceAtLeast(1f))
        }
        val textFinder = remember { TextVisionFinder() }
        var textRects = remember {
            mutableStateOf(listOf<Rect>())
        }
        LaunchedEffect(Unit) {
            textFinder.textBoxFlow
                .collect {
                    textRects.value = it.map { rect -> rect }
                }
        }
        Image(
            modifier = Modifier
                .onSizeChanged { size ->
                    viewWidth = size.width.toFloat()
                }
                .fillMaxWidth()
                .run {
                    box
                        ?.toAndroidRectF()
                        ?.times(1 / scaleRatio)
                        ?.let { dragBox ->
                            this.pixelate((box.maxDimension / scaleRatio / 27.0f * if (textRects.value.find { textBox ->
                                    with(dragBox) {
                                        textBox.intersects(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
                                    }
                                } != null) 2.5f else 1f).pow(1.12f),
                                box.topLeft,
                                box.size)
                        } ?: this
                },
            contentScale = ContentScale.FillWidth,
            painter = rememberAsyncImagePainter(
                model = imageUrl,
                onState = { state ->
                    when (state) {
                        AsyncImagePainter.State.Empty -> {}
                        is AsyncImagePainter.State.Error -> {}
                        is AsyncImagePainter.State.Loading -> {}
                        is AsyncImagePainter.State.Success -> {
                            state.result.drawable.let {
                                imageWidth = it.intrinsicWidth.toFloat()
                                textFinder.findTextFlow(it.toBitmap())
                            }
                        }
                    }
                }
            ),
            contentDescription = "contentDescription",
        )
//                            Text rect debugging
//                            Canvas(modifier = Modifier) {
//                                textRects.value.forEach { rect ->
//                                    rect.toRectF().times(scaleRatio).toRect().toComposeRect().let {
//                                        drawRect(Color(.7f, 0f, .6f, .8f), it.topLeft, it.size)
//                                    }
//                                }
//                            }
    }
}