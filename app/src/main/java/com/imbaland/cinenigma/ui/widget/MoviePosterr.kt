package com.imbaland.cinenigma.ui.widget

import android.graphics.RectF
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toAndroidRectF
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toBitmap
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.imbaland.common.tool.TextVisionFinder
import com.imbaland.common.ui.GLBitmapImageView
import com.imbaland.common.ui.util.combine
import com.imbaland.common.ui.util.toComposeRect
import com.imbaland.common.ui.widget.DragWindowLayout
import com.imbaland.common.ui.widget.rememberDragState
import com.imbaland.movies.ui.util.poster
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.sample

@Composable
fun MoviePosterr(modifier: Modifier = Modifier, posterUrl: String) {
    val dragState = rememberDragState()
    var blurGroup by mutableStateOf(PosterBlurGroup())
    LaunchedEffect(dragState) {
        snapshotFlow {
            derivedStateOf {
                if (dragState.isWindowVisible)
                    dragState.windowNormalized
                else
                    Rect.Zero
            }.value
        }
            .sample(33L)
            .collectLatest { window ->
                blurGroup = blurGroup.copy(window = window)
            }
    }
    DragWindowLayout(
        modifier = modifier,
        state = dragState
    ) {
        val overlayPainter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .data(posterUrl)
                .size(Size.ORIGINAL) // Set the target size to load the image at.
                .build()
        )
        val textFinder = remember { TextVisionFinder() }
        LaunchedEffect(overlayPainter.state) {
            textFinder.textBoxFlow
                .collect {
                    blurGroup = blurGroup.copy(text = it.map { box -> box.toComposeRect() }, regen = true)
                }
        }
        when (val imageState = overlayPainter.state) {
            is AsyncImagePainter.State.Success -> {
                val bitmap = imageState.result.drawable.toBitmap()
                textFinder.findTextFlow(bitmap, true)
                AndroidView(
                    modifier = Modifier.fillMaxWidth(1f).poster().background(Color.Yellow),
                    factory = { context ->
                        GLBitmapImageView(context).apply {
                            //setBitmap(BitmapFactory.decodeResource(context.resources, com.imbaland.movies.R.drawable.movie_poster))
                            setBitmap(bitmap)
                        }
                    },
                    onReset = {

                    },
                    onRelease = {
                        it.destroy()
                    },
                    update = {
                        it.addBlurBatch(blurGroup.batch, isNormalized = true, blurGroup.regen)
                        blurGroup.regen = false
                    })
            }

            else -> {

            }
        }
    }
}

data class PosterBlurGroup(
    val window: Rect = Rect.Zero,
    val text: List<Rect> = listOf(),
    val hints: List<Rect> = listOf(),
    var regen: Boolean = true
) {
    val batch: HashMap<String, List<RectF>>
        get() = hashMapOf(
            "window" to listOf(window.toAndroidRectF()),
            "text" to text.map { it.toAndroidRectF() },
            "hints" to hints.map { it.toAndroidRectF() })
}