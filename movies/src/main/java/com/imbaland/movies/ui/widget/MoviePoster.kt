package com.imbaland.movies.ui.widget

import android.graphics.Rect
import android.graphics.RectF
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toAndroidRectF
import androidx.compose.ui.graphics.toComposeRect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.times
import androidx.core.graphics.toRect
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.imbaland.common.tool.TextVisionFinder
import com.imbaland.common.ui.shader.pixelate
import com.imbaland.common.ui.widget.TapDrag
import com.imbaland.movies.R
import kotlin.math.pow

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun MoviePoster(
    modifier: Modifier = Modifier,
    imageUrl: String,
    enabled: Boolean = true,
    clipToSelection: Boolean = false,
    rect: Rect? = null,
    onSubmit: ((Rect, Float) -> Unit)? = null
) {
    TapDrag(
        modifier = modifier,
        enabled = enabled,
        rect?.toComposeRect(),
    ) { box, blur ->
        var imageWidth by remember { mutableFloatStateOf(0f) }
        var viewWidth by remember { mutableFloatStateOf(0f) }
        var scaleRatio by remember(imageWidth, viewWidth) {
            mutableFloatStateOf(viewWidth.coerceAtLeast(1f) / imageWidth.coerceAtLeast(1f))
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
        if (box != null && onSubmit != null) {
            IconButton(
                modifier = Modifier.size(30.dp).zIndex(10f),
                onClick = { onSubmit(box.toAndroidRectF().toRect(), 1f) }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_confirm),
                    contentDescription = "Localized description"
                )
            }
        }
        Image(
            modifier = Modifier
                .onSizeChanged { size ->
                    viewWidth = size.width.toFloat()
                }
                .fillMaxWidth()
                .aspectRatio(2/3f)
                .run {
                    box
                        ?.toAndroidRectF()
                        ?.times(1 / scaleRatio)
                        ?.let { dragBox ->
                            val shape =
                                GenericShape { size: Size, layoutDirection: LayoutDirection ->
                                    addRect(box)
                                }
                            this.pixelate((box.maxDimension / scaleRatio / 27.0f * if (textRects.value.find { textBox ->
                                    with(dragBox) {
                                        textBox.intersects(
                                            left.toInt(),
                                            top.toInt(),
                                            right.toInt(),
                                            bottom.toInt()
                                        )
                                    }
                                } != null) 2.5f else 1f).pow(1.12f),
                                box.topLeft,
                                box.size).let {
                                    if(clipToSelection) {
                                        it.clip(shape)
                                    } else it
                            }
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
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun MoviePosterHint(modifier: Modifier = Modifier, poster: String, onSubmit: ((Rect, Float) -> Unit)? = null) {
    MoviePoster(
        modifier = Modifier.fillMaxWidth(0.7f),
        imageUrl = poster,
        clipToSelection = false,
        onSubmit = onSubmit
    )
}
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun MoviePosterGuess(modifier: Modifier = Modifier, poster: String, area: RectF) {
    Box(
        modifier = Modifier.fillMaxWidth(0.7f)
            .border(1.5.dp, Color.Blue, RoundedCornerShape(5.dp)),
        contentAlignment = Alignment.Center
    ) {
        MoviePoster(
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            imageUrl = poster,
            clipToSelection = true,
            rect = area.toRect()
        )
    }
}