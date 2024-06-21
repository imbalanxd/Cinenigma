package com.imbaland.movies.ui.util

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.Modifier

fun Modifier.poster(): Modifier {
    return this.aspectRatio(2/3f)
}