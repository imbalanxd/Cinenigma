package com.imbaland.cinenigma.domain.model

import java.util.Date

sealed class Hint(type: String = HintType.Holder()): GameRound(type) {
    data object Holder:Hint(HintType.Holder())
    data class PosterHint(
        val url: String = "",
        val topLeft: Pair<Float, Float> = 0f to 0f,
        val bottomRight: Pair<Float, Float> = 1f to 1f,
        val blurValue: Float = 1f): Hint(HintType.Poster())
    data class KeywordHint(val keyword: String = "", val start: Int = 0, val end: Int = 0): Hint(HintType.Keyword())
}

enum class HintType {
    Poster, Keyword, Holder
}
operator fun HintType.invoke(): String {
    return this.name
}