package com.imbaland.cinenigma.domain.model

sealed class Hint: GameRound() {
    data object Holder:Hint()
    data class PosterHint(
        val url: String = "",
        val topLeft: Pair<Float, Float> = 0f to 0f,
        val bottomRight: Pair<Float, Float> = 1f to 1f,
        val blurValue: Float = 1f): Hint()
}