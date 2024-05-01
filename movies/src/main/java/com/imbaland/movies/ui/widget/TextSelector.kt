package com.imbaland.movies.ui.widget

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextGeometricTransform
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Preview
@Composable
fun TextSelector(
    modifier: Modifier = Modifier,
    text: String = "This is some test text for selecting, isn't that nice? Blah blah blah so much text",
    style: TextStyle = TextStyle.Default.copy(fontWeight = FontWeight.Medium, lineHeight = 30.sp, fontSize = 15.sp),
    limit: Int = 4,
    maxScale: Float = 1.8f,
    highlightColor: Color = Color.Red,
    filter: (String) -> Boolean = { _ -> true }
) {
    val modStyle = style.copy(lineHeightStyle = LineHeightStyle(alignment = LineHeightStyle.Alignment.Center, trim = LineHeightStyle.Trim.None))

    val wordMap = rememberSaveable(text) {
        "(\\w+)".toRegex().findAll(text).associateBy { result -> result.range }
            .filter { filter(it.value.value) }
    }
    Box(modifier = modifier) {
        var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
        val selectedWords = rememberSaveable {
            mutableStateOf(listOf<IntRange>())
        }
        fun wordSelected(selectedOffset: Offset) {
            layoutResult?.getOffsetForPosition(selectedOffset)?.let { index ->
                wordMap.firstNotNullOfOrNull { entry -> if (entry.key.contains(index)) entry.value.range else null }
                    ?.let { wordRange ->
                        val word = text.substring(wordRange.first..wordRange.last)
                        if (word.isNullOrBlank()) {
                            null
                        } else {
                            wordRange
                        }
                    }?.let { selectedWordRange ->
                    selectedWords.value =
                        (selectedWords.value.find { wordRange -> wordRange == selectedWordRange }
                            ?.let { existingWordRange ->
                                selectedWords.value.filter { it != existingWordRange }
                            }) ?: (selectedWords.value + listOf(selectedWordRange))
                }
            }
        }

        val normalText = remember(wordMap, selectedWords.value) {
            buildAnnotatedString {
                with(selectedWords.value.sortedBy { it.first }) {
                    var previous = IntRange(0, 0)
                    forEach { current ->
                        withStyle(style = modStyle.toSpanStyle()) {
                            append(text.substring(previous.last, current.first))
                        }
                        withStyle(style = modStyle.copy(textGeometricTransform = TextGeometricTransform(scaleX = maxScale), color = Color.Transparent).toSpanStyle()) {
                            append(text.substring(current.first, current.last+1))
                        }
                        previous = IntRange(current.first, current.last+1)
                    }
                    withStyle(style = modStyle.toSpanStyle()) {
                        append(text.substring(previous.last, text.length))
                    }
                }
            }
        }
        Box() {
            Text(
                onTextLayout = { layoutResult = it },
                text = normalText,
                style = modStyle,
                modifier = Modifier.pointerInput(wordMap) {
                    detectTapGestures(onTap = { offset ->
                        wordSelected(offset)
                    })
                })
            selectedWords.value.forEach { wordRange ->
                val word = text.substring(wordRange.first..wordRange.last)
                val offset = layoutResult?.getBoundingBox(wordRange.first)?.topLeft?.round()!!.plus(
                    IntOffset(0, 0)
                )
                FloatingTextSelection(
                    modifier = Modifier.offset { offset },
                    text = word,
                    style = modStyle,
                    startScale = 1.0f,
                    targetScale = maxScale
                )
            }
        }
    }
}
@Preview
@Composable
fun FloatingTextSelection(
    modifier: Modifier = Modifier,
    text: String = "Text",
    style: TextStyle = TextStyle.Default.copy(fontWeight = FontWeight.Medium, lineHeight = 30.sp),
    startScale: Float = 1f,
    targetScale: Float = 2f
) {
    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var fontSize by remember { mutableFloatStateOf(startScale) }
    val fontScale: Float by animateFloatAsState(
        targetValue = fontSize, animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    LaunchedEffect(Unit) {
        fontSize = targetScale
    }
    Box(
        modifier = modifier.offset{ IntOffset(((layoutResult?.size?.width?:0).div(2) * (fontScale-1f)).toInt(),0) }.scale(fontScale),
        contentAlignment = Alignment.TopCenter,) {
        Text(
            modifier = Modifier,
            onTextLayout = { layoutResult = it },
            style = style,
            color = Color.Red,
            fontWeight = FontWeight((style.fontWeight!!.weight * fontScale).toInt().coerceAtMost(1000)),
            text = text
        )
    }

}