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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextGeometricTransform
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.sp

@Preview
@Composable
fun TextSelector(
    modifier: Modifier = Modifier,
    text: String = "This is some test text for selecting, isn't that nice? Blah blah blah so much text",
    style: TextStyle = TextStyle.Default.copy(
        fontWeight = FontWeight.Medium,
        lineHeight = 30.sp,
        fontSize = 15.sp
    ),
    limit: Int = 4,
    maxScale: Float = 1.8f,
    highlightColor: Color = Color.Red,
    filter: (String) -> Boolean = { block -> block != "some" },
    onSelected: (IntRange, String, Boolean) -> Unit = { _, _, _ -> }
) {
    val normalStyle = style.copy(
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None
        )
    )
    val selectedStyle = normalStyle.copy(
        textGeometricTransform = TextGeometricTransform(
            scaleX = maxScale
        ), color = Color.Transparent
    )
    val disabledStyle = normalStyle.copy(color = normalStyle.color.copy(alpha = 0.5f))

    val selectedWordsMap = rememberSaveable(text) {
        mutableStateOf(mapOf<IntRange, SelectionType>())
    }
    val wordMap = rememberSaveable(text) {
        "(\\w+)".toRegex().findAll(text).associateBy { result -> result.range }
            .filter {
                if(!filter(it.value.value)) {
                    selectedWordsMap.value += mapOf(it.key to SelectionType.DISABLED)
                    false
                }
                else {
                    true
                }
            }
    }
    var stateTextState by remember(text) {
        mutableStateOf(buildAnnotatedString {
            with(selectedWordsMap.value.entries.sortedBy { it.key.first }) {
                var previous = IntRange(0, 0)
                forEach { current ->
                    withStyle(style = normalStyle.toSpanStyle()) {
                        append(text.substring(previous.last, current.key.first))
                    }
                    withSelectionType(current.value,
                        normalStyle, selectedStyle, disabledStyle,
                        text.substring(current.key.first, current.key.last + 1))
                    previous = IntRange(current.key.first, current.key.last + 1)
                }
                withStyle(style = normalStyle.toSpanStyle()) {
                    append(text.substring(previous.last, text.length))
                }
            }
        })
    }
    Box(modifier = modifier) {
        var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

        fun wordSelected(selectedOffset: Offset, disabled: Boolean = false) {
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
                        var selectionType: SelectionType = if(disabled) SelectionType.DISABLED else SelectionType.SELECTED
                        selectedWordsMap.value =
                            (selectedWordsMap.value.entries.find { entry -> entry.key == selectedWordRange && entry.value == SelectionType.SELECTED }
                                ?.let { existingSelection ->
                                    selectionType = SelectionType.NORMAL
                                    (selectedWordsMap.value + mapOf(selectedWordRange to selectionType))
                                }) ?: (selectedWordsMap.value + mapOf(selectedWordRange to selectionType))
                        onSelected(selectedWordRange, text.substring(selectedWordRange), selectionType == SelectionType.SELECTED)
                        stateTextState = buildAnnotatedString {
                            append(stateTextState.subSequence(0, selectedWordRange.first))
                            withSelectionType(selectionType,
                                normalStyle, selectedStyle, disabledStyle, stateTextState.substring(
                                    selectedWordRange.first,
                                    selectedWordRange.last + 1
                                ))
                            append(
                                stateTextState.subSequence(
                                    selectedWordRange.last + 1,
                                    stateTextState.length
                                )
                            )
                        }
                    }
            }
        }
        Box() {
            Text(
                onTextLayout = { layoutResult = it },
                text = stateTextState,
                style = normalStyle,
                modifier = Modifier.pointerInput(wordMap) {
                    detectTapGestures(onTap = { offset ->
                        wordSelected(offset)
                    })
                })
            selectedWordsMap.value.forEach { wordRangeEntry ->
                if(wordRangeEntry.value == SelectionType.SELECTED) {
                    val word = text.substring(wordRangeEntry.key.first..wordRangeEntry.key.last)
                    val offset = layoutResult?.getBoundingBox(wordRangeEntry.key.first)?.topLeft?.round()!!.plus(
                        IntOffset(0, 0)
                    )
                    val state = wordRangeEntry.value
                    FloatingTextSelection(
                        modifier = Modifier.offset { offset },
                        text = word,
                        style = normalStyle,
                        startScale = if(state == SelectionType.SELECTED) 1.0f else maxScale,
                        targetScale = if(state == SelectionType.SELECTED) maxScale else 1.0f
                    )
                }
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
        modifier = modifier
            .offset {
                IntOffset(
                    ((layoutResult?.size?.width ?: 0).div(2) * (fontScale - 1f)).toInt(), 0
                )
            }
            .scale(fontScale),
        contentAlignment = Alignment.TopCenter,
    ) {
        Text(
            modifier = Modifier,
            onTextLayout = { layoutResult = it },
            style = style,
            color = Color.Red,
            fontWeight = FontWeight(
                (style.fontWeight!!.weight * fontScale).toInt().coerceAtMost(1000)
            ),
            text = text
        )
    }
}

fun AnnotatedString.Builder.withSelectionType(
    selectionType: SelectionType,
    normalStyle: TextStyle,
    selectedStyle: TextStyle,
    disabledStyle: TextStyle,
    string: String) {
    when (selectionType) {
        SelectionType.NORMAL -> {
            withStyle(
                style = normalStyle.toSpanStyle()
            ) {
                append(
                    string
                )
            }
        }

        SelectionType.SELECTED -> {
            withStyle(
                style = selectedStyle.toSpanStyle()
            ) {
                append(
                    string
                )
            }
        }

        SelectionType.DISABLED -> {
            withStyle(
                style = disabledStyle.toSpanStyle()
            ) {
                append(
                    string
                )
            }
        }
    }
}
enum class SelectionType {
    NORMAL, SELECTED, DISABLED
}