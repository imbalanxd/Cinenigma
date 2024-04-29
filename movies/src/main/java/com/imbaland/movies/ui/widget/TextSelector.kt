package com.imbaland.movies.ui.widget

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.sp

@Composable
fun TextSelector(
    modifier: Modifier = Modifier,
    text: String = "This is some test text for selecting, isn't that nice? Blah blah blah so much text",
    style: TextStyle = TextStyle.Default.copy(fontWeight = FontWeight.Medium),
    limit: Int = 4,
    maxScale: Float = 2.0f,
    highlightColor: Color = Color.Red,
    filter: (String) -> Boolean = { _ -> true }
) {
//    var textState by rememberSaveable {
//        mutableStateOf(text)
//    }
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

        val normalText = buildAnnotatedString {
//            currentWord?.let { wordRange ->
//                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp,)) {
//                    append(text.substring(0 until wordRange.first))
//                }
//                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Transparent)) {
//                    append(text.substring(wordRange))
//                }
//                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp,)) {
//                    append(text.substring(wordRange.last+1 until text.length))
//                }
//            }?:
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp)) {
                append(text)
            }
        }
        Box() {
            Text(
                onTextLayout = { layoutResult = it },
                text = normalText,
                style = style,
                modifier = Modifier.pointerInput(wordMap) {
                    detectTapGestures(onTap = { offset ->
                        wordSelected(offset)
                    })
                })
            selectedWords.value.forEach { wordRange ->
                val word = text.substring(wordRange.first..wordRange.last)
                val offset = layoutResult?.getBoundingBox(wordRange.first)?.topLeft?.round()!!
                FloatingTextSelection(
                    modifier = Modifier.offset { offset },
                    text = word,
                    style = style
                )
            }
        }
    }
}

@Composable
fun FloatingTextSelection(
    modifier: Modifier,
    text: String,
    style: TextStyle
) {
    var fontSize by remember { mutableFloatStateOf(1f) }
    val fontScale: Float by animateFloatAsState(
        targetValue = fontSize, animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    LaunchedEffect(Unit) {
        fontSize = 1.8f
    }
    Text(
        modifier = modifier.scale(fontScale),
        style = style,
        color = Color.Red,
        fontWeight = FontWeight((style.fontWeight!!.weight * fontScale).toInt().coerceAtMost(1000)),
        text = text
    )
}