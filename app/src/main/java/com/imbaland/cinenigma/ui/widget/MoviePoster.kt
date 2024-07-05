package com.imbaland.cinenigma.ui.widget

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.imbaland.movies.R

@Preview
@Composable
fun MoviePoster(modifier: Modifier = Modifier,
                 imageUrl: String = "",
                 hints: List<Rect> = listOf()) {
    val painter  = painterResource(R.drawable.movie_poster)
    Image(modifier = modifier, contentDescription = "", painter = painter)
    Box(modifier = modifier.drawWithCache {
        onDrawWithContent {
            translate(0f,0f) {
                val width = painter.intrinsicSize.width
                val height = painter.intrinsicSize.height
                clipRect (left = 0f, top = 0f, right = width, bottom = height) {
                    with(painter) {
                        draw(Size(width/5,height/5))
                    }
                }
            }
        }
    }.blur(100.dp))
    Surface {  }
//    hints.forEach {
//        Image(painter = rememberPain)
//    }
}