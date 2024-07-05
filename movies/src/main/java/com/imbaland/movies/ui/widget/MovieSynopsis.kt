package com.imbaland.movies.ui.widget

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.imbaland.common.ui.widget.TextSelector
import com.imbaland.movies.R

@Composable
fun MovieOverview(
    modifier: Modifier = Modifier,
    fullText: String = "",
    keywords: List<IntRange> = listOf(),
    onSubmit: (List<Pair<String, IntRange>>) -> Unit = { _ -> },
    filter: (String) -> Boolean = { _ -> true }
) {
    val synopsisHint = remember { mutableListOf<Pair<String, IntRange>>() }
    ConstraintLayout(modifier = modifier) {
        val (submit, selector) = createRefs()
        IconButton(
            modifier = Modifier.size(30.dp)
                .constrainAs(submit) {
                    top.linkTo(selector.top)
                    end.linkTo(selector.start)
                },
            onClick = {
                if (synopsisHint.isNotEmpty()) {
                    onSubmit(synopsisHint)
                }
            }) {
            Icon(
                painter = painterResource(R.drawable.ic_confirm),
                contentDescription = "Localized description"
            )
        }
        TextSelector(
            modifier = Modifier.fillMaxWidth(0.8f)
                .constrainAs(selector) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            text = fullText,
            style = TextStyle.Default.copy(
                fontWeight = FontWeight.Medium,
                lineHeight = 26.sp,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            ),
            limit = 1,
            enabled = true,
            maxScale = 1.8f,
            filter = filter,
            selected = keywords,
            onSelected = { range, word, selected ->
                if (selected)
                    synopsisHint.add(
                        Pair(
                            word,
                            range
                        )
                    )
                else
                    synopsisHint.removeIf { pair -> pair.second == range }
            })
    }
}

@Composable
fun KeywordDisplay(
    modifier: Modifier = Modifier,
    fullText: String = "",
    keywords: List<IntRange> = listOf()
) {
    TextSelector(
        modifier = Modifier.fillMaxWidth(0.7f)
            .border(1.dp, Color.Blue, RoundedCornerShape(4.dp))
            .padding(3.dp),
        text = fullText,
        style = TextStyle.Default.copy(
            color = Color.Transparent,
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
            textAlign = TextAlign.Center
        ),
        enabled = false,
        selected = keywords,
        maxScale = 1.8f,
    )
}